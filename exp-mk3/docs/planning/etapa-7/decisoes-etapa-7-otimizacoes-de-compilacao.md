# Decisoes de Planejamento da Etapa 7 - Otimizacoes de Compilacao

Este documento consolida as decisoes tomadas durante o planejamento da Etapa 7 do `exp-mk3`, depois do fechamento do M2 na Etapa 6. Ele registra o estado final da arvore de decisoes e substitui premissas conflitantes do plano historico.

## Autoridade e Revalidacao

- ADRs aceitos, `CONTEXT.md`, Corpus de Expressoes e planos detalhados vigentes sao normativos.
- O plano historico e o codigo atual sao evidencias e substrato, nao autoridade quando contradizem contratos posteriores.
- O ADR 0019 e produto desta etapa e passa a ser normativo para toda transformacao de plano.
- A API publica continua provisoria e nao exige compatibilidade antes da GA.
- Nenhum ticket deve ser publicado durante esta sessao; decomposicao em issues ocorre depois do design registrado.

## Enquadramento da Etapa 7

- A Etapa 7 e a primeira etapa em que o plano executado pode divergir estruturalmente do que a Etapa 5 construiu.
- O produto e um conjunto pequeno de transformacoes provadas, mais o mecanismo de prova que as etapas 8, 9 e 13 vao reutilizar.
- Nenhuma transformacao muda semantica de valor, escala, arredondamento, dominio, falha, ordem ou efeito.
- Toda transformacao e opcional por construcao: existe sempre uma forma sem otimizacoes gerada pela mesma pipeline.
- A Etapa 7 nao especializa nos por operador e tipo, nao remove reflexao, nao cria cache de compilacao, nao implementa Memoria de Calculo e nao funde pipelines de colecao.

## Escopo

Dentro do escopo:

- Dobra de Constante completa: binario, unario, postfix, `between` constante, `??` com esquerda constante nao nula, funcao `foldable`, colecao constante, condicional com condicao constante e prefixo de navegacao puro.
- Dobra de leitura de Simbolo Externo nao sobrescrevivel, que e o unico caso em que uma leitura de simbolo e constante.
- Elisao de Assercao quando o tipo ja foi provado.
- `in` com lado direito constante baixado para estrutura de busca.
- `not not x` reduzido para `x`.
- Subexpressao Comum Memoizada, condicionada a medicao.

Fora do escopo, com motivo registrado:

- Eliminacao de atribuicao morta. Uma atribuicao so e morta para a visao de resultado; `computeAssignedValues` le todo slot para a visao de atribuicoes, e a Etapa 9 exige um unico plano por `(source, environmentId)` compartilhado entre visoes. Eliminacao dependente de visao e plano unico nao podem valer juntos. O item sai da Etapa 7 e o plano-mestre e corrigido.
- Reordenacao segura de curto-circuito. Sem perfil que mostre ganho, e risco de equivalencia por beneficio nao medido. Fica registrada como candidata da Etapa 8.
- Reescritas de `%`, de potencia, de raiz e de identidades aritmeticas. Elas nao sao presumidas validas sob `MathContext` e ADR 0017, e nenhuma delas tem prova de equivalencia em valor, escala e dominio disponivel nesta etapa.

## Mecanismo de Transformacao

- A `PlanTransformation` declarada na Etapa 5 como `ExecutionPlan -> ExecutionPlan` e inaplicavel: `ExecutableNode` e interface simples, sem protocolo de travessia e de reconstrucao, e implementar esse protocolo em toda a familia de nos seria custo pago apenas para reconstruir o que o construtor de plano acabou de construir.
- A transformacao passa a acontecer **na construcao do plano**, com o construtor operando em dois modos e um unico campo booleano interno de folding. Um enum de modo seria cerimonia para dois valores.
- Os dois pontos de entrada sao `build`, que e o plano otimizado, e `buildOracle`, que e o Oraculo Sem Otimizacoes. O nome `buildUnoptimized` e renomeado para `buildOracle` porque a partir daqui a forma sem otimizacoes existe para provar, nao apenas para preceder.
- A `PlanTransformation` e removida junto com a lista vazia de transformacoes instaladas.
- O teste de equivalencia de pipeline da Etapa 5 inverte de significado: ele deixa de afirmar que `build` e a forma sem otimizacoes produzem a mesma forma e passa a afirmar que produzem o mesmo valor e a mesma falha.
- A selecao do oraculo permanece interna ao modulo. Nao existe flag publica, propriedade de sistema, nem runtime duplicado.
- Premissa invalidada da Etapa 5: o desenho `plano sem otimizacoes -> transformacoes opcionais -> plano executado`, registrado no plano detalhado daquela etapa e materializado na `PlanTransformation`, lia a transformacao como passe posterior sobre o plano. Ela passa a ser modo de construcao. A decisao da Etapa 5 de que "a permanencia importante e a capacidade de executar sem transformacoes, nao a preservacao de cada classe original" continua valendo e e o que autoriza remover a interface sem reescrever o historico daquela etapa.

## Dobra de Constante

- Uma subarvore e dobravel quando e pura, quando todos os seus filhos ja dobraram para constante e quando nada nela le estado de execucao.
- A avaliacao de uma subarvore constante usa o proprio no executavel, com um escopo sentinela que lanca em leitura de frame e em acesso ao clock. Nao existe um segundo avaliador constante para manter em paridade semantica com o runtime.
- O escopo sentinela e o que torna violacao de elegibilidade um bug interno alto e claro em vez de dobra silenciosamente errada. `CurrentTemporalExecutableNode` e o caso que ele barra por construcao.
- Falha em tempo de dobra deixa a subarvore **nao dobrada**, para que ela falhe em execucao exatamente como falharia na forma sem otimizacoes. Diagnostico de compilacao ou constante envenenada mudariam o contrato da linguagem e exigiriam ADR proprio.
- A Checagem Diferida de um no e descartada apenas quando aquela subarvore dobra com sucesso. Qualquer falha de checagem abandona a dobra e preserva no e checagem intactos.
- Construtos preguicosos podem descartar operandos nao tomados porque o runtime ja e preguicoso; operadores binarios eager nao podem, e e por isso que identidades aritmeticas continuam proibidas.
- Leitura de Simbolo Externo nao sobrescrevivel dobra porque seu valor efetivo e o default validado do ambiente. Leitura de simbolo sobrescrevivel nunca dobra.
- Prefixo de navegacao puro dobra porque a Etapa 6 declarou pureza de metodo registrado; pureza nao e redescoberta aqui.

## Leitura Dobrada

- O plano registra as Leituras Dobradas desde esta etapa, mesmo sem consumidor: e pre-requisito da Memoria de Calculo da Etapa 10, que precisa explicar valores que nao aparecem mais como leitura em execucao.
- A forma e uma lista imutavel de nome do simbolo, Identificador de No, Trecho de Fonte e valor dobrado, guardada no plano.
- O registro nao adiciona nenhum branch ao caminho quente: ele e metadata de construcao, nao de execucao.

## Elisao de Assercao

- `BoundaryCoercion.convertScalar` retorna o proprio valor quando a classe alvo ja o aceita, portanto uma assercao escalar sobre valor do tipo asserido e identidade provada e pode virar no-op.
- A elisao acontece somente quando o tipo resolvido do argumento e exatamente o tipo asserido.
- As assercoes de colecao nao existem no catalogo atual; quando existirem, sua elisao exige prova propria e nao esta autorizada por esta decisao.

## `in` com Lado Direito Constante

- A representacao depende do tipo de elemento porque `structuralEquals` compara `NUMBER` por `compareTo` e o restante por `equals`.
- Elemento `NUMBER` baixa para array ordenado com busca binaria por `compareTo`. Um `HashSet<BigDecimal>` mudaria semantica, porque `1.0` e `1` sao iguais por `compareTo` e diferentes por `equals` e por hash.
- Elemento `STRING`, `BOOLEAN`, `DATE`, `TIME` e `DATETIME` baixa para `HashSet`, onde `equals` coincide com `structuralEquals`.
- Elemento de tipo colecao ou mapa **nao baixa**: `List.equals` e `Map.equals` usam `equals` nos numeros contidos e divergiriam de `structuralEquals`.
- Lado direito de tipo mapa continua usando `containsKey` e ganha apenas a pre-avaliacao da constante.
- O limiar de tamanho e unico e documentado: oito elementos, igual para as duas representacoes. Abaixo dele a constante e apenas pre-avaliada e a varredura linear permanece. Dois limiares distintos nao se pagariam em teste.

## Subexpressao Comum Memoizada

- O item entra como ultimo incremento e permanece condicionado a medicao. Se nao pagar em benchmark, sai por decisao registrada, nao por silencio.
- Os slots de memo sao apendice do frame no plano, a partir de `frameSize`, inicializados com o sentinela `UNBOUND` no template. O Layout de Frame semantico nao muda e a invariante do Modelo Semantico continua valendo como esta.
- A memo e preguicosa no lugar, sem icamento. Cada ocorrencia computa ou le, e a primeira ocorrencia efetivamente executada falha exatamente onde a forma sem otimizacoes falharia.
- Icar uma subexpressao pura para fora de um caminho preguicoso introduziria falha que a forma sem otimizacoes nunca teria, como em `cond ? x/y : 0`. Por isso a disciplina e no lugar, e nao hoisting.
- Elegibilidade: subarvore pura, com duas ou mais ocorrencias, que nao leia slot de Item Atual. Pureza nao cobre dependencia de `@`, cujo valor muda por elemento.
- A identidade de duas ocorrencias e uma chave canonica estrutural derivada durante a construcao, com operador, tipos resolvidos, filhos e binding. Vinculo, `FunctionDescriptor` e `Pattern` sao comparados por identidade. `Identificador de No` nunca serve como chave, porque e unico por no.

## Verificacao

- Toda transformacao e validada por equivalencia contra o Oraculo Sem Otimizacoes gerado pela mesma pipeline. Essa e a regra normativa do ADR 0019.
- O Corpus de Expressoes inteiro executa nas duas formas em todo build.
- Testes de propriedade com jqwik cobrem entradas aleatorias com foco em efeito, falha, escala e dominio real.
- Equivalencia significa mesmo valor, mesma escala, mesma falha com mesmo codigo e mesmo trecho, mesma ordem de avaliacao observavel e mesmos efeitos observaveis.
- Funcao com efeito observavel e parte da suite de equivalencia, nao caso de borda opcional.

## Desempenho

- Os benchmarks existentes sao todos dirigidos por Simbolo Externo, e a Dobra de Constante mediria aproximadamente zero neles. Medir apenas contra eles seria um gate que nao mede nada.
- A etapa adiciona benchmarks com conteudo dobravel: prefixo de navegacao constante, `in` com lado direito constante, assercao elidida e compilacao de expressao rica em constantes.
- O gate para os benchmarks existentes e nao regressao dentro da banda de ruido de mais ou menos um por cento do protocolo vigente, com excecao apenas quando documentada. O precedente da issue 85 mostra a forma que uma excecao documentada tem.
- Protocolo identico ao das Etapas 5 e 6: tres forks, aquecimento e medicao equivalentes, profiler de alocacao, `ns/op` e `B/op`, registro em `docs/perf/performance-history.md`.

## Documentacao e ADR

- A Etapa 7 possui plano detalhado e registro de decisoes proprios.
- O ADR 0019 registra apenas o que e caro de reverter: prova de equivalencia contra o oraculo como pre-condicao de toda transformacao, falha constante que permanece nao dobrada, ausencia de flag publica e exigencia de pureza declarada.
- Ficam fora do ADR, no plano detalhado: a lista de sub-itens, os limiares de JMH e a representacao do `in`.
- `CONTEXT.md` recebe cinco termos: Dobra de Constante, Oraculo Sem Otimizacoes, Leitura Dobrada, Elisao de Assercao e Subexpressao Comum Memoizada, este ultimo apenas se o CSE sobreviver a medicao.
- O plano-mestre e corrigido apenas onde premissas foram invalidadas.

## Decomposicao

- Seis incrementos, cada um fechando com `mvn -pl exp-mk3 -am test` verde.
- Incremento 1: modo de construcao, `buildOracle`, remocao da `PlanTransformation`, inversao do teste de equivalencia de pipeline e suite de propriedade.
- Incremento 2: Dobra de Constante completa, incluindo prefixo de navegacao puro e Simbolo Externo nao sobrescrevivel, mais Leitura Dobrada.
- Incremento 3: Elisao de Assercao e `not not x`.
- Incremento 4: `in` com lado direito constante.
- Incremento 5: benchmarks dobraveis, gate de nao regressao e registro no historico de desempenho.
- Incremento 6: Subexpressao Comum Memoizada, condicionada a medicao.
- A decomposicao em issues acontece depois desta sessao, nao durante.

## Impacto Posterior

- Etapa 8 especializa nos sobre um plano que ja passou por dobra e elisao, e herda o oraculo como criterio de aceite em vez de criar o seu.
- Etapa 8 recebe a reordenacao de curto-circuito como candidata, condicionada a perfil.
- Etapa 9 mantem um unico plano por `(source, environmentId)` compartilhado entre visoes, o que e exatamente o motivo de a eliminacao de atribuicao morta ter saido desta etapa.
- Etapa 10 consome a Leitura Dobrada e transfere slots/proveniencia ao no constante ou `MemoizedExecutableNode`, preservando ocorrencias alcancadas em um unico plano.
- Etapa 12 transforma os limiares desta etapa em gate permanente de CI.
- Etapa 13 reutiliza o mesmo oraculo para o Tier 1 e para a fusao de pipelines de colecao.

## Decisoes Ainda Pendentes

- Nenhuma pendencia aberta no momento. A permanencia da Subexpressao Comum Memoizada e a unica decisao diferida, e ela e resolvida por medicao no incremento 6.

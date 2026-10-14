# Plano Detalhado - Etapa 8 - Especializacao de Nos e Invocacao Sem Reflexao

Spec: #122

Este plano detalha a Etapa 8 do `exp-mk3` depois do fechamento da Etapa 7. Ele consolida as decisoes registradas em `decisoes-etapa-8-especializacao-de-nos-e-invocacao-sem-reflexao.md`, complementa o plano macro, usa o ADR 0019 como contrato de toda transformacao de plano e produz o ADR 0020.

## Objetivo

Reduzir o custo por execucao de um Plano Imutavel sem alterar valor, escala, arredondamento, dominio, falha, ordem observavel ou efeito observavel, por tres mecanismos: nos escolhidos por operador e tipo resolvido durante a construcao, invocacao de funcao e de membro registrado sem coleta de argumentos em array, e remocao de conversao de borda ja provada redundante.

A Etapa 8 nao cria cache de compilacao, nao implementa Memoria de Calculo, nao funde pipelines de colecao, nao reordena operandos de curto-circuito e nao especializa potencia nem raiz.

## Resultado Final

A etapa foi fechada na issue #130. Ficaram por medicao: Invocacao Sem Reflexao para funcoes e
membros registrados, Elisao de Coercao de Borda de argumentos provadamente identicos, especializacao
de comparacao e igualdade escalar, coalescencia binaria, condicional fixa de dois ramos e aritmetica
decimal de adicao, subtracao, multiplicacao e modulo. Sairam por medicao: `between`, concatenacao
N-aria, condicional de um ramo, divisao decimal e o pool de Escopo de Execucao. Nenhum ponto de
entrada tipado foi adicionado a `ExecutableNode`, pois nenhum benchmark demonstrou essa necessidade.
Os numeros e as decisoes locais estao em `docs/perf/performance-history.md`.

## Autoridade e Premissas

- ADRs aceitos, `CONTEXT.md`, o Corpus de Expressoes e os planos detalhados vigentes definem o contrato.
- O ADR 0019 e normativo: todo plano otimizado e validado por equivalencia contra o Oraculo Sem Otimizacoes, gerado pela mesma pipeline e selecionavel apenas dentro do modulo.
- O ADR 0020 e produto desta etapa e passa a ser normativo para invocacao sem reflexao.
- O plano historico e o codigo existente sao evidencias, nao autoridade quando contradizem contratos posteriores.
- A API publica continua provisoria e pode mudar de forma incompativel antes da GA.
- Toda a suite existente deve permanecer verde; testes que contradizem contratos normativos sao atualizados atomicamente com codigo e corpus.
- **Ambiente de Expressao longevo e pre-condicao de desempenho desta etapa**: o custo de geracao de ponto de entrada e pago no build do ambiente e amortizado por todas as execucoes daquele ambiente.

## Perfil de Carga Alvo

A carga que orienta as prioridades desta etapa e calculo financeiro: poucas expressoes, compiladas uma vez, executadas muitas vezes, uma vez por parcela. Elas leem varios Simbolos Externos, escolhem taxa por condicional, fazem aritmetica decimal e chamam funcoes.

Expressao canonica da etapa, que entra no Corpus de Expressoes como Caso de Expressao e serve de referencia ponta a ponta:

```
taxa := if contrato.indice = "PRE" then taxaPre else taxaPos + spread endif;
fator := (1 + taxa / 100) ^ (prazo / 12);
saldo * fator + pmt(taxa / 100, prazo, saldo)
```

`contrato` e Tipo Java Registrado com propriedade textual `indice`; `taxaPre`, `taxaPos`, `spread`, `saldo` e `prazo` sao Simbolos Externos sobrescreviveis. A assinatura `pmt(NUMBER, NUMBER, NUMBER)` existe no catalogo financeiro. **A forma acima e proposta e e validada contra o resolver no incremento um**, junto com a montagem do ambiente de fixture; se algum detalhe nao resolver, corrige-se a fixture e a expressao aqui, nao o desenho da etapa.

**A expressao canonica nao e o gate de nenhum item isolado.** Ela contem `^`, cujo custo por avaliacao e de ordem de grandeza superior ao de tudo o mais que a etapa toca, e mascararia qualquer delta de invocacao, comparacao ou navegacao. Ela e caso de corpus, referencia de forma e prova de equivalencia; os gates por item usam micro-benchmarks isolados.

## Estado Atual e Estrategia de Reaproveitamento

A tabela e orientada por entregas, nao por classes.

| Entrega | Estado observado | Acao planejada |
|---|---|---|
| Construcao em dois modos com `build` e `buildOracle` | Implementada na Etapa 7, com um unico campo booleano `folding` | Renomear para `optimizing` e ampliar o que ele controla |
| Especializacao de no na construcao | Precedente instalado na Etapa 7: `in` constante troca `MembershipExecutableNode` por `HashLookupMembershipExecutableNode` ou `SortedNumberMembershipExecutableNode`, e o generico permanece | Generalizar o padrao |
| `resolvedTypes`, `equalityOperandTypes` | Consumidos, mas guardados no no como `ExpressionType` para re-despacho em runtime | Consumir para escolher o no |
| `numericFacts`, `runtimeNullability`, `collectionShapes` | Presentes e validados no Modelo Semantico, **nunca lidos** pelo construtor de plano | Consumir sob gate de entrada |
| `PowerRealDomainDeferredCheck` | Emitida pelo resolver, **sem consumidor**; `RealDomainArithmetic` reclassifica o dominio a cada execucao | Consumir |
| Binario generico | `BinaryExecutableNode`: uma classe, cinco fabricas, `switch` de dezesseis bracos; igualdade e comparacao re-despacham por tipo dentro de `ExpressionRuntime` | Especializar o segundo despacho |
| Invocacao de funcao | `Object[]` novo por chamada, `invokeWithArguments`, filtro de conversao de borda por argumento e por retorno, `BigDecimal.valueOf` por chamada nos adaptadores de retorno | Substituir |
| Acessor de propriedade e metodo registrado | `MethodHandle.invoke` / `invokeWithArguments`, montados no build do ambiente | Substituir por ponto de entrada gerado |
| `?.` | Branch de null, sem `try/catch`, sem fallback reflexivo | Preservar |
| `ExecutionScope` | Frame clonado do template e escopo novo por chamada; classe nao `final`, subclasseada por `ConstantFoldSentinelScope` | Pool medido, rejeitado e removido |
| Benchmark de invocacao de funcao e de acesso a membro isolado | Ausente | Implementar |

## Gate de Entrada

O trabalho executavel comeca apenas depois de verificar os invariantes abaixo. Build verde isolado nao substitui o gate.

- Tipo resolvido presente para todo no **operando**, e nao apenas para nos de resultado.
- Fato numerico presente para todo no de tipo `NUMBER`.
- Nulidade de runtime presente para todo no.
- Toda Checagem Diferida emitida pelo resolver tem consumidor no plano ou no runtime.
- Pureza registrada para todo no e todo elo de navegacao, incluindo pureza de metodo registrado.
- Layout de Frame canonico com `frameSize` cobrindo todo slot vinculado, e slots de memo apensos a partir dele.
- **Origem unica do bit `safe`** entre `CommonSubexpressionAnalyzer` e `ExecutionPlanBuilder`. Hoje o analisador chaveia `MapKeySubscript`, `ContextualMember`, `RegisteredProperty` e `RegisteredMethod` pelo flag `safe` da AST, enquanto o construtor deriva o mesmo bit de `binding.resultNullability()`. A divergencia e hoje inalcancavel porque chave de receptor identica implica tipo resolvido identico, mas essa implicacao deixa de ser obviamente sustentadora assim que o construtor passa a consumir `runtimeNullability` e a reconstruir os nos de membro registrado. Verificado antes do incremento cinco.

Qualquer violacao e bug interno. A Etapa 8 nao redescobre semantica nem compensa metadata ausente para poder especializar. `PowerRealDomainDeferredCheck` sem consumidor e, hoje, a unica violacao conhecida; fecha-la e trabalho do incremento zero.

## Mecanismo

- A especializacao acontece na construcao do plano, pelo mesmo motivo registrado na Etapa 7: `ExecutableNode` nao tem `children()` nem protocolo de reconstrucao, e criar um so pagaria para reconstruir o que o construtor acabou de construir com toda a metadata na mao.
- O campo booleano `folding` e renomeado para `optimizing` e passa a controlar duas coisas distintas: Dobra de Constante e escolha de No Especializado. **O Oraculo Sem Otimizacoes nao dobra e nao especializa.**
- Especializacao e **aditiva**: cada familia generica permanece no modulo como o no que o Oraculo constroi, exercitada em todo build pela suite de equivalencia. Substituir a familia generica apagaria o substrato do Oraculo e contradiria o ADR 0019.
- `ExecutableNode` pode receber pontos de entrada tipados como metodos `default` que delegam ao `execute(ExecutionScope)` generico, sobrescritos apenas por nos especializados. O Oraculo herda os defaults sem escrever uma linha. Nenhum ponto de entrada tipado entra sem benchmark pareado que o justifique, e o alargamento chega junto com a primeira familia que o justificar, nao antes.
- `AssignmentExecutable` esta fora do escopo: uma atribuicao e uma escrita de slot, e o custo esta no lado direito, que ja e `ExecutableNode`.

## Contrato de Invocacao Sem Reflexao

- O ponto de entrada e gerado no build do Ambiente de Expressao, uma vez por `FunctionDescriptor` e por membro registrado, e compartilhado por todos os planos daquele ambiente. Nao ha geracao por call-site de plano.
- Aridades um a quatro usam interface funcional dedicada gerada por `LambdaMetafactory`; as demais usam `invokeExact` com handle pre-adaptado.
- Pre-alocacao de array de varargs por call-site **nao tem alvo**: metodos varargs de provedor sao rejeitados na importacao e candidatos varargs sao descartados no catalogo de tipos Java.
- `VarHandle` **nao tem alvo**: navegacao por campo publico esta fora da v2, entao todo acessor de membro registrado e metodo.
- `?.` continua sendo checagem de null antes da invocacao, sem `try/catch` e sem fallback reflexivo. A classificacao de falha de acessor, receptor nao suportado e violacao de contrato de provedor permanece identica.
- Nenhuma reflexao nova aparece em runtime; o que a etapa remove e a coleta de argumentos em array e a entrada lenta do `MethodHandle`.

## Contrato de Elisao de Coercao de Borda

- Hoje cada argumento e o retorno de toda funcao importada passam por um filtro de `MethodHandle` que faz uma chamada virtual de conversao, mesmo quando o valor ja e exatamente o tipo canonico do parametro.
- A elisao ocorre somente quando o tipo resolvido do argumento e exatamente o tipo canonico declarado do parametro. Qualquer outro caso mantem o filtro.
- E o mesmo raciocinio da Elisao de Assercao provada na Etapa 7, um nivel abaixo: a conversao e identidade provada, entao removê-la nao pode mudar valor, escala nem falha.
- Os adaptadores numericos de retorno alocam um `BigDecimal` por chamada via `BigDecimal.valueOf`. Onde o retorno ja e `BigDecimal`, o adaptador sai junto com o filtro; onde nao e, a alocacao e inevitavel e permanece.
- Elisao de filtro de retorno nao pode remover a validacao de nao nulidade de resultado de provedor: essa e contrato de borda, nao conversao.

## Contrato de No Especializado

Cada familia entra por medicao propria, com ganho pareado contra o Oraculo na mesma execucao JMH. A lista abaixo e a ordem de expectativa, nao um compromisso de implementacao.

| Familia | O que a especializacao remove | Expectativa |
|---|---|---|
| Comparacao e igualdade | O segundo despacho por tipo dentro de `ExpressionRuntime.structuralEquals` e `compareValues`, com o tipo do operando ja resolvido na compilacao | Alta |
| Coalescencia nula | Laco n-ario generico sobre lista de operandos | Media |
| `between` | Duas chamadas com despacho por tipo interno | Media |
| Concatenacao | Somente achatamento de cadeia com tres ou mais operandos, que hoje materializa string intermediaria por no. O caso de dois operandos **nao** e alvo: `(String) esquerda + direita` ja compila para `invokedynamic` de `StringConcatFactory`, que um `StringBuilder` dimensionado a mao tende a perder | Media, so N-aria |
| Condicional | Laco sobre lista de ramos quando o numero de ramos e pequeno e fixo | Baixa |
| Aritmetica decimal binaria | O `switch` externo em volta de uma operacao de `BigDecimal` | **Baixa, provavelmente nao paga** |

A expectativa baixa da aritmetica decimal esta registrada de proposito: a medicao da issue #121 mostrou que `BigDecimal.add` custa mais que uma leitura de frame mais um branch, ou seja, a operacao domina e remover o despacho em volta dela move pouco. Se a medicao confirmar, a familia sai por decisao registrada, e isso e resultado, nao fracasso.

## Contrato de Pool de Escopo

Medido e removido no incremento sete.

- Condicao declarada de antemao: medido na expressao canonica, pareado, o pool so fica se reduzir `B/op` em pelo menos vinte por cento **e** nao piorar `ns/op` fora da banda de erro. Fora disso sai por decisao registrada no historico de desempenho, no formato da issue #121.
- Um frame reaproveitado exige limpeza por chamada, incluindo re-semeadura dos slots de memo com o sentinela `UNBOUND`; esse preenchimento pode consumir o ganho.
- Risco de despacho a ser considerado na leitura da medicao: `ExecutionScope` nao e `final` e ja e subclasseada por `ConstantFoldSentinelScope`, entao todo `scope.read` ja e chamada virtual bimorfica. Uma segunda forma concreta de escopo torna o call-site megamorfico, e o pool pode perder no despacho enquanto ganha na alocacao.
- Resultado: a implementacao provisoria reduziu `B/op` em apenas 3,58%, muito abaixo dos 20%
  exigidos, e por isso foi removida integralmente. Nao ficou `ThreadLocal`, reset mutavel, flag nem
  propriedade de sistema.

## Verificacao

Equivalencia significa, para a mesma fonte, o mesmo ambiente e as mesmas entradas: mesmo valor, mesma escala, mesma falha com mesmo codigo e mesmo trecho, mesma ordem de avaliacao observavel e mesmos efeitos observaveis. O criterio e herdado do ADR 0019; a Etapa 8 nao cria o seu.

- Corpus de Expressoes inteiro executado nas duas formas em todo build, sem exclusao e sem teste desabilitado, agora incluindo a expressao canonica desta etapa.
- Testes de propriedade com jqwik sobre entradas aleatorias, cobrindo efeito, falha, escala e dominio real, estendidos para exercitar No Especializado e Invocacao Sem Reflexao e nao apenas os caminhos genericos.
- Sonda de efeito comparando a sequencia de invocacoes entre as duas formas, para provar que especializacao e invocacao nao criam nem eliminam efeito e nao mudam ordem.
- Caso por familia especializada, com o tipo que especializa e o tipo que nao especializa, provando que a escolha na construcao nao muda resultado.
- Caso de igualdade numerica com escalas diferentes, porque comparacao especializada por tipo e exatamente onde `compareTo` contra `equals` quebraria silenciosamente.
- Caso de falha de funcao e de acessor nas duas formas, com mesmo codigo e mesmo trecho, provando que a rota de invocacao nova preserva a classificacao de falha e a violacao de contrato de provedor.
- Caso de argumento cujo tipo resolvido **nao** e exatamente o tipo canonico, provando que o filtro de borda permanece.
- Caso de `?.` sobre membro registrado nas duas rotas de invocacao.
- Caso provando que o dominio real de potencia continua sendo verificado apos o consumo da Checagem Diferida, tanto onde o resolver provou quanto onde nao provou.
- O pool nao entrou; o teste provisorio de re-semeadura foi removido com a implementacao que ele
  exercitava, e os testes de memo e concorrencia continuam cobrindo o escopo novo por chamada.

## Benchmarks e Gate

- Benchmarks novos, isolados por mecanismo: chamada de funcao, acesso a membro registrado, invocacao de metodo registrado, comparacao e igualdade por tipo. Nenhum deles existe hoje.
- Benchmark ponta a ponta da expressao canonica, como caracterizacao, sem limiar, porque `^` domina o seu tempo.
- Gate por item: ganho pareado otimizado contra oraculo, na mesma execucao, fora da banda de erro. Comparacao contra baseline historico de outro dia nao e criterio.
- Gate das quatro suites rastreadas: elas continuam sendo re-executadas, e um delta alem de mais ou menos um por cento so conta como regressao quando ha causa identificada no codigo. Sem causa identificada, o veredito e a re-medicao pareada da mesma expressao, que e imune ao ruido da maquina.
- Protocolo identico ao das Etapas 5 a 7: tres forks, aquecimento e medicao equivalentes, profiler de alocacao, `ns/op` e `B/op`, governor pinado.
- Registro em `docs/perf/performance-history.md` com ambiente, comando, commit, expressoes e resultados, incluindo os itens que sairam por medicao.

## Incrementos de Implementacao

Antes da etapa, como issue preparatoria:

0. **Simetria de `MathContext` (ADR 0021).** `MathContext` se aplica onde a operacao e obrigada a arredondar (`/`, `^`, `root`, transcendentais) ou onde a escala cresce sob repeticao (`*`, sempre, incluindo o laco de taxa composta em `npv`). `+`, `-`, `negate`, `abs` e `modulo` permanecem exatos — remover o `MathContext` hoje aplicado a `+`/`-` e a correcao, nao adiciona-lo a `sum`/`modulo`. `sum` permanece acumulacao exata e `avg` arredonda uma unica vez, na divisao final; a assimetria original se dissolve em vez de ser fechada por adicao de contexto. Muda valores (em `+`/`-` e em `npv`), portanto nao pode acontecer dentro de uma etapa cuja regra e nao mudar valor. Fecha com registro de decisao proprio (ADR 0021); sem condicao de custo, porque remover uma chamada de `MathContext` nao pode ficar mais lento.

Na etapa:

1. **Gate e mecanismo.** Verificacao dos invariantes de entrada; consumo de `runtimeNullability` e `numericFacts` no construtor; consumo da `PowerRealDomainDeferredCheck`; renomeacao de `folding` para `optimizing`. Sem mudanca em `ExecutableNode`.
2. **Piloto.** Invocacao Sem Reflexao e Elisao de Coercao de Borda, medidas juntas, mais o fim do `BigDecimal.valueOf` por chamada onde o retorno ja e `BigDecimal`. Inclui os benchmarks de invocacao que hoje nao existem.
3. **Ponto de decisao**, registrado no historico de desempenho.
4. **Comparacao e igualdade sem duplo despacho**, segundo piloto. Provavelmente e aqui que os pontos de entrada tipados se pagam.
5. **Acessores de navegacao** por ponto de entrada gerado.
6. **Familias restantes por medicao**, na ordem da tabela de No Especializado a partir de Coalescencia Nula, ja que comparacao e igualdade fecharam no incremento quatro.
7. **Pool de `ExecutionScope`**, implementado provisoriamente, medido sob a condicao declarada e removido.
8. **Fechamento.** Re-execucao das quatro suites, gate, registro no historico, ADR 0020, `CONTEXT.md` e correcoes do plano-mestre.

Regra de parada: se o piloto do incremento dois nao pagar, o trabalho segue para o incremento quatro assim mesmo, porque invocacao e despacho de comparacao sao mecanismos independentes e um nao prediz o outro. Se os dois falharem, a etapa encerra por decisao registrada, sem os incrementos restantes.

Cada incremento fecha com `mvn -pl exp-mk3 -am test` verde. A decomposicao em issues ocorre depois do design registrado.

## Criterios de Aceite da Etapa 8

- Issue preparatoria de simetria de `MathContext` fechada **antes** do trabalho executavel da etapa, ou a assimetria mantida por decisao registrada sob custo medido. Especializar sobre semantica ainda em movimento significaria refazer a prova de equivalencia.
- Gate de entrada verificado antes do trabalho executavel, com a Checagem Diferida de potencia consumida e a origem unica do bit `safe` confirmada.
- `optimizing` no lugar de `folding`, controlando dobra e especializacao, com o Oraculo sem nenhuma das duas.
- Familias genericas preservadas e exercitadas como nos do Oraculo; nenhuma removida.
- Equivalencia verde nas duas formas sobre o corpus inteiro, incluindo a expressao canonica, e sobre a suite de propriedade estendida.
- Invocacao Sem Reflexao instalada sem reflexao nova em runtime, sem coleta de argumentos em array nas aridades cobertas, e com classificacao de falha identica a anterior.
- Elisao de Coercao de Borda apenas com tipo canonico exato provado, com validacao de nao nulidade de resultado de provedor preservada.
- Toda familia especializada com caso que especializa e caso que nao especializa, e com ganho pareado medido; familia sem ganho removida por decisao registrada.
- Pool de escopo instalado apenas se cumprir a condicao declarada, ou removido com a medicao registrada.
- Benchmarks novos de invocacao e de comparacao executados; quatro suites rastreadas re-executadas sob a regra de causa identificada; resultados no historico.
- ADR 0020 aceito, `CONTEXT.md` com os tres termos novos e plano-mestre corrigido nos sete pontos invalidados.
- Toda a suite existente permanece verde.

## Fora de Escopo

- Reordenacao segura de curto-circuito. Chegou da Etapa 7 como candidata condicionada a perfil, mas o ADR 0019, contrato posterior, ja a lista entre as otimizacoes que ficam de fora, por ordem observavel e efeitos. O motivo e o contrato, nao a ausencia de perfil.
- Especializacao de potencia e raiz. `BigDecimalMath.pow` ja detecta expoente inteiro e usa exponenciacao binaria sem `log`/`exp`, e o ADR 0017 exige o caminho obrigatorio da biblioteca numerica em todos os tiers. Nao ha ganho a colher nem autorizacao para colhe-lo.
- Cache de constantes pequenas. Um `BigDecimal` cacheado tem escala fixa, e substituir uma constante de escala dois por uma de escala zero mudaria um observavel que a definicao de equivalencia protege. Alem disso o item ja esta quase satisfeito: literais viram valor preparado e `stripTrailingZeros` nao existe em `src/main`.
- Pre-alocacao de varargs por call-site e `VarHandle`, ambos sem alvo no modulo.
- `AssignmentExecutable`.
- Reescritas algebricas, de `%`, de potencia e de raiz.
- Cache de compilacao, contador de execucoes e engine, que ficam na Etapa 9.
- Memoria de Calculo e slots de captura no plano unico, que ficam na Etapa 10.
- Fusao de pipelines de colecao e Tier 1, que ficam na Etapa 13.
- Flag publica, propriedade de sistema ou API que exponha a selecao do oraculo ou a rota de invocacao.
- Mudanca de gramatica.
- Publicacao de tickets durante o planejamento.

## Impacto nas Etapas Posteriores

- **Etapa 9** herda um plano com nos especializados e pontos de entrada compartilhados por ambiente. Observacao registrada para o dimensionamento dela: no perfil de carga desta etapa, poucas expressoes sao compiladas uma vez e executadas muitas vezes, entao o valor do cache de compilacao esta no plano unico compartilhado entre visoes e no contador de execucoes, nao na amortizacao do custo de compilar. Isso nao muda a Etapa 9 agora; e insumo de quando ela for planejada.
- **Etapa 10** adiciona slots primitivos aos nos marcaveis das familias especializadas existentes; cada troca de estrategia preserva `NodeId`, Trecho de Fonte e Chave de Proveniencia, sem decorator ou segunda arvore.
- **Etapa 12** transforma os gates pareados desta etapa em gate permanente de CI, junto com perfil de alocacao.
- **Etapa 13** reutiliza o mesmo Oraculo para o Tier 1 e para a fusao de pipelines de colecao, e herda os pontos de entrada gerados como base da compilacao promovida.

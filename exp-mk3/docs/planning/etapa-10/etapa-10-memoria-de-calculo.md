# Plano Detalhado - Etapa 10 - Memoria de Calculo

Este plano substitui a proposta anterior de auditoria por eventos. Ele incorpora a pesquisa JVM, o
prototipo JMH de armazenamento e as decisoes publicas fechadas para `computeWithMemory()`. O gate de
reconciliacao da issue #155 selecionou captura append-only local com payload publico compacto.

## Objetivo

Entregar uma Memoria de Calculo deterministica junto ao resultado de uma execucao, explicando os
Simbolos Externos e Internos que participaram do calculo e os Pontos de Calculo efetivamente
alcancados, sem criar trace temporal, segunda arvore, segundo plano ou estado compartilhado entre
execucoes.

O caminho normal de `compute()` continua com zero alocacao adicional. Nos marcaveis admitem no maximo
um teste previsivel no caminho efetivamente gerado, sujeito ao gate pareado de latencia, branches e
codigo de maquina. Ordem de testes, encoding do slot inativo e contagem de alcance sao detalhes medidos,
nao premissas fixadas pelo desenho fonte.

## Autoridade e Evidencia

- O ADR 0019 continua normativo para equivalencia entre o plano otimizado e o Oraculo Sem
  Otimizacoes.
- O ADR 0023 registra a escolha de um unico plano com captura append-only local.
- `performance-compute-with-memory.md` registra a pesquisa JVM e os riscos de retencao.
- `prototype-calculation-memory-storage.md` registra a comparacao JMH entre cauda no frame,
  append-only e denso com bitmap.
- `calculation-capture-storage-reconciliation.md` registra o desempate em Temurin 21.0.8 e autoriza a
  implementacao de producao append-only.

## Status do Gate de Software

Gate de software concluido em 2026-08-30. O gate de deployment repetiu build, suite, JMH, JFR, JOL e
inspecao de inlining no Eclipse Temurin 21.0.8+9-LTS. Permanecem como escolhas finais: recorder append-only local,
branch mode-first, contagem durante captura, payload colunar exato e API indexada como percurso de
persistencia. O resultado vinculante e seus comandos reproduziveis estao em
`docs/perf/performance-history.md`, na entrada da issue #147. O fechamento da etapa permanece condicionado
ao contador de branches em um host com acesso a eventos de hardware; este host usa
`perf_event_paranoid=4` sem `CAP_PERFMON`.

## Resultado Publico

As quatro Visoes de Expressao oferecem `computeWithMemory()` com e sem sobrescritas:

```java
ComputationWithMemory<Object>                 // ResultExpression
ComputationWithMemory<BigDecimal>             // MathExpression
ComputationWithMemory<Boolean>                // LogicalExpression
ComputationWithMemory<Map<String, Object>>    // AssignmentsExpression
```

O envelope e os elementos publicos sao imutaveis:

```java
record ComputationWithMemory<T>(T result, CalculationMemory memory) {}
record VariableEntry(VariableKey key, Object value) {}
record CalculationEntry(CalculationKey key, Object value) {}
record VariableKey(String name, VariableOrigin origin) {}
record CalculationKey(
        int nodeId,
        SourceSpan sourceSpan,
        CalculationKind kind,
        String name) {}
```

`VariableOrigin` possui `EXTERNAL` e `INTERNAL`. `CalculationKind` possui `FUNCTION`, `PROPERTY`,
`METHOD` e `CURRENT_TEMPORAL`. `CalculationMemory` expoe as listas de conveniencia e a leitura
indexada equivalente:

```java
List<VariableEntry> variables();
List<CalculationEntry> calculations();

int variableCount();
VariableKey variableKeyAt(int index);
Object variableValueAt(int index);

int calculationCount();
CalculationKey calculationKeyAt(int index);
Object calculationValueAt(int index); // pode retornar null
```

Os indices seguem exatamente a ordem publica definida neste plano; nao sao slots internos. A API
indexada e o caminho recomendado para serializacao, JDBC/JPA em lote e escrita de arquivo porque nao
cria `VariableEntry`/`CalculationEntry` temporarios. As listas sao vistas imutaveis sobre o mesmo
payload e criam o record correspondente somente em `get`/iteracao; nao mantem cache de entries nem
estado lazy mutavel. Cada chamada nao vazia a `variables()`/`calculations()` cria somente sua pequena
projecao stateless; o consumidor que escolhe a API de lista deve guardar essa referencia durante o uso.
O caminho indexado nao cria projecao alguma. Listas vazias usam singleton. Nao ha mapa derivado, busca
por nome ou copia defensiva por acesso.
Todos os acessores indexados sao `O(1)`, devolvem a mesma referencia de chave compartilhada e lancam
`IndexOutOfBoundsException` para indice invalido, como `List.get`.

O payload materializado por `computeWithMemory()` e colunar e exato: arrays de valores da execucao,
um sidecar primitivo de ordinais somente quando lacunas de alcance o exigirem e referencias a chaves
preconstruidas em um schema independente do plano. Nao existe `VariableEntry[]`, `CalculationEntry[]`,
`ArrayList` seguido de `List.copyOf` ou objeto por entrada no caminho de computacao.

O schema de variaveis carrega explicitamente `int[] variableFrameSlots`: slots externos, internos e de
Item Atual sao intercalados no frame real, portanto nenhum freeze pode assumir que variaveis ocupam um
prefixo. Existem duas projecoes imutaveis sobre a mesma metadata compilada: participantes da execucao
completa e participantes somente das atribuicoes. A segunda exclui externos usados exclusivamente pela
expressao final que `AssignmentsExpression` nao executa.

O resultado do envelope passa pela mesma Materializacao Publica de `compute()`. Os valores da memoria
sao as referencias canonicas validadas que participaram da execucao, sem copia profunda adicional.
Colecoes e mapas canonicos continuam imutaveis; objetos Java registrados preservam identidade e nao
prometem snapshot historico do estado interno.

## Conteudo de `variables()`

- Entram somente Simbolos Externos semanticamente usados pela parte que a Visao de Expressao executa,
  inclusive leituras dobradas, e Simbolos Internos criados pelas atribuicoes executadas. Declaracoes
  externas nao usadas nao formam dump do Ambiente de Expressao. Em `AssignmentsExpression`, um
  externo usado apenas pela expressao de resultado omitida nao aparece.
- Cada simbolo aparece uma vez com seu valor efetivo: default ou override para externo, valor final
  para interno.
- Externos seguem a ordem canonica do plano. Internos seguem a ordem da primeira criacao na fonte.
  Os externos precedem os internos.
- Sombreamento preserva as duas entradas. `VariableKey("x", EXTERNAL)` e
  `VariableKey("x", INTERNAL)` sao chaves distintas.
- Valores de variavel permanecem nao nulos pelo contrato atual das bordas e atribuicoes.

## Conteudo de `calculations()`

Sao Pontos de Calculo marcaveis:

- chamada de funcao global;
- leitura de propriedade Java registrada;
- chamada de metodo Java registrado;
- Valor Temporal Corrente.

Nao sao Pontos de Calculo: literais, operadores aritmeticos/logicos, leituras de simbolo, atribuicoes,
subscritos comuns ou operacoes de colecao. Uma operacao de colecao e uma fronteira opaca completa.
Em `transactions.map(@ -> calculateFee(@)).sum()`, nao se registram `calculateFee`, `map`, `sum` nem
`@`.

Cada ocorrencia fonte alcancada produz no maximo uma entrada. A ordem e a ordem crescente dos slots
de calculo atribuidos segundo a avaliacao estavel da fonte; pontos de ramos nao alcancados deixam
lacunas que sao omitidas durante o freeze.

Uma fronteira alcancada que produz Valor Nulo de Runtime gera uma entrada cujo `value()` e `null`.
Uma fronteira nao alcancada nao gera entrada. Se a expressao raiz tambem for marcavel, ela permanece
em `calculations()` alem de fornecer o resultado do envelope.

Falha de execucao ou de Materializacao Publica propaga a mesma falha de `compute()` e nao publica
memoria parcial.

## Consumo para Persistencia

A Memoria de Calculo e um dado de transferencia compacto, nao um mecanismo de persistencia. O modulo
nao serializa JSON, abre arquivo, inicia transacao, cria entidade JPA nem copia recursivamente valores.
O adaptador consumidor percorre `variableCount()`/`calculationCount()` e os acessores `*At`, gravando
chave e valor diretamente no formato de destino. Isso evita criar um segundo grafo intermediario antes
dos objetos/bytes que a propria persistencia inevitavelmente produz.

Nao se adiciona callback, `BiConsumer`, visitor ou `CalculationMemorySink` publico. A leitura indexada
evita lambda/dispatch por entrada, nao acopla o evaluator a excecoes de I/O/JDBC e deixa ownership da
transacao/recurso inteiramente no adaptador.

As listas permanecem para uso idiomatico e compatibilidade com bibliotecas que exigem `List`, mas seu
custo de records temporarios pertence ao consumidor. O guia de API deve mostrar primeiro a travessia
indexada para persistencia e alertar que serializadores reflexivos sobre `variables()`/`calculations()`
pagam uma projecao por entrada.

A persistencia acontece fora da medicao e da transacao interna da expressao. O valor capturado e a
referencia canonica usada pela execucao; se a aplicacao precisa de snapshot serializado antes que um
objeto Java mutavel seja alterado, ela deve consumir a memoria imediatamente ou definir essa copia na
borda de persistencia.

## Plano e Metadados

Existe um unico Plano Imutavel cacheado para `compute()` e `computeWithMemory()`. Nao existem classes
`Captured*`, wrappers, decorators, segunda arvore, plano instrumentado ou construcao lazy de outra
representacao.

Cada familia de no marcavel recebe um `int calculationSlot` imutavel e, somente quando CSE exige
republicacao de proveniencia, metadata imutavel de slots de replay. O encoding de
`NO_CALCULATION_SLOT` permanece interno. O no calcula o valor uma unica vez e, quando a captura esta
ativa e a ocorrencia e marcavel, acrescenta a referencia ja calculada. O gate escolheu mode-first:
`compute()` abandona a captura antes de validar o slot; descendentes opacos usam o slot inativo.

Nao se adiciona um segundo plano para obter um branch melhor. A forma vencedora precisa preservar
opacidade de colecao, inline do helper e uma unica avaliacao do valor.

O plano guarda somente slots e metadados imutaveis. A Chave de Proveniencia e preconstruida uma vez e
compartilhada por todas as memorias do mesmo compilado dentro de um schema autocontido. O schema nao
tem back-reference: `CalculationMemory` nao pode alcancar `ExecutionPlan`, `ExecutableNode`, AST,
Modelo Semantico, Ambiente de Expressao ou texto fonte. Isso amortiza `VariableKey`, `CalculationKey`
e `SourceSpan` sem prolongar a vida do plano. Schema primitivo que reconstrua essas chaves por execucao
fica rejeitado salvo evidencia JMH/JOL contraria, pois transfere CPU e alocacao para toda mini auditoria.

Os slots sao selecionados a partir das ocorrencias semanticas, antes que a forma executavel esconda
proveniencia:

- Dobra de Constante transfere todas as ocorrencias marcaveis colapsadas para um grupo de proveniencia
  estatica associado ao no constante substituto, com seus valores ja dobrados e sua ordem original. O
  grupo aparece somente se esse substituto for alcancado; chamadas marcaveis aninhadas nao somem quando
  um unico no constante substitui a subarvore inteira.
- Subexpressao Comum Memoizada transfere ao `MemoizedExecutableNode` de cada ocorrencia o grupo de
  capturas que sua leitura de memo pode pular. Em miss, o delegate calcula e grava os valores uma vez;
  em hit, o wrapper publica nas chaves da ocorrencia alcancada os valores mantidos exclusivamente no
  frame daquela execucao, sem reinvocar funcao ou membro. Duas ocorrencias fonte alcancadas geram duas
  sequencias equivalentes de entradas mesmo quando a segunda le o memo.
- Elisao de Assercao nao cria um ponto para a chamada eliminada, pois uma assercao provadamente
  identidade nao e uma fronteira dinamica.
- Especializacao de no preserva slot e proveniencia ao trocar apenas a estrategia executavel.
- Descendentes de operacoes de colecao e qualquer corpo/predicado executado uma vez por elemento,
  inclusive filtro, sao construidos em contexto opaco e recebem
  `NO_CALCULATION_SLOT`.

O inventario de calculos percorre primeiro atribuicoes em ordem de fonte e depois a expressao final,
seguindo ordem real de avaliacao dentro de cada arvore. Nao usa ordem de construcao dos nos, pois o
builder atual constroi a expressao final antes das atribuicoes, nem ordena por `NodeId`, pois pais recebem
identidade antes de filhos que executam primeiro. Plano otimizado e Oracle compartilham ordinais/chaves
publicos independentemente dos slots de memo.

O Oraculo Sem Otimizacoes recebe o mesmo inventario de ocorrencias e chaves. Sua memoria deve ser
equivalente a memoria otimizada em chaves, valores, nulls, alcance e ordem, alem da equivalencia de
resultado e falha ja exigida pelo ADR 0019.

## Execucao com Captura Append-only

Se `F` e o tamanho normal do frame, incluindo slots de memo, `S` e o numero de Pontos de Calculo e `K`
e o numero de pontos alcancados:

- `compute()` continua clonando o template de tamanho exato `F` e nao cria recorder;
- `computeWithMemory()` ativa um recorder append-only local apenas quando `S > 0`; o frame permanece
  exato quando nao ha replay de CSE e recebe somente os slots scratch necessarios quando um memo hit
  precisa republicar valores sem reinvocar o calculo;
- o recorder mantem `Object[] values`, `int[] ordinals` opcional e `count`; `count` distingue null
  alcancado de ausencia sem sentinel ou bitmap;
- a capacidade inicial e pequena e limitada por `S`; crescimento usa arrays simples e nunca ultrapassa
  o numero estatico de pontos;
- enquanto os ordinais alcancados formam o prefixo `0..K-1`, nao existe sidecar; a primeira lacuna cria
  o sidecar e preenche o prefixo anterior;
- o resultado publico e materializado antes do freeze; se essa borda falhar, nenhum payload final de
  memoria e alocado;
- no freeze, arrays com tamanho exato transferem ownership; arrays com capacidade excedente sao
  truncados para `K`; chaves do schema sao reutilizadas;
- calculos densos em ordem usam apenas `Object[K]`; calculos com lacunas usam tambem `int[K]` exato;
- nenhuma entrada publica e criada durante captura ou freeze;
- `count` e incrementado durante a captura, conforme o gate da issue #139;
- slots de memo e Item Atual nao sao publicados. A disciplina `finally` de Item Atual continua sendo
  requisito de correcao, e nem frame nem recorder ficam retidos pela memoria.

Sem participantes, retorna-se uma instancia vazia compartilhada de `CalculationMemory`.

### Ciclo de vida sem holder intermediario

A rota de memoria mantem o `ExecutionScope` apenas em variavel local da implementacao enquanto executa
estas fases:

1. preparar frame normal, recorder append-only quando `S > 0`, e aplicar overrides;
2. executar atribuicoes e, exceto em `AssignmentsExpression`, a expressao final;
3. executar exatamente uma vez a Materializacao Publica normal;
4. congelar o payload colunar;
5. criar diretamente `ComputationWithMemory<T>`.

Nao se cria `PreparedExecution`, tuple, record ou wrapper temporario apenas para transportar scope e
resultado cru entre `ExecutionPlan` e a Visao de Expressao. O seam interno pode expor operacoes de ciclo
de vida somente dentro do modulo; `ExecutionScope` nunca aparece no contrato publico nem escapa do
metodo. Uma falha em qualquer fase abandona frame/scope e nao executa freeze.

Em `AssignmentsExpression`, a Materializacao Publica le cada `AssignedSymbol` diretamente do frame e
constroi o mapa final. A rota de memoria nao chama o atual `computeAssignedValues()` que cria uma
`ArrayList<Object>` crua intermediaria. Essa eliminacao deve ser medida separadamente e pode ser aplicada
ao `compute()` normal somente se preservar comportamento e passar seu proprio gate; nao e pre-condicao
para alterar o caminho normal nesta etapa.

Cauda estendida no frame continua apenas como controle de benchmark. Denso com bitmap esta descartado.
Nao ha estrategia adaptativa, selecao em runtime ou limiar publico nesta etapa.

## Organizacao e Direcao de Dependencias

- `com.runestone.expeval_mk3.api` contem os records/enums publicos, `CalculationMemory`, as projecoes
  de lista package-private e as Visoes de Expressao. Os novos contratos de memoria nao dependem
  de `ExecutionPlan`, `ExecutionScope` ou schema interno; as visoes mantem apenas sua dependencia
  interna ja existente do plano.
- `internal.plan` seleciona Pontos de Calculo, atribui slots/chaves, preserva proveniencia durante
  folding/CSE/especializacao e incorpora o schema independente ao Plano Imutavel.
- A fronteira coesa `internal.memory` possui schema, payload colunar e operacoes de freeze. Ela
  depende dos contratos publicos de chave, mas nao de Visoes de Expressao nem do
  grafo de nos.
- `internal.runtime` grava somente por ordinal no recorder local. Nos executaveis conhecem apenas o
  `calculationSlot` e `ExecutionScope`; nao importam `CalculationEntry`, listas ou builders publicos.
- A Visao de Expressao continua responsavel por Materializacao Publica do resultado e monta
  `ComputationWithMemory<T>` a partir do payload interno por um seam de propriedade, sem copia de
  arrays e sem entries. Essa borda preserva o comportamento tipado de `ResultExpression`, `MathExpression`,
  `LogicalExpression` e `AssignmentsExpression`.
- O seam entre a Visao e `ExecutionPlan` preserva scope e resultado cru em variaveis locais, nao em um
  objeto de transferencia. Ele nao e API publica e nao autoriza callers a executar fases fora de ordem.

Nao se cria pacote publico `memory`, SPI, sink ou interface de extensao. A fronteira existe para manter
dependencias internas coesas, nao para suportar multiplas estrategias em producao.

## Incrementos

### Incremento 1 - Contrato e fixture de publicacao

- Introduzir no JMH de prototipo as formas finais de `VariableKey`, `CalculationKey`, schema
  independente, payload colunar, vistas imutaveis e leitura indexada.
- Comparar publicacao colunar com entries eager em `computeWithMemory()` isolado, travessia indexada
  sem alocacao e travessia pelas listas.
- Medir separadamente execucao/captura, freeze e consumo completo das chaves/valores nos cenarios de
  `S=0,4,64,256`, incluindo denso, alternado, prefixo, vazio e esparso.
- Comparar cauda e append em condicoes identicas. Denso permanece apenas como evidencia historica e
  nao precisa ganhar nova implementacao.
- Comparar slot-first, mode-first e comparacao fundida, alem de contador no hot path contra contagem no
  freeze. O controle de `compute()` nao pode ganhar alocacao nem segundo plano.
- Registrar o resultado no historico de desempenho.

**Stop rule:** se o armazenamento ou o payload colunar deixarem de ser Pareto vencedores no fluxo
`computeWithMemory() -> consumo sequencial` dos casos representativos, a respectiva escolha e reaberta
antes de codigo de producao. A issue #155 aplicou esta regra e selecionou append-only.

### Incremento 2 - API publica e schema

- Criar os tipos publicos fechados e validacoes de construtor.
- Criar schema de variaveis/calculos sem back-reference, chaves preconstruidas e ordem deterministica.
- Inventariar slots explicitos e schemas de participantes completo/assignments-only enquanto o Modelo
  Semantico ainda esta disponivel; nunca inferir variaveis por faixa de frame.
- Criar payload colunar, acessores indexados e projecoes `List` stateless sem cache por entrada ou por
  memoria.
- Adicionar `computeWithMemory()` com e sem overrides nas quatro visoes.
- Preservar exatamente a Materializacao Publica de cada `compute()` existente.

### Incremento 3 - Captura append-only e freeze

- Separar preparacao normal e preparacao com recorder sem duplicar validacao de overrides.
- Criar o ciclo interno `preparar -> executar -> materializar -> freeze` sem holder intermediario e sem
  alterar a rota direta de `compute()`.
- Adicionar append por ordinal, sidecar lazy e contagem durante captura.
- Materializar somente arrays compactos depois da Materializacao Publica e liberar o recorder temporario.
- Em `AssignmentsExpression`, ler slots atribuidos diretamente durante a construcao do mapa publico,
  sem a lista crua intermediaria.
- Cobrir vazio, null, falha, raiz marcavel e todas as visoes.

### Incremento 4 - Inventario de pontos e otimizacoes

- Atribuir slots e chaves aos quatro tipos marcaveis.
- Instalar a fronteira opaca de operacoes de colecao.
- Incluir filtros e todo corpo/predicado repetido por elemento na fronteira opaca.
- Transferir captura em folding, CSE e especializacao.
- Cobrir CSE compartilhada entre uma atribuicao e a expressao final, cuja segunda ocorrencia pode ser
  hit apesar de pertencer a outra parte da execucao.
- Comparar memoria otimizada e Oracle em testes focados, corpus e propriedades.

### Incremento 5 - Retencao, concorrencia e layout

- Provar que memorias concorrentes do mesmo plano sao isoladas e nao usam sincronizacao.
- Reter memorias, soltar engine/plano e verificar que valores e metadados sobrevivem sem reter o
  plano; fazer o inverso para provar que planos nao retem valores de execucoes anteriores.
- Medir com JOL o crescimento de nos e do grafo de plano para 10, 100 e 1.000 nos, uma memoria para
  cada um de muitos planos e muitas memorias do mesmo plano. O primeiro caso revela o custo nao amortizado do
  schema; o segundo prova o compartilhamento de chaves.
- Verificar restauracao de slots de Item Atual e descarte de memos.

### Incremento 6 - Gates finais

- Executar JMH pareado do caminho normal marcado contra controle sem gravacao ativa.
- Executar `computeWithMemory()` completo, travessia indexada, travessia por listas e perfil `gc`.
- Executar um sink sequencial sem I/O que represente a escrita de todos os campos de uma mini auditoria;
  disco, rede e banco reais ficam fora do microbenchmark para nao esconder custo do evaluator.
- Inspecionar inlining/branches com `perfasm` ou `PrintInlining` e atribuicao com JFR quando
  disponivel.
- Repetir o gate de producao no Java 21 de deployment.
- Atualizar plano, ADR, historico e resultados do prototipo com o veredito final.

## Gates de Aceite

### Funcional

- As quatro visoes executam uma vez e devolvem o mesmo resultado materializado de `compute()`.
- Variaveis participantes, sombreamento, ordem e valores efetivos obedecem ao contrato.
- Cada visao inclui somente variaveis da parte que executa; `AssignmentsExpression` omite dependencias
  exclusivas da expressao de resultado nao avaliada.
- Slots de variavel intercalados com Item Atual/memos produzem a mesma ordem publica do schema.
- Os quatro tipos de Ponto de Calculo respeitam alcance, ordem e null.
- `variableCount()`/`calculationCount()` coincidem com os tamanhos das listas e cada par `keyAt`/
  `valueAt` e igual ao record projetado no mesmo indice; listas rejeitam mutacao e acessores invalidos
  lancam `IndexOutOfBoundsException`.
- Curto-circuito, condicionais e coalescencia nao publicam ramos nao alcancados.
- Folding aninhado, CSE com hit que pula descendentes e especializacao preservam chaves, valores e
  ordem por ocorrencia.
- Operacoes de colecao e seus descendentes permanecem opacos.
- Filtros e qualquer corpo/predicado repetido por elemento permanecem opacos.
- Falhas nao devolvem memoria parcial.
- Memoria otimizada e Oracle sao equivalentes.

### Desempenho

- `compute()` tem zero B/op adicional.
- Qualquer regressao reproduzivel acima de 1% em latencia no plano marcado e investigada; nao se cria
  segundo plano para remove-la.
- O delta estrutural alvo de `computeWithMemory()` sobre a mesma computacao normal e: um envelope, uma
  `CalculationMemory`, no maximo um `Object[V]`, um `Object[K]` e, somente para alcance com lacunas,
  um `int[K]`. Recorder append-only e Materializacao Publica continuam contabilizados separadamente
  como working allocation e resultado normal. Schema, chaves e spans alocam na compilacao, nao por
  execucao.
- O caminho indexado nao aloca lista, iterator, entry, chave, span, lambda, builder ou mapa. Qualquer
  objeto adicional por execucao precisa de justificativa em perfil e comparacao JMH.
- A forma de branch escolhida inlineia e passa os gates de branches/op, branch-misses/op e codigo de
  maquina tanto em `compute()` quanto em descendentes opacos executados repetidamente.
- A representacao escolhida passa o gate vinculante de publicacao e consumo sequencial em `ns/op`,
  B/op e bytes retidos.
- O relatorio separa custo ja existente de Materializacao Publica do delta de memoria. A rota de memoria
  materializa o resultado exatamente uma vez e nao faz segunda copia de colecao/mapa capturado.
- A travessia indexada completa aloca zero B/op no evaluator; alocacoes do sink de persistencia sao
  responsabilidade do sink e medidas separadamente.
- Nao existe entry por item no freeze, `ArrayList`, recorder polimorfico, bitmap, mapa de identidade ou
  reconstrucao de chave por execucao.

### Retencao e Concorrencia

- `CalculationMemory` nao retem plano, nos, ambiente, fonte, AST ou Modelo Semantico.
- Plano e schema nao retem frame nem valores de execucoes anteriores.
- Um plano compartilhado produz memorias independentes sob concorrencia e reentrancia.
- Nenhum `ThreadLocal`, pool, lock, estado de geracao ou cache de resultado e introduzido.

## Fora de Escopo

- Trace temporal, eventos, snapshots, profundidade, ring buffer, `maxAuditEvents` ou memoria parcial.
- Classes `Captured*`, wrappers, decorators, segunda arvore ou segundo plano.
- Captura dentro de operacoes de colecao.
- Busca por nome, mapa derivado ou indice hash adicional.
- Sink/callback de persistencia, serializer oficial ou integracao JDBC/JPA.
- Copia profunda ou opcao de snapshot destacado.
- Cauda no frame em producao, estrategia adaptativa, dense+bitmap ou pooling.
- Telemetria de producao nesta etapa. Cauda so volta a ser candidata se dados posteriores mostrarem uma
  populacao material de planos densos grandes em que sua economia de working allocation seja decisiva.

## Decisoes Pendentes

Nenhuma decisao de produto permanece aberta. A issue #155 selecionou append-only; mode-first e contagem
durante captura permanecem os resultados da issue #139 e foram confirmados pelo gate final da issue
#147, condicionado somente a contadores de hardware ainda indisponiveis. Detalhes internos de capacidade
e crescimento continuam condicionados aos gates declarados, sem alterar o contrato publico ou introduzir
estrategia adaptativa em producao.

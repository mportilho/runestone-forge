# Plano Detalhado - Etapa 12 - Endurecimento e Verificacao

Este plano fecha o marco M4 do `exp-mk3`. Seu foco e tornar a implementacao resistente a Fontes de
Expressao Nao Confiaveis e valores de tenant, auditar o contrato de Diagnostico de Expressao e deixar
gates locais reproduziveis de corretude, concorrencia, retencao, alocacao e desempenho. Migracao v1,
CI/CD, tagging e publicacao foram retirados do escopo.

## Objetivo

Entregar um evaluator que aceite entradas nao confiaveis somente dentro de orcamentos deterministas,
sem alterar a semantica decimal, a ordem de efeitos, o Plano Imutavel unico, a Memoria de Calculo ou o
caminho quente escalar. Toda falha controlada pelo evaluator deve sair por diagnostico estruturado; o
processo nao deve depender de timeout interno, captura de erro fatal ou heuristica de regex
backtracking.

## Autoridade e Premissas

- `CONTEXT.md` define a linguagem do dominio.
- ADR 0019 continua normativo para equivalencia entre plano otimizado e Oraculo Sem Otimizacoes, com a
  excecao operacional de trabalho adicionada pela Etapa 12.
- ADR 0022 continua normativo para reuso por Engine de Expressao.
- ADR 0023 continua normativo para um unico plano e captura append-only da Memoria de Calculo.
- ADR 0024 define o modelo de Fonte de Expressao Nao Confiavel, orcamentos deterministas, Peso Retido de
  Compilacao e Regex Linear.
- A gramatica permanece congelada. Trocar o engine regex nao altera tokens ou precedencia.
- O Ambiente de Expressao e seus providers/membros Java registrados sao confiaveis. O evaluator valida
  suas bordas, mas nao tenta interromper ou sandboxar o corpo de codigo registrado.
- O deployment de referencia e Eclipse Temurin 21. Os tetos deste plano podem ser reduzidos se o stress
  nessa JVM provar que algum valor nao e seguro; nao podem ser elevados apenas para fazer um teste
  passar.

## Fora de Escopo

- Migracao v1 para v2, diagnosticos especiais de migracao, migrador e corpus diferencial legado.
- CI/CD, publicacao Maven, GitHub Release, tagging, assinatura e versionamento do reactor.
- Timeout interno, cancelamento assincrono, captura de `VirtualMachineError` ou isolamento de processo
  para providers registrados.
- Quota agregada por tenant, rate limiting ou limite global de chamadas concorrentes.
- Alteracao de gramatica, parser Pratt, Tier 1 e fusao de pipelines da Etapa 13.
- Estatistica ou administracao publica do cache.
- Garantia byte a byte para o Peso Retido de Compilacao.

## Estado Atual e Gaps de Entrada

| Area | Estado atual | Gap da Etapa 12 |
|---|---|---|
| Diagnosticos | modelo publico rico e codigos internos | sem registro de categoria/span/sugestao, ordem nao uniforme e corpus verifica principalmente o primeiro erro |
| Parser | SLL/LL e contexto `ThreadLocal` reutilizavel | sem limites de fonte/tokens/profundidade; error strategy LL pode reter a fonte |
| AST/resolver | nos posicionados e erros independentes acumulados | sem limite de nos e sem gate completo de ordenacao/exaustividade |
| Valores | materializacao por container, Item Atual e fatorial limitados | texto, profundidade, precisao, escala e trabalho variavel ainda nao limitados |
| Regex | operador literal precompilado; built-ins dinamicos | `java.util.regex` admite backtracking catastrofico e outputs nao limitados antes da alocacao |
| Runtime | Plano Imutavel e Escopo de Execucao isolado | cadeias de colecao e built-ins caros nao possuem saldo acumulado |
| Cache | 1.024 entradas por engine | quantidade nao limita payload hostil retido por entrada |
| Concorrencia | testes representativos e single-flight | sem contencao sustentada de fontes distintas e sem gate de virtual threads |
| Propriedades | nove propriedades, com boa cobertura numerica/oraculo | geracao estrutural estreita e sem propriedades dos novos orcamentos |
| Gates | JMH/JFR/JOL executados manualmente em etapas anteriores | perfil Maven apenas lista benchmarks; nao ha manifesto nem avaliacao automatica |
| Documentacao | glossario, ADRs e comentarios da gramatica | sem referencia publica da linguagem, API, diagnosticos ou hardening |

Baseline funcional observado antes da implementacao em 2026-09-07: `mvn -pl exp-mk3 -am test` verde em
75 s, com 1.178 testes no MK3 e 50 abortados pelo filtro atual do corpus/oraculo. Os aborts devem ser
eliminados por classificacao explicita; nao sao aceite final.

## Interface Publica de Limites

Um modulo publico profundo concentra a politica:

```java
ExpressionResourceLimits limits = ExpressionResourceLimits.builder()
        .maxSourceLength(16_384)
        .maxTokenCount(4_096)
        .maxSyntaxDepth(64)
        .maxAstNodeCount(4_096)
        .maxCurrentItemDepth(32)
        .maxMaterializedSize(10_000)
        .maxFactorialInput(1_000)
        .maxTextLength(1_048_576)
        .maxValueDepth(64)
        .maxNumericPrecision(10_000)
        .maxNumericScaleMagnitude(10_000)
        .maxRegexPatternLength(1_024)
        .maxEvaluationWork(1_000_000)
        .build();

ExpressionEnvironment environment = ExpressionEnvironment.builder()
        .resourceLimits(limits)
        .build();
```

`ExpressionResourceLimits` e final e imutavel, oferece `defaults()` e `builder()`, e faz toda validacao
no mutator do builder. O Ambiente copia a referencia imutavel e a expoe por `resourceLimits()`. Os
getters/setters diretos de `maxCurrentItemDepth`, `maxMaterializedSize` e `maxFactorialInput` saem antes
de M4; nao ha ponte deprecada para a API pre-GA.

### Defaults e tetos iniciais

| Propriedade | Default | Teto absoluto | Unidade |
|---|---:|---:|---|
| `maxSourceLength` | 16.384 | 262.144 | unidades UTF-16 |
| `maxTokenCount` | 4.096 | 65.536 | tokens armazenados, exceto EOF |
| `maxSyntaxDepth` | 64 | 256 | frames estruturais projetados |
| `maxAstNodeCount` | 4.096 | 65.536 | nos AST, incluindo links e targets |
| `maxCurrentItemDepth` | 32 | 64 | contextos simultaneos de Item Atual |
| `maxMaterializedSize` | 10.000 | 100.000 | elementos por container |
| `maxFactorialInput` | 1.000 | 10.000 | maior inteiro aceito |
| `maxTextLength` | 1.048.576 | 8.388.608 | unidades UTF-16 |
| `maxValueDepth` | 64 | 256 | containers recursivos por caminho |
| `maxNumericPrecision` | 10.000 | 100.000 | digitos de `BigDecimal.precision()` |
| `maxNumericScaleMagnitude` | 10.000 | 100.000 | `abs((long) scale())` |
| `maxRegexPatternLength` | 1.024 | 8.192 | unidades UTF-16 decodificadas |
| `maxEvaluationWork` | 1.000.000 | 100.000.000 | unidades abstratas de trabalho |

Zero e valido e desabilita efetivamente a capacidade correspondente. Nao existe sentinela de
"ilimitado". Mensagens de builder informam propriedade, valor fornecido e intervalo aceito. O corpus e
os benchmarks ordinarios ficam muito abaixo dos defaults; o probe atual de 1.000 nos continua aceito.

## Ordem de Enforcement

As validacoes seguem a ordem abaixo para nunca executar uma fase cara antes de seu guard rail:

1. `ExpressionEngine.compile` valida null e `maxSourceLength` antes de hash/chave/cache.
2. O seam sem cache repete `maxSourceLength` como defesa em profundidade.
3. Tokenizacao limitada interrompe `fill()` no primeiro token excedente, antes de alocar o restante.
4. Preflight sobre tipos de token calcula a profundidade projetada antes de SLL/LL.
5. `SemanticAstBuilder` conta nos durante a construcao, antes de criar a arvore acima do limite;
   `AstNodeIdAssigner` verifica defensivamente o total.
6. Resolver valida forma de constantes e pre-condicoes provaveis antes do folding.
7. Folding de built-ins oficiais recebe um saldo proprio de `maxEvaluationWork`.
8. Preparacao de inputs valida todos os overrides, forma e saldo antes do primeiro efeito da expressao.
9. Runtime debita trabalho imediatamente antes da unidade que o executara.
10. Materializacao Publica e freeze da Memoria de Calculo terminam usando o mesmo escopo/saldo local.

Fonte acima de `maxSourceLength` nao cria `CompilationCacheKey` e nao e cacheada. Outros resultados
deterministicos dentro do limite continuam sujeitos ao single-flight/cache normal. Esgotamento de
orcamento e terminal para a fase: emite um diagnostico e nao tenta acumular erros adicionais.

## Profundidade e Contagem Estrutural

`maxSyntaxDepth` mede a profundidade que chegaria as travessias recursivas, nao somente delimitadores.
O preflight cobre:

- grupos, colecoes, subscritos, filtros, argumentos, lambdas e condicionais;
- prefixos unarios consecutivos;
- potencia associativa a direita;
- cadeias associativas a esquerda que o builder representa como arvore left-deep.

Links sequenciais armazenados como lista nao aumentam artificialmente a profundidade. Seu total e
limitado por tokens/nos. Strings e comentarios nao participam porque o preflight opera sobre tokens. O
caso `limite`, `limite + 1`, entrada malformada e contexto reutilizado depois da rejeicao deve existir
para cada familia.

## Forma de Valores

Um unico validador interno aplica forma em literais materializados, defaults, overrides, argumentos e
retornos Java, resultados de built-ins, constantes dobradas e Materializacao Publica:

- texto e chave textual: `maxTextLength`;
- numero: precisao e magnitude de escala, calculada sem overflow;
- container: `maxMaterializedSize` por container e `maxValueDepth` por caminho;
- null externo, chave null, elemento null e retorno null continuam violacoes de contrato existentes.

Containers ciclicos terminam por profundidade antes de estourar a pilha. Compartilhamento aciclico nao
e tratado como ciclo. Expansoes de tamanho previsivel (`repeat`, padding e concatenacao) calculam o
tamanho com aritmetica saturada antes de alocar. `replaceAll` e `split` constroem resultados de forma
limitada em vez de chamar uma API que materialize o resultado inteiro antes da verificacao.

Validacao numerica e orientada por prova. Nao existe chamada generica a `precision()`/`scale()` em todo
no aritmetico. Bordas e resultados publicos sempre validam; operacoes expansoras validam antes/depois;
operacoes cuja metadata, limite de entrada, quantidade de nos e `MathContext` provam teto seguro podem
omitir a checagem intermediaria. O gate JMH decide qualquer duvida no caminho financeiro.

## Orcamento de Trabalho de Avaliacao

`maxEvaluationWork` e operacional: mede trabalho variavel efetivamente realizado pelo tier. Cada
compilacao e cada execucao recebe saldo novo. Nao existe contador por tenant, engine ou thread.
Reentrancia iniciada por provider cria outro escopo e outro saldo.

### Modulo e escopo

- `WorkCostPolicy` concentra formulas saturadas e classificacao `CONSTANT`, `METERED` ou
  `TRUSTED_UNMETERED`.
- Toda Operacao de Colecao e todo built-in oficial aparece no registro; ausencia falha teste de
  exaustividade.
- Provider customizado e implicitamente `TRUSTED_UNMETERED`; apenas conversao/validacao feita pelo
  evaluator e medida.
- `ExecutionScope` escalar preserva o layout atual.
- `BudgetedExecutionScope` adiciona um `int remainingWork` somente quando a Visao executada pode
  alcancar trabalho medido. O teto de 100 milhoes cabe em `int`; formulas usam `long` saturado antes do
  debito.
- Debito que excederia o saldo falha antes de iniciar aquela unidade. Efeitos anteriores permanecem;
  nao ha rollback.

### Matriz minima de debito

| Familia | Debito |
|---|---|
| `all`, `any`, filtro, `map`, `reduce` | uma unidade antes de cada item alcancado; trabalho da lambda soma no mesmo saldo |
| `sum`, `avg`, igualdade estrutural | uma unidade por item/par realmente visitado |
| slice, wildcard, `keys`, `values`, snapshots e materializacao | uma unidade por entrada lida/copiada |
| `count` sobre container canonico | constante; `size()` nao simula percurso |
| membership generico | uma unidade por candidato visitado ate curto-circuito |
| membership hash/binario | custo estimado do lookup realmente usado, sem varredura ficticia |
| `sortBy` | chaves incrementalmente; custo fixo saturado baseado em `n * ceil(log2(n))`; copia por item |
| regex | compilacao por tamanho do padrao; match por produto saturado padrao x texto |
| `replaceAll`/`split` | regex + match/segmento + unidades de output, respeitando limites de texto/container |
| expansao textual | unidades proporcionais ao output previsto/produzido |
| fatorial | uma unidade por multiplicacao autorizada |
| potencia, raiz e built-ins caros | estimativa conservadora baseada em argumento, precisao e algoritmo oficial |
| adapter Java | uma unidade por valor que o evaluator converte/valida/materializa |

As formulas sao deterministicas e documentadas, mas a passagem exatamente na fronteira nao e contrato
entre otimizacoes. Folding paga durante compilacao e nao novamente em runtime; um lookup otimizado paga
seu custo otimizado. Por isso apenas `*_WORK_EXCEEDED` fica fora da equivalencia estrita do ADR 0019.
Testes gerais de equivalencia usam saldo amplo; testes de trabalho provam ausencia de subcontagem em
cada tier.

## Regex Linear

RE2/J e dependencia direta de producao. Nao existe `java.util.regex.Pattern` no caminho da linguagem:

- operador `=~`/`!~`: padrao literal validado e preparado pelo resolver;
- padrao literal de built-in: elegivel a preparacao/folding;
- padrao dinamico: validado e compilado por chamada dentro dos limites;
- lookaround, backreference e construcao fora do subset RE2/J: diagnostico estruturado;
- falha de regex dinamica pertence ao span da chamada e nao e `RUNTIME_PROVIDER_FAILURE`;
- texto, padrao, trabalho, output e quantidade de segmentos sao validados antes/durante materializacao.

A suite inclui padroes adversariais conhecidos de engines backtracking e mede escala crescente. O gate
nao exige que RE2/J seja mais rapido em padrao pequeno; exige crescimento aproximadamente linear e
ausencia de fallback.

## Cache com Peso Retido de Compilacao

`CacheConfig` passa a conter:

```java
CacheConfig.defaults(); // 1024 entradas, 64 Mi unidades de peso, sem expiracao

CacheConfig.builder()
        .maximumEntries(2_048)
        .maximumRetainedWeight(128L * 1024 * 1024)
        .expireAfterAccess(Duration.ofHours(6))
        .build();
```

- `maximumEntries`: positivo, default 1.024, teto 65.536.
- `maximumRetainedWeight`: positivo, default 64 Mi unidades, teto 1 Gi unidade.
- peso estimado cobre fonte exata da chave, plano, diagnosticos e constantes dobradas recursivas.
- componentes compartilhados e confiaveis do Ambiente nao entram.
- JOL calibra pesos conservadores; o nome nao promete bytes exatos.
- o `Weigher` usa o maior entre peso estimado e cota-base derivada de
  `maximumRetainedWeight / maximumEntries`, permitindo a um unico `maximumWeight` limitar peso e
  quantidade.
- entrada pesada demais e entregue ao chamador, mas nao fica residente.
- hit nao recalcula peso e compute nunca consulta o cache.

Testes cobrem limite por quantidade, peso, combinacao, sucesso/falha, entrada individual acima do
saldo, eviction, nova geracao, single-flight e validade de referencias antigas. Os gates de hit/miss da
Etapa 9 continuam vinculantes.

## Diagnosticos

`DiagnosticCode` vira o registro interno unico. Cada item declara:

```text
codigo textual + categoria + severidade + politica de span + politica de sugestao
```

Politica de span: `REQUIRED`, `OPTIONAL` ou `FORBIDDEN`. Falha originada na fonte e `REQUIRED`; entrada
externa invalida e `FORBIDDEN`; causa mista documentada pode ser `OPTIONAL`. Sugestao e obrigatoria
somente quando existe correcao local e nao ambigua. Mensagem/sugestao podem evoluir; codigo, categoria,
severidade e politica de span ficam estaveis depois de M4.

### Ordenacao canonica

1. diagnosticos posicionados antes dos sem trecho;
2. offset inicial crescente;
3. no mesmo offset, `ERROR` antes de `WARNING`;
4. categoria;
5. codigo textual;
6. offset final como desempate.

Parser, AST e resolver aplicam o mesmo comparador. Erros semanticos independentes continuam acumulados;
`Tipo Invalido` suprime apenas cascatas dependentes. Orcamento esgotado e terminal e nao participa da
acumulacao posterior.

### Familias novas minimas

- parsing/estrutura: fonte, tokens, profundidade e nos excedidos;
- semantica/valor constante: texto, profundidade, precisao, escala, regex e trabalho excedidos;
- runtime: texto, profundidade, precisao, escala, materializacao e trabalho excedidos;
- cache/configuracao nao produz Diagnostico de Expressao; builder invalido usa `IllegalArgumentException`.

Cada dimensao/fase possui codigo especifico; nao ha codigo por operador. Entrada externa nula ou
incoercivel passa a `ExpressionExecutionException` com diagnostico sem trecho. O audit tambem corrige
spans grosseiros de postfix encadeado, usa related information quando uma segunda ocorrencia referencia
a primeira e remove codigos/categorias de migracao sem produtor.

### UTF-16

Offsets e colunas usam unidades UTF-16, coerentes com `String` e clientes Java/LSP. O parser converte
indices de code point do ANTLR. Testes cobrem BMP, caracteres suplementares, multiline, EOF e spans
vazios sem dividir pares substitutos.

## Concorrencia, Retencao e Stress

### Suite cotidiana

- plano compartilhado entre threads com escopos e Memorias de Calculo isolados;
- single-flight de sucesso e falhas deterministicas;
- fontes distintas concorrentes em SLL, fallback LL, erro lexical e erro semantico;
- contexto do parser sem fonte, token, parse tree ou `CapturingErrorStrategy` retido;
- limites em `limite - 1`, `limite`, `limite + 1` usando valores pequenos;
- zero teste skipped, disabled ou aborted.

O teste corpus/oraculo seleciona somente casos planejaveis antes de criar casos dinamicos. Um gate de
estrutura prova que todo Caso de Expressao pertence exatamente a uma suite executavel; casos de
diagnostico nao aparecem como abort do oraculo.

### Perfil `stage12-stress`

- JVM filha Temurin 21 com `-Xms512m -Xmx512m -Xss1m` e timeout de processo;
- fonte/tokens/profundidade/nos e valores proximos aos tetos;
- regex adversarial e crescimento de input;
- expansao textual, containers profundos e cache preenchido por fontes distintas;
- pool de plataforma `min(32, max(4, CPUs * 2))`;
- 10.000 tarefas em virtual threads;
- pelo menos 100 mil compilacoes/execucoes combinadas;
- todas as visoes, compute normal/com memoria, CSE, Item Atual, current time, sucesso e falha;
- barreiras/latches, nunca `sleep`; providers de teste thread-safe;
- artefatos e logs em `target/stage12/`.

O teste nao tenta causar OOM. Ele prova que a rejeicao ocorre antes do trabalho/alocacao proibido e que
a JVM filha termina normalmente. Retencao usa travessia deterministica do grafo; GC/WeakReference apenas
complementam e nunca decidem o build.

## Property-based

- propriedades essenciais usam pelo menos 1.000 tentativas no `test` normal;
- propriedades volumosas usam pelo menos 10.000 no perfil de stress;
- o gerador de AST deixa de escolher apenas formas preconstruidas e passa a ser recursivo e limitado;
- geradores cobrem fontes validas/invalidas, Unicode suplementar, valores profundos, limites e
  operacoes de custo variavel;
- plano otimizado/oraculo compara valor, escala, falha nao operacional, span, efeitos e Memoria de
  Calculo com saldo amplo;
- propriedades especificas comparam debito com um oraculo simples de custo;
- seed de falha fica no relatorio; o caso minimizado entra no Corpus de Expressoes como regressao;
- o smoke de soma de inteiros e removido, pois nao prova comportamento do produto.

## Gates Locais

### Nivel cotidiano

```bash
mvn -pl exp-mk3 -am test
```

Meta de engenharia: ate dois minutos na maquina de referencia. Inclui testes unitarios, corpus,
propriedades essenciais, diagnosticos, limites pequenos e concorrencia representativa.

### Nivel de stress

```bash
mvn -pl exp-mk3 -am -Pstage12-stress verify
```

Executa somente depois da suite cotidiana. Falha por timeout, processo filho anormal, teste pulado ou
artefato ausente.

### Gate integral manual

```bash
exp-mk3/scripts/run-stage12-gates.sh
```

O script valida JDK/Maven, executa suite e stress, roda o manifesto JMH com JSON e `-prof gc`, avalia
limiares, produz JOL, captura JFR e grava tudo sob `target/stage12/`. Nao altera Git, versao, tag ou
repositorio remoto.

## Manifesto JMH

`docs/perf/stage12-gates.md` e um manifesto de metodos, pares, metricas e limiares; um arquivo de dados
versionado consumido pelo script evita parsing de prosa. O protocolo padrao continua 3 forks, 5 x 500 ms
de warm-up, 10 x 500 ms de medicao, heap fixo e mesma JVM/maquina, salvo justificativa especifica.

| Familia | Cobertura vinculante | Gate |
|---|---|---|
| escalar | `Phase5BaselineBenchmark.arithmeticCompute`, `logicalCompute`, funcao registrada e Memoria sem colecao | zero B/op adicional; delta pareado dentro de +/-1% |
| colecao | `map`, `mapThenSum`, `allShortCircuit`, `sortBy`, `reduce`, wildcard, filtro e lambda aninhada | zero alocacao por debito; regressao pareada <=5% |
| compilacao | parse warm, compilacao sem cache e fontes proximas dos limites aceitos | sem regressao >5% fora da banda; rejeicao limitada caracterizada separadamente |
| cache | pipeline, miss, hit puro e hit+visao da Etapa 9 | miss <=10%; hit >=20x/99%; hit+visao >=10x/95% |
| regex | literal, dinamica, replace/split e serie adversarial crescente | sem fallback; crescimento aproximadamente linear; latencia simples registrada |
| memoria | `compute`, `computeWithMemory`, percurso indexado/listas | preserva gates aceitos da Etapa 10 |

JMH `gc` decide `B/op`. JOL prova que `ExecutionScope` escalar nao cresceu e calibra pesos do cache. JFR
e evidencia obrigatoria para origem de alocacao e CPU no fechamento, mas nao tem parser automatico de
aprovacao. Contadores de hardware nao sao gate.

Benchmark fora do manifesto e caracterizacao. Falha vinculante exige perfil e correcao; excecao de
desempenho so entra com causa, dados e justificativa em `docs/perf/performance-history.md`. Regex Linear
nao pode ser revertida por ficar mais lenta em padrao benigno.

Um resultado historico isolado nao reprova limiar de latencia. Deltas sao decididos por controle
pareado na mesma execucao quando houver seam interno, ou por execucoes baseline/candidata alternadas na
mesma maquina e com bandas de erro. Delta fora de +/-1% sem causa identificada exige remedicao antes de
qualquer veredito.

## Documentacao Publica

Entregas versionadas:

- `exp-mk3/README.md`: inicio rapido e indice;
- `docs/reference/language.md`: sintaxe, tipos, operadores, precedencia, avaliacao, navegacao, colecoes,
  null de runtime, temporais, Regex Linear e residuos;
- `docs/reference/diagnostics.md`: tabela completa do registro e estabilidade;
- `docs/guides/api.md`: Engine, Ambiente, limites, compilacao, visoes, overrides, providers,
  concorrencia e Memoria de Calculo com percurso indexado primeiro;
- `docs/guides/hardening.md`: modelo de ameaca, tuning, tetos, providers confiaveis, deadline externo,
  cache e observacao de falhas;
- `docs/perf/stage12-gates.md`: ambiente, manifesto, comandos, limiares e leitura dos resultados.

Blocos marcados de expressao sao extraidos por teste e compilados/executados. A tabela de diagnosticos
e comparada ao registro. Cada linha da tabela de precedencia aponta para caso AST. O Javadoc da API roda
com doclint; `target/reports/apidocs` continua artefato, nao fonte versionada.

## Incrementos de Implementacao

Cada incremento comeca por teste direcionado e termina com `mvn -pl exp-mk3 -am test` verde.

### Incremento 1 - Baseline e inventario

- Capturar JMH/JOL/JFR antes de alterar o hot path.
- Registrar maquina, JDK, Maven, heap, forks, comandos e JSON.
- Congelar lista de codigos, emissores, loops, built-ins e alocadores expansores.
- Criar manifesto inicial sem ainda aplicar novos limiares.

### Incremento 2 - Limpeza do escopo cancelado

- Remover categoria/fases/fixtures `TBD` exclusivos de migracao.
- Remover benchmark e dependencia do modulo legado.
- Classificar corpus/oraculo sem `Assumptions.abort` e chegar a zero skips.
- Reconciliar plano-mestre e referencias historicas ainda normativas.

### Incremento 3 - Contratos publicos e diagnosticos

- Introduzir `ExpressionResourceLimits` e migrar Ambiente/consumidores.
- Adicionar tetos e zero uniforme.
- Transformar `DiagnosticCode` em registro com metadata e comparador canonico.
- Criar gates de cobertura do registro, lista completa e acumulacao sem cascata.

### Incremento 4 - Compilacao limitada

- Rejeitar fonte antes do cache e no seam interno.
- Token stream limitado e preflight de profundidade projetada.
- Contagem de nos durante AST build.
- Converter spans ANTLR para UTF-16.
- Limpar error handler LL e provar nao retencao do grafo completo do parser.

### Incremento 5 - Forma, folding e Regex Linear

- Aplicar limites de texto, numero e profundidade em todas as bordas.
- Prevalidar expansoes constantes antes de folding.
- Integrar RE2/J no operador e built-ins; remover Java Pattern do caminho da linguagem.
- Implementar replace/split limitados.
- Criar `WorkCostPolicy` e meter folding de built-ins oficiais.

### Incremento 6 - Runtime medido

- Criar `BudgetedExecutionScope` sem alterar layout escalar.
- Instrumentar matriz de colecao, regex, texto, numerico e adapters.
- Manter escopo ate Materializacao Publica/freeze, sem holder intermediario.
- Provar ordem de efeitos, isolamento concorrente e ausencia de subcontagem.
- Repetir JMH escalar/colecao antes de seguir.

### Incremento 7 - Cache ponderado

- Adicionar `maximumRetainedWeight` e tetos ao `CacheConfig`.
- Calibrar estimador com JOL e instalar weigher composto.
- Provar quantidade, peso, single-flight, nao residencia de entrada pesada e gates hit/miss.

### Incremento 8 - Stress e propriedades

- Criar perfil `stage12-stress` e runner em JVM filha.
- Ampliar geradores e concorrencia de parser/planos.
- Cobrir limites nos tres pontos de fronteira e regex adversarial.
- Eliminar todo skip/disabled/abort.

### Incremento 9 - Fechamento

- Implementar avaliador do manifesto JMH e script integral.
- Executar JMH GC, JOL e JFR no Temurin 21 de referencia.
- Ajustar para baixo qualquer teto inseguro e repetir gates afetados.
- Escrever documentacao publica e testes de drift.
- Registrar veredito em `performance-history.md`, fechar decisoes e reconciliar todos os documentos.

## Criterios de Aceite

### Funcionais e de seguranca

- Toda Fonte de Expressao Nao Confiavel encontra limite antes de trabalho/alocacao nao limitado.
- Defaults aceitam corpus e benchmarks; tetos passam stress na JVM filha configurada.
- Regex da linguagem usa somente RE2/J e rejeita subset incompatível por diagnostico.
- Valores de borda e publicos respeitam texto, profundidade, precisao, escala e container.
- Debito de trabalho e isolado por compilacao/execucao, sem estado global ou por thread.
- Providers customizados permanecem confiaveis e nao medidos; adapters continuam medidos.
- Falhas de limite possuem codigo/span corretos e nao escapam como excecao crua.

### Diagnosticos

- Todo codigo emitido esta no registro e possui teste emissor.
- Categoria, severidade, span e sugestao obedecem metadata.
- Ordem e lista completa sao deterministicas.
- Erros semanticos independentes acumulam; budget terminal nao continua.
- Unicode suplementar preserva offsets/colunas UTF-16.
- Nao resta categoria, fase ou codigo de migracao.

### Concorrencia e retencao

- Plano, Memoria de Calculo, Item Atual, tempo e work budget permanecem isolados.
- Parser concorrente nao mistura estado nem retem fonte, tokens, parse tree ou error strategy.
- Threads de plataforma e virtuais sao corretas; desempenho relativo apenas documentado.
- Cache respeita quantidade/peso e nao retem AST, Modelo Semantico ou fonte duplicada.

### Desempenho e alocacao

- Caminho escalar: zero `B/op` adicional e delta dentro de +/-1%.
- Colecoes: zero alocacao por debito e regressao <=5% ou excecao documentada depois de perfil.
- Cache preserva os gates da Etapa 9.
- Memoria de Calculo preserva os gates de software aceitos da Etapa 10.
- Regex prova escala aproximadamente linear; nao ha gate de contador de hardware.
- JMH/JOL/JFR e historico identificam JVM, maquina, comando e artefatos.

### Processo local e documentacao

- `mvn -pl exp-mk3 -am test` verde dentro do budget de engenharia.
- perfil `stage12-stress` verde e JVMs filhas terminam normalmente.
- script integral verde uma vez no Temurin 21 de referencia.
- zero skipped, disabled ou aborted.
- documentos publicos existem, exemplos sao executaveis e tabelas nao divergem dos registros.
- nenhuma decisao de planejamento permanece aberta.

## Impacto na Etapa 13

Pratt, Tier 1 e fusao de colecoes devem consumir `ExpressionResourceLimits`, `WorkCostPolicy`, registro
diagnostico e gates deste plano. Um tier novo pode reduzir trabalho operacional e, portanto, alterar a
fronteira `*_WORK_EXCEEDED`, mas continua obrigado a equivalencia semantica do ADR 0019 e a testes de
nao subcontagem. Nenhuma trilha pode reintroduzir regex backtracking, bypass de forma, plano mutavel ou
estado global de budget.

# Plano Detalhado - Etapa 9 - Cache de Compilacao

Este plano detalha a Etapa 9 do `exp-mk3` depois do fechamento da Etapa 8. Ele consolida as decisoes registradas em `decisoes-etapa-9-cache-de-compilacao.md`, complementa o plano-mestre e produz o ADR 0022.

## Objetivo

Estabelecer o Engine de Expressao como fronteira publica longeva de compilacao, RuntimeServices e reuso limitado. Dentro de um engine, chamadas para a mesma fonte textual e o mesmo Identificador de Instancia do Ambiente compartilham uma geracao residente de Resultado de Compilacao e executam o pipeline uma unica vez por geracao, inclusive quando o resultado e uma falha sintatica ou semantica.

A Etapa 9 nao otimiza `compute`. A carga financeira principal compila poucas formulas, guarda suas Visoes de Expressao e as executa muitas vezes por contrato; nenhuma consulta ao cache, contagem ou escrita compartilhada entra nesse caminho quente. O cache protege a integracao contra recompilacao acidental, tempestade concorrente de compilacao e duplicacao de Planos Imutaveis.

## Autoridade e Premissas

- ADRs aceitos, `CONTEXT.md`, o Corpus de Expressoes e os planos detalhados vigentes definem o contrato.
- O ADR 0014 e normativo: identidade de Ambiente de Expressao e por instancia, e ambientes construidos separadamente nao compartilham compilacao mesmo quando equivalentes.
- O ADR 0019 continua normativo para os planos otimizados produzidos em cache; o cache nao cria uma terceira forma de plano nem expoe o Oraculo Sem Otimizacoes.
- O ADR 0020 continua normativo: ambientes sao longevos e carregam pontos de entrada de invocacao preparados que todos os planos daquele ambiente reutilizam.
- O ADR 0022 e produto desta etapa e torna o Engine de Expressao o limite de reuso de compilacao.
- A API publica continua provisoria e pode mudar de forma incompativel antes da GA.
- Toda a suite existente permanece verde; a Etapa 9 nao muda semantica de linguagem, resultado, diagnostico, ordem ou efeito.
- Caffeine passa a ser dependencia direta de `exp-mk3`; o modulo nao depende acidentalmente da transitividade de `runestone-toolkit` nem usa os wrappers genericos de memoizacao do toolkit.

## Perfil de Uso Alvo

O perfil que orienta a API e um catalogo de produtos com formulas versionadas de juros:

1. A aplicacao constroi um Engine de Expressao e um Ambiente de Expressao longevos.
2. Ao carregar ou ativar uma versao de formula, compila a fonte e cria uma Visao de Expressao matematica.
3. O catalogo da aplicacao associa produto e versao comercial a essa visao.
4. Cada contrato reutiliza diretamente a visao e fornece seus valores como sobrescritas de Simbolos Externos.
5. O servico de dominio aplica o valor calculado ao saldo; o avaliador nao altera o contrato.

Produto, contrato e versao comercial nao entram na chave tecnica. Duas versoes comerciais com a mesma fonte exata e o mesmo ambiente podem compartilhar uma Expressao Compilada, enquanto o catalogo da aplicacao preserva suas identidades de negocio. Esse catalogo fica fora do modulo.

## Estado Atual e Estrategia de Reaproveitamento

| Entrega | Estado observado | Acao planejada |
|---|---|---|
| Fachada de compilacao | `ExpressionCompiler` estatica, com `compile` e `compileOrThrow` | Extrair pipeline interno e substituir a fachada publica por `ExpressionEngine` |
| Engine | Inexistente | Adicionar singleton default e builder para engines isolados |
| Cache | Pacote interno reservado, sem implementacao | Um Caffeine limitado por engine |
| Dependencia Caffeine | Disponivel apenas de forma transitiva por `runestone-toolkit` | Declarar diretamente em `exp-mk3` |
| Resultado | `ExpressionCompilationResult` fechado, imutavel, com `Success` e `Failure` | Usar o resultado completo como valor cacheado |
| Expressao Compilada | Retem Plano Imutavel, RuntimeServices e warnings | Preservar; compartilhar a mesma instancia nos hits da geracao |
| Visoes | Fachadas finas sobre o mesmo plano e RuntimeServices | Preservar; completar a prova explicita para `asAssignments()` |
| RuntimeServices | Singleton UTC associado pela fachada estatica; injecao de `Clock` somente em seam de teste | Tornar propriedade de cada engine e expor `Clock` no builder |
| Parser | Instancia global com lexer/parser por `ThreadLocal` | Manter global, aquecer uma vez e liberar entradas retidas |
| Nao retencao | Teste percorre o plano, nao o resultado cacheado nem o contexto do parser | Estender com gates deterministas do pipeline completo |
| Concorrencia | Plano compartilhado e escopos isolados ja provados | Acrescentar single-flight, isolamento de engine e geracoes |
| Benchmarks | Compilacao uncached e startup do parser ja possuem seams | Preservar e adicionar miss, hit e hit com visao |

## Gate de Entrada

O primeiro incremento verifica e registra os invariantes abaixo antes de mudar a API publica:

- `ExpressionCompilationResult`, `CompiledExpression`, Plano Imutavel e Visoes de Expressao sao imutaveis e seguros para compartilhamento.
- A compilacao orientada a resultado e `compileOrThrow` usam um unico pipeline; nao existe rota semantica paralela.
- O benchmark `fullUncachedCompilation` atravessa parser, AST, resolvedor e planner em toda invocacao.
- O Plano Imutavel nao retem `ExpressionEnvironment`, Arvore Semantica de Expressao, Modelo Semantico, parse tree ou fonte.
- Toda Visao de Expressao usa o mesmo Plano Imutavel e os mesmos RuntimeServices da Expressao Compilada.
- O `ThreadLocal` do parser retem hoje a ultima fonte, tokens e parser context; isso e violacao conhecida a fechar no incremento dois, nao motivo para enfraquecer o gate de nao retencao.
- Caffeine 3.1.8 esta centralizado no parent, mas precisa de declaracao direta no `pom.xml` de `exp-mk3`.

Violacao nova e bug interno. A Etapa 9 nao transforma falha interna em Diagnostico de Expressao para conseguir preencher o cache.

## Contrato do Engine de Expressao

`ExpressionEngine` e o unico ponto publico de compilacao:

```java
ExpressionEngine engine = ExpressionEngine.defaultEngine();

ExpressionCompilationResult result = engine.compile(source, environment);
CompiledExpression compiled = engine.compileOrThrow(source, environment);
```

Para isolamento:

```java
CacheConfig cacheConfig = CacheConfig.builder()
        .maximumEntries(2_048)
        .expireAfterAccess(Duration.ofHours(6))
        .build();

ExpressionEngine engine = ExpressionEngine.builder()
        .cacheConfig(cacheConfig)
        .clock(clock)
        .build();
```

- `ExpressionEngine.defaultEngine()` usa inicializacao lazy segura, `CacheConfig.defaults()` e `Clock.systemUTC()`.
- `ExpressionEngine.builder()` tambem parte desses defaults e produz uma instancia isolada e imutavel.
- `Clock` define valores temporais correntes das expressoes compiladas por aquele engine. Nao participa da chave porque engines nunca compartilham entradas.
- `RuntimeServices` permanece interno e e criado uma vez por engine.
- `ExpressionCompiler` deixa de ser API publica. Nao permanece delegador por compatibilidade, pois a API ainda nao foi publicada como GA e dois pontos de entrada esconderiam o proprietario do cache.
- Nao ha bypass publico, cache desabilitavel, estatisticas, invalidacao, manutencao, `close` ou outra operacao administrativa na primeira versao.

## Organizacao e Direcao de Dependencias

- `ExpressionEngine` e `CacheConfig` pertencem ao pacote publico `api`; ambos sao concretos, finais e imutaveis, sem interface publica de uma unica implementacao.
- Chave, Caffeine, `Ticker`, carregador e qualquer operacao de manutencao pertencem a `internal.cache`.
- O pipeline de compilacao permanece interno e depende dos contratos publicos de fonte, ambiente, resultado e diagnostico; consumidores publicos nunca dependem de parser, AST, semantica, plano ou cache internos.
- Nenhum tipo Caffeine aparece em construtor, metodo, retorno ou excecao publica. Trocar a biblioteca nao altera o contrato do engine.
- `RuntimeServices` permanece em `internal.runtime`; apenas `Clock`, tipo da JDK, atravessa o builder publico.
- Nao nasce novo modulo Maven nem interface de cache. A fronteira existente do modulo e suficiente e evita cerimonia sem segunda implementacao.

## Contrato de CacheConfig

`CacheConfig` e publico e imutavel:

- `defaults()` equivale a 1024 resultados e nenhuma expiracao.
- `builder().maximumEntries(int)` aceita apenas valor positivo.
- `builder().expireAfterAccess(Duration)` e opcional e aceita apenas duracao positiva.
- Ausencia de expiracao nao e representada por `null` ou `Optional` na API publica.
- O `Ticker` monotono do Caffeine nao e o `Clock` da linguagem e permanece seam interno injetavel em testes.
- Capacidade usa `maximumSize`, nao `maximumWeight`. Numero de nos nao mede constantes, defaults, providers, servicos, fonte ou falhas sem plano, e uma heuristica composta nao imporia limite real de heap.

Expiracao por acesso, e nao por escrita, evita recompilar periodicamente uma formula ativa. O limite de quantidade continua valendo com ou sem expiracao.

## Chave, Valor e Identidade

A chave interna contem:

```text
(source exata por String.equals, environmentId)
```

- Fonte nao e normalizada, copiada, internada nem substituida exclusivamente por hash.
- Tipo de Visao de Expressao, `Clock`, produto, versao comercial e configuracao de resultado nao entram na chave.
- A chave retem uma referencia intencional a fonte enquanto a entrada estiver residente; o valor nao retem outra copia.
- Outro engine possui outro cache e nunca consulta esta entrada, mesmo com a mesma fonte e a mesma instancia de ambiente.

O valor e o `ExpressionCompilationResult` completo:

- `Success` retem uma Expressao Compilada e warnings.
- `Failure` retem diagnosticos sintaticos ou semanticos deterministas.
- Nao existe cache negativo separado nem TTL diferente para falhas.
- Um hit de `compile` devolve a mesma instancia de `Success` ou `Failure` da geracao.
- Um hit bem-sucedido de `compileOrThrow` devolve a mesma Expressao Compilada.
- Uma falha de `compileOrThrow` cria uma nova `ExpressionCompilationException` sobre os diagnosticos imutaveis cacheados; excecao e stack trace nunca sao compartilhados.

Excecao interna, `Error` ou outra falha inesperada do pipeline escapa da carga e nao instala entrada. A chamada posterior pode tentar novamente.

## Single-Flight e Geracoes

O carregamento atomico de Caffeine e parte do contrato:

- Chamadas concorrentes da mesma chave executam o pipeline uma unica vez por geracao.
- Chamadores concorrentes recebem o mesmo Resultado de Compilacao, tanto em sucesso quanto em falha esperada.
- Falha interna da carga nao deixa valor parcial nem envenena chamadas posteriores.
- Eviction ou expiracao encerra apenas a residencia da geracao. Uma compilacao futura pode produzir outro resultado e outro plano.
- Expressao Compilada e Visoes de Expressao entregues antes da remocao permanecem validas e thread-safe; cache nao e proprietario de sua validade.
- Uma geracao antiga mantida pela aplicacao e uma nova geracao residente podem coexistir. Evitar essa coexistencia exigiria registro global de referencias e esta fora do desenho.

O cache nunca e consultado por `compute`. A aplicacao guarda e reutiliza a Visao de Expressao adequada ao seu ciclo de vida.

## Parser e Warm-up

O parser continua unico no modulo. Criar um parser por engine multiplicaria `ThreadLocal`s, fontes retidas e custo de warm-up sem aumentar isolamento do cache.

- A construcao do primeiro engine executa sincronicamente um warm-up global protegido contra concorrencia.
- O warm-up usa um conjunto pequeno e fixo de expressoes internas representativas e nao le o Corpus de Expressoes em runtime.
- Engines posteriores, inclusive isolados, observam o estado aquecido e nao repetem trabalho.
- Falha das expressoes internas de warm-up e bug de inicializacao.
- O pipeline envolve parsing e materializacao da AST em `try/finally`. Depois que a AST ou os diagnosticos foram materializados, o contexto da thread solta fonte, token stream e parse tree.
- O reuso de instancias de lexer/parser e preservado quando puder ser feito sem manter o input anterior; se a API do ANTLR impedir a liberacao segura, corretude de retencao prevalece sobre o reuso e a decisao e medida.

## Pipeline Interno Sem Cache

Existe uma unica implementacao interna de:

```text
source + Ambiente de Expressao + RuntimeServices
    -> Resultado de Compilacao
```

Ela executa parser, materializacao da AST, resolucao semantica e construcao do plano. O carregador Caffeine chama esse pipeline. Testes e JMH possuem acesso interno ao mesmo seam para provar compilacao sem cache; consumidores publicos nao.

`compileOrThrow` nao e outro pipeline: chama `compile`, retorna o sucesso ou cria a excecao publica a partir da falha.

## Nao Retencao

Os gates sao deterministas:

- Percorrer chave e Resultado de Compilacao e provar que a referencia integral da fonte existe apenas na chave.
- Provar que o valor nao alcanca parse tree, Arvore Semantica de Expressao, Modelo Semantico ou Ambiente de Expressao inteiro.
- Inspecionar o contexto reutilizavel do parser depois de sucesso, falha sintatica e falha semantica e provar que nao alcanca fonte, tokens nem parse tree anteriores.
- Cobrir planos escalares, navegacao, operacoes de colecao, constantes dobradas, funcoes registradas e Subexpressao Comum Memoizada, reutilizando e ampliando `ExecutionPlanNonRetentionTest`.

Teste dependente de `System.gc()` nao e gate. `WeakReference`, heap dump ou JFR podem complementar a caracterizacao, sem decidir o build.

## Verificacao Funcional e Concorrente

A matriz obrigatoria cobre:

- hit de sucesso e de falha por identidade;
- `compileOrThrow` com mesma Expressao Compilada em sucesso e excecao nova em falha;
- fonte igual por conteudo em objetos `String` distintos;
- fonte com diferenca textual produzindo outra entrada;
- mesma fonte com outro Ambiente de Expressao;
- mesma fonte e ambiente em engines distintos e com `Clock`s distintos;
- single-flight concorrente em sucesso e falha, contado no seam interno;
- falha interna seguida por nova tentativa;
- capacidade com `maximumEntries` pequeno;
- expiracao por acesso com `Ticker` falso, sem `sleep`;
- nova geracao depois de eviction e expiracao;
- execucao correta de uma visao antiga depois da nova geracao;
- warm-up unico sob construcao concorrente de engines;
- liberacao do parser em todas as saidas;
- plano compartilhado explicitamente por `asResult`, `asMath`, `asLogical` e `asAssignments`;
- suite existente de plano compartilhado, escopos isolados, efeitos, falhas e Corpus de Expressoes sem alteracao semantica.

Testes nao afirmam uma classe concreta de Caffeine, ordem de eviction ou implementacao interna da chave alem do contrato observavel. Contencao sustentada e estresse multi-tenant permanecem na Etapa 12.

## Benchmarks e Gates

Uma classe JMH da Etapa 9 mede quatro caminhos pareados com a mesma expressao e o mesmo ambiente:

1. **Pipeline sem cache:** seam interno completo, baseline real.
2. **Miss pelo engine:** lookup, carga completa e instalacao da entrada.
3. **Hit puro:** retorno do Resultado de Compilacao residente.
4. **Hit + `asMath()`:** lookup e validacao da Visao de Expressao.

O miss e preparado fora da janela medida por mecanismo interno de benchmark, sem expor invalidacao na API. O benchmark nao constroi engine ou ambiente por operacao e nao altera a fonte medida para fabricar misses.

Gates pareados na mesma execucao:

- miss no maximo 10% mais lento que o pipeline direto, e apenas conta como falha fora das bandas de erro;
- hit puro pelo menos 20 vezes mais rapido e com pelo menos 99% menos `B/op` que o pipeline direto;
- hit seguido de `asMath()` pelo menos 10 vezes mais rapido e com pelo menos 95% menos `B/op` que o pipeline direto.

Startup e warm-up permanecem benchmark separado de caracterizacao, sem limiar. O protocolo e o vigente: tres forks, aquecimento e medicao equivalentes, `gc` profiler, `ns/op`, `B/op`, ambiente e comando registrados em `docs/perf/performance-history.md`.

Falha em gate bloqueia fechamento e exige perfil, correcao ou simplificacao. O cache nao sai automaticamente, pois sua fronteira arquitetural ja foi escolhida; qualquer revisao de limiar exige evidencia de premissa ou benchmark invalido e atualizacao explicita do registro de decisoes.

## Incrementos de Implementacao

Cada incremento fecha com `mvn -pl exp-mk3 -am test` verde.

1. **Gate e pipeline interno.** Verificar invariantes, declarar Caffeine diretamente, extrair a unica compilacao sem cache e preservar `fullUncachedCompilation`.
2. **Parser compartilhado.** Liberar input e arvore em todas as saidas, instalar warm-up sincrono unico e provar a concorrencia de inicializacao.
3. **Fronteira publica.** Introduzir `CacheConfig`, `ExpressionEngine`, builders, engine default e propriedade de RuntimeServices/`Clock`; migrar testes e consumidores internos e remover a fachada estatica publica.
4. **Cache e single-flight.** Instalar chave exata e Resultado de Compilacao completo no Caffeine por engine; fechar identidade, falhas esperadas, excecoes internas e isolamento.
5. **Limites e verificacao.** Implementar capacidade e expiracao por acesso; fechar geracoes, referencias antigas, matriz concorrente e gates deterministas de nao retencao.
6. **Desempenho e fechamento.** Executar JMH e caracterizacao de startup, registrar o historico, confirmar ADR 0022, atualizar glossario e reconciliar planos.

Nao ha regra de parada entre os incrementos: a Etapa 9 e fronteira arquitetural escolhida, nao piloto de otimizacao descartavel. Um gate falho bloqueia o incremento seis ate a causa ser resolvida ou a premissa ser formalmente revista.

## Criterios de Aceite da Etapa 9

- `ExpressionEngine` e o unico ponto publico de compilacao; default e engines isolados obedecem seus RuntimeServices e `Clock`.
- `CacheConfig` imutavel valida capacidade e expiracao e aplica 1024 entradas sem TTL por default.
- Existe exatamente um cache por engine, sem segundo nivel ou compartilhamento global.
- Chave usa fonte textual exata e Identificador de Instancia do Ambiente, sem tipo de visao.
- Sucesso e falha esperada sao cacheados como Resultado de Compilacao completo.
- Single-flight executa uma carga por chave e geracao sob concorrencia.
- Falha interna nao instala entrada.
- Hits preservam identidade do resultado; `compileOrThrow` cria excecao nova por falha.
- Eviction e expiracao permitem nova geracao sem invalidar expressoes e visoes antigas.
- Todas as Visoes de Expressao compartilham o mesmo Plano Imutavel da Expressao Compilada e uma visao incompativel falha sem recompilar.
- Parser e aquecido uma vez e nao retem fonte, tokens ou parse tree depois da compilacao.
- Valor cacheado nao retem fonte duplicada, parse tree, AST, Modelo Semantico ou ambiente inteiro.
- Pipeline realmente sem cache permanece acessivel somente a testes e JMH.
- `compute` nao consulta cache, nao incrementa contador e nao ganha estado compartilhado.
- Quatro benchmarks e caracterizacao de startup sao executados; todos os gates declarados passam e os resultados entram no historico.
- ADR 0022, `CONTEXT.md`, estrategia, plano-mestre, plano detalhado e decisoes ficam reconciliados.
- Toda a suite existente permanece verde.

## Fora de Escopo

- Cache de resultados de execucao ou de sobrescritas por contrato.
- Segundo nivel global, cache distribuido ou compartilhamento entre engines.
- Compartilhamento entre ambientes construidos separadamente, mesmo quando equivalentes.
- Contador de execucoes, promocao Tier 1 ou preparacao do caminho quente para a Etapa 13.
- Memoria de Calculo, schema de proveniencia ou `computeWithMemory`, que pertencem a Etapa 10.
- Catalogo de produtos, contratos, versoes comerciais ou politica de formula historica da aplicacao.
- Parser por engine.
- Estatisticas, invalidacao, limpeza, manutencao, lifecycle ou bypass publico.
- Peso por numero de nos ou estimativa publica de memoria.
- Mudanca de gramatica.
- Publicacao de tickets durante o planejamento.

## Impacto nas Etapas Posteriores

- **Etapa 10:** a geracao residente compartilha o mesmo Plano Imutavel e seu schema de proveniencia entre `compute()` e `computeWithMemory()`. Nao ha segundo plano lazy, e a Memoria de Calculo nao muda a chave do cache nem retem a geracao depois que o plano se torna inalcancavel.
- **Etapa 11:** o migrador valida fontes pelo Engine de Expressao; resultados invalidos repetidos podem compartilhar diagnosticos sem recompilar.
- **Etapa 12:** amplia single-flight e execucao para estresse sustentado, confirma limites multi-tenant e integra perfil de alocacao no CI.
- **Etapa 13:** se Tier 1 for ativado, ele introduz e mede sua propria politica de observacao. Nenhum contador herdado da Etapa 9 condiciona o desenho.

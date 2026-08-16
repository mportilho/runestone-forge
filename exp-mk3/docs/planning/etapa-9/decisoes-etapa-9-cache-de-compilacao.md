# Decisoes de Planejamento da Etapa 9 - Cache de Compilacao

Este documento registra incrementalmente as decisoes tomadas durante o planejamento da Etapa 9 do `exp-mk3`. Apenas decisoes fechadas entram aqui; questoes ainda abertas permanecem na arvore da sessao de planejamento.

## Autoridade e Revalidacao

- ADRs aceitos, `CONTEXT.md`, Corpus de Expressoes e planos detalhados vigentes sao normativos.
- O plano historico e o codigo atual sao evidencias e substrato, nao autoridade quando contradizem contratos posteriores.
- A API publica continua provisoria e pode mudar de forma incompativel antes da GA.
- Nenhum ticket deve ser publicado durante esta sessao; a decomposicao ocorre depois do design registrado e com confirmacao propria.

## Objetivo Reenquadrado

- A Etapa 9 permanece obrigatoria e introduz o Engine de Expressao como fronteira longeva de compilacao, reuso e RuntimeServices.
- O cache nao e apresentado como ganho do caminho quente da carga financeira principal. Essa carga compila poucas formulas uma vez, guarda suas Visoes de Expressao e as executa muitas vezes por contrato.
- O valor da etapa e proteger integracoes contra recompilacao acidental, evitar Planos Imutaveis duplicados e deduplicar compilacoes concorrentes da mesma chave.
- Dentro de um Engine de Expressao, a mesma fonte por conteudo e o mesmo Identificador de Instancia do Ambiente resolvem para uma Expressao Compilada compartilhada. Outro engine ou outro Ambiente de Expressao possui outro limite de compartilhamento.
- Uma carga por contrato reutiliza diretamente a Visao de Expressao ja obtida; nenhuma consulta ao cache entra em `compute`.
- Identidade de produto e versao comercial de formula permanecem fora da chave tecnica. Um catalogo da aplicacao decide qual formula se aplica ao contrato, enquanto o engine decide se aquela fonte ja foi compilada naquele ambiente.

## Topologia e API

- Existe um unico cache Caffeine limitado por Engine de Expressao. Engine default e engines isolados sao instancias com ciclos de vida distintos, nao dois niveis de cache.
- Nao existe segundo nivel global, cache distribuido nem compartilhamento de entrada entre engines. RuntimeServices e `Clock` pertencem ao engine que produziu a Expressao Compilada.
- `ExpressionEngine` passa a ser o unico ponto publico de compilacao, com `defaultEngine()` para o singleton padrao e `builder()` para engines isolados.
- Cada engine oferece `compile` e `compileOrThrow`. O builder recebe a configuracao de cache e o `Clock` do engine.
- A fachada estatica `ExpressionCompiler` deixa de ser API publica; a API ainda e provisoria e nao ha consumidor externo publicado que justifique manter dois caminhos.

## Conteudo do Cache e Single-Flight

- O valor cacheado e o Resultado de Compilacao completo, nao apenas uma Expressao Compilada bem-sucedida.
- Sucessos, erros sintaticos e erros semanticos deterministas compartilham a mesma politica de capacidade e expiracao; nao existe cache negativo separado.
- Chamadas concorrentes da mesma chave executam o pipeline de compilacao uma unica vez, inclusive quando o resultado e falha.
- `compileOrThrow` cria uma nova excecao a partir dos diagnosticos de falha cacheados; excecoes nao sao valores do cache.

## Contador de Execucoes

- A Etapa 9 nao adiciona contador de execucoes. Nao existe escrita compartilhada, amostragem nem estado de promocao no caminho de `compute` sem consumidor atual.
- Se o Tier 1 opcional for ativado na Etapa 13, a propria implementacao de promocao introduzira e medira sua politica de observacao, podendo escolher contagem exata, amostragem ou estado por thread conforme evidencia.

## Capacidade e Expiracao

- A capacidade e limitada por numero de Resultados de Compilacao, nao por peso. Caffeine nao combina `maximumSize` e `maximumWeight`, e numero de nos nao representa a memoria retida por constantes, defaults, providers, servicos ou fontes; uma heuristica composta adicionaria complexidade sem impor limite real de heap.
- O engine default usa `maximumEntries = 1024`.
- Expiracao e opcional e desabilitada por default. Quando configurada, usa tempo desde o ultimo acesso, para que uma formula ativa nao recompile periodicamente apenas por idade.
- O mecanismo de expiracao do cache usa tempo monotono proprio do Caffeine e nao o `Clock` dos RuntimeServices, que pertence a semantica dos valores temporais da linguagem.
- O Caffeine do engine usa um executor direto (`Runnable::run`) para sua manutencao interna (drenagem de buffers, bookkeeping de admissao, eviction), em vez do `ForkJoinPool.commonPool()` default. O fechamento da Etapa 9 (issue #137) mediu esse ponto: despachar manutencao para o pool comum custava microssegundos por operacao de sinalizacao entre threads, o suficiente para violar o gate de miss por larga margem em um cache pequeno que nunca esta no caminho quente. Executar a manutencao inline no thread chamador elimina esse custo sem abrir mao de capacidade ou expiracao.

## Superficie Operacional

- A primeira versao nao expoe estatisticas, tamanho estimado, invalidacao por chave, limpeza global, `cleanUp` nem `close`.
- Mudanca de fonte ou de Ambiente de Expressao produz outra chave, e o limite de entradas garante remocao eventual da entrada antiga.
- O engine nao possui recurso externo que exija fechamento. Operacoes administrativas so entram diante de um caso de uso concreto.
- Nao existe cache desabilitavel nem bypass publico. `compile` e `compileOrThrow` sempre passam pelo cache do engine.

## Parser, Warm-up e Nao Retencao

- O parser e infraestrutura unica compartilhada pelo modulo, nao propriedade de cada engine. A construcao do primeiro engine executa sincronicamente um unico warm-up compartilhado com um conjunto pequeno e fixo de expressoes internas representativas, sem ler o Corpus de Expressoes em runtime; engines posteriores nao multiplicam parser, DFA nem warm-up. Falha nesse conjunto e bug de inicializacao.
- Cada cache miss libera a fonte, tokens e parse tree mantidos no contexto da thread depois de materializar a Arvore Semantica de Expressao, preservando o reuso de lexer/parser quando tecnicamente possivel.
- O criterio de nao retencao cobre o pipeline completo. Nao basta provar que o valor Caffeine nao possui campos de AST, Modelo Semantico, parse tree ou fonte duplicada enquanto o `ThreadLocal` do parser ainda retiver esses objetos.
- Um seam interno de compilacao sem cache e a unica implementacao do pipeline. O carregador Caffeine, testes e JMH o usam diretamente; ele nao e API publica.
- `CompilationCache.invalidate(source, environment)` (issue #137) e o mesmo tipo de seam: encerra a geracao residente de uma chave para que o benchmark JMH prepare um miss fora da janela medida, sem alterar a fonte medida. `ExpressionEngine` ganha um acessor `cache()` de pacote apenas para alcancar esse seam a partir do bridge de teste `EngineCacheInvalidation` (espelhando `UncachedCompilation`). Nenhum dos dois e API publica: `invalidate` fica em `internal.cache`, `cache()` e de pacote em `api`, e `ExpressionEngine` continua sem qualquer metodo de invalidacao, estatistica ou bypass no seu contrato publico.

## Geracoes, Chave e Falhas Inesperadas

- A fonte participa da chave por `String.equals` exato, sem normalizacao, copia, `intern` ou substituicao exclusiva por hash. Textos diferentes sao entradas diferentes mesmo quando semanticamente equivalentes, preservando a correspondencia de Trecho de Fonte e diagnostico.
- Enquanto uma entrada esta residente, chamadas da mesma chave compartilham o mesmo Resultado de Compilacao. Depois de expiracao ou eviction, a proxima chamada pode criar uma nova geracao enquanto consumidores ainda executam uma Expressao Compilada da geracao anterior.
- Eviction nunca invalida nem altera uma Expressao Compilada ou Visao de Expressao ja entregue. Single-flight vale para cada carga de uma geracao, nao como unicidade eterna por chave na JVM.
- Apenas `Success` e `Failure` produzidos normalmente sao valores cacheados. Excecao interna, `Error` ou outra falha inesperada escapa da carga e nao instala entrada; chamadas posteriores podem tentar novamente.

## Verificacao de Nao Retencao

- Gates deterministas percorrem chave e Resultado de Compilacao para provar que a unica retencao intencional da fonte esta na chave e que o valor nao alcanca parse tree, Arvore Semantica de Expressao ou Modelo Semantico.
- O contexto reutilizavel do parser e inspecionado depois de sucesso e de falha para provar que nao retem fonte, tokens nem parse tree.
- `WeakReference`, heap dump, JFR ou teste dependente de `System.gc()` podem complementar a caracterizacao, mas nao decidem o build.

## API Publica Concreta

- `CacheConfig.defaults()` produz a configuracao do engine default: 1024 entradas e nenhuma expiracao.
- `CacheConfig.builder()` permite `maximumEntries(int)` e `expireAfterAccess(Duration)` opcional. Quantidade e duracao devem ser estritamente positivas, e o produto e imutavel.
- `ExpressionEngine.defaultEngine()` devolve o singleton padrao.
- `ExpressionEngine.builder()` cria engine isolado; aceita `cacheConfig(CacheConfig)` e `clock(Clock)`, com defaults `CacheConfig.defaults()` e `Clock.systemUTC()`, e produz engine imutavel.
- O `Ticker` monotono usado por Caffeine nao faz parte da API publica. Um seam interno permite injeta-lo em testes sem confundi-lo com o `Clock` semantico da linguagem.

Exemplo da configuracao completa:

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

## Identidade do Resultado

- Enquanto a geracao esta residente, `compile` devolve a mesma instancia imutavel de `ExpressionCompilationResult.Success` ou `ExpressionCompilationResult.Failure` em todo hit.
- Em sucesso, `compileOrThrow` devolve a mesma Expressao Compilada guardada no resultado.
- Em falha, cada chamada de `compileOrThrow` cria uma nova `ExpressionCompilationException` sobre os mesmos diagnosticos imutaveis; stack trace e identidade de excecao nunca sao compartilhados nem cacheados.

## Matriz Funcional Obrigatoria

- Mesmo engine, fonte e ambiente devolvem o mesmo Resultado de Compilacao; fonte textual diferente, outro ambiente ou outro engine produzem entradas distintas.
- Single-flight executa uma carga por geracao sob concorrencia, tanto em sucesso quanto em falha.
- Expiracao e eviction permitem nova geracao; Expressao Compilada e Visoes de Expressao antigas continuam validas.
- Falha interna nao instala entrada e a chamada seguinte tenta novamente.
- Engines com `Clock` distintos nao compartilham resultado, mesmo quando recebem a mesma fonte e o mesmo ambiente.
- Warm-up compartilhado ocorre uma unica vez sob construcao concorrente de engines.
- Parser libera fonte, tokens e parse tree depois de sucesso sintatico/semantico, falha sintatica e falha semantica.
- A prova explicita de compartilhamento do Plano Imutavel cobre `asResult`, `asMath`, `asLogical` e `asAssignments`.
- Testes de expiracao usam `Ticker` falso; testes de single-flight usam contadores internos. Nenhum teste funcional usa `sleep` ou depende de coleta de lixo.

## Benchmarks e Gates

- Quatro caminhos sao medidos de forma pareada na mesma execucao JMH: pipeline interno sem cache, miss pelo engine, hit puro que devolve o Resultado de Compilacao e hit seguido de `asMath()`.
- O miss nao pode ultrapassar o pipeline direto em mais de 10% fora das bandas de erro.
- O hit puro deve ser pelo menos 20 vezes mais rapido e alocar pelo menos 99% menos que o pipeline direto.
- O hit seguido de `asMath()` deve ser pelo menos 10 vezes mais rapido e alocar pelo menos 95% menos que o pipeline direto.
- Startup e warm-up permanecem benchmark separado de caracterizacao, sem limiar.
- Resultados usam o protocolo vigente das etapas anteriores e sao registrados em `docs/perf/performance-history.md` com ambiente, comando, commit, `ns/op` e `B/op`.
- O fechamento (issue #137) mediu `engineMiss` com 10 forks (em vez dos 3 forks vigentes), depois que a primeira medicao com o protocolo padrao mostrou um custo real de execucao assincrona no `ForkJoinPool.commonPool()` (corrigido, ver secao de Capacidade e Expiracao) e, mesmo apos a correcao, uma variancia grande demais para decidir o gate de miss com confianca. Mais forks reduzem o erro amostral sem mudar o metodo medido; o numero registrado no historico e o resultado de 10 forks, citado como tal.

Falha em qualquer gate bloqueia o fechamento e exige perfil, correcao ou simplificacao. O cache nao e removido automaticamente, pois sua fronteira arquitetural ja foi escolhida, mas nenhum limiar e dispensado sem evidencia de que o benchmark ou a premissa estava errado e sem atualizacao explicita desta decisao. Nao existe fallback publico para a fachada estatica anterior.

## Incrementos de Implementacao

Cada incremento fecha com `mvn -pl exp-mk3 -am test` verde:

1. **Gate e pipeline interno.** Inventariar seams e invariantes atuais, declarar Caffeine como dependencia direta do modulo e extrair a unica compilacao sem cache, preservando o benchmark existente.
2. **Parser compartilhado.** Liberar fonte, tokens e parse tree em todas as saidas; instalar warm-up sincrono unico e provar que engines concorrentes nao o repetem.
3. **Fronteira publica.** Introduzir `CacheConfig`, `ExpressionEngine`, engine default, builders e propriedade de RuntimeServices/`Clock`; migrar consumidores e remover `ExpressionCompiler` da API publica.
4. **Cache e single-flight.** Instalar o Caffeine por engine com chave textual exata, Resultado de Compilacao completo, identidade por geracao, falhas inesperadas nao cacheadas e isolamento entre engines.
5. **Limites e verificacao.** Fechar capacidade, expiracao por acesso, nova geracao, validade de referencias antigas, matriz concorrente e gates deterministas de nao retencao.
6. **Desempenho e fechamento.** Executar os quatro benchmarks pareados e a caracterizacao de startup, registrar o historico, aceitar o ADR 0022 e reconciliar glossario, plano detalhado, plano-mestre e estrategia.

## ADR 0022

- O ADR 0022 registra apenas a decisao dificil de reverter: reuso de compilacao pertence ao Engine de Expressao; a chave combina fonte exata e Identificador de Instancia do Ambiente; engines nao compartilham entradas; RuntimeServices pertencem ao engine.
- Caffeine, defaults, TTL, cache de falhas, gates e numeros de benchmark permanecem fora do ADR por serem mecanismos reversiveis documentados neste plano.

## Fora de Escopo

- Cache de resultado de execucao.
- Segundo nivel global, cache distribuido ou compartilhamento entre engines.
- Compartilhamento entre Ambientes de Expressao construidos separadamente, mesmo quando equivalentes.
- Contador de execucoes, promocao Tier 1 ou qualquer escrita preparatoria no caminho de `compute`.
- Auditoria e plano instrumentado.
- Catalogo comercial de produtos, contratos ou versoes de formula da aplicacao consumidora.
- Parser por engine.
- Estatisticas, invalidacao, manutencao, lifecycle ou bypass publico do cache.
- Mudanca de gramatica.
- Publicacao de tickets durante o planejamento.

## Decisoes Ainda Pendentes

- Nenhuma. A arvore de decisoes foi confirmada e consolidada em `etapa-9-cache-de-compilacao.md`.

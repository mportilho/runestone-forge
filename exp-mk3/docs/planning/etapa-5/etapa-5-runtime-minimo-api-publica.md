# Plano Detalhado - Etapa 5 - Runtime Minimo e API Publica

Este plano detalha a Etapa 5 do `exp-mk3` depois da revalidacao das Etapas 0-4. Ele consolida as decisoes registradas em `decisoes-etapa-5-runtime-minimo-api-publica.md`, complementa o plano macro e usa o ADR 0017 para a semantica numerica.

## Objetivo

Fechar o marco M1 com um caminho `compile -> plan -> view -> compute` semanticamente correto, uma API publica coerente e um baseline funcional e de desempenho. O plano de execucao deve ser simples, generico, imutavel, compartilhavel entre threads e deliberadamente sem otimizacoes.

A Etapa 5 nao deve corrigir lacunas da Etapa 4 dentro do planner nem transformar a execucao antecipada da Etapa 6 em novo escopo. O `Modelo Semantico` completo e a unica entrada planejavel.

## Autoridade e Premissas

- ADRs aceitos, `CONTEXT.md`, o Corpus de Expressoes e os planos detalhados vigentes definem o contrato.
- O plano historico e o codigo existente sao evidencias e substrato de implementacao, nao autoridade quando contradizem contratos posteriores.
- A API publica atual de compilacao e execucao e provisoria e pode mudar de forma incompativel antes da GA.
- A Etapa 5 executa semantica escalar, atribuicoes, desestruturacao, literais de colecao e funcoes globais.
- Navegacao, filtros, lambdas e operacoes receptoras continuam pertencendo ao gate formal da Etapa 6, ainda que parte do codigo ja exista.
- Toda a suite existente deve permanecer verde; testes que contradizem contratos normativos devem ser atualizados atomicamente com codigo e corpus.
- Nao ha folding, CSE, reescrita algebrica, especializacao de nos, pooling de escopo, cache de compilacao ou fusao de pipelines nesta etapa.

## Estado Atual e Estrategia de Reaproveitamento

| Entrega | Estado observado | Acao planejada |
|---|---|---|
| `compile -> plan -> compute` | Existe com API baseada em excecao e resultado obrigatorio | Adaptar |
| `SemanticModel` planejavel | Parcial; faltam contratos do gate da Etapa 4 | Substituir as lacunas antes do planner |
| `ExecutionPlanBuilder` e runtime escalar | Existem, mas redescobrem algumas regras e capturam configuracao ampla | Adaptar |
| Nulidade estrita | Implementacao e testes ainda aceitam escapes proibidos | Substituir comportamento e testes conflitantes |
| Layout de frame canonico | Parcial e diferente do contrato detalhado | Adaptar no resolver antes do runtime |
| Fatos numericos e checagens diferidas | Ausentes ou redescobertos no runtime | Implementar no resolver e consumir no plano |
| Diagnosticos publicos | Forma parcial, sem modelo compartilhado completo | Substituir |
| Visoes publicas | Ausentes | Implementar |
| Arquivo apenas com atribuicoes | Rejeitado atualmente | Adaptar resolver e plano |
| Potencia e raiz | Misturam `BigDecimal` e `big-math`; dominio incompleto | Substituir pelo contrato do ADR 0017 |
| Valores temporais correntes | Usam relogio global diretamente | Adaptar para `RuntimeServices` |
| Materializacao publica | Parcial e acoplada ao runtime existente | Adaptar para fronteira tipada |
| Navegacao e colecoes da Etapa 6 | Implementadas antecipadamente em parte | Preservar quando compativeis; fora do gate M1 |
| Baseline JMH da Etapa 5 | Ausente | Implementar e registrar |

A matriz e orientada por entregas, nao por classes. Durante a implementacao, uma classe existente pode ser mantida, dividida ou removida desde que o contrato da linha correspondente seja satisfeito.

## Gate de Estabilizacao da Etapa 4

A Etapa 5 so inicia planejamento executavel depois que os criterios normativos da Etapa 4 estiverem atendidos. Um build verde isolado nao substitui este gate.

Entregas bloqueantes:

- `SemanticModel` completo com tipos, nulidade, fatos numericos, formas, pureza/politicas de avaliacao, valores preparados, bindings e checagens diferidas.
- Nulidade estrita em resultado, atribuicoes, operadores, argumentos, predicados e receptores nao seguros.
- Bindings completos para simbolos, funcoes, navegacao e operacoes de colecao.
- `FrameLayout` canonico: externos usados por primeira referencia, internos por primeira criacao, alvos de desestruturacao em ordem textual e slots de `Item Atual` por profundidade realmente usada.
- Arquivos com atribuicoes e sem expressao final aceitos; arquivo completamente vazio rejeitado.
- Fatos e checagens numericas alinhados ao ADR 0017.
- Checagens diferidas tipadas para pre-condicoes de valor, sem tipo, overload ou membro resolvido em runtime.
- Invariantes que rejeitam sucesso com placeholder, tipo invalido, binding ausente ou metadata obrigatoria ausente.

O `ExecutionPlanBuilder` deve tratar qualquer violacao desse contrato como bug interno. Ele nao deve inferir tipo, resolver nome, escolher overload, procurar membro, reconstruir fato numerico ou decidir uma regra de nulidade.

## Pipeline de Compilacao

```text
source
  -> Resultado de Parsing
  -> Arvore Semantica de Expressao
  -> Resultado de Resolucao Semantica
  -> Plano Imutavel sem otimizacoes
  -> CompiledExpression
  -> Visao de Expressao
  -> compute
```

Falha de parsing ou semantica produz resultado de compilacao sem plano. Warnings nao bloqueiam sucesso e permanecem acessiveis no resultado e no `CompiledExpression`.

O planner recebe apenas um `SemanticModel` de sucesso e extrai o necessario para execucao. Depois da construcao, o plano e o `CompiledExpression` nao retem parse tree, AST, `SemanticModel`, texto-fonte nem o `ExpressionEnvironment` inteiro.

## API Publica

Forma conceitual esperada:

```java
public sealed interface ExpressionCompilationResult {
    record Success(
            CompiledExpression compiledExpression,
            List<ExpressionDiagnostic> diagnostics)
            implements ExpressionCompilationResult {}

    record Failure(List<ExpressionDiagnostic> diagnostics)
            implements ExpressionCompilationResult {}
}
```

```java
public final class ExpressionCompiler {
    public static ExpressionCompilationResult compile(
            String source,
            ExpressionEnvironment environment);

    public static CompiledExpression compileOrThrow(
            String source,
            ExpressionEnvironment environment);
}
```

`Success.diagnostics()` contem apenas warnings. `Failure.diagnostics()` contem ao menos um erro e tambem pode conter warnings. `compileOrThrow` lanca `ExpressionCompilationException` com a mesma lista imutavel que seria devolvida por `Failure`.

`CompiledExpression` nao expoe `compute()` diretamente. Ele preserva warnings e cria fachadas finas sobre o mesmo plano:

```java
public final class CompiledExpression {
    public List<ExpressionDiagnostic> compilationDiagnostics();
    public ResultExpression asResult();
    public MathExpression asMath();
    public LogicalExpression asLogical();
    public AssignmentsExpression asAssignments();
}
```

Contratos das visoes:

| Visao | Pre-condicao | Resultado |
|---|---|---|
| `ResultExpression` | Expressao final presente e publicamente exponivel | `Object` canonico |
| `MathExpression` | Expressao final `NUMBER` | `BigDecimal` |
| `LogicalExpression` | Expressao final `BOOLEAN` | `boolean` |
| `AssignmentsExpression` | Ao menos uma atribuicao e todos os simbolos expostos publicamente exponiveis | `Map<String, Object>` |

Cada visao expoe `compute()` e `compute(Map<String, ?> overrides)`. Solicitar uma visao incompativel falha imediatamente com `ExpressionViewException`, razao fechada, tipo encontrado e trecho relevante quando existir.

`AssignmentsExpression.compute()` executa apenas as atribuicoes. Uma expressao final opcional nao e avaliada, nao produz efeitos e nao integra o mapa. O mapa e imutavel, segue a ordem da primeira criacao dos simbolos internos e contem o valor final depois de reatribuicoes.

## Modelo Publico de Diagnostico

`ExpressionDiagnostic` e o contrato comum de parsing, semantica, runtime e migracao. Ele contem:

- categoria;
- severidade `ERROR` ou `WARNING`;
- codigo textual estavel;
- mensagem;
- trecho primario opcional;
- informacoes relacionadas com mensagem e trecho;
- notas;
- sugestao opcional.

Diagnosticos originados na fonte sempre tem trecho primario. Falhas puramente externas, como uma chave de override nao declarada, podem nao ter trecho primario e podem relacionar referencias da expressao quando aplicavel.

`ExpressionExecutionException` carrega exatamente um diagnostico. A primeira falha encerra a execucao porque funcoes impuras e atribuicoes tornam insegura qualquer tentativa de continuar. A causa original e preservada para falhas de provider e biblioteca; `VirtualMachineError`, `ThreadDeath` e `LinkageError` propagam sem encapsulamento. Um `InterruptedException` restaura o status de interrupcao antes de ser encapsulado.

Familias iniciais de runtime:

- entrada externa invalida;
- dominio numerico fora dos reais;
- operacao matematica indefinida;
- falha de calculo/arredondamento;
- limite de ambiente excedido;
- falha lancada por provider;
- contrato de retorno de funcao violado;
- desestruturacao insuficiente;
- valor nulo indevido.

Codigos especificos de navegacao, filtro e operacao de colecao pertencem ao detalhamento da Etapa 6.

## Plano Imutavel e Nos Executaveis

`ExecutionPlanBuilder` constroi a forma sem otimizacoes a partir do `SemanticModel`. O plano contem conceitualmente:

- atribuicoes executaveis em ordem de fonte;
- expressao final executavel opcional;
- template de frame com defaults canonicos;
- planos ordenados dos externos usados;
- descritores minimos para validar externos declarados mas nao usados;
- metadados das visoes;
- `frameSize` e profundidade maxima de `Item Atual`;
- `MathContext`, zona e limites efetivamente necessarios;
- indicacao dos Valores Temporais Correntes usados.

O plano nao retem o ambiente inteiro. Providers e `MethodHandle`s escolhidos podem ser retidos por seus bindings, pois fazem parte da execucao compilada daquela instancia de ambiente.

`ExecutableNode` e uma interface interna com avaliacao sobre `ExecutionScope`. Cada no preserva `NodeId` e `SourceSpan`, alem do operador, binding ou valor preparado necessario. Implementacoes genericas por familia sao suficientes: literal, leitura, operacao unaria, operacao binaria, condicional, coalescencia, chamada e colecao. Um switch de operador dentro da familia e aceito nesta etapa.

Nao usar closures opacas que capturem AST, `SemanticModel` ou ambiente. Nao introduzir familias especializadas operador x tipo antes da Etapa 8.

## Forma sem Otimizacoes como Oraculo

A pipeline deve permitir omitir internamente o passo de otimizacao:

```text
SemanticModel -> plano sem otimizacoes -> transformacoes opcionais -> plano executado
```

Na Etapa 5, as transformacoes opcionais sao vazias. Nas Etapas 7-8, a forma sem otimizacoes e gerada pelos mesmos builders basicos, nos e runtime, e usada como oraculo diferencial. Nao ha flag publica, runtime duplicado ou copia historica congelada da Etapa 5.

## Checagens Diferidas

Checagens diferidas formam uma hierarquia interna fechada e tipada. Cada instancia declara o `NodeId`, o trecho de origem e o codigo da falha de runtime. O planner as associa ao no executavel correspondente sem reconstruir sua regra.

Exemplos no M1:

- dominio real ou definido de potencia e raiz;
- integralidade, nao-negatividade e limite de fatorial;
- tamanho minimo de desestruturacao;
- limites de materializacao conhecidos apenas em runtime.

Violacoes provadas por valores constantes sao diagnosticos de compilacao. Somente pre-condicoes dependentes de valores runtime chegam ao plano.

## ExecutionScope e Entradas

Cada chamada cria um unico `Object[] frame`, inicializado por copia de um template imutavel. `UNBOUND` e uma sentinela privada distinta de `null`.

Ordem de preparacao:

1. Copiar defaults canonicos para o frame.
2. Rejeitar a menor chave de override nao declarada em ordem lexicografica.
3. Validar simbolos declarados na ordem canonica do plano.
4. Para cada override, validar politica, null, coercao e containers recursivos.
5. Gravar uma unica vez os valores dos externos usados.
6. Descartar valores validos de externos declarados mas nao usados.
7. Executar atribuicoes e, conforme a visao, a expressao final.

Toda entrada e validada antes de qualquer atribuicao ou chamada com efeito. A coercao ocorre uma vez por valor. Externos nao usados nao recebem slot, mas mantem descritor minimo de validacao.

## Valores Temporais Correntes

O relogio vive em um `RuntimeServices` interno associado ao `CompiledExpression`, fora do plano e da identidade do ambiente. A API estatica usa o relogio padrao; testes internos podem injetar `Clock`.

Se o plano nao usa `currDate`, `currTime` nem `currDateTime`, a execucao nao consulta o relogio. Caso use ao menos um deles, consulta `Clock.instant()` exatamente uma vez, trunca o instante para segundos e deriva apenas os valores usados no `ZoneId` do ambiente.

## Politicas de Avaliacao

Toda construcao eager avalia filhos da esquerda para a direita. Isso inclui operadores binarios, `nand`, `nor`, `xor`, `xnor`, argumentos de funcao e elementos de literal de colecao.

Politicas lazy:

- `and`: nao avalia a direita quando a esquerda e `false`.
- `or`: nao avalia a direita quando a esquerda e `true`.
- `??`: avalia da esquerda para a direita e para no primeiro valor nao nulo.
- condicional: avalia condicoes em ordem e apenas o ramo escolhido.
- `between`: avalia valor e limite inferior; so avalia o limite superior se a primeira comparacao permitir. A forma negada inverte o resultado, nao a ordem.

Todos os ramos e operandos sao validados semanticamente mesmo quando podem nao executar.

## Semantica Numerica Real Decimal

O ADR 0017 e normativo. `NUMBER` aceita semanticamente operacoes classificadas como matematicamente definidas com resultado real. Numeros complexos e operacoes indefinidas falham. Uma operacao admissivel ainda pode falhar por limite configurado, representacao `BigDecimal` ou incapacidade de `big-math` na precisao pedida; essa falha pertence a calculo/arredondamento ou limite, nao a dominio complexo ou operacao indefinida.

Todo decimal finito e interpretado como fracao exata canonica reduzida para decidir dominio e sinal. Em `p/q` ou `a/b`, numerador e denominador sao coprimos e o denominador e positivo; paridade de numerador usa o valor absoluto. Essa classificacao nao altera o valor decimal usado no calculo e nao deve materializar denominador proporcional a `10^scale`.

Para base negativa elevada a `p/q` reduzido, o resultado e real apenas quando `q` e impar. O sinal e negativo quando `abs(p)` e impar. Para `degree root radicand`, um grau reduzido canonico `a/b` equivale ao expoente `b/a`; com radicando negativo, o resultado e real apenas quando `abs(a)` e impar. Quando real, o resultado e negativo exatamente quando `b` e impar e positivo quando `b` e par.

`big-math` calcula toda potencia e raiz em todos os tiers. Quando o resultado classificado for negativo, a magnitude positiva usa um `MathContext` derivado com a mesma precisao e com `CEILING` e `FLOOR` trocados; os demais modos permanecem iguais. A negacao posterior e exata, produzindo o arredondamento que o contexto original aplicaria ao valor negativo.

Para base negativa e expoente racional classificado como real, a magnitude sempre e calculada por `BigDecimalMath.pow(abs(base), expoente, contextoDaMagnitude)`, independentemente do sinal final. Se `abs(p)` for impar, aplicam-se o contexto de resultado negativo e a negacao exata; se for par, usa-se o contexto original e retorna-se a magnitude positiva.

Grau negativo nunca e passado diretamente a `BigDecimalMath.root`. Para grau negativo e radicando diferente de zero, a magnitude e `BigDecimalMath.reciprocal(BigDecimalMath.root(abs(radicando), abs(grau), contextoDaMagnitude), contextoDaMagnitude)`. O arredondamento intermediario dessa composicao integra o contrato. Grau positivo usa `BigDecimalMath.root(abs(radicando), grau, contextoDaMagnitude)`. Depois, o runtime restaura exatamente o sinal classificado. `BigComplexMath` nao e usado.

Casos definidos:

- toda base diferente de zero elevada a zero resulta em `1`; por convencao, `0 ^ 0` tambem resulta em `1`.
- zero elevado a expoente positivo resulta em zero; zero elevado a expoente negativo e indefinido.
- expoente negativo sobre base nao zero resulta no reciproco.
- grau de raiz pode ser positivo, negativo, integral ou fracionario, desde que nao seja zero e o resultado seja real e definido.
- radicando zero com grau positivo resulta em zero; com grau negativo e indefinido.
- `mod` usa `BigDecimal.remainder` exato e preserva o sinal do dividendo.
- igualdade e ordenacao decimal ignoram diferencas de escala numericamente irrelevantes.

Casos indefinidos incluem divisao ou resto por zero, zero elevado a expoente negativo, radicando zero com grau negativo e raiz de grau zero. Um resultado que exigiria numero complexo usa familia diagnostica distinta de operacao indefinida.

Uso de contexto:

| Operacao | Politica |
|---|---|
| Literais | Valor e escala exatos |
| `+`, `-`, `*`, `/` | `mathContext` |
| `^`, `root` | `big-math` com `mathContext` |
| Negacao unaria, `%`, `!`, `mod` | Exatos |
| Comparacoes | Numericas, sem arredondamento |
| Funcoes transcendentais | `transcendentalMathContext` |

O ambiente rejeita contexto com precisao zero e `RoundingMode.UNNECESSARY`, tanto em `mathContext` quanto em `transcendentalMathContext`. Resultados publicos nao passam por `stripTrailingZeros`, escala global ou normalizacao equivalente.

## Funcoes

Chamadas usam exclusivamente o `MethodHandle` escolhido durante construcao do ambiente e vinculado durante resolucao. Nao ha reflexao, discovery, lookup textual ou overload no runtime.

Argumentos sao avaliados da esquerda para a direita. Retornos sao validados e convertidos imediatamente para valores canonicos nao nulos, com verificacao recursiva de containers e Limite de Materializacao.

Falha lancada pelo provider, retorno null, tipo incompativel e container invalido usam codigos distintos. A excecao original permanece como causa quando existir.

## Materializacao Publica

Um materializador interno dirigido pelo tipo converte resultados para representacoes Java publicas:

- `NUMBER` -> `BigDecimal`;
- `BOOLEAN` -> `boolean`/`Boolean` conforme a assinatura da visao;
- demais escalares -> representacao canonica ja resolvida;
- `Collection<T>` -> lista ordenada imutavel;
- `Map<V>` -> mapa imutavel com chaves textuais em ordem canonica.

Containers sao copiados recursivamente na fronteira publica nesta etapa, mesmo quando a estrutura interna ja e imutavel. Nao ha elemento, chave ou valor null. `ObjectType` e qualquer container que o contenha sao rejeitados ao criar a visao.

Cada snapshot e verificado independentemente contra `maxMaterializedSize`. O mapa produzido por `AssignmentsExpression` tambem e uma materializacao; quando sua quantidade conhecida de simbolos exceder o limite, a visao pode ser rejeitada antes da execucao.

## Concorrencia

`CompiledExpression`, suas visoes e o Plano Imutavel sao compartilhaves entre threads. Estado mutavel de uma chamada fica apenas no `ExecutionScope` e no snapshot de resultado daquela chamada. Nao ha pool de escopos na Etapa 5.

## Testes e Corpus

O Corpus de Expressoes cobre o contrato de linguagem do M1:

- operadores escalares e comparacoes;
- politicas eager e lazy;
- atribuicoes, reatribuicoes e desestruturacao;
- literais de colecao e pertencimento;
- funcoes globais e efeitos observaveis;
- defaults, overrides e falhas de entrada;
- dominio numerico real, complexo e indefinido;
- falhas de runtime com codigo e trecho esperado.

O formato do corpus nao recebe campo novo de milestone. O subconjunto M1 e identificado pelas fases e Tags de Cobertura existentes. Todos os casos reais v1 disponiveis devem passar, mas a ausencia atual de casos reais nao bloqueia M1.

Testes de API cobrem resultado de compilacao, warnings, `compileOrThrow`, selecao de visao, retorno tipado, materializacao e a nao avaliacao da expressao final por `AssignmentsExpression`.

Testes de propriedade cobrem a classificacao racional de potencia e raiz sem usar a funcao sob teste como oraculo. Casos gerados variam sinal, numerador, denominador reduzido, grau positivo/negativo, zero e paridade. A matriz de conformidade inclui `CEILING`/`FLOOR` para resultados negativos, o adapter de potencia com base negativa e numeradores reduzidos impares/pares, prova de que `BigDecimalMath.pow` nao recebe base negativa nesses caminhos reais fracionarios, radicando zero com graus positivos/negativos, rejeicao direta de grau negativo pela biblioteca, o adapter de grau negativo e escalas decimais extremas sem materializar `10^scale`.

Funcoes impuras com contador e registro de ordem provam avaliacao e nao avaliacao para `and`, `or`, `between`, `??`, condicionais, operadores eager, chamadas e colecoes.

Testes internos cobrem template/frame, `UNBOUND`, layout, coercao unica, ordem deterministica de falha, isolamento entre execucoes, Checagens Diferidas e invariantes do planner.

Testes temporais usam relogio fixo para provar truncamento em segundos, coerencia entre valores e ausencia de consulta quando nenhum valor temporal e usado. Testes concorrentes compartilham o mesmo plano entre threads com frames independentes.

Testes antecipados da Etapa 6 permanecem verdes quando compativeis. Casos que hoje aceitam nulidade, lookup ou outro comportamento proibido sao corrigidos junto com a implementacao normativa.

## Baseline JMH

Criar uma classe de benchmark da Etapa 5 com quatro casos:

1. compute hot de `a + b * 2` com overrides;
2. compute hot de expressao logica representativa com todos os operandos avaliados;
3. compilacao completa sem cache, com JVM/parser aquecidos e parse, AST, resolucao e planejamento por operacao;
4. materializacao publica de container com tamanho fixo.

Registrar `ns/op` e `B/op` quando aplicavel. O protocolo usa tres forks, warmup e medicao suficientes, heap fixo e profiler `gc`. O historico registra commit, JDK, JMH, SO, flags, comando, expressao, ambiente, inputs e tabela de resultados.

Os JSONs sao preservados durante a analise, mas nao versionados automaticamente. A Etapa 5 nao tem limiar de aprovacao: captura uma baseline reproduzivel. Etapas 7-9 definem comparacoes por benchmark depois de observar a variabilidade; excecoes futuras exigem justificativa documentada.

O benchmark existente de parsing `SingleShot` continua medindo inicializacao separadamente. Ele nao deve ser misturado com a compilacao sem cache.

## Incrementos de Implementacao

### Incremento 1 - Gate da Etapa 4

- Completar o `SemanticModel` e suas invariantes.
- Corrigir nulidade, bindings, fatos, checagens e layout.
- Aceitar arquivos apenas com atribuicoes.
- Atualizar testes/corpus conflitantes.

### Incremento 2 - Diagnosticos e Compilacao

- Unificar `ExpressionDiagnostic` publico.
- Introduzir `ExpressionCompilationResult` e `compileOrThrow`.
- Preservar warnings no artefato compilado.
- Introduzir excecoes especificas de compilacao, visao e execucao.

### Incremento 3 - Plano e Escopo sem Otimizacoes

- Fazer o planner consumir exclusivamente o modelo completo.
- Eliminar retencao e redescoberta semantica.
- Implementar nos genericos com identidade/trecho.
- Implementar template de frame, validacao de entradas e `RuntimeServices`.

### Incremento 4 - Dominio Numerico e Runtime Escalar

- Implementar ADR 0017 e Checagens Diferidas numericas.
- Cobrir operadores, ordem, curto-circuito, chamadas e desestruturacao.
- Converter falhas em diagnosticos posicionados.

### Incremento 5 - Visoes e Materializacao

- Implementar as quatro visoes.
- Materializar resultados recursivamente.
- Implementar mapa de atribuicoes sem avaliar resultado final.

### Incremento 6 - Gates Funcionais

- Expandir corpus M1.
- Adicionar propriedades numericas, testes de efeitos, plano, escopo, tempo e concorrencia.
- Manter `mvn -pl exp-mk3 -am test` verde.

### Incremento 7 - Baseline e Fechamento

- Criar e executar os quatro benchmarks.
- Registrar baseline em `docs/perf/performance-history.md`.
- Revisar nao retencao e impactos nas etapas posteriores.

Cada incremento termina verde e nao depende de uma refatoracao total posterior para tornar seu contrato valido.

## Criterios de Aceite da Etapa 5

- Gate normativo da Etapa 4 atendido antes do planner.
- Compilacao orientada a resultado preserva warnings e nao usa excecao como fluxo esperado.
- Visoes compartilham um unico plano e validam compatibilidade antes da execucao.
- Arquivos apenas com atribuicoes executam por `AssignmentsExpression`.
- Plano e valor compilado nao retem parse tree, AST, `SemanticModel`, fonte ou ambiente inteiro.
- Runtime nao resolve tipo, overload, membro ou regra semantica.
- Operadores escalares obedecem ordem, lazy/eager, dominio real e contextos definidos.
- Potencia e raiz usam `big-math` em todos os caminhos.
- Falhas de runtime usam diagnostico estavel e causa preservada quando aplicavel.
- Entradas sao validadas deterministicamente antes de efeitos.
- Materializacoes publicas sao imutaveis, recursivas e limitadas.
- Plano e visoes sao seguros para uso concorrente com escopos isolados.
- Corpus M1, propriedades e suites de API/runtime estao verdes.
- Toda a suite existente compativel permanece verde.
- Os quatro benchmarks foram executados e a baseline foi registrada sem limiar artificial.

## Fora de Escopo

- Novas funcionalidades de navegacao, filtro, lambda ou operacao de colecao.
- Otimizacoes, especializacao, cache, pooling e Memoria de Calculo.
- API publica para configurar `Clock`.
- Numeros complexos ou `BigComplexMath`.
- Compatibilidade com a API publica provisoria atual.
- Campo novo de milestone no corpus.
- Ingestao obrigatoria de corpus real ainda indisponivel.
- Publicacao de tickets durante o planejamento.

## Impacto nas Etapas Posteriores

- Etapa 6 executa bindings completos e prepara um gap plan sobre codigo antecipado; nao redescobre semantica nem usa fallback reflexivo por tipo desconhecido.
- Etapa 7 aplica transformacoes opcionais sobre a forma sem otimizacoes e prova equivalencia de resultado, falha, escala, ordem e efeitos.
- Etapa 8 especializa preservando o ADR 0017 e o uso de `big-math`; substituicao exige decisao futura explicita.
- Etapa 9 compartilha um plano entre todas as visoes, mantem `RuntimeServices` fora da identidade do ambiente e mede compilacao sem cache versus hit.
- Etapa 10 usa `NodeId` e `SourceSpan` preservados como Chave de Proveniencia da Memoria de Calculo. Um unico plano atende `compute()` e `computeWithMemory()`; somente nos marcaveis recebem o branch previsivel de `calculationSlot`, sob gate de zero B/op adicional.
- Etapa 11 nao bloqueia M1 pela ausencia atual de corpus real, mas continua responsavel pela verificacao diferencial completa.
- Etapa 12 transforma medicao em gates de CI e amplia stress/concurrency.
- Etapa 13 mantem a forma sem otimizacoes como fallback/oraculo para Tier 1 e fusao de pipelines.

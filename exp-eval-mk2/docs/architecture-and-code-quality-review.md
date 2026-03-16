# Analise de Arquitetura e Qualidade de Codigo - `exp-eval-mk2`

## Objetivo

Registrar uma analise geral do modulo `exp-eval-mk2`, com foco em:

- organizacao arquitetural
- direcao de dependencias
- qualidade de codigo
- riscos de corretude
- skills de Java mais adequadas para continuar a avaliacao ou conduzir refatoracoes

Base da analise:

- leitura da estrutura de pacotes e classes principais
- amostragem do fluxo `api -> compiler -> grammar/ast -> semantic -> runtime`
- execucao de `mvn -q -pl exp-eval-mk2 test`
- validacoes manuais de comportamento via REPL

## Skills de Java Mais Uteis

As skills mais aderentes para este modulo, no estado atual, sao:

- `architecture-review`: para avaliar fronteiras, coesao de pacotes, dependencia entre camadas e exposicao excessiva de tipos internos
- `java-code-review`: para revisar corretude, contratos de API, null safety, excecoes, colecoes e idioms de Java
- `solid-principles`: para atacar concentracao de responsabilidades e tipos centrais que acumulam varias razoes de mudanca
- `clean-code`: para a etapa posterior de simplificacao local, nomes, extracao de metodos e reducao de complexidade dentro de classes ja estabilizadas
- `java-guidelines`: para orientar implementacoes e refatoracoes futuras
- `performance-smell-detection`: skill complementar quando a revisao migrar para hotspots do parser e do runtime

## Avaliacao Estrutural

- Organizacao predominante: mista, orientada a pipeline tecnico
- Clareza: boa
- Estado geral: funcional, mas com pontos de acoplamento ciclico e concentracao de logica em classes centrais

O modulo esta estruturado em pacotes com responsabilidades tecnicas bem reconheciveis:

- `api`
- `grammar`
- `ast`
- `semantic`
- `compiler`
- `runtime`
- `catalog`

Essa separacao funciona bem para um motor de expressoes. A arquitetura nao esta organizada por feature, e sim por estagios do pipeline. Para esse dominio, isso e aceitavel e legivel.

Pontos fortes observados:

- AST e runtime modelados com `record` e `sealed interface`
- boa adocao de imutabilidade e copias defensivas
- parser, AST, semantica e fachada publica com cobertura automatizada
- separacao clara entre parse, construcao de AST, resolucao semantica e execucao

## Evidencias Estruturais

### Organizacao do Pipeline

- `api/MathExpression.java`
- `api/LogicalExpression.java`
- `compiler/ExpressionCompiler.java`
- `grammar/language/ExpressionEvaluatorV2ParserFacade.java`
- `ast/mapping/SemanticAstBuilder.java`
- `semantic/SemanticResolver.java`
- `runtime/ExpressionRuntimeSupport.java`
- `runtime/AbstractRuntimeEvaluator.java`

### Pacotes Mais Densos

- `ast`: 20 classes
- `runtime`: 17 classes
- `semantic`: 14 classes

### Classes com Maior Concentracao de Logica

- `ast/mapping/SemanticAstBuilder.java`: 799 linhas
- `semantic/SemanticResolver.java`: 329 linhas
- `runtime/AbstractRuntimeEvaluator.java`: 291 linhas
- `api/ExpressionEnvironmentBuilder.java`: 172 linhas

## Achados

| Severidade | Tema | Evidencia | Impacto | Recomendacao |
|---|---|---|---|---|
| Alta | Operadores logicos sem short-circuit | `runtime/AbstractRuntimeEvaluator.java` avalia `left` e `right` antes do `switch` em `evaluateBinary(...)` | `false and ...` e `true or ...` podem falhar desnecessariamente, avaliar simbolos ausentes e disparar efeitos colaterais de funcoes | Implementar avaliacao preguicosa para `AND` e `OR`, e revisar `NAND`, `NOR`, `XOR`, `XNOR` caso a caso |
| Media | Ciclo entre pacotes principais | `api` depende de `compiler` e `runtime`, enquanto `compiler`, `runtime` e `semantic` dependem de `api` | Dificulta isolar camadas estaveis e amplia o acoplamento entre fachada publica e miolo da engine | Introduzir contratos mais estreitos; reduzir dependencias do core para `api`; transformar `ExpressionEnvironment` em dependencia mais neutra ou mover partes compartilhadas |
| Media | Exposicao excessiva de tipos internos | `CompiledExpression`, `SemanticModel`, `MutableBindings`, `ExecutionScope`, `FunctionCatalog`, `FunctionDescriptor` e outros tipos internos estao publicos | Amplia superficie publica sem necessidade clara e dificulta evolucao interna sem risco de quebra | Tornar `package-private` o que nao precisa ser consumido externamente; manter publica apenas a API realmente necessaria |
| Media | Concentracao excessiva de responsabilidades | `SemanticAstBuilder`, `SemanticResolver` e `AbstractRuntimeEvaluator` concentram muitos casos, regras e variacoes | Aumenta custo de manutencao, eleva risco de regressao e dificulta testes mais focados | Fatiar por responsabilidade: builder por familia de operacoes, resolver por tipo de regra, runtime por operacao/coercao/comparacao |
| Media | Precisao inconsistente em operacoes avancadas | `pow`, `root` e `sqrt` em `runtime/AbstractRuntimeEvaluator.java` convertem para `double` e depois voltam para `BigDecimal` | Perde precisao justamente nas operacoes mais sensiveis de calculo | Usar implementacao baseada em `big-math` ou explicitar contrato de precisao limitada |
| Baixa | Builder de ambiente com varias responsabilidades | `api/ExpressionEnvironmentBuilder.java` descobre funcoes via reflexao, monta catalogos, gera IDs e prepara coercao/runtime | Classe cresce como ponto central de montagem e validacao | Extrair discovery de funcoes, assinatura estavel e montagem de simbolos para colaboradores dedicados |

## Observacoes de Qualidade de Codigo

### Boas Praticas Encontradas

- uso consistente de `Objects.requireNonNull(...)`
- uso de `record` para modelos pequenos e imutaveis
- colecoes defensivamente copiadas em modelos expostos
- contratos simples nas facades publicas `MathExpression` e `LogicalExpression`
- estrategia `SLL -> LL` bem encapsulada no parser facade

### Riscos de Manutencao

- o modulo depende fortemente de pattern matching em `switch` sobre muitos tipos da AST
- a semantica de tipos, resolucao de simbolos e bind de funcoes esta centralizada em um unico resolvedor
- a execucao mistura interpretacao, coercao, comparacao e algoritmos matematicos na mesma classe base

Esses pontos nao sao defeitos arquiteturais imediatos, mas sao sinais claros de que o modulo ainda esta em fase de consolidacao interna e precisa de fronteiras mais rigidas antes de crescer.

## Resultado da Validacao

Os testes automatizados do modulo passaram com:

```shell
mvn -q -pl exp-eval-mk2 test
```

Tambem foi validado manualmente que:

- `false and missing > 0` atualmente falha por avaliar o lado direito
- `2 ^ 0.5` retorna `1.4142135623730951`, evidenciando conversao para `double`

## Ordem Recomendada de Refatoracao

1. Corrigir primeiro os problemas de corretude observavel no runtime.
2. Reduzir a exposicao de tipos internos para estabilizar a API publica.
3. Quebrar ciclos de dependencia entre `api`, `compiler`, `semantic` e `runtime`.
4. Fatiar `SemanticAstBuilder`, `SemanticResolver` e `AbstractRuntimeEvaluator` por responsabilidade.
5. Revisar a politica de precisao matematica e alinhar a implementacao com o contrato desejado.
6. So depois disso aplicar refactors de limpeza local com `clean-code`.

## Direcao Recomendada

Se o modulo continuar evoluindo como engine reutilizavel, a melhor direcao nao e migrar para package-by-feature, e sim endurecer o pipeline atual:

- `grammar` como fronteira sintatica
- `ast` como modelo estrutural
- `semantic` como analise e binding
- `runtime` como execucao
- `api` como fachada minima

Ou seja: manter o estilo atual, mas com menos tipos publicos, menos ciclos e menos classes centrais sobrecarregadas.

## Conclusao

`exp-eval-mk2` esta em um bom ponto de organizacao conceitual. O pipeline principal esta claro, os testes cobrem bem a base e a modelagem da AST/runtime esta moderna para Java 21.

Os principais problemas nao estao na ideia geral da arquitetura, e sim em tres pontos concretos:

- corretude do runtime logico
- acoplamento ciclico entre pacotes principais
- concentracao de responsabilidade em poucas classes centrais

Esses itens devem ser tratados antes de qualquer expansao relevante da linguagem ou refino de desempenho.

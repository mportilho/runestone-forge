# Plano de Remediacao do Ciclo Entre Pacotes Principais

## Contexto

O achado `Ciclo entre pacotes principais` registrado em `docs/architecture-and-code-quality-review.md`
aponta um problema de direcao de dependencias no modulo `exp-eval-mk2`.

Hoje, a fachada publica em `api` depende de `compiler` e `runtime`, o que por si so e aceitavel.
O problema aparece porque o miolo da engine volta a depender de `api`, formando um ciclo entre
camadas que deveriam ser mais estaveis e internas.

## Evidencias do Ciclo Atual

### Dependencias de `api` para o core

- `api/MathExpression.java` usa `ExpressionCompiler` e `ExpressionRuntimeSupport`
- `api/LogicalExpression.java` usa `ExpressionCompiler` e `ExpressionRuntimeSupport`

### Dependencias do core para `api`

- `compiler/ExpressionCompiler.java` depende de `api.ExpressionEnvironment`
- `semantic/ResolutionContext.java` depende de `api.ExpressionEnvironment`
- `runtime/ExpressionRuntimeSupport.java` depende de `api.ExpressionEnvironment`
- `compiler/ExpressionCacheKey.java` depende de `api.ExpressionEnvironmentId`

## Diagnostico

O problema central nao e apenas o pacote `api` importar classes internas. O problema real e que
`ExpressionEnvironment` virou um agregado central de colaboracoes internas, apesar de morar na
fachada publica.

Na pratica, `ExpressionEnvironment` encapsula:

- identidade de ambiente para cache
- catalogo de funcoes para compilacao e resolucao semantica
- catalogo de simbolos externos
- `RuntimeValueFactory`
- `RuntimeCoercionService`

Com isso:

- `compiler` precisa de `api` para cache e resolucao semantica
- `semantic` precisa de `api` para acessar catalogos
- `runtime` precisa de `api` para bind de simbolos e coercao

Ou seja, `api` deixou de ser apenas fachada e passou a ser fornecedor de infraestrutura do core.

## Efeito Arquitetural

Esse ciclo produz alguns efeitos indesejados:

- dificulta enxergar uma direcao clara de dependencia
- aumenta o acoplamento entre fachada publica e implementacao interna
- torna mais arriscado reduzir a superficie publica depois
- dificulta transformar `api` em fachada minima
- atrapalha a evolucao independente de compilacao, semantica e runtime

## Proposicao de Solucao

A solucao recomendada e manter o estilo arquitetural atual, orientado a pipeline tecnico, mas
extraindo contratos neutros para o core e deixando `api` apenas como fachada.

### Diretriz principal

`api` deve depender do core. O core nao deve depender de `api`.

### Estrategia

Criar um pacote neutro para contratos compartilhados, por exemplo:

`com.runestone.expeval2.engine.context`

Esse pacote deve conter contratos estreitos, focados apenas no que cada etapa realmente precisa.

### Contratos propostos

#### `CompilationEnvironment`

Responsavel apenas por dados necessarios na compilacao e resolucao semantica:

- `ExpressionEnvironmentId environmentId()`
- `FunctionCatalog functionCatalog()`
- `ExternalSymbolCatalog externalSymbolCatalog()`

#### `RuntimeEnvironment`

Responsavel apenas por dados necessarios na execucao:

- `ExternalSymbolCatalog externalSymbolCatalog()`
- `RuntimeValueFactory runtimeValueFactory()`
- `RuntimeCoercionService runtimeCoercionService()`

#### `ExpressionEnvironmentId`

Tambem deve sair de `api` e ir para esse pacote neutro, pois hoje e usado pelo cache do compilador.

## Como a Solucao Quebra o Ciclo

Com essa mudanca:

- `ExpressionCompiler` passa a depender de `CompilationEnvironment`, nao de `api.ExpressionEnvironment`
- `ResolutionContext` deixa de conhecer `api` e recebe apenas os catalogos necessarios
- `ExpressionRuntimeSupport` passa a depender de `RuntimeEnvironment`, nao de `api.ExpressionEnvironment`
- `ExpressionCacheKey` deixa de importar um tipo de `api`
- `api.ExpressionEnvironment` vira apenas uma implementacao/adaptador desses contratos

Assim, a direcao correta passa a ser:

```text
api -> compiler
api -> runtime
compiler -> grammar/ast/semantic/context
semantic -> ast/catalog/context
runtime -> compiler/catalog/context
```

## Arquitetura Alvo

### `api`

Deve conter somente a fachada publica:

- `MathExpression`
- `LogicalExpression`
- builder publico do ambiente
- eventualmente um `ExpressionEnvironment` publico como adaptador imutavel

### `engine.context` ou equivalente

Deve conter contratos neutros compartilhados pelo core:

- `CompilationEnvironment`
- `RuntimeEnvironment`
- `ExpressionEnvironmentId`

### `compiler`

Deve conhecer parser, AST, semantica e contrato de compilacao, mas nao a fachada publica.

### `runtime`

Deve conhecer tipos compilados e contrato de runtime, mas nao a fachada publica.

### `semantic`

Deve conhecer AST, catalogos e contexto semantico, sem importar `api`.

## Ajuste Adicional Recomendado

Existe um segundo acoplamento estrutural que vale tratar na mesma frente, porque ele reduz a
eficacia da quebra de ciclo se permanecer intocado.

Hoje:

- `catalog/FunctionDescriptor.java` depende de `semantic.ResolvedType`
- `catalog/ExternalSymbolDescriptor.java` depende de `runtime.RuntimeValue`

Isso sugere que `catalog` tambem esta acoplado a camadas que deveriam consumi-lo, nao defini-lo.

### Proposicao complementar

Extrair um pacote neutro de tipos, por exemplo:

`com.runestone.expeval2.types`

Esse pacote pode receber:

- `ResolvedType`
- `ResolvedTypes`
- `ScalarType`
- `VectorType`
- `UnknownType`

E, no caso dos simbolos externos, substituir o `defaultValue` tipado como `RuntimeValue` por uma
representacao mais neutra, como:

- `Object defaultValue`
- ou `Supplier<Object> defaultValueSupplier`

Com isso, a materializacao em `RuntimeValue` fica no runtime, onde ela pertence.

## Refatoracao Recomendada em Etapas

### Etapa 1 - Extrair contratos de contexto

Objetivo: quebrar a dependencia direta do core para `api`.

Acoes:

- criar `CompilationEnvironment`
- criar `RuntimeEnvironment`
- mover `ExpressionEnvironmentId` para pacote neutro
- adaptar `api.ExpressionEnvironment` para implementar esses contratos

### Etapa 2 - Ajustar compilacao e semantica

Objetivo: remover imports de `api` em `compiler` e `semantic`.

Acoes:

- alterar `ExpressionCompiler.compile(...)` para receber `CompilationEnvironment`
- alterar `ResolutionContext` para construir contexto sem conhecer `api.ExpressionEnvironment`
- ajustar `ExpressionCacheKey` para usar o `ExpressionEnvironmentId` do pacote neutro

### Etapa 3 - Ajustar runtime

Objetivo: remover import de `api` em `runtime`.

Acoes:

- alterar `ExpressionRuntimeSupport.from(...)` para receber `RuntimeEnvironment`
- manter o bind de simbolos e coercao dependentes apenas dos contratos de runtime

### Etapa 4 - Limpar superficie publica

Objetivo: consolidar `api` como fachada minima.

Acoes:

- revisar quais tipos realmente precisam continuar publicos
- impedir que tipos internos do core sejam expostos sem necessidade

### Etapa 5 - Extrair sistema de tipos

Objetivo: reduzir acoplamento lateral entre `catalog`, `semantic` e `runtime`.

Acoes:

- criar pacote neutro de tipos
- mover `ResolvedType` e correlatos
- revisar `FunctionDescriptor` e `ExternalSymbolDescriptor`

### Etapa 6 - Neutralizar default values de simbolos externos

Objetivo: retirar dependencia de `catalog` para `runtime`.

Acoes:

- trocar `RuntimeValue` por representacao neutra em `ExternalSymbolDescriptor`
- converter para `RuntimeValue` apenas em `MutableBindings` ou colaborador equivalente

## Ordem de Implementacao Sugerida

1. Extrair `CompilationEnvironment`, `RuntimeEnvironment` e `ExpressionEnvironmentId`
2. Adaptar `ExpressionEnvironment` para implementar os contratos novos
3. Ajustar `compiler`, `semantic` e `runtime` para depender desses contratos
4. Garantir que nao restem imports de `api` fora da fachada
5. Extrair o pacote neutro de tipos
6. Revisar `catalog` para remover dependencia de `runtime`

## Criterio de Pronto

O problema pode ser considerado resolvido quando:

- `compiler`, `semantic` e `runtime` nao importarem nada de `api`
- `api` atuar apenas como fachada e composicao
- os contratos compartilhados estiverem em pacote neutro e estavel
- `catalog` nao depender de `runtime`
- a direcao de dependencia do pipeline ficar explicita e defensavel

## Conclusao

A proposta nao exige migrar o modulo para package-by-feature nem reescrever o pipeline atual.
Ela preserva a organizacao conceitual existente e corrige o principal desvio: a fachada publica
ter se tornado um ponto de dependencia do core.

Em termos praticos, a medida mais importante e transformar `ExpressionEnvironment` em adaptador da
API, e nao em contrato central da engine.

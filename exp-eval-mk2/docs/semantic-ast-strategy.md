## Estratégia para AST Semântica em `exp-eval-mk2`

### Objetivo

Criar uma AST semântica separada da parse tree do ANTLR para que a gramática volte a ser responsável só por reconhecer sintaxe, enquanto tipagem, normalização e futuras regras semânticas passem a viver em Java.

### Premissas

- preservar a gramática atual como baseline de compatibilidade e desempenho
- não reabrir neste passo experimentos que já regrediram parse
- não espelhar a parse tree 1:1; a AST deve representar significado, não produção gramatical
- manter o caminho atual `SLL -> LL` intacto durante a introdução da AST
- tratar `typeHint` como artefato sintático de desambiguação e performance do parser, não como operação semântica

### Problema que a AST deve resolver

Hoje a gramática diferencia demais por tipo em pontos como:

- `logicalComparisonExpression`
- `allEntityTypes`
- `assignmentValue`
- famílias duplicadas como `numericEntity`, `stringEntity`, `dateEntity`, `timeEntity`, `dateTimeEntity`, `vectorEntity`

Isso melhora controle sintático, mas espalha semântica por muitas regras e duplica estruturas equivalentes. A AST deve absorver essa duplicação, principalmente:

- unificar os dois formatos de decisão (`if ... then ... else` e `if(...)`)
- unificar comparações tipadas em um único nó semântico
- apagar da AST os marcadores sintáticos usados só para desambiguação do parser
- separar forma sintática de tipo resolvido
- permitir mover validações e inferência sem tocar imediatamente na gramática

### Estratégia em 4 camadas

#### 1. Parse tree continua como está

O ANTLR segue gerando lexer, parser e contexts. Nenhuma mudança inicial no `.g4`.

#### 2. AST sintática mínima, já orientada a semântica

Adicionar um conjunto pequeno de nós estáveis, sem reproduzir cada regra do parser:

- `ExpressionFileNode`
- `AssignmentNode`
- `SimpleAssignmentNode`
- `DestructuringAssignmentNode`
- `ExpressionNode`
- `LiteralNode`
- `IdentifierNode`
- `FunctionCallNode`
- `ConditionalNode`
- `UnaryOperationNode`
- `BinaryOperationNode`
- `PostfixOperationNode`
- `VectorLiteralNode`

Shape base recomendado para os nós:

- usar `sealed interface Node` como raiz comum
- exigir `nodeId()` e `sourceSpan()` no contrato base
- modelar nós concretos como `record`
- evitar classe abstrata geral enquanto a AST puder permanecer declarativa e imutável

`SourceSpan` é o metadado mínimo de localização do nó no texto original, para permitir diagnóstico e rastreamento fora da parse tree. Shape inicial recomendado:

```java
public record SourceSpan(
    int startOffset,
    int endOffset,
    int startLine,
    int startColumn,
    int endLine,
    int endColumn
) {}
```

Exemplo de shape:

```java
public sealed interface Node permits ExpressionNode, AssignmentNode {
    NodeId nodeId();
    SourceSpan sourceSpan();
}
```

```java
public sealed interface AssignmentNode extends Node
    permits SimpleAssignmentNode, DestructuringAssignmentNode {}
```

```java
public sealed interface ExpressionNode extends Node
    permits LiteralNode, IdentifierNode, FunctionCallNode, ConditionalNode,
            UnaryOperationNode, BinaryOperationNode, PostfixOperationNode, VectorLiteralNode {}
```

```java
public record IdentifierNode(
    NodeId nodeId,
    SourceSpan sourceSpan,
    String name
) implements ExpressionNode {}
```

Regra prática: se dois formatos diferentes geram o mesmo significado, eles devem cair no mesmo nó.

Exemplos:

- `if true then 1 else 2 endif` e `if(true, 1, 2)` viram `ConditionalNode`
- `1 > 2`, `"a" = "b"` e `<date>x <= currDate` viram `BinaryOperationNode` com operador de comparação
- `foo`, `<number>foo` e `<number>(foo)` viram a mesma forma semântica quando o wrapper tipado existir só para ajudar o parser

Consequência direta: a AST não deve expor `CastNode`. Se a parse tree usar `castExpression` ou referência tipada apenas como mecanismo sintático, o builder deve normalizar isso para a expressão subjacente.

#### 3. Anotação semântica fora da AST estrutural

Não misturar estrutura com resolução logo de saída. A AST estrutural deve ser imutável e simples. Tudo que for derivado, indexado ou resolvido deve ficar concentrado em um container paralelo chamado `SemanticModel`.

Também é importante separar três coisas que parecem próximas, mas têm papéis diferentes:

- `IdentifierNode`: ocorrência sintática de um nome dentro da AST
- `SymbolRef`: símbolo semântico ao qual uma ou mais ocorrências de identificador apontam
- `MutableBindings`: valores externos fornecidos pelo usuário após o parse
- `ExecutionScope`: estado efetivo de uma execução, incluindo variáveis derivadas de assignments internos

Regra semântica explícita:

- variáveis definidas por `assignmentExpression` nunca podem receber valor via API do usuário
- `setValue(...)` aceita apenas símbolos de entrada externa
- todo símbolo criado por `SimpleAssignmentNode` ou `DestructuringAssignmentNode` é interno à expressão

Exemplo conceitual:

- na expressão `principal + principal * rate`, a AST pode ter dois `IdentifierNode` distintos para `principal`
- os dois nós podem apontar para o mesmo `SymbolRef("principal")`
- o valor informado pelo usuário para `principal` deve existir uma única vez em `MutableBindings`

Além disso, o modelo semântico deve expor índices para buscas diretas por símbolo, sem precisar varrer a AST:

- `Map<SymbolRef, List<IdentifierNode>>`: todas as leituras/ocorrências de variáveis na expressão
- `Map<SymbolRef, List<AssignmentNode>>`: todos os pontos onde a variável é definida por assignment

Para `DestructuringAssignmentNode`, a recomendação é indexar cada variável alvo nesse mesmo mapa de `AssignmentNode`, para que consumidores consultem assignments simples e destructuring pela mesma estrutura.

Exemplo conceitual:

- `principal + principal * rate`
  - `Map<SymbolRef, List<IdentifierNode>>["principal"] -> [IdentifierNode#1, IdentifierNode#2]`
- `[left, right] = coords()`
  - `Map<SymbolRef, List<AssignmentNode>>["left"] -> [DestructuringAssignmentNode#10]`
  - `Map<SymbolRef, List<AssignmentNode>>["right"] -> [DestructuringAssignmentNode#10]`

Cada `ExpressionNode` terá um `nodeId`, e o resolvedor deve produzir um `SemanticModel` com:

- `ExpressionFileNode ast`
- `Map<NodeId, ResolvedType>`
- `Map<NodeId, SymbolRef>`
- `Map<SymbolRef, List<IdentifierNode>>`
- `Map<SymbolRef, List<AssignmentNode>>`
- `Map<String, SymbolRef>` para símbolos externos
- `Map<String, SymbolRef>` para símbolos internos
- `List<SemanticIssue>`

Shape sugerido:

```java
public record SemanticModel(
    ExpressionFileNode ast,
    Map<NodeId, ResolvedType> resolvedTypes,
    Map<NodeId, SymbolRef> symbolByNodeId,
    Map<SymbolRef, List<IdentifierNode>> identifierUsages,
    Map<SymbolRef, List<AssignmentNode>> assignmentUsages,
    Map<String, SymbolRef> externalSymbolsByName,
    Map<String, SymbolRef> internalSymbolsByName,
    List<SemanticIssue> issues
) {}
```

Isso evita acoplar a AST a um estado mutável e permite testar builder, resolução e execução separadamente.

### Estrutura de runtime recomendada

Para suportar um fluxo como:

```java
Expression exp = new Expression("principal + principal * rate", functionCatalog);
exp.setValue("principal", 10000);
exp.setValue("rate", 0.038);
BigDecimal result = exp.computeMath();
```

o desenho recomendado é separar quatro objetos:

- `Expression`: facade mutável exposta ao usuário
- `CompiledExpression`: resultado imutável de parse + análise
- `MutableBindings`: valores externos preenchidos pelo usuário após o parse
- `ExecutionScope`: estado temporário usado por uma execução específica
- `FunctionCatalog`: catálogo imutável e compartilhável com as funções conhecidas pela resolução semântica e pela execução

Responsabilidades:

- `Expression` encapsula a API amigável (`setValue`, `computeMath`, `computeLogical`)
- `CompiledExpression` guarda `source` e `SemanticModel`
- `MutableBindings` resolve nome de variável para `SymbolRef` apenas entre os símbolos externos e armazena valores informados pelo usuário
- `ExecutionScope` nasce de um snapshot de `MutableBindings` e recebe também assignments internos da própria expressão
- `FunctionCatalog` concentra descoberta, indexação e armazenamento dos métodos públicos expostos pela API
- `FunctionCatalogBuilder` monta um `FunctionCatalog` a partir de classes estáticas e instâncias do usuário
- `ResolvedFunctionBinding` representa o binding estável entre um `FunctionCallNode` e a função concreta escolhida na fase de resolução

Regra arquitetural explícita:

- a gramática continua reconhecendo `IDENTIFIER(...)` sem consultar catálogo
- `FunctionCatalog` não participa do parse
- `FunctionCatalog` entra apenas em `compile -> resolve -> runtime`
- `compute...()` não deve fazer busca por nome, reflexão nem resolução de overload
- um mesmo `FunctionCatalog` pode ser compartilhado por múltiplas instâncias de `Expression`

Shape sugerido:

```java
public final class Expression {

    private final CompiledExpression compiledExpression;
    private final MutableBindings bindings;

    public Expression(String source, FunctionCatalog functionCatalog) {
        this.compiledExpression = ExpressionCompiler.compile(source, functionCatalog);
        this.bindings = new MutableBindings(compiledExpression.semanticModel());
    }

    public Expression setValue(String symbolName, Object rawValue) {
        bindings.setValue(symbolName, rawValue);
        return this;
    }

    public BigDecimal computeMath() {
        ExecutionScope scope = ExecutionScope.from(bindings.snapshot());
        return new MathEvaluator(compiledExpression).evaluate(scope);
    }
}
```

```java
public record CompiledExpression(
    String source,
    SemanticModel semanticModel
) {}
```

```java
public interface FunctionCatalog {
    Optional<FunctionDescriptor> findExact(String name, int arity);
    Collection<FunctionDescriptor> findCandidates(String name);
}
```

```java
public interface FunctionCatalogBuilder {
    FunctionCatalogBuilder registerStaticProvider(Class<?> providerClass);
    FunctionCatalogBuilder registerInstanceProvider(Object providerInstance);
    FunctionCatalog build();
}
```

```java
public record ResolvedFunctionBinding(
    FunctionRef functionRef,
    FunctionDescriptor descriptor,
    ResolvedType returnType
) {}
```

Observações de desempenho:

- descoberta de métodos e criação de invokers acontece no `FunctionCatalogBuilder`, fora do caminho quente
- `FunctionCatalog` deve ser imutável depois de construído
- cache de expressão compilada deve considerar a identidade ou versão do catálogo, não apenas o source
- providers de instância registrados no catálogo precisam ser imutáveis ou thread-safe para compartilhamento seguro

Cache de expressão compilada:

- é um cache de `CompiledExpression`, não do resultado final de `compute...()`
- deve viver no compilador ou em uma facade de compilação compartilhada, não dentro de cada instância de `Expression`
- serve para reaproveitar `parse + buildAst + resolve` quando a mesma expressão for compilada repetidamente com o mesmo contexto estrutural
- não deve armazenar `MutableBindings`, `ExecutionScope` nem valores calculados

Shape sugerido:

```java
public record ExpressionCacheKey(
    String source,
    Object functionCatalogIdentity,
    ExpressionResultType resultType
) {}
```

Regras:

- `functionCatalogIdentity` deve representar a identidade estável ou a versão lógica do catálogo
- duas expressões com o mesmo `source`, mas com catálogos diferentes, não podem compartilhar o mesmo `CompiledExpression`
- se no futuro existirem modos distintos de compilação para matemática e lógica, `resultType` também entra na chave

Exemplo conceitual:

- `sum(a, b)` + catálogo A -> `CompiledExpression#1`
- `sum(a, b)` + catálogo A -> reutiliza `CompiledExpression#1`
- `sum(a, b)` + catálogo B -> compila outro `CompiledExpression`

```java
public final class MutableBindings {

    private final SemanticModel semanticModel;
    private final Map<SymbolRef, RuntimeValue> values = new HashMap<>();

    public MutableBindings(SemanticModel semanticModel) {
        this.semanticModel = semanticModel;
    }

    public void setValue(String symbolName, Object rawValue) {
        SymbolRef symbolRef = requireSymbol(symbolName);
        values.put(symbolRef, RuntimeValueFactory.from(rawValue));
    }

    public Optional<RuntimeValue> find(SymbolRef symbolRef) {
        return Optional.ofNullable(values.get(symbolRef));
    }

    public Map<SymbolRef, RuntimeValue> snapshot() {
        return Map.copyOf(values);
    }
}
```

```java
public final class ExecutionScope {

    private final Map<SymbolRef, RuntimeValue> values;

    private ExecutionScope(Map<SymbolRef, RuntimeValue> values) {
        this.values = new HashMap<>(values);
    }

    public static ExecutionScope from(Map<SymbolRef, RuntimeValue> baseValues) {
        return new ExecutionScope(baseValues);
    }

    public Optional<RuntimeValue> find(SymbolRef symbolRef) {
        return Optional.ofNullable(values.get(symbolRef));
    }

    public void assign(SymbolRef symbolRef, RuntimeValue value) {
        values.put(symbolRef, value);
    }
}
```

Observações de modelagem:

- `SemanticModel` deve expor `externalSymbolsByName` e `internalSymbolsByName`
- `setValue("principal", ...)` consulta apenas `externalSymbolsByName`
- `setValue(...)` deve falhar se o nome pertencer a `internalSymbolsByName`
- `MutableBindings` é mutável e vive junto da facade `Expression`
- `ExecutionScope` é mutável, mas existe só durante uma computação
- assignments da própria expressão escrevem em `ExecutionScope`, não em `MutableBindings`

#### 4. Resolver semântico incremental

Criar um resolvedor em fases:

1. resolução de literais e built-ins (`currDate`, `currTime`, `currDateTime`)
2. resolução de referências
3. resolução de operadores
4. resolução de decisões condicionais
5. resolução de vetores e destructuring

No início, o resolvedor pode aceitar `UNKNOWN` quando ainda não conseguir inferir algo com segurança. O objetivo inicial é mapear bem a linguagem, não fechar todas as regras de tipo.

### Modelo de tipos recomendado

Evitar um `enum` simplista se a AST já precisa carregar vetor e futuro refinamento. Preferir:

```java
sealed interface ResolvedType permits ScalarType, VectorType, UnknownType {}
```

Com início mínimo:

- `ScalarType(Boolean | Number | String | Date | Time | DateTime)`
- `VectorType`
- `UnknownType`

Como a linguagem aceita vetores heterogêneos, `VectorType` não deve assumir homogeneidade na primeira versão. Se isso for necessário depois, pode evoluir para metadados por posição.

### Normalizações obrigatórias no builder

O builder `parse tree -> AST` deve fazer as seguintes normalizações desde o primeiro dia:

- colapsar decisões textuais e funcionais em `ConditionalNode`
- colapsar comparações tipadas em uma forma única
- remover wrappers de `typeHint` que existam apenas para desambiguação sintática
- tratar referência tipada e referência não tipada como a mesma forma semântica, salvo se no futuro surgir regra semântica explícita que justifique distinção
- representar cadeia de operadores por associatividade sem preservar a árvore artificial da gramática

Exemplo:

```text
sumExpression
  -> multiplicationExpression ((PLUS | MINUS) multiplicationExpression)*
```

Deve virar uma árvore binária semântica previsível, não uma lista heterogênea de contexts do ANTLR.

### O que não fazer

- não criar um nó por alternativa rotulada do ANTLR
- não mover validação semântica para dentro do parser
- não relaxar a gramática junto com a introdução da AST
- não tentar tipar tudo no builder
- não promover `typeHint` a contrato semântico se ele só existir para o parser
- não introduzir visitor sem testes de normalização

### Pipeline proposto

Pipeline alvo:

```text
input
-> ExpressionCompiler
-> ExpressionEvaluatorV2ParserFacade
-> parse tree
-> SemanticAstBuilder
-> AST estrutural
-> SemanticResolver
-> SemanticModel
-> CompiledExpression
-> MutableBindings
-> ExecutionScope
-> evaluator futuro
```

API sugerida:

```java
public interface SemanticAstBuilder {
    ExpressionFileNode buildMath(ExpressionEvaluatorV2Parser.MathStartContext root);
    ExpressionFileNode buildLogical(ExpressionEvaluatorV2Parser.LogicalStartContext root);
}
```

```java
public interface SemanticResolver {
    SemanticModel resolve(ExpressionFileNode file, ResolutionContext context);
}
```

```java
public record CompiledExpression(
    String source,
    SemanticModel semanticModel
) {}
```

```java
public interface ExpressionCompiler {
    CompiledExpression compile(String source, FunctionCatalog functionCatalog);
}
```

```java
public interface MutableBindings {
    void setValue(String symbolName, Object rawValue);
    Optional<RuntimeValue> find(SymbolRef symbolRef);
    Map<SymbolRef, RuntimeValue> snapshot();
}
```

```java
public interface ExecutionScope {
    Optional<RuntimeValue> find(SymbolRef symbolRef);
    void assign(SymbolRef symbolRef, RuntimeValue value);
}
```

### Pacotes sugeridos

Sem misturar ANTLR com domínio semântico:

- `com.runestone.expeval2.grammar.language`
  - parser facade e integração ANTLR
- `com.runestone.expeval2.ast`
  - nós estruturais da AST
- `com.runestone.expeval2.ast.build`
  - visitor ou mapper `parse tree -> AST`
- `com.runestone.expeval2.semantic`
  - `SemanticModel`, resolução, tipos e issues
  - índices de leitura e assignment por `SymbolRef`
- `com.runestone.expeval2.runtime`
  - `CompiledExpression`, `MutableBindings`, `ExecutionScope`, evaluator

### Sequência de implementação recomendada

#### Fase 1. Estrutura base semântica

Entregar:

- hierarquia de nós da AST com `sealed interface Node`
- `sealed interface AssignmentNode` como família comum de assignments
- `NodeId` e `SourceSpan`
- `record` para os nós concretos
- testes unitários dos nós e invariantes básicos

Critério:

- nenhuma mudança no parser atual
- nenhuma classe abstrata base, salvo se surgir comportamento comum que realmente justifique abandonar `record`

#### Fase 2. Builder do subset mais estável

Começar por construções com semântica mais direta:

- literais
- identificadores
- funções
- aritmética
- operadores lógicos
- comparações
- assignments simples

Critério:

- para o corpus de cobertura atual, builder deve montar AST sem erro para casos sem `if` e sem vetores

#### Fase 3. Normalização de decisões e hints sintáticos

Adicionar:

- `ConditionalNode`
- suporte completo a `genericEntity`
- eliminação de wrappers `typeHint(...)`
- eliminação da distinção semântica entre referência tipada e não tipada

Critério:

- builder cobre os cenários de assignment genérico aceitos hoje

#### Fase 4. Vetores e destructuring

Adicionar:

- `VectorLiteralNode`
- `DestructuringAssignmentNode`
- suporte a vetores heterogêneos

Critério:

- builder cobre todos os cenários de `ExpressionEvaluatorV2GrammarCoverageTest`

#### Fase 5. Resolução semântica

Adicionar:

- `SemanticModel`
- `ResolvedType`
- `SemanticIssue`
- `ResolutionContext` com variáveis, funções e built-ins conhecidos na fase de resolução
- `FunctionCatalog`
- `FunctionCatalogBuilder`
- `ResolvedFunctionBinding`
- `CompiledExpression`
- `MutableBindings` com os valores externos informados pelo usuário
- `ExecutionScope` para a execução efetiva da expressão
- índices dentro de `SemanticModel`:
  - `Map<SymbolRef, List<IdentifierNode>>` para leituras
  - `Map<SymbolRef, List<AssignmentNode>>` para definições, incluindo `DestructuringAssignmentNode`
  - `Map<String, SymbolRef>` para símbolos externos aceitos pela facade
  - `Map<String, SymbolRef>` para símbolos internos definidos por assignment
  - `Map<NodeId, ResolvedFunctionBinding>` para chamadas de função já ligadas

Regras da fase 5 para funções:

- `Expression` passa um `FunctionCatalog` para o compilador no construtor
- `SemanticResolver` usa o catálogo para resolver overload, retorno e coerções
- `FunctionCallNode` permanece estrutural; o binding fica fora da AST
- execução usa apenas `ResolvedFunctionBinding`, sem reflection e sem lookup por nome
- erros de função desconhecida, aridade inválida ou ambiguidade devem surgir na compilação, não no `compute...()`

Critério:

- tipos resolvidos para literais, operadores básicos e comparações simples
- chamadas de função resolvidas para binding estável e reutilizável entre execuções da mesma expressão

### Estratégia de testes

Separar testes por responsabilidade:

- `builder`:
  - valida forma da AST
  - valida normalização
  - não valida regra de tipo profunda
- `resolver`:
  - valida inferência
  - valida incompatibilidades
  - ignora a presença de `typeHint` quando ele não tiver papel semântico
  - valida resolução de função via `FunctionCatalog`
  - valida erro para função ausente, aridade inválida e overload ambíguo
- `runtime bindings`:
  - valida `setValue(String, Object)` apenas para símbolos externos
  - rejeita `setValue(String, Object)` para símbolos internos definidos por assignment
  - valida lookup por `SymbolRef`
  - valida snapshot para criar `ExecutionScope`
- `execution scope`:
  - valida reutilização do mesmo símbolo em múltiplos `IdentifierNode`
  - valida assignments internos sem mutar `MutableBindings`
  - valida invocação via `ResolvedFunctionBinding` sem nova resolução
- `symbol indexes`:
  - valida `Map<SymbolRef, List<IdentifierNode>>`
  - valida `Map<SymbolRef, List<AssignmentNode>>`
  - valida presença de variáveis de `DestructuringAssignmentNode` no índice de assignments
- `parser`:
  - continua cobrindo apenas aceitação sintática e estratégia `SLL -> LL`

Testes essenciais do builder:

- mesma AST para `if then else` e `if(...)`
- mesma AST de comparação independentemente da família sintática de origem
- preservação correta de precedência matemática e lógica
- `foo`, `<number>foo` e `<number>(foo)` produzindo a mesma AST semântica quando a diferença for apenas de desambiguação do parser

### Estratégia de medição

Como a AST adiciona custo depois do parse, medir separado:

1. `parse only`
2. `parse + buildAst`
3. `parse + buildAst + resolve`

Registrar isso como novos benchmarks em vez de misturar com os números antigos de parser. O item 9 só deve ser aceito se o ganho arquitetural vier com custo operacional controlado.

### Critério para revisar a gramática depois

Só depois de o builder e o resolvedor estarem estáveis faz sentido revisar a gramática para simplificar pontos como:

- comparações por tipo
- duplicação entre entidades escalares
- parte da pressão hoje concentrada em `allEntityTypes`

Sem essa ordem, o projeto corre o risco de pagar o custo de uma gramática mais permissiva sem ter a camada semântica madura para absorver a mudança.

### Decisão arquitetural recomendada

A estratégia mais segura é:

1. manter a gramática atual
2. introduzir AST estrutural pequena e normalizada
3. introduzir resolvedor semântico separado
4. só então testar simplificações de gramática apoiadas por benchmark

Isso desacopla o problema em partes medíveis e evita repetir a regressão observada nos experimentos que já tentaram empurrar mais responsabilidade para uma etapa pós-parse sem uma AST clara.

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

- usar `sealed interface Node` como raiz comum, incluindo `ExpressionFileNode`
- exigir `nodeId()` e `sourceSpan()` no contrato base
- modelar nós concretos como `record`
- evitar classe abstrata geral enquanto a AST puder permanecer declarativa e imutável
- separar o nó-raiz de arquivo dos nós de expressão para não forçar o root semântico a caber artificialmente em `ExpressionNode`

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
public sealed interface Node permits ExpressionFileNode, ExpressionNode, AssignmentNode {
    NodeId nodeId();
    SourceSpan sourceSpan();
}
```

```java
public record ExpressionFileNode(
    NodeId nodeId,
    SourceSpan sourceSpan,
    List<AssignmentNode> assignments,
    ExpressionNode resultExpression
) implements Node {}
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

Também é importante separar quatro coisas que parecem próximas, mas têm papéis diferentes:

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

Regras adicionais de modelagem:

- `SemanticModel`, `ResolutionContext` e coleções expostas publicamente devem ser imutáveis e defensivamente copiadas
- `SemanticIssue` deve carregar ao menos código estável, severidade, mensagem e `SourceSpan`
- índices semânticos são leitura derivada; nenhuma coleção exposta por `SemanticModel` deve permitir mutação externa

### Estrutura de runtime recomendada

Para suportar um fluxo como:

```java
MathExpression exp = MathExpression.compile("principal + principal * annualRate", environment);
exp.setValue("principal", 10000);
BigDecimal result = exp.compute();
```

o desenho recomendado é separar dez objetos:

- `MathExpression`: facade mutável pública para expressões matemáticas
- `LogicalExpression`: facade mutável pública para expressões lógicas
- `ExpressionEnvironment`: contexto imutável compartilhável entre compilação e runtime
- `ExternalSymbolCatalog`: catálogo imutável de símbolos externos pré-vinculados ao ambiente
- `CompiledExpression`: resultado imutável de parse + análise
- `MutableBindings`: valores externos preenchidos pelo usuário após o parse
- `ExecutionScope`: estado temporário usado por uma execução específica
- `FunctionCatalog`: catálogo imutável e compartilhável com as funções conhecidas pela resolução semântica e pela execução
- `ExternalSymbolDescriptor`: descritor de símbolo externo com valor padrão obrigatório
- `ExpressionRuntimeSupport`: suporte interno compartilhado entre as duas facades públicas

Responsabilidades:

- `MathExpression` encapsula a API amigável de matemática (`setValue`, `compute`)
- `LogicalExpression` encapsula a API amigável de lógica (`setValue`, `compute`)
- `ExpressionEnvironment` agrega `FunctionCatalog`, `ExternalSymbolCatalog` e a identidade estável usada no cache de compilação
- `ExternalSymbolCatalog` concentra símbolos externos conhecidos pela aplicação já com valor padrão no ambiente
- `CompiledExpression` guarda `source` e `SemanticModel`
- `MutableBindings` resolve nome de variável para `SymbolRef` apenas entre os símbolos externos e armazena valores informados pelo usuário
- `ExecutionScope` nasce de um snapshot de `MutableBindings` e recebe também assignments internos da própria expressão
- `FunctionCatalog` concentra descoberta, indexação e armazenamento dos métodos públicos expostos pela API
- `ExpressionEnvironmentBuilder` monta um `ExpressionEnvironment`, incluindo funções e símbolos externos defaultados
- `ResolvedFunctionBinding` representa o binding estável entre um `FunctionCallNode` e a função concreta escolhida na fase de resolução
- `ExternalSymbolDescriptor` modela nome, tipo, valor padrão e política de sobrescrita para símbolos do ambiente
- `ExpressionRuntimeSupport` concentra o estado compartilhado (`CompiledExpression` + `MutableBindings`) para evitar duplicação entre `MathExpression` e `LogicalExpression`

Regra arquitetural explícita:

- a gramática continua reconhecendo `IDENTIFIER(...)` sem consultar catálogo
- `ExpressionEnvironment` não participa do parse
- `ExpressionEnvironment` entra apenas em `compile -> resolve -> runtime`
- cada instância pública de `MathExpression` ou `LogicalExpression` é vinculada a um único `ExpressionResultType` desde a compilação
- a API pública não deve expor uma facade genérica `Expression` com múltiplos `compute...()`
- `compute()` não deve fazer busca por nome, reflexão nem resolução de overload
- um mesmo `ExpressionEnvironment` pode ser compartilhado por múltiplas instâncias de `MathExpression` e `LogicalExpression`
- todo símbolo presente em `ExternalSymbolCatalog` deve possuir `defaultValue`
- `ExternalSymbolCatalog` não declara símbolos "somente tipados"; identificadores externos sem valor padrão continuam sendo descobertos pela análise semântica da expressão
- `setValue(...)` pode sobrescrever apenas símbolos externos marcados como `overridable`

Shape sugerido:

```java
public final class MathExpression {

    private static final ExpressionCompiler COMPILER = new ExpressionCompiler();

    private final ExpressionRuntimeSupport runtime;

    private MathExpression(ExpressionRuntimeSupport runtime) {
        this.runtime = runtime;
    }

    public static MathExpression compile(String source, ExpressionEnvironment environment) {
        CompiledExpression compiled = COMPILER.compile(source, ExpressionResultType.MATH, environment);
        return new MathExpression(ExpressionRuntimeSupport.from(compiled, environment));
    }

    public MathExpression setValue(String symbolName, Object rawValue) {
        runtime.setValue(symbolName, rawValue);
        return this;
    }

    public BigDecimal compute() {
        return new MathEvaluator(runtime.compiledExpression()).evaluate(runtime.createExecutionScope());
    }
}
```

```java
public final class LogicalExpression {

    private static final ExpressionCompiler COMPILER = new ExpressionCompiler();

    private final ExpressionRuntimeSupport runtime;

    private LogicalExpression(ExpressionRuntimeSupport runtime) {
        this.runtime = runtime;
    }

    public static LogicalExpression compile(String source, ExpressionEnvironment environment) {
        CompiledExpression compiled = COMPILER.compile(source, ExpressionResultType.LOGICAL, environment);
        return new LogicalExpression(ExpressionRuntimeSupport.from(compiled, environment));
    }

    public LogicalExpression setValue(String symbolName, Object rawValue) {
        runtime.setValue(symbolName, rawValue);
        return this;
    }

    public boolean compute() {
        return new LogicalEvaluator(runtime.compiledExpression()).evaluate(runtime.createExecutionScope());
    }
}
```

```java
final class ExpressionRuntimeSupport {

    private final CompiledExpression compiledExpression;
    private final MutableBindings bindings;

    private ExpressionRuntimeSupport(
        CompiledExpression compiledExpression,
        MutableBindings bindings
    ) {
        this.compiledExpression = compiledExpression;
        this.bindings = bindings;
    }

    static ExpressionRuntimeSupport from(
        CompiledExpression compiledExpression,
        ExpressionEnvironment environment
    ) {
        return new ExpressionRuntimeSupport(
            compiledExpression,
            MutableBindings.from(
                compiledExpression.semanticModel(),
                environment.externalSymbolCatalog(),
                environment.runtimeValueFactory()
            )
        );
    }

    void setValue(String symbolName, Object rawValue) {
        bindings.setValue(symbolName, rawValue);
    }

    ExecutionScope createExecutionScope() {
        return ExecutionScope.from(bindings.snapshot());
    }

    CompiledExpression compiledExpression() {
        return compiledExpression;
    }
}
```

```java
public final class ExpressionEnvironment {
    public ExpressionEnvironmentId environmentId() { /* ... */ }
    public FunctionCatalog functionCatalog() { /* ... */ }
    public ExternalSymbolCatalog externalSymbolCatalog() { /* ... */ }
    public RuntimeValueFactory runtimeValueFactory() { /* ... */ }
}
```

```java
public final class ExternalSymbolCatalog {
    public Optional<ExternalSymbolDescriptor> find(String name) { /* ... */ }
    public Map<String, ExternalSymbolDescriptor> symbolsByName() { /* ... */ }
}
```

```java
public record ExternalSymbolDescriptor(
    String name,
    ResolvedType declaredType,
    RuntimeValue defaultValue,
    boolean overridable
) {}
```

```java
public sealed interface RuntimeValue
    permits NumberValue, BooleanValue, StringValue, DateValue, TimeValue, DateTimeValue, VectorValue, NullValue {

    ResolvedType type();
    Object raw();
}
```

```java
public record NumberValue(BigDecimal value) implements RuntimeValue {
    @Override
    public ResolvedType type() { return ScalarType.NUMBER; }

    @Override
    public Object raw() { return value; }
}
```

```java
public record BooleanValue(boolean value) implements RuntimeValue {
    @Override
    public ResolvedType type() { return ScalarType.BOOLEAN; }

    @Override
    public Object raw() { return value; }
}
```

```java
public record StringValue(String value) implements RuntimeValue {
    @Override
    public ResolvedType type() { return ScalarType.STRING; }

    @Override
    public Object raw() { return value; }
}
```

```java
public record DateValue(LocalDate value) implements RuntimeValue {
    @Override
    public ResolvedType type() { return ScalarType.DATE; }

    @Override
    public Object raw() { return value; }
}
```

```java
public record TimeValue(LocalTime value) implements RuntimeValue {
    @Override
    public ResolvedType type() { return ScalarType.TIME; }

    @Override
    public Object raw() { return value; }
}
```

```java
public record DateTimeValue(LocalDateTime value) implements RuntimeValue {
    @Override
    public ResolvedType type() { return ScalarType.DATETIME; }

    @Override
    public Object raw() { return value; }
}
```

```java
public record VectorValue(List<RuntimeValue> elements) implements RuntimeValue {
    public VectorValue {
        elements = List.copyOf(elements);
    }

    @Override
    public ResolvedType type() { return VectorType.INSTANCE; }

    @Override
    public Object raw() { return elements; }
}
```

```java
public enum NullValue implements RuntimeValue {
    INSTANCE;

    @Override
    public ResolvedType type() { return UnknownType.INSTANCE; }

    @Override
    public Object raw() { return null; }
}
```

```java
public final class RuntimeValueFactory {

    private final DataConversionService conversionService;

    public RuntimeValueFactory(DataConversionService conversionService) {
        this.conversionService = Objects.requireNonNull(conversionService);
    }

    public RuntimeValue from(Object rawValue) {
        return from(rawValue, null);
    }

    public RuntimeValue from(Object rawValue, ResolvedType expectedType) {
        // normaliza Object externo para um RuntimeValue canônico
    }
}
```

```java
public final class RuntimeCoercionService {

    private final DataConversionService conversionService;

    public RuntimeCoercionService(DataConversionService conversionService) {
        this.conversionService = Objects.requireNonNull(conversionService);
    }

    public BigDecimal asNumber(RuntimeValue value) { /* ... */ }
    public boolean asBoolean(RuntimeValue value) { /* ... */ }
    public String asString(RuntimeValue value) { /* ... */ }
    public LocalDate asDate(RuntimeValue value) { /* ... */ }
    public LocalTime asTime(RuntimeValue value) { /* ... */ }
    public LocalDateTime asDateTime(RuntimeValue value) { /* ... */ }
}
```

```java
public record CompiledExpression(
    String source,
    ExpressionResultType resultType,
    SemanticModel semanticModel
) {}
```

```java
public final class FunctionCatalog {
    public FunctionCatalogId catalogId() { /* ... */ }
    public Optional<FunctionDescriptor> findExact(String name, int arity) { /* ... */ }
    public Collection<FunctionDescriptor> findCandidates(String name) { /* ... */ }
}
```

```java
public final class ExpressionEnvironmentBuilder {
    public ExpressionEnvironmentBuilder conversionService(DataConversionService conversionService) { /* ... */ }
    public ExpressionEnvironmentBuilder registerStaticProvider(Class<?> providerClass) { /* ... */ }
    public ExpressionEnvironmentBuilder registerInstanceProvider(Object providerInstance) { /* ... */ }
    public ExpressionEnvironmentBuilder registerExternalSymbol(
        String name,
        ResolvedType declaredType,
        Object defaultValue,
        boolean overridable
    ) { /* ... */ }
    public ExpressionEnvironment build() { /* ... */ }
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

- descoberta de métodos, criação de invokers e normalização dos símbolos externos do ambiente acontecem no `ExpressionEnvironmentBuilder`, fora do caminho quente
- `ExpressionEnvironment`, `FunctionCatalog` e `ExternalSymbolCatalog` devem ser imutáveis depois de construídos
- `RuntimeValueFactory` deve reutilizar um `DataConversionService` estável, em vez de construir lógica de conversão ad hoc em cada evaluator
- cache de expressão compilada deve considerar a identidade estável do ambiente, não apenas o source
- providers de instância registrados no ambiente precisam ser imutáveis ou thread-safe para compartilhamento seguro
- `MathExpression` e `LogicalExpression` devem permanecer facades finas; todo comportamento compartilhado deve ficar fora da API pública

Cache de expressão compilada:

- é um cache de `CompiledExpression`, não do resultado final de `compute...()`
- deve viver no compilador ou em uma facade de compilação compartilhada, não dentro de cada instância de `MathExpression` ou `LogicalExpression`
- serve para reaproveitar `parse + buildAst + resolve` quando a mesma expressão for compilada repetidamente com o mesmo contexto estrutural
- não deve armazenar `MutableBindings`, `ExecutionScope` nem valores calculados

Shape sugerido:

```java
public record ExpressionEnvironmentId(String value) {}
```

```java
public record ExpressionCacheKey(
    String source,
    ExpressionEnvironmentId environmentId,
    ExpressionResultType resultType
) {}
```

Regras:

- `environmentId` deve ser um value object estável, nunca um `Object` genérico sem contrato
- duas expressões com o mesmo `source`, mas com ambientes diferentes, não podem compartilhar o mesmo `CompiledExpression`
- se no futuro existirem modos distintos de compilação para matemática e lógica, `resultType` também entra na chave
- se um símbolo do ambiente mudar apenas de valor padrão, mas isso não afetar resolução, tipagem ou disponibilidade estrutural, o `environmentId` não deve ser invalidado por isso

Exemplo conceitual:

- `sum(a, b)` + ambiente A -> `CompiledExpression#1`
- `sum(a, b)` + ambiente A -> reutiliza `CompiledExpression#1`
- `sum(a, b)` + ambiente B -> compila outro `CompiledExpression`

```java
public final class MutableBindings {

    private final SemanticModel semanticModel;
    private final RuntimeValueFactory runtimeValueFactory;
    private final Map<SymbolRef, RuntimeValue> values = new HashMap<>();

    public static MutableBindings from(
        SemanticModel semanticModel,
        ExternalSymbolCatalog externalSymbolCatalog,
        RuntimeValueFactory runtimeValueFactory
    ) {
        MutableBindings bindings = new MutableBindings(semanticModel, runtimeValueFactory);
        bindings.seedDefaults(externalSymbolCatalog);
        return bindings;
    }

    private MutableBindings(
        SemanticModel semanticModel,
        RuntimeValueFactory runtimeValueFactory
    ) {
        this.semanticModel = semanticModel;
        this.runtimeValueFactory = runtimeValueFactory;
    }

    public void setValue(String symbolName, Object rawValue) {
        SymbolRef symbolRef = requireSymbol(symbolName);
        rejectWhenInternal(symbolName);
        rejectWhenNonOverridable(symbolName);
        values.put(symbolRef, runtimeValueFactory.from(rawValue));
    }

    public Optional<RuntimeValue> find(SymbolRef symbolRef) {
        return Optional.ofNullable(values.get(symbolRef));
    }

    public Map<SymbolRef, RuntimeValue> snapshot() {
        return Map.copyOf(values);
    }

    private void seedDefaults(ExternalSymbolCatalog externalSymbolCatalog) { /* ... */ }
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
- `RuntimeValue` é a representação canônica consumida pelo evaluator; `DataConversionService` é infraestrutura de conversão, não substituto de `RuntimeValue`
- `RuntimeValueFactory` deve receber `Object` externo e produzir `RuntimeValue`, opcionalmente usando o `ResolvedType` esperado
- `RuntimeCoercionService` deve concentrar coerções de `RuntimeValue` para tipos concretos usados pelos evaluators
- `MutableBindings` deve nascer pré-populado com os `defaultValue` de símbolos do `ExternalSymbolCatalog` efetivamente usados pela expressão
- `setValue(...)` deve falhar para símbolos do ambiente com `overridable = false`
- `MutableBindings` é mutável e vive junto da facade pública tipada (`MathExpression` ou `LogicalExpression`)
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
public final class MathExpression {
    public static MathExpression compile(String source, ExpressionEnvironment environment) { /* ... */ }
    public MathExpression setValue(String symbolName, Object rawValue) { /* ... */ }
    public BigDecimal compute() { /* ... */ }
}
```

```java
public final class LogicalExpression {
    public static LogicalExpression compile(String source, ExpressionEnvironment environment) { /* ... */ }
    public LogicalExpression setValue(String symbolName, Object rawValue) { /* ... */ }
    public boolean compute() { /* ... */ }
}
```

```java
final class SemanticAstBuilder {
    ExpressionFileNode buildMath(ExpressionEvaluatorV2Parser.MathStartContext root);
    ExpressionFileNode buildLogical(ExpressionEvaluatorV2Parser.LogicalStartContext root);
}
```

```java
final class SemanticResolver {
    SemanticModel resolve(ExpressionFileNode file, ResolutionContext context);
}
```

```java
public final class ExpressionEnvironment {
    public ExpressionEnvironmentId environmentId() { /* ... */ }
    public FunctionCatalog functionCatalog() { /* ... */ }
    public ExternalSymbolCatalog externalSymbolCatalog() { /* ... */ }
    public RuntimeValueFactory runtimeValueFactory() { /* ... */ }
}
```

```java
public record CompiledExpression(
    String source,
    ExpressionResultType resultType,
    SemanticModel semanticModel
) {}
```

```java
public final class ExpressionCompiler {
    public CompiledExpression compile(
        String source,
        ExpressionResultType resultType,
        ExpressionEnvironment environment
    ) {
        // orchestration
    }
}
```

```java
public final class MutableBindings { /* ... */ }
public final class ExecutionScope { /* ... */ }
final class ExpressionRuntimeSupport { /* ... */ }
public final class ExternalSymbolCatalog { /* ... */ }
public final class FunctionCatalog { /* ... */ }
public final class ExpressionEnvironmentBuilder { /* ... */ }
public final class RuntimeValueFactory { /* ... */ }
public final class RuntimeCoercionService { /* ... */ }
```

Regra de encapsulamento:

- começar com `final class` package-private para `SemanticAstBuilder`, `SemanticResolver` e colaboradores internos
- não criar interfaces prematuramente; começar com `final class` sempre que houver uma única implementação conhecida
- expor como API pública apenas `MathExpression`, `LogicalExpression`, `ExpressionEnvironment`, `ExpressionEnvironmentBuilder` e tipos realmente necessários para integração externa
- evitar hierarquia de interfaces para tipos que hoje têm uma única implementação esperada
- manter um tipo público por arquivo e deixar classes auxiliares como package-private por padrão

### Pacotes sugeridos

Sem misturar ANTLR com domínio semântico:

- `com.runestone.expeval2.grammar.language`
  - parser facade e integração ANTLR
- `com.runestone.expeval2.ast`
  - nós estruturais da AST
- `com.runestone.expeval2.ast.mapping`
  - visitor ou mapper `parse tree -> AST`
- `com.runestone.expeval2.semantic`
  - `SemanticModel`, resolução, tipos e issues
  - índices de leitura e assignment por `SymbolRef`
- `com.runestone.expeval2.compiler`
  - `ExpressionCompiler`, `CompiledExpression`, cache e orquestração de compilação
- `com.runestone.expeval2.api`
  - `MathExpression`, `LogicalExpression`, `ExpressionEnvironment`, `ExpressionEnvironmentBuilder`
- `com.runestone.expeval2.catalog`
  - `FunctionCatalog`, `ExternalSymbolCatalog`, `ExternalSymbolDescriptor`
- `com.runestone.expeval2.runtime`
  - `RuntimeValue`, `RuntimeValueFactory`, `RuntimeCoercionService`
  - `MutableBindings`, `ExecutionScope`, `ExpressionRuntimeSupport`, evaluators

Direção de dependências esperada:

- `grammar` não depende de `ast`, `semantic`, `compiler` nem `runtime`
- `ast` não depende de ANTLR, reflection nem runtime
- `ast.mapping` depende de `grammar` + `ast`
- `semantic` depende de `ast` e dos catálogos/descritores estáveis do ambiente, nunca do parser
- `compiler` orquestra `grammar -> ast.mapping -> semantic`
- `api` depende de `compiler` + `runtime`, sem conhecer ANTLR nem detalhes da resolução
- `catalog` concentra reflection, indexação e validação estrutural do ambiente
- `runtime` depende de `compiler` e `semantic`, mas não volta a falar com ANTLR
- qualquer acesso a reflection de funções deve ficar isolado no builder/catálogo, fora do caminho quente de `runtime`

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
- `ResolutionContext` derivado de `ExpressionEnvironment`, com funções, símbolos externos defaultados e built-ins conhecidos na fase de resolução
- `ExpressionEnvironment`
- `ExpressionEnvironmentBuilder`
- `FunctionCatalog`
- `ExternalSymbolCatalog`
- `ExternalSymbolDescriptor`
- `RuntimeValue`
- `RuntimeValueFactory`
- `RuntimeCoercionService`
- `ResolvedFunctionBinding`
- `CompiledExpression`
- `MutableBindings` com os valores externos informados pelo usuário
- `ExecutionScope` para a execução efetiva da expressão
- `MathExpression` e `LogicalExpression` como facades públicas tipadas
- `ExpressionRuntimeSupport` como suporte interno compartilhado
- `ExpressionResultType` explícito no contrato de compilação e no `CompiledExpression`
- índices dentro de `SemanticModel`:
  - `Map<SymbolRef, List<IdentifierNode>>` para leituras
  - `Map<SymbolRef, List<AssignmentNode>>` para definições, incluindo `DestructuringAssignmentNode`
  - `Map<String, SymbolRef>` para símbolos externos aceitos pela facade
  - `Map<String, SymbolRef>` para símbolos internos definidos por assignment
  - `Map<NodeId, ResolvedFunctionBinding>` para chamadas de função já ligadas

Regras da fase 5 para funções:

- `MathExpression.compile(...)` e `LogicalExpression.compile(...)` passam `ExpressionEnvironment` e o `ExpressionResultType` implícito para o compilador
- `SemanticResolver` usa `FunctionCatalog` e `ExternalSymbolCatalog` vindos do ambiente para resolver funções, símbolos externos defaultados, retorno e coerções
- `FunctionCallNode` permanece estrutural; o binding fica fora da AST
- execução usa apenas `ResolvedFunctionBinding`, sem reflection e sem lookup por nome
- `RuntimeValueFactory` usa `DataConversionService` para normalizar `Object` externo em `RuntimeValue`
- evaluators usam `RuntimeCoercionService`, em vez de chamar `DataConversionService` diretamente em cada operação
- erros de função desconhecida, aridade inválida ou ambiguidade devem surgir na compilação, não no `compute()`
- símbolos externos pré-vinculados pelo ambiente devem entrar na execução já com seus `defaultValue`
- símbolos externos com `overridable = false` devem rejeitar sobrescrita por `setValue(...)`

Critério:

- tipos resolvidos para literais, operadores básicos e comparações simples
- chamadas de função resolvidas para binding estável e reutilizável entre execuções da mesma expressão
- símbolos defaultados do ambiente disponíveis sem `setValue(...)`

### Workflow de entrega por fase

Cada fase acima deve seguir um ciclo curto e explícito:

1. `RED`: definir primeiro os cenários e testes da fase
2. `GREEN`: introduzir a estrutura mínima para deixar esses testes passarem
3. `IMPLEMENT`: substituir stubs e regras temporárias incrementalmente, sem misturar builder, resolver e runtime no mesmo salto
4. `VERIFY`: rodar a suíte relevante do módulo e os benchmarks aplicáveis antes de avançar para a próxima fase

Regras operacionais:

- não começar uma fase com código de produção sem antes definir os testes que delimitam o subset daquela entrega
- não fundir numa única PR mudanças de gramática, builder semântico e runtime
- qualquer mudança em caminho quente deve ser acompanhada de medição comparável com o baseline já documentado

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
  - valida resolução de símbolos pré-vinculados via `ExternalSymbolCatalog`
  - valida erro para função ausente, aridade inválida e overload ambíguo
- `runtime bindings`:
  - valida `setValue(String, Object)` apenas para símbolos externos
  - rejeita `setValue(String, Object)` para símbolos internos definidos por assignment
  - pré-carrega `defaultValue` de símbolos do ambiente efetivamente usados pela expressão
  - rejeita sobrescrita de símbolo do ambiente com `overridable = false`
  - valida normalização de `Object` para `RuntimeValue` via `RuntimeValueFactory`
  - valida lookup por `SymbolRef`
  - valida snapshot para criar `ExecutionScope`
- `execution scope`:
  - valida reutilização do mesmo símbolo em múltiplos `IdentifierNode`
  - valida assignments internos sem mutar `MutableBindings`
  - valida invocação via `ResolvedFunctionBinding` sem nova resolução
- `runtime coercion`:
  - valida coerção de `RuntimeValue` para número, boolean, texto, data, hora e datetime
  - valida fallback para `DataConversionService` apenas quando o valor ainda não estiver na forma canônica
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

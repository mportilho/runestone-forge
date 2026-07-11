# Expression Evaluator MK3

This context defines the language used around expression compilation and evaluation in the `exp-mk3` module.

## Language

**Caso de Expressao**:
A versioned example that describes an expression, its environment, inputs, expected result or expected diagnostic, and language coverage tags.
_Avoid_: Test fixture, corpus item

**Corpus de Expressoes**:
A versioned collection of expression cases used as the shared behavioral contract for parser, semantic resolver, runtime, migration, and differential verification.
_Avoid_: Test data, examples folder

**Plano Imutavel**:
A compiled, thread-safe, reusable representation of an expression that is executed with an isolated scope for each call.
_Avoid_: Compiled tree, executable expression internals

**Resultado Esperado**:
The value or diagnostic that an expression case declares as the correct behavior.
_Avoid_: Assertion payload, expected output

**Ambiente de Expressao**:
The compilation configuration that declares external symbols, functions, Java types, numeric mode, time zone, and limits used to interpret an expression case.
_Avoid_: Test context, evaluation setup

**Identificador de Ambiente**:
A canonical, stable identity derived from all compilation-relevant Ambiente de Expressao content so equivalent environments can share compiled plans safely.
_Avoid_: Object identity, cache tag, random environment id

**Simbolo Externo**:
A named value declared by an Ambiente de Expressao with a required default value and an overwrite policy. Its type is either declared explicitly and validated against the default, or inferred from the default; runtime inputs may replace it only when the symbol is declared overridable.
_Avoid_: Input variable, parameter, free variable, type-only declaration

**Simbolo Interno**:
A named value introduced or updated by expression assignments and stored in one stable expression-frame slot for later assignments, the result expression, or assignment views.
_Avoid_: Local variable, computed parameter, temporary input

**Sombreamento de Simbolo**:
The case where an assignment introduces a Simbolo Interno with the same name as a Simbolo Externo, making later references use the internal value while the creating assignment may still read the external value on its right-hand side.
_Avoid_: Redeclaration error, parameter mutation, aliasing

**Desestruturacao de Atribuicao**:
An assignment target that binds multiple internal symbols from an ordered expression value, with compile-time validation when shape is known and runtime shape checking only when the source shape is known only at execution time.
_Avoid_: Tuple unpacking syntax, multiple assignment statement, array pattern

**Valor Padrao de Simbolo**:
The required value declared with every Simbolo Externo and validated as part of the Ambiente de Expressao. It is the effective value for non-overridable symbols and the fallback value for overridable symbols when no runtime override is supplied.
_Avoid_: Optional runtime fallback, missing input handler

**Politica de Sobrescrita de Simbolo**:
The Ambiente de Expressao declaration that says whether a Simbolo Externo default can be replaced by runtime input. Non-overridable external symbols are fixed environment values; overridable external symbols accept boundary-coerced runtime replacements.
_Avoid_: Mutable parameter, assignment permission, runtime redeclaration

**Fato Numerico**:
Semantic facts about a numeric expression under the language's decimal numeric semantics, such as whether a value is provably integral or provably fractional, used by validation and later planning without changing the public expression type.
_Avoid_: Numeric mode, public number type, Java primitive type, optimization hint

**Tipo Vetor**:
The type of a vector value created by the expression language, preserving ordered elements and one common element type resolved from the vector literal.
_Avoid_: Java list type, generic collection

**Tipo Colecao**:
The type of an iterable group of values supplied externally or produced by navigation and collection operations, preserving an element type without implying vector-literal semantics, indexability, or sliceability.
_Avoid_: Vector alias, raw iterable

**Limite de Materializacao**:
An Ambiente de Expressao guard rail that bounds materialized container values created or exposed by the expression language, such as vector literals, materialized collection-operation results, map entries, or public-boundary collection materialization, without necessarily rejecting large external collections at entry.
_Avoid_: Parser size limit, input collection limit

**Tipo Mapa**:
The type of a text-keyed value map understood by the expression language, preserving the type of values reachable by textual keys.
_Avoid_: Generic Java map, object property bag

**Entrada de Mapa**:
The contextual item type exposed by filtering a Tipo Mapa, with `@.k` as the textual key and `@.v` as the map value. It is available only inside the map filter predicate and is not a general source-level value type.
_Avoid_: Map object, structural pair, Java Map.Entry API

**Tipo Objeto**:
A nominal domain object type whose registered members can be used for navigation without making object compatibility structural.
_Avoid_: Structural record type, map-like object

**Tipo Java Registrado**:
A Java-backed Tipo Objeto whose navigable members are declared by the Ambiente de Expressao through a chosen exposure policy, from property accessors to optionally all public methods.
_Avoid_: Reflected class, automatic object shape

**Valor Nulo de Runtime**:
A runtime absence value introduced by safe navigation. It is not a source literal, cannot be supplied as an external symbol override, and does not have a normal expression type; Java nulls from external data, members, maps, or collections are boundary/runtime contract violations rather than normal expression values. Null coalescence is the explicit way to discharge possible runtime null before a value reaches a non-null context.
_Avoid_: Null literal, bottom type, unknown value

**Nulidade de Runtime**:
Semantic metadata that records whether an expression or binding is proven never to produce a runtime null value or may produce one. It is not a source-level type and does not participate in ordinary type unification, but constructs whose contract rejects runtime null may use it to issue semantic diagnostics.
_Avoid_: Nullable type, optional type, bottom type

**Tipagem Conhecida**:
The semantic requirement that every accepted expression node, symbol, function binding, navigation binding, and collection operation has a known expression type at compilation time. Missing Java metadata, unconstrained empty containers, or ambiguous function contracts are semantic errors rather than unknown types deferred to runtime.
_Avoid_: Dynamic type, any type, unknown semantic type

**Variavel de Tipo Pendente**:
An internal resolver-only placeholder used while inferring a known type from local context, such as an empty vector literal receiving its element type from a sibling branch, function parameter, or membership operand. Every pending type variable must resolve to a known type or produce a semantic diagnostic before a Modelo Semantico can succeed.
_Avoid_: UnknownType, dynamic type, planner-visible placeholder

**Tipo Invalido**:
An internal semantic marker assigned to an expression node after a root semantic diagnostic has already been emitted, allowing resolution to continue and suppressing duplicate cascade diagnostics caused by the same invalid node.
_Avoid_: Unknown type, runtime type error, partial exception

**Restricao de Tipo**:
A semantic requirement imposed by a source construct on an expression, declared symbol, or resolver-local pending type variable, either concrete such as arithmetic requiring a number or abstract such as ordering requiring both sides to share an orderable type. Conflicting restrictions produce a semantic diagnostic with the contributing source spans.
_Avoid_: Runtime cast, parser type rule, hint

**Inferencia Bidirecional Simples**:
The semantic resolution pass where expected types from enclosing constructs and inferred types from child expressions constrain each other until operators, symbols, and function bindings become deterministic, without performing runtime trial resolution or speculative execution.
_Avoid_: Runtime overload resolution, global type inference, parser hint recovery

**Funcao Pura**:
A function whose call has no observable side effects and returns the same result for the same arguments within the same Ambiente de Expressao.
_Avoid_: Safe function, utility function

**Assinatura de Funcao**:
The language-level identity of a callable function, made from its name, arity, and parameter types; return type, parameter nullability, and Java reflection details do not disambiguate calls. Function calls do not accept null arguments.
_Avoid_: Java method signature, method handle identity

**Vinculo de Funcao**:
The semantic choice of which registered function signature a function call means in one Ambiente de Expressao, including whether an unknown-value boundary check remains for a single non-ambiguous signature.
_Avoid_: Runtime overload resolution, method invocation, reflective call

**Valor Semantico Preparado**:
A value produced during semantic resolution for a source-faithful AST node, such as a temporal literal normalized through the Ambiente de Expressao time zone or a compiled regular expression, and consumed later without reinterpreting the source. Temporal prepared values may retain whether an offset was explicit or inferred from the environment zone, the effective offset, and the normalized value used by execution.
_Avoid_: Mutated literal, planner recomputation, runtime parse artifact

**Funcao Dobravel**:
A Funcao Pura that may be evaluated during compilation when all arguments are compile-time constants.
_Avoid_: Constant function, cached function

**Funcao Embutida**:
An official function provided by the expression language through the Ambiente de Expressao; built-in function groups are expected to be registered as complete, validated sets.
_Avoid_: Helper method, default Java function

**Provedor de Funcoes**:
A Java class or object instance whose directly declared public methods are intentionally exposed for import into an Ambiente de Expressao as callable functions.
_Avoid_: Utility class, reflected class, function container

**Importador de Funcoes Refletidas**:
A setup-time tool that turns eligible methods from a Provedor de Funcoes into function descriptors by inferring expression types, adapting invocation handles, applying explicit renames, and validating duplicate signatures before catalog registration.
_Avoid_: Runtime reflection, dependency injection container, automatic scanner

**Funcao de Assercao**:
A built-in `as*` function that locally asserts or converts a value to a requested expression type, consuming an unknown argument explicitly without enabling implicit conversion for surrounding operators or changing the argument symbol globally.
_Avoid_: Global cast rule, type hint syntax

**Coercao de Borda**:
A type conversion allowed only at explicit boundaries, such as external values, declared defaults, explicit assertion functions, unknown-value function binding, or API result conversion, without silently changing the meaning of concrete internal expression operations; its configured profile is part of the Ambiente de Expressao identity.
_Avoid_: Implicit cast, dynamic conversion, overload priority rule

**Compatibilidade de Operador**:
The semantic requirement that an operator's operands already have acceptable expression types without applying boundary coercion between concrete internal values.
_Avoid_: Operator casting, parser precedence rule, runtime conversion

**Pertencimento**:
The typed meaning of `in` and `not in`: membership in a vector or collection by compatible element value, membership in a map by textual key, or a runtime-deferred membership check for an unknown right-hand side constrained as a membership container. Text containment is not part of this operator.
_Avoid_: Substring search, generic contains call, dynamic inclusion

**Arquivo de Expressao**:
A complete expression source made of zero or more assignments followed by an optional result expression; syntactically valid empty files are represented and rejected later by semantic validation.
_Avoid_: Program, script, parse result

**Condicional de Expressao**:
A language construct that selects the first matching branch and has one semantic meaning whether written in classic `if then else endif` form or functional `if(...)` form.
_Avoid_: If function, separate conditional languages

**Coalescencia Nula**:
A lazy left-to-right expression that returns the first non-null operand from a `??` chain.
_Avoid_: Default operator, null fallback function

**Politica de Avaliacao**:
The semantic evaluation contract attached to an operator or construct, such as short-circuit evaluation for `and`, `or`, and null coalescence, or eager evaluation for boolean operators that must evaluate both operands before producing a result.
_Avoid_: Runtime optimization, parser associativity, incidental evaluation order

**Cadeia de Navegacao**:
A source-ordered access path from an expression receiver through property, method, subscript, filter, wildcard, or collection-operation links; safe navigation belongs to the individual link that declares it.
_Avoid_: Nested getter calls, path string, reflection chain

**Navegacao Segura**:
The per-link navigation behavior that returns null only when that link's receiver is null, without hiding invalid members, invalid subscripts, or predicate errors.
_Avoid_: Null-safe chain, error suppression, optional property access

**Vinculo de Navegacao**:
The semantic resolution of one navigation link against the receiver type, either to a known object member, explicit map key subscript, collection operation, subscript behavior, or a runtime-deferred unknown receiver check. Property navigation does not access map keys.
_Avoid_: Reflection lookup, path segment, dynamic property access

**Curinga de Navegacao**:
The navigation link that expands child values from a receiver: `[*]` expands vector or collection elements, while `.*` expands map values or explicitly exposed object child values. It produces a collection of values and does not preserve map keys unless a collection operation explicitly requests keys.
_Avoid_: Recursive search, implicit reflection over all members, map entry wildcard

**Operacao de Colecao**:
A navigation operation invoked with collection-operation syntax on a receiver value, with semantics for receiver type, arguments, optional Item Atual usage, materialization, and future pipeline optimization.
_Avoid_: Global function, Java collection method, stream operation

**Catalogo de Operacoes de Colecao**:
The Ambiente de Expressao catalog that declares built-in and user-provided Operacao de Colecao descriptors separately from global functions.
_Avoid_: FunctionCatalog convention, method registry, stream extension list

**Item Atual**:
The contextual value referenced by `@` inside filters and lambdas, typed from the current collection element when that element type is known; parsing can recognize it anywhere, but semantic validation decides whether a current item context exists.
_Avoid_: At variable, implicit identifier, lambda parameter name

**Profundidade de Item Atual**:
The nesting depth of filters and lambdas that introduce an Item Atual; the Ambiente de Expressao can limit this depth as a guard rail for semantic resolution and execution frame layout.
_Avoid_: Filter depth, lambda count

**Layout de Frame**:
The stable slot arrangement selected during semantic resolution for internal symbols, external symbols, and Item Atual depths so a Plano Imutavel can execute without name lookup.
_Avoid_: Variable map, runtime scope, symbol table order

**Valor Temporal Corrente**:
A dynamic expression value such as `currDate`, `currTime`, or `currDateTime` that is derived from the execution clock, not a constant literal captured during AST construction.
_Avoid_: Date literal, external symbol, compile-time constant

**Nome Reservado**:
A source identifier reserved by the expression language for built-in simple-name values such as current temporal values; it cannot be declared as an external symbol or assigned as an internal symbol.
_Avoid_: Built-in function name, Java keyword

**Tag de Cobertura**:
A controlled marker that identifies which language constructs an expression case covers.
_Avoid_: Free-form label, test category

**Resultado de Parsing**:
The ephemeral outcome of parsing an expression source during compilation: either a valid internal parse tree with parser metadata, or one or more positioned expression diagnostics and no valid tree.
_Avoid_: Parse exception, partial parse tree

**Arvore Semantica de Expressao**:
A source-faithful, immutable expression tree that carries materialized literals, explicit source grouping, and unified syntax synonyms, but no type resolution, planning rewrites, or execution strategy.
_Avoid_: Parse tree, canonical plan, typed AST

**Modelo Semantico**:
The complete semantic interpretation of an Arvore Semantica de Expressao for one Ambiente de Expressao, including resolved types, symbols, function bindings, contextual diagnostics, and runtime-deferred decisions for unknown values.
_Avoid_: Typed AST, partial resolver result, execution plan

**Resultado de Resolucao Semantica**:
The outcome of resolving an Arvore Semantica de Expressao in one Ambiente de Expressao, containing all semantic diagnostics and a planejable Modelo Semantico only when no semantic errors were found.
_Avoid_: Semantic exception, partial semantic model, planner input with errors

**Checagem Diferida**:
A runtime validation selected during semantic resolution when a source construct has known types but a value precondition cannot be proven at compilation time, such as dynamic factorial bounds, root degree constraints, subscript bounds, receiver null checks for non-safe navigation, or materialization limits. The execution plan consumes these checks without rediscovering semantic rules.
_Avoid_: Late semantic resolution, runtime overload choice, generic cast, unknown type handling

**Identificador de No**:
A deterministic, compilation-local identity assigned to each node or navigation link of an Arvore Semantica de Expressao in stable pre-order traversal.
_Avoid_: UUID, source span key, persistent node id

**Igualdade Estrutural de Arvore**:
A comparison of expression tree shape, preserved source-form fields, operators, literals, and children that intentionally ignores node identities and source spans.
_Avoid_: Record equality, source-text equality, semantic equivalence

**Trecho de Fonte**:
A half-open character range in an expression source, with zero-based offsets for machines and one-based line and column numbers for human-facing diagnostics. Empty ranges identify insertion points such as missing tokens or end of source.
_Avoid_: Token position, raw ANTLR location

**Diagnostico de Expressao**:
A stable, categorized and severity-marked explanation of why an expression source cannot be accepted or should be warned about in a compilation phase, always tied to a primary source span when it originates from source text and identified by a testable diagnostic code. It may include related spans or notes for multi-cause diagnostics. Error diagnostics block a planejable semantic model; warning diagnostics do not.
_Avoid_: Exception message, ANTLR error text

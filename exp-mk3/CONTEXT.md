# Expression Evaluator MK3

This context defines the language used around expression compilation and evaluation in the `exp-mk3` module.

## Language

**Caso de Expressao**:
A versioned example that describes an expression, its environment, inputs, expected result or expected diagnostic, and language coverage tags.
_Avoid_: Test fixture, corpus item

**Corpus de Expressoes**:
A versioned collection of expression cases used as the shared behavioral contract for parser, semantic resolver, runtime, migration, and differential verification.
_Avoid_: Test data, examples folder

**Resultado de Compilacao**:
The closed public outcome of compiling one expression source, containing either a reusable compiled expression plus warnings or the complete compilation diagnostics without an executable plan.
_Avoid_: Nullable compiled expression, compilation exception as the primary result

**Plano Imutavel**:
A compiled, thread-safe, reusable representation of an expression that is executed with an isolated scope for each call.
_Avoid_: Compiled tree, executable expression internals

**Visao de Expressao**:
A validated public projection over one Plano Imutavel that defines which result is executed and exposed, such as a general result, number, boolean, or final assignment map, without recompiling the source.
_Avoid_: Compilation mode, result cast, independent compiled plan

**Resultado Esperado**:
The value or diagnostic that an expression case declares as the correct behavior.
_Avoid_: Assertion payload, expected output

**Ambiente de Expressao**:
The compilation configuration that declares external symbols, functions, Java types, decimal numeric semantics, time zone, and limits used to interpret an expression case.
_Avoid_: Test context, evaluation setup

**Identificador de Instancia do Ambiente**:
An opaque UUID string generated when an Ambiente de Expressao is built and used to share compiled plans only while that same environment instance is reused. Separately built environments have different identifiers even when their configurations are equal.
_Avoid_: Environment content hash, deterministic environment ID, semantic fingerprint, persistent environment ID

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
An assignment target that binds multiple internal symbols from the prefix of an ordered collection, ignoring surplus elements and checking that the source has at least as many elements as targets.
_Avoid_: Tuple unpacking syntax, multiple assignment statement, array pattern

**Valor Padrao de Simbolo**:
The required value declared with every Simbolo Externo and validated as part of the Ambiente de Expressao. It is the effective value for non-overridable symbols and the fallback value for overridable symbols when no runtime override is supplied.
_Avoid_: Optional runtime fallback, missing input handler

**Politica de Sobrescrita de Simbolo**:
The Ambiente de Expressao declaration that says whether a Simbolo Externo default can be replaced by runtime input. Non-overridable external symbols are fixed environment values; overridable external symbols accept boundary-coerced runtime replacements.
_Avoid_: Mutable parameter, assignment permission, runtime redeclaration

**Fato Numerico**:
Semantic facts about a numeric expression under the language's decimal numeric semantics, such as integral shape or exact reduced-rational parity relevant to a real-domain operation, used by validation and later planning without changing the public expression type.
_Avoid_: Numeric mode, public number type, Java primitive type, optimization hint

**Dominio Numerico Real**:
The numeric contract that classifies mathematically defined decimal operations with real results as admissible and rejects complex or undefined results, interpreting every finite decimal as an exact rational when classifying exponentiation and roots. Admissible operations may still fail under representation or configured resource limits.
_Avoid_: Floating-point domain, complex-number fallback, library-defined domain

**Tipo Colecao**:
The single sequential container type of the expression language, preserving ordered values of one known element type and supporting indexing, slicing, filtering, wildcard expansion, and assignment destructuring.
_Avoid_: Vector, Java collection implementation, raw iterable

**Limite de Materializacao**:
An Ambiente de Expressao guard rail that bounds every container snapshot materialized at an external boundary or by the language, including collection literals, maps, operation results, function-provider results, and public results.
_Avoid_: Parser size limit, result-only collection limit

**Materializacao Publica**:
The type-directed creation of a bounded immutable snapshot when an expression result or assignment map crosses the public API boundary, recursively excluding null and non-exposable object values.
_Avoid_: Raw Java result, unchecked collection cast, result-only coercion

**Tipo Mapa**:
The type of a text-keyed value map understood by the expression language, preserving the type of values reachable by textual keys.
_Avoid_: Generic Java map, object property bag

**Entrada de Mapa**:
The contextual item type exposed by filtering or lambda-processing a Tipo Mapa, with `@.k` as the textual key and `@.v` as the map value. It is available only inside map Item Atual contexts and is not a general source-level value type.
_Avoid_: Map object, structural pair, Java Map.Entry API

**Tipo Objeto**:
A nominal domain object type whose registered members can be used for navigation without making object compatibility structural.
_Avoid_: Structural record type, map-like object

**Tipo Java Registrado**:
A Java-backed Tipo Objeto declared by the Ambiente de Expressao for nominal use in external values, function signatures, and navigation. Its navigable members follow a chosen exposure policy, from property accessors to optionally all public methods.
_Avoid_: Reflected class, automatic object shape

**Valor Nulo de Runtime**:
A runtime absence value introduced by safe navigation, whether from a null receiver or from an absent element or map key tolerated by that link. It is not a source literal, cannot be supplied as an external symbol override, and does not have a normal expression type; Java nulls from external data, members, functions, maps, or collections are boundary/runtime contract violations rather than normal expression values. Null coalescence is the explicit way to discharge possible runtime null before a value reaches a non-null context.
_Avoid_: Null literal, bottom type, unknown value

**Nulidade de Runtime**:
Semantic metadata that records whether an expression or binding is proven never to produce a runtime null value or may produce one. It is not a source-level type and does not participate in ordinary type unification, but constructs whose contract rejects runtime null may use it to issue semantic diagnostics.
_Avoid_: Nullable type, optional type, bottom type

**Tipagem Conhecida**:
The semantic requirement that every accepted expression node, symbol, function binding, navigation binding, and collection operation has a known expression type at compilation time. Missing Java metadata, unconstrained empty containers, or ambiguous function contracts are semantic errors rather than unknown types deferred to runtime.
_Avoid_: Dynamic type, any type, unknown semantic type

**Variavel de Tipo Pendente**:
An internal resolver-only placeholder used while inferring a known type from local context, such as an empty collection literal receiving its element type from a sibling branch, function parameter, or membership operand. Every pending type variable must resolve to a known type or produce a semantic diagnostic before a Modelo Semantico can succeed.
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
The semantic choice of which registered function signature a function call means in one Ambiente de Expressao. Function binding matches known argument types to explicit registered signatures and does not apply boundary coercion between ordinary expression values.
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
A Java class or object instance whose directly declared public methods are intentionally exposed for import into an Ambiente de Expressao as callable functions. A supplied instance remains bound to that environment and is responsible for honoring its declared purity, non-null, lifetime, and concurrency contracts.
_Avoid_: Utility class, reflected class, function container

**Importador de Funcoes Refletidas**:
A setup-time declaration resolved atomically while building an Ambiente de Expressao, turning eligible provider methods into function descriptors against its registered Java types and boundary-coercion profile. It supports explicit selection, renaming, and custom-function replacement while rejecting incompatible methods and duplicate signatures before catalog registration.
_Avoid_: Runtime reflection, dependency injection container, automatic scanner

**Funcao de Assercao**:
A built-in `as*` function that locally validates or converts a value from an explicit known source contract to a known target expression type, without enabling implicit conversion for surrounding operators or changing the argument symbol globally.
_Avoid_: Global cast rule, type hint syntax, unknown argument type

**Coercao de Borda**:
A type conversion allowed only at explicit boundaries, such as external values, declared defaults, Java function-provider arguments and results, explicit assertion functions, or API result conversion, without silently changing the meaning of concrete internal expression operations. Its configured profile supports setup-time compatibility validation and audit, but neither participates in function overload resolution nor forms part of the instance identifier.
_Avoid_: Implicit cast, dynamic conversion, overload priority rule

**Compatibilidade de Operador**:
The semantic requirement that an operator's operands already have acceptable expression types without applying boundary coercion between concrete internal values.
_Avoid_: Operator casting, parser precedence rule, runtime conversion

**Pertencimento**:
The typed meaning of `in` and `not in`: membership in a collection by compatible element value or membership in a map by textual key. Text containment is not part of this operator.
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
A source-ordered access path from an expression receiver through property, unresolved call, subscript, filter, or wildcard links; safe navigation belongs to the individual link that declares it.
_Avoid_: Nested getter calls, path string, reflection chain

**Navegacao Segura**:
The per-link navigation behavior that tolerates a null receiver and legitimate absence at that link, returning null instead of failing, without hiding an unsupported receiver, a failing accessor, a predicate error, or an exceeded materialization limit. It does not propagate along the chain: a safe link makes its result possibly null, so every following link must declare itself safe or the possible null must be discharged before it.
_Avoid_: Null-safe chain, error suppression, optional property access, whole-chain short circuit

**Subscrito**:
The bracketed navigation link family made of index, slice, textual key, wildcard, and filter forms, whose index, slice-bound, and key payloads are source literals rather than computed values. Index and textual key name one element: a strict link fails when it is absent and a safe link yields null, while a slice clamps its bounds to the receiver on either link form and may yield an empty collection.
_Avoid_: Dynamic index, computed key, array access operator

**Vinculo de Navegacao**:
The semantic resolution of one navigation link against a known receiver type, selecting a registered object member, explicit map key subscript, collection operation, or typed subscript behavior before planning. Property navigation does not access map keys.
_Avoid_: Reflection lookup, path segment, dynamic property access, runtime member choice

**Curinga de Navegacao**:
The `[*]` navigation link that produces a collection from collection elements, map values, or an explicitly registered homogeneous set of object child members. It does not preserve map keys or reflect arbitrary public members.
_Avoid_: `.*`, recursive search, implicit reflection over all members, map entry wildcard

**Operacao de Colecao**:
A receiver operation invoked with ordinary `.` or safe `?.` call syntax and resolved from the receiver's collection or map type, with explicit contracts for arguments, Item Atual usage, evaluation, and materialization.
_Avoid_: Global function, Java collection method, stream operation

**Catalogo de Operacoes de Colecao**:
The Ambiente de Expressao catalog that declares official Operacao de Colecao descriptors separately from global functions, retaining only an internal seam for future extensions in the initial version.
_Avoid_: FunctionCatalog convention, method registry, stream extension list

**Descritor de Operacao de Colecao**:
The declarative catalog contract that defines accepted receivers, an ordered list of value or lambda argument contracts, Item Atual type derivation, result rule, intrinsic purity, evaluation policy, and materialization policy for an Operacao de Colecao. It contains no runtime execution and is not the semantic binding of a source occurrence.
_Avoid_: Collection function, operation handler, runtime implementation, collection-operation binding

**Item Atual**:
The contextual value referenced by `@` inside filters and lambdas, typed from the current collection element when that element type is known; parsing can recognize it anywhere, but semantic validation decides whether a current item context exists.
_Avoid_: At variable, implicit identifier, lambda parameter name

**Item de Reducao**:
The contextual Item Atual available only inside a `reduce` lambda, exposing the current accumulator as `@.accumulator` and the current collection element as `@.item` without becoming a registrable object type.
_Avoid_: Tuple, map entry, first-class lambda parameter

**Profundidade de Item Atual**:
The nesting depth of filters and lambdas that introduce an Item Atual; the Ambiente de Expressao can limit this depth as a guard rail for semantic resolution and execution frame layout.
_Avoid_: Filter depth, lambda count

**Layout de Frame**:
The stable slot arrangement selected during semantic resolution for internal symbols, external symbols, and Item Atual depths so a Plano Imutavel can execute without name lookup.
_Avoid_: Variable map, runtime scope, symbol table order

**Escopo de Execucao**:
The isolated per-call state used to execute a Plano Imutavel, containing its frame, one coherent current-time snapshot when needed, and no mutable state shared with another execution.
_Avoid_: Compiled expression state, variable map, pooled session

**Valor Temporal Corrente**:
A dynamic expression value such as `currDate`, `currTime`, or `currDateTime` that is derived from one execution-clock instant truncated to whole seconds, not a constant literal captured during AST construction.
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
The complete semantic interpretation of an Arvore Semantica de Expressao for one Ambiente de Expressao, including known types, resolved bindings, evaluation policies, prepared values, value preconditions, diagnostics, and frame layout required for planning.
_Avoid_: Typed AST, partial resolver result, execution plan

**Resultado de Resolucao Semantica**:
The outcome of resolving an Arvore Semantica de Expressao in one Ambiente de Expressao, containing all semantic diagnostics and a planejable Modelo Semantico only when no semantic errors were found.
_Avoid_: Semantic exception, partial semantic model, planner input with errors

**Checagem Diferida**:
A runtime validation selected during semantic resolution when a source construct has known types but a value precondition cannot be proven at compilation time, such as a dynamic real-number domain, factorial bounds, subscript bounds, or materialization limits. The execution plan consumes these checks without rediscovering semantic rules.
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
A stable, categorized and severity-marked explanation of a compilation warning/error or execution failure, identified by a testable code and tied to a primary source span whenever the failure originates in source text. It may include related spans, notes, or a suggestion; compilation errors block a planejable semantic model while warnings do not.
_Avoid_: Exception message, ANTLR error text

**Oraculo Sem Otimizacoes**:
The form of a Plano Imutavel that applies no optimizing transformation, built by the same pipeline as the optimized form and selectable only inside the module. It is the reference semantics against which every optimized plan is proven equivalent in value, scale, rounding, domain, failure, observable order, and observable effects.
_Avoid_: Interpreter mode, debug plan, public execution mode, legacy plan

**Dobra de Constante**:
The replacement of a pure subexpression whose operands are all known during compilation by its already computed value. A subexpression that fails while being folded is left unfolded so that it fails during execution exactly as the Oraculo Sem Otimizacoes would.
_Avoid_: Compile-time error for failing constants, poisoned constant, precomputed cache

**Leitura Dobrada**:
The record kept in a Plano Imutavel of each symbol read that became a constant during Dobra de Constante, carrying the symbol name, its Identificador de No, its Trecho de Fonte, and the folded value. It exists so that auditing can explain a value that no longer appears as a read during execution.
_Avoid_: Execution trace entry, variable snapshot, audit event

**Elisao de Assercao**:
The removal of an assertion function call whose asserted type is exactly the argument's already proven type, turning the call into no operation because the underlying boundary conversion returns the value itself in that case.
_Avoid_: Type cast removal, unchecked coercion, silent conversion

# Decisoes de Planejamento da Etapa 4 - Resolver Semantico

Este documento consolida as decisoes tomadas durante a sessao de planejamento da Etapa 4 do `exp-mk3`. Ele registra o estado atual das decisoes, ja considerando revisoes que substituem decisoes anteriores da conversa.

## Escopo da Etapa 4

- O `SemanticResolver` e responsavel por decidir o significado semantico da expressao; etapas posteriores executam esse significado.
- O `SemanticResolver` produz um `Modelo Semantico` interno e planejavel apenas quando nao houver diagnosticos de erro.
- O resolver deve acumular todos os problemas independentes em uma unica execucao, evitando parar no primeiro erro.
- O resolver fica em pacote interno, por exemplo `com.runestone.expeval_mk3.internal.semantics`.
- A API publica do modulo nao deve expor o `SemanticResolver` nem o `Modelo Semantico` nesta etapa.
- A API interna principal esperada e `resolve(ExpressionFileNode ast, ExpressionEnvironment environment)`.
- A Etapa 4 decide tipos, simbolos, funcoes, navegacao, operacoes de colecao, layout de frame, nulidade de runtime, valores semanticos preparados e checagens diferidas.
- A Etapa 4 nao executa constant folding, CSE, reordenacao, elisao de `as*`, nem outras otimizacoes de plano.
- A Etapa 4 nao deve virar um runtime parcial.

## ADRs e Pre-Trabalho Obrigatorio

- ADR 0007 define que literais inteiros da linguagem sao apenas decimais.
- ADR 0008 define que simbolos externos exigem valor padrao e politica de sobrescrita.
- ADR 0009 define que a linguagem fonte nao tem literal `null`.
- ADR 0010 define que o modelo semantico aceito exige tipos conhecidos.
- ADR 0011 remove `strict mode` do `Ambiente de Expressao`.
- As ADRs 0007 e 0008 sao pre-trabalho bloqueante para implementar a Etapa 4.
- O plano macro deve ser ajustado para remover referencias obsoletas a `UnknownType`, `NullType`, simbolos externos sem default, `strict mode`, e politica de hex/octal em subscripts.
- A Etapa 4 deve ter um documento detalhado proprio em `exp-mk3/docs/planning/etapa-4-resolver-semantico.md` quando a sessao de planejamento for encerrada.

## Gramática e Literais

- Hexadecimal e octal devem ser removidos da gramatica como um todo.
- `0x10`, `077` e formas similares nao sao `INT` valido em nenhuma posicao da fonte.
- A Etapa 4 nao deve conter politica especial de rejeicao de hex/octal em subscripts, porque essas formas nao chegam como AST valida.
- Diagnosticos didaticos para hex/octal antigos pertencem ao parser/migracao, nao ao resolver semantico.
- O literal fonte `null` deve ser removido da gramatica.
- Fonte como `null`, `[null]`, `x = null` e `asNumber(null)` deve falhar antes ou durante migracao, nao como caso semantico normal.
- Literais `DATETIME` com e sem offset sao interpretados pela politica temporal do ambiente.
- Literais `DATETIME` sem offset sao horarios locais no `ZoneId` do ambiente, com offset efetivo inferido pelas `ZoneRules`.
- Literais `DATETIME` com offset explicito sao convertidos para o `ZoneId` do ambiente antes de virar valor semantico preparado.

## Tipagem Conhecida

- O `Modelo Semantico` de sucesso nao contem `UnknownType`.
- Todo no de expressao aceito deve ter tipo conhecido em compilacao.
- Toda funcao registrada deve ter retorno conhecido.
- Todo metodo/propriedade Java registrado deve ter tipo de retorno conhecido.
- Toda operacao de colecao deve ter retorno conhecido ou computavel pelos argumentos.
- Navegacao sem metadata suficiente e erro semantico, nao vinculo diferido por tipo desconhecido.
- Resultado final de expressao com tipo indeterminado e erro.
- `Tipo Invalido` existe apenas internamente no fluxo de falha para suprimir cascata de diagnosticos.
- `Tipo Invalido` nunca aparece em `SemanticResolutionSuccess`.
- `Variavel de Tipo Pendente` pode existir apenas durante a resolucao para inferencia contextual local.
- Toda `Variavel de Tipo Pendente` deve resolver para tipo conhecido ou gerar diagnostico antes de sucesso.
- Exemplos de uso de `Variavel de Tipo Pendente`: vetor vazio em `??`, condicional, membership ou parametro de funcao.

## Null e Nulidade de Runtime

- A fonte nao tem literal `null`.
- `Valor Nulo de Runtime` pode vir de dados Java, overrides externos, navegacao, metodos Java, mapas, colecoes ou navegacao segura.
- `Valor Nulo de Runtime` nao e tipo de expressao.
- `Nulidade de Runtime` e metadata nao bloqueante, nao um tipo nullable.
- `RuntimeNullability.NEVER_NULL` indica que o no e provado como nunca nulo se executar com sucesso.
- `RuntimeNullability.MAY_BE_NULL` indica que o valor pode ser nulo em runtime.
- A metadata de nulidade apoia auditoria, diagnosticos e possiveis warnings futuros, mas nao participa da compatibilidade comum de tipos.
- `??` e `?.` sao os constructs explicitos para protecao contra null de runtime.
- `??` aceita operandos de tipos estaticos unificaveis e retorna o primeiro valor runtime nao nulo.
- `??` nao converte tipo.
- `??` nao exige que o operando esquerdo seja marcado como `MAY_BE_NULL`.
- `1 ?? 2` e semanticamente valido, sem warning na Etapa 4.
- Se todos os operandos de `??` forem `MAY_BE_NULL`, o resultado tambem e `MAY_BE_NULL`.
- Se uma cadeia `??` tem algum fallback `NEVER_NULL`, o resultado pode ser `NEVER_NULL`.
- `x = null` nao existe na linguagem.
- Nao deve haver teste explicito de null na v2, como `isNull(x)`, nesta etapa.
- Funcoes nao aceitam argumento null.
- Nao deve haver `acceptsNull=true` em descriptor de parametro de funcao.
- Metodos de objeto e operacoes de colecao tambem nao aceitam argumentos explicitamente nulos.
- Built-ins `as*` tambem rejeitam null como qualquer funcao.
- Constructs da linguagem como `??`, `if`, igualdade entre tipos compativeis e navegacao segura nao sao tratados como funcoes para essa regra.
- Operadores comuns podem receber um valor null em runtime e falhar com diagnostico runtime.
- O usuario protege esses casos com fallback explicito, por exemplo `(customer?.age ?? 0) + 1`.

## Ambiente de Expressao e Simbolos Externos

- Todo `Simbolo Externo` exige valor padrao nao nulo.
- Todo `Simbolo Externo` exige politica de sobrescrita.
- O tipo do simbolo externo e declarado e validado contra o default, ou inferido do default.
- Declaracao externa apenas com tipo nao e permitida.
- Declaracao externa apenas com nome nao e permitida.
- Default null nao e permitido.
- Override runtime null e permitido para simbolos sobrescreviveis.
- Simbolo externo `overridable=false` e valor fixo do ambiente.
- Se runtime input tenta sobrescrever simbolo `overridable=false`, isso e erro de entrada/runtime, nao deve ser ignorado.
- Simbolos fixos podem ser dobrados em Etapa 7 se o valor for constante e a expressao for pura.
- Simbolos sobrescreviveis nunca sao constantes dobraveis apenas por terem default.
- Na Etapa 4, todo simbolo externo usado recebe slot no `Layout de Frame`, mesmo se fixo.
- Etapa 7 pode elidir leitura de simbolo fixo dobrado e registrar `foldedVariableReads`.
- Simbolo externo declarado mas nao usado nao cria slot no frame.
- Simbolo externo declarado mas nao usado nao gera warning por padrao.
- `ExpressionEnvironmentId` deve incluir nome, tipo declarado ou inferido, default canonico e `overridable` de cada simbolo externo.
- Ambientes que diferem apenas em `overridable` nao podem compartilhar plano.
- Defaults externos heterogeneos de mapa ou colecao sem tipo declarado devem ser rejeitados.
- Default externo de colecao/mapa vazio exige tipo declarado.
- Default externo Java `List`, array ou `Iterable` entra como `Tipo Colecao<T>` por padrao, nao `Tipo Vetor<T>`.
- Externo so e `Tipo Vetor<T>` se o ambiente declarar explicitamente `VectorType<T>`.

## Simbolos Internos e Atribuicoes

- Resolucao de atribuicoes e sequencial.
- Nao ha forward reference para simbolo interno declarado depois.
- A LHS de uma atribuicao so introduz o simbolo depois de resolver a RHS daquela atribuicao.
- `x := x + 1` sem `x` externo ou interno anterior e erro de simbolo desconhecido.
- Se existe `x` externo, `x := x + 1` le o externo na RHS e cria/sombreia um interno na LHS.
- Depois da atribuicao que cria o interno `x`, usos posteriores de `x` resolvem para o simbolo interno.
- Externo `x` e interno `x` tem slots distintos e identidade distinta.
- Sombreamento externo por interno e permitido com warning no target.
- Reatribuicao sequencial de simbolo interno e permitida.
- O mesmo simbolo interno usa um unico slot, atualizado em ordem.
- O tipo de simbolo interno deve permanecer estavel/unificavel ao longo das atribuicoes.
- `x := 1; x := "s"` e erro semantico.
- `x := []; x := [1]` pode refinar o vetor vazio para `Vetor<NUMBER>`.
- Usos intermediarios tambem participam das restricoes do simbolo interno.
- Atribuicao cujo RHS e `Tipo Invalido` faz o simbolo interno propagar `Tipo Invalido` no fluxo de falha sem cascata.
- Programa com atribuicoes e sem expressao de resultado e semanticamente valido.
- Arquivo sem atribuicoes e sem expressao de resultado e erro semantico de arquivo vazio.
- Span recomendado para arquivo vazio: offset `0`, linha `1`, coluna `1`, comprimento `0`.

## Desestruturacao

- Desestruturacao atual e apenas plana, conforme gramatica e AST.
- Nao projetar suporte a desestruturacao aninhada agora.
- Cada identificador folha da desestruturacao vira um `Simbolo Interno` proprio com slot proprio.
- A ordem de slots da desestruturacao segue a ordem textual esquerda-para-direita.
- Atribuicao de desestruturacao nao cria slot para a tupla/vetor inteiro, salvo temporario de execucao se necessario.
- Aridade conhecida de vetor literal deve ser validada em compile-time.
- `[a, b] := [1, 2]` e valido.
- `[a, b] := [1]` e erro semantico.
- `[a, b] := []` e erro semantico de aridade conhecida incompativel.
- Fonte desestruturavel com aridade desconhecida pode exigir checagem runtime de aridade se o tipo for conhecido como colecao/vetor mas forma nao for conhecida.
- Nomes duplicados no mesmo target, como `[a, a]`, sao erro semantico.
- O diagnostico de duplicidade deve apontar para a segunda ocorrencia e relacionar a primeira.

## Layout de Frame

- O `Layout de Frame` e definido durante a resolucao semantica.
- A ordem deve ser canonica e independente de `HashMap`.
- Simbolos externos usados entram em ordem de primeira referencia na AST.
- Simbolos internos entram em ordem da primeira atribuicao que cria cada simbolo.
- Slots de `Item Atual` entram por profundidade usada.
- Slots sinteticos ficam para Etapa 7, depois dos simbolos declarados.
- O frame reserva slots apenas para simbolos externos efetivamente usados.
- Slots de `Item Atual` sao reservados ate a profundidade maxima realmente usada, nao ate o limite configurado.
- Desestruturacao atribui slots por simbolo alvo individual.

## Diagnosticos

- `Diagnostico de Expressao` tem categoria, codigo estavel, severidade, span primario e pode ter spans relacionados/notas.
- Severidades iniciais: `ERROR` e `WARNING`.
- Apenas `ERROR` bloqueia `Modelo Semantico` planejavel.
- `WARNING` nao bloqueia planejamento nem execucao.
- Sombreamento de simbolo externo por interno gera warning.
- Eliminacao de atribuicao morta na Etapa 7 pode gerar warning sem bloquear.
- `SemanticResolutionSuccess` contem modelo e diagnosticos, podendo incluir warnings.
- `SemanticResolutionFailure` contem diagnosticos quando existe pelo menos um erro.
- Evitar resultado de sucesso com erros.
- Ordenacao de diagnosticos: `SourceSpan.offset`, depois severidade, depois categoria/codigo estavel.
- Diagnosticos multi-causa usam span primario para ordenacao e spans relacionados para causas secundarias.
- Conflitos de restricoes devem apontar para o uso que torna o conflito evidente e relacionar o uso anterior.
- Erros independentes devem ser emitidos mesmo que outro ramo da expressao tenha erro.
- Cascatas devem ser suprimidas quando qualquer operando ou simbolo envolvido ja tem `Tipo Invalido`.
- Parser, resolver e runtime devem usar continuidade conceitual de diagnosticos com categorias distintas, como `PARSE`, `SEMANTIC`, `RUNTIME` e possivelmente `MIGRATION`.

## Resultado de Resolucao e SemanticModel

- `SemanticModel` deve preservar a AST imutavel e source-faithful.
- Anotacoes semanticas devem ficar em mapas por `NodeId`, nao em wrappers mutaveis de AST.
- Campos esperados incluem AST, tipos resolvidos, bindings de simbolo, bindings de funcao, bindings de navegacao, categorias numericas, nulidade de runtime, valores preparados, checagens diferidas e layout de frame.
- Todo `ExpressionNode` em `SemanticResolutionSuccess` deve ter entrada em `resolvedTypes`.
- Elementos de AST que produzem valor, incluindo links de navegacao com `NodeId`, devem ter tipo resolvido ou tipo resultante registrado.
- `navigationBindings` deve ser indexado pelo `NodeId` do link de navegacao.
- `functionBindings` deve ser indexado pelo `NodeId` da chamada.
- `symbolBindings` deve ser indexado pelo `NodeId` da referencia/target relevante.
- `Tipo Invalido` e placeholders de inferencia interna nao aparecem em mapas do modelo de sucesso.

## Checagens Diferidas

- `Checagem Diferida` existe apenas para pre-condicoes de valor runtime em constructs ja tipados.
- Checagens diferidas nao podem representar escolha de tipo, overload runtime ou navegacao sobre tipo desconhecido.
- Exemplos validos: fatorial integral nao negativo, grau de `root` integral positivo, bounds de subscript, receiver null em navegacao nao-safe, limites de materializacao.
- O `ExecutionPlanBuilder` consome checagens diferidas sem redescobrir regras semanticas.
- Com `strict mode` removido, nao ha politica de rejeicao especial de checagens diferidas por modo estrito.

## Operadores Numericos

- Operadores aritmeticos comuns exigem `NUMBER` e retornam `NUMBER`.
- Operadores aritmeticos nao aceitam valores temporais.
- Operacoes temporais devem ser funcoes built-in explicitas.
- Divisao em modo `DECIMAL` e permitida e usa `MathContext` do ambiente.
- Resultado de operacao decimal exposta deve ser `BigDecimal` ou equivalente interno convertido para `BigDecimal` na borda.
- Literais inteiros pequenos podem ser preservados internamente como `long`/`BigInteger` para AST/folding.
- `root` exige operandos `NUMBER`.
- Grau de `root` deve ser integral positivo quando estaticamente conhecido.
- Grau dinamico de `root` gera checagem diferida de valor integral positivo.
- `x!` exige `NUMBER` com restricao de valor integral nao negativo.
- Fatorial constante negativa/fracionaria e erro semantico.
- Fatorial dinamico gera checagem diferida.
- Limite maximo de fatorial e guard-rail de ambiente/runtime; constante acima do limite pode ser rejeitada semanticamente.
- `%` pos-fixado exige `NUMBER` e retorna `NUMBER`.
- `%` pos-fixado nao e reescrito na Etapa 4.
- Em `DECIMAL`, `%` produz categoria numerica decimal mesmo se a entrada for integral.

## Categoria Numerica

- A Etapa 4 deve calcular `Categoria Numerica` por no numerico.
- A categoria numerica nao muda o tipo publico `NUMBER`.
- Em `DECIMAL`, literais inteiros podem ser integrais e operacoes gerais podem ser decimais.
- Em `FAST`, a categoria pode preparar `LONG`, `DOUBLE` ou `DECIMAL` conforme modo, literais, funcoes e operadores.
- A Etapa 5 pode ignorar boa parte dessas categorias e executar o baseline decimal.
- Etapa 8 consome essas categorias sem re-resolver tipos.

## Operadores Booleanos e Politica de Avaliacao

- `and`, `or`, `nand`, `nor`, `xor` e `xnor` exigem operandos `BOOLEAN` e retornam `BOOLEAN`.
- `and` e `or` tem politica de avaliacao lazy/curto-circuito.
- `nand`, `nor`, `xor` e `xnor` avaliam ambos os lados.
- A politica de avaliacao deve ficar no modelo/binding do no para orientar runtime e otimizacoes.
- Predicados de filtro devem ser `BOOLEAN`.
- Null de runtime nao e tratado como `false` em predicados.
- Usuario deve escrever fallback explicito como `@?.active ?? false`.

## Igualdade, Ordenacao e Pertencimento

- Igualdade exige tipos compativeis conhecidos.
- Tipos concretos incompativeis em igualdade geram erro semantico, nao `false` silencioso.
- Igualdade entre objetos nominais e valida apenas para o mesmo tipo nominal.
- Ordenacao aceita apenas familias homogeneas ordenaveis: `NUMBER`, `STRING`, `DATE`, `TIME`, `DATETIME`.
- `BOOLEAN`, `VECTOR`, `COLLECTION`, `MAP` e `OBJECT` nao sao ordenaveis por padrao.
- Temporais diferentes nao sao comparaveis diretamente.
- `DATE = DATETIME` e erro semantico.
- `DATE < DATE`, `TIME < TIME` e `DATETIME < DATETIME` sao validos.
- `between` exige que valor e limites unifiquem na mesma familia ordenavel.
- `x in Tipo Vetor<T>` e valido se `x` for compativel com `T`.
- `x in Tipo Colecao<T>` e valido se `x` for compativel com `T`.
- `x in Tipo Mapa<V>` testa existencia de chave textual e exige lado esquerdo `STRING`.
- `value in map` nao testa valores.
- Para valores de mapa, o usuario deve usar `value in map..values()`.
- `x in STRING` nao significa substring.
- Substring deve ser funcao explicita, se existir.
- `x in []` pode usar o tipo conhecido de `x` para tipar o vetor vazio.

## Condicionais e Coalescencia Nula

- A gramatica atual exige `else` em condicionais classicos e funcionais.
- Todas as condicoes de `if` e `elsif` devem ser `BOOLEAN`.
- Ramos de resultado de condicional devem unificar para tipo conhecido.
- Como a fonte nao tem `null`, nao ha ramo literal nulo.
- Condicionais podem usar inferencia contextual local para vetor vazio.
- `if c then [] else [1] endif` resolve como `Vetor<NUMBER>`.
- `if c then [] else [] endif` e erro por elemento indeterminado.
- `??` exige operandos de tipos estaticos unificaveis.
- `[] ?? [1]` resolve como `Vetor<NUMBER>`.
- `[] ?? []` e erro por elemento indeterminado.
- `1 ?? "x"` e erro de tipos incompativeis, mesmo que folding posterior pudesse provar alcance.

## Vetores, Colecoes e Mapas

- Vetor literal com elementos deve unificar os elementos para tipo conhecido.
- `[1, 2, 3]` resolve como `Tipo Vetor<NUMBER>`.
- `[1, "x"]` e erro semantico.
- `[null]` nao existe porque `null` nao e literal de fonte.
- `[]` e valido apenas quando contexto fornece tipo de elemento conhecido.
- `[]` isolado como expressao final e erro semantico.
- `x := []` e erro semantico se nao houver tipo esperado para `x`.
- `asVector([])` e erro se `asVector` nao define elemento.
- Vetores aninhados unificam recursivamente quando os elementos sao conhecidos.
- `Tipo Vetor` representa valores vetoriais produzidos pela linguagem ou declarados explicitamente.
- `Tipo Colecao` representa grupos externos/iteraveis e resultados de operacoes que nao prometem semantica vetorial.
- `Tipo Mapa` e text-keyed.
- Acesso a mapa por propriedade nao e permitido.
- `m["key"]` e a forma de acessar valor de mapa.
- `m.key` e invalido quando `m` e `Tipo Mapa`.
- Mapa vazio default e valido apenas com `MapType<T>` declarado.

## Funcoes Globais

- Chamada global resolve por `FunctionCatalog`.
- `Vinculo de Funcao` deve carregar descriptor, assinatura escolhida, pureza, dobrabilidade e metadados necessarios.
- Overload deve ser deterministico em compile-time.
- Se exatamente uma assinatura e viavel, ela e escolhida.
- Se mais de uma assinatura continua viavel, erro de overload ambiguo.
- Se nenhuma assinatura e viavel, erro semantico.
- Contexto de retorno pode ajudar a desambiguar overload por inferencia bidirecional simples.
- Nao ha runtime overload resolution.
- Funcoes nao aceitam argumento null.
- Coercao de borda nao se aplica implicitamente entre valores internos concretos.
- `sqrt("1")` e erro semantico.
- Coercao de borda vale para defaults externos, overrides externos, funcoes `as*` e conversao de resultado da API.
- `asNumber("1")` e borda explicita de coercao.
- Constantes em funcoes `as*` devem ser validadas semanticamente quando possivel.
- `asNumber("abc")` pode ser erro semantico se o perfil nao permite converter.
- `asNumber(x)` quando `x` ja e `NUMBER` deve ser marcado como assercao redundante para Etapa 7 elidir.
- `asVector(x)` afirma apenas vetor, mas precisa de tipo de elemento conhecido por contexto ou contrato.
- `f([])` pode tipar `[]` pelo parametro se houver assinatura unica, como `f(Vector<NUMBER>)`.
- `f([])` e ambiguo se houver overloads como `f(Vector<NUMBER>)` e `f(Vector<STRING>)`.
- `FunctionCatalog` deve validar que toda `Funcao Dobravel` e `Funcao Pura`.
- Etapa 4 nao executa funcoes dobraveis; apenas marca.
- Funcoes impuras sao semanticamente validas, mas bloqueiam folding, CSE e reordenacao posterior.

## Nomes Reservados e Namespaces

- `currDate`, `currTime` e `currDateTime` sao nomes reservados para valores temporais correntes.
- Simbolo externo com nome reservado deve ser proibido no builder do ambiente.
- Atribuicao a nome reservado deve ser proibida no resolver.
- `currDate` sem parenteses resolve como `Valor Temporal Corrente`.
- `currDate()` e chamada de funcao global e so seria valida se tal funcao existisse, mas o builder deve proibir funcao com nome reservado.
- `sqrt` sem parenteses e simbolo, nao funcao de primeira classe.
- A linguagem nao tem funcoes como valores na Etapa 4.
- Nomes de funcoes globais e nomes de simbolos ficam em namespaces separados.
- `sqrt := 10` e permitido.
- `sqrt(4)` continua resolvendo para funcao global.
- Proibir apenas nomes reservados como simbolos.

## Valores Temporais

- `currDate` resolve para `DATE`.
- `currTime` resolve para `TIME`.
- `currDateTime` resolve para `DATETIME`.
- Valores temporais correntes sao dinamicos, derivados do clock de execucao.
- Valores temporais correntes nao sao constantes dobraveis.
- Operadores temporais como `DATE + NUMBER` nao devem existir.
- Operacoes temporais ficam em funcoes built-in explicitas.
- Comparacoes temporais sao estritas por tipo: `DATE` com `DATE`, `TIME` com `TIME`, `DATETIME` com `DATETIME`.
- `DATETIME` sem offset usa offset efetivo do `ZoneId` do ambiente.
- Em gaps/overlaps de DST, usar politica padrao de `LocalDateTime.atZone(zone)`.
- Metadata temporal deve registrar se houve offset explicito ou inferido, `ZoneId`, offset efetivo, valor normalizado e possivelmente gap/overlap.
- Comparacao de `DATETIME` usa o `LocalDateTime` normalizado no ambiente.

## Regex

- Operadores regex exigem lado esquerdo `STRING`.
- Regex dinamica nao e aceita nesta versao.
- O padrao do lado direito deve ser string estaticamente conhecida ou expressao constante dobravel de string.
- O resolver deve validar e pre-compilar `Pattern` como `Valor Semantico Preparado`.
- Regex invalida nunca passa da compilacao.

## Navegacao

- `Vinculo de Navegacao` deve ser produzido na Etapa 4 para cada elo de `Cadeia de Navegacao`.
- Etapa 6 executa navegacao; nao decide semantica nova.
- `navigationBindings` e por link, nao apenas pela cadeia inteira.
- Propriedade em `Tipo Objeto` conhecido exige membro registrado.
- Metodo em `Tipo Objeto` conhecido exige metodo registrado.
- Nao ha fallback reflexivo para objeto conhecido.
- Fallback reflexivo generico por tipo desconhecido nao existe, porque tipos devem ser conhecidos.
- `Tipo Objeto` nominal sem membros registrados nao permite navegacao de propriedade/metodo.
- `ObjectType` nominal pode participar de igualdade nominal, membership em colecao do mesmo tipo, retorno/passagem de funcao e atribuicoes.
- Metodo de objeto e `Vinculo de Navegacao`, nao `Vinculo de Funcao`.
- `customer.fullName()` pertence ao Java type/catalogo de navegacao do receptor.
- `customer?.fullName()` segue politica de navegacao segura.
- Navegacao segura protege apenas receiver null daquele link.
- Navegacao segura nao mascara membro invalido, tipo invalido, indice invalido ou erro de predicado.
- `customer?.age` resolve como `NUMBER` com metadata de nulidade `MAY_BE_NULL`.
- `customer?.age + 1` e semanticamente valido e pode falhar em runtime se o valor for null.
- O usuario usa `(customer?.age ?? 0) + 1` para protecao.
- Mapas nao aceitam acesso por propriedade: `m.key` e invalido para `Tipo Mapa`.
- Acesso a valor de mapa usa subscript textual: `m["key"]`.
- `m["key"]` pode produzir null de runtime se chave ausente ou valor null.

## Wildcards

- `[*]` em `Tipo Vetor<T>` ou `Tipo Colecao<T>` resulta `Tipo Colecao<T>`.
- `[*]` em `Tipo Mapa` e invalido.
- `.*` em `Tipo Mapa<V>` resulta `Tipo Colecao<V>`.
- `.*` em `Tipo Objeto` conhecido so e valido se o tipo registrado declarar exposicao de filhos/valores.
- `.*` nao deve refletir automaticamente todos os membros publicos.
- Wildcard produz colecao de valores e nao preserva chaves de mapa.
- Para chaves de mapa, usar `..keys()`.

## Filtros, Lambdas e Item Atual

- Filtro e valido em `Tipo Vetor<T>`, `Tipo Colecao<T>` e `Tipo Mapa<V>`.
- Filtro em vetor preserva `Tipo Vetor<T>`.
- Filtro em colecao preserva `Tipo Colecao<T>`.
- Filtro em mapa preserva `Tipo Mapa<V>`.
- Em filtro de vetor/colecao, `@` tem tipo do elemento `T`.
- Em filtro de mapa, `@` tem tipo contextual `Entrada de Mapa<V>`.
- `@.k` em filtro de mapa e `STRING`.
- `@.v` em filtro de mapa e `V`.
- `@.v.xyz` e valido se `V` for objeto com membro registrado `xyz`.
- `@` fora de filtro/lambda e erro semantico.
- Em filtros aninhados, `@` sempre aponta para o contexto mais interno.
- A v2 nao tem sintaxe para acessar item atual externo.
- `maxCurrentItemDepth` conta filtros e lambdas que introduzem `Item Atual`.
- O limite e validado semanticamente na Etapa 4.
- Diagnostico por excesso de profundidade aponta para o filtro/lambda que tentaria entrar no nivel proibido.
- Operacoes de colecao com lambda, como `..map(@ -> e)`, devem ser resolvidas na Etapa 4.
- Lambda introduz `Item Atual` com tipo vindo do descriptor da operacao de colecao.
- `..map(@ -> e)` retorna colecao/vetor conforme descriptor e preservacao declarada.
- `..sum()` exige elemento `NUMBER` e retorna `NUMBER`.
- O binding de operacao de colecao carrega descriptor escolhido, tipos de argumento/lambda, pureza e materializacao.
- Safe navigation sobre `@`, como `@?.active`, deve ser permitida.

## Operacoes de Colecao em Mapas

- `map..keys()` retorna `Tipo Colecao<STRING>`.
- `map..values()` retorna `Tipo Colecao<V>`.
- `map..map(@ -> e)` e valido com `@` como `Entrada de Mapa<V>` e retorna `Tipo Colecao<R>`.
- `map..map(@ -> e)` nao preserva chaves.
- `map..sum()` nao soma valores implicitamente.
- Para somar valores, usar `map..values()..sum()`.
- Filtro sintatico `map[?(...)]` cobre o caso de filtrar preservando mapa.
- Nao e necessario criar uma operacao separada `map..filter(...)` para preservar mapa nesta etapa.

## JavaTypeCatalog e Metadados Java

- `Tipo Java Registrado` deve declarar membros navegaveis por uma politica de exposicao do ambiente.
- Membro nao registrado e erro semantico.
- Metodos Java registrados devem carregar metadado de pureza/efeitos.
- Por padrao, metodos Java registrados devem ser considerados impuros, salvo marcacao explicita.
- Retornos Java por referencia refletidos devem ser considerados `MAY_BE_NULL` por padrao.
- Retornos Java primitivos e built-ins proprios que garantem valor podem ser `NEVER_NULL`.
- A nulidade de retorno Java e metadata, nao tipo nullable.

## Limites e Materializacao

- `maxVectorSize` deve ser validado na Etapa 4 apenas para materializacoes estaticamente conhecidas.
- Vetor literal maior que `maxVectorSize` e erro semantico.
- Filtros, `map`, `values`, wildcard e operacoes com tamanho runtime carregam metadata de materializacao.
- Etapa 6 aplica limites em runtime para materializacoes dinamicas.
- Grandes colecoes externas nao devem ser rejeitadas semanticamente apenas por tamanho.

## Compilacao, Views e Runtime

- `ExpressionCompilationResult` deve ser orientado a resultado, nao excecao como fluxo esperado.
- Sucesso de compilacao deve conter `CompiledExpression` e warnings.
- Falha de compilacao deve conter errors e warnings.
- `compileOrThrow` pode existir depois como conveniencia, mas nao deve ser o contrato primario.
- `asAssignments()` e valido quando ha apenas warnings.
- `asAssignments()` expõe valores finais de simbolos internos, incluindo reatribuicoes.
- `asMath()` e `asLogical()` devem rejeitar arquivo sem result expression na validacao de view da Etapa 5.
- Runtime errors devem usar diagnosticos com `SourceSpan` e `DiagnosticCode`, categoria `RUNTIME`.

## Testes e Corpus

- Criar suites por eixo semantico, nao um megateste.
- Eixos de teste: literais e valores preparados, simbolos externos, simbolos internos, restricoes, operadores, funcoes, overload, condicionais, coalescencia, navegacao, filtros/lambdas, desestruturacao, frame layout, diagnosticos e corpus gate.
- Cada regra positiva e negativa deve ter teste com codigo de diagnostico e span.
- Corpus deve aceitar `phase: semantic` para casos que parseiam/constroem AST mas falham no resolver.
- Casos validos de corpus podem declarar `expectedType`, warnings esperados e tags de cobertura.
- Casos invalidos de corpus devem declarar `expectedDiagnostic` com codigo, span e spans relacionados opcionais.
- Detalhes internos como slots de frame nao devem ir para corpus geral; devem ficar em testes unitarios do resolver.

## Decisoes Ainda Pendentes

- Confirmar a regra para membro Java registrado sem tipo de retorno mapeavel: rejeitar no builder do catalogo ou diagnosticar no uso. Recomendacao atual: rejeitar no registro do catalogo.
- Decidir a semantica de navegacao/projecao sobre colecoes para expressao como `pessoa.endereco[?(@.principal)].ativo = true`, onde `endereco` e `List<Endereco>`.
- Decidir se `.ativo` aplicado a `Tipo Colecao<Endereco>` projeta implicitamente para `Tipo Colecao<BOOLEAN>`.
- Se houver projecao implicita sobre colecao, decidir se `Tipo Colecao<BOOLEAN> = true` deve ser invalido ou se deve significar `any` implicitamente.
- Recomendacao pendente para a expressao de colecao: permitir projecao implicita em `.ativo`, mas exigir `any/all` explicito para comparacao colecao-escalar, evitando ambiguidade entre "algum" e "todos".

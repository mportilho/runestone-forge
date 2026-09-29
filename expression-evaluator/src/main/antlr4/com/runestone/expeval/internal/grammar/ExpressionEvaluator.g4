/*
 * ExpressionEvaluator.g4 — versão revisada (gramática combinada, lexer + parser)
 *
 * Baseada na gramática original de Marcelo Portilho (MIT License, 2021).
 *
 * PRINCIPAIS MUDANÇAS EM RELAÇÃO À ORIGINAL
 * ------------------------------------------------------------------------------
 * 1. HIERARQUIA UNIFICADA DE EXPRESSÕES
 *    As oito famílias tipadas (numericEntity, stringEntity, dateEntity, timeEntity,
 *    dateTimeEntity, logicalEntity, sequentialEntity, genericEntity) foram fundidas em
 *    uma única cadeia de precedência. A verificação de tipos passa a ser
 *    responsabilidade da análise semântica (visitor), que já precisava fazê-la de
 *    qualquer forma, pois toda família aceitava referenceTarget e o tipo real só é
 *    conhecido em runtime. Isso elimina ~70% das alternativas, todas as duplicações
 *    de IF/THEN/ELSE e as alternativas mortas de logicalComparisonExpression.
 *
 * 2. LITERAIS TEMPORAIS PREFIXADOS
 *    d"2021-01-02"                          (data)
 *    t"10:30" / t"10:30:45"                 (hora)
 *    dt"2021-01-02T10:30:45"                (data e hora)
 *    dt"2021-01-02T10:30:45+02:00"          (com offset, dentro do literal)
 *    Isso elimina de uma vez: a colisão TIME vs. slice ([10:20]), o caso quebrado
 *    [-10:20], o TIME_OFFSET guloso roubando "+10:30" de expressões, o separador
 *    '-' em DATETIME engolindo subtrações, e o fato de "2021-01-02" nunca poder
 *    ser interpretado como aritmética. As regras sliceTimeSubscript e COLON_OP
 *    deixam de ser necessárias.
 *
 * 3. MÓDULO |x| REMOVIDO
 *    Delimitadores idênticos pareados são ambíguos com aninhamento e colidiam com
 *    CONCAT '||'. Use a função de runtime abs(x). O token '|' isolado deixa de
 *    existir; '||' é inequivocamente concatenação.
 *
 * 4. SQRT REMOVIDO DA GRAMÁTICA
 *    sqrt(x) passa a ser uma função de runtime comum (functionCallOperation),
 *    como abs(x), max(x, y) etc. Menos casos especiais no parser.
 *
 * 5. INTEIROS E DECIMAIS SEPARADOS (INT / FLOAT)
 *    Índices e slices agora exigem INT sintaticamente — [1.5:2] vira erro de
 *    parse, não de runtime. Os tokens mortos POSITIVE e NegativeSymbol foram
 *    removidos (POSITIVE era inalcançável: NUMBER sempre vencia).
 *
 * 6. FILTROS REUTILIZAM A EXPRESSÃO PRINCIPAL
 *    [?( ... )] aceita qualquer expressão. A restrição de que '@' só é válido
 *    dentro de filtros/lambdas vira uma checagem semântica simples, em vez de
 *    uma mini-gramática paralela (filterPredicate/filterAtom/filterValue).
 *
 * 7. OPERADORES DE PERTINÊNCIA UNIFORMIZADOS
 *    'in', 'not in' e 'nin' (sinônimo de 'not in') valem em qualquer contexto,
 *    inclusive filtros.
 *
 * 8. '??' VIRA OPERADOR BINÁRIO GERAL
 *    De menor precedência, encadeável (a ?? b ?? c), em vez de sufixo repetido
 *    em cada família tipada.
 *
 * 9. COLEÇÃO VAZIA [] PASSA A SER VÁLIDA.
 *
 * 10. VALORES SEQUENCIAIS USAM UM ÚNICO CONCEITO: COLEÇÃO
 *     A sintaxe dedicada de funções de coleção — 'x..sum()', com o operador '..'
 *     (DOUBLE_PERIOD) e a regra paralela collectionFunctionAccess /
 *     collectionFunctionArguments — foi removida. Chamadas sobre coleções usam a
 *     mesma sintaxe de chamada navegada: 'x.sum()', 'x.map(@ -> @ * 2)'.
 *     O lambda '@ -> expr' deixa de ser exclusividade de uma regra especial e
 *     vira um TIPO DE ARGUMENTO comum (regra 'argument'), aceito tanto em
 *     chamadas de função de topo ('map(lista, @ -> @ * 2)') quanto em chamadas navegadas.
 *     O despacho "operação de coleção vs. método do objeto" é decidido pelo
 *     resolvedor semântico a partir do tipo conhecido do receptor. Ganhos colaterais:
 *       - safe-nav em funções de coleção: 'pedidos?.map(@ -> @.total)'
 *         (a antiga collectionFunctionAccess não tinha variante '?.');
 *       - argumentos mistos: 'reduce(0, @ -> ...)', 'sortBy(@ -> @.nome, "desc")'
 *         (a forma antiga era OU um lambda sozinho OU só posicionais);
 *       - encadeamento uniforme: 'x.map(@ -> @ * 2)[0]', sem alternar '.' e '..'.
 *     Literais e padrões de desestruturação usam nomenclatura de coleção.
 *
 * 11. WILDCARD TEM FORMA ÚNICA: [*]
 *     O acesso '.*' (childWildcardAccess) foi removido; 'x[*]' é a única grafia.
 *     Uma forma, um nó no AST.
 *
 * PRECEDÊNCIA (da menor para a maior):
 *    ??  <  or  <  and  <  comparação/in/between/regex  <  nand/nor/xor/xnor
 *        <  || (concat)  <  + -  <  * / mod  <  - unário e not (~ ¬ !)
 *        <  root  <  ^ (assoc. à direita)  <  % ! pós-fixados  <  primário
 *
 * NOTAS DE COMPATIBILIDADE / ARMADILHAS DOCUMENTADAS:
 *  - Desigualdade é escrita apenas com '<>' (o '!=' foi removido). Assim '5<>3'
 *    é comparação e '5!' é sempre fatorial, sem a antiga armadilha de '5!=3'.
 *    Resíduo: '5!~"x"' ainda lexa como '5' '!~' '"x"' (maximal munch de REGEX_NOT_MATCH);
 *    fatorial seguido de regex-não-casa exige espaço ('5! ~ "x"'). Caso semântica
 *    sem sentido, apenas documentado.
 *  - Atribuição usa ':=' (ex.: 'x := 2 + 3;'); '=' é exclusivamente igualdade.
 *    Isso elimina o lookahead até o ';' que a forma antiga ('=' para ambos) exigia,
 *    barateando a predição. Sem colisão com o ':' de slices: por maximal munch,
 *    ':=' só vence quando um '=' segue o ':'.
 *  - Comparações não são encadeáveis (a < b < c é erro), como na original.
 *  - Com a remoção do token DOUBLE_PERIOD, '..' é apenas um erro sintático comum.
 *  - '->' (ARROW) não é operador de expressão; aparece apenas na alternativa
 *    lambdaArgument. '@' sozinho como argumento cai em expressionArgument sem
 *    ambiguidade.
 */

grammar ExpressionEvaluator;

/* ########################################  Regras léxicas  ######################################## */

IF     : 'if' ;
THEN   : 'then' ;
ELSE   : 'else' ;
ELSEIF : 'elsif' ;
ENDIF  : 'endif' ;

// Reserved only so obsolete source null cannot be parsed as an identifier.
OBSOLETE_NULL : 'null' ;

AND    : 'and' ;
OR     : 'or' ;
XOR    : 'xor' ;
XNOR   : 'xnor' ;
NAND   : 'nand' ;
NOR    : 'nor' ;
TRUE   : 'true' ;
FALSE  : 'false' ;

IN      : 'in' ;
NIN     : 'nin' ;
NOT_KW  : 'not' ;
BETWEEN : 'between' ;
MODULO  : 'mod' ;
ROOT    : 'root' | '\u221A' ;

MULT           : '*' ;
DIV            : '/' ;
PLUS           : '+' ;
MINUS          : '-' ;
PERCENT        : '%' ;
CONCAT         : '||' ;
EXCLAMATION    : '!' ;
EXPONENTIATION : '^' ;

GT              : '>' ;
GE              : '>=' ;
LT              : '<' ;
LE              : '<=' ;
EQ              : '=' ;
NEQ             : '<>' ;
NOT             : '~' | '\u00AC' ;
REGEX_MATCH     : '=~' ;
REGEX_NOT_MATCH : '!~' ;

// Valores temporais correntes
NOW_DATE     : 'currDate' ;
NOW_TIME     : 'currTime' ;
NOW_DATETIME : 'currDateTime' ;

// Tokens auxiliares
LPAREN        : '(' ;
RPAREN        : ')' ;
LBRACKET      : '[' ;
RBRACKET      : ']' ;
COMMA         : ',' ;
SEMI          : ';' ;
PERIOD        : '.' ;
NULLCOALESCE  : '??' ;   // '??' e '?.' vencem '?' por maximal munch
SAFE_NAV      : '?.' ;
QUESTION      : '?' ;
COLON         : ':' ;    // usado apenas em slices; literais de hora agora são prefixados (t"...")
ASSIGN        : ':=' ;   // ':=' vence ':' por maximal munch; em slices ([1:2]) o ':' é seguido de dígito e lexa normalmente
AT            : '@' ;
ARROW         : '->' ;

// Literais temporais prefixados — o conteúdo é validado estruturalmente pelo lexer
DATE     : 'd"'  DateFragment '"' ;
TIME     : 't"'  TimeFragment '"' ;
DATETIME : 'dt"' DateFragment 'T' TimeFragment TimeOffsetFragment? '"' ;

// Literais numéricos e de texto
IDENTIFIER : [a-zA-Z_][a-zA-Z_0-9]* ;
STRING     : '"' ( '\\' [btnfr"'\\] | ~[\r\n\\"] )* '"' ;
FLOAT      : [0-9]+ '.' [0-9]+ ;
INT        : '0' | [1-9] [0-9]* ;

// Fragments
fragment DayFragment          : '0' [1-9] | [1-2] [0-9] | '3' [0-1] ;
fragment MonthFragment        : '0' [1-9] | '1' [0-2] ;
fragment HourFragment         : [0-1] [0-9] | '2' [0-3] ;
fragment MinuteSecondFragment : [0-5] [0-9] ;
fragment DateFragment         : [0-9] [0-9] [0-9] [0-9] '-' MonthFragment '-' DayFragment ;
fragment TimeFragment         : HourFragment ':' MinuteSecondFragment (':' MinuteSecondFragment)? ;
fragment TimeOffsetFragment   : ('+' | '-') HourFragment ':' MinuteSecondFragment ;

// Comentários e espaços
LINE_COMMENT  : '//' ~[\r\n]* -> skip ;
BLOCK_COMMENT : '/*' .*? '*/' -> skip ;

WS         : [ \r\t\u000C\n]+ -> channel(HIDDEN) ;
ERROR_CHAR : . ;

/* ########################################  Regras sintáticas  ######################################## */

// Ponto de entrada único: zero ou mais atribuições seguidas de uma expressão final opcional.
// Substitui mathStart / assignmentStart / logicalStart — a validação do tipo do
// resultado (numérico, lógico etc.) é feita pelo visitor conforme o contexto de uso.
start
    : assignmentExpression* expression? EOF                              # startInput
    ;

assignmentExpression
    : IDENTIFIER ASSIGN expression SEMI                                  # assignmentOperation
    | destructuringPattern ASSIGN expression SEMI                        # destructuringAssignmentOperation
    ;

// Padrão de atribuição, não um valor: [a, b] := minhaColecao;
destructuringPattern
    : LBRACKET IDENTIFIER (COMMA IDENTIFIER)* RBRACKET                   # destructuringPatternOperation
    ;

// ---------------------------------------------------------------------------
// Cadeia de precedência unificada
// ---------------------------------------------------------------------------

expression
    : coalesceExpression                                                 # expressionOperation
    ;

// a ?? b ?? c — avaliação da esquerda para a direita, primeiro valor não nulo
coalesceExpression
    : orExpression (NULLCOALESCE orExpression)*                          # coalesceOperation
    ;

orExpression
    : andExpression (OR andExpression)*                                  # logicalOrOperation
    ;

andExpression
    : comparisonExpression (AND comparisonExpression)*                   # logicalAndOperation
    ;

// Comparações não encadeáveis (no máximo um operador por nível, como na original).
// BETWEEN reutiliza AND com segurança: os operandos param no nível bitwise,
// que não consome 'and'.
comparisonExpression
    : bitwiseLogicalExpression comparisonOperator bitwiseLogicalExpression                          # comparisonOperation
    | bitwiseLogicalExpression NOT_KW? IN bitwiseLogicalExpression                                  # inOperation
    | bitwiseLogicalExpression NIN bitwiseLogicalExpression                                         # ninOperation
    | bitwiseLogicalExpression NOT_KW? BETWEEN bitwiseLogicalExpression AND bitwiseLogicalExpression # betweenOperation
    | bitwiseLogicalExpression REGEX_MATCH STRING                                                   # regexMatchOperation
    | bitwiseLogicalExpression REGEX_NOT_MATCH STRING                                               # regexNotMatchOperation
    | bitwiseLogicalExpression                                                                      # bitwisePassthroughOperation
    ;

bitwiseLogicalExpression
    : concatExpression ((NAND | NOR | XOR | XNOR) concatExpression)*     # logicalBitwiseOperation
    ;

concatExpression
    : additiveExpression (CONCAT additiveExpression)*                    # stringConcatenationOperation
    ;

additiveExpression
    : multiplicativeExpression ((PLUS | MINUS) multiplicativeExpression)* # additiveOperation
    ;

multiplicativeExpression
    : unaryExpression ((MULT | DIV | MODULO) unaryExpression)*           # multiplicativeOperation
    ;

unaryExpression
    : MINUS unaryExpression                                              # unaryMinusOperation
    | (NOT | EXCLAMATION) unaryExpression                                # logicalNotOperation
    | rootExpression                                                     # rootPassthroughOperation
    ;

rootExpression
    : exponentiationExpression (ROOT exponentiationExpression)*          # rootChainOperation
    ;

// Recursão em unaryExpression preserva a associatividade à direita e permite 2^-3
exponentiationExpression
    : postfixExpression (EXPONENTIATION unaryExpression)?                # exponentiationOperation
    ;

postfixExpression
    : primaryExpression (PERCENT | EXCLAMATION)*                         # postfixOperation
    ;

// ---------------------------------------------------------------------------
// Primários
// ---------------------------------------------------------------------------

primaryExpression
    : LPAREN expression RPAREN                                           # parenthesisOperation
    | ifExpression                                                       # decisionOperation
    | collectionLiteral                                                  # collectionLiteralOperation
    | referenceTarget                                                    # referenceTargetOperation
    | literal                                                            # literalOperation
    ;

ifExpression
    : IF expression THEN expression
      (ELSEIF expression THEN expression)*
      ELSE expression ENDIF                                              # ifThenElseOperation
    | IF LPAREN expression (COMMA | SEMI) expression
      ((COMMA | SEMI) expression (COMMA | SEMI) expression)*
      (COMMA | SEMI) expression RPAREN                                   # functionalIfOperation
    ;

literal
    : INT                                                                # intConstantOperation
    | FLOAT                                                              # floatConstantOperation
    | STRING                                                             # stringConstantOperation
    | (TRUE | FALSE)                                                     # logicalConstantOperation
    | DATE                                                               # dateConstantOperation
    | TIME                                                               # timeConstantOperation
    | DATETIME                                                           # dateTimeConstantOperation
    | NOW_DATE                                                           # dateCurrentValueOperation
    | NOW_TIME                                                           # timeCurrentValueOperation
    | NOW_DATETIME                                                       # dateTimeCurrentValueOperation
    ;

collectionLiteral
    : LBRACKET (expression (COMMA expression)*)? RBRACKET                # collectionOfEntitiesOperation
    ;

// ---------------------------------------------------------------------------
// Referências, funções e navegação
// ---------------------------------------------------------------------------

memberName
    : IDENTIFIER
    | IF
    | THEN
    | ELSE
    | ELSEIF
    | ENDIF
    | AND
    | OR
    | XOR
    | XNOR
    | NAND
    | NOR
    | TRUE
    | FALSE
    | IN
    | NIN
    | NOT_KW
    | BETWEEN
    | MODULO
    | ROOT
    | NOW_DATE
    | NOW_TIME
    | NOW_DATETIME
    ;

referenceTarget
    : function memberChain*                                              # functionReferenceTarget
    | IDENTIFIER memberChain*                                            # identifierReferenceTarget
    // '@' (elemento corrente) só é válido dentro de filtros [?(...)] e lambdas
    // '@ -> ...' — validação feita na análise semântica.
    | AT memberChain*                                                    # atReferenceTarget
    ;

function
    : IDENTIFIER LPAREN argumentList? RPAREN                             # functionCallOperation
    ;

// Lista de argumentos única para funções de topo e chamadas navegadas.
// Lambdas ('@ -> expr') podem ser misturados a argumentos posicionais em
// qualquer posição: reduce(0, @ -> ...), sortBy(@ -> @.nome, "desc").
argumentList
    : argument (COMMA argument)*                                         # argumentListOperation
    ;

argument
    : AT ARROW expression                                                # lambdaArgument
    | expression                                                         # expressionArgument
    ;

// Uma única sintaxe de chamada '.nome(...)' cobre destinos ainda não classificados;
// o despacho para método de objeto ou operação de coleção é feito
// pelo resolvedor semântico a partir do tipo conhecido do receptor.
memberChain
    // navigatedCallAccess antes de propertyAccess: 'ident(' não deve virar propriedade + '(' extra
    : PERIOD memberName LPAREN argumentList? RPAREN                      # navigatedCallAccess
    | SAFE_NAV memberName LPAREN argumentList? RPAREN                    # safeNavigatedCallAccess
    | PERIOD memberName                                                  # propertyAccess
    | SAFE_NAV memberName                                                # safePropertyAccess
    | SAFE_NAV subscript                                                 # safeSubscriptAccess
    | subscript                                                          # subscriptAccess
    ;

// Com literais de hora prefixados (t"10:20"), [10:20] é sempre um slice comum:
// não há mais colisão léxica, e [-10:20], [10:20:30]... simplesmente não existem
// como tokens — slices negativos ([-10:20]) agora funcionam naturalmente.
// [*] é a ÚNICA grafia de wildcard (item 11).
subscript
    : LBRACKET MULT RBRACKET                                             # wildcardSubscript
    | LBRACKET STRING RBRACKET                                           # stringKeySubscript
    | LBRACKET signedInteger COLON signedInteger? RBRACKET               # sliceWithStartSubscript
    | LBRACKET COLON signedInteger RBRACKET                              # sliceToEndSubscript
    | LBRACKET signedInteger RBRACKET                                    # indexSubscript
    | LBRACKET QUESTION LPAREN expression RPAREN RBRACKET                # filterSubscript
    ;

// INT em vez de NUMBER: [1.5], [0x1F] e [077] deixam de parsear como índices válidos.
signedInteger
    : MINUS? INT                                                         # signedIntegerOperation
    ;

comparisonOperator
    : GT                                                                 # greaterThanOperator
    | GE                                                                 # greaterThanOrEqualOperator
    | LT                                                                 # lessThanOperator
    | LE                                                                 # lessThanOrEqualOperator
    | EQ                                                                 # equalOperator
    | NEQ                                                                # notEqualOperator
    ;

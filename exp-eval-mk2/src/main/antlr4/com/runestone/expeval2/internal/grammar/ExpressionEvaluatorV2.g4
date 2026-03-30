/*MIT License

Copyright (c) 2021 Marcelo Portilho

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.*/

grammar ExpressionEvaluatorV2;

/* ########################################  Lexical rules  ######################################## */

IF        : 'if' ;
THEN      : 'then' ;
ELSE      : 'else' ;
ELSEIF    : 'elsif' ;
ENDIF     : 'endif' ;

NULL  : 'null' ;

AND   : 'and' ;
OR    : 'or' ;
XOR   : 'xor' ;
XNOR  : 'xnor' ;
NAND  : 'nand' ;
NOR   : 'nor' ;
TRUE  : 'true' ;
FALSE : 'false' ;

MULT          : '*' ;
DIV           : '/' ;
PLUS          : '+' ;
MINUS         : '-' ;
PERCENT       : '%' ;
MODULO        : 'mod' ;
MODULUS       : '|' ;
CONCAT        : '||' ;
EXCLAMATION   : '!' ;
EXPONENTIATION: '^' ;
ROOT          : 'root' | '\u221A' ;
SQRT          : 'sqrt' ;

GT              : '>' ;
GE              : '>=' ;
LT              : '<' ;
LE              : '<=' ;
EQ              : '=' ;
NEQ             : '!=' | '<>' ;
NOT             : '~' | '\u00AC' ;
REGEX_MATCH     : '=~' ;
REGEX_NOT_MATCH : '!~' ;

// Date/time current-value tokens
NOW_DATE     : 'currDate' ;
NOW_TIME     : 'currTime' ;
NOW_DATETIME : 'currDateTime' ;

// Grammar assist tokens
LPAREN             : '(' ;
RPAREN             : ')' ;
LBRACKET           : '[' ;
RBRACKET           : ']' ;
COMMA              : ',' ;
SEMI               : ';' ;
PERIOD             : '.' ;
NULLCOALESCE       : '??' ;
SAFE_NAV           : '?.' ;
IN      : 'in' ;
NOT_KW  : 'not' ;

// Literals
IDENTIFIER : IdentifierText ;
STRING     : '"' ( '\\' [btnfr"'\\] | ~[\r\n\\"] )* '"';
NUMBER     : Decimal | OctalDigits | HexDigits ;
POSITIVE   : PositiveNumber ;
DATE       : DateFragment ;
TIME       : HourFragment Colon MinuteSecondFragment (Colon MinuteSecondFragment)? ;
TIME_OFFSET: TimeOffsetFragment ;
DATETIME   : DateFragment ('T' | '-') HourFragment Colon MinuteSecondFragment (Colon MinuteSecondFragment)? ;

// Type-hint tokens
BOOLEAN_TYPE  : '<bool>' ;
NUMBER_TYPE   : '<number>' ;
STRING_TYPE   : '<text>' ;
DATE_TYPE     : '<date>' ;
TIME_TYPE     : '<time>' ;
DATETIME_TYPE : '<datetime>' ;
VECTOR_TYPE   : '<vector>' ;

// Fragments
fragment IdentifierText      : [a-zA-Z_][a-zA-Z_0-9]* ;
fragment NegativeSymbol      : '-' ;
fragment Decimal             : [0-9]+ ('.' [0-9]+)? ;
fragment PositiveNumber      : [0-9]+ ;
fragment OctalDigits         : '0' '0'..'7'+ ;
fragment HexDigits           : '0x' ('0'..'9' | 'a'..'f' | 'A'..'F')+ ;
fragment Colon               : ':' ;
fragment DayFragment         : '0' '1'..'9' | '1'..'2' '0'..'9' | '3' '0'..'1' ;
fragment MonthFragment       : '0' '1'..'9' | '1' '0'..'2' ;
fragment HourFragment        : '0'..'1' '0'..'9' | '2' '0'..'3' ;
fragment MinuteSecondFragment: '0'..'5' '0'..'9' ;
fragment DateFragment        : '0'..'9' '0'..'9' '0'..'9' '0'..'9' '-' MonthFragment '-' DayFragment ;
fragment TimeOffsetFragment  : ('+' | '-') HourFragment Colon MinuteSecondFragment ;

// Comments and whitespace
LINE_COMMENT  : '//' ~[\r\n]* -> skip ;
BLOCK_COMMENT : '/*' .*? '*/' -> skip ;

WS : [ \r\t\u000C\n]+ -> channel(HIDDEN) ;
ERROR_CHAR : . ;

/* ########################################  Grammar rules  ######################################## */

mathStart
    : (assignmentExpression)* mathExpression EOF                          # mathInput
    ;

assignmentStart
    : assignmentExpression+ EOF                                           # assignmentInput
    ;

logicalStart
    : assignmentExpression* logicalExpression EOF                        # logicalInput
    ;

assignmentExpression
    : IDENTIFIER EQ assignmentValue SEMI                     # assignmentOperation
    | vectorOfVariables EQ vectorEntity SEMI                   # destructuringAssignmentOperation
    ;

// Logical precedence:
// NOT (highest) > NAND/NOR/XOR/XNOR > comparison > AND > OR
logicalExpression
    : logicalOrExpression                                                # logicalOrOperation
    ;

logicalOrExpression
    : logicalAndExpression (OR logicalAndExpression)*                   # logicalOrChainOperation
    ;

logicalAndExpression
    : logicalComparisonExpression (AND logicalComparisonExpression)*    # logicalAndChainOperation
    ;

logicalComparisonExpression
    : logicalBitwiseExpression (comparisonOperator logicalBitwiseExpression)? # logicalComparisonOperation
    | mathExpression comparisonOperator mathExpression                        # mathComparisonOperation
    | stringConcatExpression comparisonOperator stringConcatExpression        # stringComparisonOperation
    | dateEntity comparisonOperator dateEntity                                # dateComparisonOperation
    | timeEntity comparisonOperator timeEntity                                # timeComparisonOperation
    | dateTimeEntity comparisonOperator dateTimeEntity                        # dateTimeComparisonOperation
    | stringConcatExpression REGEX_MATCH STRING                               # regexMatchOperation
    | stringConcatExpression REGEX_NOT_MATCH STRING                           # regexNotMatchOperation
    | mathExpression IN vectorEntity                                          # mathInOperation
    | stringConcatExpression IN vectorEntity                                  # stringInOperation
    | dateEntity IN vectorEntity                                              # dateInOperation
    | timeEntity IN vectorEntity                                              # timeInOperation
    | dateTimeEntity IN vectorEntity                                          # dateTimeInOperation
    | logicalBitwiseExpression IN vectorEntity                                # logicalInOperation
    | NULL IN vectorEntity                                                    # nullInOperation
    | mathExpression NOT_KW IN vectorEntity                                   # mathNotInOperation
    | stringConcatExpression NOT_KW IN vectorEntity                           # stringNotInOperation
    | dateEntity NOT_KW IN vectorEntity                                       # dateNotInOperation
    | timeEntity NOT_KW IN vectorEntity                                       # timeNotInOperation
    | dateTimeEntity NOT_KW IN vectorEntity                                   # dateTimeNotInOperation
    | logicalBitwiseExpression NOT_KW IN vectorEntity                         # logicalNotInOperation
    | NULL NOT_KW IN vectorEntity                                             # nullNotInOperation
    ;

logicalBitwiseExpression
    : logicalNotExpression ((NAND | NOR | XOR | XNOR) logicalNotExpression)* # logicalBitwiseOperation
    ;

logicalNotExpression
    : (NOT | EXCLAMATION) logicalNotExpression                          # logicalNotOperation
    | logicalPrimary                                                    # logicalPrimaryOperation
    ;

logicalPrimary
    : LPAREN logicalExpression RPAREN                                   # logicalExpressionParenthesisOperation
    | logicalEntity                                                     # logicalEntityOperation
    ;

// Mathematical precedence:
// postfix % and ! > exponentiation ^ > root > unary - > *, /, mod > +, -
mathExpression
    : sumExpression                                                      # sumOperation
    ;

sumExpression
    : multiplicationExpression ((PLUS | MINUS) multiplicationExpression)* # additiveOperation
    ;

multiplicationExpression
    : unaryExpression ((MULT | DIV | MODULO) unaryExpression)*           # multiplicativeOperation
    ;

unaryExpression
    : MINUS unaryExpression                                              # unaryMinusOperation
    | rootExpression                                                     # rootExpressionOperation
    ;

rootExpression
    : exponentiationExpression (ROOT exponentiationExpression)*          # rootChainOperation
    ;

exponentiationExpression
    : postfixExpression (EXPONENTIATION unaryExpression)?                # exponentiationOperation
    ;

postfixExpression
    : primaryMathExpression ((PERCENT | EXCLAMATION))*                   # postfixOperation
    ;

primaryMathExpression
    : LPAREN mathExpression RPAREN                                       # mathExpressionParenthesisOperation
    | SQRT LPAREN mathExpression RPAREN                                  # squareRootOperation
    | MODULUS mathExpression MODULUS                                     # modulusOperation
    | numericEntity                                                      # numericEntityOperation
    ;

function
    : IDENTIFIER LPAREN (allEntityTypes ((COMMA | SEMI) allEntityTypes)*)? RPAREN # functionCallOperation
    ;

referenceTarget
    : function                                                           # functionReferenceTarget
    | IDENTIFIER memberChain*                                            # identifierReferenceTarget
    ;

memberChain
    : PERIOD IDENTIFIER                                                  # propertyAccess
    | SAFE_NAV IDENTIFIER                                                # safePropertyAccess
    | PERIOD IDENTIFIER LPAREN
          (allEntityTypes (COMMA allEntityTypes)*)?
      RPAREN                                                             # methodCallAccess
    | SAFE_NAV IDENTIFIER LPAREN
          (allEntityTypes (COMMA allEntityTypes)*)?
      RPAREN                                                             # safeMethodCallAccess
    ;

comparisonOperator
    : GT                                                                 # greaterThanOperator
    | GE                                                                 # greaterThanOrEqualOperator
    | LT                                                                 # lessThanOperator
    | LE                                                                 # lessThanOrEqualOperator
    | EQ                                                                 # equalOperator
    | NEQ                                                                # notEqualOperator
    ;

allEntityTypes
    : mathExpression                                                     # mathEntityType
    | logicalExpression                                                  # logicalEntityType
    | dateEntity                                                         # dateEntityType
    | timeEntity                                                         # timeEntityType
    | dateTimeEntity                                                     # dateTimeEntityType
    | stringConcatExpression                                             # stringEntityType
    | vectorEntity                                                       # vectorEntityType
    | NULL                                                               # nullEntityType
    ;

assignmentValue
    : genericEntity                                                      # genericAssignmentValue
    | mathExpression                                                     # mathAssignmentValue
    | logicalExpression                                                  # logicalAssignmentValue
    | dateEntity                                                         # dateAssignmentValue
    | timeEntity                                                         # timeAssignmentValue
    | dateTimeEntity                                                     # dateTimeAssignmentValue
    | stringConcatExpression                                             # stringAssignmentValue
    | vectorEntity                                                       # vectorAssignmentValue
    ;

genericEntity
    : IF logicalExpression THEN genericEntity (ELSEIF logicalExpression THEN genericEntity)* ELSE genericEntity ENDIF # genericDecisionExpression
    | IF LPAREN logicalExpression (COMMA | SEMI) genericEntity ((COMMA | SEMI) logicalExpression (COMMA | SEMI) genericEntity)* (COMMA | SEMI) genericEntity RPAREN # genericFunctionDecisionExpression
    | castExpression                                                                                                                       # castExpressionOperation
    | referenceTarget                                                                                                                      # referenceTargetOperation
    ;

castExpression
    : typeHint LPAREN genericEntity RPAREN                              # typeCastOperation
    ;

typeHint
    : BOOLEAN_TYPE                                                       # booleanTypeHint
    | NUMBER_TYPE                                                        # numberTypeHint
    | STRING_TYPE                                                        # stringTypeHint
    | DATE_TYPE                                                          # dateTypeHint
    | TIME_TYPE                                                          # timeTypeHint
    | DATETIME_TYPE                                                      # dateTimeTypeHint
    | VECTOR_TYPE                                                        # vectorTypeHint
    ;

logicalEntity
    : (TRUE | FALSE)                                                                                           # logicalConstantOperation
    | IF logicalExpression THEN logicalExpression (ELSEIF logicalExpression THEN logicalExpression)* ELSE logicalExpression ENDIF # logicalDecisionOperation
    | IF LPAREN logicalExpression (COMMA | SEMI) logicalExpression ((COMMA | SEMI) logicalExpression (COMMA | SEMI) logicalExpression)* (COMMA | SEMI) logicalExpression RPAREN # logicalFunctionDecisionOperation
    | BOOLEAN_TYPE? referenceTarget (NULLCOALESCE logicalEntity)?                                                        # logicalReferenceOperation
    ;

numericEntity
    : IF logicalExpression THEN mathExpression (ELSEIF logicalExpression THEN mathExpression)* ELSE mathExpression ENDIF # mathDecisionOperation
    | IF LPAREN logicalExpression (COMMA | SEMI) mathExpression ((COMMA | SEMI) logicalExpression (COMMA | SEMI) mathExpression)* (COMMA | SEMI) mathExpression RPAREN # mathFunctionDecisionOperation
    | NUMBER                                                                                                             # numericConstantOperation
    | NUMBER_TYPE? referenceTarget (NULLCOALESCE numericEntity)?                                                         # numericReferenceOperation
    ;

stringConcatExpression
    : stringEntity (CONCAT stringEntity)*                                                                       # stringConcatenationOperation
    ;

stringEntity
    : IF logicalExpression THEN stringConcatExpression (ELSEIF logicalExpression THEN stringConcatExpression)* ELSE stringConcatExpression ENDIF # stringDecisionOperation
    | IF LPAREN logicalExpression (COMMA | SEMI) stringConcatExpression ((COMMA | SEMI) logicalExpression (COMMA | SEMI) stringConcatExpression)* (COMMA | SEMI) stringConcatExpression RPAREN # stringFunctionDecisionOperation
    | STRING                                                                                                    # stringConstantOperation
    | STRING_TYPE? referenceTarget (NULLCOALESCE stringConcatExpression)?                                       # stringReferenceOperation
    ;

dateEntity
    : IF logicalExpression THEN dateEntity (ELSEIF logicalExpression THEN dateEntity)* ELSE dateEntity ENDIF # dateDecisionOperation
    | IF LPAREN logicalExpression (COMMA | SEMI) dateEntity ((COMMA | SEMI) logicalExpression (COMMA | SEMI) dateEntity)* (COMMA | SEMI) dateEntity RPAREN # dateFunctionDecisionOperation
    | DATE                                                                                                     # dateConstantOperation
    | NOW_DATE                                                                                                 # dateCurrentValueOperation
    | DATE_TYPE? referenceTarget (NULLCOALESCE dateEntity)?                                                    # dateReferenceOperation
    ;

timeEntity
    : IF logicalExpression THEN timeEntity (ELSEIF logicalExpression THEN timeEntity)* ELSE timeEntity ENDIF # timeDecisionOperation
    | IF LPAREN logicalExpression (COMMA | SEMI) timeEntity ((COMMA | SEMI) logicalExpression (COMMA | SEMI) timeEntity)* (COMMA | SEMI) timeEntity RPAREN # timeFunctionDecisionOperation
    | TIME                                                                                                     # timeConstantOperation
    | NOW_TIME                                                                                                 # timeCurrentValueOperation
    | TIME_TYPE? referenceTarget (NULLCOALESCE timeEntity)?                                                    # timeReferenceOperation
    ;

dateTimeEntity
    : IF logicalExpression THEN dateTimeEntity (ELSEIF logicalExpression THEN dateTimeEntity)* ELSE dateTimeEntity ENDIF # dateTimeDecisionOperation
    | IF LPAREN logicalExpression (COMMA | SEMI) dateTimeEntity ((COMMA | SEMI) logicalExpression (COMMA | SEMI) dateTimeEntity)* (COMMA | SEMI) dateTimeEntity RPAREN # dateTimeFunctionDecisionOperation
    | DATETIME TIME_OFFSET?                                                                                    # dateTimeConstantOperation
    | NOW_DATETIME                                                                                             # dateTimeCurrentValueOperation
    | DATETIME_TYPE? referenceTarget (NULLCOALESCE dateTimeEntity)?                                            # dateTimeReferenceOperation
    ;

vectorEntity
    : IF logicalExpression THEN vectorEntity (ELSEIF logicalExpression THEN vectorEntity)* ELSE vectorEntity ENDIF # vectorDecisionOperation
    | IF LPAREN logicalExpression (COMMA | SEMI) vectorEntity ((COMMA | SEMI) logicalExpression (COMMA | SEMI) vectorEntity)* (COMMA | SEMI) vectorEntity RPAREN # vectorFunctionDecisionOperation
    | LBRACKET allEntityTypes (COMMA allEntityTypes)* RBRACKET                               # vectorOfEntitiesOperation
    | VECTOR_TYPE? referenceTarget (NULLCOALESCE vectorEntity)?                              # vectorReferenceOperation
    ;

vectorOfVariables
    : LBRACKET IDENTIFIER (COMMA IDENTIFIER)* RBRACKET                   # vectorOfVariablesOperation
    ;

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

@header {
package com.runestone.expeval2.grammar.language;
}

/* ########################################  Lexical rules  ######################################## */

IF        : 'if' ;
THEN      : 'then' ;
ELSE      : 'else' ;
ELSEIF    : 'elsif' ;
ENDIF     : 'endif' ;

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
EXCLAMATION   : '!' ;
EXPONENTIATION: '^' ;
ROOT          : 'root' | '\u221A' ;
SQRT          : 'sqrt' ;

GT  : '>' ;
GE  : '>=' ;
LT  : '<' ;
LE  : '<=' ;
EQ  : '=' ;
NEQ : '!=' | '<>' ;
NOT : '~' | '\u00AC' ;

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
CACHE_FUNCTION_PREFIX : '$.' ;

// Literals
IDENTIFIER : IdentifierText ;
STRING     : '"' ( '\\' [btnfr"'\\] | ~[\r\n\\"] )* '"';
NUMBER     : Decimal | OctalDigits | HexDigits ;
POSITIVE   : PositiveNumber ;
DATE       : DateFragment ;
TIME       : HourFragment Colon MinuteSecondFragment (Colon MinuteSecondFragment)? ;
TIME_OFFSET: TimeOffsetFragment ;
DATETIME   : DateFragment ('T' | '-') HourFragment Colon MinuteSecondFragment (Colon MinuteSecondFragment)? ;

// Type-casting tokens
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
fragment Decimal             : NegativeSymbol? [0-9]+ ('.' [0-9]+)? ;
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
    : (assignmentExpression)* mathExpression EOF
    ;

logicalStart
    : assignmentExpression* logicalExpression EOF
    ;

assignmentExpression
    : IDENTIFIER EQ allEntityTypes SEMI                        # assignOperation
    | vectorOfVariables EQ (vectorEntity | function) SEMI      # destructuringAssignment
    ;

// Logical expression — precedence expressed via alternative ordering:
// NOT (highest) > NAND/NOR/XOR/XNOR > comparison > AND > OR
logicalExpression
    : (NOT | EXCLAMATION) logicalExpression                             # notExpression
    | logicalExpression (NAND | NOR | XOR | XNOR) logicalExpression     # bitwiseLogicExpression
    | logicalExpression comparisonOperator logicalExpression            # logicComparisonExpression
    | logicalExpression AND logicalExpression                           # andExpression
    | logicalExpression OR logicalExpression                            # orExpression
    | mathExpression comparisonOperator mathExpression                  # mathComparisonExpression
    | stringEntity comparisonOperator stringEntity                      # stringExpression
    | dateEntity comparisonOperator dateEntity                          # dateComparisonExpression
    | timeEntity comparisonOperator timeEntity                          # timeComparisonExpression
    | dateTimeEntity comparisonOperator dateTimeEntity                  # dateTimeComparisonExpression
    | LPAREN logicalExpression RPAREN                                   # logicalParenthesis
    | logicalEntity                                                     # logicalValue
    ;

mathExpression
    : MINUS LPAREN mathExpression RPAREN                                # negateMathParenthesis
    | LPAREN mathExpression RPAREN                                      # mathParenthesis
    | mathExpression PERCENT                                            # percentExpression
    | mathExpression EXCLAMATION                                        # factorialExpression
    | mathExpression ROOT mathExpression                                # rootExpression
    | SQRT LPAREN mathExpression RPAREN                                 # squareRootExpression
    | <assoc=right> mathExpression EXPONENTIATION mathExpression        # exponentiationExpression
    | mathExpression (MULT | DIV | MODULO) mathExpression               # multiplicationExpression
    | mathExpression (PLUS | MINUS) mathExpression                      # sumExpression
    | MODULUS mathExpression MODULUS                                    # modulusExpression
    | numericEntity                                                     # numberValue
    ;

function
    : CACHE_FUNCTION_PREFIX? IDENTIFIER LPAREN (allEntityTypes ((COMMA | SEMI) allEntityTypes)*)? RPAREN
    ;

comparisonOperator
    : GT | GE | LT | LE | EQ | NEQ
    ;

allEntityTypes
    : mathExpression
    | logicalExpression
    | dateEntity
    | timeEntity
    | dateTimeEntity
    | stringEntity
    | vectorEntity
    ;

logicalEntity
    : (TRUE | FALSE)                                                                                           # logicalConstant
    | IF logicalExpression THEN logicalExpression (ELSEIF logicalExpression THEN logicalExpression)* ELSE logicalExpression ENDIF # logicalDecisionExpression
    | IF LPAREN logicalExpression (COMMA | SEMI) logicalExpression ((COMMA | SEMI) logicalExpression (COMMA | SEMI) logicalExpression)* (COMMA | SEMI) logicalExpression RPAREN # logicalFunctionDecisionExpression
    | BOOLEAN_TYPE? function                                                                                   # logicalFunctionResult
    | BOOLEAN_TYPE? IDENTIFIER                                                                                 # logicalVariable
    ;

numericEntity
    : MINUS? IF logicalExpression THEN mathExpression (ELSEIF logicalExpression THEN mathExpression)* ELSE mathExpression ENDIF # mathDecisionExpression
    | MINUS? IF LPAREN logicalExpression (COMMA | SEMI) mathExpression ((COMMA | SEMI) logicalExpression (COMMA | SEMI) mathExpression)* (COMMA | SEMI) mathExpression RPAREN # mathFunctionDecisionExpression
    | NUMBER                                                                                                   # numericConstant
    | NUMBER_TYPE? MINUS? function                                                                                    # numericFunctionResult
    // Identifiers may resolve to user variables or semantic built-ins such as E/pi.
    | NUMBER_TYPE? MINUS? IDENTIFIER                                                                                  # numericVariable
    ;

stringEntity
    : IF logicalExpression THEN stringEntity (ELSEIF logicalExpression THEN stringEntity)* ELSE stringEntity ENDIF # stringDecisionExpression
    | IF LPAREN logicalExpression (COMMA | SEMI) stringEntity ((COMMA | SEMI) logicalExpression (COMMA | SEMI) stringEntity)* (COMMA | SEMI) stringEntity RPAREN # stringFunctionDecisionExpression
    | STRING                                                                                                    # stringConstant
    | STRING_TYPE? function                                                                                    # stringFunctionResult
    | STRING_TYPE? IDENTIFIER                                                                                  # stringVariable
    ;

dateEntity
    : IF logicalExpression THEN dateEntity (ELSEIF logicalExpression THEN dateEntity)* ELSE dateEntity ENDIF # dateDecisionExpression
    | IF LPAREN logicalExpression (COMMA | SEMI) dateEntity ((COMMA | SEMI) logicalExpression (COMMA | SEMI) dateEntity)* (COMMA | SEMI) dateEntity RPAREN # dateFunctionDecisionExpression
    | DATE                                                                                                     # dateConstant
    | NOW_DATE                                                                                                 # dateCurrentValue
    | DATE_TYPE? IDENTIFIER                                                                                    # dateVariable
    | DATE_TYPE? function                                                                                      # dateFunctionResult
    ;

timeEntity
    : IF logicalExpression THEN timeEntity (ELSEIF logicalExpression THEN timeEntity)* ELSE timeEntity ENDIF # timeDecisionExpression
    | IF LPAREN logicalExpression (COMMA | SEMI) timeEntity ((COMMA | SEMI) logicalExpression (COMMA | SEMI) timeEntity)* (COMMA | SEMI) timeEntity RPAREN # timeFunctionDecisionExpression
    | TIME                                                                                                     # timeConstant
    | NOW_TIME                                                                                                 # timeCurrentValue
    | TIME_TYPE? IDENTIFIER                                                                                    # timeVariable
    | TIME_TYPE? function                                                                                      # timeFunctionResult
    ;

dateTimeEntity
    : IF logicalExpression THEN dateTimeEntity (ELSEIF logicalExpression THEN dateTimeEntity)* ELSE dateTimeEntity ENDIF # dateTimeDecisionExpression
    | IF LPAREN logicalExpression (COMMA | SEMI) dateTimeEntity ((COMMA | SEMI) logicalExpression (COMMA | SEMI) dateTimeEntity)* (COMMA | SEMI) dateTimeEntity RPAREN # dateTimeFunctionDecisionExpression
    | DATETIME TIME_OFFSET?                                                                                    # dateTimeConstant
    | NOW_DATETIME                                                                                             # dateTimeCurrentValue
    | DATETIME_TYPE? IDENTIFIER                                                                                # dateTimeVariable
    | DATETIME_TYPE? function                                                                                  # dateTimeFunctionResult
    ;

vectorEntity
    : LBRACKET allEntityTypes (COMMA allEntityTypes)* RBRACKET # vectorOfEntities
    | VECTOR_TYPE? IDENTIFIER                                  # vectorVariable
    ;

vectorOfVariables
    : LBRACKET IDENTIFIER (COMMA IDENTIFIER)* RBRACKET
    ;

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
MODULUS       : '|' ;
EXCLAMATION   : '!' ;
EXPONENTIATION: '^' ;
ROOT          : 'root' | '\u221A' ;
SQRT          : 'sqrt' ;
BINARY_LOGARITHM  : 'lb' ;
NATURAL_LOGARITHM : 'ln' ;
COMMON_LOGARITHM  : 'log10' ;
LOGARITHM         : 'log' ;
SUMMATION         : 'S[' ;
PRODUCT_SEQUENCE  : 'P[' ;
SUMMATION_VARIABLE        : 'S' ;
PRODUCT_SEQUENCE_VARIABLE : 'P' ;

PI    : 'pi' | 'PI' ;
EULER : 'E' ;
DEGREE : '\u00B0' | 'deggre' | 'deggres' ;

GT  : '>' ;
GE  : '>=' ;
LT  : '<' ;
LE  : '<=' ;
EQ  : '=' ;
NEQ : '!=' | '<>' ;
NOT : '~' | '\u00AC' ;

// Rounding tokens
R_UP          : 'up' ;
R_DOWN        : 'down' ;
R_CEILING     : 'ceiling' ;
R_FLOOR       : 'floor' ;
R_HALF_UP     : 'halfUp' ;
R_HALF_DOWN   : 'halfDown' ;
R_HALF_EVEN   : 'halfEven' ;
R_UNNECESSARY : 'unnecessary' ;

// Date/time unit tokens
DAY_UNIT    : 'days' | 'day' ;
MONTH_UNIT  : 'months' | 'month' ;
YEAR_UNIT   : 'years' | 'year' ;
HOUR_UNIT   : 'hours' | 'hour' ;
MINUTE_UNIT : 'minutes' | 'minute' ;
SECOND_UNIT : 'seconds' | 'second' ;

// Set-field operator
SET_FIELD_OP : '@' ;

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
CONTAINS           : 'contains' ;
CACHE_FUNCTION_PREFIX : '$.' ;

// Literals
IDENTIFIER : IdentifierText ;
STRING     : '\'' (~'\'')* '\'' ;
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
fragment TimeOffsetFragment  : ('+' | '-') '0'..'2' [0-9] ':' [0-9] [0-9] ;

// Comments and whitespace
COMMENT
    : ( '//' ~[\r\n]* '\r'? '\n'
      | '/*' .*? '*/'
      | EOF
      ) -> skip
    ;

WS : [ \r\t\u000C\n]+ -> channel(HIDDEN) ;

/* ########################################  Grammar rules  ######################################## */

start : mathStart | logicalStart ;

mathStart
    : (assignmentExpression)* mathExpression EOF
    ;

logicalStart
    : (assignmentExpression)* logicalExpression? EOF
    ;

assignmentExpression
    : IDENTIFIER EQ allEntityTypes SEMI                        # assignOperation
    | vectorOfVariables EQ (vectorEntity | function) SEMI      # destructuringAssignment
    ;

// Logical expression — precedence expressed via alternative ordering:
// NOT (highest) > NAND/NOR/XOR/XNOR > AND > OR (lowest among binary logical)
logicalExpression
    : (NOT | EXCLAMATION) logicalExpression                             # notExpression
    | logicalExpression (NAND | NOR | XOR | XNOR) logicalExpression    # logicExpression
    | logicalExpression AND logicalExpression                           # logicExpression
    | logicalExpression OR logicalExpression                            # logicExpression
    | logicalExpression comparisonOperator logicalExpression            # logicComparisonExpression
    | mathExpression comparisonOperator mathExpression                  # comparisonMathExpression
    | stringEntity comparisonOperator stringEntity                      # stringExpression
    | dateOperation comparisonOperator dateOperation                    # dateExpression
    | timeOperation comparisonOperator timeOperation                    # timeExpression
    | dateTimeOperation comparisonOperator dateTimeOperation            # dateTimeExpression
    | LPAREN logicalExpression RPAREN                                   # logicalParenthesis
    | logicalEntity                                                     # logicalValue
    ;

mathExpression
    : MINUS mathExpression                                              # unaryMinusExpression
    | MINUS LPAREN mathExpression RPAREN                                # negateMathParenthesis
    | LPAREN mathExpression RPAREN                                      # mathParenthesis
    | mathExpression PERCENT                                            # percentExpression
    | mathExpression EXCLAMATION                                        # factorialExpression
    | mathExpression ROOT mathExpression                                # rootExpression
    | SQRT LPAREN mathExpression RPAREN                                 # squareRootExpression
    | <assoc=right> mathExpression EXPONENTIATION mathExpression        # exponentiationExpression
    | mathExpression (MULT | DIV | PERCENT) mathExpression              # multiplicationExpression
    | mathExpression (PLUS | MINUS) mathExpression                      # sumExpression
    | MODULUS mathExpression MODULUS                                    # modulusExpression
    | mathExpression DEGREE                                             # degreeExpression
    | mathSpecificFunction                                              # mathSpecificExpression
    | numericEntity                                                     # numberValue
    ;

mathSpecificFunction
    : logarithmFunction   # logarithmExpression
    | roundingFunction    # roundingExpression
    | sequenceFunction    # sequenceExpression
    ;

logarithmFunction
    : (BINARY_LOGARITHM | NATURAL_LOGARITHM | COMMON_LOGARITHM) LPAREN mathExpression RPAREN # fixedLogarithm
    | LOGARITHM LPAREN mathExpression COMMA mathExpression RPAREN                             # variableLogarithm
    ;

roundingFunction
    : (R_UP | R_DOWN | R_CEILING | R_FLOOR | R_HALF_UP | R_HALF_DOWN | R_HALF_EVEN | R_UNNECESSARY)
      LPAREN mathExpression COMMA mathExpression RPAREN
    ;

sequenceFunction
    : (SUMMATION | PRODUCT_SEQUENCE) vectorEntity RBRACKET LPAREN mathExpression RPAREN
    ;

// Date/time unit rules
dateUnit : mathExpression (DAY_UNIT | MONTH_UNIT | YEAR_UNIT) ;
timeUnit : mathExpression (HOUR_UNIT | MINUTE_UNIT | SECOND_UNIT) ;

dateOperation
    : LPAREN dateOperation RPAREN                                                                  # dateParenthesis
    | dateEntity ((PLUS | MINUS) dateUnit | SET_FIELD_OP dateUnit)*                                # dateFunction
    ;

timeOperation
    : LPAREN timeOperation RPAREN                                                                  # timeParenthesis
    | timeEntity ((PLUS | MINUS) timeUnit | SET_FIELD_OP timeUnit)*                                # timeFunction
    ;

dateTimeOperation
    : LPAREN dateTimeOperation RPAREN                                                              # dateTimeParenthesis
    | dateTimeEntity ((PLUS | MINUS) (dateUnit | timeUnit) | SET_FIELD_OP (dateUnit | timeUnit))* # dateTimeFunction
    ;

function
    : CACHE_FUNCTION_PREFIX? IDENTIFIER LPAREN (allEntityTypes ((COMMA | SEMI) allEntityTypes)*)* RPAREN
    ;

comparisonOperator
    : GT | GE | LT | LE | EQ | NEQ
    ;

logicalOperator
    : AND | OR | NAND | NOR | XOR | XNOR
    ;

allEntityTypes
    : mathExpression
    | logicalExpression
    | dateOperation
    | timeOperation
    | dateTimeOperation
    | stringEntity
    | vectorEntity
    ;

logicalEntity
    : (TRUE | FALSE)                                                                                           # logicalConstant
    | IF logicalExpression THEN logicalExpression (ELSEIF logicalExpression THEN logicalExpression)? ELSE logicalExpression ENDIF # logicalDecisionExpression
    | IF LPAREN logicalExpression (COMMA | SEMI) logicalExpression ((COMMA | SEMI) logicalExpression (COMMA | SEMI) logicalExpression)* (COMMA | SEMI) logicalExpression RPAREN # logicalFunctionDecisionExpression
    | BOOLEAN_TYPE? function                                                                                   # logicalFunctionResult
    | BOOLEAN_TYPE? IDENTIFIER                                                                                 # logicalVariable
    ;

numericEntity
    : IF logicalExpression THEN mathExpression (ELSEIF logicalExpression THEN mathExpression)? ELSE mathExpression ENDIF # mathDecisionExpression
    | IF LPAREN logicalExpression (COMMA | SEMI) mathExpression ((COMMA | SEMI) logicalExpression (COMMA | SEMI) mathExpression)* (COMMA | SEMI) mathExpression RPAREN # mathFunctionDecisionExpression
    | EULER                                                                                                    # eulerConstant
    | PI                                                                                                       # piConstant
    | SUMMATION_VARIABLE                                                                                       # summationVariable
    | PRODUCT_SEQUENCE_VARIABLE                                                                                # productSequenceVariable
    | NUMBER                                                                                                   # numericConstant
    | NUMBER_TYPE? function                                                                                    # numericFunctionResult
    | NUMBER_TYPE? IDENTIFIER                                                                                  # numericVariable
    ;

stringEntity
    : IF logicalExpression THEN stringEntity (ELSEIF logicalExpression THEN stringEntity)? ELSE stringEntity ENDIF # stringDecisionExpression
    | IF LPAREN logicalExpression (COMMA | SEMI) stringEntity ((COMMA | SEMI) logicalExpression (COMMA | SEMI) stringEntity)* (COMMA | SEMI) stringEntity RPAREN # stringFunctionDecisionExpression
    | STRING                                                                                                    # stringConstant
    | STRING_TYPE? function                                                                                    # stringFunctionResult
    | STRING_TYPE? IDENTIFIER                                                                                  # stringVariable
    ;

dateEntity
    : IF logicalExpression THEN dateOperation (ELSEIF logicalExpression THEN dateOperation)? ELSE dateOperation ENDIF # dateDecisionExpression
    | IF LPAREN logicalExpression (COMMA | SEMI) dateOperation ((COMMA | SEMI) logicalExpression (COMMA | SEMI) dateOperation)* (COMMA | SEMI) dateOperation RPAREN # dateFunctionDecisionExpression
    | DATE                                                                                                     # dateConstant
    | NOW_DATE                                                                                                 # dateCurrentValue
    | DATE_TYPE? IDENTIFIER                                                                                    # dateVariable
    | DATE_TYPE? function                                                                                      # dateFunctionResult
    ;

timeEntity
    : IF logicalExpression THEN timeOperation (ELSEIF logicalExpression THEN timeOperation)? ELSE timeOperation ENDIF # timeDecisionExpression
    | IF LPAREN logicalExpression (COMMA | SEMI) timeOperation ((COMMA | SEMI) logicalExpression (COMMA | SEMI) timeOperation)* (COMMA | SEMI) timeOperation RPAREN # timeFunctionDecisionExpression
    | TIME                                                                                                     # timeConstant
    | NOW_TIME                                                                                                 # timeCurrentValue
    | TIME_TYPE? IDENTIFIER                                                                                    # timeVariable
    | TIME_TYPE? function                                                                                      # timeFunctionResult
    ;

dateTimeEntity
    : IF logicalExpression THEN dateTimeOperation (ELSEIF logicalExpression THEN dateTimeOperation)? ELSE dateTimeOperation ENDIF # dateTimeDecisionExpression
    | IF LPAREN logicalExpression (COMMA | SEMI) dateTimeOperation ((COMMA | SEMI) logicalExpression (COMMA | SEMI) dateTimeOperation)* (COMMA | SEMI) dateTimeOperation RPAREN # dateTimeFunctionDecisionExpression
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

# Análise da Gramática `ExpressionEvaluatorV2.g4`

**Data**: 2026-03-12
**Arquivo analisado**: `exp-eval-mk2/src/main/resources/ExpressionEvaluatorV2.g4`
**Runtime ANTLR**: 4.13.1

---

## Resumo por Severidade

| # | Severidade | Problema |
|---|-----------|----------|
| 1 | CRÍTICO | Combined grammar — deveria ser split |
| 2 | CRÍTICO | `PERCENT` com duplo significado — ambiguidade real |
| 3 | CRÍTICO | `EOF` como alternativa em `COMMENT` — bug semântico |
| 4 | ALTO | `EULER : 'E'` — colisão silenciosa com identificadores |
| 5 | ALTO | Ausência de fallback lexical (`ANY : . ;`) |
| 6 | ALTO | Typo em `DEGREE`: `'deggre'` / `'deggres'` |
| 7 | ALTO | `logicalExpression?` opcional cria problemas de null |
| 8 | ALTO | `S[` e `P[` incorporados ao token — sintaxe confusa |
| 9 | MÉDIO | Três alternativas com label `#logicExpression` |
| 10 | MÉDIO | `logicComparisonExpression` semanticamente vago |
| 11 | MÉDIO | `allEntityTypes` com alta ambiguidade de lookahead |

---

## Problemas Detalhados

### [CRÍTICO] 1. Combined Grammar — Deveria ser Split

A gramática usa `grammar ExpressionEvaluatorV2;` (combined), misturando Lexer e Parser no mesmo arquivo. Para uma gramática desta complexidade (~330 linhas, múltiplos contextos semânticos), isso impede clareza nos erros e dificulta manutenção.

**Solução**: Separar em `ExpressionEvaluatorV2Lexer.g4` e `ExpressionEvaluatorV2Parser.g4`.

---

### [CRÍTICO] 2. `PERCENT` com Duplo Significado — Ambiguidade Real

```antlr
// Linha 196: postfix (percentual de um valor)
| mathExpression PERCENT   # percentExpression

// Linha 201: binary (módulo/resto?)
| mathExpression (MULT | DIV | PERCENT) mathExpression  # multiplicationExpression
```

O token `%` aparece em dois contextos incompatíveis na mesma regra `mathExpression`. Se o usuário escreve `10 % 3`, o parser não consegue distinguir entre "10% de 3" e "10 mod 3". É uma ambiguidade léxico-semântica que o ANTLR resolve pelo ordinal da alternativa, silenciosamente escolhendo errado.

**Solução**: Usar tokens distintos — ex.: `PERCENT` para postfix e `MODULO` para o operador binário.

---

### [CRÍTICO] 3. `EOF` como Alternativa do Token `COMMENT` — Bug Semântico

```antlr
// Linhas 149-154
COMMENT
    : ( '//' ~[\r\n]* '\r'? '\n'
      | '/*' .*? '*/'
      | EOF          // ← problema aqui
      ) -> skip
    ;
```

O objetivo provavelmente é tratar comentários `//` sem newline no final do arquivo. Porém, `EOF` como alternativa de um token com `-> skip` consome e descarta o token de fim-de-stream, o que pode interferir com as regras do parser que dependem explicitamente de `EOF` (`mathStart` e `logicalStart`).

**Solução correta** para comentários sem newline no EOF:

```antlr
LINE_COMMENT : '//' ~[\r\n]* -> skip ;
BLOCK_COMMENT: '/*' .*? '*/' -> skip ;
```

Sem a alternativa `| EOF`.

---

### [ALTO] 4. `EULER : 'E'` — Colisão com Identificador

```antlr
EULER : 'E' ;
```

O token `E` (maiúsculo, sozinho) é reservado para o número de Euler. Qualquer variável chamada `E` vira a constante matemática. O mesmo problema existe com `PI : 'pi' | 'PI'`. Como o ANTLR escolhe o token definido primeiro (quando há empate de comprimento), o usuário nunca consegue usar `E`, `pi` ou `PI` como nomes de variáveis.

Isso é uma restrição documentada ou um bug silencioso? Se for restrição, precisa estar documentada e gerar um erro claro.

---

### [ALTO] 5. Ausência de Regra de Fallback Lexical

```antlr
// Não existe nenhuma regra do tipo:
// ANY : . ;
```

Se o usuário digita um caractere inválido, o ANTLR lança um erro genérico de reconhecimento sem contexto. Uma regra `ERROR_CHAR : . ;` no final permite capturar caracteres inesperados com mensagens de erro controladas via `BaseErrorListener`.

---

### [ALTO] 6. Typo nos Literais do Token `DEGREE`

```antlr
// Linha 67
DEGREE : '\u00B0' | 'deggre' | 'deggres' ;
```

`'deggre'` e `'deggres'` parecem typos de `'degree'` e `'degrees'` (duplo `g`). Se for um alias intencional, não há comentário explicando. Se for typo, o token correto nunca é reconhecido quando o usuário escreve `degree`.

---

### [ALTO] 7. `logicalExpression?` Opcional — Problema de Null no Visitor

```antlr
logicalStart
    : (assignmentExpression)* logicalExpression? EOF
    ;
```

O `?` torna `logicalExpression` opcional. Uma entrada contendo apenas assignments (ou até vazia) é um `logicalStart` válido. Isto provavelmente resultará em `null` retornado pelo visitor/listener para o campo de expressão, exigindo null-checks defensivos em todo o código Java de visita.

---

### [ALTO] 8. Design Confuso de `SUMMATION` / `PRODUCT_SEQUENCE`

```antlr
SUMMATION         : 'S[' ;
PRODUCT_SEQUENCE  : 'P[' ;
```

O `[` é incorporado ao token. Então a regra:

```antlr
sequenceFunction
    : (SUMMATION | PRODUCT_SEQUENCE) vectorEntity RBRACKET LPAREN mathExpression RPAREN
    ;
```

Para usar um vetor literal, o usuário precisa escrever `S[[1,2,3]](expr)` — dois colchetes de abertura — porque `vectorEntity` exige seu próprio `LBRACKET`. É uma sintaxe não-intuitiva e difícil de comunicar em documentação.

---

### [MÉDIO] 9. Três Alternativas com Label Idêntico em `logicalExpression`

```antlr
| logicalExpression (NAND | NOR | XOR | XNOR) logicalExpression    # logicExpression
| logicalExpression AND logicalExpression                           # logicExpression
| logicalExpression OR logicalExpression                            # logicExpression
```

Todas chamam `#logicExpression`. O ANTLR gera um único método `visitLogicExpression()` / `enterLogicExpression()`. Para distinguir AND de XOR de OR dentro do visitor, é preciso inspecionar o token filho manualmente — perde-se a elegância do visitor pattern.

**Solução**: Labels únicos (ex.: `#andExpression`, `#orExpression`, `#bitwiseLogicExpression`) geram métodos distintos.

---

### [MÉDIO] 10. `logicComparisonExpression` é Semanticamente Duvidoso

```antlr
| logicalExpression comparisonOperator logicalExpression   # logicComparisonExpression
```

Isso permite `true > false` ou `(a and b) = (c or d)`, comparações entre expressões booleanas com operadores relacionais. Abre espaço para expressões sem sentido que o parser aceita silenciosamente.

---

### [MÉDIO] 11. `allEntityTypes` — Alta Ambiguidade de Lookahead

```antlr
allEntityTypes
    : mathExpression
    | logicalExpression
    | dateOperation
    | timeOperation
    | dateTimeOperation
    | stringEntity
    | vectorEntity
    ;
```

`mathExpression` pode ser atingida via `logicalExpression` (ex.: uma expressão matemática é um caso especial de uma lógica via `comparisonMathExpression`). Isso força o ANTLR a usar `LL(*)` com lookahead profundo ao resolver `allEntityTypes` — é um ponto de potencial explosão de performance em expressões aninhadas complexas.

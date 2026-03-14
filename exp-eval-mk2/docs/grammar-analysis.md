# Análise da Gramática `ExpressionEvaluatorV2.g4`

**Data**: 2026-03-12
**Arquivo analisado**: `exp-eval-mk2/src/main/resources/ExpressionEvaluatorV2.g4`
**Runtime ANTLR**: 4.13.1

---

## Problemas Detalhados

### [CRÍTICO] 2. `PERCENT` com Duplo Significado — Ambiguidade Real

```antlr
// Linha 196: postfix (percentual de um valor)
| mathExpression PERCENT   # percentExpression

// Linha 201: binary (módulo/resto?)
| mathExpression (MULT | DIV | PERCENT) mathExpression  # multiplicationExpression
```

O token `%` aparece em dois contextos incompatíveis na mesma regra `mathExpression`. Se o usuário escreve `10 % 3`, o parser não consegue distinguir entre "10% de 3" e "10 mod 3". É uma ambiguidade léxico-semântica que o ANTLR resolve pelo ordinal da alternativa, silenciosamente escolhendo errado.

**Solução**: Usar tokens distintos — ex.: `PERCENT` para postfix e `MODULO` (mod) para o operador binário.

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

Solução: built-ins semânticos com fallback em IDENTIFIER. Isso costuma ser o desenho mais flexível porque:
- não bloqueia nomes do usuário;
- mantém a sintaxe curta para constantes conhecidas;
- permite uma regra explícita de precedência, por exemplo “variável do usuário sombreia built-in”.


---

### [ALTO] 5. Ausência de Regra de Fallback Lexical

```antlr
// Não existe nenhuma regra do tipo:
// ANY : . ;
```

Se o usuário digita um caractere inválido, o ANTLR lança um erro genérico de reconhecimento sem contexto. Uma regra `ERROR_CHAR : . ;` no final permite capturar caracteres inesperados com mensagens de erro controladas via `BaseErrorListener`.

Solução: adicione o fallback 'ERROR_CHAR' ao final para capturar caracteres inesperados.

---

### [ALTO] 6. Typo nos Literais do Token `DEGREE`

```antlr
// Linha 67
DEGREE : '\u00B0' | 'deggre' | 'deggres' ;
```

`'deggre'` e `'deggres'` parecem typos de `'degree'` e `'degrees'` (duplo `g`). Se for um alias intencional, não há comentário explicando. Se for typo, o token correto nunca é reconhecido quando o usuário escreve `degree`.

solução: corrigir typo

---

### [ALTO] 7. `logicalExpression?` Opcional — Problema de Null no Visitor

```antlr
logicalStart
    : (assignmentExpression)* logicalExpression? EOF
    ;
```

O `?` torna `logicalExpression` opcional. Uma entrada contendo apenas assignments (ou até vazia) é um `logicalStart` válido. Isto provavelmente resultará em `null` retornado pelo visitor/listener para o campo de expressão, exigindo null-checks defensivos em todo o código Java de visita.

Solução: logicalExpression é opcional, mas se ele não existir, pelo menos um assignmentExpression deve existir

---

### [MÉDIO] 9. Três Alternativas com Label Idêntico em `logicalExpression`

```antlr
| logicalExpression (NAND | NOR | XOR | XNOR) logicalExpression    # logicExpression
| logicalExpression AND logicalExpression                           # logicExpression
| logicalExpression OR logicalExpression                            # logicExpression
```

Todas chamam `#logicExpression`. O ANTLR gera um único método `visitLogicExpression()` / `enterLogicExpression()`. Para distinguir AND de XOR de OR dentro do visitor, é preciso inspecionar o token filho manualmente — perde-se a elegância do visitor pattern.

**Solução**: Labels únicos (ex.: `#andExpression`, `#orExpression`, `#bitwiseLogicExpression`) geram métodos distintos.

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

# Referência da linguagem

Esta é a referência da linguagem MK3. A gramática reconhece a forma; o resolvedor define tipos,
nulidade, vínculos e validade semântica antes da criação do plano.

## Arquivo de expressão

Um arquivo contém zero ou mais atribuições terminadas em `;` e um resultado final opcional. Um arquivo
sem atribuição nem resultado é sintaticamente válido, mas recebe `SEMANTIC_EMPTY_EXPRESSION`.

```text
name := expression;
[first, second] := collectionExpression;
resultExpression
```

A desestruturação lê o prefixo de uma coleção ordenada. Elementos excedentes são ignorados; uma coleção
curta falha. Uma atribuição pode sombrear símbolo externo e produz aviso.

<!-- runestone-example: NUMBER=15 -->
```runestone
base := 10;
increment := 5;
base + increment
```

## Tipos e valores

Toda expressão aceita tem tipo conhecido na compilação.

| Tipo | Literais/forma Java canônica | Observações |
|---|---|---|
| `NUMBER` | `1`, `1.25` / `BigDecimal` | semântica decimal; inteiros fonte são decimais |
| `BOOLEAN` | `true`, `false` / `Boolean` | exigido por predicados |
| `STRING` | `"text"` / `String` | escapes `b t n f r " ' \\` |
| `DATE` | `d"2026-09-27"` / `LocalDate` | data sem fuso |
| `TIME` | `t"10:30"`, `t"10:30:45"` / `LocalTime` | segundos opcionais |
| `DATETIME` | `dt"2026-09-27T10:30:00"` / `LocalDateTime` | offset explícito é normalizado |
| coleção | `[1, 2, 3]` / lista imutável | elementos possuem um único tipo conhecido |
| mapa | valor externo com chaves textuais | não possui literal fonte |
| objeto | tipo Java nominal registrado | só membros registrados são navegáveis |

Não existem literal `null`, tipo dinâmico, inteiros hexadecimal/octal ou coerção implícita entre
operandos. `[]` precisa receber tipo do contexto. `currDate`, `currTime` e `currDateTime` são nomes
reservados e usam um único instante do `Clock` por execução, truncado em segundos.

<!-- runestone-example: DATE=2026-09-27 -->
```runestone
d"2026-09-27"
```

## Precedência e avaliação

Da menor para a maior precedência. As IDs são casos executáveis de AST; operadores no mesmo nível são
associativos à esquerda, exceto `^`, que é associativo à direita. `??`, `or` e `and` são lazy;
os demais operadores booleanos são eager e preservam ordem esquerda→direita.

| Caso AST | Nível | Operadores/construções | Associatividade | Expressão do caso |
|---|---:|---|---|---|
| `PREC-01` | 1 | `??` | esquerda, lazy | `a ?? b ?? c` |
| `PREC-02` | 2 | `or` | esquerda, lazy | `a or b or c` |
| `PREC-03` | 3 | `and` | esquerda, lazy | `a and b and c` |
| `PREC-04` | 4 | `> >= < <= = <>`, `in`, `not in`, `nin`, `between`, `=~`, `!~` | não encadeável | `a = b xor c` |
| `PREC-05` | 5 | `nand nor xor xnor` | esquerda, eager | `a xor b xor c` |
| `PREC-06` | 6 | `&#124;&#124;` | esquerda | `a &#124;&#124; b &#124;&#124; c` |
| `PREC-07` | 7 | `+ -` | esquerda | `a - b - c` |
| `PREC-08` | 8 | `* / mod` | esquerda | `a / b / c` |
| `PREC-09` | 9 | `-` unário, `~`, `¬`, `!` prefixo | prefixo | `-a root b` |
| `PREC-10` | 10 | `root`, `√` | esquerda | `a root b root c` |
| `PREC-11` | 11 | `^` | direita | `a ^ b ^ c` |
| `PREC-12` | 12 | `%`, `!` pós-fixos | esquerda, na ordem fonte | `a%!` |
| `PREC-13` | 13 | primários, grupos, chamadas e navegação | n/a | `(a)` |

Parênteses tornam o agrupamento explícito. Comparações não podem ser encadeadas: escreva
`a < b and b < c`.

<!-- runestone-example: NUMBER=512 -->
```runestone
2 ^ 3 ^ 2
```

### Semântica dos operadores

- `+`, `-`, `*`, `/` e `mod` operam em números decimais. Soma/subtração são exatas; operações que
  exigem arredondamento usam o `MathContext` do Ambiente. Divisão por zero é indefinida.
- `^` e `root`/`√` aceitam somente resultados reais definidos. Expoentes e raízes que exigiriam número
  complexo, `0 ^ 0`, raiz de índice zero e demais casos indefinidos recebem diagnóstico.
- `%` pós-fixo divide por 100. `!` pós-fixo calcula fatorial de inteiro não negativo até o limite do
  Ambiente. `-`, `~`/`¬`/`!` prefixo são negação numérica e lógica.
- `>`, `>=`, `<`, `<=` comparam escalares ordenáveis do mesmo tipo. `=`/`<>` usam igualdade estrutural
  tipada; números com escalas diferentes comparam pelo valor decimal.
- `between low and high` é inclusivo. `in`, `not in` e `nin` procuram elemento compatível em coleção ou
  chave textual em mapa.
- `and`/`or` fazem curto-circuito. `nand`, `nor`, `xor`, `xnor` avaliam ambos os operandos.
- `||` concatena textos; `=~` e `!~` testam o texto completo segundo o contrato RE2/J implementado.
- `??` devolve o primeiro operando não nulo e não avalia os seguintes depois de encontrá-lo.

Operadores avaliam operandos alcançados da esquerda para a direita. Não há coerção de borda implícita
entre valores internos: os operandos já precisam ter tipos compatíveis.

## Condicionais, funções e coleções

As duas formas de condicional têm a mesma semântica e exigem ramos de tipo compatível:

```text
if condition then value elsif condition then value else value endif
if(condition, value, condition, value, elseValue)
if(condition; value; condition; value; elseValue)
```

Comentários de linha usam `//`; comentários de bloco usam `/* ... */`.

Chamadas globais usam `name(args)`. Operações oficiais de coleção usam navegação:
`all`, `any`, `avg`, `count`, `keys`, `map`, `reduce`, `sortBy`, `sum` e `values`. Lambdas usam
`@ -> expression`; `@` é válido somente dentro de filtro ou lambda. Em mapas, `@.k` é a chave e
`@.v` o valor; em `reduce`, `@.accumulator` e `@.item` formam o item contextual.

<!-- runestone-example: NUMBER=12 -->
```runestone
values := [1, 2, 3];
values.map(@ -> @ * 2).sum()
```

## Navegação

Uma cadeia pode conter propriedade (`order.total`), chamada (`order.total()`), índice (`items[0]`),
chave textual (`map["key"]`), slice (`items[-2:]`), filtro (`items[?(@ > 0)]`) e curinga (`items[*]`).
Índices negativos contam a partir do fim; slices prendem limites ao tamanho e podem ser vazios. Índice
e chave estritos falham quando ausentes. A única grafia de curinga é `[*]`; propriedade não lê chave de
mapa.

O prefixo seguro `?.` pertence a **um elo**: `customer?.name` tolera receptor nulo ou ausência legítima
naquele elo. Ele não mascara membro desconhecido, receptor incompatível, falha de accessor/predicado ou
limite. Também não se propaga: `customer?.address.city` é inválido se o elo seguinte puder receber null;
use `customer?.address?.city` ou descarregue a nulidade com `??`.

## Nulidade de runtime

Null não pode entrar por símbolo externo, coleção, mapa, provider ou resultado público. Somente
navegação segura produz um valor possivelmente nulo. Resultado, atribuição, operando, argumento,
predicado ou receptor estrito exigem valor não nulo. `a ?? fallback` avalia da esquerda para a direita e
é a forma explícita de eliminar essa possibilidade.

## Temporais

Literal `DATETIME` sem offset é interpretado no `ZoneId` do ambiente. Com offset, representa um instante
que é normalizado para `LocalDateTime` no mesmo fuso; o offset original continua metadata semântica.
Valores correntes vêm do `Clock` da Engine, enquanto o fuso vem do Ambiente.

## Regex Linear

`=~`, `!~` e os built-ins que recebem regex usam exclusivamente RE2/J. Não existe fallback para
`java.util.regex`. Lookaround, backreference e demais construções fora do subset RE2/J recebem
diagnóstico estruturado. O padrão dos operadores deve ser literal e obedece `maxRegexPatternLength`.
Regex linear evita backtracking catastrófico, mas não substitui limites de tamanho nem deadline externo.

<!-- runestone-example: BOOLEAN=true -->
```runestone
"invoice-42" =~ "invoice-[0-9]+"
```

## Resíduos e armadilhas da gramática

- desigualdade é somente `<>`; `!=` não existe;
- atribuição é `:=`, enquanto `=` é igualdade e `:` aparece somente em slices;
- `5!~"x"` é tokenizado como `5 !~ "x"`; se a intenção for fatorial seguido de regex, use espaço;
- `abs(x)` e `sqrt(x)` são funções, não construções da gramática;
- `|x|`, `.*`, `..`, literal `null`, índices decimais, hexadecimal e octal não pertencem à linguagem;
- `[-10:20]` e `[]` são válidos; `t"10:20"` evita colisão entre hora e slice;
- `in`, `not in` e `nin` testam elemento de coleção ou chave textual de mapa, nunca substring;
- `->` existe apenas no argumento lambda e comparações não são encadeáveis.

Falhas dessas formas são diagnósticos normais de parsing/semântica; não há migrador nem diagnóstico
especial para versões anteriores.

# Expression Syntax

## Literals

| Type | Examples | Notes |
|---|---|---|
| Number | `42`, `3.14`, `0xFF`, `007` | All numeric literals become `BigDecimal` |
| String | `"hello"`, `'world'` | Single or double quotes |
| Boolean | `true`, `false` | |
| Date | `2024-01-31` | `YYYY-MM-DD` |
| Time | `14:30`, `14:30:00` | `HH:MM` or `HH:MM:SS` |
| DateTime | `2024-01-31T14:30`, `2024-01-31 14:30:00+03:00` | Timezone offset is optional |
| Current date/time | `currDate`, `currTime`, `currDateTime` | Evaluated once per `compute()` call |
| Vector | `[1, 2, 3]`, `["a", "b"]` | At least one element required — `[]` is invalid |
| Null | `null` | |

> [!WARNING]
> Empty vectors `[]` are rejected by the parser. Vectors require at least one element.

> [!NOTE]
> `currDate`, `currTime`, and `currDateTime` are literals, not variables. `<date>currDate` is invalid syntax — the `<date>` cast prefix only applies to variable references and function calls. Use `d = currDate;` directly.

## Arithmetic Operators

| Operator | Description | Example |
|---|---|---|
| `+` | Addition | `a + b` |
| `-` | Subtraction | `a - b` |
| `*` | Multiplication | `a * b` |
| `/` | Division | `a / b` |
| `mod` | Modulo (remainder) | `10 mod 3` → `1` |
| `%` | Percentage (postfix) | `50%` → `0.5` |
| `^` | Exponentiation | `2 ^ 10` → `1024` |
| `root` / `√` | N-th root (infix) | `8 root 3` → `2` |
| `\|expr\|` | Absolute value | `\|-5\|` → `5` |

> [!WARNING]
> `%` is **percentage**, not modulo. `50%` equals `0.5`. For integer remainder, use `mod`: `10 mod 3 = 1`.

**Precedence** (high to low): postfix (`%`) → exponentiation (`^`) → root → unary minus → multiplicative (`*`, `/`, `mod`) → additive (`+`, `-`)

**Square root**: `sqrt(x)` is available as a function. `root` and `√` are infix operators: `a root b` computes the b-th root of a.

## Comparison and Logical Operators

| Operator | Description |
|---|---|
| `=` | Equals |
| `!=`, `<>` | Not equals |
| `>`, `>=`, `<`, `<=` | Ordered comparison |
| `and` | Logical AND |
| `or` | Logical OR |
| `not`, `~`, `¬`, `!` | Logical NOT |
| `xor` | Exclusive OR |
| `xnor` | Logical equivalence |
| `nand` | NOT AND |
| `nor` | NOT OR |
| `between` / `not between` | Range check (inclusive) |
| `in` / `not in` | Set membership |
| `=~` | Regex match |
| `!~` | Regex non-match |

```
age between 18 and 65
status in ["active", "pending"]
email =~ '[a-z]+@[a-z]+\.[a-z]+'
```

## String Operators

| Operator | Description | Example |
|---|---|---|
| `\|\|` | String concatenation | `"Hello" \|\| " " \|\| name` |

> [!WARNING]
> `||` only accepts string operands. Trying `1 || "b"` fails at parse time.

## Null Coalescing and Safe Navigation

| Operator | Description | Example |
|---|---|---|
| `??` | Return right side if left is null | `name ?? "Anonymous"` |
| `?.` | Null-safe property access | `customer?.address?.city` |

The `??` fallback accepts full arithmetic or logical expressions on the right side — no need for `if()`:

```
price ?? base + markup       // "if price is null, return base + markup"
flag ?? (x > 0)              // "if flag is null, evaluate x > 0"
```

For date/time/datetime/vector operands, the fallback is restricted to a literal, variable reference, or `if()`.

## Conditional Expressions

Two forms — both are equivalent:

```
// Functional form (comma or semicolon as separator)
if(qty >= 100; price * 0.85; price)
if(qty >= 100, price * 0.85, price)

// Block form
if qty >= 100 then price * 0.85 else price endif

// With elsif
if score >= 90 then "A"
elsif score >= 80 then "B"
elsif score >= 70 then "C"
else "F"
endif
```

## Type Casting

Cast between types explicitly when the compiler cannot infer the target:

| Cast | Description |
|---|---|
| `<number>(expr)` | Convert to `BigDecimal` |
| `<text>(expr)` | Convert to `String` |
| `<bool>(expr)` | Convert to `Boolean` |
| `<date>(expr)` | Convert to `LocalDate` |
| `<time>(expr)` | Convert to `LocalTime` |
| `<datetime>(expr)` | Convert to `LocalDateTime` |
| `<vector>(expr)` | Convert to list |

Cast works with variables and function results:

```
<text>(totalAmount)      // variable to string
<bool>isActive           // variable (short form without parentheses)
<number>(someString)     // string to number
```

## Assignment Expressions

Used with `AssignmentExpression`. Each statement assigns a value to a variable; later statements can reference earlier ones. The result is a `Map<String, Object>` with all assigned variables.

```
base = price * qty;
tax = base * taxRate;
discount = if(qty > 10; base * 0.05; 0);
total = base + tax - discount
```

**Destructuring:**

```
[a, b, c] = [1, 2, 3];
sum = a + b + c
```

## Comments

```
// Single-line comment
a + b  // inline comment

/* Multi-line
   comment block */
a + b * 2
```

Comments are stripped by the lexer and can appear anywhere in the expression, including inside assignment blocks.

## Function Calls

```
sqrt(9)
round(price, 2)
substring(name, 0, 5)
mean([10, 20, 30])
```

Functions are called with parentheses and comma-separated arguments. The available functions depend on what was registered in `ExpressionEnvironment`. See [Built-in Functions](04-built-in-functions.md) for the full catalog.

## Summary: Operator Precedence

From highest to lowest binding strength:

1. Postfix: `%`, `|...|`
2. Exponentiation: `^`
3. Root: `root`, `√`
4. Unary: `-`, `not`, `~`, `¬`, `!`
5. Multiplicative: `*`, `/`, `mod`
6. Additive: `+`, `-`
7. String concatenation: `||`
8. Comparison: `=`, `!=`, `<>`, `>`, `>=`, `<`, `<=`
9. Range/membership: `between`, `in`
10. Logical: `and`, `nand`
11. Logical: `xor`, `xnor`
12. Logical: `or`, `nor`
13. Null coalescing: `??`
14. Safe navigation: `?.`
15. Conditional: `if...then...else...endif`

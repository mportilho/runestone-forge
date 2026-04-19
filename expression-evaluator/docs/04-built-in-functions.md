# Built-in Functions

Register built-in functions when building `ExpressionEnvironment`. Use `addAllFunctions()` to register everything, or register providers individually if you need a smaller footprint.

## Comparable Functions

Registered by `addComparableFunctions()`.

| Function | Signature | Description | Example |
|---|---|---|---|
| `max` | `max(T[])` | Largest value in a vector | `max([10, 5, 20])` → `20` |
| `min` | `min(T[])` | Smallest value in a vector | `min([10, 5, 20])` → `5` |

> [!NOTE]
> There is no `abs()` or `sign()` function. Absolute value uses the `|expr|` operator: `|-5|` → `5`.

## Math Functions

Registered by `addMathFunctions()`. All functions receive `MathContext` as a first parameter injected by the runtime — it does not appear in expressions.

| Function | Description | Example |
|---|---|---|
| `mean(T[])` | Arithmetic mean | `mean([10, 20, 30])` → `20` |
| `geometricMean(T[])` | Geometric mean | |
| `harmonicMean(T[])` | Harmonic mean | |
| `variance(T[], type)` | Variance; `type` = 0 for population, 1 for sample | |
| `stdDev(T[], type)` | Standard deviation | |
| `meanDev(T[])` | Mean absolute deviation | |
| `sum(T[])` | Sum of elements | `sum([1, 2, 3])` → `6` |
| `sqrt(x)` | Square root | `sqrt(9)` → `3` |
| `pow(base, exp)` | Power | `pow(2, 10)` → `1024` |
| `ceil(x)` | Ceiling | `ceil(3.2)` → `4` |
| `floor(x)` | Floor | `floor(3.8)` → `3` |
| `round(x, scale)` | Round to decimal places | `round(3.456, 2)` → `3.46` |
| `rule3d(a, b, c)` | Direct rule of three: result = b*c/a | |
| `rule3i(a, b, c)` | Inverse rule of three: result = a*b/c | |
| `distribute(total, parts)` | Distribute total into equal parts | |
| `spread(total, weights[])` | Distribute total proportionally to weights | |

## Logarithm Functions

Registered by `addMathFunctions()`. Functions ending in `Fast` use `double` arithmetic.

| Function | Signature | Description |
|---|---|---|
| `ln` | `ln(x)` | Natural logarithm (base e) |
| `lb` | `lb(x)` | Logarithm base 2 |
| `log` | `log(base, value)` | Logarithm to any base |
| `lnFast` | `lnFast(double)` | Natural log using double precision |
| `lbFast` | `lbFast(double)` | Log base 2 using double precision |
| `logFast` | `logFast(double, double)` | Log to any base using double precision |

## Trigonometry Functions

Registered by `addTrigonometryFunctions()`. Also registers constants `pi` / `π`, `e`, and `tau` / `τ` as symbols.

| Function | Description |
|---|---|
| `sin(x)` | Sine |
| `cos(x)` | Cosine |
| `tan(x)` | Tangent |
| `asin(x)` | Arc sine |
| `acos(x)` | Arc cosine |
| `atan(x)` | Arc tangent |
| `atan2(y, x)` | Arc tangent of y/x |
| `sinh(x)` | Hyperbolic sine |
| `cosh(x)` | Hyperbolic cosine |
| `tanh(x)` | Hyperbolic tangent |
| `asinh(x)` | Inverse hyperbolic sine |
| `acosh(x)` | Inverse hyperbolic cosine |
| `atanh(x)` | Inverse hyperbolic tangent |

All trig functions use `transcendentalMathContext` for precision. See [Environment Configuration](02-environment-configuration.md#configuring-math-precision).

## String Functions

Registered by `addStringFunctions()`.

| Function | Description |
|---|---|
| `concat(a, b, ...)` | Concatenate strings |
| `toUpper(s)` | Uppercase |
| `toLower(s)` | Lowercase |
| `trim(s)` | Remove leading and trailing whitespace |
| `trimLeft(s)` | Remove leading whitespace |
| `trimRight(s)` | Remove trailing whitespace |
| `substring(s, begin)` | Substring from index to end |
| `substring(s, begin, end)` | Substring from begin (inclusive) to end (exclusive) |
| `substringBefore(s, sep)` | Part before first occurrence of sep |
| `substringAfter(s, sep)` | Part after first occurrence of sep |
| `substringBeforeLast(s, sep)` | Part before last occurrence of sep |
| `substringAfterLast(s, sep)` | Part after last occurrence of sep |
| `padLeft(s, size)` | Pad with spaces on the left |
| `padLeft(s, size, padding)` | Pad with a custom character on the left |
| `padRight(s, size)` | Pad with spaces on the right |
| `padRight(s, size, padding)` | Pad with a custom character on the right |
| `repeat(s, n)` | Repeat string n times |
| `replace(s, old, new)` | Replace first occurrence |
| `replaceFirst(s, pattern, replacement)` | Replace first regex match |
| `replaceAll(s, pattern, replacement)` | Replace all regex matches |
| `indexOf(s, sub)` | Index of first occurrence (-1 if absent) |
| `lastIndexOf(s, sub)` | Index of last occurrence |
| `startsWith(s, prefix)` | True if s starts with prefix |
| `endsWith(s, suffix)` | True if s ends with suffix |
| `contains(s, sub)` | True if s contains sub |
| `isEmpty(s)` | True if empty string |
| `isBlank(s)` | True if blank (only whitespace) |
| `length(s)` | Character count |
| `split(s, sep)` | Split into a vector |
| `join(sep, parts[])` | Join vector elements with separator |

## Date/Time Functions

Registered by `addDateTimeFunctions()`. All functions accept `Temporal` — either `LocalDate`, `LocalTime`, or `LocalDateTime` — and return the same concrete type they received.

### Duration Between Two Points

| Function | Returns |
|---|---|
| `secondsBetween(a, b)` | `Long` |
| `minutesBetween(a, b)` | `Long` |
| `hoursBetween(a, b)` | `Long` |
| `daysBetween(a, b)` | `Long` |
| `monthsBetween(a, b)` | `Long` |
| `yearsBetween(a, b)` | `Long` |

### Set a Field

| Function | Description |
|---|---|
| `setDay(t, n)` | Set day-of-month to n |
| `setMonth(t, n)` | Set month to n (1–12) |
| `setYear(t, n)` | Set year to n |
| `setHours(t, n)` | Set hour to n |
| `setMinutes(t, n)` | Set minute to n |
| `setSeconds(t, n)` | Set second to n |
| `setMidnight(t)` | Set time component to 00:00:00 |
| `setMidday(t)` | Set time component to 12:00:00 |

### Add and Subtract

| Function | Description |
|---|---|
| `addDay(t, n)` / `subDay(t, n)` | Add/subtract n days |
| `addMonth(t, n)` / `subMonth(t, n)` | Add/subtract n months |
| `addYear(t, n)` / `subYear(t, n)` | Add/subtract n years |
| `addHours(t, n)` / `subHours(t, n)` | Add/subtract n hours |
| `addMinutes(t, n)` / `subMinutes(t, n)` | Add/subtract n minutes |
| `addSeconds(t, n)` / `subSeconds(t, n)` | Add/subtract n seconds |

> [!NOTE]
> There are no parsing or formatting functions (`parse()`, `format()`, `year()`, `month()`, `day()`, etc.). Date/time values come in through bindings as Java `LocalDate`/`LocalTime`/`LocalDateTime` objects, or as date literals in the expression.

## Financial Functions (Excel-compatible)

Registered by `addExcelFunctions()`. All functions receive `MathContext` as their first parameter, injected automatically — it does not appear in expressions.

### Future Value (`fv`)

| Signature | Description |
|---|---|
| `fv(r, n, y, p, t: boolean)` | Rate r, periods n, payment y, present value p, type t |
| `fv(r, nper: int, pmt, pv, type: int)` | As above with int params |
| `fv(r, nper: int, c, pv)` | Type defaults to 0 |

### Present Value (`pv`)

| Signature | Description |
|---|---|
| `pv(r, n, y, f, t: boolean)` | Rate r, periods n, payment y, future value f, type t |

### Net Present Value (`npv`)

| Signature | Description |
|---|---|
| `npv(r, cfs: BigDecimal[])` | Rate r, vector of cash flows |

### Payment (`pmt`)

| Signature | Description |
|---|---|
| `pmt(r, n, p, f, t: boolean)` | Rate, periods, principal, future value, type |
| `pmt(r, nper: int, pv, fv, type: int)` | As above with int params |
| `pmt(r, nper: int, pv, fv)` | Type defaults to 0 |
| `pmt(r, nper: int, pv)` | Future value and type default to 0 |

### Number of Periods (`nper`)

| Signature | Description |
|---|---|
| `nper(r, y, p, f, t: boolean)` | Rate, payment, present value, future value, type |

### Interest Payment (`ipmt`)

| Signature | Description |
|---|---|
| `ipmt(r, per: int, nper: int, pv, fv, type: int)` | Interest portion of payment for period per |
| `ipmt(r, per: int, nper: int, pv, fv)` | Type defaults to 0 |
| `ipmt(r, per: int, nper: int, pv)` | Future value and type default to 0 |

### Principal Payment (`ppmt`)

| Signature | Description |
|---|---|
| `ppmt(r, per: int, nper: int, pv, fv, type: int)` | Principal portion of payment for period per |
| `ppmt(r, per: int, nper: int, pv, fv)` | Type defaults to 0 |
| `ppmt(r, per: int, nper: int, pv)` | Future value and type default to 0 |

> [!NOTE]
> There are no `IRR` or `RATE` functions in the current release.

### Example

```java
// Monthly payment on a 30-year, 6% annual rate mortgage of $300,000
// rate = 6%/12, nper = 360, pv = 300000
MathExpression mortgage = MathExpression.compile(
    "pmt(rate / 12; nper * 12; principal)",
    env
);
BigDecimal payment = mortgage.compute(
    Map.of("rate", new BigDecimal("0.06"), "nper", 30, "principal", 300000)
);
```

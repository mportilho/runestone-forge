# exp-eval-mk2 × Apache NiFi Expression Language — Análise Comparativa

Comparação entre a gramática e o catálogo de funções do `exp-eval-mk2` e o
[Apache NiFi Expression Language](https://nifi.apache.org/docs/nifi-docs/html/expression-language-guide.html),
com o objetivo de identificar oportunidades de incremento.

---

## Contexto

São linguagens com propósitos distintos:

- **NiFi EL** — orientada a transformação de atributos de FlowFile (string-heavy, funcional/fluente com encadeamento por `:`, sem operadores matemáticos avançados).
- **exp-eval-mk2** — motor de avaliação matemática/lógica de precisão arbitrária (`BigDecimal`), com tipagem rica, suporte a vetores, datas e condicional multi-branch.

As lacunas identificadas são quase sempre **ao nível de catálogo de funções**, não de gramática — a gramática do mk2 já é substancialmente mais poderosa que a do NiFi EL.

---

## 1. Vantagens exclusivas do exp-eval-mk2 (não presentes no NiFi EL)

| Recurso | Detalhe |
|---|---|
| Aritmética de precisão arbitrária | `BigDecimal` nativo |
| Operadores matemáticos avançados | `^`, `root`/`√`, `sqrt`, `\|x\|` (módulo absoluto), `%` (percentual postfix), `!` (fatorial) |
| Operadores booleanos completos | `xor`, `xnor`, `nand`, `nor` |
| Regex como operadores infix | `=~`, `!~` |
| Type hints / cast explícito | `<bool>`, `<number>`, `<text>`, `<date>`, `<time>`, `<datetime>`, `<vector>` |
| Null coalescing | `??` |
| Safe navigation | `?.` |
| Literais vetoriais | `[1, 2, 3]` |
| Destructuring assignment | `[a, b] = vector;` |
| Condicional multi-branch | `if/then/elsif/else/endif` |
| Literais de data/hora tipados | `2024-01-01`, `12:30`, `2024-01-01T10:00` |

---

## 2. Lacunas em relação ao NiFi EL

### 2a. Gramática (mudanças no `.g4`)

| Recurso NiFi | Sintaxe NiFi | Proposta para mk2 | Complexidade |
|---|---|---|---|
| **Operador `in`** | `${attr:in("a","b","c")}` | `expr in [a, b, c]` como operador infix | Média |
| **Operador `not in`** | — | `expr not in [a, b, c]` | Baixa (extensão do `in`) |
| **Operador `between`** | — (usa `gt` + `lt`) | `a between b and c` (açúcar para `a >= b and a <= c`) | Baixa |
| **Ternário compacto** | `${attr:ifElse(t,f)}` | `cond ? t : f` (alternativa ao `if/then/else/endif`) | Média |
| **`isEmpty` como operador** | `${attr:isEmpty()}` | `isEmpty expr` como unário | Baixa |

> **`in` é o ganho mais expressivo na gramática.** Hoje verificar pertinência em um conjunto exige uma função catalog (`contains(vector, value)`). Com gramática nativa:
> ```
> status in ["ACTIVE", "PENDING"]
> age in [18, 21, 25]
> ```

**Esboço da adição ao `.g4`:**

```antlr4
// Tokens novos
IN      : 'in' ;
BETWEEN : 'between' ;

// Extensão de logicalComparisonExpression:
| mathExpression IN vectorEntity                              # inOperation
| mathExpression NOT IN vectorEntity                         # notInOperation
| stringConcatExpression IN vectorEntity                     # stringInOperation
| mathExpression BETWEEN mathExpression AND mathExpression   # betweenOperation
```

> **Atenção ao `between`**: o token `AND` já existe; verificar que a regra não cria ambiguidade com `logicalAndExpression`.

### 2b. Catálogo de funções (sem mudança no `.g4`)

#### String

| Função NiFi | Assinatura sugerida |
|---|---|
| `toUpper` / `toLower` | `toUpper(String)`, `toLower(String)` |
| `trim` / `trimLeft` / `trimRight` | `trim(String)`, `trimLeft(String)`, `trimRight(String)` |
| `substring` | `substring(String, int)`, `substring(String, int, int)` |
| `substringBefore` / `substringAfterLast` | `substringBefore(String, String)`, `substringAfter(String, String)`, `substringBeforeLast(String, String)`, `substringAfterLast(String, String)` |
| `padLeft` / `padRight` | `padLeft(String, int)`, `padLeft(String, int, String)` |
| `repeat` | `repeat(String, int)` |
| `replace` / `replaceFirst` / `replaceAll` | `replace(String, String, String)`, `replaceAll(String, String, String)` (com regex) |
| `indexOf` / `lastIndexOf` | `indexOf(String, String)`, `lastIndexOf(String, String)` |
| `startsWith` / `endsWith` / `contains` | `startsWith(String, String)`, `endsWith(String, String)`, `contains(String, String)` |
| `isEmpty` / `isBlank` | `isEmpty(String)`, `isBlank(String)` |
| `length` | `length(String)` |
| `split` | `split(String, String)` → vetor |
| `join` | `join(vector, String)` |
| `getDelimitedField` | `getDelimitedField(String, int, String)` (extração de campo CSV) |

#### Encoding / Hashing

| Função NiFi | Assinatura sugerida |
|---|---|
| `urlEncode` / `urlDecode` | `urlEncode(String)`, `urlDecode(String)` |
| `base64Encode` / `base64Decode` | `base64Encode(String)`, `base64Decode(String)` |
| `escapeJson` / `unescapeJson` | `escapeJson(String)`, `unescapeJson(String)` |
| `escapeXml` / `unescapeXml` | `escapeXml(String)`, `unescapeXml(String)` |
| `escapeHtml` / `unescapeHtml` | `escapeHtml(String)`, `unescapeHtml(String)` |
| `hash(algorithm)` | `hash(String, String)` — ex.: `hash(value, "SHA-256")` |
| `uuid` / `uuid3` / `uuid5` | `uuid()`, `uuid3(String, String)`, `uuid5(String, String)` |

#### Tipo / Coerção

| Função NiFi | Equivalente mk2 | Lacuna? |
|---|---|---|
| `toString()` | `<text>(x)` via type hint | Coberto |
| `toNumber()` | `<number>(x)` via type hint | Coberto |
| `toDecimal()` | — | Ausente: conversão explícita para `Double` (64 bits IEEE 754) |
| `toDate(format)` | `<date>(x)` | Parse com formato customizado não coberto |
| `format(pattern)` | — | **Ausente**: formatação de data/número como `String` |

#### Sistema

| Função NiFi | Assinatura sugerida |
|---|---|
| `hostname()` | `hostname()` |
| `ip()` | `ip()` |
| `uuid()` | `uuid()` (já listado acima) |
| `nextInt()` | `randomInt()`, `randomInt(int max)` |

#### JSON

| Função NiFi | Assinatura sugerida |
|---|---|
| `jsonPath(path)` | `jsonPath(String json, String path)` |
| `isJson()` | `isJson(String)` |

#### Numérico

| Função NiFi | Assinatura sugerida |
|---|---|
| `toRadix(radix)` | `toRadix(BigDecimal, int)` |
| `fromRadix(radix)` | `fromRadix(String, int)` |

---

## 3. Priorização das recomendações

### Alta prioridade

1. **Catálogo de strings** — `toUpper`, `toLower`, `trim`, `substring`, `substringBefore/After`, `startsWith`, `endsWith`, `contains`, `indexOf`, `length`, `replace`, `replaceAll`, `isEmpty`, `isBlank`, `split`, `join`
2. **`format(value, pattern)`** — formatação de datas e números como `String`; sua ausência obriga os consumidores a fazer esse passo fora da expressão
3. **`urlEncode/Decode`, `base64Encode/Decode`** — necessários em pipelines de dados modernos

### Média prioridade

4. **Operador `in` na gramática** — `expr in [val1, val2]` — ganho sintático mais valioso
5. **`hash(str, algo)`** — integridade e segurança de dados
6. **`jsonPath(json, path)`** — extração inline de valores JSON
7. **`toDate(str, pattern)`** — parse de datas com formato customizado

### Baixa prioridade

8. **`toRadix` / `fromRadix`** — nicho (conversão de base numérica)
9. **`hostname()`, `ip()`, `uuid()`** — funções de sistema; úteis mas raras em regras de negócio
10. **Ternário `?:`** — conveniente, mas `if(cond; t; f)` já cobre o caso de uso
11. **String interpolation** — complexidade gramatical alta, benefício marginal

---

## 4. Gaps não cobertos por nenhuma das duas linguagens

Recursos ausentes em ambas, que poderiam ser avaliados no futuro:

| Recurso | Descrição |
|---|---|
| `between` como operador de range | Presente em SQL; açúcar útil para `a >= x and x <= b` |
| `not in` | Complemento natural do `in` |
| `reduce(vector, fn, initial)` | Acumulador/fold sobre vetores |
| Pattern matching estrutural | Match por tipo/forma de valor |
| Acesso por índice em vetor | `vector[0]` — atualmente vetores são entidades completas |
| Slicing de vetor | `vector[1..3]` — sub-vetores |

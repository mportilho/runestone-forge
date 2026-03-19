# Análise de Desempenho: MathFunctions.java

Esta análise identifica potenciais gargalos e "performance smells" no arquivo `com.runestone.expeval2.catalog.functions.MathFunctions`, com foco em operações de alta precisão usando `BigDecimal`.

## Pontos de Atenção Identificados

### 1. Criação Excessiva de Objetos em Loops (Imutabilidade do BigDecimal)
**Severidade: Média/Alta**
`BigDecimal` é imutável. Métodos como `mean`, `geometricMean`, `harmonicMean`, `variance` e `meanDev` realizam operações aritméticas dentro de loops sobre arrays. Cada operação (`add`, `multiply`, `subtract`) cria uma nova instância de `BigDecimal`.
*   **Impacto:** Em arrays grandes (ex: 1.000+ elementos), o loop gera milhares de objetos intermediários, aumentando a pressão sobre o Garbage Collector (GC).
*   **Sugestão:** Avaliar se a precisão do `DECIMAL128` é estritamente necessária para todos os cálculos ou se somas intermediárias poderiam ser otimizadas.

### 2. Uso Ineficiente de `pow` para Quadrados
**Severidade: Média**
No método `variance`, é utilizado `BigDecimalMath.pow(..., 2, MC)`.
```java
x = x.add(BigDecimalMath.pow(param.subtract(mean, MC), 2, MC), MC);
```
*   **Impacto:** `BigDecimalMath.pow` é projetado para potências genéricas (incluindo expoentes fracionários) e é significativamente mais lento do que uma multiplicação direta para potências inteiras pequenas.
*   **Recomendação:** Substituir por `diff.multiply(diff, MC)`, onde `diff` é o resultado de `param.subtract(mean, MC)`.

### 3. Chamadas Redundantes a `BigDecimal.valueOf()`
**Severidade: Baixa/Média**
O valor `BigDecimal.valueOf(p.length)` é calculado repetidamente em métodos como `harmonicMean`, `variance` e `meanDev`.
*   **Recomendação:** Cachear o valor do tamanho do array como um `BigDecimal` no início do método (ex: `var size = BigDecimal.valueOf(p.length)`) para evitar chamadas de método e lógica de conversão repetitivas.

### 4. Múltiplas Passagens sobre o Mesmo Array
**Severidade: Média**
Os métodos `variance` e `meanDev` chamam `mean(p)` internamente. Isso resulta em duas passagens completas sobre o array: uma para calcular a média e outra para calcular os desvios.
*   **Sugestão:** Para ganhos de performance em volumes massivos de dados, consolidar o cálculo da soma e da soma dos quadrados em uma única iteração, embora isso possa impactar levemente a legibilidade.

### 5. Configuração Repetitiva de Escala
**Severidade: Baixa**
No método `spread`, o comando `ZERO.setScale(value.scale(), HALF_EVEN)` pode ser chamado repetidamente ou dentro de `Arrays.fill`.
```java
Arrays.fill(distributed, ZERO.setScale(value.scale(), HALF_EVEN));
```
*   **Recomendação:** Extrair para uma variável local `zeroWithScale` antes do loop ou da operação de preenchimento.

### 6. Divisões em Loop (`spread`)
**Severidade: Média**
```java
BigDecimal coefficient = references[i].divide(totalSum, 16, HALF_EVEN);
```
*   **Impacto:** Divisões de `BigDecimal` são operações custosas. Além disso, o uso de escala fixa `16` pode divergir da precisão `DECIMAL128` (34 dígitos) usada no restante da classe.
*   **Sugestão:** Utilizar DECIMAL128 e considerar se o cálculo pode ser reorganizado para minimizar divisões.

## Próximos Passos Recomendados

1. **Benchmarking:** Implementar microbenchmarks usando **JMH** para medir o impacto real da substituição de `pow(..., 2)` por `multiply(...)`.
2. **Refatoração Cirúrgica:** Aplicar as correções de "baixo risco / alto ganho" (como o cache de `BigDecimal.valueOf(p.length)` e a troca de `pow`).
3. **Análise de Precisão:** Revisar o uso consistente do `MathContext` vs escalas fixas em operações de divisão.

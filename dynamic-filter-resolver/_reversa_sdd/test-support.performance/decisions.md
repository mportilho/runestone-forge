# Test Support Performance, Decisões de Design

> Spec operacional gerada pelo Reversa Writer em 2026-05-16.
> Escala de confiança: 🟢 CONFIRMADO, 🟡 INFERIDO, 🔴 LACUNA.

| ID | Decisão | Consequência | Evidência | Confiança |
|---|---|---|---|---|
| DEC-PERF-01 | Usar JMH como evidência para otimização. | Evita mudanças por hipótese sem medição. | ADR 005, `docs/performance-history.md` | 🟢 |
| DEC-PERF-02 | Separar baseline simples de cenários Spring/H2 pesados. | Benchmarks rápidos e pesados podem ser executados seletivamente. | `DynamicFilterResolverBenchmark`, `Perf02` | 🟢 |
| DEC-PERF-03 | Comparar otimizado contra legado em sort translation. | Garante equivalência comportamental mínima e evidência de ganho. | `DynamicFilterRepositorySortPerfBenchmark` | 🟢 |
| DEC-PERF-04 | Não aceitar otimização pequena com complexidade alta. | PERF-006 foi revertida/limitada conforme histórico. | `docs/performance-history.md`, `Perf06` | 🟢 |
| DEC-PERF-05 | Runner manual executa apenas baseline. | Outros benchmarks exigem include específico. | `DynamicFilterResolverBenchmarkRunner.java` | 🟡 |

## Pendência

- 🟡 Se a suíte de performance virar gate de CI, o runner deve ser revisto para seleção explícita de cenários.

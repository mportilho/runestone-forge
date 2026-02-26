# Template de Registro (Append-Only)

> Arquivo de destino obrigatório: `<módulo>/docs/perf/performance-history.md`
> Em multi-módulo, usar sempre o submódulo alvo (ex.: `expression-evaluator/docs/perf/performance-history.md`).

```md
## Experimento PERF-XXX
- Data: YYYY-MM-DD
- Objetivo:
- Baseline commit/estado:
- Commit/estado testado:

### Hipótese
1.
2.

### Mudanças aplicadas
- `arquivo:linha`:
- `arquivo:linha`:

### Protocolo de medição
- JVM:
- JMH:
- Benchmark class (src/test/java):
- Package de benchmark:
- Parâmetros:
  - `-wi ... -i ... -w ... -r ... -f ... -tu ns`
  - `-jvmArgs '...'`
- Comando executado:
```bash
<comando real>
```
- Comandos adicionais (quando solicitados):
```bash
<comando real>
```

### Resultado
| Benchmark | Before (ns/op) | After (ns/op) | Delta (ns/op) | Melhoria (%) |
|---|---:|---:|---:|---:|
| ... | ... | ... | ... | ... |

### Decisão
- [ ] Aceitar
- [ ] Ajustar
- [ ] Descartar

### Leitura técnica
1.
2.

### Atividades executadas
1. `comando 1` - sucesso/falha
2. `comando 2` - sucesso/falha
3. `comando 3` - sucesso/falha

### Riscos residuais
1.
2.
```

## Convenções
- Manter histórico append-only.
- Não sobrescrever resultados anteriores.
- Sempre registrar `Melhoria (%)` baseada em comparação de `ns/op`.

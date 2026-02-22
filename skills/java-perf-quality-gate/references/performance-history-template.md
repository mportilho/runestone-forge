# Template de Registro (Append-Only)

> Arquivo de destino obrigatorio: `<modulo>/docs/perf/performance-history.md`
> Em multi-modulo, usar sempre o submodulo alvo (ex.: `expression-evaluator/docs/perf/performance-history.md`).

```md
## Experimento PERF-XXX
- Data: YYYY-MM-DD
- Objetivo:
- Baseline commit/estado:
- Commit/estado testado:

### Hipotese
1.
2.

### Mudancas aplicadas
- `arquivo:linha`:
- `arquivo:linha`:

### Protocolo de medicao
- JVM:
- JMH:
- Benchmark class (src/test/java):
- Package de benchmark:
- Parametros:
  - `-wi ... -i ... -w ... -r ... -f ... -tu ns`
  - `-jvmArgs '...'`
- Comando executado:
```bash
<comando real>
```

### Resultado
| Benchmark | Before (ns/op) | After (ns/op) | Delta (ns/op) | Melhoria (%) |
|---|---:|---:|---:|---:|
| ... | ... | ... | ... | ... |

### Decisao
- [ ] Aceitar
- [ ] Ajustar
- [ ] Descartar

### Leitura tecnica
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

## Convencoes
- Manter historico append-only.
- Nao sobrescrever resultados anteriores.
- Sempre referenciar artefatos JSON de benchmark.
- Sempre registrar `Melhoria (%)` baseada em comparacao de `ns/op`.

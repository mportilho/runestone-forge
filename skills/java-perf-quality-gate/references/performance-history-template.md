# Template de Registro (Append-Only)

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
- Parametros:
  - `-wi ... -i ... -w ... -r ... -f ... -tu ...`
  - `-jvmArgs '...'`
- Comando executado:
```bash
<comando real>
```

### Resultado
| Benchmark | Before (us/op) | After (us/op) | Delta |
|---|---:|---:|---:|
| ... | ... | ... | ... |

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

## Convenções
- Manter histórico append-only.
- Não sobrescrever resultados anteriores.
- Sempre referenciar artefatos JSON de benchmark.

# JMH Playbook

## Objetivo
Padronizar medições antes/depois para comparação confiável.

## Pre-condicoes obrigatorias
- O modulo deve ter JMH habilitado.
- Dependencias minimas esperadas no `pom.xml`:
  - `org.openjdk.jmh:jmh-core`
  - `org.openjdk.jmh:jmh-generator-annprocess`
- Benchmarks devem ser criados em `src/test/java` e em package dedicado de performance.
- Convencao recomendada:
  - caminho: `src/test/java/<base_package>/perf/jmh/<Nome>Benchmark.java`
  - package: `<base_package>.perf.jmh`
- Historico de performance deve ficar no modulo alvo:
  - `<modulo>/docs/perf/performance-history.md`
  - Em multi-modulo, nao usar a raiz agregadora para historico de submodulo.

## Protocolo Base
- JVM: registrar versão exata (`java -version`)
- JMH: registrar versão usada no módulo
- Parâmetros padrão:
  - `-wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu ns`
  - `-jvmArgs '-Xms1g -Xmx1g'`
- Exportar JSON:
  - `-rf json -rff <arquivo>.json`

## Comando (Maven + classpath de teste)
```bash
cd <modulo>
DEP_CP="$(mvn -DincludeScope=test -DskipTests dependency:build-classpath -Dmdep.outputAbsoluteArtifactFilename=true -Dmdep.path | sed -n '/Dependencies classpath:/{n;p;}')"
CP="target/test-classes:target/classes:${DEP_CP}"
java -cp "$CP" org.openjdk.jmh.Main '<RegexDoBenchmark>' \
  -wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu ns \
  -jvmArgs '-Xms1g -Xmx1g' \
  -rf json -rff target/<resultado>.json -foe true
```

## Regras de Comparabilidade
- Usar os mesmos cenários, parâmetros e máquina para before/after.
- Evitar processos de build/test em paralelo durante geração de classes JMH.
- Se limpar `target`, recomputar before/after no mesmo protocolo.
- Não comparar resultados com blackhole/mode diferentes.
- Nao comparar resultados com unidades diferentes de `ns/op`.

## Leitura de Resultado
- Reportar no mínimo:
  - `Score`, `Error`, `Units`
  - `before ns/op`, `after ns/op`, `delta ns/op`, `Melhoria (%)`
- Formula obrigatoria para melhoria percentual:
  - `Melhoria (%) = ((before_ns_op - after_ns_op) / before_ns_op) * 100`
- Em regressões:
  - declarar magnitude absoluta e percentual em `ns/op`
  - decidir com base no trade-off (latência vs benefício estrutural)

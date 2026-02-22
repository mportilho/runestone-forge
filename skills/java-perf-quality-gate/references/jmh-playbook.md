# JMH Playbook

## Objetivo
Padronizar medições antes/depois para comparação confiável.

## Protocolo Base
- JVM: registrar versão exata (`java -version`)
- JMH: registrar versão usada no módulo
- Parâmetros padrão:
  - `-wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu us`
  - `-jvmArgs '-Xms1g -Xmx1g'`
- Exportar JSON:
  - `-rf json -rff <arquivo>.json`

## Comando (Maven + classpath de teste)
```bash
cd <modulo>
DEP_CP="$(mvn -DincludeScope=test -DskipTests dependency:build-classpath -Dmdep.outputAbsoluteArtifactFilename=true -Dmdep.path | sed -n '/Dependencies classpath:/{n;p;}')"
CP="target/test-classes:target/classes:${DEP_CP}"
java -cp "$CP" org.openjdk.jmh.Main '<RegexDoBenchmark>' \
  -wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu us \
  -jvmArgs '-Xms1g -Xmx1g' \
  -rf json -rff target/<resultado>.json -foe true
```

## Regras de Comparabilidade
- Usar os mesmos cenários, parâmetros e máquina para before/after.
- Evitar processos de build/test em paralelo durante geração de classes JMH.
- Se limpar `target`, recomputar before/after no mesmo protocolo.
- Não comparar resultados com blackhole/mode diferentes.

## Leitura de Resultado
- Reportar no mínimo:
  - `Score`, `Error`, `Units`
  - delta absoluto e percentual
- Em regressões:
  - declarar magnitude absoluta e percentual
  - decidir com base no trade-off (latência vs benefício estrutural)

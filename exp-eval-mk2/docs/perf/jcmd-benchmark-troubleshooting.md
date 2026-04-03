# exp-eval-mk2 — Guia de Troubleshooting para `jcmd` + JMH + JFR

**Data:** 2026-03-25  
**Modulo:** `exp-eval-mk2`  
**Contexto:** profiling dos benchmarks de objetos navegaveis

## 1. Objetivo

Este documento registra o procedimento operacional e o troubleshooting observado durante a analise
de hot spots de `compute()` com objetos navegaveis em `exp-eval-mk2`.

O foco e permitir proximas execucoes de benchmark e coleta com `jcmd`/JFR sem repetir os erros
encontrados no inicio da investigacao.

## 2. Escopo deste guia

Este guia cobre:

- como preparar o modulo para rodar os benchmarks
- como iniciar benchmarks JMH de runtime
- como anexar `jcmd` ao processo correto
- como coletar JFR para CPU e alocacao
- como lidar com erros de sandbox, attach e lock do JMH

Este guia nao cobre:

- otimizacao de codigo
- interpretacao detalhada dos hot spots
- mudancas no benchmark em si

## 3. Pre-requisitos

## 3.1 Compilacao do modulo

Antes de qualquer coleta:

```bash
mvn -q -DskipTests test-compile -pl exp-eval-mk2
```

Validacoes esperadas apos a compilacao:

- `exp-eval-mk2/target/classes` existe
- `exp-eval-mk2/target/test-classes` existe
- `exp-eval-mk2/target/jmh-cp.txt` existe

## 3.2 Ferramentas

Confirmar:

```bash
java -version
which jcmd
which jfr
```

Durante a avaliacao de referencia, o ambiente usou:

- JDK 21.0.6
- `jcmd`
- `jfr`

## 3.3 Onde gravar os artefatos

Usar sempre:

```bash
mkdir -p /tmp/performance-benchmark
```

Artefatos esperados:

- `*.out`
- `*.json`
- `*.jfr`

## 4. Regra mais importante: `jcmd` precisa rodar fora do sandbox

Durante a investigacao, o maior bloqueio inicial foi o attach do `jcmd` dentro do sandbox.

### 4.1 Sintomas observados no sandbox

Erros vistos:

```text
java.io.IOException: Can not attach to current VM
```

```text
AttachNotSupportedException: Unable to open socket file /proc/<pid>/root/tmp/.java_pid<pid>
```

```text
java.io.IOException: No such process
```

### 4.2 Causa

O sandbox interfere em:

- namespace de PID
- attach socket do HotSpot
- visibilidade do processo real para o `jcmd`

Na pratica:

- o `jcmd` ate pode listar um PID
- mas o attach falha porque o socket `.java_pid<pid>` nao fica acessivel no namespace esperado

### 4.3 Regra operacional

Se a coleta exigir `jcmd`, `JFR.start` ou attach no processo Java:

- **rode a coleta fora do sandbox**

Nao tente insistir no sandbox depois de ver erro de attach. Isso so perde tempo.

## 5. Regra importante: usar `-XX:+StartAttachListener`

Para reduzir problemas de attach, iniciar o benchmark Java com:

```bash
-XX:+StartAttachListener
```

Sem isso, o attach pode funcionar em alguns casos, mas fica menos confiavel. Para coleta repetivel,
considere esse flag obrigatorio.

## 6. Regra importante: filtrar apenas os benchmarks de runtime

Para investigacao de hot spots de execucao:

- nao usar a classe inteira sem filtro
- nao misturar build de environment
- nao misturar cenarios de compilacao

No caso de `ObjectNavigationBenchmark`, os cenarios de runtime sao:

- `typedNestedProperty`
- `typedMethodNoArg`
- `typedMethodWithArgument`
- `reflectiveNestedProperty`
- `reflectiveMethodWithArgument`

Evitar incluir:

- `buildTypedEnvironment`
- `compileTypedNestedProperty`
- `compileTypedMethodWithArgument`
- `compileReflectiveMethodWithArgument`

## 7. Regra importante: o runner atual nao e o melhor ponto de entrada

O `ObjectNavigationBenchmarkRunner` atual:

- inclui todos os benchmarks da classe
- usa `forks(0)`
- adiciona `StackProfiler` sempre

Para hot spot analysis, prefira chamar `org.openjdk.jmh.Main` diretamente com filtro explicito.

## 8. Protocolo recomendado para novas coletas

## 8.1 Rodada base de benchmark sem JFR

Use esta rodada para obter `ns/op` e `B/op` com menor perturbacao:

```bash
java -cp "$(cat exp-eval-mk2/target/jmh-cp.txt):exp-eval-mk2/target/test-classes:exp-eval-mk2/target/classes" \
  org.openjdk.jmh.Main \
  'ObjectNavigationBenchmark\.(typedNestedProperty|typedMethodNoArg|typedMethodWithArgument|reflectiveNestedProperty|reflectiveMethodWithArgument)' \
  -wi 5 -i 10 -w 500ms -r 500ms -f 3 -tu ns -prof gc \
  -rf json -rff /tmp/performance-benchmark/object-navigation-runtime.json
```

Uso:

- essa rodada serve para comparacao entre cenarios
- nao anexar JFR aqui, se o objetivo principal for numero mais estavel

## 8.2 Rodada exploratoria com JFR em um cenario isolado

Use quando quiser hot spots de CPU e alocacao:

```bash
java -XX:+StartAttachListener \
  -cp "$(cat exp-eval-mk2/target/jmh-cp.txt):exp-eval-mk2/target/test-classes:exp-eval-mk2/target/classes" \
  -Djmh.ignoreLock=true \
  org.openjdk.jmh.Main \
  'ObjectNavigationBenchmark\.typedMethodWithArgument' \
  -wi 3 -i 10 -w 2s -r 2s -f 0 -tu ns -prof gc \
  -rf json -rff /tmp/performance-benchmark/object-navigation-typed-method.json \
  > /tmp/performance-benchmark/object-navigation-typed-method.out 2>&1 &
pid=$!
echo "$pid"
sleep 2
jcmd $pid JFR.start name=typed-nav settings=profile \
  filename=/tmp/performance-benchmark/object-navigation-typed-method.jfr \
  duration=24s
wait $pid
```

Repita o mesmo padrao para:

- `reflectiveMethodWithArgument`
- `reflectiveNestedProperty`

## 8.3 Pos-processamento do JFR

Resumo:

```bash
jfr summary /tmp/performance-benchmark/object-navigation-typed-method.jfr
```

Samples de execucao:

```bash
jfr print --events jdk.ExecutionSample --stack-depth 12 \
  /tmp/performance-benchmark/object-navigation-typed-method.jfr
```

Samples de alocacao:

```bash
jfr print --events jdk.ObjectAllocationSample --stack-depth 12 \
  /tmp/performance-benchmark/object-navigation-typed-method.jfr
```

Top frames por frequencia:

```bash
jfr print --events jdk.ExecutionSample --stack-depth 1 file.jfr \
  | awk '/stackTrace = \[/ {getline; sub(/^[[:space:]]+/, "", $0); sub(/,?$/, "", $0); print}' \
  | sort | uniq -c | sort -nr | head -20
```

```bash
jfr print --events jdk.ObjectAllocationSample --stack-depth 1 file.jfr \
  | awk '/stackTrace = \[/ {getline; sub(/^[[:space:]]+/, "", $0); sub(/,?$/, "", $0); print}' \
  | sort | uniq -c | sort -nr | head -20
```

## 9. Troubleshooting observado

## 9.1 Erro: `Can not attach to current VM`

Mensagem observada:

```text
java.io.IOException: Can not attach to current VM
```

Causa tipica:

- tentativa de usar `jcmd` no mesmo processo errado
- attach dentro do sandbox

Correcao:

- sair do sandbox
- validar o PID real com `jcmd -l`
- usar `-XX:+StartAttachListener`

## 9.2 Erro: `Unable to open socket file /proc/<pid>/root/tmp/.java_pid<pid>`

Mensagem observada:

```text
AttachNotSupportedException: Unable to open socket file /proc/<pid>/root/tmp/.java_pid<pid>
```

Causa tipica:

- processo em namespace diferente
- attach bloqueado pelo sandbox
- attach listener nao disponivel

Correcao:

- executar fora do sandbox
- iniciar o benchmark com `-XX:+StartAttachListener`
- esperar 1 a 3 segundos antes de rodar `jcmd`

## 9.3 Erro: `No such process`

Mensagem observada:

```text
java.io.IOException: No such process
```

Causas tipicas:

- PID capturado era do shell/background job errado
- processo Java ja terminou antes do attach
- a execucao falhou antes do `jcmd`

Checklist de correcao:

1. Verificar o PID com `jcmd -l`.
2. Confirmar que o processo ainda esta vivo com `ps -fp <pid>`.
3. Confirmar que o benchmark realmente iniciou lendo o arquivo `.out`.
4. Aumentar o tempo de medicao se o processo estiver terminando rapido demais.

## 9.4 Erro: `Another JMH instance might be running`

Mensagem observada:

```text
ERROR: Another JMH instance might be running. Unable to acquire the JMH lock (/tmp/jmh.lock)
```

Causa:

- ainda havia outro processo JMH em execucao
- o lock ficou ativo entre tentativas

Correcao preferida:

1. Confirmar se ha JMH ativo:

```bash
jcmd -l | grep org.openjdk.jmh.Main
```

2. Esperar a rodada anterior terminar.

3. So usar `-Djmh.ignoreLock=true` em rodada exploratoria controlada, quando voce sabe que nao ha
   outra medicao concorrente relevante.

Observacao:

- nao sair apagando `/tmp/jmh.lock` sem confirmar que nao existe JMH rodando

## 9.5 Problema: `jcmd` lista o processo, mas nao consegue anexar

Isso aconteceu na investigacao.

Interpretacao:

- listar o PID nao garante que o attach vai funcionar
- o teste real e `jcmd <pid> VM.version`

Use esta verificacao antes de iniciar uma coleta longa:

```bash
jcmd <pid> VM.version
```

Se isso falhar:

- nao siga para `JFR.start`
- corrija o ambiente primeiro

## 9.6 Problema: `forks(0)` distorce a leitura

O JFR com attach foi feito em `forks(0)` por praticidade operacional.

Consequencia:

- `ns/op` fica menos confiavel para comparacoes finas
- existe mais ruido de JVM aquecida, profilers e estado do processo

Uso correto:

- `forks(0)` para profiling e descoberta de hot spots
- `forks(>0)` para benchmark comparativo

## 9.7 Problema: `StackProfiler` junto com JFR

Evite combinar:

- `StackProfiler`
- JFR com `settings=profile`

na mesma rodada principal de coleta.

Motivo:

- ambos aumentam o overhead
- o numero absoluto de `ns/op` piora
- a analise fica mais ruidosa

## 10. Sequencia recomendada de execucao

Use esta ordem.

1. Compilar o modulo:

```bash
mvn -q -DskipTests test-compile -pl exp-eval-mk2
```

2. Rodar benchmark base com `forks(3)` e `-prof gc`.

3. Escolher um cenario por vez para profiling.

4. Rodar o cenario com `-XX:+StartAttachListener` e `forks(0)`.

5. Validar attach:

```bash
jcmd <pid> VM.version
```

6. Iniciar JFR:

```bash
jcmd <pid> JFR.start name=<name> settings=profile filename=<file>.jfr duration=24s
```

7. Esperar o benchmark terminar.

8. Ler:

- `*.out`
- `*.json`
- `jfr summary`
- `jdk.ExecutionSample`
- `jdk.ObjectAllocationSample`

## 11. Lista de verificacao rapida

Antes de iniciar:

- `exp-eval-mk2/target/jmh-cp.txt` existe
- `jcmd` e `jfr` estao disponiveis
- `/tmp/performance-benchmark` existe
- nao ha outro JMH ativo
- a coleta sera feita fora do sandbox

Antes do `JFR.start`:

- o PID foi confirmado com `jcmd -l`
- `jcmd <pid> VM.version` funcionou
- o benchmark esta realmente em execucao

Depois da coleta:

- o `.out` foi gerado
- o `.json` foi gerado
- o `.jfr` foi gerado
- `jfr summary` abre normalmente

## 12. Artefatos de referencia desta investigacao

Arquivos produzidos e validados na analise:

- `/tmp/performance-benchmark/object-navigation-typed-method.out`
- `/tmp/performance-benchmark/object-navigation-reflective-method.out`
- `/tmp/performance-benchmark/object-navigation-reflective-property.out`
- `/tmp/performance-benchmark/object-navigation-typed-method.json`
- `/tmp/performance-benchmark/object-navigation-reflective-method.json`
- `/tmp/performance-benchmark/object-navigation-reflective-property.json`
- `/tmp/performance-benchmark/object-navigation-typed-method.jfr`
- `/tmp/performance-benchmark/object-navigation-reflective-method.jfr`
- `/tmp/performance-benchmark/object-navigation-reflective-property.jfr`

## 13. Recomendacao final

Se o objetivo for:

- **medir desempenho**: usar JMH com forks reais e GC profiler
- **descobrir hot spots**: usar cenario isolado + `jcmd` + JFR
- **evitar perda de tempo**: nao insistir no sandbox quando o attach falhar

Na pratica, a coleta com `jcmd` deve ser tratada como procedimento operacional de ambiente
privilegiado, nao como extensao da execucao sandboxada normal.


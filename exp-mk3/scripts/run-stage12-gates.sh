#!/usr/bin/env bash
set -euo pipefail

MODULE_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)
ROOT_DIR=$(cd "$MODULE_DIR/.." && pwd)
MANIFEST="$MODULE_DIR/docs/perf/stage12-gates.json"
MANIFEST_TOOL="$MODULE_DIR/scripts/stage12-manifest.py"
OUTPUT_DIR=${STAGE12_OUTPUT_DIR:-"$MODULE_DIR/target/stage12"}

usage() {
    cat <<'EOF'
Usage: exp-mk3/scripts/run-stage12-gates.sh [--output DIR]

Runs the local Etapa 12 baseline: functional suite, manifest JMH with GC,
JOL layout report, and a JFR smoke recording. Artifacts are written below
exp-mk3/target/stage12 by default. The script never changes Git state.
EOF
}

while (($#)); do
    case "$1" in
        --output) OUTPUT_DIR=$2; shift 2 ;;
        --help|-h) usage; exit 0 ;;
        *) echo "Unknown argument: $1" >&2; usage >&2; exit 2 ;;
    esac
done

for command in cat cp date find getconf git grep head lscpu mvn python3 realpath rm sed tee uname; do
    command -v "$command" >/dev/null || { echo "Required command not found: $command" >&2; exit 2; }
done

OUTPUT_DIR=$(realpath -m "$OUTPUT_DIR")
if [[ "$OUTPUT_DIR" == / || "$OUTPUT_DIR" == "$ROOT_DIR" || "$OUTPUT_DIR" == "$MODULE_DIR" ]]; then
    echo "Unsafe output directory: $OUTPUT_DIR" >&2
    exit 2
fi

python3 "$MANIFEST_TOOL" "$MANIFEST" validate
EXPECTED_JDK=$(python3 "$MANIFEST_TOOL" "$MANIFEST" protocol jdkMajor)
EXPECTED_JDK_VENDOR=$(python3 "$MANIFEST_TOOL" "$MANIFEST" protocol jdkVendor)
MAVEN_JAVA_HOME=$(mvn -version | sed -nE 's/^Java home: //p; s/^Java version:.* runtime: (.*)$/\1/p' | head -1)
if [[ -z "$MAVEN_JAVA_HOME" || ! -x "$MAVEN_JAVA_HOME/bin/java" ]]; then
    echo "Maven did not report a usable Java home" >&2
    exit 2
fi
JAVA_BIN="$MAVEN_JAVA_HOME/bin/java"
JAVA_MAJOR=$($JAVA_BIN -XshowSettings:properties -version 2>&1 | sed -n 's/^[[:space:]]*java.specification.version = //p')
JAVA_VENDOR=$($JAVA_BIN -XshowSettings:properties -version 2>&1 | sed -n 's/^[[:space:]]*java.vendor = //p')
if [[ "$JAVA_MAJOR" != "$EXPECTED_JDK" ]]; then
    echo "Etapa 12 requires JDK $EXPECTED_JDK; Maven uses JDK $JAVA_MAJOR at $MAVEN_JAVA_HOME" >&2
    exit 2
fi
if [[ "$JAVA_VENDOR" != "$EXPECTED_JDK_VENDOR" ]]; then
    echo "Etapa 12 requires JDK vendor $EXPECTED_JDK_VENDOR; Maven uses $JAVA_VENDOR" >&2
    exit 2
fi

mkdir -p "$OUTPUT_DIR/jmh" "$OUTPUT_DIR/jfr" "$OUTPUT_DIR/jol"
find "$OUTPUT_DIR/jmh" "$OUTPUT_DIR/jfr" "$OUTPUT_DIR/jol" -mindepth 1 -delete
rm -f "$OUTPUT_DIR/artifacts.txt" "$OUTPUT_DIR/commands.txt" "$OUTPUT_DIR/environment.txt"
cp "$MANIFEST" "$OUTPUT_DIR/manifest.json"

FORKS=$(python3 "$MANIFEST_TOOL" "$MANIFEST" protocol forks)
WARMUP_ITERATIONS=$(python3 "$MANIFEST_TOOL" "$MANIFEST" protocol warmupIterations)
WARMUP_TIME=$(python3 "$MANIFEST_TOOL" "$MANIFEST" protocol warmupTime)
MEASUREMENT_ITERATIONS=$(python3 "$MANIFEST_TOOL" "$MANIFEST" protocol measurementIterations)
MEASUREMENT_TIME=$(python3 "$MANIFEST_TOOL" "$MANIFEST" protocol measurementTime)
THREADS=$(python3 "$MANIFEST_TOOL" "$MANIFEST" protocol threads)
HEAP=$(python3 "$MANIFEST_TOOL" "$MANIFEST" protocol heap)
PROFILER=$(python3 "$MANIFEST_TOOL" "$MANIFEST" protocol profiler)

{
    echo "timestamp=$(date --iso-8601=seconds)"
    echo "working.directory=$ROOT_DIR"
    echo "git.commit=$(git -C "$ROOT_DIR" rev-parse HEAD)"
    echo "git.dirty=$(if [[ -n $(git -C "$ROOT_DIR" status --short) ]]; then echo true; else echo false; fi)"
    echo "os=$(uname -srvmo)"
    echo "cpu=$(lscpu | sed -n 's/^Model name:[[:space:]]*//p' | head -1)"
    echo "logical.processors=$(getconf _NPROCESSORS_ONLN)"
    echo "java.home=$MAVEN_JAVA_HOME"
    "$JAVA_BIN" -version 2>&1
    mvn -version
} > "$OUTPUT_DIR/environment.txt"

record_command() {
    printf '%q ' "$@" >> "$OUTPUT_DIR/commands.txt"
    printf '\n' >> "$OUTPUT_DIR/commands.txt"
}

: > "$OUTPUT_DIR/commands.txt"
record_command mvn -f "$ROOT_DIR/pom.xml" -pl exp-mk3 -am test
mvn -f "$ROOT_DIR/pom.xml" -pl exp-mk3 -am test | tee "$OUTPUT_DIR/tests.log"

if grep -q '<id>stage12-stress</id>' "$MODULE_DIR/pom.xml"; then
    record_command mvn -f "$ROOT_DIR/pom.xml" -pl exp-mk3 -am -Pstage12-stress verify
    mvn -f "$ROOT_DIR/pom.xml" -pl exp-mk3 -am -Pstage12-stress verify | tee "$OUTPUT_DIR/stress.log"
fi

record_command mvn -f "$ROOT_DIR/pom.xml" -pl exp-mk3 -am -DskipTests test-compile
mvn -f "$ROOT_DIR/pom.xml" -pl exp-mk3 -am -DskipTests test-compile | tee "$OUTPUT_DIR/test-compile.log"
record_command mvn -f "$MODULE_DIR/pom.xml" -DincludeScope=test dependency:build-classpath \
    -Dmdep.outputFile="$OUTPUT_DIR/test-classpath.txt"
mvn -f "$MODULE_DIR/pom.xml" -DincludeScope=test dependency:build-classpath \
    -Dmdep.outputFile="$OUTPUT_DIR/test-classpath.txt" | tee "$OUTPUT_DIR/classpath.log"
TEST_CP="$MODULE_DIR/target/test-classes:$MODULE_DIR/target/classes:$(cat "$OUTPUT_DIR/test-classpath.txt")"

while IFS= read -r family; do
    include=$(python3 "$MANIFEST_TOOL" "$MANIFEST" regex "$family")
    mapfile -t family_arguments < <(python3 "$MANIFEST_TOOL" "$MANIFEST" arguments "$family")
    jmh_command=("$JAVA_BIN" -cp "$TEST_CP" org.openjdk.jmh.Main "$include"
        -wi "$WARMUP_ITERATIONS" -w "$WARMUP_TIME" \
        -i "$MEASUREMENT_ITERATIONS" -r "$MEASUREMENT_TIME" \
        -f "$FORKS" -t "$THREADS" -jvmArgs "-Xms$HEAP -Xmx$HEAP" \
        -prof "$PROFILER" -rf json -rff "$OUTPUT_DIR/jmh/$family.json" \
        "${family_arguments[@]}")
    record_command "${jmh_command[@]}"
    "${jmh_command[@]}" | tee "$OUTPUT_DIR/jmh/$family.log"
done < <(python3 "$MANIFEST_TOOL" "$MANIFEST" families)

record_command "$JAVA_BIN" -cp "$TEST_CP" \
    com.runestone.expeval_mk3.perf.jmh.CalculationMemoryProductionLayoutReport
"$JAVA_BIN" -cp "$TEST_CP" com.runestone.expeval_mk3.perf.jmh.CalculationMemoryProductionLayoutReport \
    > "$OUTPUT_DIR/jol/calculation-memory-layout.txt"

jfr_command=("$JAVA_BIN" -cp "$TEST_CP" org.openjdk.jmh.Main \
    '^com\.runestone\.expeval_mk3\.perf\.jmh\.Phase5BaselineBenchmark\.arithmeticCompute$' \
    -wi 1 -w 200ms -i 1 -r 1s -f 1 \
    -jvmArgs "-Xms$HEAP -Xmx$HEAP" \
    -prof "jfr:dir=$OUTPUT_DIR/jfr;configName=profile")
record_command "${jfr_command[@]}"
"${jfr_command[@]}" | tee "$OUTPUT_DIR/jfr/jfr.log"

find "$OUTPUT_DIR" -type f -printf '%P\n' | LC_ALL=C sort > "$OUTPUT_DIR/artifacts.txt"
echo "Etapa 12 artifacts: $OUTPUT_DIR"

#!/usr/bin/env bash
set -euo pipefail

MODULE_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)
ROOT_DIR=$(cd "$MODULE_DIR/.." && pwd)
MANIFEST="$MODULE_DIR/docs/perf/stage12-gates.json"
MANIFEST_TOOL="$MODULE_DIR/scripts/stage12-manifest.py"
EVALUATOR="$MODULE_DIR/scripts/stage12-evaluate.py"
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

for command in cat cp date find getconf git grep head lscpu mktemp mvn python3 realpath rm sed tar tee uname; do
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

mkdir -p "$OUTPUT_DIR/jmh" "$OUTPUT_DIR/jmh-baseline" "$OUTPUT_DIR/jfr" "$OUTPUT_DIR/jol"
find "$OUTPUT_DIR/jmh" "$OUTPUT_DIR/jmh-baseline" "$OUTPUT_DIR/jfr" "$OUTPUT_DIR/jol" -mindepth 1 -delete
rm -f "$OUTPUT_DIR/artifacts.txt" "$OUTPUT_DIR/commands.txt" "$OUTPUT_DIR/environment.txt" \
    "$OUTPUT_DIR/verdict.json" "$OUTPUT_DIR/verdict.txt" "$OUTPUT_DIR"/*.log
cp "$MANIFEST" "$OUTPUT_DIR/manifest.json"

FORKS=$(python3 "$MANIFEST_TOOL" "$MANIFEST" protocol forks)
WARMUP_ITERATIONS=$(python3 "$MANIFEST_TOOL" "$MANIFEST" protocol warmupIterations)
WARMUP_TIME=$(python3 "$MANIFEST_TOOL" "$MANIFEST" protocol warmupTime)
MEASUREMENT_ITERATIONS=$(python3 "$MANIFEST_TOOL" "$MANIFEST" protocol measurementIterations)
MEASUREMENT_TIME=$(python3 "$MANIFEST_TOOL" "$MANIFEST" protocol measurementTime)
THREADS=$(python3 "$MANIFEST_TOOL" "$MANIFEST" protocol threads)
HEAP=$(python3 "$MANIFEST_TOOL" "$MANIFEST" protocol heap)
PROFILER=$(python3 "$MANIFEST_TOOL" "$MANIFEST" protocol profiler)
BASELINE_COMMIT=$(python3 "$MANIFEST_TOOL" "$MANIFEST" protocol traversalBaselineCommit)

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

has_family_argument() {
    local expected=$1
    local argument
    for argument in "${family_arguments[@]}"; do
        [[ "$argument" == "$expected" ]] && return 0
    done
    return 1
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
    jmh_command=("$JAVA_BIN" -cp "$TEST_CP" org.openjdk.jmh.Main "$include")
    has_family_argument -wi || jmh_command+=(-wi "$WARMUP_ITERATIONS")
    has_family_argument -w || jmh_command+=(-w "$WARMUP_TIME")
    has_family_argument -i || jmh_command+=(-i "$MEASUREMENT_ITERATIONS")
    has_family_argument -r || jmh_command+=(-r "$MEASUREMENT_TIME")
    has_family_argument -f || jmh_command+=(-f "$FORKS")
    has_family_argument -t || jmh_command+=(-t "$THREADS")
    has_family_argument -jvmArgs || jmh_command+=(-jvmArgs "-Xms$HEAP -Xmx$HEAP")
    has_family_argument -prof || jmh_command+=(-prof "$PROFILER")
    jmh_command+=(-rf json -rff "$OUTPUT_DIR/jmh/$family.json" "${family_arguments[@]}")
    record_command "${jmh_command[@]}"
    "${jmh_command[@]}" | tee "$OUTPUT_DIR/jmh/$family.log"
done < <(python3 "$MANIFEST_TOOL" "$MANIFEST" families)

BASELINE_WORK=$(mktemp -d /tmp/opencode/stage12-baseline.XXXXXX)
trap 'rm -rf "$BASELINE_WORK"' EXIT
BASELINE_ARCHIVE="$BASELINE_WORK/source.tar"
BASELINE_SOURCE="$BASELINE_WORK/source"
mkdir -p "$BASELINE_SOURCE"
record_command git -C "$ROOT_DIR" archive "$BASELINE_COMMIT" -o "$BASELINE_ARCHIVE"
git -C "$ROOT_DIR" archive "$BASELINE_COMMIT" -o "$BASELINE_ARCHIVE"
record_command tar -xf "$BASELINE_ARCHIVE" -C "$BASELINE_SOURCE"
tar -xf "$BASELINE_ARCHIVE" -C "$BASELINE_SOURCE"
BASELINE_BENCHMARK=com/runestone/expeval_mk3/perf/jmh/Stage12TraversalBenchmark.java
record_command cp "$MODULE_DIR/src/test/java/$BASELINE_BENCHMARK" \
    "$BASELINE_SOURCE/exp-mk3/src/test/java/$BASELINE_BENCHMARK"
cp "$MODULE_DIR/src/test/java/$BASELINE_BENCHMARK" \
    "$BASELINE_SOURCE/exp-mk3/src/test/java/$BASELINE_BENCHMARK"
record_command mvn -f "$BASELINE_SOURCE/pom.xml" -pl exp-mk3 -am -DskipTests test-compile
mvn -f "$BASELINE_SOURCE/pom.xml" -pl exp-mk3 -am -DskipTests test-compile \
    | tee "$OUTPUT_DIR/baseline-test-compile.log"
record_command mvn -f "$BASELINE_SOURCE/exp-mk3/pom.xml" -DincludeScope=test dependency:build-classpath \
    -Dmdep.outputFile="$OUTPUT_DIR/baseline-test-classpath.txt"
mvn -f "$BASELINE_SOURCE/exp-mk3/pom.xml" -DincludeScope=test dependency:build-classpath \
    -Dmdep.outputFile="$OUTPUT_DIR/baseline-test-classpath.txt" | tee "$OUTPUT_DIR/baseline-classpath.log"
BASELINE_CP="$BASELINE_SOURCE/exp-mk3/target/test-classes:$BASELINE_SOURCE/exp-mk3/target/classes:$(cat "$OUTPUT_DIR/baseline-test-classpath.txt")"
baseline_jmh_command=("$JAVA_BIN" -cp "$BASELINE_CP" org.openjdk.jmh.Main \
    '^com\.runestone\.expeval_mk3\.perf\.jmh\.Stage12TraversalBenchmark\.(scalar|scalarAllocation|collection|collectionAllocation)$' \
    -wi "$WARMUP_ITERATIONS" -w "$WARMUP_TIME" \
    -i "$MEASUREMENT_ITERATIONS" -r "$MEASUREMENT_TIME" \
    -f "$FORKS" -t "$THREADS" -jvmArgs "-Xms$HEAP -Xmx$HEAP" \
    -prof "$PROFILER" -rf json -rff "$OUTPUT_DIR/jmh-baseline/traversal.json")
record_command "${baseline_jmh_command[@]}"
"${baseline_jmh_command[@]}" | tee "$OUTPUT_DIR/jmh-baseline/traversal.log"

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

record_command python3 "$EVALUATOR" "$MANIFEST" "$OUTPUT_DIR/jmh" "$OUTPUT_DIR/verdict.json" \
    --baseline "$OUTPUT_DIR/jmh-baseline"
python3 "$EVALUATOR" "$MANIFEST" "$OUTPUT_DIR/jmh" "$OUTPUT_DIR/verdict.json" \
    --baseline "$OUTPUT_DIR/jmh-baseline" \
    | tee "$OUTPUT_DIR/verdict.txt"

find "$OUTPUT_DIR" -type f -printf '%P\n' | LC_ALL=C sort > "$OUTPUT_DIR/artifacts.txt"
echo "Etapa 12 artifacts: $OUTPUT_DIR"

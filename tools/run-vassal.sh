#!/usr/bin/env bash

set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

if [[ -x "$ROOT/mvnw" ]]; then
  MVN="${MVN:-$ROOT/mvnw}"
else
  MVN="${MVN:-mvn}"
fi

if [[ -n "${JAVA_HOME:-}" ]]; then
  JAVA="${JAVA:-$JAVA_HOME/bin/java}"
else
  JAVA="${JAVA:-java}"
fi

DEFAULT_MAVEN_THREADS="$(nproc)"
DEFAULT_MAVEN_THREADS="$(( DEFAULT_MAVEN_THREADS > 1 ? DEFAULT_MAVEN_THREADS / 2 : 1 ))"
MAVEN_THREADS="${MAVEN_THREADS:-$DEFAULT_MAVEN_THREADS}"
CLASSPATH_FILE="${TMPDIR:-/tmp}/vassal-app.classpath"

"$MVN" -T "$MAVEN_THREADS" -U -pl vassal-deprecation -am install -DskipTests
"$MVN" -T "$MAVEN_THREADS" -U -pl vassal-app -am package -DskipTests
"$MVN" -T "$MAVEN_THREADS" -pl vassal-app dependency:build-classpath \
  "-Dmdep.outputFile=$CLASSPATH_FILE"

exec "$JAVA" -cp \
  "$ROOT/vassal-app/target/classes:$ROOT/vassal-deprecation/target/classes:$(cat "$CLASSPATH_FILE")" \
  VASSAL.launch.ModuleManager "$@"

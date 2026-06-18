#!/usr/bin/env bash

set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

if [[ -x "$ROOT/mvnw" ]]; then
  MVN="${MVN:-$ROOT/mvnw}"
else
  MVN="${MVN:-mvn}"
fi

MAVEN_THREADS="${MAVEN_THREADS:-8}"
CLASSPATH_FILE="${TMPDIR:-/tmp}/vassal-app.classpath"

"$MVN" -T "$MAVEN_THREADS" -U -pl vassal-app -am package -DskipTests
"$MVN" -T "$MAVEN_THREADS" -pl vassal-app dependency:build-classpath \
  "-Dmdep.outputFile=$CLASSPATH_FILE"

exec java -cp \
  "$ROOT/vassal-app/target/classes:$ROOT/vassal-deprecation/target/classes:$(cat "$CLASSPATH_FILE")" \
  VASSAL.launch.ModuleManager "$@"

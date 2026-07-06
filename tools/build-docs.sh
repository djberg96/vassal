#!/usr/bin/env bash

set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

if [[ -x "$ROOT/mvnw" ]]; then
  MVN="${MVN:-$ROOT/mvnw}"
else
  MVN="${MVN:-mvn}"
fi

"$MVN" -pl vassal-doc generate-resources

mkdir -p "$ROOT/docs"

USER_GUIDE="$ROOT/vassal-doc/target/classes/userguide/userguide.pdf"
DESIGNER_GUIDE="$ROOT/vassal-doc/target/classes/designerguide/designerguide.pdf"

if [[ ! -f "$USER_GUIDE" ]]; then
  echo "User Guide PDF was not generated: $USER_GUIDE" >&2
  exit 1
fi

if [[ ! -f "$DESIGNER_GUIDE" ]]; then
  echo "Designer Guide PDF was not generated: $DESIGNER_GUIDE" >&2
  exit 1
fi

cp "$USER_GUIDE" "$ROOT/docs/userguide.pdf"
cp "$DESIGNER_GUIDE" "$ROOT/docs/designerguide.pdf"

echo "Updated docs/userguide.pdf"
echo "Updated docs/designerguide.pdf"

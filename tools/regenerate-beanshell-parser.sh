#!/usr/bin/env bash
set -euo pipefail

usage() {
  cat <<'EOF'
Usage: tools/regenerate-beanshell-parser.sh [--output DIR] [--full-diff] [--include-support]

Regenerates the vendored BeanShell JJTree/JavaCC parser into a temporary
directory and compares it with the checked-in source tree. This command does
not modify repository files.

Options:
  --output DIR  Regeneration directory. Defaults to
                /private/tmp/vassal-beanshell-parser-gen
  --full-diff   Also write a full git-style diff next to the summary.
  --include-support
                Keep JavaCC-regenerated support classes in the output. By
                default, BeanShell-customized support classes are restored
                after generation so the audit focuses on parser artifacts.
EOF
}

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SOURCE_DIR="$ROOT_DIR/vassal-app/src/main/java/bsh"
OUTPUT_DIR="/private/tmp/vassal-beanshell-parser-gen"
FULL_DIFF=false
INCLUDE_SUPPORT=false

while [[ $# -gt 0 ]]; do
  case "$1" in
    --output)
      if [[ $# -lt 2 ]]; then
        echo "Missing value for --output" >&2
        exit 2
      fi
      OUTPUT_DIR="$2"
      shift 2
      ;;
    --full-diff)
      FULL_DIFF=true
      shift
      ;;
    --include-support)
      INCLUDE_SUPPORT=true
      shift
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      echo "Unknown argument: $1" >&2
      usage >&2
      exit 2
      ;;
  esac
done

JAVACC_VERSION="${JAVACC_VERSION:-7.0.13}"
MAVEN="${MAVEN:-$ROOT_DIR/mvnw}"
JAVACC_JAR="${JAVACC_JAR:-$HOME/.m2/repository/net/java/dev/javacc/javacc/$JAVACC_VERSION/javacc-$JAVACC_VERSION.jar}"

PARSER_FILES=(
  bsh.jj
  Parser.java
  ParserConstants.java
  ParserTokenManager.java
  ParserTreeConstants.java
)

BEANSHELL_SUPPORT_FILES=(
  JavaCharStream.java
  JJTParserState.java
  ParseException.java
  Token.java
  TokenMgrError.java
)

GENERATED_FILES=("${PARSER_FILES[@]}" "${BEANSHELL_SUPPORT_FILES[@]}")

if [[ ! -d "$SOURCE_DIR" ]]; then
  echo "BeanShell source directory not found: $SOURCE_DIR" >&2
  exit 1
fi

if [[ ! -f "$SOURCE_DIR/bsh.jjt" ]]; then
  echo "BeanShell grammar not found: $SOURCE_DIR/bsh.jjt" >&2
  exit 1
fi

OUTPUT_PARENT="$(dirname "$OUTPUT_DIR")"
mkdir -p "$OUTPUT_PARENT"
OUTPUT_PARENT_REAL="$(cd "$OUTPUT_PARENT" && pwd -P)"
case "$OUTPUT_PARENT_REAL" in
  /private/tmp|/private/tmp/*|/tmp|/tmp/*)
    ;;
  *)
    echo "Refusing to delete/regenerate outside /private/tmp or /tmp: $OUTPUT_DIR" >&2
    exit 2
    ;;
esac

if [[ ! -f "$JAVACC_JAR" ]]; then
  "$MAVEN" -q dependency:get \
    -Dartifact="net.java.dev.javacc:javacc:$JAVACC_VERSION" \
    -Dtransitive=false
fi

rm -rf "$OUTPUT_DIR"
mkdir -p "$OUTPUT_DIR"
cp -R "$SOURCE_DIR/." "$OUTPUT_DIR/"

for file in "${GENERATED_FILES[@]}"; do
  rm -f "$OUTPUT_DIR/$file"
done

java -cp "$JAVACC_JAR" org.javacc.jjtree.Main \
  -OUTPUT_DIRECTORY="$OUTPUT_DIR" \
  "$OUTPUT_DIR/bsh.jjt"

java -cp "$JAVACC_JAR" org.javacc.parser.Main \
  -OUTPUT_DIRECTORY="$OUTPUT_DIR" \
  "$OUTPUT_DIR/bsh.jj"

if [[ "$INCLUDE_SUPPORT" == false ]]; then
  for file in "${BEANSHELL_SUPPORT_FILES[@]}"; do
    cp "$SOURCE_DIR/$file" "$OUTPUT_DIR/$file"
  done
fi

perl -0pi -e 's@(/\*\* Constructor\. \*/\n\s*)public ParserTokenManager \(JavaCharStream stream, int lexState\)\{@${1}\x40SuppressWarnings("this-escape")\n  public ParserTokenManager (JavaCharStream stream, int lexState){@' \
  "$OUTPUT_DIR/ParserTokenManager.java"

perl -pi -e 'while (s/^(\t*) +\t/$1\t/) {}' "$OUTPUT_DIR/bsh.jj"

for file in "${PARSER_FILES[@]}"; do
  perl -pi -e 's/[ \t]+$//' "$OUTPUT_DIR/$file"
done

SUMMARY="$OUTPUT_DIR.diff-summary.txt"
FULL="$OUTPUT_DIR.diff"

diff -qr "$SOURCE_DIR" "$OUTPUT_DIR" > "$SUMMARY" || true

echo "Regenerated BeanShell parser into: $OUTPUT_DIR"
echo "Diff summary: $SUMMARY"

if [[ "$FULL_DIFF" == true ]]; then
  git diff --no-index -- "$SOURCE_DIR" "$OUTPUT_DIR" > "$FULL" || true
  echo "Full diff: $FULL"
fi

if [[ -s "$SUMMARY" ]]; then
  echo
  sed -n '1,80p' "$SUMMARY"
else
  echo
  echo "No differences found."
fi

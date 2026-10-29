#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
OUT_DIR="$SCRIPT_DIR/out"
mkdir -p "$OUT_DIR"

JAVA_CMD=""
JAVAC_CMD=""

if [ -n "${JAVA_HOME:-}" ] && [ -x "$JAVA_HOME/bin/java" ] && [ -x "$JAVA_HOME/bin/javac" ]; then
  JAVA_CMD="$JAVA_HOME/bin/java"
  JAVAC_CMD="$JAVA_HOME/bin/javac"
fi

if [ -z "$JAVA_CMD" ]; then
  for candidate in \
    "$(command -v java || true)" \
    /usr/lib/jvm/*/bin/java \
    /usr/local/java/bin/java \
    /opt/java/bin/java; do
    if [ -n "$candidate" ] && [ -x "$candidate" ]; then
      JAVA_CMD="$candidate"
      break
    fi
  done
fi

if [ -z "$JAVAC_CMD" ]; then
  for candidate in \
    "$(command -v javac || true)" \
    /usr/lib/jvm/*/bin/javac \
    /usr/local/java/bin/javac \
    /opt/java/bin/javac; do
    if [ -n "$candidate" ] && [ -x "$candidate" ]; then
      JAVAC_CMD="$candidate"
      break
    fi
  done
fi

if [ -z "$JAVA_CMD" ] || [ ! -x "$JAVA_CMD" ] || [ -z "$JAVAC_CMD" ] || [ ! -x "$JAVAC_CMD" ]; then
  echo "Java JDK was not found. Please install Java 8+ and ensure 'java' and 'javac' are on your PATH."
  exit 1
fi

"$JAVAC_CMD" -d "$OUT_DIR" "$SCRIPT_DIR"/src/*.java
exec "$JAVA_CMD" -cp "$OUT_DIR" Main

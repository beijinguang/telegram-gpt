#!/usr/bin/env bash

set -Eeuo pipefail

PROJECT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_DIR"

# Support the usual Homebrew locations on Apple Silicon and Intel Macs.
if [[ -z "${JAVA_HOME:-}" ]]; then
    for candidate in \
        "/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home" \
        "/usr/local/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home"; do
        if [[ -x "$candidate/bin/java" ]]; then
            export JAVA_HOME="$candidate"
            break
        fi
    done
fi

export PATH="/opt/homebrew/bin:/usr/local/bin:${JAVA_HOME:+$JAVA_HOME/bin:}$PATH"

if ! command -v java >/dev/null 2>&1; then
    echo "未找到 Java 21，请先安装 JDK 21。" >&2
    exit 1
fi

if ! command -v mvn >/dev/null 2>&1; then
    echo "未找到 Maven，请先安装 Maven。" >&2
    exit 1
fi

: "${TELEGRAM_BOT_TOKEN:?请先设置环境变量 TELEGRAM_BOT_TOKEN}"
: "${OPENAI_API_KEY:?请先设置环境变量 OPENAI_API_KEY}"

exec mvn spring-boot:run

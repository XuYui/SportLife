#!/bin/sh

APP_HOME=$(cd "$(dirname "$0")" && pwd -P)
JAVA_EXE="${JAVA_HOME:+$JAVA_HOME/bin/}java"

if ! command -v "$JAVA_EXE" >/dev/null 2>&1; then
  echo "ERROR: JAVA_HOME is not set and no java command could be found." >&2
  exit 1
fi

exec "$JAVA_EXE" "-Xmx64m" "-Xms64m" -classpath "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain "$@"

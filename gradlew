#!/usr/bin/env sh

# Gradle wrapper launcher.
DIR="$(cd "$(dirname "$0")" && pwd)"
JAVA_CMD=${JAVA_HOME:+"$JAVA_HOME/bin/java"}
JAVA_CMD=${JAVA_CMD:-java}
CLASSPATH="$DIR/gradle/wrapper/gradle-wrapper.jar"

exec "$JAVA_CMD" -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"

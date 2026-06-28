#!/bin/sh
set -e

GRADLE_OPTS="${GRADLE_OPTS:-}"
JAVA_OPTS="${JAVA_OPTS:-}"

CLASSPATH="${APP_HOME}/gradle/wrapper/gradle-wrapper.jar"

GRADLE_USER_HOME="${GRADLE_USER_HOME:-${HOME}/.gradle}"

find_java_home() {
  if [ -n "${JAVA_HOME}" ]; then
    echo "${JAVA_HOME}"
  else
    echo ""
  fi
}

JAVA_HOME="$(find_java_home)"

if [ -z "${JAVA_HOME}" ]; then
  JAVACMD="java"
else
  JAVACMD="${JAVA_HOME}/bin/java"
fi

exec "${JAVACMD}" \
  ${JAVA_OPTS} \
  -classpath "${CLASSPATH}" \
  org.gradle.wrapper.GradleWrapperMain \
  "$@"

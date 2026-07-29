#!/bin/sh
APP_HOME=$(pwd); JAVA_CMD="java"; DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'; set -- "-Dorg.gradle.appname=${0##*/}" -classpath "\"${APP_HOME}/gradle/wrapper/gradle-wrapper.jar\"" org.gradle.wrapper.GradleWrapperMain "$@"; eval exec "$JAVA_CMD" "$@"

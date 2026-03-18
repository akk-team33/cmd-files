#!/bin/sh
JAVA_HOME="/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home"
JAVA="$JAVA_HOME/bin/java"
JAR_FILE="files-fat/target/files-fat.jar"
exec "$JAVA" -jar "$JAR_FILE" "$0" "$@"

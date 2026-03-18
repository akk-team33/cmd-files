@echo off
setlocal
rem set "JAVA_HOME=X:\path\to\java\home"
rem set "JAVA=%JAVA_HOME%\bin\java.exe"
set JAVA=java
set "JAR_FILE=files-fat\target\files-fat.jar"
"%JAVA%" -jar "%JAR_FILE%" "%0" %*
endlocal

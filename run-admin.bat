@echo off
set "JAVA_HOME=%~dp0src\user\src\main\java\user\ui\oracleJdk-27"
set "PATH=%JAVA_HOME%\bin;%PATH%"
set "MVN_CMD=C:\Users\ADMIN\.vscode\extensions\oracle.oracle-java-27.0.0\nbcode\java\maven\bin\mvn.cmd"

echo ========================================================
echo  Launching Admin Console UI (Chat System)...
echo ========================================================
call "%MVN_CMD%" -f src/admin/pom.xml javafx:run
pause

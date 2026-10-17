$env:JAVA_HOME = "$PSScriptRoot\src\user\src\main\java\user\ui\oracleJdk-27"
$env:Path = "$env:JAVA_HOME\bin;" + $env:Path
$mvn = "C:\Users\ADMIN\.vscode\extensions\oracle.oracle-java-27.0.0\nbcode\java\maven\bin\mvn.cmd"
Write-Host "Launching Admin Console UI..." -ForegroundColor Cyan
& $mvn -f src/admin/pom.xml javafx:run

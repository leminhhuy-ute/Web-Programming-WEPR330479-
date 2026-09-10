param([switch]$Test, [switch]$Build)
$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
$bundledJdk = Get-ChildItem (Join-Path $PSScriptRoot '.tools/jdk21') -Directory -ErrorAction SilentlyContinue | Select-Object -First 1
if ($bundledJdk) { $env:JAVA_HOME = $bundledJdk.FullName }
if (-not $env:JAVA_HOME) { throw 'Set JAVA_HOME to a JDK 21 installation before running.' }
if ($Test) { & .\mvnw.cmd test }
elseif ($Build) { & .\mvnw.cmd package }
else { & .\mvnw.cmd spring-boot:run }
exit $LASTEXITCODE

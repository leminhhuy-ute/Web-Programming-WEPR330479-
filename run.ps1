param([switch]$Test, [switch]$Build)
$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
if ($Test) { & .\mvnw.cmd test }
elseif ($Build) { & .\mvnw.cmd clean package }
else { & .\mvnw.cmd spring-boot:run }
exit $LASTEXITCODE

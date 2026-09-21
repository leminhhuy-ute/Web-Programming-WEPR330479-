[CmdletBinding()]
param(
    [string]$CatalinaBase
)

$ErrorActionPreference = 'Stop'

if ([string]::IsNullOrWhiteSpace($CatalinaBase)) {
    $CatalinaBase = $env:CATALINA_BASE
}
if ([string]::IsNullOrWhiteSpace($CatalinaBase) -and (Test-Path -LiteralPath 'D:\Tomcat 11.0')) {
    $CatalinaBase = 'D:\Tomcat 11.0'
}
if ([string]::IsNullOrWhiteSpace($CatalinaBase)) {
    throw 'CATALINA_BASE was not found. Run again with -CatalinaBase <Tomcat-path>.'
}

$template = Join-Path $PSScriptRoot '..\config\db.properties.example'
$template = (Resolve-Path -LiteralPath $template).Path
$destinationDirectory = Join-Path $CatalinaBase 'conf\topic-management'
$destination = Join-Path $destinationDirectory 'db.properties'

New-Item -ItemType Directory -Path $destinationDirectory -Force | Out-Null
if (Test-Path -LiteralPath $destination) {
    Write-Host "Existing configuration was kept unchanged: $destination"
    Write-Host 'Check db.url, db.username and db.password before restarting Tomcat.'
    return
}

Copy-Item -LiteralPath $template -Destination $destination
Write-Host "Created template: $destination"
Write-Host 'Edit this file and replace <YOUR_MYSQL_USERNAME> and <YOUR_MYSQL_PASSWORD> before restarting Tomcat.'

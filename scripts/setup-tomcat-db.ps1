param(
    [Parameter(Mandatory=$true)]
    [string]$CatalinaBase,
    [string]$DbUrl = "jdbc:mysql://localhost:3306/student_topic_management?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Ho_Chi_Minh",
    [string]$DbUsername = "root",
    [string]$DbPassword = ""
)

$targetDir = Join-Path -Path $CatalinaBase -ChildPath "conf\topic-management"
if (!(Test-Path -Path $targetDir)) {
    New-Item -ItemType Directory -Path $targetDir -Force | Out-Null
    Write-Host "Created directory: $targetDir"
}

$targetFile = Join-Path -Path $targetDir -ChildPath "db.properties"
$content = @"
db.url=$DbUrl
db.username=$DbUsername
db.password=$DbPassword
"@

Set-Content -Path $targetFile -Value $content -Encoding UTF8
Write-Host "Successfully generated Tomcat DB configuration at: $targetFile"

# Audit evidence only. Run exclusively against the disposable H2 instance on port 18080.
$ErrorActionPreference = 'Stop'
$auditBase = 'http://127.0.0.1:18080'
$auditResults = [System.Collections.Generic.List[object]]::new()
function New-AuditSession($username, $password) {
    $session = [Microsoft.PowerShell.Commands.WebRequestSession]::new()
    $null = Invoke-RestMethod "$auditBase/api/auth/csrf" -WebSession $session
    $token = $session.Cookies.GetCookies($auditBase)['XSRF-TOKEN'].Value
    $null = Invoke-RestMethod "$auditBase/api/auth/login" -Method Post -WebSession $session -Headers @{'X-XSRF-TOKEN'=$token} -ContentType 'application/json' -Body (@{username=$username;password=$password}|ConvertTo-Json)
    return $session
}
function Invoke-Audit($label, $session, $method, $path, $body=$null) {
    $parameters = @{Uri="$auditBase$path";Method=$method;WebSession=$session;SkipHttpErrorCheck=$true}
    if ($method -ne 'GET') {
        $parameters.Headers = @{'X-XSRF-TOKEN'=$session.Cookies.GetCookies($auditBase)['XSRF-TOKEN'].Value}
        $parameters.ContentType='application/json'
        if ($null -ne $body) { $parameters.Body=$body|ConvertTo-Json -Depth 10 }
    }
    $response=Invoke-WebRequest @parameters
    $parsed=try {$response.Content|ConvertFrom-Json} catch {$response.Content}
    $auditResults.Add([pscustomobject]@{label=$label;method=$method;path=$path;status=$response.StatusCode;response=$parsed})
    return $parsed
}
$admin=New-AuditSession 'audit_admin' 'AuditOnly2026!'
$now=Get-Date
$period=@{name='Audit period';type='COURSE';lecturerStartAt=$now.AddDays(-2).ToString('s');lecturerEndAt=$now.AddDays(1).ToString('s');studentStartAt=$now.AddDays(2).ToString('s');studentEndAt=$now.AddDays(5).ToString('s')}
$p=Invoke-Audit 'Create valid period' $admin POST '/api/admin/registration-periods' $period
$badPeriod=$period.Clone();$badPeriod.name='Invalid dates';$badPeriod.lecturerEndAt=$now.AddDays(-3).ToString('s')
$null=Invoke-Audit 'Invalid period dates' $admin POST '/api/admin/registration-periods' $badPeriod
$user=@{userCode='AUDIT_LECT';username='audit_lect';fullName='Audit lecturer';email='audit-lect@lecturer.hcmute.edu.vn';role='LECTURER';departmentId=1;status='ACTIVE';password='AuditOnly2026!'}
$lect=Invoke-Audit 'Create audit lecturer' $admin POST '/api/admin/users' $user
$lectSession=New-AuditSession 'audit_lect' 'AuditOnly2026!'
$topic=@{topicCode='AUDIT-TOPIC';title='Audit topic';description='Meaningful audit topic description';maxStudents=3;topicType='COURSE';departmentId=1;periodId=$p.data.id}
$t=Invoke-Audit 'Create topic with zero supervisors' $lectSession POST '/api/lecturer/topics' $topic
$null=Invoke-Audit 'Lock topic creator' $admin PATCH "/api/admin/users/$($lect.data.id)/status" @{status='LOCKED'}
$null=Invoke-Audit 'Approve topic with locked default supervisor' $admin POST "/api/lecturer/department-topics/$($t.data.id)/approval" @{status='APPROVED'}
$period.type='TLCN';$period.reviewDeadline=$now.AddDays(8).ToString('s')
$null=Invoke-Audit 'Change period type after approved topic exists' $admin PUT "/api/admin/registration-periods/$($p.data.id)" $period
$null=Invoke-Audit 'Read topic with inconsistent period type' $admin GET "/api/lecturer/topics/$($t.data.id)"
$null=Invoke-Audit 'Null council roster elements' $admin POST '/api/councils' @{code='AUDIT-NULL';name='Audit null council';defenseDate=$now.AddDays(12).ToString('s');room='A1';members=@($null,$null,$null)}
$user.status='ACTIVE';$user.userCode='AUDIT_CASE1';$user.username='AuditCase';$user.email='auditcase1@lecturer.hcmute.edu.vn'
$null=Invoke-Audit 'Create first case-variant login' $admin POST '/api/admin/users' $user
$user.userCode='AUDIT_CASE2';$user.username='auditcase';$user.email='auditcase2@lecturer.hcmute.edu.vn'
$null=Invoke-Audit 'Create second case-variant login' $admin POST '/api/admin/users' $user
$loginSession=[Microsoft.PowerShell.Commands.WebRequestSession]::new()
$null=Invoke-RestMethod "$auditBase/api/auth/csrf" -WebSession $loginSession
$null=Invoke-Audit 'Login with ambiguous case-insensitive username' $loginSession POST '/api/auth/login' @{username='AuditCase';password='AuditOnly2026!'}
$user.userCode='AUDIT_PASS';$user.username='auditpass';$user.email='auditpass@lecturer.hcmute.edu.vn'
$passUser=Invoke-Audit 'Create password-reset subject' $admin POST '/api/admin/users' $user
$oldSession=New-AuditSession 'auditpass' 'AuditOnly2026!'
$user.password='ChangedAudit2026!'
$null=Invoke-Audit 'Change subject password as admin' $admin PUT "/api/admin/users/$($passUser.data.id)" $user
$null=Invoke-Audit 'Old session after password reset' $oldSession GET '/api/auth/me'
$user.userCode='AUDIT_LONGPW';$user.username='auditlongpw';$user.email='auditlongpw@lecturer.hcmute.edu.vn';$user.password='x'*80
$null=Invoke-Audit 'DTO accepts 80-byte password but BCrypt rejects' $admin POST '/api/admin/users' $user
$auditResults|ConvertTo-Json -Depth 20|Set-Content -LiteralPath "$PSScriptRoot/runtime-probes.json" -Encoding utf8
$auditResults|Select-Object label,status|Format-Table -AutoSize

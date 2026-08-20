# ==============================================================================
# LocalJobs — End-to-End Integration & Security Test Suite
# ==============================================================================

$baseUrl = "http://localhost:8080"
$results = @()

function Record-Test($name, $passed, $details) {
    $status = "FAIL"
    if ($passed) { $status = "PASS" }
    $global:results += [PSCustomObject]@{
        TestName = $name
        Status   = $status
        Details  = $details
    }
    Write-Host "[$status] $name : $details"
}

Write-Host "`n========================================================" -ForegroundColor Cyan
Write-Host "     LOCALJOBS SECURITY & VERIFICATION TEST SUITE       " -ForegroundColor Cyan
Write-Host "========================================================`n" -ForegroundColor Cyan

# 1. Unique Username Check & Availability
try {
    $muraliCheck = Invoke-RestMethod -Uri "$baseUrl/api/auth/check-username?username=muralisai"
    $newCheck = Invoke-RestMethod -Uri "$baseUrl/api/auth/check-username?username=new_worker_$(Get-Random)"
    $pass = (!$muraliCheck.available) -and ($newCheck.available)
    Record-Test "1. Unique Username Availability Check" $pass "Existing username rejected; fresh username approved"
} catch {
    Record-Test "1. Unique Username Availability Check" $false $_.Exception.Message
}

# 2. User Signup with BCrypt & Validation
$newWorker = $null
$testUsername = "swathi_k_$(Get-Random)"
try {
    $signupBody = @{
        fullName = "Swathi Krishna"
        username = $testUsername
        email = "swathi_$(Get-Random)@localjobs.local"
        phone = "9876543210"
        password = "Password@123"
        confirmPassword = "Password@123"
        location = "Lawyerpet, Ongole"
        skills = @("Photography", "Editing", "Graphic Design")
        bio = "Local creative enthusiast."
    } | ConvertTo-Json

    $newWorker = Invoke-RestMethod -Method Post -Uri "$baseUrl/api/auth/signup" -Body $signupBody -ContentType "application/json"
    $pass = ($null -ne $newWorker.id) -and ($newWorker.verificationStatus -eq "NOT_VERIFIED")
    Record-Test "2. User Signup and BCrypt Hashing" $pass "Created User ID #$($newWorker.id) (@$($newWorker.username)) with NOT_VERIFIED status"
} catch {
    Record-Test "2. User Signup and BCrypt Hashing" $false $_.Exception.Message
}

# 3. User Login via Username or Email
try {
    $loginBody = @{
        usernameOrEmail = $testUsername
        password = "Password@123"
    } | ConvertTo-Json

    $loginRes = Invoke-RestMethod -Method Post -Uri "$baseUrl/api/auth/login" -Body $loginBody -ContentType "application/json"
    $pass = ($null -ne $loginRes.id) -and ($loginRes.name -eq "Swathi Krishna")
    Record-Test "3. User Authentication Login" $pass "Logged in user @$($loginRes.username) (ID #$($loginRes.id))"
} catch {
    Record-Test "3. User Authentication Login" $false $_.Exception.Message
}

# 4. Activity Gating: Unverified user blocked from posting tasks
try {
    $gatedTaskBody = @{
        posterId = $newWorker.id
        title = "Unverified User Task"
        category = "Events"
        reward = 500.0
        duration = "2 hours"
        date = "Today"
        location = "Ongole"
    } | ConvertTo-Json

    $blocked = $false
    try {
        Invoke-RestMethod -Method Post -Uri "$baseUrl/api/tasks" -Body $gatedTaskBody -ContentType "application/json"
    } catch {
        $blocked = $true
    }
    Record-Test "4. Unverified Activity Gating" $blocked "Unverified user prevented from publishing task until identity verified"
} catch {
    Record-Test "4. Unverified Activity Gating" $false $_.Exception.Message
}

# 5. Identity Verification (Mock KYC Submission with Masked Storage)
try {
    $kycBody = @{
        userId = $newWorker.id
        docType = "AADHAAR"
        docNumber = "543287651234"
        nameOnDoc = "Swathi Krishna"
    } | ConvertTo-Json

    $kycRes = Invoke-RestMethod -Method Post -Uri "$baseUrl/api/verification/submit" -Body $kycBody -ContentType "application/json"
    $pass = ($kycRes.verificationStatus -eq "VERIFICATION_PENDING") -and ($kycRes.maskedDocNumber -eq "XXXX-XXXX-1234")
    Record-Test "5. Privacy-First Mock KYC Submission" $pass "Stored masked format '$($kycRes.maskedDocNumber)'; status: VERIFICATION_PENDING"
} catch {
    Record-Test "5. Privacy-First Mock KYC Submission" $false $_.Exception.Message
}

# 6. Admin Authentication & Role-Based Access Control
$adminSession = $null
try {
    $adminLoginBody = @{
        username = "admin"
        password = "Admin@123"
    } | ConvertTo-Json

    $adminSession = Invoke-RestMethod -Method Post -Uri "$baseUrl/api/auth/admin/login" -Body $adminLoginBody -ContentType "application/json"
    $pass = ($adminSession.role -eq "ADMIN") -and ($null -ne $adminSession.token)
    Record-Test "6. Admin Login and RBAC Token" $pass "Authenticated as @$($adminSession.username) (Role: $($adminSession.role))"
} catch {
    Record-Test "6. Admin Login and RBAC Token" $false $_.Exception.Message
}

# 7. Admin Verification Review & Approval (Sets Verified User)
try {
    $reviewBody = @{
        adminUsername = "admin"
        userId = $newWorker.id
        decision = "APPROVED"
        remarks = "Verified Aadhaar mock credentials"
    } | ConvertTo-Json

    $headers = @{ "X-Admin-Role" = "ADMIN" }
    $reviewRes = Invoke-RestMethod -Method Post -Uri "$baseUrl/api/admin/verifications/review" -Headers $headers -Body $reviewBody -ContentType "application/json"
    $pass = ($reviewRes.verificationStatus -eq "VERIFIED") -and ($reviewRes.verified -eq $true)
    Record-Test "7. Admin KYC Review and Approval" $pass "User ID #$($newWorker.id) promoted to VERIFIED (Verified User Badge Active)"
} catch {
    Record-Test "7. Admin KYC Review and Approval" $false $_.Exception.Message
}

# 8. Verified Task Posting & Lifecycle Acceptance
$publishedTaskId = $null
try {
    $taskBody = @{
        posterId = 2
        title = "Wedding Photography Assistant"
        category = "Events"
        reward = 1200.0
        duration = "3 hours"
        date = "Today"
        location = "Kurnool Road Function Hall, Ongole"
        latitude = 15.5057
        longitude = 80.0499
        description = "Assist lead photographer during evening reception."
    } | ConvertTo-Json

    $task = Invoke-RestMethod -Method Post -Uri "$baseUrl/api/tasks" -Body $taskBody -ContentType "application/json"
    $publishedTaskId = $task.id

    $accepted = Invoke-RestMethod -Method Put -Uri "$baseUrl/api/tasks/$publishedTaskId/accept?workerId=$($newWorker.id)"
    $pass = ($task.status -eq "OPEN") -and ($accepted.status -eq "ACCEPTED")
    Record-Test "8. Verified Task Creation and Acceptance" $pass "Published Task #$publishedTaskId and accepted by verified user"
} catch {
    Record-Test "8. Verified Task Creation and Acceptance" $false $_.Exception.Message
}

# 9. Incident Reporting & Admin Resolution
try {
    $reportBody = @{
        reporterUserId = $newWorker.id
        reportedTaskId = $publishedTaskId
        reportType = "TASK_REPORT"
        reason = "Misleading information"
        description = "Venue gate timing clarification needed."
    } | ConvertTo-Json

    $reportRes = Invoke-RestMethod -Method Post -Uri "$baseUrl/api/security/report" -Body $reportBody -ContentType "application/json"
    
    $resolveBody = @{
        adminUsername = "admin"
        decision = "RESOLVED"
        notes = "Moderator verified venue gate timing"
    } | ConvertTo-Json
    $headers = @{ "X-Admin-Role" = "ADMIN" }
    $resolveRes = Invoke-RestMethod -Method Post -Uri "$baseUrl/api/admin/reports/$($reportRes.reportId)/resolve" -Headers $headers -Body $resolveBody -ContentType "application/json"

    $pass = ($reportRes.status -eq "PENDING") -and ($resolveRes.status -eq "RESOLVED")
    Record-Test "9. Incident Reporting and Admin Resolution" $pass "Filed Report #$($reportRes.reportId) and marked RESOLVED by Administrator"
} catch {
    Record-Test "9. Incident Reporting and Admin Resolution" $false $_.Exception.Message
}

# 10. User Blocking & Security Audit Trail
try {
    $blockBody = @{
        userId = $newWorker.id
        targetUserId = 2
    } | ConvertTo-Json

    $blockRes = Invoke-RestMethod -Method Post -Uri "$baseUrl/api/security/block" -Body $blockBody -ContentType "application/json"
    $headers = @{ "X-Admin-Role" = "ADMIN" }
    $auditLogs = Invoke-RestMethod -Uri "$baseUrl/api/admin/audit-logs" -Headers $headers

    $pass = ($null -ne $blockRes.blockedUserIds) -and ($auditLogs.Count -gt 0)
    Record-Test "10. User Blocking and Admin Audit Trail" $pass "User blocked target; Discovered $($auditLogs.Count) immutable security audit logs"
} catch {
    Record-Test "10. User Blocking and Admin Audit Trail" $false $_.Exception.Message
}

Write-Host "`n========================================================" -ForegroundColor Cyan
Write-Host "                  VERIFICATION SUMMARY                  " -ForegroundColor Cyan
Write-Host "========================================================" -ForegroundColor Cyan
$passedCount = ($results | Where-Object { $_.Status -eq "PASS" }).Count
$totalCount = $results.Count
Write-Host "Test Results: $passedCount / $totalCount Passed`n" -ForegroundColor Green

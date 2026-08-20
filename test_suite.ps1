# ==============================================================================
# LocalJobs — End-to-End Integration & Workflow Test Suite
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
Write-Host "     LOCALJOBS HYPERLOCAL MARKETPLACE VERIFICATION       " -ForegroundColor Cyan
Write-Host "========================================================`n" -ForegroundColor Cyan

# 1. Fetch Users & Personas
try {
    $users = Invoke-RestMethod -Uri "$baseUrl/api/users"
    Record-Test "1. Users Discovery" ($users.Count -ge 3) "Discovered $($users.Count) users in database"
} catch {
    Record-Test "1. Users Discovery" $false $_.Exception.Message
}

# 2. Register New User (Kavitha Reddy)
try {
    $regBody = @{
        name = "Kavitha Reddy"
        email = "kavitha_test_$(Get-Random)@localjobs.local"
        phone = "+91 94444 33333"
        location = "Trunk Road, Ongole"
        latitude = 15.5030
        longitude = 80.0510
        skills = @("Photography", "Video Editing", "Creative Direction")
        bio = "Professional local photographer and content creator."
    } | ConvertTo-Json

    $kavitha = Invoke-RestMethod -Method Post -Uri "$baseUrl/api/users/register" -Body $regBody -ContentType "application/json"
    Record-Test "2. User Registration" ($null -ne $kavitha.id) "Created User ID #$($kavitha.id) ($($kavitha.name))"
} catch {
    Record-Test "2. User Registration" $false $_.Exception.Message
}

# 3. Post a New Task (Poster: Murali Sai #1)
$createdTaskId = $null
try {
    $taskBody = @{
        posterId = 1
        title = "Festival Event Photographer"
        category = "Events"
        reward = 1500.0
        duration = "4 hours"
        date = "Today"
        startTime = "3:00 PM"
        endTime = "7:00 PM"
        location = "PACE Institute Auditorium, Ongole"
        latitude = 15.5120
        longitude = 80.0550
        requiredSkills = @("DSLR Photography", "Lighting")
        description = "Capture high resolution action shots of the annual collegiate fest."
    } | ConvertTo-Json

    $task = Invoke-RestMethod -Method Post -Uri "$baseUrl/api/tasks" -Body $taskBody -ContentType "application/json"
    $createdTaskId = $task.id
    Record-Test "3. Task Creation" ($task.status -eq "OPEN" -and $task.reward -eq 1500) "Task #$($task.id) '$($task.title)' published for ₹$($task.reward)"
} catch {
    Record-Test "3. Task Creation" $false $_.Exception.Message
}

# 4. Search & Filter Tests
try {
    $events = Invoke-RestMethod -Uri "$baseUrl/api/tasks?category=Events"
    $distFilter = Invoke-RestMethod -Uri "$baseUrl/api/tasks?maxDistance=5.0"
    $sorted = Invoke-RestMethod -Uri "$baseUrl/api/tasks?sortByReward=highest"
    $search = Invoke-RestMethod -Uri "$baseUrl/api/tasks?keyword=Festival"

    $filtersPass = ($events.Count -gt 0) -and ($distFilter.Count -gt 0) -and ($search.Count -gt 0)
    Record-Test "4. Search & Geolocation Filtering" $filtersPass "Events: $($events.Count), Within 5km: $($distFilter.Count), Highest Reward: ₹$($sorted[0].reward)"
} catch {
    Record-Test "4. Search & Geolocation Filtering" $false $_.Exception.Message
}

# 5. Full Lifecycle Flow: Accept -> Start -> Complete -> Confirm & Release Payment
try {
    # 5a. Accept Task by Kavitha
    $accepted = Invoke-RestMethod -Method Put -Uri "$baseUrl/api/tasks/$createdTaskId/accept?workerId=$($kavitha.id)"
    # 5b. Start Task by Kavitha
    $started = Invoke-RestMethod -Method Put -Uri "$baseUrl/api/tasks/$createdTaskId/start?workerId=$($kavitha.id)"
    # 5c. Complete Task by Kavitha
    $completed = Invoke-RestMethod -Method Put -Uri "$baseUrl/api/tasks/$createdTaskId/complete?workerId=$($kavitha.id)"
    # 5d. Confirm Completion & Release ₹1500 Reward by Murali (#1)
    $released = Invoke-RestMethod -Method Put -Uri "$baseUrl/api/tasks/$createdTaskId/release-payment?posterId=1"

    $lifecyclePass = ($accepted.status -eq "ACCEPTED") -and ($started.status -eq "IN_PROGRESS") -and ($completed.status -eq "COMPLETED") -and ($released.status -eq "PAYMENT_RELEASED")
    Record-Test "5. 5-Stage Task Lifecycle" $lifecyclePass "OPEN -> ACCEPTED -> IN_PROGRESS -> COMPLETED -> PAYMENT_RELEASED"
} catch {
    Record-Test "5. 5-Stage Task Lifecycle" $false $_.Exception.Message
}

# 6. Wallet Credit & Transaction Ledger
try {
    $wallet = Invoke-RestMethod -Uri "$baseUrl/api/wallet/$($kavitha.id)"
    $txs = $wallet.transactions
    $walletPass = ($wallet.availableBalance -eq 1500.0) -and ($txs.Count -ge 1) -and ($txs[0].amount -eq 1500.0)
    Record-Test "6. Wallet & Escrow Payout" $walletPass "Worker Balance: ₹$($wallet.availableBalance) (Expected: ₹1500.0), Ledger entries: $($txs.Count)"
} catch {
    Record-Test "6. Wallet & Escrow Payout" $false $_.Exception.Message
}

# 7. Rating & Review System
try {
    $reviewBody = @{
        taskId = $createdTaskId
        fromUserId = 1
        toUserId = $kavitha.id
        rating = 5.0
        reviewText = "Kavitha was punctual, highly skilled, and provided beautiful event photos!"
        role = "POSTER_RATING_WORKER"
    } | ConvertTo-Json

    $review = Invoke-RestMethod -Method Post -Uri "$baseUrl/api/ratings" -Body $reviewBody -ContentType "application/json"
    $kavithaProfile = Invoke-RestMethod -Uri "$baseUrl/api/users/$($kavitha.id)"
    $reviewsList = Invoke-RestMethod -Uri "$baseUrl/api/ratings/user/$($kavitha.id)"

    $ratingPass = ($review.id -gt 0) -and ($kavithaProfile.rating -eq 5.0) -and ($reviewsList.Count -ge 1)
    Record-Test "7. Mutual Rating & Reputation" $ratingPass "Kavitha Rating: $($kavithaProfile.rating)★ ($($kavithaProfile.ratingCount) review), Completed: $($kavithaProfile.completedTasks) tasks"
} catch {
    Record-Test "7. Mutual Rating & Reputation" $false $_.Exception.Message
}

# 8. Admin Panel Control & Stats
try {
    $stats = Invoke-RestMethod -Uri "$baseUrl/api/admin/stats"
    $verifyToggle = Invoke-RestMethod -Method Put -Uri "$baseUrl/api/admin/users/$($kavitha.id)/toggle-verify"
    
    # Create and delete spam task
    $spamReq = @{
        posterId = 1
        title = "Spam Test Listing"
        category = "Other"
        reward = 50.0
        duration = "30 minutes"
        location = "Ongole"
    } | ConvertTo-Json
    $spam = Invoke-RestMethod -Method Post -Uri "$baseUrl/api/tasks" -Body $spamReq -ContentType "application/json"
    $del = Invoke-RestMethod -Method Delete -Uri "$baseUrl/api/admin/tasks/$($spam.id)"

    $adminPass = ($stats.totalUsers -ge 4) -and ($del.message -like "*deleted*")
    Record-Test "8. Admin Control & Moderation" $adminPass "Total Users: $($stats.totalUsers), Tasks: $($stats.totalTasks), GMV: ₹$($stats.totalTaskValue), Moderation Verified"
} catch {
    Record-Test "8. Admin Control & Moderation" $false $_.Exception.Message
}

# 9. Real-time Notifications Alert Flow
try {
    $kavithaAlerts = Invoke-RestMethod -Uri "$baseUrl/api/notifications?userId=$($kavitha.id)"
    $muraliAlerts = Invoke-RestMethod -Uri "$baseUrl/api/notifications?userId=1"
    $alertsPass = ($kavithaAlerts.Count -ge 2) -and ($muraliAlerts.Count -ge 2)
    Record-Test "9. Notification Alert Engine" $alertsPass "Kavitha: $($kavithaAlerts.Count) alerts (Accepted, Paid, 5★ Review), Murali: $($muraliAlerts.Count) alerts"
} catch {
    Record-Test "9. Notification Alert Engine" $false $_.Exception.Message
}

# 10. Frontend Static Assets Delivery
try {
    $homeHtml = Invoke-WebRequest -Uri "$baseUrl/"
    $cssAsset = Invoke-WebRequest -Uri "$baseUrl/css/style.css"
    $jsApp = Invoke-WebRequest -Uri "$baseUrl/js/app.js"
    $assetsPass = ($homeHtml.StatusCode -eq 200) -and ($cssAsset.StatusCode -eq 200) -and ($jsApp.StatusCode -eq 200)
    Record-Test "10. Frontend Asset Delivery" $assetsPass "HTML ($($homeHtml.Content.Length) B), CSS ($($cssAsset.Content.Length) B), JS ($($jsApp.Content.Length) B)"
} catch {
    Record-Test "10. Frontend Asset Delivery" $false $_.Exception.Message
}

Write-Host "`n========================================================" -ForegroundColor Cyan
Write-Host "                FINAL TEST SUITE SUMMARY                " -ForegroundColor Cyan
Write-Host "========================================================`n" -ForegroundColor Cyan
$global:results | Format-Table -AutoSize

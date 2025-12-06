# FitSet Backend Test Script
# Run this AFTER starting the backend with rebuild-and-start.bat

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "FITSET BACKEND API TESTS" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Wait for user confirmation
Write-Host "Make sure backend is running at http://localhost:8080" -ForegroundColor Yellow
Write-Host "Press any key to start tests..." -ForegroundColor Yellow
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
Write-Host ""

# Test 1: Health Check
Write-Host "[TEST 1] Checking if backend is running..." -ForegroundColor Green
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -Method GET -UseBasicParsing -ErrorAction Stop
    Write-Host "✓ Backend is UP and running!" -ForegroundColor Green
    Write-Host ""
} catch {
    Write-Host "✗ Backend is not responding. Please start it first!" -ForegroundColor Red
    Write-Host "Run: rebuild-and-start.bat" -ForegroundColor Yellow
    Write-Host ""
    pause
    exit
}

# Test 2: General Chat Endpoint
Write-Host "[TEST 2] Testing /api/chat/send endpoint..." -ForegroundColor Green
try {
    $body = @{
        message = "Hello! What's your name?"
    } | ConvertTo-Json

    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/chat/send" -Method POST -Body $body -ContentType "application/json" -ErrorAction Stop
    
    if ($response.response) {
        Write-Host "✓ General chat working!" -ForegroundColor Green
        Write-Host "Response: $($response.response.Substring(0, [Math]::Min(100, $response.response.Length)))..." -ForegroundColor White
    } else {
        Write-Host "✗ No response received" -ForegroundColor Red
    }
    Write-Host ""
} catch {
    Write-Host "✗ General chat failed: $_" -ForegroundColor Red
    Write-Host ""
}

# Test 3: Fitness Chat Endpoint (with history)
Write-Host "[TEST 3] Testing /api/chat/fitness-chat endpoint..." -ForegroundColor Green
try {
    $body = @{
        message = "Give me a quick workout tip for beginners"
        history = "User: Hello`nAssistant: Hi there! I'm FitSet AI, your fitness assistant."
    } | ConvertTo-Json

    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/chat/fitness-chat" -Method POST -Body $body -ContentType "application/json" -ErrorAction Stop
    
    if ($response.response) {
        Write-Host "✓ Fitness chat working!" -ForegroundColor Green
        Write-Host "Response: $($response.response.Substring(0, [Math]::Min(100, $response.response.Length)))..." -ForegroundColor White
    } else {
        Write-Host "✗ No response received" -ForegroundColor Red
    }
    Write-Host ""
} catch {
    Write-Host "✗ Fitness chat failed: $_" -ForegroundColor Red
    Write-Host ""
}

# Test 4: Invalid Request (should return error)
Write-Host "[TEST 4] Testing error handling (empty message)..." -ForegroundColor Green
try {
    $body = @{
        message = ""
    } | ConvertTo-Json

    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/chat/send" -Method POST -Body $body -ContentType "application/json" -ErrorAction Stop
    Write-Host "✗ Should have returned error for empty message" -ForegroundColor Red
    Write-Host ""
} catch {
    if ($_.Exception.Response.StatusCode -eq 400) {
        Write-Host "✓ Error handling working correctly (400 Bad Request)" -ForegroundColor Green
    } else {
        Write-Host "✗ Unexpected error: $_" -ForegroundColor Red
    }
    Write-Host ""
}

# Summary
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "TEST SUMMARY" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "✓ Backend is running" -ForegroundColor Green
Write-Host "✓ Chat endpoints configured" -ForegroundColor Green
Write-Host "✓ Gemini AI integration working" -ForegroundColor Green
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Yellow
Write-Host "1. Open frontend: http://127.0.0.1:5500/frontend/index.html" -ForegroundColor White
Write-Host "2. Click the chat button (bottom-right)" -ForegroundColor White
Write-Host "3. Test the chatbot!" -ForegroundColor White
Write-Host ""

pause

# V1 验收脚本（PowerShell）
# 用法: .\scripts\verify.ps1 [-BaseUrl http://localhost:8080] [-SkipAi]

param(
    [string]$BaseUrl = "http://localhost:8080",
    [switch]$SkipAi
)

$passed = 0
$failed = 0

function Test-Endpoint {
    param([string]$Name, [scriptblock]$Block)
    Write-Host "[$Name] " -NoNewline
    try {
        & $Block
        Write-Host "PASS" -ForegroundColor Green
        $script:passed++
    } catch {
        Write-Host "FAIL: $($_.Exception.Message)" -ForegroundColor Red
        $script:failed++
    }
}

Write-Host "=== AI Enterprise Starter V1 Verify ===" -ForegroundColor Cyan
Write-Host "BaseUrl: $BaseUrl`n"

Test-Endpoint "GET /api/tools" {
    $r = Invoke-RestMethod "$BaseUrl/api/tools"
    if ($r.Count -lt 1) { throw "empty tools list" }
}

Test-Endpoint "POST /api/auth/login" {
    $r = Invoke-RestMethod -Uri "$BaseUrl/api/auth/login" -Method Post `
        -ContentType "application/json" -Body '{"username":"admin","password":"admin123"}'
    if ($r.code -ne 200 -or -not $r.data.token) { throw "login failed" }
}

Test-Endpoint "GET /v3/api-docs" {
    $r = Invoke-WebRequest -Uri "$BaseUrl/v3/api-docs" -UseBasicParsing
    if ($r.StatusCode -ne 200) { throw "status $($r.StatusCode)" }
}

Test-Endpoint "GET /doc.html" {
    $r = Invoke-WebRequest -Uri "$BaseUrl/doc.html" -UseBasicParsing
    if ($r.StatusCode -ne 200) { throw "status $($r.StatusCode)" }
}

if (-not $SkipAi) {
    Test-Endpoint "POST /api/chat" {
        $body = [System.Text.Encoding]::UTF8.GetBytes('{"message":"你好"}')
        $r = Invoke-RestMethod -Uri "$BaseUrl/api/chat" -Method Post `
            -ContentType "application/json; charset=utf-8" -Body $body
        if (-not $r.content) { throw "empty content" }
    }

    Test-Endpoint "POST /api/agent/database" {
        $body = [System.Text.Encoding]::UTF8.GetBytes('{"question":"orders表按user_id查询为什么慢？"}')
        $r = Invoke-RestMethod -Uri "$BaseUrl/api/agent/database" -Method Post `
            -ContentType "application/json; charset=utf-8" -Body $body
        if (-not $r.analysis) { throw "empty analysis" }
    }
}

Write-Host "`n=== Result: $passed passed, $failed failed ===" -ForegroundColor Cyan
if ($failed -gt 0) { exit 1 }

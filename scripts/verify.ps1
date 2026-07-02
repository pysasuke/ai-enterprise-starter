# V1 验收脚本（PowerShell）
# 用法: .\scripts\verify.ps1 [-BaseUrl http://localhost:8080] [-SkipAi] [-SkipRag] [-SkipPrompt]

param(
    [string]$BaseUrl = "http://localhost:8080",
    [switch]$SkipAi,
    [switch]$SkipRag,
    [switch]$SkipPrompt
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
        $json = @{ question = "Why is querying orders by user_id slow?" } | ConvertTo-Json -Compress
        $body = [System.Text.Encoding]::UTF8.GetBytes($json)
        $r = Invoke-RestMethod -Uri "$BaseUrl/api/agent/database" -Method Post `
            -ContentType "application/json; charset=utf-8" -Body $body -TimeoutSec 120
        if (-not $r.analysis) { throw "empty analysis" }
    }
}

if (-not $SkipRag) {
    Test-Endpoint "POST /api/rag/documents" {
        $filePath = Join-Path $PSScriptRoot "..\examples\refund-policy.md"
        if (-not (Test-Path $filePath)) { throw "missing examples/refund-policy.md" }
        $boundary = [System.Guid]::NewGuid().ToString()
        $fileBytes = [System.IO.File]::ReadAllBytes($filePath)
        $bodyLines = @(
            "--$boundary",
            'Content-Disposition: form-data; name="file"; filename="refund-policy.md"',
            'Content-Type: text/markdown',
            '',
            [System.Text.Encoding]::UTF8.GetString($fileBytes),
            "--$boundary--"
        )
        $body = $bodyLines -join "`r`n"
        $r = Invoke-RestMethod -Uri "$BaseUrl/api/rag/documents" -Method Post `
            -ContentType "multipart/form-data; boundary=$boundary" `
            -Body ([System.Text.Encoding]::UTF8.GetBytes($body)) `
            -TimeoutSec 180
        if (-not $r.id) { throw "upload failed" }
    }

    Test-Endpoint "POST /api/rag/chat" {
        $body = [System.Text.Encoding]::UTF8.GetBytes('{"question":"退款政策是什么？","topK":3}')
        $r = Invoke-RestMethod -Uri "$BaseUrl/api/rag/chat" -Method Post `
            -ContentType "application/json; charset=utf-8" -Body $body -TimeoutSec 180
        if (-not $r.answer) { throw "empty answer" }
    }
}

if (-not $SkipPrompt) {
    Test-Endpoint "GET /api/prompts/database.agent/user" {
        $r = Invoke-RestMethod -Uri "$BaseUrl/api/prompts/database.agent/user"
        if (-not $r.key) { throw "missing prompt key" }
        if ($r.activeVersion -lt 1) { throw "no active version" }
    }

    Test-Endpoint "POST /api/prompts/render" {
        $body = [System.Text.Encoding]::UTF8.GetBytes(
            '{"key":"database.agent","type":"user","variables":{"question":"test","schema":"s","indexes":"i"}}')
        $r = Invoke-RestMethod -Uri "$BaseUrl/api/prompts/render" -Method Post `
            -ContentType "application/json; charset=utf-8" -Body $body
        if (-not $r.rendered) { throw "empty rendered" }
        if ($r.rendered -notmatch "test") { throw "variable not rendered" }
    }

    Test-Endpoint "POST+PUT /api/prompts version flow" {
        $createBody = [System.Text.Encoding]::UTF8.GetBytes('{"content":"test {{question}}"}')
        $created = Invoke-RestMethod -Uri "$BaseUrl/api/prompts/verify.test/user/versions" -Method Post `
            -ContentType "application/json; charset=utf-8" -Body $createBody
        if (-not $created.version) { throw "create failed" }
        $activeBody = [System.Text.Encoding]::UTF8.GetBytes("{`"version`":$($created.version)}")
        Invoke-RestMethod -Uri "$BaseUrl/api/prompts/verify.test/user/active" -Method Put `
            -ContentType "application/json; charset=utf-8" -Body $activeBody | Out-Null
        $got = Invoke-RestMethod -Uri "$BaseUrl/api/prompts/verify.test/user"
        if ($got.activeVersion -ne $created.version) { throw "active not updated" }
    }
}

Write-Host "`n=== Result: $passed passed, $failed failed ===" -ForegroundColor Cyan
if ($failed -gt 0) { exit 1 }

# ============================================================
#  start.ps1 - Khoi dong: frontend -> mock-gateway -> Comment_Filter
#  Chay: .\start.ps1
# ============================================================

$ErrorActionPreference = "Stop"

# --- Cau hinh port ---
$FRONTEND_PORT       = 5173
$MOCK_GATEWAY_PORT   = 3000
$COMMENT_FILTER_PORT = 8081

# --- Timeout cho service (giay) ---
$HEALTH_TIMEOUT = 120

$ROOT_DIR = $PSScriptRoot
$JOBS = @()

# --- Tao thu muc logs ---
$logsDir = Join-Path $ROOT_DIR "logs"
if (-not (Test-Path $logsDir)) {
    New-Item -ItemType Directory -Path $logsDir | Out-Null
}

# --- Helper functions ---
function Log  { param($msg) Write-Host "[$(Get-Date -Format 'HH:mm:ss')] $msg" -ForegroundColor Cyan }
function Ok   { param($msg) Write-Host "[$(Get-Date -Format 'HH:mm:ss')] OK: $msg" -ForegroundColor Green }
function Warn { param($msg) Write-Host "[$(Get-Date -Format 'HH:mm:ss')] WARN: $msg" -ForegroundColor Yellow }
function Err  { param($msg) Write-Host "[$(Get-Date -Format 'HH:mm:ss')] ERR: $msg" -ForegroundColor Red }

# Kill tat ca process dang dung port chi dinh
function Kill-Port {
    param([int]$Port)
    $pids = netstat -ano | Select-String ":$Port\s" | ForEach-Object {
        ($_ -split '\s+')[-1]
    } | Sort-Object -Unique
    foreach ($procId in $pids) {
        if ($procId -match '^\d+$' -and $procId -ne '0') {
            try {
                Stop-Process -Id $procId -Force -ErrorAction SilentlyContinue
                Warn "Da kill process PID $procId dang chiem port $Port"
            } catch {}
        }
    }
}

function Stop-AllProcesses {
    Warn "Dang dung tat ca services..."
    foreach ($job in $JOBS) {
        if ($job -and -not $job.HasExited) {
            # Kill toan bo process tree (ca child)
            taskkill /PID $job.Id /T /F 2>$null | Out-Null
            Ok "Da dung process tree PID $($job.Id)"
        }
    }
}

function Wait-Port {
    param(
        [string]$ServiceName,
        [int]$Port,
        [System.Diagnostics.Process]$Process
    )
    $elapsed = 0
    Start-Sleep -Seconds 3
    Log "Cho $ServiceName khoi dong tren port $Port..."
    while ($true) {
        # Phat hien som neu process da thoat
        if ($Process -and $Process.HasExited) {
            $exitCode = $Process.ExitCode
            Err "$ServiceName process da thoat som (exit code: $exitCode). Kiem tra logs."
            Stop-AllProcesses
            exit 1
        }

        # Kiem tra ca IPv4 va IPv6 loopback
        $isOpen = $false
        $addresses = @('127.0.0.1', '[::1]')
        foreach ($addr in $addresses) {
            try {
                $test = Test-NetConnection -ComputerName $addr -Port $Port -WarningAction SilentlyContinue -ErrorAction SilentlyContinue
                if ($test.TcpTestSucceeded) {
                    $isOpen = $true
                    break
                }
            } catch {}
        }

        if ($isOpen) {
            Ok "$ServiceName san sang tren port $Port (sau ${elapsed}s)"
            return
        }
        if ($elapsed -ge $HEALTH_TIMEOUT) {
            Err "$ServiceName khong khoi dong duoc sau ${HEALTH_TIMEOUT}s. Dung tat ca."
            Stop-AllProcesses
            exit 1
        }
        Start-Sleep -Seconds 2
        $elapsed += 2
        Write-Host "  -> Da cho ${elapsed}s..." -ForegroundColor Yellow
    }
}

# =============================================================
# KILL CAC PORT CU TRUOC KHI CHAY
# =============================================================
Log "Kiem tra va giai phong cac port cu..."
Kill-Port $FRONTEND_PORT
Kill-Port $MOCK_GATEWAY_PORT
Kill-Port $COMMENT_FILTER_PORT
Start-Sleep -Seconds 2   # Them thoi gian cho port duoc giai phong

# =============================================================
# BUOC 1: FRONTEND (Vue 3 + Vite)
# =============================================================
Log "--- [1/3] Khoi dong Frontend (Vue 3) ---"

$frontendDir = Join-Path $ROOT_DIR "frontend"
Set-Location $frontendDir

if (-not (Test-Path "node_modules")) {
    Log "Cai dat dependencies frontend..."
    & cmd /c "npm install --silent"
}

$p1 = Start-Process -FilePath "cmd.exe" `
    -ArgumentList "/c", "npm run dev -- --port $FRONTEND_PORT" `
    -RedirectStandardOutput "$logsDir\frontend.log" `
    -RedirectStandardError  "$logsDir\frontend-err.log" `
    -WorkingDirectory $frontendDir `
    -PassThru -WindowStyle Hidden
$JOBS += $p1
Ok "Frontend PID: $($p1.Id)"

Wait-Port "Frontend" $FRONTEND_PORT -Process $p1

# =============================================================
# BUOC 2: MOCK GATEWAY (Next.js) - DUNG DEVELOPMENT SERVER
# =============================================================
Log "--- [2/3] Khoi dong Mock Gateway (Next.js - dev) ---"

$gatewayDir = Join-Path $ROOT_DIR "mock-gateway"
Set-Location $gatewayDir

if (-not (Test-Path "node_modules")) {
    Log "Cai dat dependencies mock-gateway..."
    & cmd /c "npm install --silent"
}

# KHONG can build khi chay dev
# Chay npm run dev, truyen port qua tham so --port hoac -p
$p2 = Start-Process -FilePath "cmd.exe" `
    -ArgumentList "/c", "npm run dev -- -p $MOCK_GATEWAY_PORT" `
    -RedirectStandardOutput "$logsDir\mock-gateway.log" `
    -RedirectStandardError  "$logsDir\mock-gateway-err.log" `
    -WorkingDirectory $gatewayDir `
    -PassThru -WindowStyle Hidden
$JOBS += $p2
Ok "Mock Gateway PID: $($p2.Id)"

Wait-Port "Mock Gateway" $MOCK_GATEWAY_PORT -Process $p2

# =============================================================
# BUOC 3: COMMENT FILTER (Python)
# =============================================================
Log "--- [3/3] Khoi dong Comment Filter (Python) ---"

$aiDir = Join-Path $ROOT_DIR "Comment_Filter"
Set-Location $aiDir

$venvPython = Join-Path $aiDir "venv\Scripts\python.exe"

if (-not (Test-Path "venv")) {
    Log "Tao Python virtual environment..."
    & cmd /c "python -m venv venv"
    Ok "Tao venv thanh cong"
}

if (Test-Path "requirements.txt") {
    Log "Cai dat Python dependencies..."
    & cmd /c "`"$venvPython`" -m pip install -r requirements.txt -q"
}

$p3 = Start-Process -FilePath "cmd.exe" `
    -ArgumentList "/c", "`"$venvPython`" api.py" `
    -RedirectStandardOutput "$logsDir\comment-filter.log" `
    -RedirectStandardError  "$logsDir\comment-filter-err.log" `
    -WorkingDirectory $aiDir `
    -PassThru -WindowStyle Hidden
$JOBS += $p3
Ok "Comment Filter PID: $($p3.Id)"

Wait-Port "Comment Filter" $COMMENT_FILTER_PORT -Process $p3

# =============================================================
# TAT CA SERVICES DA SAN SANG
# =============================================================
Set-Location $ROOT_DIR

Write-Host ""
Write-Host "+----------------------------------------------+" -ForegroundColor Green
Write-Host "|   Tat ca services da san sang!               |" -ForegroundColor Green
Write-Host "+----------------------------------------------+" -ForegroundColor Green
Write-Host "|  Frontend       -> http://localhost:$FRONTEND_PORT     |" -ForegroundColor Green
Write-Host "|  Mock Gateway   -> http://localhost:$MOCK_GATEWAY_PORT      |" -ForegroundColor Green
Write-Host "|  Comment Filter -> http://localhost:$COMMENT_FILTER_PORT     |" -ForegroundColor Green
Write-Host "+----------------------------------------------+" -ForegroundColor Green
Write-Host "|  Logs: .\logs\<service>.log                  |" -ForegroundColor Green
Write-Host "|  Dung tat ca: Ctrl+C                         |" -ForegroundColor Green
Write-Host "+----------------------------------------------+" -ForegroundColor Green
Write-Host ""

Log "Nhan Ctrl+C de dung tat ca services."

try {
    while ($true) {
        Start-Sleep -Seconds 5
        foreach ($job in $JOBS) {
            if ($job.HasExited) {
                Warn "Process PID $($job.Id) da thoat voi code $($job.ExitCode). Kiem tra logs."
            }
        }
    }
} finally {
    Stop-AllProcesses
}
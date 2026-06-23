param(
    [int]$BasePort = 8765,
    [int]$EmulatorTargetPort = 8765,
    [switch]$CleanOnly,
    [switch]$Watch
)

function Get-Emulators {
    $output = adb devices 2>&1
    $emulators = @()
    foreach ($line in $output) {
        if ($line -match '^(emulator-\d+)\s+device') {
            $emulators += $Matches[1]
        }
    }
    return $emulators
}

function Setup-Forwards {
    $emulators = Get-Emulators
    if ($emulators.Count -eq 0) {
        return @()
    }
    $port = $BasePort
    $results = @()
    foreach ($emu in $emulators) {
        $existing = adb -s $emu forward --list 2>&1 | Select-String "tcp:$port"
        if (-not $existing) {
            adb -s $emu forward "tcp:$port" "tcp:$EmulatorTargetPort" 2>&1 | Out-Null
            $results += [PSCustomObject]@{ Port = $port; Emulator = $emu; Status = "NEW" }
        } else {
            $results += [PSCustomObject]@{ Port = $port; Emulator = $emu; Status = "EXISTS" }
        }
        $port++
    }
    return $results
}

if ($Watch) {
    Write-Host "=== Oder++ Auto Port Forwarding (Watch Mode) ===" -ForegroundColor Cyan
    Write-Host "Monitoring for emulator connections... Press Ctrl+C to stop." -ForegroundColor Yellow
    $known = @{}
    while ($true) {
        $emulators = Get-Emulators
        $current = @{}
        foreach ($emu in $emulators) { $current[$emu] = $true }
        $newEmus = $current.Keys | Where-Object { -not $known.ContainsKey($_) }
        $lostEmus = $known.Keys | Where-Object { -not $current.ContainsKey($_) }
        if ($newEmus) {
            Write-Host "`n[$(Get-Date -Format 'HH:mm:ss')] New emulator(s) detected: $($newEmus -join ', ')" -ForegroundColor Green
            Setup-Forwards | ForEach-Object {
                Write-Host "  $($_.Port) -> $($_.Emulator) (tcp:$EmulatorTargetPort)" -ForegroundColor Green
            }
            Write-Host "  Client connects to 10.0.2.2:$BasePort" -ForegroundColor White
        }
        if ($lostEmus) {
            Write-Host "`n[$(Get-Date -Format 'HH:mm:ss')] Emulator(s) disconnected: $($lostEmus -join ', ')" -ForegroundColor DarkGray
        }
        $known = $current
        Start-Sleep -Seconds 3
    }
    exit 0
}

$emulators = Get-Emulators

if ($emulators.Count -eq 0) {
    Write-Host "No running emulators found." -ForegroundColor Yellow
    Write-Host ""
    Write-Host "For real devices on the same LAN:" -ForegroundColor Cyan
    Write-Host "  1. Ensure both devices are on the same WiFi network" -ForegroundColor Gray
    Write-Host "  2. Start the Host on one device" -ForegroundColor Gray
    Write-Host "  3. The Host IP will be displayed in Settings > Network" -ForegroundColor Gray
    Write-Host "  4. On the Client device, use manual connection with the Host IP" -ForegroundColor Gray
    Write-Host "  No ADB forwarding needed for real devices." -ForegroundColor Gray
    exit 0
}

Write-Host "=== Oder++ Emulator Port Forwarding ===" -ForegroundColor Cyan
Write-Host "Found $($emulators.Count) emulator(s):" -ForegroundColor Cyan
$emulators | ForEach-Object { Write-Host "  $_" -ForegroundColor Gray }

if ($CleanOnly) {
    Write-Host "`nCleaning existing forwards for emulators..." -ForegroundColor Yellow
    foreach ($emu in $emulators) {
        $existing = adb -s $emu forward --list 2>&1 | Select-String "tcp:$BasePort"
        if ($existing) {
            adb -s $emu forward --remove "tcp:$EmulatorTargetPort" 2>&1 | Out-Null
            Write-Host "  Removed forward on $emu" -ForegroundColor Gray
        }
    }
    Write-Host "`nDone. All forwards cleaned." -ForegroundColor Green
    exit 0
}

Write-Host ""
Write-Host "Setting up port forwarding:" -ForegroundColor Cyan
Write-Host "Host Port".PadRight(12) + "-> Emulator".PadRight(22) + "Target Port" -ForegroundColor White
Write-Host ("-" * 58)

$results = Setup-Forwards

foreach ($r in $results) {
    $status = if ($r.Status -eq "EXISTS") { "(already exists)" } else { "" }
    $color = if ($r.Status -eq "EXISTS") { "DarkGray" } else { "Green" }
    Write-Host "$($r.Port)".PadRight(12) + "-> $($r.Emulator)".PadRight(22) + "$EmulatorTargetPort $status" -ForegroundColor $color
}

Write-Host ("-" * 58)
Write-Host ""
Write-Host "Forwarding complete!" -ForegroundColor Green
Write-Host ""
Write-Host "Emulator Client setup:" -ForegroundColor Yellow
Write-Host "  IP: 10.0.2.2" -ForegroundColor White
Write-Host "  Port: $BasePort" -ForegroundColor White
Write-Host ""
Write-Host "Real device (LAN) setup:" -ForegroundColor Cyan
Write-Host "  No ADB forwarding needed. Connect devices to same WiFi." -ForegroundColor Gray
Write-Host "  Host device IP is shown in Settings > Network." -ForegroundColor Gray
Write-Host ""
Write-Host "Map:" -ForegroundColor Cyan
$results | ForEach-Object {
    $label = if ($_.Port -eq $BasePort) { " (Default Server)" } else { "" }
    Write-Host "  10.0.2.2:$($_.Port) -> $($_.Emulator):$EmulatorTargetPort$label" -ForegroundColor Gray
}

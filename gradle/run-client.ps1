$ErrorActionPreference = 'Stop'

$workspace = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$gradle = Start-Process `
    -FilePath (Join-Path $workspace 'gradlew.bat') `
    -ArgumentList '--no-daemon', 'runClient', '--console=plain' `
    -WorkingDirectory $workspace `
    -PassThru `
    -NoNewWindow

function Get-DescendantProcesses([int]$ParentId) {
    $allProcesses = @(Get-CimInstance Win32_Process)
    $descendants = @()
    $pending = [System.Collections.Generic.Queue[int]]::new()
    $pending.Enqueue($ParentId)

    while ($pending.Count -gt 0) {
        $currentParentId = $pending.Dequeue()
        foreach ($process in $allProcesses | Where-Object { $_.ParentProcessId -eq $currentParentId }) {
            $descendants += $process
            $pending.Enqueue([int]$process.ProcessId)
        }
    }

    return $descendants
}

try {
    $client = $null
    while (-not $gradle.HasExited) {
        $client = @(Get-DescendantProcesses $gradle.Id |
            Where-Object {
                $_.Name -eq 'java.exe' -and
                $_.CommandLine -match 'fml\.modFolders|clientRunProgramArgs|net\.neoforged\.devlaunch'
            } |
            Select-Object -First 1)

        if ($client.Count -gt 0) {
            break
        }

        Start-Sleep -Seconds 1
        $gradle.Refresh()
    }

    if ($client.Count -gt 0) {
        $clientId = [int]$client[0].ProcessId
        while (-not $gradle.HasExited) {
            $clientStillRunning = Get-Process -Id $clientId -ErrorAction SilentlyContinue
            if ($null -eq $clientStillRunning) {
                break
            }

            Start-Sleep -Seconds 1
            $gradle.Refresh()
        }
    }
}
finally {
    for ($attempt = 0; $attempt -lt 5; $attempt++) {
        $descendants = @(Get-DescendantProcesses $gradle.Id)
        foreach ($process in $descendants | Sort-Object { $_.ParentProcessId } -Descending) {
            Stop-Process -Id ([int]$process.ProcessId) -Force -ErrorAction SilentlyContinue
        }

        if (-not $gradle.HasExited) {
            Stop-Process -Id $gradle.Id -Force -ErrorAction SilentlyContinue
        }

        if ($descendants.Count -eq 0 -and $gradle.HasExited) {
            break
        }

        Start-Sleep -Milliseconds 200
    }
}

param(
    [Parameter(Mandatory=$true)]
    [ValidateSet("QA","PROD")]
    [string]$Environment
)

. ".\lib.ps1"

$Timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$LogFile   = ".\audit-job-$Timestamp.log"

$Config = Get-Content ".\config.$($Environment.ToLower()).json" | ConvertFrom-Json

Write-LogEntry -LogFile $LogFile -Message "Starting audit job installation for environment: $Environment"
Write-LogEntry -LogFile $LogFile -Message "NSSM path: $($Config.NssmPath)"
Write-LogEntry -LogFile $LogFile -Message "Service account: $($Config.ServiceAccount)"

foreach ($Service in $Config.InstallServices) {
    Write-LogEntry -LogFile $LogFile -Message "Installing service: $($Service.ServiceName)"

    & $Config.NssmPath install $Service.ServiceName powershell.exe
    & $Config.NssmPath set    $Service.ServiceName AppParameters "-NonInteractive -File `"$($Service.ScriptPath)`" -Environment $Environment"
    & $Config.NssmPath set    $Service.ServiceName Start SERVICE_DEMAND_START
    & $Config.NssmPath set    $Service.ServiceName ObjectName $Config.ServiceAccount $Config.ServicePassword

    if ($LASTEXITCODE -eq 0) {
        Write-LogEntry -LogFile $LogFile -Message "Service installed successfully: $($Service.ServiceName)"
    } else {
        Write-LogEntry -LogFile $LogFile -Message "Service install may have failed for: $($Service.ServiceName) (exit code $LASTEXITCODE)" -Level "WARNING"
    }
}

Write-LogEntry -LogFile $LogFile -Message "Installation complete."

param(
    [Parameter(Mandatory=$true)]
    [ValidateSet("QA","PROD")]
    [string]$Environment,

    [Parameter(Mandatory=$true)]
    [string]$JobName
)

. ".\lib.ps1"

$Timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$LogFile   = ".\audit-job-$Timestamp.log"

$Config = Get-Content ".\config.$($Environment.ToLower()).json" | ConvertFrom-Json

Write-LogEntry -LogFile $LogFile -Message "Triggering audit job '$JobName' for environment: $Environment"

$ServiceDef = $Config.InstallServices | Where-Object { $_.JobName -eq $JobName } | Select-Object -First 1

if (-not $ServiceDef) {
    Write-LogEntry -LogFile $LogFile -Message "No service found for JobName '$JobName'. Check InstallServices in config.$($Environment.ToLower()).json." -Level "ERROR"
    exit 1
}

Write-LogEntry -LogFile $LogFile -Message "Found service: $($ServiceDef.ServiceName)"

$Credential = New-Object System.Management.Automation.PSCredential(
    $Config.ServiceAccount,
    (ConvertTo-SecureString $Config.ServicePassword -AsPlainText -Force)
)

try {
    Start-Service -Name $ServiceDef.ServiceName
    Write-LogEntry -LogFile $LogFile -Message "Service '$($ServiceDef.ServiceName)' started successfully."
}
catch {
    Write-LogEntry -LogFile $LogFile -Message "Failed to start service '$($ServiceDef.ServiceName)': $_" -Level "ERROR"
    exit 1
}

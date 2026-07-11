param(
    [Parameter(Mandatory=$true)]
    [ValidateSet("QA","PROD")]
    [string]$Environment
)

. ".\lib.ps1"

$Timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$LogFile   = ".\audit-job-$Timestamp.log"

$Config = Get-Content ".\config.$($Environment.ToLower()).json" | ConvertFrom-Json

Write-LogEntry -LogFile $LogFile -Message "SampleJob started for environment: $Environment"

# TODO: add your SQL query and output logic here
# Example:
#   $sql = "SELECT ..."
#   Get_Result_SqlConnection -ConnectionString $Config.ConnectionString -SQL $sql -OutputFile ".\output.csv" -LogFile $LogFile

Write-LogEntry -LogFile $LogFile -Message "SampleJob completed."

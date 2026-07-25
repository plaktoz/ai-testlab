function Write-LogEntry {
    param([string]$LogFile, [string]$Message, [string]$Level = "INFO")
    $Entry = "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') [$Level] $Message"
    Add-Content -Path $LogFile -Value $Entry
    switch ($Level) {
        "ERROR"   { Write-Host $Entry -ForegroundColor Red }
        "WARNING" { Write-Host $Entry -ForegroundColor Yellow }
        default   { Write-Host $Entry }
    }
}

function Invoke-SqlQuery {
    param(
        [Parameter(Mandatory=$true)][string]$ConnectionString,
        [Parameter(Mandatory=$true)][string]$Sql,
        [int]$CommandTimeout = 120
    )

    $connection = $null
    $command = $null
    $adapter = $null
    $dataSet = $null
    try {
        $connection = New-Object System.Data.SqlClient.SqlConnection($ConnectionString)
        $connection.Open()

        $command = $connection.CreateCommand()
        $command.CommandText = $Sql
        $command.CommandTimeout = $CommandTimeout

        $adapter = New-Object System.Data.SqlClient.SqlDataAdapter($command)
        $dataSet = New-Object System.Data.DataSet
        $null = $adapter.Fill($dataSet)

        return $dataSet.Tables[0]
    }
    catch {
        throw $_
    }
    finally {
        if ($adapter)    { $adapter.Dispose() }
        if ($command)    { $command.Dispose() }
        if ($connection -and $connection.State -eq [System.Data.ConnectionState]::Open) {
            $connection.Close()
        }
        if ($connection) { $connection.Dispose() }
    }
}

function Test-DbConnection {
    param([Parameter(Mandatory=$true)][string]$ConnectionString)
    try {
        $connection = New-Object System.Data.SqlClient.SqlConnection($ConnectionString)
        $connection.Open()
        $command = $connection.CreateCommand()
        $command.CommandText = "SELECT 1"
        $command.CommandTimeout = 10
        $result = $command.ExecuteScalar()
        $command.Dispose()
        $connection.Close()
        $connection.Dispose()
        return ($result -eq 1)
    }
    catch {
        return $false
    }
}

function Get-WeekOfMonth {
    param (
        [parameter(ValueFromPipeline=$true)]
        [datetime]$Date
    )
    $firstDayOfMonth = Get-Date -Year $Date.Year -Month $Date.Month -Day 1
    $weekNumber = [math]::Ceiling(($Date.Day + $firstDayOfMonth.DayOfWeek.value__) / 7)
    return $weekNumber
}

function Get_Result_SqlConnection {
    param(
        [Parameter(Mandatory=$true)]$ConnectionString,
        [Parameter(Mandatory=$true)]$SQL,
        [Parameter(Mandatory=$true)]$OutputFile,
        [Parameter(Mandatory=$true)]$LogFile
    )

    try {
        Write-LogEntry -LogFile $LogFile -Message "Audit job started."

        $Table = Invoke-SqlQuery -ConnectionString $ConnectionString -Sql $SQL
        $Columns = $Table.Columns | ForEach-Object { $_.ColumnName }
        if ($Table.Rows.Count -le 0) {
            Write-LogEntry -LogFile $LogFile -Message "Query returned no rows." -Level "WARNING"
            $headerLine = ($Columns | ForEach-Object { "`"$_`"" }) -join ","
            $headerLine | Set-Content -Path $OutputFile -Encoding UTF8
        } else {
            $Table.Rows | Select-Object $Columns | Export-Csv -Path $OutputFile -NoTypeInformation -Encoding UTF8
        }
    }
    catch {
        Write-LogEntry -LogFile $LogFile -Message "ERROR: $_" -Level "ERROR"
        exit 1
    }
}

function Write_To_HTML {
    param (
        [Parameter(Mandatory=$true)]$db_output,
        [Parameter(Mandatory=$true)]$db_property
    )
    $html = $db_output | ConvertTo-Html -Property $db_property
    $xml = [xml]$html
    $html = $xml.OuterXml | Out-String
    return $html
}

function Get-WeekDateRange {
    $today = (Get-Date).AddHours(8)
    $start = $today.AddDays(-7)
    $startFormatted = Get-Date $start -Format "d MMMM yyyy"
    $endFormatted   = Get-Date $today -Format "d MMMM yyyy"
    return $startFormatted, $endFormatted
}

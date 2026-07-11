# Audit Job Starter Guide

`audit-job-template/` is the canonical template for all audit-job projects. Copy it, fill in your config, rename the sample job script, and you have a working service-based audit job.

---

## Template Files

| File | Purpose |
|---|---|
| `lib.ps1` | Shared utility functions. Dot-source this in every script with `. ".\lib.ps1"`. Do not edit unless adding functions that all projects need. |
| `config.qa.json` | QA environment configuration. Fill in real values for your QA environment. |
| `config.prod.json` | PROD environment configuration. Uses `<PLACEHOLDER>` tokens by default — replace before deploying. |
| `InstallJob.ps1` | Registers all services defined in `InstallServices[]` as Windows services via NSSM. Run once per environment per machine. |
| `TriggerJob.ps1` | Starts a single named service on demand. Use this to run a job without waiting for a scheduled trigger. |
| `SampleJob.ps1` | Skeleton job script. Copy and rename this for each audit job your project needs. |

---

## How to Start a New Project

1. **Copy the `audit-job-template/` directory** to a new folder named after your project:
   ```
   cp -r audit-job-template/ my-project-audit-job/
   ```

2. **Rename `SampleJob.ps1`** to reflect your actual job:
   ```
   mv my-project-audit-job/SampleJob.ps1 my-project-audit-job/MyAuditJob.ps1
   ```
   If you have multiple jobs, make a copy per job.

3. **Update `config.qa.json`** with your QA environment values (database endpoint, connection string, bucket, NSSM path, service account, and an entry in `InstallServices[]` for each job script).

4. **Update `config.prod.json`** by replacing every `<PLACEHOLDER>` token with your PROD values.

5. **Fill in `MyAuditJob.ps1`** — replace the `# TODO` section with your SQL query and output logic (see [Customising SampleJob.ps1](#customising-samplejobps1) below).

6. **Install the services** on the target machine (see [Installing Services](#installing-services)).

7. **Trigger a run** to verify (see [Triggering a Job](#triggering-a-job)).

---

## Config Schema

Both `config.qa.json` and `config.prod.json` share the same structure.

### Required top-level fields

| Field | Type | Description |
|---|---|---|
| `RdsEndpoint` | string | RDS hostname for this environment |
| `ConnectionString` | string | Full ADO.NET connection string |
| `AwsBucket` | string | S3 bucket for audit output |
| `NssmPath` | string | Absolute path to `nssm.exe` |
| `ServiceAccount` | string | Windows service account (`DOMAIN\user`) |
| `ServicePassword` | string | Password for the service account |
| `InstallServices` | array | One entry per job script to register as a service |

### `InstallServices[]` entry fields

| Field | Type | Description |
|---|---|---|
| `ServiceName` | string | Windows service name (must be unique on the machine) |
| `JobName` | string | Logical name used by `TriggerJob.ps1 -JobName` |
| `ScriptPath` | string | Absolute path to the job `.ps1` script on the target machine |

### Annotated example

```json
{
  "RdsEndpoint": "mydb.us-east-1.rds.amazonaws.com",
  "ConnectionString": "Server=mydb.us-east-1.rds.amazonaws.com;Database=AuditDB;Integrated Security=True;",
  "AwsBucket": "my-qa-audit-output",
  "NssmPath": "C:\\nssm\\nssm.exe",
  "ServiceAccount": "DOMAIN\\svc-auditjob-qa",
  "ServicePassword": "s3cr3t",
  "InstallServices": [
    {
      "ServiceName": "MyAuditJob-QA",
      "JobName": "MyAuditJob",
      "ScriptPath": "C:\\audit-jobs\\my-project-audit-job\\MyAuditJob.ps1"
    }
  ]
}
```

---

## Installing Services

Run `InstallJob.ps1` once per environment on the target machine. It reads the matching config file and registers every entry in `InstallServices[]` as a Windows service via NSSM with `SERVICE_DEMAND_START`.

```powershell
# Install services for QA
.\InstallJob.ps1 -Environment QA

# Install services for PROD
.\InstallJob.ps1 -Environment PROD
```

A timestamped log file (`audit-job-<timestamp>.log`) is written to the working directory. Check it if any service fails to install.

> Re-running install on an already-installed service will overwrite the NSSM configuration. This is safe and idempotent.

---

## Triggering a Job

Run `TriggerJob.ps1` to start a specific job by its `JobName`. This looks up the matching `InstallServices[]` entry and calls `Start-Service`.

```powershell
# Trigger a job in QA
.\TriggerJob.ps1 -Environment QA -JobName MyAuditJob

# Trigger a job in PROD
.\TriggerJob.ps1 -Environment PROD -JobName MyAuditJob
```

If the `JobName` is not found in the config, the script logs an error and exits without starting anything.

---

## Customising SampleJob.ps1

`SampleJob.ps1` is a minimal skeleton. The only section you need to replace is the `# TODO` block:

```powershell
# TODO: add your SQL query and output logic here
# Example:
#   $sql = "SELECT ..."
#   Get_Result_SqlConnection -ConnectionString $Config.ConnectionString -SQL $sql -OutputFile ".\output.csv" -LogFile $LogFile
```

Replace this with your actual audit query using the helper functions from `lib.ps1`:

- **`Invoke-SqlQuery`** — executes a SQL command and returns rows
- **`Get_Result_SqlConnection`** — runs a query and writes results to a file
- **`Write_To_HTML`** — formats results as an HTML report
- **`Write-LogEntry`** — writes a log line (supports `INFO`, `WARNING`, `ERROR` levels)

The rest of the script (parameter declaration, dot-source, config read, log setup) is already correct — do not change those sections.

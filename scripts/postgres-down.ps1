param(
    [switch]$ResetData
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot

Push-Location $root
try {
    if ($ResetData) {
        Write-Warning "The named volume and all local PostgreSQL data will be deleted."
        docker compose down --volumes
    }
    else {
        docker compose down
    }

    if ($LASTEXITCODE -ne 0) {
        throw "Failed to stop the PostgreSQL container."
    }
}
finally {
    Pop-Location
}

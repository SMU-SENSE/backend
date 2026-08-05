$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
$envFile = Join-Path $root ".env"

if (-not (Test-Path -LiteralPath $envFile)) {
    throw ".env is missing. Create it from .env.example."
}

docker info *> $null
if ($LASTEXITCODE -ne 0) {
    throw "Cannot connect to Docker. Start Docker Desktop first."
}

Push-Location $root
try {
    docker compose up -d postgres
    if ($LASTEXITCODE -ne 0) {
        throw "Failed to start the PostgreSQL container."
    }

    $deadline = (Get-Date).AddMinutes(2)
    do {
        $health = docker inspect --format "{{.State.Health.Status}}" malmoa-postgres-dev 2>$null
        if ($health -eq "healthy") {
            docker compose ps postgres
            exit 0
        }
        Start-Sleep -Seconds 2
    } while ((Get-Date) -lt $deadline)

    docker compose ps postgres
    throw "PostgreSQL did not become healthy before the timeout."
}
finally {
    Pop-Location
}

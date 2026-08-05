param(
    [switch]$Smoke
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
$envFile = Join-Path $root ".env"

if (-not (Test-Path -LiteralPath $envFile)) {
    throw ".env is missing. Create it from .env.example."
}

Get-Content -LiteralPath $envFile | ForEach-Object {
    $line = $_.Trim()
    if ($line -and -not $line.StartsWith("#")) {
        $parts = $line.Split("=", 2)
        if ($parts.Count -eq 2) {
            [Environment]::SetEnvironmentVariable($parts[0].Trim(), $parts[1].Trim(), "Process")
        }
    }
}

foreach ($name in @("DB_URL", "DB_USERNAME", "DB_PASSWORD")) {
    if (-not [Environment]::GetEnvironmentVariable($name, "Process")) {
        throw "Required environment variable $name is missing."
    }
}

if ($Smoke) {
    if (-not $env:GOOGLE_CLIENT_ID) {
        $env:GOOGLE_CLIENT_ID = "postgres-smoke-login-disabled"
    }
    if (-not $env:GOOGLE_CLIENT_SECRET) {
        $env:GOOGLE_CLIENT_SECRET = "postgres-smoke-login-disabled"
    }
    Write-Warning "Smoke mode does not validate Google login."
}
elseif (-not $env:GOOGLE_CLIENT_ID -or -not $env:GOOGLE_CLIENT_SECRET) {
    throw "GOOGLE_CLIENT_ID and GOOGLE_CLIENT_SECRET are required for normal startup."
}

Push-Location $root
try {
    & .\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=postgres"
    exit $LASTEXITCODE
}
finally {
    Pop-Location
}

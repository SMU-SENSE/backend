param(
    [string]$BaseUrl = "http://localhost:8080"
)

$ErrorActionPreference = "Stop"
$checks = @(
    @{ Name = "Health"; Path = "/api/v1/health" },
    @{ Name = "OpenAPI"; Path = "/v3/api-docs" },
    @{ Name = "Swagger UI"; Path = "/swagger-ui/index.html" }
)

$failed = $false
foreach ($check in $checks) {
    $uri = $BaseUrl + $check.Path
    try {
        $response = Invoke-WebRequest -Uri $uri -Method Get -UseBasicParsing -TimeoutSec 15
        Write-Host ("{0}: HTTP {1} {2}" -f $check.Name, $response.StatusCode, $uri)
        if ($response.StatusCode -ne 200) {
            $failed = $true
        }
    }
    catch {
        Write-Error ("{0}: request failed {1} - {2}" -f $check.Name, $uri, $_.Exception.Message)
        $failed = $true
    }
}

if ($failed) {
    exit 1
}

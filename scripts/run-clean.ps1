param(
    [switch]$SkipTests,
    [string[]]$AppArgs
)

$ErrorActionPreference = "Stop"

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
Set-Location $repoRoot

$mavenArgs = @("clean", "package")
if ($SkipTests) {
    $mavenArgs += "-DskipTests"
}

Write-Host "Cleaning and packaging the Spring Boot app..." -ForegroundColor Cyan
& "$repoRoot\mvnw.cmd" @mavenArgs
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

$jar = Get-ChildItem -Path (Join-Path $repoRoot "target") -Filter "*.jar" |
    Where-Object { $_.Name -notlike "original-*" } |
    Sort-Object LastWriteTime -Descending |
    Select-Object -First 1

if (-not $jar) {
    throw "No runnable jar was found in target/ after the build."
}

Write-Host "Starting Spring Boot from $($jar.FullName)..." -ForegroundColor Green
Write-Host "Open http://localhost:8080 once the app is ready." -ForegroundColor Yellow

& java -jar $jar.FullName @AppArgs
exit $LASTEXITCODE

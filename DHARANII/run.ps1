# Standalone DTC Application Launcher
$mavenVersion = "3.9.6"
$mavenDir = "$PSScriptRoot\.maven"
$mavenZip = "$PSScriptRoot\maven.zip"
$mavenUrl = "https://archive.apache.org/dist/maven/maven-3/$mavenVersion/binaries/apache-maven-$mavenVersion-bin.zip"

# Resolve TLS Security protocols for downloading
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12

if (-not (Test-Path $mavenDir)) {
    Write-Host "--------------------------------------------------------" -ForegroundColor Cyan
    Write-Host "Maven is not installed. Downloading standalone Maven $mavenVersion..." -ForegroundColor Cyan
    Write-Host "--------------------------------------------------------" -ForegroundColor Cyan
    
    try {
        Invoke-WebRequest -Uri $mavenUrl -OutFile $mavenZip -TimeoutSec 120
        Write-Host "Extracting Maven package..." -ForegroundColor Cyan
        Expand-Archive -Path $mavenZip -DestinationPath "$PSScriptRoot\.tmp_maven"
        Move-Item -Path "$PSScriptRoot\.tmp_maven\apache-maven-$mavenVersion" -Destination $mavenDir
        Remove-Item -Path $mavenZip -Force
        Remove-Item -Path "$PSScriptRoot\.tmp_maven" -Recurse -Force
        Write-Host "Maven setup complete." -ForegroundColor Green
    } catch {
        Write-Host "Download failed. Please ensure you are connected to the internet." -ForegroundColor Red
        Exit
    }
}

Write-Host "--------------------------------------------------------" -ForegroundColor Green
Write-Host "Starting Spring Boot server on http://localhost:8080..." -ForegroundColor Green
Write-Host "--------------------------------------------------------" -ForegroundColor Green

& "$mavenDir\bin\mvn.cmd" spring-boot:run

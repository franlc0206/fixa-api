<#
Run Spring Boot app locally using .env.local variables.

Usage:
  - Default (Maven):   ./run-local.ps1
  - Docker mode:       ./run-local.ps1 -Docker
  - Clean container(s): ./run-local.ps1 -Docker -Rebuild

The script reads .env.local, exports variables to the current process,
and then starts the app using Maven or Docker.
#>
param(
  [switch]$Docker,
  [switch]$Rebuild
)

$ErrorActionPreference = 'Stop'

function Load-EnvFile {
  param([string]$Path)
  if (-not (Test-Path $Path)) {
    Write-Error "No se encontró '$Path'. Copiá .env.local.example a .env.local"
  }
  Get-Content $Path | ForEach-Object {
    $line = $_.Trim()
    if ([string]::IsNullOrWhiteSpace($line)) { return }
    if ($line.StartsWith('#')) { return }
    $eqIndex = $line.IndexOf('=')
    if ($eqIndex -lt 1) { return }
    $key = $line.Substring(0, $eqIndex).Trim()
    $val = $line.Substring($eqIndex + 1).Trim()
    # Remove surrounding quotes if any
    if (($val.StartsWith('"') -and $val.EndsWith('"')) -or ($val.StartsWith("'") -and $val.EndsWith("'"))) {
      $val = $val.Substring(1, $val.Length - 2)
    }
    Set-Item -Path Env:$key -Value $val | Out-Null
  }
}

function Ensure-Command {
  param([string]$Name)
  if (-not (Get-Command $Name -ErrorAction SilentlyContinue)) {
    Write-Error "Comando requerido no encontrado: $Name"
  }
}

Write-Host "Cargando variables desde .env.local..." -ForegroundColor Cyan
Load-EnvFile -Path ".env.local"

$port = if ($env:PORT) { [int]$env:PORT } else { 8080 }
$containerPort = $port

if ($Docker) {
  Ensure-Command docker
  $image = 'fixa-api:dev'
  if ($Rebuild) {
    Write-Host "Limpiando imágenes anteriores ($image)..." -ForegroundColor Yellow
    try { docker rmi -f $image | Out-Null } catch {}
  }
  Write-Host "Construyendo imagen Docker ($image)..." -ForegroundColor Cyan
  docker build -t $image .

  Write-Host "Levantando contenedor en http://localhost:$port ..." -ForegroundColor Green
  docker run --rm -p "$port:$containerPort" --env-file .env.local $image
} else {
  # Maven mode
  if (Test-Path "./mvnw.cmd") {
    $mvnw = "./mvnw.cmd"
  } else {
    $mvnw = "mvn"
  }
  Ensure-Command $mvnw
  Write-Host "Iniciando aplicación con Maven (perfil: $env:SPRING_PROFILES_ACTIVE) en puerto $port ..." -ForegroundColor Green
  & $mvnw spring-boot:run
}

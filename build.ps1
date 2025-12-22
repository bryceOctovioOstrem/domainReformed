# Starsector Mod Build Script
# This script packages the Domain Reformed mod into a ZIP file for distribution

param(
    [string]$StarsectorPath = "",
    [switch]$CompileJava = $false
)

$ErrorActionPreference = "Stop"

Write-Host "Building Domain Reformed Mod..." -ForegroundColor Green

# Get mod info
$modInfo = Get-Content "mod_info.json" | ConvertFrom-Json
$modName = $modInfo.id
$modVersion = $modInfo.version
$outputZip = "${modName}_v${modVersion}.zip"

# Remove old build if it exists
if (Test-Path $outputZip) {
    Write-Host "Removing old build: $outputZip" -ForegroundColor Yellow
    Remove-Item $outputZip -Force
}

# Optional: Compile Java files if Starsector path is provided
if ($CompileJava -and $StarsectorPath) {
    Write-Host "Compiling Java files..." -ForegroundColor Cyan
    
    $starsectorJar = Join-Path $StarsectorPath "starfarer.api.jar"
    if (-not (Test-Path $starsectorJar)) {
        Write-Host "Warning: starfarer.api.jar not found at $starsectorJar" -ForegroundColor Yellow
        Write-Host "Skipping Java compilation. Starsector will compile on-the-fly." -ForegroundColor Yellow
        $CompileJava = $false
    } else {
        $javaFiles = Get-ChildItem -Path "data\scripts" -Filter "*.java" -Recurse
        $classpath = $starsectorJar
        
        foreach ($file in $javaFiles) {
            Write-Host "  Compiling: $($file.FullName)" -ForegroundColor Gray
            javac -cp $classpath -d "data\scripts" $file.FullName
            if ($LASTEXITCODE -ne 0) {
                Write-Host "  Warning: Failed to compile $($file.Name)" -ForegroundColor Yellow
            }
        }
    }
}

# Create temporary directory for packaging
$tempDir = "build_temp"
if (Test-Path $tempDir) {
    Remove-Item $tempDir -Recurse -Force
}
New-Item -ItemType Directory -Path $tempDir | Out-Null

Write-Host "Packaging mod files..." -ForegroundColor Cyan

# Copy mod files to temp directory
$filesToInclude = @(
    "mod_info.json",
    "data",
    "graphics",
    "sounds"
)

foreach ($item in $filesToInclude) {
    if (Test-Path $item) {
        Write-Host "  Copying: $item" -ForegroundColor Gray
        Copy-Item -Path $item -Destination $tempDir -Recurse -Force
    } else {
        Write-Host "  Warning: $item not found, skipping" -ForegroundColor Yellow
    }
}

# Remove .class files if they exist (keep source .java files)
Get-ChildItem -Path $tempDir -Filter "*.class" -Recurse | Remove-Item -Force

# Create ZIP file
Write-Host "Creating ZIP archive: $outputZip" -ForegroundColor Cyan

# Use .NET compression to create ZIP
Add-Type -AssemblyName System.IO.Compression.FileSystem
[System.IO.Compression.ZipFile]::CreateFromDirectory($tempDir, $outputZip, [System.IO.Compression.CompressionLevel]::Optimal, $false)

# Clean up temp directory
Remove-Item $tempDir -Recurse -Force

$zipSize = (Get-Item $outputZip).Length / 1MB
Write-Host "`nBuild complete!" -ForegroundColor Green
Write-Host "  Output: $outputZip ($([math]::Round($zipSize, 2)) MB)" -ForegroundColor Green
Write-Host "`nTo install: Extract $outputZip to your Starsector mods folder" -ForegroundColor Cyan


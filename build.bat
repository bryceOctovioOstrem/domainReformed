@echo off
REM Starsector Mod Build Script (Windows Batch)
REM This script packages the Domain Reformed mod into a ZIP file

echo Building Domain Reformed Mod...

REM Get mod version from mod_info.json (simple extraction)
for /f "tokens=2 delims=:," %%a in ('findstr "version" mod_info.json') do set VERSION=%%a
set VERSION=%VERSION:"=%
set VERSION=%VERSION: =%
set MODNAME=domainReformed
set OUTPUT=%MODNAME%_v%VERSION%.zip

REM Remove old build
if exist "%OUTPUT%" del "%OUTPUT%"

REM Create temp directory
if exist build_temp rmdir /s /q build_temp
mkdir build_temp

echo Packaging mod files...

REM Copy files
xcopy /E /I /Y mod_info.json build_temp\ >nul
xcopy /E /I /Y data build_temp\data\ >nul
xcopy /E /I /Y graphics build_temp\graphics\ >nul
xcopy /E /I /Y sounds build_temp\sounds\ >nul

REM Remove .class files if any
del /s /q build_temp\*.class 2>nul

REM Create ZIP using PowerShell
powershell -Command "Compress-Archive -Path build_temp\* -DestinationPath %OUTPUT% -Force"

REM Clean up
rmdir /s /q build_temp

echo.
echo Build complete!
echo Output: %OUTPUT%
echo.
echo To install: Extract %OUTPUT% to your Starsector mods folder
pause


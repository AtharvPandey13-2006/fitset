@echo off
echo Running backend tests...
powershell.exe -ExecutionPolicy Bypass -File "%~dp0test-backend.ps1"
pause

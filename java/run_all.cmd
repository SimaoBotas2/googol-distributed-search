@echo off
cd /d "%~dp0"

echo A eliminar processos Java anteriores...
taskkill /F /IM java.exe >nul 2>&1
timeout /t 1 >nul

echo A iniciar servicos...

set CP=target\classes;target\lib\*

echo A iniciar Manager...
start "Manager" cmd /k java -cp %CP% barrel.IndexManager
timeout /t 2 >nul

echo A iniciar Downloader...
start "Downloader" cmd /k java -cp %CP% downloader.Downloader 1
timeout /t 2 >nul

echo A iniciar Gateway...
start "Gateway" cmd /k java -cp %CP% gateway.Gateway
timeout /t 2 >nul

echo A iniciar Client...
start "Client" cmd /k java -cp %CP% client.Client

echo.
echo [OK] Todos os servicos foram iniciados!
pause

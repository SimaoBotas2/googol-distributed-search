@echo off
REM ==========================================
REM  SD-GOOGOL - MAQUINA 1
REM  (Barrel 1 + Downloader 1 + Gateway)
REM ==========================================

cd /d "%~dp0"

set CP=target\classes;target\lib\*

echo A iniciar Barrel 1...
start "Barrel1" cmd /k java -Djava.rmi.server.hostname=127.0.0.1 -cp "%CP%" barrel.IndexBarrel 1
timeout /t 2 >nul

echo A iniciar Downloader 1...
start "Downloader1" cmd /k java -Djava.rmi.server.hostname=127.0.0.1 -cp "%CP%" downloader.Downloader 1
timeout /t 2 >nul

echo A iniciar Gateway...
start "Gateway" cmd /k java -Djava.rmi.server.hostname=127.0.0.1 -cp "%CP%" gateway.Gateway

echo.
echo [OK] Todos os servicos da MAQUINA 1 foram iniciados!
pause

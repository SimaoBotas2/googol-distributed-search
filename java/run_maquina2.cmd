@echo off
REM ==========================================
REM  SD-GOOGOL - MAQUINA 2
REM  (Barrel 2 + Manager + Downloader 2 + Client)
REM ==========================================

cd /d "%~dp0"

set CP=target\classes;target\lib\*

echo A iniciar Barrel 2...
start "Barrel2" cmd /k java -Djava.rmi.server.hostname=127.0.0.1 -cp "%CP%" barrel.IndexBarrel 2
timeout /t 5 /nobreak

echo A iniciar Manager...
start "Manager" cmd /k java -Djava.rmi.server.hostname=127.0.0.1 -cp "%CP%" barrel.IndexManager
timeout /t 5 /nobreak

echo A iniciar Downloader 2...
start "Downloader2" cmd /k java -Djava.rmi.server.hostname=127.0.0.1 -cp "%CP%" downloader.Downloader 2
timeout /t 5 /nobreak

echo A iniciar Client...
start "Client" cmd /k java -cp "%CP%" client.Client

echo.
echo [OK] Todos os servicos da MAQUINA 2 foram iniciados!
pause

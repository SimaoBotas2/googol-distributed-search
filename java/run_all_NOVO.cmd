@echo off
setlocal enabledelayedexpansion

cd /d "%~dp0"

REM Usa o target\classes que ja existe
if not exist "target\classes" (
    echo [ERROR] target\classes nao existe!
    pause
    exit /b 1
)

cd target\classes

REM Constrói o classpath com TODAS as bibliotecas
set CP=.
for /r ..\lib %%F in (*.jar) do set CP=!CP!;%%F

echo [DEBUG] Classpath construido com sucesso

echo [DEBUG] A iniciar Manager...
start "Manager" cmd /k java -cp "%CP%" barrel.IndexManager
timeout /t 3 >nul

echo [DEBUG] A iniciar Downloader...
start "Downloader" cmd /k java -cp "%CP%" downloader.Downloader 1
timeout /t 3 >nul

echo [DEBUG] A iniciar Gateway...
start "Gateway" cmd /k java -cp "%CP%" gateway.Gateway

echo [DEBUG] A iniciar Client...
timeout /t 3 >nul
start "Client" cmd /k java -cp "%CP%" client.Client

echo.
echo [OK] Todos os servicos foram iniciados!
pause

@echo off
REM ==========================================
REM  SD-GOOGOL - INICIALIZACAO COMPLETA
REM  (Manager + Downloader + Gateway + Client)
REM ==========================================

setlocal enabledelayedexpansion

cd /d "%~dp0"

echo [DEBUG] Assumindo que classes ja foram compiladas...
echo [DEBUG] (Se nao, roda: mvn compile)

REM Usa o target\classes que ja existe
if not exist "target\classes" (
    echo [ERROR] target\classes nao existe!
    echo [ERROR] Roda primeiro: mvn compile
    pause
    exit /b 1
)

cd target\classes

REM Constrói o classpath com TODAS as bibliotecas
set CP=.
for /r ..\lib %%F in (*.jar) do set CP=!CP!;%%F

echo [DEBUG] Classpath: %CP:~0,100%...

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

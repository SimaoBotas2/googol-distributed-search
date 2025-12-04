@echo off
setlocal enabledelayedexpansion

cd /d "%~dp0"

REM Verifica se target\classes existe
if not exist "target\classes" (
    echo [ERROR] target\classes nao existe! A correr maven package...
    mvn package -DskipTests -q
)

REM Copiar dependencias se nao existirem
if not exist "target\lib" (
    echo [DEBUG] A copiar dependencias...
    mvn dependency:copy-dependencies -q
)

cd target\classes

REM Build classpath com TODOS os .jars de target\lib
set CP=.
for /r "..\lib" %%f in (*.jar) do (
    set CP=!CP!;%%f
)

echo [DEBUG] Classpath incluindo todos os jars de target\lib

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

cd C:\Users\simao\Desktop\Uni\Pasta_Universidade\5ºAno(3- Mestrado . Informática)\1º Semestre\Sistemas Distribuídos\Projeto\sd-googol\java
run_final.cmd

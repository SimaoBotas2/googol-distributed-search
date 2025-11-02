@echo off
REM ==========================================
REM  SD-GOOGOL - MAQUINA 2
REM  (Barrel 2 + Manager + Downloader 2 + Client)
REM ==========================================

cd /d "%~dp0"

echo [1/3] Compilando todos os ficheiros...
javac -cp "target\lib\jsoup-1.18.3.jar" -d target\classes src\main\java\**\*.java

echo [2/3] Copiando config.properties...
copy src\main\java\resources\config.properties target\classes\ >nul

cd target\classes
set CP=.;..\lib\jsoup-1.18.3.jar

echo [3/3] A iniciar componentes...

start "Barrel2" cmd /k java -cp "%CP%" barrel.IndexBarrel 2
timeout /t 3 >nul
start "Manager" cmd /k java -cp "%CP%" barrel.IndexManager
timeout /t 3 >nul
start "Downloader2" cmd /k java -cp "%CP%" downloader.Downloader 2
timeout /t 3 >nul
start "Client" cmd /k java -cp "%CP%" client.Client

echo.
echo [OK] Todos os serviços da MAQUINA 2 foram iniciados!
pause

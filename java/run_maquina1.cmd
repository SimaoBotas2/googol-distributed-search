@echo off
REM ==========================================
REM  SD-GOOGOL - MAQUINA 1
REM  (Barrel 1 + Downloader 1 + Gateway)
REM ==========================================

cd /d "%~dp0"

echo [1/3] Compilando todos os ficheiros...
javac -cp "target\lib\jsoup-1.18.3.jar" -d target\classes src\main\java\**\*.java

echo [2/3] Copiando config.properties...
copy src\main\java\resources\config.properties target\classes\ >nul

cd target\classes
set CP=.;..\lib\jsoup-1.18.3.jar

echo [3/3] A iniciar componentes...

start "Barrel1" cmd /k java -cp "%CP%" barrel.IndexBarrel 1
timeout /t 3 >nul
start "Downloader1" cmd /k java -cp "%CP%" downloader.Downloader 1
timeout /t 3 >nul
start "Gateway" cmd /k java -cp "%CP%" gateway.Gateway

echo.
echo [OK] Todos os serviços da MAQUINA 1 foram iniciados!
pause

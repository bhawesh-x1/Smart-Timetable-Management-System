@echo off
title Smart Campus Desktop Application (PIET v1.0.0)
echo ================================================================================
echo  SMART CAMPUS TIMETABLE MANAGEMENT SYSTEM - DESKTOP APPLICATION LAUNCHER
echo  Runs Main.java: Embedded Java Server + Desktop Window App + Browser Mode
echo ================================================================================
echo.

set "JAVAC_CMD=javac"
set "JAVA_CMD=java"
if exist "C:\Program Files\Java\jdk-21.0.11\bin\javac.exe" (
    set "JAVAC_CMD=C:\Program Files\Java\jdk-21.0.11\bin\javac.exe"
    set "JAVA_CMD=C:\Program Files\Java\jdk-21.0.11\bin\java.exe"
)

if not exist "target\classes" mkdir "target\classes"

echo [1/2] Compiling Main.java & Runtime Modules...
"%JAVAC_CMD%" -encoding UTF-8 -d target\classes ^
  src\main\java\edu\piet\smartcampus\service\AuthHelper.java ^
  src\main\java\edu\piet\smartcampus\runtime\SmartCampusDataStore.java ^
  src\main\java\edu\piet\smartcampus\runtime\SmartCampusServer.java ^
  src\main\java\edu\piet\smartcampus\runtime\SmartCampusDesktopApp.java ^
  src\main\java\edu\piet\smartcampus\runtime\SmartCampusVerificationTest.java ^
  src\main\java\edu\piet\smartcampus\Main.java
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Compilation failed. Ensure JDK 17+ is installed.
    pause
    exit /b 1
)

if not exist "target\classes\templates" mkdir "target\classes\templates"
copy /Y "src\main\resources\templates\*.html" "target\classes\templates\" >nul

echo [2/2] Launching Smart Campus Desktop Application (edu.piet.smartcampus.Main)...
"%JAVA_CMD%" -cp target\classes edu.piet.smartcampus.Main
if %ERRORLEVEL% NEQ 0 pause

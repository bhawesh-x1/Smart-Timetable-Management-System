@echo off
title Smart Campus Timetable Management System (v1.0.0 - College Edition)
echo ================================================================================
echo  SMART CAMPUS TIMETABLE MANAGEMENT SYSTEM (v1.0.0 - College Edition)
echo  Poornima Institute of Engineering ^& Technology (PIET) - Dept. of Computer Engg.
echo ================================================================================
echo.

set "JAVAC_CMD=javac"
set "JAVA_CMD=java"
if exist "C:\Program Files\Java\jdk-21.0.11\bin\javac.exe" (
    set "JAVAC_CMD=C:\Program Files\Java\jdk-21.0.11\bin\javac.exe"
    set "JAVA_CMD=C:\Program Files\Java\jdk-21.0.11\bin\java.exe"
)

if not exist "target\classes" mkdir "target\classes"

echo [1/2] Compiling Java Enterprise Runtime Server (JDK 21)...
"%JAVAC_CMD%" -encoding UTF-8 -d target\classes ^
  src\main\java\edu\piet\smartcampus\service\AuthHelper.java ^
  src\main\java\edu\piet\smartcampus\runtime\SmartCampusDataStore.java ^
  src\main\java\edu\piet\smartcampus\runtime\SmartCampusServer.java ^
  src\main\java\edu\piet\smartcampus\runtime\SmartCampusDesktopApp.java ^
  src\main\java\edu\piet\smartcampus\runtime\SmartCampusVerificationTest.java ^
  src\main\java\edu\piet\smartcampus\Main.java
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Compilation failed. Please ensure Java JDK 17+ is installed.
    pause
    exit /b 1
)

if not exist "target\classes\templates" mkdir "target\classes\templates"
copy /Y "src\main\resources\templates\*.html" "target\classes\templates\" >nul

echo [2/2] Starting Smart Campus Server on http://localhost:8085/login ...
start "" http://localhost:8085/login
"%JAVA_CMD%" -cp target\classes edu.piet.smartcampus.runtime.SmartCampusServer 8085
pause

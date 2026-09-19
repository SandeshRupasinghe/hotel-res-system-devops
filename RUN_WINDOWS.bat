@echo off
setlocal
cd /d "%~dp0"

echo ==============================================
echo   Hotel Reservation System - Local Launcher
echo ==============================================
echo.

where java >nul 2>nul
if errorlevel 1 (
    echo ERROR: Java was not found.
    echo Install JDK 17 or newer, then reopen this window.
    pause
    exit /b 1
)

echo Java detected:
java -version
echo.
echo NOTE: MySQL must already be running and database/setup.sql must have been run once.
echo If your MySQL root account has a password, set DB_PASSWORD before starting.
echo.
echo Maven does NOT need to be installed. mvnw.cmd will download the project Maven version automatically the first time.
echo.

call mvnw.cmd spring-boot:run
if errorlevel 1 (
    echo.
    echo The application stopped with an error. Read the messages above.
    pause
    exit /b 1
)

endlocal

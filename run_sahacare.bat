@echo off
echo SahaCare - Healthcare Management System
echo ======================================
echo.

:: Check if XAMPP is running by checking MySQL port
echo Checking if MySQL is running...
netstat -an | findstr :3306 > nul
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERROR: MySQL does not appear to be running!
    echo Please start XAMPP and ensure MySQL is running before continuing.
    echo.
    echo Steps to start XAMPP:
    echo 1. Open the XAMPP Control Panel
    echo 2. Click Start next to MySQL
    echo 3. Wait until it shows as running (green)
    echo 4. Run this script again
    echo.
    pause
    exit /b 1
)

echo MySQL is running. Setting up the database...
echo.

:: Set MySQL path
set MYSQL_PATH="C:\xampp\mysql\bin\mysql.exe"
if not exist %MYSQL_PATH% (
    echo MySQL executable not found at default XAMPP location.
    echo.
    echo Please enter the path to your MySQL executable:
    set /p MYSQL_PATH=
)

:: Test the database connection first
echo Testing database connection...
%MYSQL_PATH% -u root -e "SELECT 1" > nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERROR: Unable to connect to MySQL!
    echo Please check that:
    echo - MySQL is running in XAMPP
    echo - The root user has no password set (default XAMPP configuration)
    echo.
    pause
    exit /b 1
)

:: Run the database setup script
echo Setting up SahaCare database tables...
%MYSQL_PATH% -u root < sahacare_auth_setup.sql

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERROR: Database setup failed!
    echo Please check the sahacare_auth_setup.sql file and try again.
    echo Common issues:
    echo - Syntax errors in SQL
    echo - Insufficient permissions
    echo - Database already exists with incompatible schema
    echo.
    pause
    exit /b 1
)

echo.
echo Database setup complete!
echo Test accounts have been created.
echo.
echo Starting SahaCare application...
echo.

:: Run the application with Maven
echo Compiling and running the application...
call mvn clean javafx:run

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Application execution failed.
    echo.
    echo Common issues:
    echo - Missing JavaFX dependencies
    echo - Compilation errors
    echo - Path issues with resources
    echo.
    echo For more details, try running: mvn clean javafx:run -X
    echo.
    pause
)

echo.
echo Goodbye!
exit /b 0 
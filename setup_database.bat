@echo off
echo SahaCare Database Setup
echo =====================

REM Check if MySQL executable exists in default XAMPP location
set MYSQL_PATH="C:\xampp\mysql\bin\mysql.exe"
if not exist %MYSQL_PATH% (
  echo MySQL executable not found at default XAMPP location.
  echo Please enter the path to your MySQL executable:
  set /p MYSQL_PATH=
)

echo.
echo Setting up the SahaCare database...
echo.

REM Execute the SQL setup script with default XAMPP credentials
%MYSQL_PATH% -u root < sahacare_auth_setup.sql

if %ERRORLEVEL% == 0 (
  echo.
  echo =============================================
  echo Database setup completed successfully!
  echo.
  echo You can now log in with the following test accounts:
  echo.
  echo Admin:      username: admin,      password: password123
  echo Doctor:     username: dr.smith,   password: password123
  echo Patient:    username: patient1,   password: password123
  echo Laboratory: username: lab_central, password: password123
  echo =============================================
) else (
  echo.
  echo Error: Database setup failed.
  echo Make sure XAMPP is running and MySQL service is started.
)

echo.
pause 
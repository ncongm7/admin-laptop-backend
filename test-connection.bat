@echo off
echo Testing SQL Server Connection...
sqlcmd -S localhost\SQLEXPRESS -U sa -P "Admin@2025" -Q "SELECT DB_NAME() as CurrentDB, @@VERSION as SQLVersion"
if %ERRORLEVEL% EQU 0 (
    echo Connection SUCCESS!
) else (
    echo Connection FAILED! Error code: %ERRORLEVEL%
)
pause

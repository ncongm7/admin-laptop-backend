@echo off
chcp 65001
echo ==============================================
echo RE-CREATING DATABASE AND IMPORTING DATA
echo ==============================================

echo 1. Running datareal.sql (Schema & Initial Data)...
sqlcmd -S . -E -i datareal.sql
if %errorlevel% neq 0 (
    echo Error running datareal.sql
    pause
    exit /b %errorlevel%
)

echo 2. Running datareal2.sql (Supplement Data)...
sqlcmd -S . -E -i datareal2.sql
if %errorlevel% neq 0 (
    echo Error running datareal2.sql. Continuing...
)

echo 3. Running datareal3.sql (Supplement Data)...
sqlcmd -S . -E -i datareal3.sql
if %errorlevel% neq 0 (
    echo Error running datareal3.sql. Continuing...
)

echo 4. Running datareal4.sql (Warranty Data)...
sqlcmd -S . -E -i datareal4.sql
if %errorlevel% neq 0 (
    echo Error running datareal4.sql. Continuing...
)

echo 5. Running datareal5.sql (Statistics System Enhancement)...
sqlcmd -S . -E -i datareal5.sql
if %errorlevel% neq 0 (
    echo Error running datareal5.sql. Continuing...
)

echo ==============================================
echo DONE. DATABASE RE-CREATED SUCESSFULLY.
echo ==============================================
pause

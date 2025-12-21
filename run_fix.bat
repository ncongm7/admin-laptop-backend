@echo off
chcp 65001
echo ==============================================
echo FIXING CHAT DATABASE SCHEMA
echo ==============================================
sqlcmd -S . -E -i fix_chat_schema.sql
if %errorlevel% neq 0 (
    echo Error running fix_chat_schema.sql
    pause
    exit /b %errorlevel%
)
echo ==============================================
echo DONE. SCHEMA UPDATED.
echo ==============================================

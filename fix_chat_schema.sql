USE QuanLyBanHangLaptop_TheoERD1_New;
GO

PRINT '>> Fixing chat_sessions schema...';

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'[dbo].[chat_sessions]') AND name = 'current_state')
BEGIN
    ALTER TABLE chat_sessions ADD current_state NVARCHAR(50);
    PRINT 'Added current_state column';
END

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'[dbo].[chat_sessions]') AND name = 'goal')
BEGIN
    ALTER TABLE chat_sessions ADD goal NVARCHAR(50);
    PRINT 'Added goal column';
END

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'[dbo].[chat_sessions]') AND name = 'progress_data')
BEGIN
    ALTER TABLE chat_sessions ADD progress_data NVARCHAR(MAX);
    PRINT 'Added progress_data column';
END

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'[dbo].[chat_sessions]') AND name = 'step_count')
BEGIN
    ALTER TABLE chat_sessions ADD step_count INT;
    PRINT 'Added step_count column';
END

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'[dbo].[chat_sessions]') AND name = 'is_stuck')
BEGIN
    ALTER TABLE chat_sessions ADD is_stuck BIT;
    PRINT 'Added is_stuck column';
END

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'[dbo].[chat_sessions]') AND name = 'context_data')
BEGIN
    ALTER TABLE chat_sessions ADD context_data NVARCHAR(MAX);
    PRINT 'Added context_data column';
END

PRINT '>> Fixing chat_intents schema...';

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'[dbo].[chat_intents]') AND name = 'business_role')
BEGIN
    ALTER TABLE chat_intents ADD business_role NVARCHAR(50);
    PRINT 'Added business_role column';
END

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'[dbo].[chat_intents]') AND name = 'triggers_state_change')
BEGIN
    ALTER TABLE chat_intents ADD triggers_state_change BIT;
    PRINT 'Added triggers_state_change column';
END

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'[dbo].[chat_intents]') AND name = 'next_state')
BEGIN
    ALTER TABLE chat_intents ADD next_state NVARCHAR(50);
    PRINT 'Added next_state column';
END

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'[dbo].[chat_intents]') AND name = 'requires_auth')
BEGIN
    ALTER TABLE chat_intents ADD requires_auth BIT;
    PRINT 'Added requires_auth column';
END

PRINT '>> Schema fix completed.';

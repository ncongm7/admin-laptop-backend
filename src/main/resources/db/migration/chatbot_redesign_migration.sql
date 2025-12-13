-- ===================================================================
-- CHATBOT REDESIGN - DATABASE MIGRATION
-- ===================================================================
-- Run this script to add new fields for goal-oriented chatbot
-- Date: 2025-12-13
-- ===================================================================

-- 1. UPDATE chat_sessions table
-- Add state machine fields
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'chat_sessions') AND name = 'current_state')
BEGIN
    ALTER TABLE chat_sessions ADD current_state NVARCHAR(50);
    PRINT 'Added current_state to chat_sessions';
END

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'chat_sessions') AND name = 'goal')
BEGIN
    ALTER TABLE chat_sessions ADD goal NVARCHAR(50);
    PRINT 'Added goal to chat_sessions';
END

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'chat_sessions') AND name = 'progress_data')
BEGIN
    ALTER TABLE chat_sessions ADD progress_data NVARCHAR(MAX);
    PRINT 'Added progress_data to chat_sessions';
END

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'chat_sessions') AND name = 'step_count')
BEGIN
    ALTER TABLE chat_sessions ADD step_count INT DEFAULT 0;
    PRINT 'Added step_count to chat_sessions';
END

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'chat_sessions') AND name = 'is_stuck')
BEGIN
    ALTER TABLE chat_sessions ADD is_stuck BIT DEFAULT 0;
    PRINT 'Added is_stuck to chat_sessions';
END

-- 2. UPDATE chat_intents table
-- Add business metadata fields
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'chat_intents') AND name = 'business_role')
BEGIN
    ALTER TABLE chat_intents ADD business_role NVARCHAR(50);
    PRINT 'Added business_role to chat_intents';
END

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'chat_intents') AND name = 'triggers_state_change')
BEGIN
    ALTER TABLE chat_intents ADD triggers_state_change BIT DEFAULT 0;
    PRINT 'Added triggers_state_change to chat_intents';
END

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'chat_intents') AND name = 'next_state')
BEGIN
    ALTER TABLE chat_intents ADD next_state NVARCHAR(50);
    PRINT 'Added next_state to chat_intents';
END

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'chat_intents') AND name = 'requires_auth')
BEGIN
    ALTER TABLE chat_intents ADD requires_auth BIT DEFAULT 0;
    PRINT 'Added requires_auth to chat_intents';
END

PRINT 'Migration completed successfully!';
GO

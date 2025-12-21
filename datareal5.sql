-- =====================================================
-- Statistics System Enhancement Migration
-- Version: V3
-- Description: Add payment tracking and audit trail capabilities
-- =====================================================

-- Chọn database
USE QuanLyBanHangLaptop_TheoERD1_New;
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'[dbo].[chat_sessions]') AND name = 'current_state')
BEGIN
    ALTER TABLE chat_sessions ADD current_state NVARCHAR(50);
END
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'[dbo].[chat_sessions]') AND name = 'goal')
BEGIN
    ALTER TABLE chat_sessions ADD goal NVARCHAR(50);
END
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'[dbo].[chat_sessions]') AND name = 'progress_data')
BEGIN
    ALTER TABLE chat_sessions ADD progress_data NVARCHAR(MAX);
END
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'[dbo].[chat_sessions]') AND name = 'step_count')
BEGIN
    ALTER TABLE chat_sessions ADD step_count INT;
END
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'[dbo].[chat_sessions]') AND name = 'is_stuck')
BEGIN
    ALTER TABLE chat_sessions ADD is_stuck BIT;
END
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'[dbo].[chat_sessions]') AND name = 'context_data')
BEGIN
    ALTER TABLE chat_sessions ADD context_data NVARCHAR(MAX);
END

-- Add new columns to hoa_don table
ALTER TABLE hoa_don 
ADD payment_method VARCHAR(20),
    payment_confirmed_at DATETIME2,
    sales_channel VARCHAR(20);
GO

-- Create index for faster statistics queries
CREATE INDEX idx_hoa_don_payment_stats 
ON hoa_don(payment_method, sales_channel, payment_confirmed_at, trang_thai, ngay_tao);
GO

-- =====================================================
-- Payment Logs Table - Track all payment events
-- =====================================================
CREATE TABLE payment_logs (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    hoa_don_id UNIQUEIDENTIFIER NOT NULL,
    payment_method VARCHAR(20) NOT NULL,
    amount DECIMAL(18,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    confirmed_at DATETIME2,
    created_at DATETIME2 DEFAULT GETDATE(),
    notes NVARCHAR(MAX),
    FOREIGN KEY (hoa_don_id) REFERENCES hoa_don(id) ON DELETE CASCADE
);

-- Index for querying payment logs by order
CREATE INDEX idx_payment_logs_hoa_don 
ON payment_logs(hoa_don_id, created_at DESC);

-- Index for payment confirmation tracking
CREATE INDEX idx_payment_logs_status 
ON payment_logs(status, confirmed_at);
GO

-- =====================================================
-- Order Status Logs Table - Track all status changes
-- =====================================================
CREATE TABLE order_status_logs (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    hoa_don_id UNIQUEIDENTIFIER NOT NULL,
    old_status INT,
    new_status INT NOT NULL,
    changed_by UNIQUEIDENTIFIER,
    changed_at DATETIME2 DEFAULT GETDATE(),
    reason NVARCHAR(500),
    FOREIGN KEY (hoa_don_id) REFERENCES hoa_don(id) ON DELETE CASCADE
);

-- Index for querying status history
CREATE INDEX idx_order_status_logs_hoa_don 
ON order_status_logs(hoa_don_id, changed_at DESC);

-- Index for audit queries
CREATE INDEX idx_order_status_logs_changed_by 
ON order_status_logs(changed_by, changed_at DESC);
GO

-- =====================================================
-- Data Migration for Existing Orders
-- =====================================================

-- Set sales_channel based on loai_hoa_don
-- 0 = POS (bán tại quầy), 1 = ONLINE
UPDATE hoa_don 
SET sales_channel = CASE 
    WHEN loai_hoa_don = 0 THEN 'POS'
    WHEN loai_hoa_don = 1 THEN 'ONLINE'
    ELSE 'POS'  -- Default to POS for NULL or unknown values
END
WHERE sales_channel IS NULL;
GO

-- Set payment_method for completed orders (heuristic approach)
-- For POS orders: assume CASH
-- For ONLINE orders: if ngay_thanh_toan exists, assume it was paid (could be QR or COD delivered)
UPDATE hoa_don
SET payment_method = CASE
    WHEN sales_channel = 'POS' THEN 'CASH'
    WHEN sales_channel = 'ONLINE' AND trang_thai = 4 THEN 'COD'  -- Completed online = COD delivered
    WHEN sales_channel = 'ONLINE' AND ngay_thanh_toan IS NOT NULL THEN 'QR'  -- Has payment date = QR
    ELSE 'CASH'  -- Default
END
WHERE payment_method IS NULL;
GO

-- Set payment_confirmed_at for completed orders
-- Use ngay_thanh_toan if available, otherwise use ngay_tao for completed orders
UPDATE hoa_don
SET payment_confirmed_at = COALESCE(ngay_thanh_toan, ngay_tao)
WHERE payment_confirmed_at IS NULL 
  AND trang_thai IN (1, 4)  -- DA_THANH_TOAN or HOAN_THANH
  AND payment_method IN ('CASH', 'QR');  -- Don't set for COD unless delivered
GO

-- For COD orders that are completed (status 4), set confirmation time
UPDATE hoa_don
SET payment_confirmed_at = ngay_thanh_toan
WHERE payment_confirmed_at IS NULL
  AND payment_method = 'COD'
  AND trang_thai = 4;  -- HOAN_THANH
GO

-- =====================================================
-- Create initial payment logs for existing orders
-- =====================================================
INSERT INTO payment_logs (hoa_don_id, payment_method, amount, status, confirmed_at, created_at, notes)
SELECT 
    id as hoa_don_id,
    payment_method,
    COALESCE(tong_tien_sau_giam, 0) as amount,  -- Handle NULL amounts
    CASE 
        WHEN payment_confirmed_at IS NOT NULL THEN 'CONFIRMED'
        WHEN trang_thai = 2 THEN 'CANCELLED'
        ELSE 'PENDING'
    END as status,
    payment_confirmed_at,
    ngay_tao as created_at,
    'Migrated from existing data' as notes
FROM hoa_don
WHERE id NOT IN (SELECT DISTINCT hoa_don_id FROM payment_logs);
GO

-- =====================================================
-- Create initial status logs for existing orders
-- =====================================================
INSERT INTO order_status_logs (hoa_don_id, old_status, new_status, changed_at, reason)
SELECT 
    id as hoa_don_id,
    NULL as old_status,
    trang_thai as new_status,
    ngay_tao as changed_at,
    'Initial status from migration' as reason
FROM hoa_don
WHERE id NOT IN (SELECT DISTINCT hoa_don_id FROM order_status_logs);
GO

PRINT 'Migration completed successfully!';

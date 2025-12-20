-- =====================================================
-- HƯỚNG DẪN CHẠY MIGRATION THỦ CÔNG
-- =====================================================
-- Copy toàn bộ script dưới đây và chạy trong SQL Server Management Studio (SSMS)
-- hoặc Azure Data Studio kết nối đến database: QuanLyBanHangLaptop_TheoERD1_New

USE QuanLyBanHangLaptop_TheoERD1_New;
GO

-- Kiểm tra xem columns đã tồn tại chưa
IF NOT EXISTS (
    SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = 'hoa_don' AND COLUMN_NAME = 'payment_method'
)
BEGIN
    PRINT 'Adding new columns to hoa_don table...';
    
    -- Add new columns to hoa_don table
    ALTER TABLE hoa_don 
    ADD payment_method VARCHAR(20),
        payment_confirmed_at DATETIME2,
        sales_channel VARCHAR(20);
    
    PRINT '✅ Columns added successfully!';
END
ELSE
BEGIN
    PRINT 'ℹ️ Columns already exist, skipping...';
END
GO

-- Create index for faster statistics queries
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'idx_hoa_don_payment_stats')
BEGIN
    PRINT 'Creating index for payment statistics...';
    CREATE INDEX idx_hoa_don_payment_stats 
    ON hoa_don(payment_method, sales_channel, payment_confirmed_at, trang_thai, ngay_tao);
    PRINT '✅ Index created!';
END
GO

-- =====================================================
-- Payment Logs Table - Track all payment events
-- =====================================================
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'payment_logs')
BEGIN
    PRINT 'Creating payment_logs table...';
    
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
    
    PRINT '✅ payment_logs table created!';
END
ELSE
BEGIN
    PRINT 'ℹ️ payment_logs table already exists';
END
GO

-- =====================================================
-- Order Status Logs Table - Track all status changes
-- =====================================================
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'order_status_logs')
BEGIN
    PRINT 'Creating order_status_logs table...';
    
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
    
    PRINT '✅ order_status_logs table created!';
END
ELSE
BEGIN
    PRINT 'ℹ️ order_status_logs table already exists';
END
GO

-- =====================================================
-- Data Migration for Existing Orders
-- =====================================================
PRINT 'Starting data migration for existing orders...';

-- Set sales_channel based on loai_hoa_don
-- 0 = POS (bán tại quầy), 1 = ONLINE
UPDATE hoa_don 
SET sales_channel = CASE 
    WHEN loai_hoa_don = 0 THEN 'POS'
    WHEN loai_hoa_don = 1 THEN 'ONLINE'
    ELSE 'POS'  -- Default to POS for NULL or unknown values
END
WHERE sales_channel IS NULL;
PRINT '✅ sales_channel updated';
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
PRINT '✅ payment_method updated';
GO

-- Set payment_confirmed_at for completed orders
-- Use ngay_thanh_toan if available, otherwise use ngay_tao for completed orders
UPDATE hoa_don
SET payment_confirmed_at = COALESCE(ngay_thanh_toan, ngay_tao)
WHERE payment_confirmed_at IS NULL 
  AND trang_thai IN (1, 4)  -- DA_THANH_TOAN or HOAN_THANH
  AND payment_method IN ('CASH', 'QR');  -- Don't set for COD unless delivered
PRINT '✅ payment_confirmed_at updated for CASH/QR';
GO

-- For COD orders that are completed (status 4), set confirmation time
UPDATE hoa_don
SET payment_confirmed_at = ngay_thanh_toan
WHERE payment_confirmed_at IS NULL
  AND payment_method = 'COD'
  AND trang_thai = 4;  -- HOAN_THANH
PRINT '✅ payment_confirmed_at updated for COD';
GO

-- =====================================================
-- Create initial payment logs for existing orders
-- =====================================================
PRINT 'Creating payment logs for existing orders...';

INSERT INTO payment_logs (hoa_don_id, payment_method, amount, status, confirmed_at, created_at, notes)
SELECT 
    id as hoa_don_id,
    payment_method,
    tong_tien_sau_giam as amount,
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

PRINT '✅ Payment logs created: ' + CAST(@@ROWCOUNT AS VARCHAR);
GO

-- =====================================================
-- Create initial status logs for existing orders
-- =====================================================
PRINT 'Creating status logs for existing orders...';

INSERT INTO order_status_logs (hoa_don_id, old_status, new_status, changed_at, reason)
SELECT 
    id as hoa_don_id,
    NULL as old_status,
    trang_thai as new_status,
    ngay_tao as changed_at,
    'Initial status from migration' as reason
FROM hoa_don
WHERE id NOT IN (SELECT DISTINCT hoa_don_id FROM order_status_logs);

PRINT '✅ Status logs created: ' + CAST(@@ROWCOUNT AS VARCHAR);
GO

PRINT '';
PRINT '========================================';
PRINT '✅ MIGRATION COMPLETED SUCCESSFULLY!';
PRINT '========================================';
PRINT '';
PRINT 'Next steps:';
PRINT '1. Restart Spring Boot application';
PRINT '2. Test API endpoints at /api/v1/thongke/';
PRINT '3. Verify data in new columns';
PRINT '';

-- Add column if not exists (Though checked it exists in some dumps, safer to check)
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'[dbo].[san_pham]') AND name = 'thoi_han_bh_thang')
BEGIN
    ALTER TABLE [dbo].[san_pham] ADD [thoi_han_bh_thang] INT DEFAULT 12;
    PRINT 'Added column thoi_han_bh_thang to table san_pham';
END
GO

-- Update existing records that might have NULL to default 12
UPDATE [dbo].[san_pham]
SET [thoi_han_bh_thang] = 12
WHERE [thoi_han_bh_thang] IS NULL;
PRINT 'Updated NULL warranty periods to 12 months';
GO

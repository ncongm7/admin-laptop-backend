USE QuanLyBanHangLaptop_TheoERD1_New;
GO

DECLARE @CashId UNIQUEIDENTIFIER;
DECLARE @QrId UNIQUEIDENTIFIER;

SELECT @CashId = MIN(id)
FROM phuong_thuc_thanh_toan
WHERE loai_phuong_thuc = 'Cash';

SELECT @QrId = MIN(id)
FROM phuong_thuc_thanh_toan
WHERE loai_phuong_thuc = 'QR Payment';

-- Re-point payment details to the target records
UPDATE chi_tiet_thanh_toan
SET phuong_thuc_thanh_toan_id = @CashId
WHERE phuong_thuc_thanh_toan_id <> @CashId
  AND phuong_thuc_thanh_toan_id IN (
      SELECT id FROM phuong_thuc_thanh_toan WHERE loai_phuong_thuc = 'Cash'
  );

UPDATE chi_tiet_thanh_toan
SET phuong_thuc_thanh_toan_id = @QrId
WHERE phuong_thuc_thanh_toan_id <> @QrId
  AND phuong_thuc_thanh_toan_id IN (
      SELECT id FROM phuong_thuc_thanh_toan WHERE loai_phuong_thuc = 'QR Payment'
  );

-- Remove duplicate rows that no longer have references
DELETE FROM phuong_thuc_thanh_toan
WHERE loai_phuong_thuc = 'Cash' AND id <> @CashId;

DELETE FROM phuong_thuc_thanh_toan
WHERE loai_phuong_thuc = 'QR Payment' AND id <> @QrId;

-- Update display names to ASCII variants
UPDATE phuong_thuc_thanh_toan
SET ten_phuong_thuc = CASE
        WHEN loai_phuong_thuc = 'Cash' THEN 'Tien mat'
        WHEN loai_phuong_thuc = 'QR Payment' THEN 'Chuyen khoan QR'
        ELSE ten_phuong_thuc
    END;

-- Show final state
SELECT id, ten_phuong_thuc, loai_phuong_thuc
FROM phuong_thuc_thanh_toan;
GO

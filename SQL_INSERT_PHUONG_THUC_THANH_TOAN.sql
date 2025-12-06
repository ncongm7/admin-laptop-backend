-- ===================================================================================
-- SQL Script: INSERT PHƯƠNG THỨC THANH TOÁN
-- Mô tả: Insert dữ liệu phương thức thanh toán vào bảng phuong_thuc_thanh_toan
-- Ngày tạo: 2025-12-06
-- ===================================================================================

USE QuanLyBanHangLaptop_TheoERD1_New;
GO

-- Xóa dữ liệu cũ (nếu có) để tránh duplicate
DELETE FROM phuong_thuc_thanh_toan;
GO

-- Insert các phương thức thanh toán
INSERT INTO phuong_thuc_thanh_toan (id, ten_phuong_thuc, loai_phuong_thuc) VALUES
(NEWID(), N'Tiền mặt', 'Cash'),
(NEWID(), N'Chuyển khoản QR', 'QR Payment'),
(NEWID(), N'Thẻ tín dụng', 'Credit Card'),
(NEWID(), N'Thẻ ghi nợ', 'Debit Card'),
(NEWID(), N'Ví điện tử', 'E-Wallet');
GO

-- Kiểm tra kết quả
SELECT id, ten_phuong_thuc, loai_phuong_thuc
FROM phuong_thuc_thanh_toan
ORDER BY loai_phuong_thuc;
GO

PRINT 'Đã thêm ' + CAST(@@ROWCOUNT AS VARCHAR(10)) + ' phương thức thanh toán thành công!';
GO







-- ===================================================================================
-- SCRIPT INSERT PHƯƠNG THỨC THANH TOÁN
-- Database: QuanLyBanHangLaptop_TheoERD1_New
-- Mô tả: Thêm các phương thức thanh toán vào database
-- ===================================================================================

USE QuanLyBanHangLaptop_TheoERD1_New;
GO

-- Xóa dữ liệu cũ trước khi thêm mới
DELETE FROM phuong_thuc_thanh_toan;
GO

-- Thêm các phương thức thanh toán
INSERT INTO phuong_thuc_thanh_toan (ten_phuong_thuc, loai_phuong_thuc) VALUES
('Tiền mặt', 'Cash'),
('Chuyển khoản QR', 'QR Payment');

-- Kiểm tra kết quả
SELECT * FROM phuong_thuc_thanh_toan;
GO

PRINT 'Đã thêm 2 phương thức thanh toán thành công!';
GO

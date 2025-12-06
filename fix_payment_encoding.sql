USE QuanLyBanHangLaptop_TheoERD1_New;
GO

-- Cập nhật lại tên phương thức thanh toán cho đúng tiếng Việt
UPDATE phuong_thuc_thanh_toan 
SET ten_phuong_thuc = N'Tiền mặt' 
WHERE loai_phuong_thuc = 'Cash' OR ten_phuong_thuc LIKE N'%Ti%n m%t%';

UPDATE phuong_thuc_thanh_toan 
SET ten_phuong_thuc = N'Chuyển khoản QR' 
WHERE loai_phuong_thuc = 'QR Payment' OR ten_phuong_thuc LIKE N'%QR%';

GO

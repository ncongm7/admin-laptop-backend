-- ===================================================================================
-- SCRIPT BỔ SUNG DỮ LIỆU DANH MỤC BẢO HÀNH (WARRANTY MASTER DATA)
-- Mục đích: Insert đầy đủ Lý do bảo hành & Địa điểm bảo hành
-- ===================================================================================

USE QuanLyBanHangLaptop_TheoERD1_New;
GO

PRINT '>> Bat dau bo sung du lieu Bao Hanh...';

-- ===================================================================================
-- 1. INSERT ĐẦY ĐỦ 16 LÝ DO BẢO HÀNH
-- ===================================================================================

-- Xóa dữ liệu cũ (nếu có) để đảm bảo sạch sẽ và không trùng lặp ID/Mã
DELETE FROM ly_do_bao_hanh;
GO

INSERT INTO ly_do_bao_hanh (id, ma_ly_do, ten_ly_do, mo_ta, loai_ly_do, is_active, thu_tu, ngay_tao) VALUES
(NEWID(), 'LDBH001', N'Màn hình bị vỡ/nứt', N'Màn hình laptop bị vỡ, nứt hoặc có vết nứt do va đập hoặc lỗi sản xuất', 'PHAN_CUNG', 1, 1, GETDATE()),
(NEWID(), 'LDBH002', N'Bàn phím không hoạt động', N'Bàn phím không phản hồi, bị liệt một số phím hoặc toàn bộ', 'PHAN_CUNG', 1, 2, GETDATE()),
(NEWID(), 'LDBH003', N'Touchpad không phản hồi', N'Bàn di chuột (touchpad) không hoạt động, loạn cảm ứng hoặc phản hồi chậm', 'PHAN_CUNG', 1, 3, GETDATE()),
(NEWID(), 'LDBH004', N'Pin không sạc được', N'Pin không nhận sạc, sạc không vào điện hoặc pin bị chai phồng', 'PHAN_CUNG', 1, 4, GETDATE()),
(NEWID(), 'LDBH005', N'Quạt tản nhiệt kêu to/bị lỗi', N'Quạt tản nhiệt phát ra tiếng kêu to bất thường hoặc không quay', 'PHAN_CUNG', 1, 5, GETDATE()),
(NEWID(), 'LDBH006', N'Ổ cứng bị lỗi', N'Ổ cứng không nhận diện được, máy báo lỗi ổ cứng hoặc có tiếng kêu lạ từ ổ cứng cơ', 'PHAN_CUNG', 1, 6, GETDATE()),
(NEWID(), 'LDBH007', N'Lỗi bo mạch chủ (Mainboard)', N'Máy không khởi động được nguồn, bị chập cháy bo mạch', 'PHAN_CUNG', 1, 7, GETDATE()),
(NEWID(), 'LDBH008', N'Cổng kết nối không hoạt động', N'Các cổng USB, HDMI, LAN, Audio không nhận thiết bị ngoại vi', 'PHAN_CUNG', 1, 8, GETDATE()),
(NEWID(), 'LDBH009', N'Loa không có âm thanh/bị rè', N'Loa máy tính không phát ra tiếng hoặc âm thanh bị rè, méo tiếng', 'PHAN_CUNG', 1, 9, GETDATE()),
(NEWID(), 'LDBH010', N'Webcam không hoạt động', N'Webcam không bật được hoặc hình ảnh bị nhiễu, đen màn hình', 'PHAN_CUNG', 1, 10, GETDATE()),
(NEWID(), 'LDBH011', N'Lỗi hệ điều hành/Phần mềm', N'Hệ điều hành Windows bị lỗi, treo logo, không boot được', 'PHAN_MEM', 1, 11, GETDATE()),
(NEWID(), 'LDBH012', N'Máy tự động tắt/khởi động lại', N'Máy tính đang sử dụng tự động sập nguồn hoặc khởi động lại liên tục', 'PHAN_MEM', 1, 12, GETDATE()),
(NEWID(), 'LDBH013', N'Màn hình xanh (Blue Screen)', N'Máy tính hiển thị màn hình xanh chết chóc (BSOD) kèm mã lỗi', 'PHAN_MEM', 1, 13, GETDATE()),
(NEWID(), 'LDBH014', N'Sạc (Adapter) bị hỏng', N'Bộ sạc laptop bị hỏng, đèn báo không sáng, không cấp điện cho máy', 'PHU_KIEN', 1, 14, GETDATE()),
(NEWID(), 'LDBH015', N'Cáp kết nối bị đứt/lỏng', N'Cáp màn hình, cáp nguồn bị đứt ngầm hoặc lỏng jack cắm', 'PHU_KIEN', 1, 15, GETDATE()),
(NEWID(), 'LDBH016', N'Lỗi khác (Mô tả chi tiết)', N'Các lỗi không thuộc danh mục trên, cần kỹ thuật viên kiểm tra trực tiếp', 'KHAC', 1, 16, GETDATE());

PRINT '>> Da insert 16 Ly do bao hanh.';
GO

-- ===================================================================================
-- 2. INSERT CÁC TRUNG TÂM BẢO HÀNH
-- ===================================================================================

-- Xóa dữ liệu cũ
DELETE FROM dia_diem_bao_hanh;
GO

INSERT INTO dia_diem_bao_hanh (id, ten, dia_chi, so_dien_thoai, email, gio_lam_viec, is_active, ngay_tao) VALUES
(NEWID(), N'Trung tâm bảo hành Hà Nội', N'Số 123 Đường Cầu Giấy, Quận Cầu Giấy, Hà Nội', '024-1234-5678', 'baohanh.hn@laptopstore.vn', N'08:00 - 17:30 (Thứ 2 - Thứ 7)', 1, GETDATE()),
(NEWID(), N'Trung tâm bảo hành TP.HCM', N'Số 456 Đường Nguyễn Thị Minh Khai, Quận 3, TP.HCM', '028-9876-5432', 'baohanh.hcm@laptopstore.vn', N'08:00 - 17:30 (Thứ 2 - Thứ 7)', 1, GETDATE()),
(NEWID(), N'Trung tâm bảo hành Đà Nẵng', N'Số 789 Đường Nguyễn Văn Linh, Quận Hải Châu, Đà Nẵng', '0236-5555-6666', 'baohanh.dn@laptopstore.vn', N'08:00 - 17:00 (Thứ 2 - Thứ 6)', 1, GETDATE());

PRINT '>> Da insert 3 Dia diem bao hanh.';
GO

PRINT '>> HOAN TAT BO SUNG DU LIEU BAO HANH!';
GO
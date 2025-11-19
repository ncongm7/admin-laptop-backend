-- ===================================================================================
-- SCRIPT INSERT DỮ LIỆU ĐỊA LÝ VIỆT NAM (TỈNH/THÀNH PHỐ - HUYỆN/QUẬN - XÃ/PHƯỜNG)
-- ===================================================================================
-- File này chứa dữ liệu địa lý đầy đủ, tách riêng để dễ quản lý
-- Chạy file này SAU KHI đã chạy data.sql (để tạo bảng)
-- ===================================================================================

USE QuanLyBanHangLaptop_TheoERD1;
GO

-- ===================================================================================
-- 1. TẠO BẢNG lc_district (HUYỆN/QUẬN) NẾU CHƯA CÓ
-- ===================================================================================

IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='lc_district' AND xtype='U')
BEGIN
    CREATE TABLE lc_district (
        id            INT           NOT NULL PRIMARY KEY,
        province_id   INT           NOT NULL,
        province_code NVARCHAR(20)  NOT NULL,
        name          NVARCHAR(100) NOT NULL,
        shortname     NVARCHAR(100) NULL,
        code          NVARCHAR(36)  NULL,
        description   NVARCHAR(255) NULL,
        created_by    NVARCHAR(100) NULL,
        created_date  DATETIME2(6)  NULL,
        modified_by   NVARCHAR(100) NULL,
        modified_date DATETIME2(6)  NULL,
        FOREIGN KEY (province_id) REFERENCES lc_province(id)
    );
    PRINT 'Đã tạo bảng lc_district';
END
ELSE
BEGIN
    PRINT 'Bảng lc_district đã tồn tại';
END
GO

-- ===================================================================================
-- 2. XÓA DỮ LIỆU CŨ (NẾU MUỐN INSERT LẠI TỪ ĐẦU)
-- ===================================================================================

-- Uncomment các dòng dưới nếu muốn xóa dữ liệu cũ và insert lại
/*
DELETE FROM lc_subdistrict;
DELETE FROM lc_district;
DELETE FROM lc_province;
GO
*/

-- ===================================================================================
-- 3. INSERT DỮ LIỆU TỈNH/THÀNH PHỐ (63 TỈNH/THÀNH PHỐ ĐẦY ĐỦ)
-- ===================================================================================

-- Kiểm tra xem đã có dữ liệu chưa
IF NOT EXISTS (SELECT TOP 1 * FROM lc_province)
BEGIN
    INSERT INTO lc_province (id, name, shortname, code, country_id, description, created_by, created_date, modified_by, modified_date)
    VALUES
        (1, N'Thành phố Hà Nội', N'HN', '01', 1, NULL, NULL, GETDATE(), NULL, NULL),
        (2, N'Tỉnh Hà Giang', N'HG', '02', 1, NULL, NULL, GETDATE(), NULL, NULL),
        (3, N'Tỉnh Cao Bằng', N'CB', '04', 1, NULL, NULL, GETDATE(), NULL, NULL),
        (4, N'Tỉnh Bắc Kạn', N'BK', '06', 1, NULL, NULL, GETDATE(), NULL, NULL),
        (5, N'Tỉnh Tuyên Quang', N'TQ', '08', 1, NULL, NULL, GETDATE(), NULL, NULL),
        (6, N'Tỉnh Lào Cai', N'LC', '10', 1, NULL, NULL, GETDATE(), NULL, NULL),
        (7, N'Tỉnh Điện Biên', N'ĐB', '11', 1, NULL, NULL, GETDATE(), NULL, NULL),
        (8, N'Tỉnh Lai Châu', N'LCh', '12', 1, NULL, NULL, GETDATE(), NULL, NULL),
        (9, N'Tỉnh Sơn La', N'SL', '14', 1, NULL, NULL, GETDATE(), NULL, NULL),
        (10, N'Tỉnh Yên Bái', N'YB', '15', 1, NULL, NULL, GETDATE(), NULL, NULL),
        (11, N'Tỉnh Hoà Bình', N'HB', '17', 1, NULL, NULL, GETDATE(), NULL, NULL),
        (12, N'Tỉnh Thái Nguyên', N'TN', '19', 1, NULL, NULL, GETDATE(), NULL, NULL),
        (13, N'Tỉnh Lạng Sơn', N'LS', '20', 1, NULL, NULL, GETDATE(), NULL, NULL),
        (14, N'Tỉnh Quảng Ninh', N'QN', '22', 1, NULL, NULL, GETDATE(), NULL, NULL),
        (15, N'Tỉnh Bắc Giang', N'BG', '24', 1, NULL, NULL, GETDATE(), NULL, NULL),
        (16, N'Tỉnh Phú Thọ', N'PT', '25', 1, NULL, NULL, GETDATE(), NULL, NULL),
        (17, N'Tỉnh Vĩnh Phúc', N'VP', '26', 1, NULL, NULL, GETDATE(), NULL, NULL),
        (18, N'Tỉnh Bắc Ninh', N'BN', '27', 1, NULL, NULL, GETDATE(), NULL, NULL),
        (19, N'Tỉnh Hải Dương', N'HD', '30', 1, NULL, NULL, GETDATE(), NULL, NULL),
        (20, N'Thành phố Hải Phòng', N'HP', '31', 1, NULL, NULL, GETDATE(), NULL, NULL),
        (21, N'Tỉnh Hưng Yên', N'HY', '33', 1, NULL, NULL, GETDATE(), NULL, NULL),
        (22, N'Tỉnh Thái Bình', N'TB', '34', 1, NULL, NULL, GETDATE(), NULL, NULL),
        (23, N'Tỉnh Hà Nam', N'HN', '35', 1, NULL, NULL, GETDATE(), NULL, NULL),
        (24, N'Tỉnh Nam Định', N'ND', '36', 1, NULL, NULL, GETDATE(), NULL, NULL),
        (25, N'Tỉnh Ninh Bình', N'NB', '37', 1, NULL, NULL, GETDATE(), NULL, NULL),
        (26, N'Tỉnh Thanh Hóa', N'TH', '38', 1, NULL, NULL, GETDATE(), NULL, NULL),
        (27, N'Tỉnh Nghệ An', N'NA', '40', 1, NULL, NULL, GETDATE(), NULL, NULL),
        (28, N'Tỉnh Hà Tĩnh', N'HT', '42', 1, NULL, NULL, GETDATE(), NULL, NULL),
        (29, N'Tỉnh Quảng Bình', N'QB', '44', 1, NULL, NULL, GETDATE(), NULL, NULL),
        (30, N'Tỉnh Quảng Trị', N'QT', '45', 1, NULL, NULL, GETDATE(), NULL, NULL),
        (31, N'Tỉnh Thừa Thiên Huế', N'TT-H', '46', 1, NULL, NULL, GETDATE(), NULL, NULL),
        (32, N'Thành phố Đà Nẵng', N'ĐN', '48', 1, NULL, NULL, GETDATE(), NULL, NULL),
        (33, N'Tỉnh Quảng Nam', N'QN', '49', 1, NULL, NULL, GETDATE(), NULL, NULL),
        (34, N'Tỉnh Quảng Ngãi', N'QG', '51', 1, NULL, NULL, GETDATE(), NULL, NULL),
        (35, N'Tỉnh Bình Định', N'BĐ', '52', 1, NULL, NULL, GETDATE(), NULL, NULL),
        (36, N'Tỉnh Phú Yên', N'PY', '54', 1, NULL, NULL, GETDATE(), NULL, NULL),
        (37, N'Tỉnh Khánh Hòa', N'KH', '56', 1, NULL, NULL, GETDATE(), NULL, NULL),
        (38, N'Tỉnh Ninh Thuận', N'NT', '58', 1, NULL, NULL, GETDATE(), NULL, NULL),
        (39, N'Tỉnh Bình Thuận', N'BT', '60', 1, NULL, NULL, GETDATE(), NULL, NULL),
        (40, N'Tỉnh Kon Tum', N'KT', '62', 1, NULL, NULL, GETDATE(), NULL, NULL),
        (41, N'Tỉnh Gia Lai', N'GL', '64', 1, NULL, NULL, GETDATE(), NULL, NULL),
        (42, N'Tỉnh Đắk Lắk', N'ĐL', '66', 1, NULL, NULL, GETDATE(), NULL, NULL),
        (43, N'Tỉnh Đắk Nông', N'ĐN', '67', 1, NULL, NULL, GETDATE(), NULL, NULL),
        (44, N'Tỉnh Lâm Đồng', N'LĐ', '68', 1, NULL, NULL, GETDATE(), NULL, NULL),
        (45, N'Tỉnh Bình Phước', N'BP', '70', 1, NULL, NULL, GETDATE(), NULL, NULL),
        (46, N'Tỉnh Tây Ninh', N'TN', '72', 1, NULL, NULL, GETDATE(), NULL, NULL),
        (47, N'Tỉnh Bình Dương', N'BD', '74', 1, NULL, NULL, GETDATE(), NULL, NULL),
        (48, N'Tỉnh Đồng Nai', N'ĐN', '75', 1, NULL, NULL, GETDATE(), NULL, NULL),
        (49, N'Tỉnh Bà Rịa - Vũng Tàu', N'BR-VT', '77', 1, NULL, NULL, GETDATE(), NULL, NULL),
        (50, N'Thành phố Hồ Chí Minh', N'HCM', '79', 1, NULL, NULL, GETDATE(), NULL, NULL),
        (51, N'Tỉnh Long An', N'LA', '80', 1, NULL, NULL, GETDATE(), NULL, NULL),
        (52, N'Tỉnh Tiền Giang', N'TG', '82', 1, NULL, NULL, GETDATE(), NULL, NULL),
        (53, N'Tỉnh Bến Tre', N'BT', '83', 1, NULL, NULL, GETDATE(), NULL, NULL),
        (54, N'Tỉnh Trà Vinh', N'TV', '84', 1, NULL, NULL, GETDATE(), NULL, NULL),
        (55, N'Tỉnh Vĩnh Long', N'VL', '86', 1, NULL, NULL, GETDATE(), NULL, NULL),
        (56, N'Tỉnh Đồng Tháp', N'ĐT', '87', 1, NULL, NULL, GETDATE(), NULL, NULL),
        (57, N'Tỉnh An Giang', N'AG', '89', 1, NULL, NULL, GETDATE(), NULL, NULL),
        (58, N'Tỉnh Kiên Giang', N'KG', '91', 1, NULL, NULL, GETDATE(), NULL, NULL),
        (59, N'Thành phố Cần Thơ', N'CT', '92', 1, NULL, NULL, GETDATE(), NULL, NULL),
        (60, N'Tỉnh Hậu Giang', N'HG', '93', 1, NULL, NULL, GETDATE(), NULL, NULL),
        (61, N'Tỉnh Sóc Trăng', N'ST', '94', 1, NULL, NULL, GETDATE(), NULL, NULL),
        (62, N'Tỉnh Bạc Liêu', N'BL', '95', 1, NULL, NULL, GETDATE(), NULL, NULL),
        (63, N'Tỉnh Cà Mau', N'CM', '96', 1, NULL, NULL, GETDATE(), NULL, NULL);
    
    PRINT 'Đã insert ' + CAST(@@ROWCOUNT AS VARCHAR) + ' tỉnh/thành phố';
END
ELSE
BEGIN
    PRINT 'Dữ liệu tỉnh/thành phố đã tồn tại, bỏ qua insert';
END
GO

-- ===================================================================================
-- 4. INSERT DỮ LIỆU HUYỆN/QUẬN
-- ===================================================================================
-- LƯU Ý: Dữ liệu huyện/quận rất nhiều (khoảng 700+ huyện/quận)
-- Bạn có thể:
--   1. Tải file CSV từ nguồn chính thức (Bộ Tài nguyên và Môi trường)
--   2. Sử dụng API công khai
--   3. Import từ file Excel/CSV bằng BULK INSERT
-- ===================================================================================

-- Ví dụ một số huyện/quận của Hà Nội (bạn có thể thêm tiếp)
IF NOT EXISTS (SELECT TOP 1 * FROM lc_district WHERE province_id = 1)
BEGIN
    INSERT INTO lc_district (id, province_id, province_code, name, shortname, code, description, created_by, created_date, modified_by, modified_date)
    VALUES
        (1, 1, '01', N'Quận Ba Đình', N'Q.BĐ', '001', NULL, NULL, GETDATE(), NULL, NULL),
        (2, 1, '01', N'Quận Hoàn Kiếm', N'Q.HK', '002', NULL, NULL, GETDATE(), NULL, NULL),
        (3, 1, '01', N'Quận Tây Hồ', N'Q.TH', '003', NULL, NULL, GETDATE(), NULL, NULL),
        (4, 1, '01', N'Quận Long Biên', N'Q.LB', '004', NULL, NULL, GETDATE(), NULL, NULL),
        (5, 1, '01', N'Quận Cầu Giấy', N'Q.CG', '005', NULL, NULL, GETDATE(), NULL, NULL),
        (6, 1, '01', N'Quận Đống Đa', N'Q.ĐĐ', '006', NULL, NULL, GETDATE(), NULL, NULL),
        (7, 1, '01', N'Quận Hai Bà Trưng', N'Q.HBT', '007', NULL, NULL, GETDATE(), NULL, NULL),
        (8, 1, '01', N'Quận Hoàng Mai', N'Q.HM', '008', NULL, NULL, GETDATE(), NULL, NULL),
        (9, 1, '01', N'Quận Thanh Xuân', N'Q.TX', '009', NULL, NULL, GETDATE(), NULL, NULL),
        (10, 1, '01', N'Huyện Sóc Sơn', N'H.SS', '016', NULL, NULL, GETDATE(), NULL, NULL),
        (11, 1, '01', N'Huyện Đông Anh', N'H.ĐA', '017', NULL, NULL, GETDATE(), NULL, NULL),
        (12, 1, '01', N'Huyện Gia Lâm', N'H.GL', '018', NULL, NULL, GETDATE(), NULL, NULL),
        (13, 1, '01', N'Huyện Nam Từ Liêm', N'H.NTL', '019', NULL, NULL, GETDATE(), NULL, NULL),
        (14, 1, '01', N'Huyện Thanh Trì', N'H.TT', '020', NULL, NULL, GETDATE(), NULL, NULL),
        (15, 1, '01', N'Huyện Bắc Từ Liêm', N'H.BTL', '021', NULL, NULL, GETDATE(), NULL, NULL),
        (16, 1, '01', N'Huyện Mê Linh', N'H.ML', '250', NULL, NULL, GETDATE(), NULL, NULL),
        (17, 1, '01', N'Huyện Hà Đông', N'H.HĐ', '268', NULL, NULL, GETDATE(), NULL, NULL),
        (18, 1, '01', N'Thị xã Sơn Tây', N'TX.ST', '269', NULL, NULL, GETDATE(), NULL, NULL),
        (19, 1, '01', N'Huyện Ba Vì', N'H.BV', '271', NULL, NULL, GETDATE(), NULL, NULL),
        (20, 1, '01', N'Huyện Phúc Thọ', N'H.PT', '272', NULL, NULL, GETDATE(), NULL, NULL),
        (21, 1, '01', N'Huyện Đan Phượng', N'H.ĐP', '273', NULL, NULL, GETDATE(), NULL, NULL),
        (22, 1, '01', N'Huyện Hoài Đức', N'H.HĐ', '274', NULL, NULL, GETDATE(), NULL, NULL),
        (23, 1, '01', N'Huyện Quốc Oai', N'H.QO', '275', NULL, NULL, GETDATE(), NULL, NULL),
        (24, 1, '01', N'Huyện Thạch Thất', N'H.TT', '276', NULL, NULL, GETDATE(), NULL, NULL),
        (25, 1, '01', N'Huyện Chương Mỹ', N'H.CM', '277', NULL, NULL, GETDATE(), NULL, NULL),
        (26, 1, '01', N'Huyện Thanh Oai', N'H.TO', '278', NULL, NULL, GETDATE(), NULL, NULL),
        (27, 1, '01', N'Huyện Thường Tín', N'H.TT', '279', NULL, NULL, GETDATE(), NULL, NULL),
        (28, 1, '01', N'Huyện Phú Xuyên', N'H.PX', '280', NULL, NULL, GETDATE(), NULL, NULL),
        (29, 1, '01', N'Huyện Ứng Hòa', N'H.ỨH', '281', NULL, NULL, GETDATE(), NULL, NULL),
        (30, 1, '01', N'Huyện Mỹ Đức', N'H.MĐ', '282', NULL, NULL, GETDATE(), NULL, NULL);
    
    PRINT 'Đã insert ' + CAST(@@ROWCOUNT AS VARCHAR) + ' huyện/quận cho Hà Nội';
END
ELSE
BEGIN
    PRINT 'Dữ liệu huyện/quận đã tồn tại, bỏ qua insert';
END
GO

-- ===================================================================================
-- 5. HƯỚNG DẪN IMPORT DỮ LIỆU ĐẦY ĐỦ
-- ===================================================================================

/*
CÁCH 1: Sử dụng BULK INSERT từ file CSV
----------------------------------------
1. Tải file CSV dữ liệu địa lý từ nguồn chính thức
2. Lưu file vào thư mục SQL Server có quyền đọc (ví dụ: C:\temp\districts.csv)
3. Chạy lệnh:

BULK INSERT lc_district
FROM 'C:\temp\districts.csv'
WITH (
    FIELDTERMINATOR = ',',
    ROWTERMINATOR = '\n',
    FIRSTROW = 2,
    CODEPAGE = '65001'  -- UTF-8
);
GO

CÁCH 2: Sử dụng Python/Node.js script để generate SQL
-------------------------------------------------------
1. Tải dữ liệu từ API hoặc file JSON/CSV
2. Tạo script Python/Node.js để đọc và generate file SQL
3. Chạy file SQL được generate

CÁCH 3: Sử dụng API công khai
------------------------------
Có một số API miễn phí cung cấp dữ liệu địa lý Việt Nam:
- https://provinces.open-api.vn/
- https://api.mysupership.vn/v1/partner/areas/province

Bạn có thể tạo script để fetch và insert vào database.
*/

PRINT '===================================================================================';
PRINT 'HOÀN THÀNH: Đã tạo bảng và insert dữ liệu mẫu';
PRINT 'LƯU Ý: Để có dữ liệu đầy đủ, bạn cần:';
PRINT '  1. Tải dữ liệu từ nguồn chính thức';
PRINT '  2. Hoặc sử dụng BULK INSERT từ file CSV';
PRINT '  3. Hoặc tạo script để import từ API';
PRINT '===================================================================================';
GO


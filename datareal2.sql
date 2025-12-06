-- ===================================================================================
-- SCRIPT BỔ SUNG DỮ LIỆU THẬT (DATA REAL SUPPLEMENT)
-- Mục đích: Thêm SP031-SP040 và Cập nhật Full hình ảnh cho SP001-SP040
-- Chạy sau file tạo DB ban đầu.
-- ===================================================================================

USE QuanLyBanHangLaptop_TheoERD1_New;
GO

PRINT '>> Bat dau cap nhat du lieu bo sung...';

-- ===================================================================================
-- 1. BỔ SUNG THÔNG SỐ KỸ THUẬT (CPU, RAM, GPU, SSD MỚI)
-- ===================================================================================

-- CPU
IF NOT EXISTS (SELECT 1 FROM cpu WHERE ma_cpu = 'CPU008') 
    INSERT INTO cpu (ma_cpu, ten_cpu, mo_ta, trang_thai) VALUES ('CPU008', N'Intel Core 7 Raptor Lake - 150U', N'Bộ xử lý Intel Core 7 150U', 1);
IF NOT EXISTS (SELECT 1 FROM cpu WHERE ma_cpu = 'CPU009') 
    INSERT INTO cpu (ma_cpu, ten_cpu, mo_ta, trang_thai) VALUES ('CPU009', N'Intel Core 7 Raptor Lake - 240H', N'Bộ xử lý Intel Core 7 240H', 1);
IF NOT EXISTS (SELECT 1 FROM cpu WHERE ma_cpu = 'CPU010') 
    INSERT INTO cpu (ma_cpu, ten_cpu, mo_ta, trang_thai) VALUES ('CPU010', N'Intel Core i7 Alder Lake', N'Bộ xử lý Intel Core i7 12th Gen', 1);

-- RAM
IF NOT EXISTS (SELECT 1 FROM ram WHERE ma_ram = 'RAM006') 
    INSERT INTO ram (ma_ram, ten_ram, mo_ta, trang_thai) VALUES ('RAM006', N'DDR4 16GB 2600MHz', N'RAM 16GB Bus 2600', 1);
IF NOT EXISTS (SELECT 1 FROM ram WHERE ma_ram = 'RAM007') 
    INSERT INTO ram (ma_ram, ten_ram, mo_ta, trang_thai) VALUES ('RAM007', N'DDR4 16GB 2660MHz', N'RAM 16GB Bus 2660', 1);
IF NOT EXISTS (SELECT 1 FROM ram WHERE ma_ram = 'RAM008') 
    INSERT INTO ram (ma_ram, ten_ram, mo_ta, trang_thai) VALUES ('RAM008', N'DDR5 16GB 4400MHz', N'RAM 16GB Bus 4400', 1);
IF NOT EXISTS (SELECT 1 FROM ram WHERE ma_ram = 'RAM009') 
    INSERT INTO ram (ma_ram, ten_ram, mo_ta, trang_thai) VALUES ('RAM009', N'DDR4 8GB 2660MHz', N'RAM 8GB Bus 2660', 1);

-- GPU
IF NOT EXISTS (SELECT 1 FROM gpu WHERE ma_gpu = 'GPU004') 
    INSERT INTO gpu (ma_gpu, ten_gpu, mo_ta, trang_thai) VALUES ('GPU004', N'RTX 5050 8GB', N'Card rời RTX 5050', 1);

-- SSD
IF NOT EXISTS (SELECT 1 FROM o_cung WHERE ma_o_cung = 'SSD007') 
    INSERT INTO o_cung (ma_o_cung, dung_luong, mo_ta, trang_thai) VALUES ('SSD007', N'512GB SSD NVMe PCIe 4.0', N'SSD 512GB PCIe 4.0', 1);
IF NOT EXISTS (SELECT 1 FROM o_cung WHERE ma_o_cung = 'SSD008') 
    INSERT INTO o_cung (ma_o_cung, dung_luong, mo_ta, trang_thai) VALUES ('SSD008', N'512GB SSD M.2 PCIe Gen 4', N'SSD 512GB Gen 4', 1);
IF NOT EXISTS (SELECT 1 FROM o_cung WHERE ma_o_cung = 'SSD009') 
    INSERT INTO o_cung (ma_o_cung, dung_luong, mo_ta, trang_thai) VALUES ('SSD009', N'1TB SSD M.2 PCIe Gen 4', N'SSD 1TB Gen 4', 1);
IF NOT EXISTS (SELECT 1 FROM o_cung WHERE ma_o_cung = 'SSD010') 
    INSERT INTO o_cung (ma_o_cung, dung_luong, mo_ta, trang_thai) VALUES ('SSD010', N'1TB SSD NVMe PCIe Gen 4', N'SSD 1TB NVMe Gen 4', 1);

PRINT '>> Da them thong so ky thuat moi.';
GO

-- ===================================================================================
-- 2. THÊM MỚI SẢN PHẨM TỪ SP031 ĐẾN SP040 (NẾU CHƯA CÓ)
-- ===================================================================================

-- SP031
IF NOT EXISTS (SELECT 1 FROM san_pham WHERE ma_san_pham = 'SP031')
BEGIN
    INSERT INTO san_pham (ma_san_pham, ten_san_pham, mo_ta, slug, trang_thai, ngay_tao, ngay_sua) 
    VALUES ('SP031', N'Laptop Dell Inspiron 15 3520 - 71053682', N'Laptop Dell Inspiron 15 3520 (i7 1255U, 16GB, 512GB)', 'dell-inspiron-15-3520-71053682', 1, GETDATE(), GETDATE());

    INSERT INTO chi_tiet_san_pham (ma_ctsp, sp_id, cpu_id, ram_id, o_cung_id, gpu_id, loai_man_hinh_id, pin_id, mau_sac_id, gia_ban, so_luong_ton, trang_thai, ngay_tao, ngay_sua)
    VALUES ('DELL-INS-3520-71053682', (SELECT id FROM san_pham WHERE ma_san_pham = 'SP031'), 
    (SELECT TOP 1 id FROM cpu WHERE ma_cpu = 'CPU010'), (SELECT TOP 1 id FROM ram WHERE ma_ram = 'RAM002'), (SELECT TOP 1 id FROM o_cung WHERE ma_o_cung = 'SSD001'), (SELECT TOP 1 id FROM gpu WHERE ma_gpu = 'GPU001'), (SELECT TOP 1 id FROM loai_man_hinh WHERE ma_loai_man_hinh = 'MH001'), (SELECT TOP 1 id FROM pin WHERE ma_pin = 'PIN002'), (SELECT TOP 1 id FROM mau_sac WHERE ma_mau = 'MS001'), 
    18990000, 50, 1, GETDATE(), GETDATE());
END

-- SP032
IF NOT EXISTS (SELECT 1 FROM san_pham WHERE ma_san_pham = 'SP032')
BEGIN
    INSERT INTO san_pham (ma_san_pham, ten_san_pham, mo_ta, slug, trang_thai, ngay_tao, ngay_sua) 
    VALUES ('SP032', N'Laptop Dell Inspiron 15 3530 - N3530-i7U161W11SLU-BL', N'Laptop Dell Inspiron 15 3530 i7 1TB', 'dell-inspiron-15-3530-n3530-i7-bl', 1, GETDATE(), GETDATE());

    INSERT INTO chi_tiet_san_pham (ma_ctsp, sp_id, cpu_id, ram_id, o_cung_id, gpu_id, loai_man_hinh_id, pin_id, mau_sac_id, gia_ban, so_luong_ton, trang_thai, ngay_tao, ngay_sua)
    VALUES ('DELL-INS-3530-N3530-BL', (SELECT id FROM san_pham WHERE ma_san_pham = 'SP032'), 
    (SELECT TOP 1 id FROM cpu WHERE ma_cpu = 'CPU004'), (SELECT TOP 1 id FROM ram WHERE ma_ram = 'RAM002'), (SELECT TOP 1 id FROM o_cung WHERE ma_o_cung = 'SSD004'), (SELECT TOP 1 id FROM gpu WHERE ma_gpu = 'GPU001'), (SELECT TOP 1 id FROM loai_man_hinh WHERE ma_loai_man_hinh = 'MH001'), (SELECT TOP 1 id FROM pin WHERE ma_pin = 'PIN002'), (SELECT TOP 1 id FROM mau_sac WHERE ma_mau = 'MS001'), 
    21990000, 50, 1, GETDATE(), GETDATE());
END

-- SP033
IF NOT EXISTS (SELECT 1 FROM san_pham WHERE ma_san_pham = 'SP033')
BEGIN
    INSERT INTO san_pham (ma_san_pham, ten_san_pham, mo_ta, slug, trang_thai, ngay_tao, ngay_sua) 
    VALUES ('SP033', N'Laptop Dell 15 DC15250 - DC15250-i7U161W11SLU', N'Laptop Dell 15 DC15250 i7 1TB', 'dell-15-dc15250-i7-1tb', 1, GETDATE(), GETDATE());

    INSERT INTO chi_tiet_san_pham (ma_ctsp, sp_id, cpu_id, ram_id, o_cung_id, gpu_id, loai_man_hinh_id, pin_id, mau_sac_id, gia_ban, so_luong_ton, trang_thai, ngay_tao, ngay_sua)
    VALUES ('DELL-15-DC15250-I7', (SELECT id FROM san_pham WHERE ma_san_pham = 'SP033'), 
    (SELECT TOP 1 id FROM cpu WHERE ma_cpu = 'CPU004'), (SELECT TOP 1 id FROM ram WHERE ma_ram = 'RAM002'), (SELECT TOP 1 id FROM o_cung WHERE ma_o_cung = 'SSD004'), (SELECT TOP 1 id FROM gpu WHERE ma_gpu = 'GPU002'), (SELECT TOP 1 id FROM loai_man_hinh WHERE ma_loai_man_hinh = 'MH001'), (SELECT TOP 1 id FROM pin WHERE ma_pin = 'PIN002'), (SELECT TOP 1 id FROM mau_sac WHERE ma_mau = 'MS001'), 
    21990000, 50, 1, GETDATE(), GETDATE());
END

-- SP034
IF NOT EXISTS (SELECT 1 FROM san_pham WHERE ma_san_pham = 'SP034')
BEGIN
    INSERT INTO san_pham (ma_san_pham, ten_san_pham, mo_ta, slug, trang_thai, ngay_tao, ngay_sua) 
    VALUES ('SP034', N'Laptop Dell Inspiron 15 3520 - 71054775', N'Laptop Dell Inspiron 15 3520 i5 16GB', 'dell-inspiron-15-3520-71054775', 1, GETDATE(), GETDATE());

    INSERT INTO chi_tiet_san_pham (ma_ctsp, sp_id, cpu_id, ram_id, o_cung_id, gpu_id, loai_man_hinh_id, pin_id, mau_sac_id, gia_ban, so_luong_ton, trang_thai, ngay_tao, ngay_sua)
    VALUES ('DELL-INS-3520-71054775', (SELECT id FROM san_pham WHERE ma_san_pham = 'SP034'), 
    (SELECT TOP 1 id FROM cpu WHERE ma_cpu = 'CPU006'), (SELECT TOP 1 id FROM ram WHERE ma_ram = 'RAM006'), (SELECT TOP 1 id FROM o_cung WHERE ma_o_cung = 'SSD001'), (SELECT TOP 1 id FROM gpu WHERE ma_gpu = 'GPU001'), (SELECT TOP 1 id FROM loai_man_hinh WHERE ma_loai_man_hinh = 'MH001'), (SELECT TOP 1 id FROM pin WHERE ma_pin = 'PIN002'), (SELECT TOP 1 id FROM mau_sac WHERE ma_mau = 'MS001'), 
    15990000, 50, 1, GETDATE(), GETDATE());
END

-- SP035
IF NOT EXISTS (SELECT 1 FROM san_pham WHERE ma_san_pham = 'SP035')
BEGIN
    INSERT INTO san_pham (ma_san_pham, ten_san_pham, mo_ta, slug, trang_thai, ngay_tao, ngay_sua) 
    VALUES ('SP035', N'Laptop Dell Inspiron 15 3520 - N5I7125W1', N'Laptop Dell Inspiron 15 3520 i7 16GB', 'dell-inspiron-15-3520-n5i7125w1', 1, GETDATE(), GETDATE());

    INSERT INTO chi_tiet_san_pham (ma_ctsp, sp_id, cpu_id, ram_id, o_cung_id, gpu_id, loai_man_hinh_id, pin_id, mau_sac_id, gia_ban, so_luong_ton, trang_thai, ngay_tao, ngay_sua)
    VALUES ('DELL-INS-3520-N5I7125W1', (SELECT id FROM san_pham WHERE ma_san_pham = 'SP035'), 
    (SELECT TOP 1 id FROM cpu WHERE ma_cpu = 'CPU010'), (SELECT TOP 1 id FROM ram WHERE ma_ram = 'RAM002'), (SELECT TOP 1 id FROM o_cung WHERE ma_o_cung = 'SSD001'), (SELECT TOP 1 id FROM gpu WHERE ma_gpu = 'GPU001'), (SELECT TOP 1 id FROM loai_man_hinh WHERE ma_loai_man_hinh = 'MH001'), (SELECT TOP 1 id FROM pin WHERE ma_pin = 'PIN002'), (SELECT TOP 1 id FROM mau_sac WHERE ma_mau = 'MS001'), 
    18990000, 50, 1, GETDATE(), GETDATE());
END

-- SP036
IF NOT EXISTS (SELECT 1 FROM san_pham WHERE ma_san_pham = 'SP036')
BEGIN
    INSERT INTO san_pham (ma_san_pham, ten_san_pham, mo_ta, slug, trang_thai, ngay_tao, ngay_sua) 
    VALUES ('SP036', N'Laptop Dell Inspiron 15 3520 - 71069748', N'Laptop Dell Inspiron 15 3520 i7 16GB', 'dell-inspiron-15-3520-71069748', 1, GETDATE(), GETDATE());

    INSERT INTO chi_tiet_san_pham (ma_ctsp, sp_id, cpu_id, ram_id, o_cung_id, gpu_id, loai_man_hinh_id, pin_id, mau_sac_id, gia_ban, so_luong_ton, trang_thai, ngay_tao, ngay_sua)
    VALUES ('DELL-INS-3520-71069748', (SELECT id FROM san_pham WHERE ma_san_pham = 'SP036'), 
    (SELECT TOP 1 id FROM cpu WHERE ma_cpu = 'CPU010'), (SELECT TOP 1 id FROM ram WHERE ma_ram = 'RAM002'), (SELECT TOP 1 id FROM o_cung WHERE ma_o_cung = 'SSD001'), (SELECT TOP 1 id FROM gpu WHERE ma_gpu = 'GPU001'), (SELECT TOP 1 id FROM loai_man_hinh WHERE ma_loai_man_hinh = 'MH001'), (SELECT TOP 1 id FROM pin WHERE ma_pin = 'PIN002'), (SELECT TOP 1 id FROM mau_sac WHERE ma_mau = 'MS001'), 
    18990000, 50, 1, GETDATE(), GETDATE());
END

-- SP037
IF NOT EXISTS (SELECT 1 FROM san_pham WHERE ma_san_pham = 'SP037')
BEGIN
    INSERT INTO san_pham (ma_san_pham, ten_san_pham, mo_ta, slug, trang_thai, ngay_tao, ngay_sua) 
    VALUES ('SP037', N'Laptop Dell Inspiron 7440 - N7440-C7U161W11IBU', N'Laptop Dell Inspiron 7440 Core 7 1TB Cảm ứng', 'dell-inspiron-7440-n7440', 1, GETDATE(), GETDATE());

    INSERT INTO chi_tiet_san_pham (ma_ctsp, sp_id, cpu_id, ram_id, o_cung_id, gpu_id, loai_man_hinh_id, pin_id, mau_sac_id, gia_ban, so_luong_ton, trang_thai, ngay_tao, ngay_sua)
    VALUES ('DELL-INS-7440-V1', (SELECT id FROM san_pham WHERE ma_san_pham = 'SP037'), 
    (SELECT TOP 1 id FROM cpu WHERE ma_cpu = 'CPU008'), (SELECT TOP 1 id FROM ram WHERE ma_ram = 'RAM004'), (SELECT TOP 1 id FROM o_cung WHERE ma_o_cung = 'SSD004'), (SELECT TOP 1 id FROM gpu WHERE ma_gpu = 'GPU002'), (SELECT TOP 1 id FROM loai_man_hinh WHERE ma_loai_man_hinh = 'MH001'), (SELECT TOP 1 id FROM pin WHERE ma_pin = 'PIN002'), (SELECT TOP 1 id FROM mau_sac WHERE ma_mau = 'MS001'), 
    29890000, 50, 1, GETDATE(), GETDATE());
END

-- SP038
IF NOT EXISTS (SELECT 1 FROM san_pham WHERE ma_san_pham = 'SP038')
BEGIN
    INSERT INTO san_pham (ma_san_pham, ten_san_pham, mo_ta, slug, trang_thai, ngay_tao, ngay_sua) 
    VALUES ('SP038', N'Laptop Dell Latitude 3450 - 71058806', N'Laptop Dell Latitude 3450 i7 16GB', 'dell-latitude-3450-71058806', 1, GETDATE(), GETDATE());

    INSERT INTO chi_tiet_san_pham (ma_ctsp, sp_id, cpu_id, ram_id, o_cung_id, gpu_id, loai_man_hinh_id, pin_id, mau_sac_id, gia_ban, so_luong_ton, trang_thai, ngay_tao, ngay_sua)
    VALUES ('DELL-LAT-3450-71058806', (SELECT id FROM san_pham WHERE ma_san_pham = 'SP038'), 
    (SELECT TOP 1 id FROM cpu WHERE ma_cpu = 'CPU004'), (SELECT TOP 1 id FROM ram WHERE ma_ram = 'RAM005'), (SELECT TOP 1 id FROM o_cung WHERE ma_o_cung = 'SSD001'), (SELECT TOP 1 id FROM gpu WHERE ma_gpu = 'GPU002'), (SELECT TOP 1 id FROM loai_man_hinh WHERE ma_loai_man_hinh = 'MH001'), (SELECT TOP 1 id FROM pin WHERE ma_pin = 'PIN002'), (SELECT TOP 1 id FROM mau_sac WHERE ma_mau = 'MS001'), 
    24490000, 50, 1, GETDATE(), GETDATE());
END

-- SP039
IF NOT EXISTS (SELECT 1 FROM san_pham WHERE ma_san_pham = 'SP039')
BEGIN
    INSERT INTO san_pham (ma_san_pham, ten_san_pham, mo_ta, slug, trang_thai, ngay_tao, ngay_sua) 
    VALUES ('SP039', N'Laptop Dell Inspiron 15 3530 - N3530-i5U165W11SLU', N'Laptop Dell Inspiron 15 3530 i5 16GB', 'dell-inspiron-15-3530-n3530-i5u165', 1, GETDATE(), GETDATE());

    INSERT INTO chi_tiet_san_pham (ma_ctsp, sp_id, cpu_id, ram_id, o_cung_id, gpu_id, loai_man_hinh_id, pin_id, mau_sac_id, gia_ban, so_luong_ton, trang_thai, ngay_tao, ngay_sua)
    VALUES ('DELL-INS-3530-N3530-V2', (SELECT id FROM san_pham WHERE ma_san_pham = 'SP039'), 
    (SELECT TOP 1 id FROM cpu WHERE ma_cpu = 'CPU002'), (SELECT TOP 1 id FROM ram WHERE ma_ram = 'RAM002'), (SELECT TOP 1 id FROM o_cung WHERE ma_o_cung = 'SSD001'), (SELECT TOP 1 id FROM gpu WHERE ma_gpu = 'GPU001'), (SELECT TOP 1 id FROM loai_man_hinh WHERE ma_loai_man_hinh = 'MH001'), (SELECT TOP 1 id FROM pin WHERE ma_pin = 'PIN002'), (SELECT TOP 1 id FROM mau_sac WHERE ma_mau = 'MS001'), 
    17490000, 50, 1, GETDATE(), GETDATE());
END

-- SP040
IF NOT EXISTS (SELECT 1 FROM san_pham WHERE ma_san_pham = 'SP040')
BEGIN
    INSERT INTO san_pham (ma_san_pham, ten_san_pham, mo_ta, slug, trang_thai, ngay_tao, ngay_sua) 
    VALUES ('SP040', N'Laptop Dell Gaming Alienware 16 Aurora AC16250 - C7H161W11II5050', N'Laptop Alienware Core 7 RTX 5050', 'dell-alienware-16-c7h161', 1, GETDATE(), GETDATE());

    INSERT INTO chi_tiet_san_pham (ma_ctsp, sp_id, cpu_id, ram_id, o_cung_id, gpu_id, loai_man_hinh_id, pin_id, mau_sac_id, gia_ban, so_luong_ton, trang_thai, ngay_tao, ngay_sua)
    VALUES ('DELL-ALIEN-AC16250-V2', (SELECT id FROM san_pham WHERE ma_san_pham = 'SP040'), 
    (SELECT TOP 1 id FROM cpu WHERE ma_cpu = 'CPU009'), (SELECT TOP 1 id FROM ram WHERE ma_ram = 'RAM005'), (SELECT TOP 1 id FROM o_cung WHERE ma_o_cung = 'SSD004'), (SELECT TOP 1 id FROM gpu WHERE ma_gpu = 'GPU004'), (SELECT TOP 1 id FROM loai_man_hinh WHERE ma_loai_man_hinh = 'MH002'), (SELECT TOP 1 id FROM pin WHERE ma_pin = 'PIN002'), (SELECT TOP 1 id FROM mau_sac WHERE ma_mau = 'MS001'), 
    36490000, 50, 1, GETDATE(), GETDATE());
END

PRINT '>> Da them 10 san pham moi (SP031-SP040).';
GO

-- ===================================================================================
-- 3. CẬP NHẬT HÌNH ẢNH (XÓA CŨ - THÊM MỚI CHO SP001 - SP040)
-- ===================================================================================

-- Xóa ảnh cũ của các sản phẩm này để tránh trùng lặp hoặc ảnh rác
DELETE FROM hinh_anh 
WHERE id_spct IN (
    SELECT id FROM chi_tiet_san_pham 
    WHERE sp_id IN (
        SELECT id FROM san_pham 
        WHERE ma_san_pham BETWEEN 'SP001' AND 'SP040'
    )
);
PRINT '>> Da xoa anh cu cua SP001-SP040.';
GO

-- INSERT ẢNH MỚI (Dữ liệu chuẩn từ file của bạn)
-- Logic: Tìm id_spct dựa vào mã sản phẩm (SP0xx) để đảm bảo chính xác.

-- SP001
INSERT INTO hinh_anh (id_spct, url, anh_chinh_dai_dien)
SELECT id, 'https://cdnv2.tgdd.vn/mwg-static/tgdd/Products/Images/44/334803/dell-inspiron-15-3530-i5-n5i5530w1-thumb-638762534676491196-600x600.jpg', 1 
FROM chi_tiet_san_pham WHERE sp_id = (SELECT id FROM san_pham WHERE ma_san_pham = 'SP001');

INSERT INTO hinh_anh (id_spct, url, anh_chinh_dai_dien)
SELECT id, 'https://cdn.tgdd.vn/Products/Images/44/334803/Slider/vi-vn-dell-inspiron-15-3530-i5-n5i5530w1-slider-1.jpg', 0
FROM chi_tiet_san_pham WHERE sp_id = (SELECT id FROM san_pham WHERE ma_san_pham = 'SP001');

-- SP002
INSERT INTO hinh_anh (id_spct, url, anh_chinh_dai_dien)
SELECT id, 'https://cdnv2.tgdd.vn/mwg-static/tgdd/Products/Images/44/340562/dell-15-dc15250-i7-dc5i7748w1-638900114799182560-600x600.jpg', 1
FROM chi_tiet_san_pham WHERE sp_id = (SELECT id FROM san_pham WHERE ma_san_pham = 'SP002');

-- SP003
INSERT INTO hinh_anh (id_spct, url, anh_chinh_dai_dien)
SELECT id, 'https://cdnv2.tgdd.vn/mwg-static/tgdd/Products/Images/44/340560/dell-inspiron-15-3530-i5-71070372-638900114510203065-600x600.jpg', 1
FROM chi_tiet_san_pham WHERE sp_id = (SELECT id FROM san_pham WHERE ma_san_pham = 'SP003');

-- SP004
INSERT INTO hinh_anh (id_spct, url, anh_chinh_dai_dien)
SELECT id, 'https://cdnv2.tgdd.vn/mwg-static/tgdd/Products/Images/44/351612/dell-inspiron-15-3530-i3-1305u-n3530-i3u085w11slu-thumb-638937927534283365-600x600.jpg', 1
FROM chi_tiet_san_pham WHERE sp_id = (SELECT id FROM san_pham WHERE ma_san_pham = 'SP004');

-- SP005
INSERT INTO hinh_anh (id_spct, url, anh_chinh_dai_dien)
SELECT id, 'https://cdnv2.tgdd.vn/mwg-static/tgdd/Products/Images/44/342755/dell-15-dc15255-r5-7530u-dc5r5802w1-thumb-638920698565049808-600x600.jpg', 1
FROM chi_tiet_san_pham WHERE sp_id = (SELECT id FROM san_pham WHERE ma_san_pham = 'SP005');

-- SP006
INSERT INTO hinh_anh (id_spct, url, anh_chinh_dai_dien)
SELECT id, 'https://cdnv2.tgdd.vn/mwg-static/tgdd/Products/Images/44/340561/dell-15-dc15250-i5-dc5i5357w1-638900114676211077-600x600.jpg', 1
FROM chi_tiet_san_pham WHERE sp_id = (SELECT id FROM san_pham WHERE ma_san_pham = 'SP006');

-- SP007
INSERT INTO hinh_anh (id_spct, url, anh_chinh_dai_dien)
SELECT id, 'https://cdnv2.tgdd.vn/mwg-static/tgdd/Products/Images/44/339681/dell-inspiron-15-3530-i5-p16wd22-638895760085626264-600x600.jpg', 1
FROM chi_tiet_san_pham WHERE sp_id = (SELECT id FROM san_pham WHERE ma_san_pham = 'SP007');

-- SP008
INSERT INTO hinh_anh (id_spct, url, anh_chinh_dai_dien)
SELECT id, 'https://cdnv2.tgdd.vn/mwg-static/tgdd/Products/Images/44/339679/dell-latitude-3450-i5-l34501335u16512wn-638895751182170399-600x600.jpg', 1
FROM chi_tiet_san_pham WHERE sp_id = (SELECT id FROM san_pham WHERE ma_san_pham = 'SP008');

-- SP009
INSERT INTO hinh_anh (id_spct, url, anh_chinh_dai_dien)
SELECT id, 'https://cdnv2.tgdd.vn/mwg-static/tgdd/Products/Images/44/340558/dell-inspiron-15-3530-i5-n3530-i5u165w11slu-hs24-638900112743874982-600x600.jpg', 1
FROM chi_tiet_san_pham WHERE sp_id = (SELECT id FROM san_pham WHERE ma_san_pham = 'SP009');

-- SP010
INSERT INTO hinh_anh (id_spct, url, anh_chinh_dai_dien)
SELECT id, 'https://cdnv2.tgdd.vn/mwg-static/tgdd/Products/Images/44/338316/dell-inspiron-15-3530-i5-n3530-i5u165w11slu-bl-thumb-638840243364698500-600x600.jpg', 1
FROM chi_tiet_san_pham WHERE sp_id = (SELECT id FROM san_pham WHERE ma_san_pham = 'SP010');

-- SP011
INSERT INTO hinh_anh (id_spct, url, anh_chinh_dai_dien)
SELECT id, 'https://cdnv2.tgdd.vn/mwg-static/tgdd/Products/Images/44/342469/dell-inspiron-15-3530-i7-1355u-n5i7421w1-thumb-2-638981083623791430-600x600.jpg', 1
FROM chi_tiet_san_pham WHERE sp_id = (SELECT id FROM san_pham WHERE ma_san_pham = 'SP011');

-- SP012
INSERT INTO hinh_anh (id_spct, url, anh_chinh_dai_dien)
SELECT id, 'https://cdnv2.tgdd.vn/mwg-static/tgdd/Products/Images/44/342756/dell-inspiron-15-3530-i5-1334u-p16wd21-thumb-2-638981088249405986-600x600.jpg', 1
FROM chi_tiet_san_pham WHERE sp_id = (SELECT id FROM san_pham WHERE ma_san_pham = 'SP012');

-- SP013
INSERT INTO hinh_anh (id_spct, url, anh_chinh_dai_dien)
SELECT id, 'https://cdnv2.tgdd.vn/mwg-static/tgdd/Products/Images/44/357991/dell-15-dc15250-i7-1355u-cph997-thumb-2-638965530519880244-600x600.jpg', 1
FROM chi_tiet_san_pham WHERE sp_id = (SELECT id FROM san_pham WHERE ma_san_pham = 'SP013');

-- SP014
INSERT INTO hinh_anh (id_spct, url, anh_chinh_dai_dien)
SELECT id, 'https://cdnv2.tgdd.vn/mwg-static/tgdd/Products/Images/44/338311/dell-inspiron-15-3530-i7-n5i7216w1-thumb-638840241952547194-600x600.jpg', 1
FROM chi_tiet_san_pham WHERE sp_id = (SELECT id FROM san_pham WHERE ma_san_pham = 'SP014');

-- SP015
INSERT INTO hinh_anh (id_spct, url, anh_chinh_dai_dien)
SELECT id, 'https://cdnv2.tgdd.vn/mwg-static/tgdd/Products/Images/44/321192/dell-inspiron-15-3520-i5-25p231-thumb-638754902669914908-600x600.jpg', 1
FROM chi_tiet_san_pham WHERE sp_id = (SELECT id FROM san_pham WHERE ma_san_pham = 'SP015');

-- SP016
INSERT INTO hinh_anh (id_spct, url, anh_chinh_dai_dien)
SELECT id, 'https://cdnv2.tgdd.vn/mwg-static/tgdd/Products/Images/44/337476/dell-inspiron-15-3520-i5-n3520i5u165w11slubl-290425-115203-353-600x600.jpg', 1
FROM chi_tiet_san_pham WHERE sp_id = (SELECT id FROM san_pham WHERE ma_san_pham = 'SP016');

-- SP017
INSERT INTO hinh_anh (id_spct, url, anh_chinh_dai_dien)
SELECT id, 'https://cdnv2.tgdd.vn/mwg-static/tgdd/Products/Images/44/340559/dell-inspiron-15-3530-i7-n3530-i7u161w11slu-638900114347898134-600x600.jpg', 1
FROM chi_tiet_san_pham WHERE sp_id = (SELECT id FROM san_pham WHERE ma_san_pham = 'SP017');

-- SP018
INSERT INTO hinh_anh (id_spct, url, anh_chinh_dai_dien)
SELECT id, 'https://cdnv2.tgdd.vn/mwg-static/tgdd/Products/Images/44/341563/dell-alienware-16-aurora-ac16250-core-5-210h-71072939-thumb2-638939737088688848-600x600.jpg', 1
FROM chi_tiet_san_pham WHERE sp_id = (SELECT id FROM san_pham WHERE ma_san_pham = 'SP018');

-- SP019
INSERT INTO hinh_anh (id_spct, url, anh_chinh_dai_dien)
SELECT id, 'https://cdnv2.tgdd.vn/mwg-static/tgdd/Products/Images/44/337472/dell-inspiron-15-3530-i7-71053721-638900879577580798-600x600.jpg', 1
FROM chi_tiet_san_pham WHERE sp_id = (SELECT id FROM san_pham WHERE ma_san_pham = 'SP019');

-- SP020
INSERT INTO hinh_anh (id_spct, url, anh_chinh_dai_dien)
SELECT id, 'https://cdnv2.tgdd.vn/mwg-static/tgdd/Products/Images/44/337471/dell-inspiron-15-3530-i5-71053696-thumb-638828188152325496-600x600.jpg', 1
FROM chi_tiet_san_pham WHERE sp_id = (SELECT id FROM san_pham WHERE ma_san_pham = 'SP020');

-- SP021 (Từ file bạn cung cấp)
INSERT INTO hinh_anh (id_spct, url, anh_chinh_dai_dien)
SELECT id, 'https://cdnv2.tgdd.vn/mwg-static/tgdd/Products/Images/44/357990/dell-15-dc15250-i5-1334u-cph99-thumb-638967241287160500-600x600.jpg', 1
FROM chi_tiet_san_pham WHERE sp_id = (SELECT id FROM san_pham WHERE ma_san_pham = 'SP021');

-- SP022
INSERT INTO hinh_anh (id_spct, url, anh_chinh_dai_dien)
SELECT id, 'https://cdnv2.tgdd.vn/mwg-static/tgdd/Products/Images/44/325247/dell-inspiron-15-3530-i7-p16wd-thumb-638754948496889615-600x600.jpg', 1
FROM chi_tiet_san_pham WHERE sp_id = (SELECT id FROM san_pham WHERE ma_san_pham = 'SP022');

-- SP023
INSERT INTO hinh_anh (id_spct, url, anh_chinh_dai_dien)
SELECT id, 'https://cdnv2.tgdd.vn/mwg-static/tgdd/Products/Images/44/337470/dell-inspiron-15-3520-i5-71064798-thumb-638828184201929000-600x600.jpg', 1
FROM chi_tiet_san_pham WHERE sp_id = (SELECT id FROM san_pham WHERE ma_san_pham = 'SP023');

-- SP024
INSERT INTO hinh_anh (id_spct, url, anh_chinh_dai_dien)
SELECT id, 'https://cdnv2.tgdd.vn/mwg-static/tgdd/Products/Images/44/333107/dell-inspiron-14-5440-i5-ndy5v1-638774727166795143-600x600.jpg', 1
FROM chi_tiet_san_pham WHERE sp_id = (SELECT id FROM san_pham WHERE ma_san_pham = 'SP024');

-- SP025
INSERT INTO hinh_anh (id_spct, url, anh_chinh_dai_dien)
SELECT id, 'https://cdnv2.tgdd.vn/mwg-static/tgdd/Products/Images/44/325244/dell-inspiron-14-5440-core-7-n4i7204w1-thumb-638754948363791973-600x600.jpg', 1
FROM chi_tiet_san_pham WHERE sp_id = (SELECT id FROM san_pham WHERE ma_san_pham = 'SP025');

-- SP026
INSERT INTO hinh_anh (id_spct, url, anh_chinh_dai_dien)
SELECT id, 'https://cdnv2.tgdd.vn/mwg-static/tgdd/Products/Images/44/358498/dell-15-dc15250-i5-1334u-71071928-thumb-2-638975019260439384-600x600.jpg', 1
FROM chi_tiet_san_pham WHERE sp_id = (SELECT id FROM san_pham WHERE ma_san_pham = 'SP026');

-- SP027
INSERT INTO hinh_anh (id_spct, url, anh_chinh_dai_dien)
SELECT id, 'https://cdnv2.tgdd.vn/mwg-static/tgdd/Products/Images/44/325953/dell-inspiron-14-5440-core-7-7fn5j-thumb-638754955274250524-600x600.jpg', 1
FROM chi_tiet_san_pham WHERE sp_id = (SELECT id FROM san_pham WHERE ma_san_pham = 'SP027');

-- SP028
INSERT INTO hinh_anh (id_spct, url, anh_chinh_dai_dien)
SELECT id, 'https://cdnv2.tgdd.vn/mwg-static/tgdd/Products/Images/44/333102/dell-inspiron-15-3520-i7-n3520i7u165w11blufp-thumb-638760098095158958-600x600.jpg', 1
FROM chi_tiet_san_pham WHERE sp_id = (SELECT id FROM san_pham WHERE ma_san_pham = 'SP028');

-- SP029
INSERT INTO hinh_anh (id_spct, url, anh_chinh_dai_dien)
SELECT id, 'https://cdnv2.tgdd.vn/mwg-static/tgdd/Products/Images/44/333103/dell-inspiron-15-3520-i7-25p2315-thumb-638760098242571857-600x600.jpg', 1
FROM chi_tiet_san_pham WHERE sp_id = (SELECT id FROM san_pham WHERE ma_san_pham = 'SP029');

-- SP030
INSERT INTO hinh_anh (id_spct, url, anh_chinh_dai_dien)
SELECT id, 'https://cdnv2.tgdd.vn/mwg-static/tgdd/Products/Images/44/333886/dell-inspiron-15-3520-i5-n5i5057w1-638774726911323154-600x600.jpg', 1
FROM chi_tiet_san_pham WHERE sp_id = (SELECT id FROM san_pham WHERE ma_san_pham = 'SP030');

-- SP031
INSERT INTO hinh_anh (id_spct, url, anh_chinh_dai_dien)
SELECT id, 'https://cdnv2.tgdd.vn/mwg-static/tgdd/Products/Images/44/333104/dell-inspiron-15-3520-i7-71053682-thumb-638760098370549871-600x600.jpg', 1
FROM chi_tiet_san_pham WHERE sp_id = (SELECT id FROM san_pham WHERE ma_san_pham = 'SP031');

-- SP032
INSERT INTO hinh_anh (id_spct, url, anh_chinh_dai_dien)
SELECT id, 'https://cdnv2.tgdd.vn/mwg-static/tgdd/Products/Images/44/338317/dell-inspiron-15-3530-i7-n3530-i7u161w11slu-bl-638840244563868428-600x600.jpg', 1
FROM chi_tiet_san_pham WHERE sp_id = (SELECT id FROM san_pham WHERE ma_san_pham = 'SP032');

-- SP033
INSERT INTO hinh_anh (id_spct, url, anh_chinh_dai_dien)
SELECT id, 'https://cdnv2.tgdd.vn/mwg-static/tgdd/Products/Images/44/351613/dell-15-dc15250-i7-1355u-dc15250-i7u161w11slu-thumb-638938737336707122-600x600.jpg', 1
FROM chi_tiet_san_pham WHERE sp_id = (SELECT id FROM san_pham WHERE ma_san_pham = 'SP033');

-- SP034
INSERT INTO hinh_anh (id_spct, url, anh_chinh_dai_dien)
SELECT id, 'https://cdnv2.tgdd.vn/mwg-static/tgdd/Products/Images/44/337469/dell-inspiron-15-3520-i5-71054775-638854982546544663-600x600.jpg', 1
FROM chi_tiet_san_pham WHERE sp_id = (SELECT id FROM san_pham WHERE ma_san_pham = 'SP034');

-- SP035
INSERT INTO hinh_anh (id_spct, url, anh_chinh_dai_dien)
SELECT id, 'https://cdnv2.tgdd.vn/mwg-static/tgdd/Products/Images/44/333105/dell-inspiron-15-3520-i7-n5i7125w1-thumb-638760098504371682-600x600.jpg', 1
FROM chi_tiet_san_pham WHERE sp_id = (SELECT id FROM san_pham WHERE ma_san_pham = 'SP035');

-- SP036
INSERT INTO hinh_anh (id_spct, url, anh_chinh_dai_dien)
SELECT id, 'https://cdnv2.tgdd.vn/mwg-static/tgdd/Products/Images/44/341568/dell-inspiron-15-3520-i7-1255-71069748-thumb-638897293267474745-600x600.jpg', 1
FROM chi_tiet_san_pham WHERE sp_id = (SELECT id FROM san_pham WHERE ma_san_pham = 'SP036');

-- SP037
INSERT INTO hinh_anh (id_spct, url, anh_chinh_dai_dien)
SELECT id, 'https://cdnv2.tgdd.vn/mwg-static/tgdd/Products/Images/44/329852/dell-inspiron-7440-core-7-n7440c7u161w11ibu-thumb-638760093753246499-600x600.jpg', 1
FROM chi_tiet_san_pham WHERE sp_id = (SELECT id FROM san_pham WHERE ma_san_pham = 'SP037');

-- SP038
INSERT INTO hinh_anh (id_spct, url, anh_chinh_dai_dien)
SELECT id, 'https://cdnv2.tgdd.vn/mwg-static/tgdd/Products/Images/44/338315/dell-latitude-3450-i7-71058806-638851475651047423-600x600.jpg', 1
FROM chi_tiet_san_pham WHERE sp_id = (SELECT id FROM san_pham WHERE ma_san_pham = 'SP038');

-- SP039
INSERT INTO hinh_anh (id_spct, url, anh_chinh_dai_dien)
SELECT id, 'https://cdnv2.tgdd.vn/mwg-static/tgdd/Products/Images/44/327981/dell-inspiron-15-3530-i5-n3530i5u165w11slu-thumb-638754978853473065-600x600.jpg', 1
FROM chi_tiet_san_pham WHERE sp_id = (SELECT id FROM san_pham WHERE ma_san_pham = 'SP039');

-- SP040
INSERT INTO hinh_anh (id_spct, url, anh_chinh_dai_dien)
SELECT id, 'https://cdnv2.tgdd.vn/mwg-static/tgdd/Products/Images/44/341564/dell-alienware-16-aurora-ac16250-core-7-240h-c7h161w11ii5050-thumb-638893911967537316-600x600.jpg', 1
FROM chi_tiet_san_pham WHERE sp_id = (SELECT id FROM san_pham WHERE ma_san_pham = 'SP040');

PRINT '>> HOAN TAT CAP NHAT DU LIEU BO SUNG (SP031-SP040 & HINH ANH MOI)';
GO
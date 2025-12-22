-- Script tạo quy đổi điểm mặc định
-- Tỷ lệ tích điểm: 1.000.000 VND = 1 điểm (tienTichDiem = 1.000.000)
-- Tỷ lệ quy đổi điểm: 1 điểm = 1.000 VND (tienTieuDiem = 1.000)

-- Kiểm tra và tạo quy đổi điểm mặc định nếu chưa có
INSERT INTO quy_doi_diem (id, tien_tich_diem, tien_tieu_diem, trang_thai)
SELECT 
    gen_random_uuid() as id,
    1000000.00 as tien_tich_diem,  -- 1.000.000 VND = 1 điểm (tích điểm)
    1000.00 as tien_tieu_diem,     -- 1 điểm = 1.000 VND (tiêu điểm)
    1 as trang_thai                 -- Trạng thái: 1 = Hoạt động
WHERE NOT EXISTS (
    SELECT 1 FROM quy_doi_diem WHERE trang_thai = 1
);

-- Nếu đã có quy đổi điểm nhưng chưa có trạng thái = 1, cập nhật một record đầu tiên
UPDATE quy_doi_diem 
SET 
    tien_tich_diem = 1000000.00,
    tien_tieu_diem = 1000.00,
    trang_thai = 1
WHERE id = (
    SELECT id FROM quy_doi_diem 
    ORDER BY id ASC 
    LIMIT 1
)
AND NOT EXISTS (
    SELECT 1 FROM quy_doi_diem WHERE trang_thai = 1
);

-- Cập nhật quy đổi điểm đang hoạt động thành tỷ lệ mới (nếu đang là tỷ lệ cũ)
UPDATE quy_doi_diem 
SET 
    tien_tich_diem = 1000000.00,
    tien_tieu_diem = 1000.00
WHERE trang_thai = 1 
AND (tien_tich_diem != 1000000.00 OR tien_tieu_diem != 1000.00);


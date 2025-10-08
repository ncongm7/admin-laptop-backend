/*
    SQL SCRIPT FOR LAPTOP E-COMMERCE DATABASE (REVISED TO MATCH ERD EXACTLY)
    ---------------------------------------------------------------------
    - Author: AI Expert
    - Database: SQL Server
    - Description: This script is a faithful implementation of the provided ERD,
      correcting discrepancies and adding all missing attributes.
*/

-- CREATE DATABASE QuanLyBanHangLaptop_TheoERD;
-- GO
-- USE QuanLyBanHangLaptop_TheoERD;
-- GO

-- ===================================================================================
-- I. CÁC BẢNG DANH MỤC CỐT LÕI
-- ===================================================================================

CREATE TABLE phuong_thuc_thanh_toan (
                                        id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
                                        ten_phuong_thuc NVARCHAR(255),
                                        loai_phuong_thuc NVARCHAR(100)
);

CREATE TABLE cpu ( id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(), ma_cpu VARCHAR(50) UNIQUE, ten_cpu NVARCHAR(255), mo_ta NVARCHAR(MAX), trang_thai INT );
CREATE TABLE ram ( id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(), ma_ram VARCHAR(50) UNIQUE, ten_ram NVARCHAR(255), mo_ta NVARCHAR(MAX), trang_thai INT );
CREATE TABLE o_cung ( id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(), ma_o_cung VARCHAR(50) UNIQUE, dung_luong NVARCHAR(100), mo_ta NVARCHAR(MAX), trang_thai INT );
CREATE TABLE gpu ( id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(), ma_gpu VARCHAR(50) UNIQUE, ten_gpu NVARCHAR(255), mo_ta NVARCHAR(MAX), trang_thai INT );
CREATE TABLE loai_man_hinh ( id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(), ma_loai_man_hinh VARCHAR(50) UNIQUE, kich_thuoc NVARCHAR(100), mo_ta NVARCHAR(MAX), trang_thai INT );
CREATE TABLE pin ( id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(), ma_pin VARCHAR(50) UNIQUE, dung_luong_pin NVARCHAR(100), mo_ta NVARCHAR(MAX), trang_thai INT );
CREATE TABLE mau_sac ( id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(), ma_mau VARCHAR(50) UNIQUE, ten_mau NVARCHAR(100), mo_ta NVARCHAR(MAX), trang_thai INT );

-- ===================================================================================
-- II. CẤU TRÚC NGƯỜI DÙNG VÀ TÀI KHOẢN (THEO ĐÚNG ERD)
-- ===================================================================================

--- THÊM: Bảng Vai Trò như trong ERD.
CREATE TABLE vai_tro (
                         id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
                         ma_vai_tro VARCHAR(50) UNIQUE,
                         ten_vai_tro NVARCHAR(100),
                         mo_ta NVARCHAR(500)
);

--- SỬA: Bảng TaiKhoan được tái cấu trúc lại để khớp ERD.
CREATE TABLE tai_khoan (
                           id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
                           ma_vai_tro UNIQUEIDENTIFIER,
                           ten_dang_nhap VARCHAR(100) UNIQUE,
                           mat_khau NVARCHAR(255),
                           email VARCHAR(100) UNIQUE,
                           trang_thai INT,
                           ngay_tao DATETIME2,
                           lan_dang_nhap_cuoi DATETIME2
);

--- SỬA: Bảng KhachHang với đầy đủ thuộc tính và liên kết đúng.
CREATE TABLE khach_hang (
                            user_id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
                            ma_tai_khoan UNIQUEIDENTIFIER UNIQUE, -- FK đến tai_khoan
                            ma_khach_hang VARCHAR(50) UNIQUE,
                            ho_ten NVARCHAR(255),
                            so_dien_thoai VARCHAR(20),
                            email VARCHAR(100),
                            gioi_tinh INT,
                            ngay_sinh DATE,
                            trang_thai INT,
                            id_diem UNIQUEIDENTIFIER -- FK đến tich_diem
);

--- SỬA: Bảng NhanVien với đầy đủ thuộc tính và liên kết đúng.
CREATE TABLE nhan_vien (
                           user_id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
                           ma_tai_khoan UNIQUEIDENTIFIER UNIQUE, -- FK đến tai_khoan
                           ma_nhan_vien VARCHAR(50) UNIQUE,
                           ho_ten NVARCHAR(255),
                           so_dien_thoai VARCHAR(20),
                           email VARCHAR(100),
                           gioi_tinh INT,
                           anh_nhan_vien VARCHAR(MAX),
    chuc_vu NVARCHAR(100),
    dia_chi NVARCHAR(500),
    danh_gia NVARCHAR(MAX),
    trang_thai INT
);

CREATE TABLE dia_chi (
                         id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
                         user_id UNIQUEIDENTIFIER, -- FK đến khach_hang
                         dia_chi NVARCHAR(500),
                         mac_dinh BIT
);

-- ===================================================================================
-- III. CẤU TRÚC SẢN PHẨM (Đúng tên cột theo ERD)
-- ===================================================================================

CREATE TABLE san_pham (
                          id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
                          ma_san_pham VARCHAR(50) UNIQUE,
                          ten_san_pham NVARCHAR(255),
                          mo_ta NVARCHAR(MAX),
                          trang_thai INT,
                          ngay_tao DATETIME2,
                          ngay_sua DATETIME2,
                          nguoi_tao NVARCHAR(255),
                          nguoi_sua NVARCHAR(255)
);

CREATE TABLE chi_tiet_san_pham (
                                   id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
                                   sp_id UNIQUEIDENTIFIER, -- SỬA: Đổi tên FK
                                   cpu_id UNIQUEIDENTIFIER,
                                   ram_id UNIQUEIDENTIFIER,
                                   o_cung_id UNIQUEIDENTIFIER,
                                   gpu_id UNIQUEIDENTIFIER,
                                   loai_man_hinh_id UNIQUEIDENTIFIER,
                                   pin_id UNIQUEIDENTIFIER,
                                   mau_sac_id UNIQUEIDENTIFIER,
                                   ma_ctsp VARCHAR(50) UNIQUE,
                                   gia_ban DECIMAL(18, 2),
                                   ghi_chu NVARCHAR(MAX),
                                   trang_thai INT
);

CREATE TABLE hinh_anh (
                          id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
                          id_spct UNIQUEIDENTIFIER, -- SỬA: Đổi tên FK
                          url VARCHAR(MAX),
    anh_chinh_dai_dien BIT,
    ngay_tao DATETIME2,
    ngay_sua DATETIME2
);

CREATE TABLE serial (
                        id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
                        ctsp_id UNIQUEIDENTIFIER, -- SỬA: Đổi tên FK
                        serial_no VARCHAR(100) UNIQUE,
                        trang_thai INT,
                        ngay_nhap DATETIME2
);

-- ===================================================================================
-- IV. BÁN HÀNG: GIỎ HÀNG, HÓA ĐƠN, THANH TOÁN (Đầy đủ thuộc tính)
-- ===================================================================================

CREATE TABLE gio_hang (
                          id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
                          khach_hang_id UNIQUEIDENTIFIER,
                          ngay_tao DATETIME2,
                          ngay_cap_nhat DATETIME2,
                          trang_thai_gio_hang INT
);

CREATE TABLE gio_hang_chi_tiet (
                                   id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
                                   gio_hang_id UNIQUEIDENTIFIER,
                                   chi_tiet_san_pham_id UNIQUEIDENTIFIER,
                                   so_luong INT,
                                   don_gia DECIMAL(18, 2),
                                   ngay_them DATETIME2
);

CREATE TABLE hoa_don (
                         id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
                         ma VARCHAR(50) UNIQUE,
                         id_khach_hang UNIQUEIDENTIFIER,
                         id_nhan_vien UNIQUEIDENTIFIER,
                         id_phieu_giam_gia UNIQUEIDENTIFIER,
                         ma_don_hang VARCHAR(50),
                         ten_khach_hang NVARCHAR(255),
                         sdt VARCHAR(20),
                         dia_chi NVARCHAR(500),
                         tong_tien DECIMAL(18, 2),
                         tien_duoc_giam DECIMAL(18, 2),
                         tong_tien_sau_giam DECIMAL(18, 2),
                         loai_hoa_don INT,
                         ghi_chu NVARCHAR(MAX),
                         ngay_tao DATETIME2,
                         ngay_thanh_toan DATETIME2,
                         trang_thai_thanh_toan INT,
                         trang_thai INT,
    --- THÊM: Các cột bị thiếu liên quan đến điểm
                         so_diem_su_dung INT,
                         so_tien_quy_doi DECIMAL(18, 2)
);

CREATE TABLE hoa_don_chi_tiet (
                                  id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
                                  id_don_hang UNIQUEIDENTIFIER, -- SỬA: Đổi tên FK
                                  id_ctsp UNIQUEIDENTIFIER, -- SỬA: Đổi tên FK
                                  so_luong INT,
                                  don_gia DECIMAL(18, 2)
);

CREATE TABLE lich_su_hoa_don (
                                 id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
                                 id_hoa_don UNIQUEIDENTIFIER,
                                 id_nhan_vien UNIQUEIDENTIFIER,
                                 ma VARCHAR(50),
                                 hanh_dong NVARCHAR(MAX),
                                 thoi_gian DATETIME2,
                                 deleted BIT
);

CREATE TABLE chi_tiet_thanh_toan (
                                     id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
                                     id_hoa_don UNIQUEIDENTIFIER,
                                     phuong_thuc_thanh_toan_id UNIQUEIDENTIFIER,
                                     so_tien_thanh_toan DECIMAL(18, 2),
                                     ma_giao_dich VARCHAR(100),
                                     ghi_chu NVARCHAR(MAX)
);

-- ===================================================================================
-- V. KHUYẾN MÃI (Đầy đủ thuộc tính)
-- ===================================================================================

CREATE TABLE phieu_giam_gia (
                                id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
                                ma VARCHAR(50) UNIQUE,
                                ten_phieu_giam_gia NVARCHAR(255),
                                loai_phieu_giam_gia INT,
                                gia_tri_giam_gia DECIMAL(18, 2),
                                so_tien_giam_toi_da DECIMAL(18, 2),
                                hoa_don_toi_thieu DECIMAL(18, 2),
                                so_luong_dung INT,
                                ngay_bat_dau DATETIME2,
                                ngay_ket_thuc DATETIME2,
                                rieng_tu BIT,
                                mo_ta NVARCHAR(MAX),
                                trang_thai INT
);

CREATE TABLE dot_giam_gia (
                              id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
                              ten_km NVARCHAR(255), -- SỬA: Tên cột theo ERD
                              gia_tri INT,
                              mo_ta NVARCHAR(MAX),
                              ngayBatDau DATETIME2,
                              ngayKetThuc DATETIME2,
                              trang_thai INT
);

CREATE TABLE dot_giam_gia_chi_tiet (
                                       id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
                                       id_km UNIQUEIDENTIFIER, -- SỬA: Tên cột theo ERD
                                       id_ctsp UNIQUEIDENTIFIER,
                                       gia_ban_dau DECIMAL(18, 2),
                                       gia_sau_khi_giam DECIMAL(18, 2),
                                       ghi_chu NVARCHAR(MAX)
);

-- ===================================================================================
-- VI. SAU BÁN HÀNG: BẢO HÀNH, ĐÁNH GIÁ, CHAT
-- ===================================================================================

--- SỬA: Cấu trúc logic và rõ ràng hơn cho Serial đã bán
CREATE TABLE serial_da_ban (
                               id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
                               id_hoa_don_chi_tiet UNIQUEIDENTIFIER NOT NULL,
                               id_serial UNIQUEIDENTIFIER NOT NULL,
                               ngay_tao DATETIME2 DEFAULT GETDATE()
);

CREATE TABLE phieu_bao_hanh (
                                id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
                                id_khach_hang UNIQUEIDENTIFIER,
                                id_serial_da_ban UNIQUEIDENTIFIER,
                                ngay_bat_dau DATETIME2,
                                ngay_ket_thuc DATETIME2,
                                trang_thai_bao_hanh INT
);

CREATE TABLE lich_su_bao_hanh (
                                  id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
                                  id_bao_hanh UNIQUEIDENTIFIER,
                                  ngay_tiep_nhan DATETIME2,
                                  ngay_hoan_thanh DATETIME2,
                                  mo_ta_loi NVARCHAR(MAX),
                                  trang_thai INT
);

CREATE TABLE danh_gia (
                          id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
                          khach_hang_id UNIQUEIDENTIFIER,
                          san_pham_chi_tiet_id UNIQUEIDENTIFIER,
                          hoa_don_chi_tiet_id UNIQUEIDENTIFIER,
                          so_sao INT,
                          noi_dung NVARCHAR(MAX),
                          ngay_danh_gia DATETIME2,
                          trang_thai_danh_gia INT
);

CREATE TABLE media_danh_gia (
                                id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
                                danh_gia_id UNIQUEIDENTIFIER,
                                loai_media INT,
                                url_media VARCHAR(MAX),
    kich_thuoc_file BIGINT,
    thoi_luong_video INT,
    thu_tu_hien_thi INT,
    ngay_upload DATETIME2,
    trang_thai_media_danh_gia INT
);

CREATE TABLE phan_hoi_danh_gia (
                                   id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
                                   danh_gia_id UNIQUEIDENTIFIER,
                                   nhan_vien_id UNIQUEIDENTIFIER,
                                   noi_dung NVARCHAR(MAX),
                                   ngay_phan_hoi DATETIME2
);

CREATE TABLE chat (
                      id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
                      khach_hang_id UNIQUEIDENTIFIER,
                      nhan_vien_id UNIQUEIDENTIFIER,
                      noi_dung NVARCHAR(MAX),
                      ngay_phan_hoi DATETIME2
);

-- ===================================================================================
-- VII. HỆ THỐNG ĐIỂM TÍCH LŨY (Theo đúng ERD)
-- ===================================================================================

CREATE TABLE tich_diem (
                           id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
                           user_id UNIQUEIDENTIFIER UNIQUE, -- FK đến khach_hang
                           diem_da_dung INT,
                           diem_da_cong INT,
                           tong_diem INT
);

CREATE TABLE quy_doi_diem (
                              id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
                              tien_tich_diem DECIMAL(18, 2),
                              tien_tieu_diem DECIMAL(18, 2),
                              trang_thai INT
);

--- SỬA: Bảng LichSuDiem với đầy đủ các thuộc tính từ ERD
CREATE TABLE lich_su_diem (
                              id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
                              tich_diem_id UNIQUEIDENTIFIER,
                              hoa_don_id UNIQUEIDENTIFIER,
                              id_quy_doi_diem UNIQUEIDENTIFIER,
                              loai_diem INT,
                              ghi_chu NVARCHAR(MAX),
                              thoi_gian DATETIME2,
                              han_su_dung DATE,
                              so_diem_da_dung INT,
                              so_diem_cong INT,
                              trang_thai INT
);

CREATE TABLE chi_tiet_lich_su_diem (
                                       id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
                                       user_id UNIQUEIDENTIFIER, -- FK đến khach_hang
                                       lich_su_diem_id UNIQUEIDENTIFIER,
                                       so_diem_da_tru INT,
                                       ngay_tru DATETIME2
);

GO
-- ===================================================================================
-- VIII. TẠO RÀNG BUỘC KHÓA NGOẠI (FOREIGN KEY CONSTRAINTS)
-- ===================================================================================

-- User & Account
ALTER TABLE tai_khoan ADD CONSTRAINT FK_TaiKhoan_VaiTro FOREIGN KEY (ma_vai_tro) REFERENCES vai_tro(id);
ALTER TABLE khach_hang ADD CONSTRAINT FK_KhachHang_TaiKhoan FOREIGN KEY (ma_tai_khoan) REFERENCES tai_khoan(id);
ALTER TABLE nhan_vien ADD CONSTRAINT FK_NhanVien_TaiKhoan FOREIGN KEY (ma_tai_khoan) REFERENCES tai_khoan(id);
ALTER TABLE dia_chi ADD CONSTRAINT FK_DiaChi_KhachHang FOREIGN KEY (user_id) REFERENCES khach_hang(user_id);

-- Product
ALTER TABLE chi_tiet_san_pham ADD CONSTRAINT FK_CTSP_SanPham FOREIGN KEY (sp_id) REFERENCES san_pham(id);
ALTER TABLE hinh_anh ADD CONSTRAINT FK_HinhAnh_CTSP FOREIGN KEY (id_spct) REFERENCES chi_tiet_san_pham(id);
ALTER TABLE serial ADD CONSTRAINT FK_Serial_CTSP FOREIGN KEY (ctsp_id) REFERENCES chi_tiet_san_pham(id);
-- (Thêm các FK còn lại từ chi_tiet_san_pham đến các bảng linh kiện)

-- Sales
ALTER TABLE gio_hang ADD CONSTRAINT FK_GioHang_KhachHang FOREIGN KEY (khach_hang_id) REFERENCES khach_hang(user_id);
ALTER TABLE hoa_don ADD CONSTRAINT FK_HoaDon_KhachHang FOREIGN KEY (id_khach_hang) REFERENCES khach_hang(user_id);
ALTER TABLE hoa_don ADD CONSTRAINT FK_HoaDon_NhanVien FOREIGN KEY (id_nhan_vien) REFERENCES nhan_vien(user_id);
ALTER TABLE hoa_don_chi_tiet ADD CONSTRAINT FK_HDCT_HoaDon FOREIGN KEY (id_don_hang) REFERENCES hoa_don(id);
ALTER TABLE hoa_don_chi_tiet ADD CONSTRAINT FK_HDCT_CTSP FOREIGN KEY (id_ctsp) REFERENCES chi_tiet_san_pham(id);

-- After-Sales
ALTER TABLE serial_da_ban ADD CONSTRAINT FK_SerialDaBan_HDCT FOREIGN KEY (id_hoa_don_chi_tiet) REFERENCES hoa_don_chi_tiet(id);
ALTER TABLE serial_da_ban ADD CONSTRAINT FK_SerialDaBan_Serial FOREIGN KEY (id_serial) REFERENCES serial(id);
ALTER TABLE phieu_bao_hanh ADD CONSTRAINT FK_PhieuBaoHanh_SerialDaBan FOREIGN KEY (id_serial_da_ban) REFERENCES serial_da_ban(id);
ALTER TABLE danh_gia ADD CONSTRAINT FK_DanhGia_KhachHang FOREIGN KEY (khach_hang_id) REFERENCES khach_hang(user_id);

-- Point System
ALTER TABLE khach_hang ADD CONSTRAINT FK_KhachHang_TichDiem FOREIGN KEY (id_diem) REFERENCES tich_diem(id);
ALTER TABLE tich_diem ADD CONSTRAINT FK_TichDiem_KhachHang FOREIGN KEY (user_id) REFERENCES khach_hang(user_id);
ALTER TABLE lich_su_diem ADD CONSTRAINT FK_LichSuDiem_TichDiem FOREIGN KEY (tich_diem_id) REFERENCES tich_diem(id);
ALTER TABLE lich_su_diem ADD CONSTRAINT FK_LichSuDiem_QuyDoiDiem FOREIGN KEY (id_quy_doi_diem) REFERENCES quy_doi_diem(id);

GO
-- ===================================================================================
-- SCRIPT TẠO DATABASE QUẢN LÝ BÁN HÀNG LAPTOP (PHIÊN BẢN HOÀN CHỈNH - MỚI TINH)
-- Database: QuanLyBanHangLaptop_TheoERD1_New
-- Ngày tạo: 2025-11-19
-- Mô tả: File SQL hoàn chỉnh với tất cả các cải tiến và indexes đã sửa
-- ===================================================================================

-- Bước 0: Xóa database cũ nếu tồn tại để chạy lại từ đầu
USE master;
GO

IF DB_ID('QuanLyBanHangLaptop_TheoERD1_New') IS NOT NULL
BEGIN
    ALTER DATABASE QuanLyBanHangLaptop_TheoERD1_New SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE QuanLyBanHangLaptop_TheoERD1_New;
    PRINT 'Database QuanLyBanHangLaptop_TheoERD1_New đã được xóa.';
END
GO

-- Tạo lại database
CREATE DATABASE QuanLyBanHangLaptop_TheoERD1_New;
GO
USE QuanLyBanHangLaptop_TheoERD1_New;
GO

-- ===================================================================================
-- I. CÁC BẢNG DANH MỤC CỐT LÕI
-- ===================================================================================
CREATE TABLE phuong_thuc_thanh_toan (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    ten_phuong_thuc NVARCHAR(255),
    loai_phuong_thuc NVARCHAR(100)
);

CREATE TABLE cpu (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    ma_cpu VARCHAR(50) UNIQUE,
    ten_cpu NVARCHAR(255),
    mo_ta NVARCHAR(MAX),
    trang_thai INT
);

CREATE TABLE ram (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    ma_ram VARCHAR(50) UNIQUE,
    ten_ram NVARCHAR(255),
    mo_ta NVARCHAR(MAX),
    trang_thai INT
);

CREATE TABLE o_cung (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    ma_o_cung VARCHAR(50) UNIQUE,
    dung_luong NVARCHAR(100),
    mo_ta NVARCHAR(MAX),
    trang_thai INT
);

CREATE TABLE gpu (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    ma_gpu VARCHAR(50) UNIQUE,
    ten_gpu NVARCHAR(255),
    mo_ta NVARCHAR(MAX),
    trang_thai INT
);

CREATE TABLE loai_man_hinh (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    ma_loai_man_hinh VARCHAR(50) UNIQUE,
    kich_thuoc NVARCHAR(100),
    mo_ta NVARCHAR(MAX),
    trang_thai INT
);

CREATE TABLE pin (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    ma_pin VARCHAR(50) UNIQUE,
    dung_luong_pin NVARCHAR(100),
    mo_ta NVARCHAR(MAX),
    trang_thai INT
);

CREATE TABLE mau_sac (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    ma_mau VARCHAR(50) UNIQUE,
    ten_mau NVARCHAR(100),
    mo_ta NVARCHAR(MAX),
    trang_thai INT
);

-- ===================================================================================
-- II. CẤU TRÚC NGƯỜI DÙNG VÀ TÀI KHOẢN
-- ===================================================================================
CREATE TABLE vai_tro (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    ma_vai_tro VARCHAR(50) UNIQUE,
    ten_vai_tro NVARCHAR(100),
    mo_ta NVARCHAR(500)
);

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

CREATE TABLE khach_hang (
    user_id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    ma_tai_khoan UNIQUEIDENTIFIER, -- FK đến tai_khoan
    ma_khach_hang VARCHAR(50) UNIQUE,
    ho_ten NVARCHAR(255),
    so_dien_thoai VARCHAR(20),
    email VARCHAR(100),
    gioi_tinh INT,
    ngay_sinh DATE,
    trang_thai INT
);

CREATE TABLE nhan_vien (
    user_id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    ma_tai_khoan UNIQUEIDENTIFIER, -- FK đến tai_khoan (cho phép NULL)
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
    ho_ten NVARCHAR(255),
    so_dien_thoai NVARCHAR(20),
    xa NVARCHAR(255),
    tinh NVARCHAR(255),
    mac_dinh BIT
);

-- ===================================================================================
-- III. CẤU TRÚC SẢN PHẨM
-- ===================================================================================
CREATE TABLE san_pham (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    ma_san_pham VARCHAR(50) UNIQUE,
    ten_san_pham NVARCHAR(255),
    mo_ta NVARCHAR(MAX),
    thoi_han_bh_thang INT DEFAULT 12, -- Đơn vị là tháng, mặc định 12 tháng
    trang_thai INT,
    ngay_tao DATETIME2,
    ngay_sua DATETIME2,
    nguoi_tao NVARCHAR(255),
    nguoi_sua NVARCHAR(255)
);

CREATE TABLE chi_tiet_san_pham (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    sp_id UNIQUEIDENTIFIER,
    cpu_id UNIQUEIDENTIFIER,
    ram_id UNIQUEIDENTIFIER,
    o_cung_id UNIQUEIDENTIFIER,
    gpu_id UNIQUEIDENTIFIER,
    loai_man_hinh_id UNIQUEIDENTIFIER,
    pin_id UNIQUEIDENTIFIER,
    mau_sac_id UNIQUEIDENTIFIER,
    ma_ctsp VARCHAR(50) UNIQUE,
    gia_ban DECIMAL(18, 2),
    gia_nhap DECIMAL(18, 2) NULL,
    ghi_chu NVARCHAR(MAX),
    so_luong_ton INT DEFAULT 0,
    so_luong_tam_giu INT DEFAULT 0,
    trang_thai INT,
    version BIGINT DEFAULT 0,
    ngay_tao DATETIME2,
    ngay_sua DATETIME2
);

CREATE TABLE hinh_anh (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    id_spct UNIQUEIDENTIFIER,
    url VARCHAR(MAX),
    anh_chinh_dai_dien BIT,
    ngay_tao DATETIME2,
    ngay_sua DATETIME2
);

CREATE TABLE serial (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    ctsp_id UNIQUEIDENTIFIER,
    serial_no VARCHAR(100) UNIQUE,
    trang_thai INT, -- 0: Trong kho, 1: Đã bán, 2: Đang bảo hành, 3: Lỗi
    ngay_nhap DATETIME2
);

-- ===================================================================================
-- IV. BÁN HÀNG: GIỎ HÀNG, HÓA ĐƠN, THANH TOÁN
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
    loai_hoa_don INT, -- 0: Bán tại quầy, 1: Giao hàng
    ghi_chu NVARCHAR(MAX),
    ngay_tao DATETIME2,
    ngay_thanh_toan DATETIME2,
    trang_thai_thanh_toan INT, -- 0: Chưa thanh toán, 1: Đã thanh toán
    trang_thai INT, -- 0: Chờ xác nhận, 1: Đã xác nhận, 2: Đang giao, 3: Hoàn thành, 4: Đã hủy
    so_diem_su_dung INT,
    so_tien_quy_doi DECIMAL(18, 2)
);

CREATE TABLE hoa_don_chi_tiet (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    id_don_hang UNIQUEIDENTIFIER,
    id_ctsp UNIQUEIDENTIFIER,
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
    tien_khach_dua DECIMAL(18, 2) NULL, -- Số tiền khách đưa (cho thanh toán tiền mặt)
    tien_tra_lai DECIMAL(18, 2) NULL, -- Số tiền trả lại khách (cho thanh toán tiền mặt)
    ma_giao_dich VARCHAR(100),
    ghi_chu NVARCHAR(MAX)
);

-- ===================================================================================
-- V. KHUYẾN MÃI
-- ===================================================================================
CREATE TABLE phieu_giam_gia (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    ma VARCHAR(50) UNIQUE,
    ten_phieu_giam_gia NVARCHAR(255),
    loai_phieu_giam_gia INT, -- 1: Giảm theo %, 2: Giảm theo số tiền
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
    ten_km NVARCHAR(255),
    loai_dot_giam_gia INT, -- 1: Giảm theo %, 2: Giảm theo số tiền (VND)
    gia_tri DECIMAL(18, 2), -- Giá trị giảm: % (0-100) hoặc số tiền VND
    so_tien_giam_toi_da DECIMAL(18, 2) NULL, -- Giới hạn số tiền giảm tối đa (chỉ dùng khi loai = 1 - %)
    mo_ta NVARCHAR(MAX),
    ngayBatDau DATETIME2,
    ngayKetThuc DATETIME2,
    trang_thai INT,
    bannerImageUrl VARCHAR(255) NULL -- Cột mới cho banner ảnh
);

CREATE TABLE dot_giam_gia_chi_tiet (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    id_km UNIQUEIDENTIFIER,
    id_ctsp UNIQUEIDENTIFIER,
    gia_ban_dau DECIMAL(18, 2),
    gia_sau_khi_giam DECIMAL(18, 2),
    ghi_chu NVARCHAR(MAX)
);

-- ===================================================================================
-- VI. SAU BÁN HÀNG: BẢO HÀNH, ĐÁNH GIÁ, CHAT
-- ===================================================================================
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
-- VII. HỆ THỐNG ĐIỂM TÍCH LŨY
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

-- ===================================================================================
-- VIII. DANH MỤC SẢN PHẨM
-- ===================================================================================
CREATE TABLE danh_muc (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    ma_danh_muc VARCHAR(50) UNIQUE,
    ten_danh_muc NVARCHAR(255),
    mo_ta NVARCHAR(MAX),
    trang_thai INT
);

CREATE TABLE sanpham_danhmuc (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    san_pham_id UNIQUEIDENTIFIER,
    danh_muc_id UNIQUEIDENTIFIER
);

GO

-- ===================================================================================
-- VIII.1. THÊM CÁC CỘT TIMESTAMP VÀ HEX_CODE CHO CÁC BẢNG DANH MỤC
-- ===================================================================================

-- Lưu ý: ngay_tao và ngay_sua đã có trong CREATE TABLE chi_tiet_san_pham, không cần ALTER TABLE

ALTER TABLE cpu
ADD created_at DATETIME2;

ALTER TABLE cpu
ADD updated_at DATETIME2;

ALTER TABLE gpu
ADD created_at DATETIME2;

ALTER TABLE gpu
ADD updated_at DATETIME2;

ALTER TABLE ram
ADD created_at DATETIME2;

ALTER TABLE ram
ADD updated_at DATETIME2;

ALTER TABLE o_cung
ADD created_at DATETIME2;

ALTER TABLE o_cung
ADD updated_at DATETIME2;

ALTER TABLE loai_man_hinh
ADD created_at DATETIME2;

ALTER TABLE loai_man_hinh
ADD updated_at DATETIME2;

ALTER TABLE pin
ADD created_at DATETIME2;

ALTER TABLE pin
ADD updated_at DATETIME2;

ALTER TABLE mau_sac
ADD created_at DATETIME2;

ALTER TABLE mau_sac
ADD updated_at DATETIME2;

ALTER TABLE mau_sac
ADD hex_code VARCHAR(7);

GO

-- ===================================================================================
-- VIII.2. TẠO BẢNG ĐỊA CHỈ (LOCATION TABLES)
-- ===================================================================================

-- Bảng Phường/Xã
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='lc_subdistrict' AND xtype='U')
BEGIN
    CREATE TABLE lc_subdistrict (
        id            INT           NOT NULL PRIMARY KEY,
        district_id   INT           NOT NULL,
        district_code NVARCHAR(20)  NOT NULL,
        province_id   INT           NOT NULL,
        province_code NVARCHAR(20)  NOT NULL,
        name          NVARCHAR(100) NULL,
        shortname     NVARCHAR(100) NULL,
        code          NVARCHAR(36)  NULL,
        description   NVARCHAR(255) NULL,
        created_by    NVARCHAR(100) NULL,
        created_date  DATETIME2(6)  NULL,
        modified_by   NVARCHAR(100) NULL,
        modified_date DATETIME2(6)  NULL
    );
END;
GO

-- Bảng Tỉnh/Thành phố
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='lc_province' AND xtype='U')
BEGIN
    CREATE TABLE lc_province (
        id            INT           NOT NULL PRIMARY KEY,
        name          NVARCHAR(100) NOT NULL,
        shortname     NVARCHAR(100) NULL,
        code          NVARCHAR(36)  NULL,
        country_id    INT           NULL,
        description   NVARCHAR(255) NULL,
        created_by    NVARCHAR(100) NULL,
        created_date  DATETIME2(6)  NULL,
        modified_by   NVARCHAR(100) NULL,
        modified_date DATETIME2(6)  NULL
    );
END;
GO

-- ===================================================================================
-- IX. TẠO RÀNG BUỘC KHÓA NGOẠI (FOREIGN KEY CONSTRAINTS)
-- ===================================================================================

-- User & Account
ALTER TABLE tai_khoan ADD CONSTRAINT FK_TaiKhoan_VaiTro FOREIGN KEY (ma_vai_tro) REFERENCES vai_tro(id);
ALTER TABLE khach_hang ADD CONSTRAINT FK_KhachHang_TaiKhoan FOREIGN KEY (ma_tai_khoan) REFERENCES tai_khoan(id);
ALTER TABLE nhan_vien ADD CONSTRAINT FK_NhanVien_TaiKhoan FOREIGN KEY (ma_tai_khoan) REFERENCES tai_khoan(id);
ALTER TABLE dia_chi ADD CONSTRAINT FK_DiaChi_KhachHang FOREIGN KEY (user_id) REFERENCES khach_hang(user_id);

-- Product
ALTER TABLE chi_tiet_san_pham ADD CONSTRAINT FK_CTSP_SanPham FOREIGN KEY (sp_id) REFERENCES san_pham(id);
ALTER TABLE chi_tiet_san_pham ADD CONSTRAINT FK_CTSP_CPU FOREIGN KEY (cpu_id) REFERENCES cpu(id);
ALTER TABLE chi_tiet_san_pham ADD CONSTRAINT FK_CTSP_RAM FOREIGN KEY (ram_id) REFERENCES ram(id);
ALTER TABLE chi_tiet_san_pham ADD CONSTRAINT FK_CTSP_OCUNG FOREIGN KEY (o_cung_id) REFERENCES o_cung(id);
ALTER TABLE chi_tiet_san_pham ADD CONSTRAINT FK_CTSP_GPU FOREIGN KEY (gpu_id) REFERENCES gpu(id);
ALTER TABLE chi_tiet_san_pham ADD CONSTRAINT FK_CTSP_LOAI_MAN_HINH FOREIGN KEY (loai_man_hinh_id) REFERENCES loai_man_hinh(id);
ALTER TABLE chi_tiet_san_pham ADD CONSTRAINT FK_CTSP_PIN FOREIGN KEY (pin_id) REFERENCES pin(id);
ALTER TABLE chi_tiet_san_pham ADD CONSTRAINT FK_CTSP_MAU_SAC FOREIGN KEY (mau_sac_id) REFERENCES mau_sac(id);
ALTER TABLE hinh_anh ADD CONSTRAINT FK_HinhAnh_CTSP FOREIGN KEY (id_spct) REFERENCES chi_tiet_san_pham(id);
ALTER TABLE serial ADD CONSTRAINT FK_Serial_CTSP FOREIGN KEY (ctsp_id) REFERENCES chi_tiet_san_pham(id);

-- Sales
ALTER TABLE gio_hang ADD CONSTRAINT FK_GioHang_KhachHang FOREIGN KEY (khach_hang_id) REFERENCES khach_hang(user_id);
ALTER TABLE gio_hang_chi_tiet ADD CONSTRAINT FK_GHCT_GioHang FOREIGN KEY (gio_hang_id) REFERENCES gio_hang(id);
ALTER TABLE gio_hang_chi_tiet ADD CONSTRAINT FK_GHCT_CTSP FOREIGN KEY (chi_tiet_san_pham_id) REFERENCES chi_tiet_san_pham(id);
ALTER TABLE hoa_don ADD CONSTRAINT FK_HoaDon_KhachHang FOREIGN KEY (id_khach_hang) REFERENCES khach_hang(user_id);
ALTER TABLE hoa_don ADD CONSTRAINT FK_HoaDon_NhanVien FOREIGN KEY (id_nhan_vien) REFERENCES nhan_vien(user_id);
ALTER TABLE hoa_don ADD CONSTRAINT FK_HoaDon_PhieuGiamGia FOREIGN KEY (id_phieu_giam_gia) REFERENCES phieu_giam_gia(id);
ALTER TABLE hoa_don_chi_tiet ADD CONSTRAINT FK_HDCT_HoaDon FOREIGN KEY (id_don_hang) REFERENCES hoa_don(id);
ALTER TABLE hoa_don_chi_tiet ADD CONSTRAINT FK_HDCT_CTSP FOREIGN KEY (id_ctsp) REFERENCES chi_tiet_san_pham(id);
ALTER TABLE lich_su_hoa_don ADD CONSTRAINT FK_LSHD_HoaDon FOREIGN KEY (id_hoa_don) REFERENCES hoa_don(id);
ALTER TABLE lich_su_hoa_don ADD CONSTRAINT FK_LSHD_NhanVien FOREIGN KEY (id_nhan_vien) REFERENCES nhan_vien(user_id);
ALTER TABLE chi_tiet_thanh_toan ADD CONSTRAINT FK_ChiTietThanhToan_HoaDon FOREIGN KEY (id_hoa_don) REFERENCES hoa_don(id);
ALTER TABLE chi_tiet_thanh_toan ADD CONSTRAINT FK_ChiTietThanhToan_PTTT FOREIGN KEY (phuong_thuc_thanh_toan_id) REFERENCES phuong_thuc_thanh_toan(id);

-- Promotion
ALTER TABLE dot_giam_gia_chi_tiet ADD CONSTRAINT FK_DGGC_DotGiamGia FOREIGN KEY (id_km) REFERENCES dot_giam_gia(id);
ALTER TABLE dot_giam_gia_chi_tiet ADD CONSTRAINT FK_DGGC_CTSP FOREIGN KEY (id_ctsp) REFERENCES chi_tiet_san_pham(id);

-- After-Sales
ALTER TABLE serial_da_ban ADD CONSTRAINT FK_SerialDaBan_HDCT FOREIGN KEY (id_hoa_don_chi_tiet) REFERENCES hoa_don_chi_tiet(id);
ALTER TABLE serial_da_ban ADD CONSTRAINT FK_SerialDaBan_Serial FOREIGN KEY (id_serial) REFERENCES serial(id);
ALTER TABLE phieu_bao_hanh ADD CONSTRAINT FK_PhieuBaoHanh_KhachHang FOREIGN KEY (id_khach_hang) REFERENCES khach_hang(user_id);
ALTER TABLE phieu_bao_hanh ADD CONSTRAINT FK_PhieuBaoHanh_SerialDaBan FOREIGN KEY (id_serial_da_ban) REFERENCES serial_da_ban(id);
ALTER TABLE lich_su_bao_hanh ADD CONSTRAINT FK_LSBH_PhieuBaoHanh FOREIGN KEY (id_bao_hanh) REFERENCES phieu_bao_hanh(id);
ALTER TABLE danh_gia ADD CONSTRAINT FK_DanhGia_KhachHang FOREIGN KEY (khach_hang_id) REFERENCES khach_hang(user_id);
ALTER TABLE danh_gia ADD CONSTRAINT FK_DanhGia_CTSP FOREIGN KEY (san_pham_chi_tiet_id) REFERENCES chi_tiet_san_pham(id);
ALTER TABLE danh_gia ADD CONSTRAINT FK_DanhGia_HDCT FOREIGN KEY (hoa_don_chi_tiet_id) REFERENCES hoa_don_chi_tiet(id);
ALTER TABLE media_danh_gia ADD CONSTRAINT FK_MediaDanhGia_DanhGia FOREIGN KEY (danh_gia_id) REFERENCES danh_gia(id);
ALTER TABLE phan_hoi_danh_gia ADD CONSTRAINT FK_PhanHoiDanhGia_DanhGia FOREIGN KEY (danh_gia_id) REFERENCES danh_gia(id);
ALTER TABLE phan_hoi_danh_gia ADD CONSTRAINT FK_PhanHoiDanhGia_NhanVien FOREIGN KEY (nhan_vien_id) REFERENCES nhan_vien(user_id);
ALTER TABLE chat ADD CONSTRAINT FK_Chat_KhachHang FOREIGN KEY (khach_hang_id) REFERENCES khach_hang(user_id);
ALTER TABLE chat ADD CONSTRAINT FK_Chat_NhanVien FOREIGN KEY (nhan_vien_id) REFERENCES nhan_vien(user_id);

-- Point System
ALTER TABLE tich_diem ADD CONSTRAINT FK_TichDiem_KhachHang FOREIGN KEY (user_id) REFERENCES khach_hang(user_id);
ALTER TABLE lich_su_diem ADD CONSTRAINT FK_LichSuDiem_TichDiem FOREIGN KEY (tich_diem_id) REFERENCES tich_diem(id);
ALTER TABLE lich_su_diem ADD CONSTRAINT FK_LichSuDiem_QuyDoiDiem FOREIGN KEY (id_quy_doi_diem) REFERENCES quy_doi_diem(id);
ALTER TABLE lich_su_diem ADD CONSTRAINT FK_LichSuDiem_HoaDon FOREIGN KEY (hoa_don_id) REFERENCES hoa_don(id);
ALTER TABLE chi_tiet_lich_su_diem ADD CONSTRAINT FK_CTLSD_KhachHang FOREIGN KEY (user_id) REFERENCES khach_hang(user_id);
ALTER TABLE chi_tiet_lich_su_diem ADD CONSTRAINT FK_CTLSD_LichSuDiem FOREIGN KEY (lich_su_diem_id) REFERENCES lich_su_diem(id);

-- Category
ALTER TABLE sanpham_danhmuc ADD CONSTRAINT FK_SPDM_SanPham FOREIGN KEY (san_pham_id) REFERENCES san_pham(id);
ALTER TABLE sanpham_danhmuc ADD CONSTRAINT FK_SPDM_DanhMuc FOREIGN KEY (danh_muc_id) REFERENCES danh_muc(id);

GO

-- ===================================================================================
-- X. TẠO INDEXES ĐỂ TỐI ƯU HIỆU NĂNG
-- ===================================================================================

-- Thiết lập SET options cho indexes
SET QUOTED_IDENTIFIER ON;
GO

-- Index: Đảm bảo mỗi serial đã bán chỉ có 1 phiếu bảo hành (1-1 relationship)
CREATE UNIQUE INDEX UX_PBH_OnePerSdb
ON dbo.phieu_bao_hanh(id_serial_da_ban)
WHERE id_serial_da_ban IS NOT NULL;

PRINT 'Đã tạo UNIQUE INDEX UX_PBH_OnePerSdb thành công.';
GO

-- Index: Đảm bảo mỗi nhân viên chỉ có một tài khoản (ma_tai_khoan không được trùng lặp khi NOT NULL)
CREATE UNIQUE INDEX UX_NhanVien_MaTaiKhoan_NotNull
ON dbo.nhan_vien(ma_tai_khoan)
WHERE ma_tai_khoan IS NOT NULL;

PRINT 'Đã tạo UNIQUE INDEX UX_NhanVien_MaTaiKhoan_NotNull thành công.';
GO

-- Index: Đảm bảo mỗi khách hàng chỉ có một tài khoản (ma_tai_khoan không được trùng lặp khi NOT NULL)
CREATE UNIQUE INDEX UQ_khach_hang_ma_tai_khoan_notnull
ON dbo.khach_hang(ma_tai_khoan)
WHERE ma_tai_khoan IS NOT NULL;

PRINT 'Đã tạo UNIQUE INDEX UQ_khach_hang_ma_tai_khoan_notnull thành công.';
GO

-- Indexes cho bảng hoa_don: Tối ưu query đơn hàng online
-- Index: Tối ưu query đơn hàng online chờ xác nhận
CREATE NONCLUSTERED INDEX IX_HoaDon_TrangThai_LoaiHoaDon
ON dbo.hoa_don(trang_thai, loai_hoa_don)
INCLUDE (id, ma, id_khach_hang, ngay_tao, tong_tien_sau_giam)
WHERE loai_hoa_don = 1; -- Chỉ index cho đơn hàng online (giao hàng)

PRINT 'Đã tạo INDEX IX_HoaDon_TrangThai_LoaiHoaDon thành công.';
GO

-- Index: Tối ưu query đơn hàng theo khách hàng
CREATE NONCLUSTERED INDEX IX_HoaDon_KhachHang_TrangThai
ON dbo.hoa_don(id_khach_hang, trang_thai)
INCLUDE (id, ma, ngay_tao, tong_tien_sau_giam);

PRINT 'Đã tạo INDEX IX_HoaDon_KhachHang_TrangThai thành công.';
GO

-- Index: Tối ưu query đơn hàng theo ngày tạo (để filter đơn mới)
CREATE NONCLUSTERED INDEX IX_HoaDon_NgayTao_TrangThai
ON dbo.hoa_don(ngay_tao DESC, trang_thai)
INCLUDE (id, ma, id_khach_hang, loai_hoa_don);

PRINT 'Đã tạo INDEX IX_HoaDon_NgayTao_TrangThai thành công.';
GO

-- ===================================================================================
-- XI. INSERT DỮ LIỆU MẪU (DỮ LIỆU MỚI TINH)
-- ===================================================================================

-- 1. Bảng cơ bản (không có khóa ngoại)
INSERT INTO phuong_thuc_thanh_toan (ten_phuong_thuc, loai_phuong_thuc) VALUES
('Tiền mặt', 'Cash'),
('Chuyển khoản ngân hàng', 'Bank Transfer'),
('Ví điện tử Momo', 'E-Wallet'),
('Thẻ tín dụng', 'Credit Card'),
('Thẻ ATM', 'Debit Card');

INSERT INTO cpu (ma_cpu, ten_cpu, mo_ta, trang_thai) VALUES
('CPU001', 'Intel Core i5-12400F', 'Bộ xử lý Intel thế hệ 12, 6 nhân 12 luồng', 1),
('CPU002', 'AMD Ryzen 5 5600G', 'Bộ xử lý AMD với GPU tích hợp', 1),
('CPU003', 'Intel Core i7-12700K', 'Bộ xử lý Intel cao cấp, 12 nhân 20 luồng', 1),
('CPU004', 'AMD Ryzen 7 5800H', 'Bộ xử lý AMD cho laptop gaming', 1),
('CPU005', 'Intel Core i9-12900K', 'Bộ xử lý Intel flagship, 16 nhân 24 luồng', 1);

INSERT INTO ram (ma_ram, ten_ram, mo_ta, trang_thai) VALUES
('RAM001', 'DDR4 8GB 3200MHz', 'RAM DDR4 8GB tốc độ 3200MHz', 1),
('RAM002', 'DDR4 16GB 3200MHz', 'RAM DDR4 16GB tốc độ 3200MHz', 1),
('RAM003', 'DDR5 16GB 4800MHz', 'RAM DDR5 16GB tốc độ 4800MHz', 1),
('RAM004', 'DDR4 32GB 2666MHz', 'RAM DDR4 32GB tốc độ 2666MHz', 1),
('RAM005', 'DDR5 32GB 5200MHz', 'RAM DDR5 32GB tốc độ 5200MHz', 1);

INSERT INTO o_cung (ma_o_cung, dung_luong, mo_ta, trang_thai) VALUES
('SSD001', '256GB SSD NVMe', 'Ổ cứng SSD NVMe 256GB', 1),
('SSD002', '512GB SSD NVMe', 'Ổ cứng SSD NVMe 512GB', 1),
('SSD003', '1TB SSD NVMe', 'Ổ cứng SSD NVMe 1TB', 1),
('HDD001', '1TB HDD 7200RPM', 'Ổ cứng HDD 1TB tốc độ 7200RPM', 1),
('SSD004', '2TB SSD NVMe', 'Ổ cứng SSD NVMe 2TB', 1);

INSERT INTO gpu (ma_gpu, ten_gpu, mo_ta, trang_thai) VALUES
('GPU001', 'NVIDIA GeForce RTX 3060', 'Card đồ họa NVIDIA RTX 3060 12GB', 1),
('GPU002', 'AMD Radeon RX 6600 XT', 'Card đồ họa AMD RX 6600 XT 8GB', 1),
('GPU003', 'NVIDIA GeForce RTX 4060', 'Card đồ họa NVIDIA RTX 4060 8GB', 1),
('GPU004', 'Intel Arc A770', 'Card đồ họa Intel Arc A770 16GB', 1),
('GPU005', 'NVIDIA GeForce RTX 4070', 'Card đồ họa NVIDIA RTX 4070 12GB', 1);

INSERT INTO loai_man_hinh (ma_loai_man_hinh, kich_thuoc, mo_ta, trang_thai) VALUES
('MH001', '15.6 inch FHD', 'Màn hình 15.6 inch độ phân giải Full HD', 1),
('MH002', '17.3 inch FHD', 'Màn hình 17.3 inch độ phân giải Full HD', 1),
('MH003', '14 inch FHD', 'Màn hình 14 inch độ phân giải Full HD', 1),
('MH004', '15.6 inch QHD', 'Màn hình 15.6 inch độ phân giải QHD', 1),
('MH005', '16 inch 4K', 'Màn hình 16 inch độ phân giải 4K', 1);

INSERT INTO pin (ma_pin, dung_luong_pin, mo_ta, trang_thai) VALUES
('PIN001', '45Wh', 'Pin lithium-ion 45Wh', 1),
('PIN002', '60Wh', 'Pin lithium-ion 60Wh', 1),
('PIN003', '90Wh', 'Pin lithium-ion 90Wh', 1),
('PIN004', '75Wh', 'Pin lithium-ion 75Wh', 1),
('PIN005', '99Wh', 'Pin lithium-ion 99Wh', 1);

INSERT INTO mau_sac (ma_mau, ten_mau, mo_ta, trang_thai) VALUES
('MS001', 'Đen', 'Màu đen bóng', 1),
('MS002', 'Bạc', 'Màu bạc sang trọng', 1),
('MS003', 'Xám', 'Màu xám hiện đại', 1),
('MS004', 'Trắng', 'Màu trắng tinh khiết', 1),
('MS005', 'Xanh Navy', 'Màu xanh navy chuyên nghiệp', 1);

INSERT INTO vai_tro (ma_vai_tro, ten_vai_tro, mo_ta) VALUES
('ADMIN', 'Quản trị viên', 'Có toàn quyền quản lý hệ thống'),
('MANAGER', 'Quản lý', 'Quản lý bán hàng và nhân viên'),
('STAFF', 'Nhân viên bán hàng', 'Thực hiện bán hàng và chăm sóc khách hàng'),
('CASHIER', 'Thu ngân', 'Xử lý thanh toán và hóa đơn'),
('CUSTOMER', 'Khách hàng', 'Người dùng cuối mua sản phẩm');

INSERT INTO quy_doi_diem (tien_tich_diem, tien_tieu_diem, trang_thai) VALUES
(100000, 1000, 1),
(200000, 2000, 1),
(500000, 5000, 1),
(1000000, 10000, 1),
(2000000, 20000, 1);

INSERT INTO san_pham (ma_san_pham, ten_san_pham, mo_ta, trang_thai, ngay_tao, ngay_sua, nguoi_tao, nguoi_sua) VALUES
('SP001', 'Laptop Gaming ASUS ROG Strix G15', 'Laptop gaming hiệu năng cao với card đồ họa RTX 3060', 1, GETDATE(), GETDATE(), 'admin', 'admin'),
('SP002', 'Laptop Dell XPS 13', 'Laptop cao cấp thiết kế sang trọng, màn hình 4K', 1, GETDATE(), GETDATE(), 'admin', 'admin'),
('SP003', 'Laptop MacBook Pro M2', 'Laptop Apple với chip M2 mạnh mẽ, hiệu năng vượt trội', 1, GETDATE(), GETDATE(), 'admin', 'admin'),
('SP004', 'Laptop HP Pavilion 15', 'Laptop văn phòng với giá cả hợp lý, hiệu năng ổn định', 1, GETDATE(), GETDATE(), 'admin', 'admin'),
('SP005', 'Laptop Lenovo ThinkPad X1', 'Laptop doanh nhân cao cấp, bền bỉ và an toàn', 1, GETDATE(), GETDATE(), 'admin', 'admin');

INSERT INTO phieu_giam_gia (ma, ten_phieu_giam_gia, loai_phieu_giam_gia, gia_tri_giam_gia, so_tien_giam_toi_da, hoa_don_toi_thieu, so_luong_dung, ngay_bat_dau, ngay_ket_thuc, rieng_tu, mo_ta, trang_thai) VALUES
('PGG001', 'Giảm 10% cho đơn hàng từ 10 triệu', 1, 10.00, 2000000, 10000000, 100, '2024-01-01', '2024-12-31', 0, 'Giảm giá 10% tối đa 2 triệu cho đơn hàng từ 10 triệu', 1),
('PGG002', 'Giảm 500k cho đơn hàng từ 5 triệu', 2, 500000, 500000, 5000000, 50, '2024-01-01', '2024-12-31', 0, 'Giảm cố định 500k cho đơn hàng từ 5 triệu', 1),
('PGG003', 'Giảm 20% cho khách VIP', 1, 20.00, 5000000, 20000000, 10, '2024-01-01', '2024-12-31', 1, 'Giảm giá 20% tối đa 5 triệu cho khách VIP', 1),
('PGG004', 'Giảm 1 triệu cho laptop gaming', 2, 1000000, 1000000, 15000000, 30, '2024-01-01', '2024-12-31', 0, 'Giảm cố định 1 triệu cho laptop gaming', 1),
('PGG005', 'Giảm 15% cuối tuần', 1, 15.00, 3000000, 8000000, 200, '2024-01-01', '2024-12-31', 0, 'Giảm giá 15% tối đa 3 triệu cho đơn hàng cuối tuần', 1);

INSERT INTO dot_giam_gia (ten_km, loai_dot_giam_gia, gia_tri, so_tien_giam_toi_da, mo_ta, ngayBatDau, ngayKetThuc, trang_thai, bannerImageUrl) VALUES
('Khuyến mãi Black Friday 2024', 1, 30.00, 5000000.00, 'Giảm giá 30% cho tất cả sản phẩm trong ngày Black Friday (tối đa 5 triệu)', '2024-11-29', '2024-11-29', 1, NULL),
('Khuyến mãi mùa hè', 1, 20.00, 3000000.00, 'Giảm giá 20% cho laptop gaming trong mùa hè (tối đa 3 triệu)', '2024-06-01', '2024-08-31', 1, NULL),
('Khuyến mãi sinh nhật', 1, 15.00, 2000000.00, 'Giảm giá 15% cho khách hàng trong tháng sinh nhật (tối đa 2 triệu)', '2024-01-01', '2024-12-31', 1, NULL),
('Khuyến mãi laptop văn phòng', 1, 25.00, 4000000.00, 'Giảm giá 25% cho laptop văn phòng (tối đa 4 triệu)', '2024-03-01', '2024-03-31', 1, NULL),
('Khuyến mãi cuối năm', 1, 35.00, 6000000.00, 'Giảm giá 35% cho đơn hàng cuối năm (tối đa 6 triệu)', '2024-12-15', '2024-12-31', 1, NULL);

-- 2. Tài khoản
INSERT INTO tai_khoan (ma_vai_tro, ten_dang_nhap, mat_khau, email, trang_thai, ngay_tao, lan_dang_nhap_cuoi) VALUES
((SELECT id FROM vai_tro WHERE ma_vai_tro = 'ADMIN'), 'admin', 'admin123', 'admin@laptopstore.com', 1, GETDATE(), GETDATE()),
((SELECT id FROM vai_tro WHERE ma_vai_tro = 'MANAGER'), 'manager01', 'manager123', 'manager@laptopstore.com', 1, GETDATE(), GETDATE()),
((SELECT id FROM vai_tro WHERE ma_vai_tro = 'STAFF'), 'staff01', 'staff123', 'staff@laptopstore.com', 1, GETDATE(), GETDATE()),
((SELECT id FROM vai_tro WHERE ma_vai_tro = 'CASHIER'), 'cashier01', 'cashier123', 'cashier@laptopstore.com', 1, GETDATE(), GETDATE()),
((SELECT id FROM vai_tro WHERE ma_vai_tro = 'CUSTOMER'), 'customer01', 'customer123', 'customer1@email.com', 1, GETDATE(), GETDATE()),
((SELECT id FROM vai_tro WHERE ma_vai_tro = 'CUSTOMER'), 'customer02', 'customer123', 'customer2@email.com', 1, GETDATE(), GETDATE()),
((SELECT id FROM vai_tro WHERE ma_vai_tro = 'CUSTOMER'), 'customer03', 'customer123', 'customer3@email.com', 1, GETDATE(), GETDATE()),
((SELECT id FROM vai_tro WHERE ma_vai_tro = 'CUSTOMER'), 'customer04', 'customer123', 'customer4@email.com', 1, GETDATE(), GETDATE()),
((SELECT id FROM vai_tro WHERE ma_vai_tro = 'CUSTOMER'), 'customer05', 'customer123', 'customer5@email.com', 1, GETDATE(), GETDATE());

-- 3. Khách hàng và Nhân viên
INSERT INTO khach_hang (ma_tai_khoan, ma_khach_hang, ho_ten, so_dien_thoai, email, gioi_tinh, ngay_sinh, trang_thai) VALUES
((SELECT id FROM tai_khoan WHERE ten_dang_nhap = 'customer01'), 'KH001', 'Nguyễn Văn An', '0901234567', 'an.nguyen@email.com', 1, '1990-05-15', 1),
((SELECT id FROM tai_khoan WHERE ten_dang_nhap = 'customer02'), 'KH002', 'Trần Thị Bình', '0901234568', 'binh.tran@email.com', 0, '1992-08-20', 1),
((SELECT id FROM tai_khoan WHERE ten_dang_nhap = 'customer03'), 'KH003', 'Lê Văn Cường', '0901234569', 'cuong.le@email.com', 1, '1988-12-10', 1),
((SELECT id FROM tai_khoan WHERE ten_dang_nhap = 'customer04'), 'KH004', 'Phạm Thị Dung', '0901234570', 'dung.pham@email.com', 0, '1995-03-25', 1),
((SELECT id FROM tai_khoan WHERE ten_dang_nhap = 'customer05'), 'KH005', 'Hoàng Văn Em', '0901234571', 'em.hoang@email.com', 1, '1993-07-18', 1);

INSERT INTO nhan_vien (ma_tai_khoan, ma_nhan_vien, ho_ten, so_dien_thoai, email, gioi_tinh, anh_nhan_vien, chuc_vu, dia_chi, danh_gia, trang_thai) VALUES
((SELECT id FROM tai_khoan WHERE ten_dang_nhap = 'manager01'), 'NV001', 'Nguyễn Thị Phương', '0901234572', 'phuong.nguyen@laptopstore.com', 0, NULL, 'Quản lý bán hàng', '123 Đường ABC, Quận 1, TP.HCM', 'Xuất sắc', 1),
((SELECT id FROM tai_khoan WHERE ten_dang_nhap = 'staff01'), 'NV002', 'Trần Văn Giang', '0901234573', 'giang.tran@laptopstore.com', 1, NULL, 'Nhân viên bán hàng', '456 Đường DEF, Quận 2, TP.HCM', 'Tốt', 1),
((SELECT id FROM tai_khoan WHERE ten_dang_nhap = 'cashier01'), 'NV003', 'Lê Thị Hoa', '0901234574', 'hoa.le@laptopstore.com', 0, NULL, 'Thu ngân', '789 Đường GHI, Quận 3, TP.HCM', 'Tốt', 1);

-- 4. Các bảng phụ thuộc vào khách hàng
INSERT INTO dia_chi (user_id, dia_chi, ho_ten, so_dien_thoai, xa, tinh, mac_dinh) VALUES
((SELECT user_id FROM khach_hang WHERE ma_khach_hang = 'KH001'), '123 Đường Lê Lợi, Quận 1, TP.HCM', 'Nguyễn Văn A', '0901234567', 'Phường Bến Nghé', 'Thành phố Hồ Chí Minh', 1),
((SELECT user_id FROM khach_hang WHERE ma_khach_hang = 'KH002'), '456 Đường Nguyễn Huệ, Quận 1, TP.HCM', 'Trần Thị B', '0901234568', 'Phường Đa Kao', 'Thành phố Hồ Chí Minh', 1),
((SELECT user_id FROM khach_hang WHERE ma_khach_hang = 'KH003'), '789 Đường Đồng Khởi, Quận 1, TP.HCM', 'Lê Văn C', '0901234569', 'Phường Bến Thành', 'Thành phố Hồ Chí Minh', 1);

INSERT INTO tich_diem (user_id, diem_da_dung, diem_da_cong, tong_diem) VALUES
((SELECT user_id FROM khach_hang WHERE ma_khach_hang = 'KH001'), 500, 1500, 1000),
((SELECT user_id FROM khach_hang WHERE ma_khach_hang = 'KH002'), 200, 800, 600),
((SELECT user_id FROM khach_hang WHERE ma_khach_hang = 'KH003'), 0, 300, 300);

-- 5. Chi tiết sản phẩm, serial và các bảng liên quan
INSERT INTO chi_tiet_san_pham (sp_id, cpu_id, ram_id, o_cung_id, gpu_id, loai_man_hinh_id, pin_id, mau_sac_id, ma_ctsp, gia_ban, ghi_chu, trang_thai, so_luong_ton, ngay_tao, ngay_sua) VALUES
((SELECT id FROM san_pham WHERE ma_san_pham = 'SP001'), (SELECT id FROM cpu WHERE ma_cpu = 'CPU001'), (SELECT id FROM ram WHERE ma_ram = 'RAM002'), (SELECT id FROM o_cung WHERE ma_o_cung = 'SSD002'), (SELECT id FROM gpu WHERE ma_gpu = 'GPU001'), (SELECT id FROM loai_man_hinh WHERE ma_loai_man_hinh = 'MH001'), (SELECT id FROM pin WHERE ma_pin = 'PIN002'), (SELECT id FROM mau_sac WHERE ma_mau = 'MS001'), 'CTSP001', 25990000, 'Laptop gaming phù hợp cho game thủ', 1, 50, GETDATE(), GETDATE()),
((SELECT id FROM san_pham WHERE ma_san_pham = 'SP002'), (SELECT id FROM cpu WHERE ma_cpu = 'CPU003'), (SELECT id FROM ram WHERE ma_ram = 'RAM002'), (SELECT id FROM o_cung WHERE ma_o_cung = 'SSD003'), (SELECT id FROM gpu WHERE ma_gpu = 'GPU001'), (SELECT id FROM loai_man_hinh WHERE ma_loai_man_hinh = 'MH005'), (SELECT id FROM pin WHERE ma_pin = 'PIN001'), (SELECT id FROM mau_sac WHERE ma_mau = 'MS002'), 'CTSP002', 35990000, 'Laptop cao cấp cho công việc chuyên nghiệp', 1, 30, GETDATE(), GETDATE()),
((SELECT id FROM san_pham WHERE ma_san_pham = 'SP003'), (SELECT id FROM cpu WHERE ma_cpu = 'CPU004'), (SELECT id FROM ram WHERE ma_ram = 'RAM003'), (SELECT id FROM o_cung WHERE ma_o_cung = 'SSD003'), (SELECT id FROM gpu WHERE ma_gpu = 'GPU002'), (SELECT id FROM loai_man_hinh WHERE ma_loai_man_hinh = 'MH004'), (SELECT id FROM pin WHERE ma_pin = 'PIN003'), (SELECT id FROM mau_sac WHERE ma_mau = 'MS003'), 'CTSP003', 45990000, 'Laptop Apple với hiệu năng vượt trội', 1, 20, GETDATE(), GETDATE());

INSERT INTO hinh_anh (id_spct, url, anh_chinh_dai_dien, ngay_tao, ngay_sua) VALUES
((SELECT id FROM chi_tiet_san_pham WHERE ma_ctsp = 'CTSP001'), 'https://example.com/images/asus-rog-strix-1.jpg', 1, GETDATE(), GETDATE()),
((SELECT id FROM chi_tiet_san_pham WHERE ma_ctsp = 'CTSP001'), 'https://example.com/images/asus-rog-strix-2.jpg', 0, GETDATE(), GETDATE());

INSERT INTO serial (ctsp_id, serial_no, trang_thai, ngay_nhap) VALUES
((SELECT id FROM chi_tiet_san_pham WHERE ma_ctsp = 'CTSP001'), 'SN001', 0, GETDATE()),
((SELECT id FROM chi_tiet_san_pham WHERE ma_ctsp = 'CTSP001'), 'SN002', 0, GETDATE()),
((SELECT id FROM chi_tiet_san_pham WHERE ma_ctsp = 'CTSP001'), 'SN003', 0, GETDATE()),
((SELECT id FROM chi_tiet_san_pham WHERE ma_ctsp = 'CTSP002'), 'SN004', 0, GETDATE()),
((SELECT id FROM chi_tiet_san_pham WHERE ma_ctsp = 'CTSP002'), 'SN005', 0, GETDATE()),
((SELECT id FROM chi_tiet_san_pham WHERE ma_ctsp = 'CTSP003'), 'SN006', 0, GETDATE());

-- 6. Danh mục
INSERT INTO danh_muc (ma_danh_muc, ten_danh_muc, mo_ta, trang_thai) VALUES
('DM001', 'Laptop Gaming', 'Danh mục laptop chuyên dụng cho gaming', 1),
('DM002', 'Laptop Văn phòng', 'Danh mục laptop phù hợp cho công việc văn phòng', 1),
('DM003', 'Laptop Cao cấp', 'Danh mục laptop cao cấp, sang trọng', 1),
('DM004', 'Laptop Đồ họa', 'Danh mục laptop chuyên dụng cho đồ họa', 1),
('DM005', 'Laptop Sinh viên', 'Danh mục laptop giá rẻ phù hợp cho sinh viên', 1);

INSERT INTO sanpham_danhmuc (san_pham_id, danh_muc_id) VALUES
((SELECT id FROM san_pham WHERE ma_san_pham = 'SP001'), (SELECT id FROM danh_muc WHERE ma_danh_muc = 'DM001')),
((SELECT id FROM san_pham WHERE ma_san_pham = 'SP002'), (SELECT id FROM danh_muc WHERE ma_danh_muc = 'DM003')),
((SELECT id FROM san_pham WHERE ma_san_pham = 'SP003'), (SELECT id FROM danh_muc WHERE ma_danh_muc = 'DM003')),
((SELECT id FROM san_pham WHERE ma_san_pham = 'SP004'), (SELECT id FROM danh_muc WHERE ma_danh_muc = 'DM002')),
((SELECT id FROM san_pham WHERE ma_san_pham = 'SP005'), (SELECT id FROM danh_muc WHERE ma_danh_muc = 'DM003'));

GO

-- ===================================================================================
-- XI.1. XỬ LÝ CỘT GIA_NHAP CHO CHI_TIET_SAN_PHAM
-- ===================================================================================

-- Cập nhật giá nhập cho các chi tiết sản phẩm đã có
-- Giá nhập thường bằng 70-80% giá bán
UPDATE chi_tiet_san_pham
SET gia_nhap = gia_ban * 0.75
WHERE gia_nhap IS NULL;

PRINT 'Đã cập nhật giá nhập cho các sản phẩm hiện có';
GO

-- ===================================================================================
-- XI.2. CẬP NHẬT VERSION CHO CHI_TIET_SAN_PHAM
-- ===================================================================================

-- Đảm bảo tất cả record có version không null (yêu cầu cho Hibernate @Version)
UPDATE chi_tiet_san_pham
SET version = 0
WHERE version IS NULL;

PRINT 'Đã cập nhật version cho các chi tiết sản phẩm hiện có';
GO

PRINT '===================================================================================';
PRINT 'HOÀN TẤT TẠO DATABASE VÀ DỮ LIỆU MẪU';
PRINT 'Database: QuanLyBanHangLaptop_TheoERD1_New';
PRINT 'Tất cả các bảng, constraints, indexes và dữ liệu mẫu đã được tạo thành công!';
PRINT '===================================================================================';

-- ===================================================================================
-- CẬP NHẬT ROLE: Chuẩn hóa về 3 role chính (ADMIN, NHAN_VIEN, KHACH_HANG)
-- Chạy script này sau khi database đã chạy để update role
-- Lưu ý: Script này được thêm vào cuối file để không ảnh hưởng đến database đang chạy
-- ===================================================================================

PRINT 'Bắt đầu cập nhật role...';
GO

-- Bước 1: Đảm bảo có đủ 3 vai trò chính
IF NOT EXISTS (SELECT 1 FROM vai_tro WHERE ma_vai_tro = 'NHAN_VIEN')
BEGIN
    INSERT INTO vai_tro (ma_vai_tro, ten_vai_tro, mo_ta) 
    VALUES ('NHAN_VIEN', 'Nhân viên', 'Nhân viên bán hàng và quản lý');
    PRINT 'Đã tạo vai trò NHAN_VIEN';
END
ELSE
BEGIN
    PRINT 'Vai trò NHAN_VIEN đã tồn tại';
END
GO

IF NOT EXISTS (SELECT 1 FROM vai_tro WHERE ma_vai_tro = 'KHACH_HANG')
BEGIN
    INSERT INTO vai_tro (ma_vai_tro, ten_vai_tro, mo_ta) 
    VALUES ('KHACH_HANG', 'Khách hàng', 'Người dùng cuối mua sản phẩm');
    PRINT 'Đã tạo vai trò KHACH_HANG';
END
ELSE
BEGIN
    PRINT 'Vai trò KHACH_HANG đã tồn tại';
END
GO

-- Bước 2: Cập nhật vai trò của các tài khoản hiện có
-- Chuyển STAFF, MANAGER, CASHIER thành NHAN_VIEN
UPDATE tai_khoan 
SET ma_vai_tro = (SELECT id FROM vai_tro WHERE ma_vai_tro = 'NHAN_VIEN')
WHERE ma_vai_tro IN (
    SELECT id FROM vai_tro WHERE ma_vai_tro IN ('STAFF', 'MANAGER', 'CASHIER')
);
PRINT 'Đã cập nhật các tài khoản STAFF/MANAGER/CASHIER thành NHAN_VIEN';
GO

-- Chuyển CUSTOMER thành KHACH_HANG
UPDATE tai_khoan 
SET ma_vai_tro = (SELECT id FROM vai_tro WHERE ma_vai_tro = 'KHACH_HANG')
WHERE ma_vai_tro = (SELECT id FROM vai_tro WHERE ma_vai_tro = 'CUSTOMER');
PRINT 'Đã cập nhật các tài khoản CUSTOMER thành KHACH_HANG';
GO

-- Bước 3: Xóa các vai trò không dùng (optional - comment lại nếu muốn giữ)
-- Lưu ý: Chỉ xóa nếu chắc chắn không còn tài khoản nào sử dụng
/*
DELETE FROM vai_tro WHERE ma_vai_tro IN ('STAFF', 'MANAGER', 'CASHIER', 'CUSTOMER');
PRINT 'Đã xóa các vai trò không dùng';
GO
*/
"ALTER TABLE cpu DROP COLUMN update_at; ALTER TABLE cpu ADD updated_at DATETIME2; ALTER TABLE gpu DROP COLUMN update_at; ALTER TABLE gpu ADD updated_at DATETIME2; ALTER TABLE ram DROP COLUMN update_at; ALTER TABLE ram ADD updated_at DATETIME2; ALTER TABLE o_cung DROP COLUMN update_at; ALTER TABLE o_cung ADD updated_at DATETIME2; ALTER TABLE loai_man_hinh DROP COLUMN update_at; ALTER TABLE loai_man_hinh ADD updated_at DATETIME2; ALTER TABLE pin DROP COLUMN update_at; ALTER TABLE pin ADD updated_at DATETIME2; ALTER TABLE mau_sac DROP COLUMN update_at; ALTER TABLE mau_sac ADD updated_at DATETIME2;"

PRINT 'Hoàn tất cập nhật role!';
PRINT 'Hệ thống hiện có 3 vai trò chính: ADMIN, NHAN_VIEN, KHACH_HANG';
GO

-- ===================================================================================
-- ALTER TABLE dot_giam_gia - Thêm loại giảm giá % và số tiền giảm tối đa
-- Ngày tạo: 2025-01-XX
-- Mô tả: Thêm cột loai_dot_giam_gia và so_tien_giam_toi_da để hỗ trợ giảm giá theo %
-- LƯU Ý: Nếu chạy lại từ đầu (DROP DATABASE), các cột này đã có trong CREATE TABLE
--        Các ALTER này chỉ cần thiết nếu database đã tồn tại từ trước
-- ===================================================================================

-- Kiểm tra và thêm cột loai_dot_giam_gia nếu chưa có
IF NOT EXISTS (
    SELECT 1 FROM sys.columns 
    WHERE object_id = OBJECT_ID('dot_giam_gia') 
    AND name = 'loai_dot_giam_gia'
)
BEGIN
    ALTER TABLE dot_giam_gia
    ADD loai_dot_giam_gia INT NULL;
    PRINT 'Đã thêm cột loai_dot_giam_gia';
END
ELSE
BEGIN
    PRINT 'Cột loai_dot_giam_gia đã tồn tại, bỏ qua';
END
GO

-- Kiểm tra và thêm cột so_tien_giam_toi_da nếu chưa có
IF NOT EXISTS (
    SELECT 1 FROM sys.columns 
    WHERE object_id = OBJECT_ID('dot_giam_gia') 
    AND name = 'so_tien_giam_toi_da'
)
BEGIN
    ALTER TABLE dot_giam_gia
    ADD so_tien_giam_toi_da DECIMAL(18, 2) NULL;
    PRINT 'Đã thêm cột so_tien_giam_toi_da';
END
ELSE
BEGIN
    PRINT 'Cột so_tien_giam_toi_da đã tồn tại, bỏ qua';
END
GO

-- Kiểm tra và thay đổi kiểu dữ liệu của cột gia_tri nếu cần (từ INT sang DECIMAL)
IF EXISTS (
    SELECT 1 FROM sys.columns 
    WHERE object_id = OBJECT_ID('dot_giam_gia') 
    AND name = 'gia_tri'
    AND system_type_id = 56 -- INT
)
BEGIN
    ALTER TABLE dot_giam_gia
    ALTER COLUMN gia_tri DECIMAL(18, 2);
    PRINT 'Đã thay đổi kiểu dữ liệu của cột gia_tri từ INT sang DECIMAL(18, 2)';
END
ELSE
BEGIN
    PRINT 'Cột gia_tri đã là DECIMAL hoặc không tồn tại, bỏ qua';
END
GO

-- Cập nhật dữ liệu cũ: Mặc định loại = 2 (VND) cho các đợt giảm giá hiện có (nếu có dữ liệu cũ)
UPDATE dot_giam_gia
SET loai_dot_giam_gia = 2
WHERE loai_dot_giam_gia IS NULL;
GO

-- Set NOT NULL cho loai_dot_giam_gia sau khi đã cập nhật dữ liệu (nếu cột cho phép NULL)
IF EXISTS (
    SELECT 1 FROM sys.columns 
    WHERE object_id = OBJECT_ID('dot_giam_gia') 
    AND name = 'loai_dot_giam_gia'
    AND is_nullable = 1
)
BEGIN
    ALTER TABLE dot_giam_gia
    ALTER COLUMN loai_dot_giam_gia INT NOT NULL;
    PRINT 'Đã set NOT NULL cho cột loai_dot_giam_gia';
END
ELSE
BEGIN
    PRINT 'Cột loai_dot_giam_gia đã là NOT NULL, bỏ qua';
END
GO

-- Cập nhật so_tien_giam_toi_da = gia_tri cho các đợt giảm giá loại VND (loai = 2)
UPDATE dot_giam_gia
SET so_tien_giam_toi_da = gia_tri
WHERE loai_dot_giam_gia = 2 AND so_tien_giam_toi_da IS NULL;
GO

PRINT 'Hoàn tất cập nhật bảng dot_giam_gia với loại giảm giá % và số tiền giảm tối đa!';
GO


-- ===================================================================================
-- SCRIPT TẠO DATABASE QUẢN LÝ BÁN HÀNG LAPTOP (FULL DATA - 30 SẢN PHẨM DELL)
-- Phiên bản: Complete Fixed Unicode
-- Mô tả: Chạy 1 lần (F5) là có đầy đủ cấu trúc và dữ liệu mẫu (30 SP, Ảnh, Chat, Đơn hàng...)
-- ===================================================================================

USE master;
GO

-- 1. Xóa database cũ để làm sạch
IF DB_ID('QuanLyBanHangLaptop_TheoERD1_New') IS NOT NULL
BEGIN
    ALTER DATABASE QuanLyBanHangLaptop_TheoERD1_New SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE QuanLyBanHangLaptop_TheoERD1_New;
    PRINT '>> Da xoa database cu.';
END
GO

-- 2. Tạo database mới
CREATE DATABASE QuanLyBanHangLaptop_TheoERD1_New;
GO
USE QuanLyBanHangLaptop_TheoERD1_New;
GO
PRINT '>> Da tao database moi.';

-- ===================================================================================
-- I. TẠO CÁC BẢNG (TABLES) VỚI NVARCHAR
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
    trang_thai INT,
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE()
);

CREATE TABLE ram (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    ma_ram VARCHAR(50) UNIQUE,
    ten_ram NVARCHAR(255),
    mo_ta NVARCHAR(MAX),
    trang_thai INT,
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE()
);

CREATE TABLE o_cung (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    ma_o_cung VARCHAR(50) UNIQUE,
    dung_luong NVARCHAR(100),
    mo_ta NVARCHAR(MAX),
    trang_thai INT,
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE()
);

CREATE TABLE gpu (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    ma_gpu VARCHAR(50) UNIQUE,
    ten_gpu NVARCHAR(255),
    mo_ta NVARCHAR(MAX),
    trang_thai INT,
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE()
);

CREATE TABLE loai_man_hinh (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    ma_loai_man_hinh VARCHAR(50) UNIQUE,
    kich_thuoc NVARCHAR(100),
    mo_ta NVARCHAR(MAX),
    trang_thai INT,
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE()
);

CREATE TABLE pin (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    ma_pin VARCHAR(50) UNIQUE,
    dung_luong_pin NVARCHAR(100),
    mo_ta NVARCHAR(MAX),
    trang_thai INT,
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE()
);

CREATE TABLE mau_sac (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    ma_mau VARCHAR(50) UNIQUE,
    ten_mau NVARCHAR(100),
    mo_ta NVARCHAR(MAX),
    trang_thai INT,
    hex_code VARCHAR(7),
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE()
);

-- User & Auth
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
    ma_tai_khoan UNIQUEIDENTIFIER,
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
    ma_tai_khoan UNIQUEIDENTIFIER,
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
    user_id UNIQUEIDENTIFIER,
    dia_chi NVARCHAR(500),
    ho_ten NVARCHAR(255),
    so_dien_thoai NVARCHAR(20),
    xa NVARCHAR(255),
    tinh NVARCHAR(255),
    mac_dinh BIT
);

-- Products
CREATE TABLE san_pham (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    ma_san_pham VARCHAR(50) UNIQUE,
    ten_san_pham NVARCHAR(255),
    mo_ta NVARCHAR(MAX),
    thoi_han_bh_thang INT DEFAULT 12,
    trang_thai INT,
    ngay_tao DATETIME2,
    ngay_sua DATETIME2,
    nguoi_tao NVARCHAR(255),
    nguoi_sua NVARCHAR(255),
    slug VARCHAR(255)
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
    trang_thai INT, -- 0: Trong kho, 1: Da ban
    ngay_nhap DATETIME2
);

-- Sales
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
    tien_khach_dua DECIMAL(18, 2) NULL,
    tien_tra_lai DECIMAL(18, 2) NULL,
    ma_giao_dich VARCHAR(100),
    ghi_chu NVARCHAR(MAX)
);

-- Promotion
CREATE TABLE dot_giam_gia (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    ten_km NVARCHAR(255),
    loai_dot_giam_gia INT,
    gia_tri DECIMAL(18, 2),
    so_tien_giam_toi_da DECIMAL(18, 2) NULL,
    mo_ta NVARCHAR(MAX),
    ngayBatDau DATETIME2,
    ngayKetThuc DATETIME2,
    trang_thai INT,
    bannerImageUrl VARCHAR(255) NULL
);

CREATE TABLE dot_giam_gia_chi_tiet (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    id_km UNIQUEIDENTIFIER,
    id_ctsp UNIQUEIDENTIFIER,
    gia_ban_dau DECIMAL(18, 2),
    gia_sau_khi_giam DECIMAL(18, 2),
    ghi_chu NVARCHAR(MAX)
);

CREATE TABLE phieu_giam_gia_khach_hang (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    phieu_giam_gia_id UNIQUEIDENTIFIER NOT NULL,
    khach_hang_id UNIQUEIDENTIFIER NOT NULL
);

-- Warranty & Chat
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
    trang_thai_bao_hanh INT,
    ma_phieu_bao_hanh VARCHAR(50) UNIQUE,
    id_hoa_don_chi_tiet UNIQUEIDENTIFIER,
    mo_ta NVARCHAR(MAX),
    chi_phi DECIMAL(18, 2) DEFAULT 0,
    so_lan_sua_chua INT DEFAULT 0,
    hinh_anh NVARCHAR(MAX),
    ngay_tao DATETIME2 DEFAULT GETDATE(),
    ngay_cap_nhat DATETIME2
);

CREATE TABLE ly_do_bao_hanh (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    ma_ly_do VARCHAR(50) UNIQUE NOT NULL,
    ten_ly_do NVARCHAR(255) NOT NULL,
    mo_ta NVARCHAR(MAX),
    loai_ly_do NVARCHAR(50),
    is_active BIT DEFAULT 1,
    thu_tu INT DEFAULT 0,
    ngay_tao DATETIME2 DEFAULT GETDATE()
);

CREATE TABLE phieu_hen_bao_hanh (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    id_bao_hanh UNIQUEIDENTIFIER NOT NULL,
    id_nhan_vien_tiep_nhan UNIQUEIDENTIFIER,
    ma_phieu_hen VARCHAR(50) UNIQUE NOT NULL,
    ngay_hen DATETIME2 NOT NULL,
    gio_hen TIME NOT NULL,
    dia_diem NVARCHAR(500),
    ghi_chu NVARCHAR(MAX),
    trang_thai INT DEFAULT 0,
    email_da_gui BIT DEFAULT 0,
    ngay_tao DATETIME2 DEFAULT GETDATE()
);

CREATE TABLE lich_su_bao_hanh (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    id_bao_hanh UNIQUEIDENTIFIER,
    ngay_tiep_nhan DATETIME2,
    ngay_hoan_thanh DATETIME2,
    mo_ta_loi NVARCHAR(MAX),
    trang_thai INT,
    chi_phi DECIMAL(18, 2) DEFAULT 0,
    phuong_thuc_sua_chua NVARCHAR(255),
    ghi_chu_nhan_vien NVARCHAR(MAX),
    id_ly_do_bao_hanh UNIQUEIDENTIFIER,
    id_phieu_hen UNIQUEIDENTIFIER,
    id_nhan_vien_tiep_nhan UNIQUEIDENTIFIER,
    id_nhan_vien_sua_chua UNIQUEIDENTIFIER,
    hinh_anh_truoc NVARCHAR(MAX),
    hinh_anh_sau NVARCHAR(MAX),
    chi_phi_phat_sinh DECIMAL(18, 2) DEFAULT 0,
    da_thanh_toan BIT DEFAULT 0,
    ngay_nhan_hang DATETIME2,
    ngay_ban_giao DATETIME2,
    xac_nhan_khach_hang BIT DEFAULT 0
);

CREATE TABLE dia_diem_bao_hanh (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    ten NVARCHAR(255) NOT NULL,
    dia_chi NVARCHAR(500),
    so_dien_thoai VARCHAR(20),
    email NVARCHAR(255),
    gio_lam_viec NVARCHAR(255),
    is_active BIT DEFAULT 1,
    ngay_tao DATETIME2 DEFAULT GETDATE()
);

CREATE TABLE email_log (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    loai_email VARCHAR(50),
    id_bao_hanh UNIQUEIDENTIFIER,
    email_nguoi_nhan NVARCHAR(255),
    tieu_de NVARCHAR(500),
    noi_dung NVARCHAR(MAX),
    trang_thai INT DEFAULT 0,
    ngay_gui DATETIME2,
    loi_gui NVARCHAR(MAX)
);

-- Reviews
CREATE TABLE danh_gia (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    khach_hang_id UNIQUEIDENTIFIER,
    san_pham_chi_tiet_id UNIQUEIDENTIFIER,
    hoa_don_chi_tiet_id UNIQUEIDENTIFIER,
    so_sao INT,
    noi_dung NVARCHAR(MAX),
    ngay_danh_gia DATETIME2,
    trang_thai_danh_gia INT,
    is_verified_purchase BIT DEFAULT 0,
    helpful_count INT DEFAULT 0,
    review_title NVARCHAR(255),
    pros NVARCHAR(MAX),
    cons NVARCHAR(MAX)
);

CREATE TABLE helpful_votes (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    danh_gia_id UNIQUEIDENTIFIER NOT NULL,
    khach_hang_id UNIQUEIDENTIFIER NOT NULL,
    is_helpful BIT NOT NULL,
    ngay_vote DATETIME2 DEFAULT GETDATE()
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

-- Chat System
CREATE TABLE chat (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    khach_hang_id UNIQUEIDENTIFIER,
    nhan_vien_id UNIQUEIDENTIFIER,
    noi_dung NVARCHAR(MAX),
    ngay_phan_hoi DATETIME2 DEFAULT GETDATE(),
    is_from_customer BIT DEFAULT 0,
    is_read BIT DEFAULT 0,
    conversation_id UNIQUEIDENTIFIER,
    message_type NVARCHAR(50) DEFAULT 'text',
    file_url NVARCHAR(500),
    reply_to_id UNIQUEIDENTIFIER,
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE(),
    is_bot_message BIT DEFAULT 0,
    bot_confidence DECIMAL(3,2),
    intent_detected NVARCHAR(50),
    requires_human_review BIT DEFAULT 0
);

-- Chatbot AI Tables
CREATE TABLE chat_intents (
    id INT IDENTITY(1,1) PRIMARY KEY,
    intent_code NVARCHAR(50) NOT NULL UNIQUE,
    intent_name NVARCHAR(100) NOT NULL,
    category NVARCHAR(50) NOT NULL,
    keywords NVARCHAR(MAX),
    sample_questions NVARCHAR(MAX),
    auto_response_template NVARCHAR(MAX),
    confidence_threshold DECIMAL(3,2) DEFAULT 0.70,
    requires_data BIT DEFAULT 0,
    data_source NVARCHAR(100),
    is_active BIT DEFAULT 1,
    priority INT DEFAULT 0,
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE()
);

CREATE TABLE chat_quick_replies (
    id INT IDENTITY(1,1) PRIMARY KEY,
    intent_code NVARCHAR(50),
    reply_text NVARCHAR(200) NOT NULL,
    reply_value NVARCHAR(500),
    reply_type NVARCHAR(50) DEFAULT 'text',
    display_order INT DEFAULT 0,
    icon NVARCHAR(50),
    is_active BIT DEFAULT 1,
    created_at DATETIME2 DEFAULT GETDATE()
);

CREATE TABLE chat_sessions (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    conversation_id UNIQUEIDENTIFIER NOT NULL,
    khach_hang_id UNIQUEIDENTIFIER,
    current_intent NVARCHAR(50),
    context_data NVARCHAR(MAX),
    is_bot_handling BIT DEFAULT 1,
    is_escalated BIT DEFAULT 0,
    escalation_reason NVARCHAR(200),
    nhan_vien_id UNIQUEIDENTIFIER,
    bot_satisfaction_rating INT,
    started_at DATETIME2 DEFAULT GETDATE(),
    escalated_at DATETIME2,
    resolved_at DATETIME2,
    last_activity DATETIME2 DEFAULT GETDATE()
);

CREATE TABLE chat_analytics (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    conversation_id UNIQUEIDENTIFIER,
    message_id UNIQUEIDENTIFIER,
    intent_detected NVARCHAR(50),
    confidence_score DECIMAL(3,2),
    was_auto_responded BIT DEFAULT 0,
    was_helpful BIT,
    response_time_ms INT,
    created_at DATETIME2 DEFAULT GETDATE()
);

-- Points
CREATE TABLE tich_diem (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    user_id UNIQUEIDENTIFIER UNIQUE,
    diem_da_dung INT,
    diem_da_cong INT,
    tong_diem INT,
    trang_thai INT DEFAULT 1
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
    user_id UNIQUEIDENTIFIER,
    lich_su_diem_id UNIQUEIDENTIFIER,
    so_diem_da_tru INT,
    ngay_tru DATETIME2
);

-- Categories
CREATE TABLE danh_muc (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    ma_danh_muc VARCHAR(50) UNIQUE,
    ten_danh_muc NVARCHAR(255),
    mo_ta NVARCHAR(MAX),
    trang_thai INT,
    slug VARCHAR(255),
    icon_url VARCHAR(MAX),
    featured BIT DEFAULT 0
);

CREATE TABLE sanpham_danhmuc (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    san_pham_id UNIQUEIDENTIFIER,
    danh_muc_id UNIQUEIDENTIFIER
);

-- Returns
CREATE TABLE yeu_cau_tra_hang (
   id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
   id_hoa_don UNIQUEIDENTIFIER NOT NULL,
   id_khach_hang UNIQUEIDENTIFIER,
   id_nhan_vien_xu_ly UNIQUEIDENTIFIER,
   ma_yeu_cau VARCHAR(50) UNIQUE,
   ly_do_tra_hang NVARCHAR(MAX),
   ngay_mua DATETIME2,
   ngay_yeu_cau DATETIME2 DEFAULT GETDATE(),
   ngay_duyet DATETIME2,
   ngay_hoan_tat DATETIME2,
   trang_thai INT,
   so_ngay_sau_mua INT,
   loai_yeu_cau INT,
   hinh_thuc_hoan_tien INT,
   so_tien_hoan DECIMAL(18, 2),
   ly_do_tu_choi NVARCHAR(MAX),
   ghi_chu NVARCHAR(MAX),
   ngay_tao DATETIME2 DEFAULT GETDATE(),
   ngay_sua DATETIME2
);

CREATE TABLE chi_tiet_tra_hang (
   id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
   id_yeu_cau_tra_hang UNIQUEIDENTIFIER NOT NULL,
   id_hoa_don_chi_tiet UNIQUEIDENTIFIER NOT NULL,
   id_serial_da_ban UNIQUEIDENTIFIER,
   so_luong INT,
   don_gia DECIMAL(18, 2),
   thanh_tien DECIMAL(18, 2),
   tinh_trang_luc_tra NVARCHAR(100),
   mo_ta_tinh_trang NVARCHAR(MAX),
   hinh_anh NVARCHAR(MAX),
   ngay_tao DATETIME2 DEFAULT GETDATE()
);

CREATE TABLE lich_su_tra_hang (
   id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
   id_yeu_cau_tra_hang UNIQUEIDENTIFIER NOT NULL,
   id_nhan_vien UNIQUEIDENTIFIER,
   hanh_dong NVARCHAR(100),
   mo_ta NVARCHAR(MAX),
   thoi_gian DATETIME2 DEFAULT GETDATE()
);

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

PRINT '>> Da tao xong cac bang.';
GO

-- ===================================================================================
-- II. TẠO CONSTRAINTS (FOREIGN KEYS)
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
ALTER TABLE phieu_giam_gia_khach_hang ADD CONSTRAINT FK_PGGKH_PGG FOREIGN KEY (phieu_giam_gia_id) REFERENCES phieu_giam_gia(id) ON DELETE CASCADE;
ALTER TABLE phieu_giam_gia_khach_hang ADD CONSTRAINT FK_PGGKH_KH FOREIGN KEY (khach_hang_id) REFERENCES khach_hang(user_id) ON DELETE CASCADE;
ALTER TABLE phieu_giam_gia_khach_hang ADD CONSTRAINT UQ_PhieuGiamGiaKhachHang UNIQUE (phieu_giam_gia_id, khach_hang_id);

-- After-Sales
ALTER TABLE serial_da_ban ADD CONSTRAINT FK_SerialDaBan_HDCT FOREIGN KEY (id_hoa_don_chi_tiet) REFERENCES hoa_don_chi_tiet(id);
ALTER TABLE serial_da_ban ADD CONSTRAINT FK_SerialDaBan_Serial FOREIGN KEY (id_serial) REFERENCES serial(id);
ALTER TABLE phieu_bao_hanh ADD CONSTRAINT FK_PBH_KhachHang FOREIGN KEY (id_khach_hang) REFERENCES khach_hang(user_id);
ALTER TABLE phieu_bao_hanh ADD CONSTRAINT FK_PBH_SerialDaBan FOREIGN KEY (id_serial_da_ban) REFERENCES serial_da_ban(id);
ALTER TABLE phieu_bao_hanh ADD CONSTRAINT FK_PBH_HDCT FOREIGN KEY (id_hoa_don_chi_tiet) REFERENCES hoa_don_chi_tiet(id);
ALTER TABLE lich_su_bao_hanh ADD CONSTRAINT FK_LSBH_PBH FOREIGN KEY (id_bao_hanh) REFERENCES phieu_bao_hanh(id);
ALTER TABLE lich_su_bao_hanh ADD CONSTRAINT FK_LSBH_LDBH FOREIGN KEY (id_ly_do_bao_hanh) REFERENCES ly_do_bao_hanh(id);
ALTER TABLE lich_su_bao_hanh ADD CONSTRAINT FK_LSBH_PhieuHen FOREIGN KEY (id_phieu_hen) REFERENCES phieu_hen_bao_hanh(id);
ALTER TABLE lich_su_bao_hanh ADD CONSTRAINT FK_LSBH_NV_TN FOREIGN KEY (id_nhan_vien_tiep_nhan) REFERENCES nhan_vien(user_id);
ALTER TABLE lich_su_bao_hanh ADD CONSTRAINT FK_LSBH_NV_SC FOREIGN KEY (id_nhan_vien_sua_chua) REFERENCES nhan_vien(user_id);

ALTER TABLE danh_gia ADD CONSTRAINT FK_DanhGia_KH FOREIGN KEY (khach_hang_id) REFERENCES khach_hang(user_id);
ALTER TABLE danh_gia ADD CONSTRAINT FK_DanhGia_CTSP FOREIGN KEY (san_pham_chi_tiet_id) REFERENCES chi_tiet_san_pham(id);
ALTER TABLE danh_gia ADD CONSTRAINT FK_DanhGia_HDCT FOREIGN KEY (hoa_don_chi_tiet_id) REFERENCES hoa_don_chi_tiet(id);
ALTER TABLE helpful_votes ADD CONSTRAINT FK_Helpful_DanhGia FOREIGN KEY (danh_gia_id) REFERENCES danh_gia(id) ON DELETE CASCADE;
ALTER TABLE helpful_votes ADD CONSTRAINT FK_Helpful_KH FOREIGN KEY (khach_hang_id) REFERENCES khach_hang(user_id) ON DELETE CASCADE;
ALTER TABLE helpful_votes ADD CONSTRAINT UQ_helpful_votes_user UNIQUE(danh_gia_id, khach_hang_id);

ALTER TABLE media_danh_gia ADD CONSTRAINT FK_Media_DanhGia FOREIGN KEY (danh_gia_id) REFERENCES danh_gia(id);
ALTER TABLE phan_hoi_danh_gia ADD CONSTRAINT FK_PhanHoi_DanhGia FOREIGN KEY (danh_gia_id) REFERENCES danh_gia(id);
ALTER TABLE phan_hoi_danh_gia ADD CONSTRAINT FK_PhanHoi_NV FOREIGN KEY (nhan_vien_id) REFERENCES nhan_vien(user_id);

-- Chat
ALTER TABLE chat ADD CONSTRAINT FK_Chat_KhachHang FOREIGN KEY (khach_hang_id) REFERENCES khach_hang(user_id) ON DELETE CASCADE;
ALTER TABLE chat ADD CONSTRAINT FK_Chat_NhanVien FOREIGN KEY (nhan_vien_id) REFERENCES nhan_vien(user_id) ON DELETE SET NULL;
ALTER TABLE chat ADD CONSTRAINT FK_Chat_ReplyTo FOREIGN KEY (reply_to_id) REFERENCES chat(id);

-- Points
ALTER TABLE tich_diem ADD CONSTRAINT FK_TichDiem_KH FOREIGN KEY (user_id) REFERENCES khach_hang(user_id);
ALTER TABLE lich_su_diem ADD CONSTRAINT FK_LSD_TichDiem FOREIGN KEY (tich_diem_id) REFERENCES tich_diem(id);
ALTER TABLE lich_su_diem ADD CONSTRAINT FK_LSD_QuyDoi FOREIGN KEY (id_quy_doi_diem) REFERENCES quy_doi_diem(id);
ALTER TABLE lich_su_diem ADD CONSTRAINT FK_LSD_HoaDon FOREIGN KEY (hoa_don_id) REFERENCES hoa_don(id);
ALTER TABLE chi_tiet_lich_su_diem ADD CONSTRAINT FK_CTLSD_KH FOREIGN KEY (user_id) REFERENCES khach_hang(user_id);
ALTER TABLE chi_tiet_lich_su_diem ADD CONSTRAINT FK_CTLSD_LSD FOREIGN KEY (lich_su_diem_id) REFERENCES lich_su_diem(id);

-- Categories
ALTER TABLE sanpham_danhmuc ADD CONSTRAINT FK_SPDM_SP FOREIGN KEY (san_pham_id) REFERENCES san_pham(id);
ALTER TABLE sanpham_danhmuc ADD CONSTRAINT FK_SPDM_DM FOREIGN KEY (danh_muc_id) REFERENCES danh_muc(id);

-- Returns
ALTER TABLE yeu_cau_tra_hang ADD CONSTRAINT FK_YCTH_HD FOREIGN KEY (id_hoa_don) REFERENCES hoa_don(id);
ALTER TABLE yeu_cau_tra_hang ADD CONSTRAINT FK_YCTH_KH FOREIGN KEY (id_khach_hang) REFERENCES khach_hang(user_id);
ALTER TABLE yeu_cau_tra_hang ADD CONSTRAINT FK_YCTH_NV FOREIGN KEY (id_nhan_vien_xu_ly) REFERENCES nhan_vien(user_id);
ALTER TABLE chi_tiet_tra_hang ADD CONSTRAINT FK_CTTH_YCTH FOREIGN KEY (id_yeu_cau_tra_hang) REFERENCES yeu_cau_tra_hang(id);
ALTER TABLE chi_tiet_tra_hang ADD CONSTRAINT FK_CTTH_HDCT FOREIGN KEY (id_hoa_don_chi_tiet) REFERENCES hoa_don_chi_tiet(id);
ALTER TABLE chi_tiet_tra_hang ADD CONSTRAINT FK_CTTH_Serial FOREIGN KEY (id_serial_da_ban) REFERENCES serial_da_ban(id);
ALTER TABLE lich_su_tra_hang ADD CONSTRAINT FK_LSTH_YCTH FOREIGN KEY (id_yeu_cau_tra_hang) REFERENCES yeu_cau_tra_hang(id);
ALTER TABLE lich_su_tra_hang ADD CONSTRAINT FK_LSTH_NV FOREIGN KEY (id_nhan_vien) REFERENCES nhan_vien(user_id);

PRINT '>> Da tao xong Constraints.';
GO

-- ===================================================================================
-- III. INDEXES
-- ===================================================================================
CREATE UNIQUE INDEX UX_PBH_OnePerSdb ON dbo.phieu_bao_hanh(id_serial_da_ban) WHERE id_serial_da_ban IS NOT NULL;
CREATE UNIQUE INDEX UQ_phieu_bao_hanh_ma_phieu ON phieu_bao_hanh(ma_phieu_bao_hanh) WHERE ma_phieu_bao_hanh IS NOT NULL;
CREATE UNIQUE INDEX UX_NhanVien_MaTaiKhoan_NotNull ON dbo.nhan_vien(ma_tai_khoan) WHERE ma_tai_khoan IS NOT NULL;
CREATE UNIQUE INDEX UQ_khach_hang_ma_tai_khoan_notnull ON dbo.khach_hang(ma_tai_khoan) WHERE ma_tai_khoan IS NOT NULL;

CREATE INDEX IX_Chat_KhachHangId ON chat(khach_hang_id);
CREATE INDEX IX_Chat_NhanVienId ON chat(nhan_vien_id);
CREATE INDEX IX_Chat_ConversationId ON chat(conversation_id) WHERE conversation_id IS NOT NULL;
CREATE INDEX IX_Chat_NgayPhanHoi ON chat(ngay_phan_hoi);
CREATE INDEX IX_Chat_IsRead ON chat(is_read) WHERE is_read = 0;
CREATE INDEX IX_Chat_IsFromCustomer ON chat(is_from_customer);
CREATE INDEX IX_Chat_CreatedAt ON chat(created_at);

CREATE NONCLUSTERED INDEX IX_HoaDon_TrangThai_LoaiHoaDon ON dbo.hoa_don(trang_thai, loai_hoa_don) INCLUDE (id, ma, id_khach_hang, ngay_tao, tong_tien_sau_giam) WHERE loai_hoa_don = 1;
CREATE NONCLUSTERED INDEX IX_HoaDon_KhachHang_TrangThai ON dbo.hoa_don(id_khach_hang, trang_thai) INCLUDE (id, ma, ngay_tao, tong_tien_sau_giam);
CREATE NONCLUSTERED INDEX IX_HoaDon_NgayTao_TrangThai ON dbo.hoa_don(ngay_tao DESC, trang_thai) INCLUDE (id, ma, id_khach_hang, loai_hoa_don);

CREATE NONCLUSTERED INDEX IX_LichSuDiem_HoaDonId ON dbo.lich_su_diem(hoa_don_id) WHERE hoa_don_id IS NOT NULL;
CREATE NONCLUSTERED INDEX IX_LichSuDiem_ThoiGian ON dbo.lich_su_diem(thoi_gian DESC);
CREATE NONCLUSTERED INDEX IX_LichSuDiem_TichDiemId_LoaiDiem ON dbo.lich_su_diem(tich_diem_id, loai_diem) INCLUDE (so_diem_cong, so_diem_da_dung, thoi_gian, trang_thai);

CREATE INDEX IX_PhieuGiamGiaKhachHang_KhachHangId ON phieu_giam_gia_khach_hang(khach_hang_id);
CREATE INDEX IX_PhieuGiamGiaKhachHang_PhieuGiamGiaId ON phieu_giam_gia_khach_hang(phieu_giam_gia_id);
CREATE INDEX IX_san_pham_slug ON san_pham(slug);

CREATE INDEX idx_chat_intents_category ON chat_intents(category);
CREATE INDEX idx_chat_intents_active ON chat_intents(is_active);
CREATE INDEX idx_chat_intents_code ON chat_intents(intent_code);
CREATE INDEX idx_quick_replies_intent ON chat_quick_replies(intent_code);
CREATE INDEX idx_quick_replies_active ON chat_quick_replies(is_active);
CREATE INDEX idx_chat_sessions_conversation ON chat_sessions(conversation_id);
CREATE INDEX idx_chat_sessions_customer ON chat_sessions(khach_hang_id);
CREATE INDEX idx_chat_sessions_bot_handling ON chat_sessions(is_bot_handling);
CREATE INDEX idx_chat_analytics_conversation ON chat_analytics(conversation_id);
CREATE INDEX idx_chat_analytics_intent ON chat_analytics(intent_detected);

PRINT '>> Da tao xong Indexes.';
GO

-- ===================================================================================
-- IV. INSERT DỮ LIỆU MẪU (FULL)
-- ===================================================================================

PRINT '>> Bat dau Insert du lieu...';

-- 1. Phương thức thanh toán
INSERT INTO phuong_thuc_thanh_toan (ten_phuong_thuc, loai_phuong_thuc) VALUES
(N'Tiền mặt', N'Cash'),
(N'Chuyển khoản QR', N'QR Payment');

-- 2. CPU (10 CPUs)
INSERT INTO cpu (ma_cpu, ten_cpu, mo_ta, trang_thai) VALUES
('CPU001', N'Intel Core i3 Raptor Lake - 1305U', N'Bộ xử lý Intel Core i3 1305U', 1),
('CPU002', N'Intel Core i5 Raptor Lake - 1334U', N'Bộ xử lý Intel Core i5 1334U', 1),
('CPU003', N'Intel Core i5 Raptor Lake - 1335U', N'Bộ xử lý Intel Core i5 1335U', 1),
('CPU004', N'Intel Core i7 Raptor Lake - 1355U', N'Bộ xử lý Intel Core i7 1355U', 1),
('CPU005', N'Intel Core 5 Raptor Lake - 210H', N'Bộ xử lý Intel Core 5 210H', 1),
('CPU006', N'Intel Core i5 Alder Lake', N'Bộ xử lý Intel Core i5 12th Gen', 1),
('CPU007', N'AMD Ryzen 5', N'Bộ xử lý AMD Ryzen 5', 1),
('CPU008', N'Intel Core 7 Raptor Lake - 150U', N'Bộ xử lý Intel Core 7 150U', 1),
('CPU009', N'Intel Core 7 Raptor Lake - 240H', N'Bộ xử lý Intel Core 7 240H', 1),
('CPU010', N'Intel Core i7 Alder Lake', N'Bộ xử lý Intel Core i7 12th Gen', 1);

-- 3. RAM (9 RAMs)
INSERT INTO ram (ma_ram, ten_ram, mo_ta, trang_thai) VALUES
('RAM001', N'DDR4 8GB 2666MHz', N'RAM 8GB Bus 2666', 1),
('RAM002', N'DDR4 16GB 2666MHz', N'RAM 16GB Bus 2666', 1),
('RAM003', N'DDR4 16GB 3200MHz', N'RAM 16GB Bus 3200', 1),
('RAM004', N'DDR5 16GB 5200MHz', N'RAM 16GB Bus 5200', 1),
('RAM005', N'DDR5 16GB 5600MHz', N'RAM 16GB Bus 5600', 1),
('RAM006', N'DDR4 16GB 2600MHz', N'RAM 16GB Bus 2600', 1),
('RAM007', N'DDR4 16GB 2660MHz', N'RAM 16GB Bus 2660', 1),
('RAM008', N'DDR5 16GB 4400MHz', N'RAM 16GB Bus 4400', 1),
('RAM009', N'DDR4 8GB 2660MHz', N'RAM 8GB Bus 2660', 1);

-- 4. Ổ cứng (10 SSDs)
INSERT INTO o_cung (ma_o_cung, dung_luong, mo_ta, trang_thai) VALUES
('SSD001', N'512GB SSD NVMe PCIe', N'SSD 512GB NVMe', 1),
('SSD002', N'512GB SSD M.2 NVMe PCIe', N'SSD 512GB M.2', 1),
('SSD003', N'512GB SSD M.2 PCIe Gen4x4', N'SSD 512GB Gen4', 1),
('SSD004', N'1TB SSD NVMe PCIe', N'SSD 1TB NVMe', 1),
('SSD005', N'1TB SSD M.2 PCIe', N'SSD 1TB M.2', 1),
('SSD006', N'1TB SSD NVMe M.2 PCIe', N'SSD 1TB M.2 NVMe', 1),
('SSD007', N'512GB SSD NVMe PCIe 4.0', N'SSD 512GB PCIe 4.0', 1),
('SSD008', N'512GB SSD M.2 PCIe Gen 4', N'SSD 512GB Gen 4', 1),
('SSD009', N'1TB SSD M.2 PCIe Gen 4', N'SSD 1TB Gen 4', 1),
('SSD010', N'1TB SSD NVMe PCIe Gen 4', N'SSD 1TB NVMe Gen 4', 1);

-- 5. GPU (4 GPUs)
INSERT INTO gpu (ma_gpu, ten_gpu, mo_ta, trang_thai) VALUES
('GPU001', N'Intel Iris Xe Graphics', N'Card onboard', 1),
('GPU002', N'Intel UHD Graphics', N'Card onboard UHD', 1),
('GPU003', N'RTX 3050 6GB', N'Card rời RTX 3050', 1),
('GPU004', N'RTX 5050 8GB', N'Card rời RTX 5050', 1);

-- 6. Màn hình (2 Types)
INSERT INTO loai_man_hinh (ma_loai_man_hinh, kich_thuoc, mo_ta, trang_thai) VALUES
('MH001', N'15.6 inch FHD', N'Màn hình 15.6 Full HD', 1),
('MH002', N'15.6 inch WQXGA', N'Màn hình 15.6 WQXGA', 1);

-- 7. Pin (3 Types)
INSERT INTO pin (ma_pin, dung_luong_pin, mo_ta, trang_thai) VALUES
('PIN001', N'45Wh', N'Pin 45Wh', 1),
('PIN002', N'60Wh', N'Pin 60Wh', 1),
('PIN003', N'90Wh', N'Pin 90Wh', 1);

-- 8. Màu sắc (3 Colors)
INSERT INTO mau_sac (ma_mau, ten_mau, mo_ta, trang_thai, hex_code) VALUES
('MS001', N'Đen', N'Màu đen', 1, '#000000'),
('MS002', N'Bạc', N'Màu bạc', 1, '#C0C0C0'),
('MS003', N'Xám', N'Màu xám', 1, '#808080');

-- 9. Danh mục (Categories)
INSERT INTO danh_muc (ma_danh_muc, ten_danh_muc, mo_ta, trang_thai, slug, icon_url, featured) VALUES
('DM001', N'Laptop Gaming', N'Laptop chơi game', 1, 'laptop-gaming', N'🎮', 1),
('DM002', N'Laptop Văn phòng', N'Laptop làm việc', 1, 'laptop-van-phong', N'💼', 1),
('DM003', N'Laptop Cao cấp', N'Laptop sang trọng', 1, 'laptop-cao-cap', N'💎', 1),
('DM004', N'Laptop Đồ họa', N'Laptop thiết kế', 1, 'laptop-do-hoa', N'🎨', 1),
('DM005', N'Laptop Sinh viên', N'Laptop giá rẻ', 1, 'laptop-sinh-vien', N'🎓', 1);

-- 10. Vai trò (Roles)
INSERT INTO vai_tro (ma_vai_tro, ten_vai_tro, mo_ta) VALUES
('ADMIN', N'Quản trị viên', N'Admin hệ thống'),
('NHAN_VIEN', N'Nhân viên', N'Nhân viên bán hàng'),
('KHACH_HANG', N'Khách hàng', N'Người mua hàng');

-- 11. Tài khoản (Accounts)
INSERT INTO tai_khoan (ma_vai_tro, ten_dang_nhap, mat_khau, email, trang_thai, ngay_tao) VALUES
((SELECT id FROM vai_tro WHERE ma_vai_tro = 'ADMIN'), 'admin', 'admin123', 'admin@store.com', 1, GETDATE()),
((SELECT id FROM vai_tro WHERE ma_vai_tro = 'NHAN_VIEN'), 'nhanvien', '123456', 'staff@store.com', 1, GETDATE()),
((SELECT id FROM vai_tro WHERE ma_vai_tro = 'KHACH_HANG'), 'khachhang', '123456', 'customer@gmail.com', 1, GETDATE());

-- 12. Users (Nhân viên & Khách hàng)
INSERT INTO nhan_vien (ma_tai_khoan, ma_nhan_vien, ho_ten, email, trang_thai) 
VALUES ((SELECT id FROM tai_khoan WHERE ten_dang_nhap = 'nhanvien'), 'NV001', N'Nguyễn Văn A', 'staff@store.com', 1);

INSERT INTO khach_hang (ma_tai_khoan, ma_khach_hang, ho_ten, email, so_dien_thoai, trang_thai) 
VALUES ((SELECT id FROM tai_khoan WHERE ten_dang_nhap = 'khachhang'), 'KH001', N'Trần Thị B', 'customer@gmail.com', '0987654321', 1);

INSERT INTO dia_chi (user_id, dia_chi, ho_ten, so_dien_thoai, tinh, mac_dinh)
VALUES ((SELECT user_id FROM khach_hang WHERE ma_khach_hang='KH001'), N'123 Cầu Giấy', N'Trần Thị B', '0987654321', N'Hà Nội', 1);

-- ===================================================================================
-- V. SẢN PHẨM DELL (SP001 - SP030)
-- ===================================================================================
PRINT '>> Dang Insert 30 san pham Dell...';

-- Insert SP001 - SP030 vào bảng san_pham
INSERT INTO san_pham (ma_san_pham, ten_san_pham, mo_ta, trang_thai, slug, ngay_tao, ngay_sua) VALUES
('SP001', N'Laptop Dell Inspiron 15 3530 - N5I5530W1', N'Laptop Dell Inspiron 15 3530 i5 1334U', 1, 'dell-inspiron-15-3530-n5i5530w1', GETDATE(), GETDATE()),
('SP002', N'Laptop Dell 15 DC15250 - DC5I7748W1', N'Laptop Dell 15 DC15250 i7 1355U', 1, 'dell-15-dc15250-dc5i7748w1', GETDATE(), GETDATE()),
('SP003', N'Laptop Dell Inspiron 15 3530 - 71070372', N'Laptop Dell Inspiron 15 3530 i5 1334U', 1, 'dell-inspiron-15-3530-71070372', GETDATE(), GETDATE()),
('SP004', N'Laptop Dell Inspiron 15 3530 - N3530-i3U085', N'Laptop Dell Inspiron 15 3530 i3 1305U', 1, 'dell-inspiron-15-3530-n3530-i3', GETDATE(), GETDATE()),
('SP005', N'Laptop Dell 15 DC15255 - DC5R5802W1', N'Laptop Dell 15 DC15255 R5 7530U', 1, 'dell-15-dc15255-dc5r5802w1', GETDATE(), GETDATE()),
('SP006', N'Laptop Dell 15 DC15250 - DC5I5357W1', N'Laptop Dell 15 DC15250 i5 1334U', 1, 'dell-15-dc15250-dc5i5357w1', GETDATE(), GETDATE()),
('SP007', N'Laptop Dell Inspiron 15 3530 - P16WD22', N'Laptop Dell Inspiron 15 3530 i5 1334U', 1, 'dell-inspiron-15-3530-p16wd22', GETDATE(), GETDATE()),
('SP008', N'Laptop Dell Latitude 3450 - L3450-1335U', N'Laptop Dell Latitude 3450 i5 1335U', 1, 'dell-latitude-3450-l3450', GETDATE(), GETDATE()),
('SP009', N'Laptop Dell Inspiron 15 3530 - N3530-i5', N'Laptop Dell Inspiron 15 3530 i5 1334U', 1, 'dell-inspiron-15-3530-n3530-i5', GETDATE(), GETDATE()),
('SP010', N'Laptop Dell Inspiron 15 3530 - N3530-BL', N'Laptop Dell Inspiron 15 3530 i5 1334U', 1, 'dell-inspiron-15-3530-n3530-bl', GETDATE(), GETDATE()),
('SP011', N'Laptop Dell Inspiron 15 3530 - N5I7421W1', N'Laptop Dell Inspiron 15 3530 i7 1355U', 1, 'dell-inspiron-15-3530-n5i7421w1', GETDATE(), GETDATE()),
('SP012', N'Laptop Dell Inspiron 15 3530 - P16WD21', N'Laptop Dell Inspiron 15 3530 i5 1334U', 1, 'dell-inspiron-15-3530-p16wd21', GETDATE(), GETDATE()),
('SP013', N'Laptop Dell 15 DC15250 - CPH997', N'Laptop Dell 15 DC15250 i7 1355U', 1, 'dell-15-dc15250-cph997', GETDATE(), GETDATE()),
('SP014', N'Laptop Dell Inspiron 15 3530 - N5I7216W1', N'Laptop Dell Inspiron 15 3530 i7 1355U', 1, 'dell-inspiron-15-3530-n5i7216w1', GETDATE(), GETDATE()),
('SP015', N'Laptop Dell Inspiron 15 3520 - 25P231', N'Laptop Dell Inspiron 15 3520 i5 1235U', 1, 'dell-inspiron-15-3520-25p231', GETDATE(), GETDATE()),
('SP016', N'Laptop Dell Inspiron 15 3520 - N3520-BL', N'Laptop Dell Inspiron 15 3520 i5 1235U', 1, 'dell-inspiron-15-3520-n3520-bl', GETDATE(), GETDATE()),
('SP017', N'Laptop Dell Inspiron 15 3530 - N3530-i7', N'Laptop Dell Inspiron 15 3530 i7 1355U', 1, 'dell-inspiron-15-3530-n3530-i7', GETDATE(), GETDATE()),
('SP018', N'Laptop Dell Alienware 16 Aurora AC16250', N'Laptop Dell Gaming Alienware 16 Core 5', 1, 'dell-alienware-16-aurora', GETDATE(), GETDATE()),
('SP019', N'Laptop Dell Inspiron 15 3530 - 71053721', N'Laptop Dell Inspiron 15 3530 i7 1355U', 1, 'dell-inspiron-15-3530-71053721', GETDATE(), GETDATE()),
('SP020', N'Laptop Dell Inspiron 15 3530 - 71053696', N'Laptop Dell Inspiron 15 3530 i5 1334U', 1, 'dell-inspiron-15-3530-71053696', GETDATE(), GETDATE()),
('SP021', N'Laptop Dell Inspiron 15 3530 - N5I5530W1', N'Laptop Dell Inspiron 15 3530 i5 1334U', 1, 'dell-inspiron-15-3530-n5i5530w1-2', GETDATE(), GETDATE()),
('SP022', N'Laptop Dell 15 DC15250 - DC5I7748W1', N'Laptop Dell 15 DC15250 i7 1355U', 1, 'dell-15-dc15250-dc5i7748w1-2', GETDATE(), GETDATE()),
('SP023', N'Laptop Dell Inspiron 15 3530 - 71070372', N'Laptop Dell Inspiron 15 3530 i5 1334U', 1, 'dell-inspiron-15-3530-71070372-2', GETDATE(), GETDATE()),
('SP024', N'Laptop Dell Inspiron 15 3530 - N3530-i3', N'Laptop Dell Inspiron 15 3530 i3 1305U', 1, 'dell-inspiron-15-3530-n3530-i3-2', GETDATE(), GETDATE()),
('SP025', N'Laptop Dell 15 DC15250 - DC5I5357W1', N'Laptop Dell 15 DC15250 i5 1334U', 1, 'dell-15-dc15250-dc5i5357w1-2', GETDATE(), GETDATE()),
('SP026', N'Laptop Dell Inspiron 15 3530 - P16WD22', N'Laptop Dell Inspiron 15 3530 i5 1334U', 1, 'dell-inspiron-15-3530-p16wd22-2', GETDATE(), GETDATE()),
('SP027', N'Laptop Dell Latitude 3450 - L3450-1335U', N'Laptop Dell Latitude 3450 i5 1335U', 1, 'dell-latitude-3450-l3450-2', GETDATE(), GETDATE()),
('SP028', N'Laptop Dell Inspiron 15 3530 - i5U165', N'Laptop Dell Inspiron 15 3530 i5 1334U', 1, 'dell-inspiron-15-3530-i5u165-2', GETDATE(), GETDATE()),
('SP029', N'Laptop Dell Inspiron 15 3530 - i5U165BL', N'Laptop Dell Inspiron 15 3530 i5 1334U', 1, 'dell-inspiron-15-3530-i5u165bl-2', GETDATE(), GETDATE()),
('SP030', N'Laptop Dell Inspiron 15 3530 - N5I7421W1', N'Laptop Dell Inspiron 15 3530 i7 1355U', 1, 'dell-inspiron-15-3530-n5i7421w1-2', GETDATE(), GETDATE());

-- Insert Chi tiết SP (Tạo 30 chi tiết tương ứng)
-- Dùng con trỏ hoặc insert thủ công. Ở đây insert mẫu logic cho 30 SP.
-- Mapping: CPU002 (i5), RAM002 (16GB), SSD001 (512GB), GPU001 (Iris) cho đa số Dell Inspiron.
INSERT INTO chi_tiet_san_pham (sp_id, cpu_id, ram_id, o_cung_id, gpu_id, loai_man_hinh_id, pin_id, mau_sac_id, ma_ctsp, gia_ban, gia_nhap, so_luong_ton, trang_thai)
SELECT id, 
(SELECT TOP 1 id FROM cpu WHERE ma_cpu = 'CPU002'),
(SELECT TOP 1 id FROM ram WHERE ma_ram = 'RAM002'),
(SELECT TOP 1 id FROM o_cung WHERE ma_o_cung = 'SSD001'),
(SELECT TOP 1 id FROM gpu WHERE ma_gpu = 'GPU001'),
(SELECT TOP 1 id FROM loai_man_hinh WHERE ma_loai_man_hinh = 'MH001'),
(SELECT TOP 1 id FROM pin WHERE ma_pin = 'PIN002'),
(SELECT TOP 1 id FROM mau_sac WHERE ma_mau = 'MS001'),
'CTSP-' + ma_san_pham, 
15000000, 12000000, 100, 1
FROM san_pham;

-- Insert Hình ảnh (Mỗi SP 1 ảnh đại diện)
INSERT INTO hinh_anh (id_spct, url, anh_chinh_dai_dien)
SELECT id, 'https://cdnv2.tgdd.vn/mwg-static/tgdd/Products/Images/44/334803/dell-inspiron-15-3530-i5-n5i5530w1-thumb-638762534676491196-600x600.jpg', 1
FROM chi_tiet_san_pham;

-- Insert Serial (Mỗi SP 3 serial)
INSERT INTO serial (ctsp_id, serial_no, trang_thai)
SELECT id, 'SN-' + CAST(NEWID() AS VARCHAR(50)), 0 FROM chi_tiet_san_pham;
INSERT INTO serial (ctsp_id, serial_no, trang_thai)
SELECT id, 'SN-' + CAST(NEWID() AS VARCHAR(50)), 0 FROM chi_tiet_san_pham;
INSERT INTO serial (ctsp_id, serial_no, trang_thai)
SELECT id, 'SN-' + CAST(NEWID() AS VARCHAR(50)), 0 FROM chi_tiet_san_pham;

-- ===================================================================================
-- VI. CÁC DỮ LIỆU KHÁC (ĐƠN HÀNG, ĐÁNH GIÁ, CHATBOT)
-- ===================================================================================

-- Phiếu giảm giá
INSERT INTO phieu_giam_gia (ma, ten_phieu_giam_gia, loai_phieu_giam_gia, gia_tri_giam_gia, so_luong_dung, trang_thai) 
VALUES ('SALE10', N'Giảm 10%', 1, 10, 100, 1);

-- Đơn hàng mẫu
DECLARE @id_kh UNIQUEIDENTIFIER = (SELECT TOP 1 user_id FROM khach_hang);
DECLARE @id_nv UNIQUEIDENTIFIER = (SELECT TOP 1 user_id FROM nhan_vien);
DECLARE @id_ctsp UNIQUEIDENTIFIER = (SELECT TOP 1 id FROM chi_tiet_san_pham WHERE ma_ctsp = 'CTSP-SP001');

INSERT INTO hoa_don (ma, id_khach_hang, id_nhan_vien, ma_don_hang, ten_khach_hang, tong_tien, trang_thai, ngay_tao)
VALUES ('HD001', @id_kh, @id_nv, 'ORD-001', N'Trần Thị B', 15000000, 1, GETDATE());

INSERT INTO hoa_don_chi_tiet (id_don_hang, id_ctsp, so_luong, don_gia)
VALUES ((SELECT id FROM hoa_don WHERE ma='HD001'), @id_ctsp, 1, 15000000);

-- Đánh giá
INSERT INTO danh_gia (khach_hang_id, san_pham_chi_tiet_id, so_sao, noi_dung, trang_thai_danh_gia)
VALUES (@id_kh, @id_ctsp, 5, N'Sản phẩm rất tốt, chạy mượt mà!', 1);

-- Chatbot Intent
INSERT INTO chat_intents (intent_code, intent_name, category, keywords, sample_questions, auto_response_template)
VALUES ('GREETING', N'Chào hỏi', 'GENERAL', N'["xin chào","hi"]', N'["Chào shop"]', N'Xin chào! Tôi có thể giúp gì cho bạn?');

PRINT '>> HOAN TAT INSERT DU LIEU!';
GO
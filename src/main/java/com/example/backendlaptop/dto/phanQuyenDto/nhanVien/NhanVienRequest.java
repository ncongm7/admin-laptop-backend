package com.example.backendlaptop.dto.phanQuyenDto.nhanVien;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class NhanVienRequest {

    private String maNhanVien;

    private String hoTen;

    private String soDienThoai;

    private String email;

    private Integer gioiTinh;

    private String anhNhanVien;

    private String chucVu;

    private String diaChi;

    private String danhGia;

    private Integer trangThai;
    
    // Các field cho tạo tài khoản
    private Boolean createTaiKhoan; // Flag để xác định có tạo tài khoản không
    private String tenDangNhap; // Tên đăng nhập (username)
    private String matKhau; // Mật khẩu
    private UUID maVaiTro; // Vai trò (mặc định NHAN_VIEN)
}

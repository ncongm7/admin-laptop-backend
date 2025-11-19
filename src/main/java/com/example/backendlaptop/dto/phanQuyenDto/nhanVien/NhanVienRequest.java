package com.example.backendlaptop.dto.phanQuyenDto.nhanVien;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    
    private String tenDangNhap; // Tên đăng nhập (username)
    
    private String matKhau; // Mật khẩu (chỉ update nếu có giá trị)
}

package com.example.backendlaptop.dto.phanQuyenDto.nhanVien;

import jakarta.persistence.Column;
import jakarta.persistence.Lob;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NhanVienDto {

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
}

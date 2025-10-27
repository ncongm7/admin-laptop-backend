package com.example.backendlaptop.dto.phanQuyenDto.khachHang;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter

public class KhachHangRequest {


    @NotBlank(message = "Mã không để trống")
    private String maKhachHang;

    @NotBlank(message = "Họ tên khách hàng không để trống")
    private String hoTen;

    @NotBlank(message = "Số điện thoại không để trống")
    @Pattern(regexp = "^0\\d{9}$", message = "Số điện thoại phải bắt đầu bằng số 0 và có đúng 10 chữ số")
    private String soDienThoai;

    @Email(message = "Email không hợp lệ, vui lòng nhập đúng định dạng (ví dụ: ten@gmail.com)")
    private String email;

    private Integer gioiTinh;

    private LocalDate ngaySinh;

    private Integer trangThai;

}

package com.example.backendlaptop.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class RegisterRequest {
    
    @NotBlank(message = "Họ tên không để trống")
    private String hoTen;
    
    @NotBlank(message = "Số điện thoại không để trống")
    @Pattern(regexp = "^0\\d{9}$", message = "Số điện thoại phải bắt đầu bằng số 0 và có đúng 10 chữ số")
    private String soDienThoai;
    
    @Email(message = "Email không hợp lệ")
    private String email;
    
    @NotBlank(message = "Mật khẩu không để trống")
    private String matKhau;
    
    @NotBlank(message = "Xác nhận mật khẩu không để trống")
    private String xacNhanMatKhau;
    
    private Integer gioiTinh; // 0: Nữ, 1: Nam
    
    private LocalDate ngaySinh;
}


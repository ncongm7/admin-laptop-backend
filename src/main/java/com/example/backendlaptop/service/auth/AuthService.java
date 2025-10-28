package com.example.backendlaptop.service.auth;

import com.example.backendlaptop.dto.auth.LoginRequest;
import com.example.backendlaptop.dto.auth.LoginResponse;
import com.example.backendlaptop.entity.NhanVien;
import com.example.backendlaptop.entity.TaiKhoan;
import com.example.backendlaptop.expection.ApiException;
import com.example.backendlaptop.repository.NhanVienRepository;
import com.example.backendlaptop.repository.TaiKhoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private TaiKhoanRepository taiKhoanRepository;

    @Autowired
    private NhanVienRepository nhanVienRepository;

    /**
     * Đăng nhập người dùng
     */
    public LoginResponse login(LoginRequest request) {
        // 1. Tìm tài khoản theo tên đăng nhập
        TaiKhoan taiKhoan = taiKhoanRepository.findByTenDangNhapWithRole(request.getTenDangNhap())
                .orElseThrow(() -> new ApiException("Tên đăng nhập hoặc mật khẩu không đúng", "INVALID_CREDENTIALS"));

        // 2. Kiểm tra trạng thái tài khoản
        if (taiKhoan.getTrangThai() == null || taiKhoan.getTrangThai() != 1) {
            throw new ApiException("Tài khoản đã bị khóa hoặc chưa được kích hoạt", "ACCOUNT_DISABLED");
        }

        // 3. Kiểm tra mật khẩu (đơn giản - chưa mã hóa)
        // TODO: Nên sử dụng BCrypt để hash password trong thực tế
        if (!request.getMatKhau().equals(taiKhoan.getMatKhau())) {
            throw new ApiException("Tên đăng nhập hoặc mật khẩu không đúng", "INVALID_CREDENTIALS");
        }

        // 4. Cập nhật lần đăng nhập cuối
        taiKhoan.setLanDangNhapCuoi(Instant.now());
        taiKhoanRepository.save(taiKhoan);

        // 5. Lấy thông tin nhân viên
        NhanVien nhanVien = nhanVienRepository.findByTaiKhoanId(taiKhoan.getId())
                .orElse(null);

        // 6. Tạo response
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
        userInfo.setUserId(nhanVien != null ? nhanVien.getId() : taiKhoan.getId());
        userInfo.setTenDangNhap(taiKhoan.getTenDangNhap());
        userInfo.setEmail(taiKhoan.getEmail());
        userInfo.setTrangThai(taiKhoan.getTrangThai());
        
        if (nhanVien != null) {
            userInfo.setHoTen(nhanVien.getHoTen());
        }
        
        if (taiKhoan.getMaVaiTro() != null) {
            userInfo.setVaiTro(taiKhoan.getMaVaiTro().getTenVaiTro());
        }

        // 7. Tạo JWT token (tạm thời dùng UUID làm token đơn giản)
        // TODO: Nên sử dụng JWT trong thực tế
        String token = "Bearer-" + UUID.randomUUID().toString();

        return new LoginResponse(token, userInfo);
    }

    /**
     * Lấy thông tin user hiện tại
     */
    public LoginResponse.UserInfo getCurrentUser(String token) {
        // TODO: Validate token và lấy thông tin user từ token
        // Tạm thời trả về null
        throw new ApiException("Token không hợp lệ", "INVALID_TOKEN");
    }

    /**
     * Đăng xuất
     */
    public void logout(String token) {
        // TODO: Implement logout logic (invalidate token)
        // Tạm thời không làm gì
    }
}


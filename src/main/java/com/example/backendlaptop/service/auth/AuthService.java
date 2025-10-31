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
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {

    @Autowired
    private TaiKhoanRepository taiKhoanRepository;

    @Autowired
    private NhanVienRepository nhanVienRepository;

    // In-memory token storage (cho dev - production nên dùng Redis hoặc database)
    private final Map<String, LoginResponse.UserInfo> tokenStore = new ConcurrentHashMap<>();

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

        // 8. Lưu token vào tokenStore
        tokenStore.put(token, userInfo);

        return new LoginResponse(token, userInfo);
    }

    /**
     * Lấy thông tin user hiện tại
     */
    public LoginResponse.UserInfo getCurrentUser(String token) {
        // Validate token và lấy thông tin user từ tokenStore
        LoginResponse.UserInfo userInfo = tokenStore.get(token);
        
        if (userInfo == null) {
            throw new ApiException("Token không hợp lệ hoặc đã hết hạn", "INVALID_TOKEN");
        }
        
        return userInfo;
    }

    /**
     * Đăng xuất
     */
    public void logout(String token) {
        // Xóa token khỏi tokenStore để invalidate
        tokenStore.remove(token);
    }
}


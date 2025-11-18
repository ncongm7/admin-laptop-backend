package com.example.backendlaptop.service.auth;

import com.example.backendlaptop.dto.auth.LoginRequest;
import com.example.backendlaptop.dto.auth.LoginResponse;
import com.example.backendlaptop.dto.auth.RegisterRequest;
import com.example.backendlaptop.entity.KhachHang;
import com.example.backendlaptop.entity.NhanVien;
import com.example.backendlaptop.entity.TaiKhoan;
import com.example.backendlaptop.expection.ApiException;
import com.example.backendlaptop.repository.KhachHangRepository;
import com.example.backendlaptop.repository.NhanVienRepository;
import com.example.backendlaptop.repository.TaiKhoanRepository;
import com.example.backendlaptop.service.PhanQuyenSer.KhachHangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Autowired
    private KhachHangRepository khachHangRepository;

    @Autowired
    private KhachHangService khachHangService;

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

        // 6. Lấy thông tin khách hàng (nếu không phải nhân viên)
        KhachHang khachHang = null;
        if (nhanVien == null) {
            khachHang = khachHangRepository.findByMaTaiKhoanId(taiKhoan.getId())
                    .orElse(null);
        }

        // 7. Tạo response
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
        // Nếu là nhân viên, dùng nhanVien.getId(), nếu là khách hàng, dùng khachHang.getId()
        if (nhanVien != null) {
            userInfo.setUserId(nhanVien.getId());
            userInfo.setHoTen(nhanVien.getHoTen());
        } else if (khachHang != null) {
            userInfo.setUserId(khachHang.getId()); // Dùng ID khách hàng
            userInfo.setHoTen(khachHang.getHoTen());
        } else {
            // Fallback: dùng taiKhoan ID nếu không tìm thấy cả nhân viên và khách hàng
            userInfo.setUserId(taiKhoan.getId());
        }
        
        userInfo.setTenDangNhap(taiKhoan.getTenDangNhap());
        userInfo.setEmail(taiKhoan.getEmail());
        userInfo.setTrangThai(taiKhoan.getTrangThai());
        
        if (taiKhoan.getMaVaiTro() != null) {
            userInfo.setVaiTro(taiKhoan.getMaVaiTro().getTenVaiTro());
        } else if (khachHang != null) {
            userInfo.setVaiTro("KHACH_HANG");
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

    /**
     * Đăng ký tài khoản khách hàng mới
     */
    @Transactional
    public LoginResponse register(RegisterRequest request) {
        // 1. Validate mật khẩu
        if (!request.getMatKhau().equals(request.getXacNhanMatKhau())) {
            throw new ApiException("Mật khẩu xác nhận không khớp", "PASSWORD_MISMATCH");
        }

        // 2. Kiểm tra số điện thoại đã tồn tại chưa (dùng làm username)
        if (taiKhoanRepository.findByTenDangNhap(request.getSoDienThoai()).isPresent()) {
            throw new ApiException("Số điện thoại đã được đăng ký", "PHONE_EXISTS");
        }

        // 3. Kiểm tra email đã tồn tại chưa (nếu có)
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            // Kiểm tra trong KhachHang
            if (khachHangRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new ApiException("Email đã được đăng ký", "EMAIL_EXISTS");
            }
        }

        // 4. Tạo tài khoản
        TaiKhoan taiKhoan = new TaiKhoan();
        taiKhoan.setId(UUID.randomUUID());
        taiKhoan.setTenDangNhap(request.getSoDienThoai()); // Dùng SĐT làm username
        taiKhoan.setMatKhau(request.getMatKhau()); // TODO: Hash password
        taiKhoan.setEmail(request.getEmail());
        taiKhoan.setTrangThai(1); // Active
        taiKhoan.setNgayTao(Instant.now());
        taiKhoan.setMaVaiTro(null); // Khách hàng không có vai trò
        taiKhoanRepository.save(taiKhoan);

        // 5. Tạo mã khách hàng
        String maKhachHang = khachHangService.generateMaKhachHang();

        // 6. Tạo khách hàng
        KhachHang khachHang = new KhachHang();
        // Không set ID thủ công, để Hibernate tự generate theo @GeneratedValue
        khachHang.setMaTaiKhoan(taiKhoan);
        khachHang.setMaKhachHang(maKhachHang);
        khachHang.setHoTen(request.getHoTen());
        khachHang.setSoDienThoai(request.getSoDienThoai());
        khachHang.setEmail(request.getEmail());
        khachHang.setGioiTinh(request.getGioiTinh() != null ? request.getGioiTinh() : 0);
        khachHang.setNgaySinh(request.getNgaySinh());
        khachHang.setTrangThai(1); // Active
        khachHang = khachHangRepository.save(khachHang); // Lưu và lấy entity đã được persist với ID

        // 7. Tự động đăng nhập sau khi đăng ký
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
        userInfo.setUserId(khachHang.getId()); // Lưu ID khách hàng
        userInfo.setTenDangNhap(taiKhoan.getTenDangNhap());
        userInfo.setEmail(taiKhoan.getEmail());
        userInfo.setHoTen(khachHang.getHoTen());
        userInfo.setTrangThai(taiKhoan.getTrangThai());
        userInfo.setVaiTro("KHACH_HANG");

        // 8. Tạo token
        String token = "Bearer-" + UUID.randomUUID().toString();

        return new LoginResponse(token, userInfo);
    }
}


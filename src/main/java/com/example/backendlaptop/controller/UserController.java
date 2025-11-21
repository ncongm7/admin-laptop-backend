package com.example.backendlaptop.controller;

import com.example.backendlaptop.entity.TaiKhoan;
import com.example.backendlaptop.entity.NhanVien;
import com.example.backendlaptop.entity.KhachHang;
import com.example.backendlaptop.entity.VaiTro;
import com.example.backendlaptop.repository.TaiKhoanRepository;
import com.example.backendlaptop.repository.NhanVienRepository;
import com.example.backendlaptop.repository.KhachHangRepository;
import com.example.backendlaptop.repository.VaiTroRepository;
import com.example.backendlaptop.model.response.ResponseObject;
import com.example.backendlaptop.service.auth.AuthService;
import com.example.backendlaptop.dto.auth.LoginResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private TaiKhoanRepository taiKhoanRepository;
    
    @Autowired
    private NhanVienRepository nhanVienRepository;
    
    @Autowired
    private KhachHangRepository khachHangRepository;
    
    @Autowired
    private VaiTroRepository vaiTroRepository;
    
    @Autowired
    private AuthService authService;

    /**
     * Lấy danh sách tất cả users (chỉ admin)
     * Endpoint: GET /api/users
     */
    @GetMapping
    public ResponseEntity<ResponseObject<List<UserDTO>>> getAllUsers(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            // Kiểm tra quyền admin
            LoginResponse.UserInfo currentUser = getCurrentUserFromToken(authHeader);
            if (!isAdmin(currentUser)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ResponseObject<>(false, null, "Chỉ admin mới có quyền xem danh sách users"));
            }
            
            List<TaiKhoan> taiKhoans = taiKhoanRepository.findAll();
            List<UserDTO> users = taiKhoans.stream()
                    .map(tk -> mapToUserDTO(tk, true)) // Admin có thể xem tk/mk
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(new ResponseObject<>(users, "Lấy danh sách users thành công"));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ResponseObject<>(false, null, "Lỗi khi lấy danh sách users: " + e.getMessage()));
        }
    }

    /**
     * Lấy thông tin user theo ID
     * Admin: Xem bất kỳ user nào
     * Nhân viên: Chỉ xem được thông tin của chính mình
     * Endpoint: GET /api/users/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ResponseObject<UserDTO>> getUserById(
            @PathVariable String id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            LoginResponse.UserInfo currentUser = getCurrentUserFromToken(authHeader);
            java.util.UUID userId = java.util.UUID.fromString(id);
            
            // Nhân viên chỉ xem được thông tin của chính mình
            if (!isAdmin(currentUser)) {
                // Lấy taiKhoanId từ currentUser (userId là taiKhoan.id)
                Optional<TaiKhoan> currentUserTaiKhoan = taiKhoanRepository.findByTenDangNhap(currentUser.getTenDangNhap());
                if (currentUserTaiKhoan.isEmpty() || !currentUserTaiKhoan.get().getId().equals(userId)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(new ResponseObject<>(false, null, "Bạn chỉ có thể xem thông tin của chính mình"));
                }
            }
            
            TaiKhoan taiKhoan = taiKhoanRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User không tồn tại"));
            
            // Chỉ admin mới xem được tk/mk
            boolean canViewCredentials = isAdmin(currentUser);
            UserDTO userDTO = mapToUserDTO(taiKhoan, canViewCredentials);
            return ResponseEntity.ok(new ResponseObject<>(userDTO, "Lấy thông tin user thành công"));
        } catch (Exception e) {
            return ResponseEntity.status(404)
                    .body(new ResponseObject<>(false, null, "Lỗi: " + e.getMessage()));
        }
    }
    
    /**
     * Lấy thông tin tài khoản của chính mình (cho nhân viên)
     * Endpoint: GET /api/users/me
     */
    @GetMapping("/me")
    public ResponseEntity<ResponseObject<UserDTO>> getMyAccountInfo(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            LoginResponse.UserInfo currentUser = getCurrentUserFromToken(authHeader);
            
            // Lấy tài khoản từ tenDangNhap
            TaiKhoan taiKhoan = taiKhoanRepository.findByTenDangNhapWithRole(currentUser.getTenDangNhap())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"));
            
            // Nhân viên luôn xem được tk/mk của chính mình
            UserDTO userDTO = mapToUserDTO(taiKhoan, true);
            return ResponseEntity.ok(new ResponseObject<>(userDTO, "Lấy thông tin tài khoản thành công"));
        } catch (Exception e) {
            return ResponseEntity.status(404)
                    .body(new ResponseObject<>(false, null, "Lỗi: " + e.getMessage()));
        }
    }
    
    /**
     * Tạo tài khoản mới (chỉ admin)
     * Endpoint: POST /api/users
     */
    @PostMapping
    public ResponseEntity<ResponseObject<UserDTO>> createUser(
            @RequestBody CreateUserRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            // Kiểm tra quyền admin
            LoginResponse.UserInfo currentUser = getCurrentUserFromToken(authHeader);
            if (!isAdmin(currentUser)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ResponseObject<>(false, null, "Chỉ admin mới có quyền tạo tài khoản"));
            }

            // Validate
            if (request.getTenDangNhap() == null || request.getTenDangNhap().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ResponseObject<>(false, null, "Tên đăng nhập không được để trống"));
            }

            // Kiểm tra tên đăng nhập đã tồn tại chưa
            Optional<TaiKhoan> existingTaiKhoan = taiKhoanRepository.findByTenDangNhap(request.getTenDangNhap());
            if (existingTaiKhoan.isPresent()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ResponseObject<>(false, null, "Tên đăng nhập đã tồn tại"));
            }

            // Tạo tài khoản mới
            TaiKhoan taiKhoan = new TaiKhoan();
            taiKhoan.setId(UUID.randomUUID());
            taiKhoan.setTenDangNhap(request.getTenDangNhap());
            taiKhoan.setMatKhau(request.getMatKhau() != null ? request.getMatKhau() : "123456"); // Mật khẩu mặc định
            taiKhoan.setEmail(request.getEmail());
            taiKhoan.setTrangThai(request.getTrangThai() != null ? request.getTrangThai() : 1);
            taiKhoan.setNgayTao(Instant.now());

            // Set vai trò nếu có
            if (request.getMaVaiTro() != null) {
                Optional<VaiTro> vaiTro = vaiTroRepository.findById(request.getMaVaiTro());
                if (vaiTro.isPresent()) {
                    taiKhoan.setMaVaiTro(vaiTro.get());
                }
            }

            taiKhoan = taiKhoanRepository.save(taiKhoan);

            // Map to DTO
            UserDTO userDTO = mapToUserDTO(taiKhoan, true);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ResponseObject<>(userDTO, "Tạo tài khoản thành công"));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ResponseObject<>(false, null, "Lỗi khi tạo tài khoản: " + e.getMessage()));
        }
    }

    /**
     * Cập nhật tài khoản (chỉ admin)
     * Endpoint: PUT /api/users/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ResponseObject<UserDTO>> updateUser(
            @PathVariable String id,
            @RequestBody CreateUserRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            // Kiểm tra quyền admin
            LoginResponse.UserInfo currentUser = getCurrentUserFromToken(authHeader);
            if (!isAdmin(currentUser)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ResponseObject<>(false, null, "Chỉ admin mới có quyền cập nhật tài khoản"));
            }

            UUID userId = UUID.fromString(id);
            TaiKhoan taiKhoan = taiKhoanRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại"));

            // Cập nhật thông tin
            if (request.getTenDangNhap() != null) {
                // Kiểm tra tên đăng nhập đã tồn tại chưa (trừ chính nó)
                Optional<TaiKhoan> existingTaiKhoan = taiKhoanRepository.findByTenDangNhap(request.getTenDangNhap());
                if (existingTaiKhoan.isPresent() && !existingTaiKhoan.get().getId().equals(userId)) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(new ResponseObject<>(false, null, "Tên đăng nhập đã tồn tại"));
                }
                taiKhoan.setTenDangNhap(request.getTenDangNhap());
            }

            if (request.getMatKhau() != null && !request.getMatKhau().trim().isEmpty()) {
                taiKhoan.setMatKhau(request.getMatKhau());
            }

            if (request.getEmail() != null) {
                taiKhoan.setEmail(request.getEmail());
            }

            if (request.getTrangThai() != null) {
                taiKhoan.setTrangThai(request.getTrangThai());
            }

            // Cập nhật vai trò nếu có
            if (request.getMaVaiTro() != null) {
                Optional<VaiTro> vaiTro = vaiTroRepository.findById(request.getMaVaiTro());
                if (vaiTro.isPresent()) {
                    taiKhoan.setMaVaiTro(vaiTro.get());
                }
            }

            taiKhoan = taiKhoanRepository.save(taiKhoan);

            // Map to DTO
            UserDTO userDTO = mapToUserDTO(taiKhoan, true);
            return ResponseEntity.ok(new ResponseObject<>(userDTO, "Cập nhật tài khoản thành công"));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ResponseObject<>(false, null, "Lỗi khi cập nhật tài khoản: " + e.getMessage()));
        }
    }

    /**
     * Helper: Lấy current user từ token
     */
    private LoginResponse.UserInfo getCurrentUserFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Token không hợp lệ");
        }
        String token = authHeader.substring(7);
        return authService.getCurrentUser(token);
    }
    
    /**
     * Helper: Kiểm tra xem user có phải admin không
     */
    private boolean isAdmin(LoginResponse.UserInfo user) {
        if (user == null || user.getVaiTro() == null) {
            return false;
        }
        String vaiTro = user.getVaiTro();
        // Check ma_vai_tro code (ADMIN)
        return vaiTro.equals("ADMIN");
    }

    /**
     * Map TaiKhoan entity to UserDTO
     * @param taiKhoan Tài khoản cần map
     * @param includeCredentials Có bao gồm tên đăng nhập và mật khẩu không (chỉ admin hoặc chính mình)
     */
    private UserDTO mapToUserDTO(TaiKhoan taiKhoan, boolean includeCredentials) {
        if (taiKhoan == null) {
            throw new RuntimeException("TaiKhoan không được null");
        }
        
        UserDTO dto = new UserDTO();
        dto.setId(taiKhoan.getId());
        dto.setEmail(taiKhoan.getEmail());
        dto.setName(taiKhoan.getTenDangNhap() != null ? taiKhoan.getTenDangNhap() : "N/A"); // Tạm thời dùng tenDangNhap, sẽ override nếu có NhanVien/KhachHang
        dto.setStatus(taiKhoan.getTrangThai() != null && taiKhoan.getTrangThai() == 1);
        dto.setCreated_at(taiKhoan.getNgayTao() != null ? taiKhoan.getNgayTao().toString() : null);
        
        // Set role (ma_vai_tro code) - thêm null check
        try {
            if (taiKhoan.getMaVaiTro() != null && taiKhoan.getMaVaiTro().getMaVaiTro() != null) {
                dto.setRole(taiKhoan.getMaVaiTro().getMaVaiTro()); // Dùng ma_vai_tro thay vì ten_vai_tro
            } else {
                dto.setRole("KHACH_HANG"); // Mặc định nếu không có vai trò
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy maVaiTro: " + e.getMessage());
            dto.setRole("KHACH_HANG"); // Fallback
        }
        
        // Set credentials nếu có quyền
        if (includeCredentials) {
            dto.setTenDangNhap(taiKhoan.getTenDangNhap());
            dto.setMatKhau(taiKhoan.getMatKhau()); // Lưu ý: Mật khẩu có thể đã hash
        }
        
        // Tìm NhanVien hoặc KhachHang liên quan - thêm try-catch
        try {
            Optional<NhanVien> nhanVien = nhanVienRepository.findByTaiKhoanId(taiKhoan.getId());
            Optional<KhachHang> khachHang = khachHangRepository.findByMaTaiKhoanId(taiKhoan.getId());
            
            if (nhanVien.isPresent()) {
                NhanVien nv = nhanVien.get();
                dto.setName(nv.getHoTen() != null ? nv.getHoTen() : dto.getName());
                dto.setPhone(nv.getSoDienThoai());
                dto.setAvatar(nv.getAnhNhanVien());
                dto.setPosition(nv.getChucVu());
                dto.setAddress(nv.getDiaChi());
                dto.setIsStaff(true);
            } else if (khachHang.isPresent()) {
                KhachHang kh = khachHang.get();
                dto.setName(kh.getHoTen() != null ? kh.getHoTen() : dto.getName());
                dto.setPhone(kh.getSoDienThoai());
                dto.setAvatar(null); // KhachHang có thể không có avatar
                dto.setIsStaff(false);
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi query NhanVien/KhachHang: " + e.getMessage());
            // Tiếp tục với dữ liệu cơ bản từ TaiKhoan
        }
        
        return dto;
    }

    /**
     * UserDTO class
     */
    public static class UserDTO {
        private java.util.UUID id;
        private String name;
        private String email;
        private String phone;
        private String role; // ma_vai_tro code (ADMIN, NHAN_VIEN, KHACH_HANG)
        private Boolean status;
        private String avatar;
        private String created_at;
        private String tenDangNhap; // Chỉ trả về khi có quyền
        private String matKhau; // Chỉ trả về khi có quyền
        private String position; // Chức vụ (cho nhân viên)
        private String address; // Địa chỉ
        private Boolean isStaff; // Có phải nhân viên không

        // Getters and Setters
        public java.util.UUID getId() { return id; }
        public void setId(java.util.UUID id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        
        public Boolean getStatus() { return status; }
        public void setStatus(Boolean status) { this.status = status; }
        
        public String getAvatar() { return avatar; }
        public void setAvatar(String avatar) { this.avatar = avatar; }
        
        public String getCreated_at() { return created_at; }
        public void setCreated_at(String created_at) { this.created_at = created_at; }
        
        public String getTenDangNhap() { return tenDangNhap; }
        public void setTenDangNhap(String tenDangNhap) { this.tenDangNhap = tenDangNhap; }
        
        public String getMatKhau() { return matKhau; }
        public void setMatKhau(String matKhau) { this.matKhau = matKhau; }
        
        public String getPosition() { return position; }
        public void setPosition(String position) { this.position = position; }
        
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        
        public Boolean getIsStaff() { return isStaff; }
        public void setIsStaff(Boolean isStaff) { this.isStaff = isStaff; }
    }

    /**
     * CreateUserRequest class
     */
    public static class CreateUserRequest {
        private UUID id;
        private UUID maVaiTro;
        private String tenDangNhap;
        private String matKhau;
        private String email;
        private Integer trangThai;

        // Getters and Setters
        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }

        public UUID getMaVaiTro() { return maVaiTro; }
        public void setMaVaiTro(UUID maVaiTro) { this.maVaiTro = maVaiTro; }

        public String getTenDangNhap() { return tenDangNhap; }
        public void setTenDangNhap(String tenDangNhap) { this.tenDangNhap = tenDangNhap; }

        public String getMatKhau() { return matKhau; }
        public void setMatKhau(String matKhau) { this.matKhau = matKhau; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public Integer getTrangThai() { return trangThai; }
        public void setTrangThai(Integer trangThai) { this.trangThai = trangThai; }
    }
}


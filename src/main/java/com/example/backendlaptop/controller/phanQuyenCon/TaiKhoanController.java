package com.example.backendlaptop.controller.phanQuyenCon;

import com.example.backendlaptop.dto.phanQuyenDto.TaiKhoan.TaiKhoanDto;
import com.example.backendlaptop.dto.phanQuyenDto.TaiKhoan.TaiKhoanRequest;
import com.example.backendlaptop.entity.TaiKhoan;
import com.example.backendlaptop.model.response.ResponseObject;
import com.example.backendlaptop.service.PhanQuyenSer.TaiKhoanService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/tai-khoan")
@CrossOrigin(origins = "*")
public class TaiKhoanController {

    @Autowired
    private TaiKhoanService taiKhoanService;

    /**
     * GET /api/tai-khoan - Danh sách tài khoản
     */
    @GetMapping
    public ResponseEntity<ResponseObject<List<TaiKhoanDto>>> getAllTaiKhoan() {
        try {
            List<TaiKhoanDto> taiKhoanList = taiKhoanService.findAllTK();
            return ResponseEntity.ok(new ResponseObject<>(taiKhoanList, "Lấy danh sách tài khoản thành công"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseObject<>(false, null, "Lỗi khi lấy danh sách tài khoản: " + e.getMessage()));
        }
    }

    /**
     * GET /api/tai-khoan/{id} - Chi tiết tài khoản
     */
    @GetMapping("/{id}")
    public ResponseEntity<ResponseObject<TaiKhoan>> getTaiKhoanById(@PathVariable UUID id) {
        try {
            TaiKhoan taiKhoan = taiKhoanService.getOne(id);
            if (taiKhoan == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ResponseObject<>(false, null, "Tài khoản không tồn tại"));
            }
            return ResponseEntity.ok(new ResponseObject<>(taiKhoan, "Lấy thông tin tài khoản thành công"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseObject<>(false, null, "Lỗi: " + e.getMessage()));
        }
    }

    /**
     * POST /api/tai-khoan - Tạo tài khoản
     */
    @PostMapping
    public ResponseEntity<ResponseObject<TaiKhoan>> createTaiKhoan(@Valid @RequestBody TaiKhoanRequest taiKhoanRequest) {
        try {
            TaiKhoan taiKhoan = taiKhoanService.addTK(taiKhoanRequest);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ResponseObject<>(taiKhoan, "Tạo tài khoản thành công"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseObject<>(false, null, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseObject<>(false, null, "Lỗi khi tạo tài khoản: " + e.getMessage()));
        }
    }

    /**
     * PUT /api/tai-khoan/{id} - Cập nhật tài khoản
     */
    @PutMapping("/{id}")
    public ResponseEntity<ResponseObject<TaiKhoan>> updateTaiKhoan(
            @PathVariable UUID id,
            @Valid @RequestBody TaiKhoanRequest taiKhoanRequest) {
        try {
            TaiKhoan taiKhoan = taiKhoanService.updateTK(id, taiKhoanRequest);
            return ResponseEntity.ok(new ResponseObject<>(taiKhoan, "Cập nhật tài khoản thành công"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseObject<>(false, null, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseObject<>(false, null, "Lỗi khi cập nhật tài khoản: " + e.getMessage()));
        }
    }

    /**
     * PUT /api/tai-khoan/{id}/reset-password - Reset mật khẩu
     */
    @PutMapping("/{id}/reset-password")
    public ResponseEntity<ResponseObject<TaiKhoan>> resetPassword(
            @PathVariable UUID id,
            @RequestBody Map<String, String> request) {
        try {
            String newPassword = request.get("newPassword");
            if (newPassword == null || newPassword.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ResponseObject<>(false, null, "Mật khẩu mới không được để trống"));
            }
            
            TaiKhoan taiKhoan = taiKhoanService.resetPassword(id, newPassword);
            return ResponseEntity.ok(new ResponseObject<>(taiKhoan, "Reset mật khẩu thành công"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseObject<>(false, null, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseObject<>(false, null, "Lỗi khi reset mật khẩu: " + e.getMessage()));
        }
    }

    /**
     * PUT /api/tai-khoan/{id}/toggle-status - Khóa/Mở khóa tài khoản
     */
    @PutMapping("/{id}/toggle-status")
    public ResponseEntity<ResponseObject<TaiKhoan>> toggleStatus(@PathVariable UUID id) {
        try {
            TaiKhoan taiKhoan = taiKhoanService.toggleStatus(id);
            String message = taiKhoan.getTrangThai() == 1 
                    ? "Mở khóa tài khoản thành công" 
                    : "Khóa tài khoản thành công";
            return ResponseEntity.ok(new ResponseObject<>(taiKhoan, message));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseObject<>(false, null, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseObject<>(false, null, "Lỗi khi thay đổi trạng thái: " + e.getMessage()));
        }
    }

    /**
     * DELETE /api/tai-khoan/{id} - Xóa tài khoản
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseObject<String>> deleteTaiKhoan(@PathVariable UUID id) {
        try {
            taiKhoanService.deleteTK(id);
            return ResponseEntity.ok(new ResponseObject<>("Xóa tài khoản thành công", "Xóa tài khoản thành công"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseObject<>(false, null, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseObject<>(false, null, "Lỗi khi xóa tài khoản: " + e.getMessage()));
        }
    }

    /**
     * GET /api/tai-khoan/{id}/login-history - Lịch sử đăng nhập
     */
    @GetMapping("/{id}/login-history")
    public ResponseEntity<ResponseObject<Map<String, Object>>> getLoginHistory(@PathVariable UUID id) {
        try {
            Instant lanDangNhapCuoi = taiKhoanService.getLoginHistory(id);
            
            Map<String, Object> loginHistory = new HashMap<>();
            loginHistory.put("taiKhoanId", id);
            loginHistory.put("lanDangNhapCuoi", lanDangNhapCuoi);
            loginHistory.put("formattedDate", lanDangNhapCuoi != null 
                    ? lanDangNhapCuoi.toString() 
                    : "Chưa có lịch sử đăng nhập");
            
            return ResponseEntity.ok(new ResponseObject<>(loginHistory, "Lấy lịch sử đăng nhập thành công"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseObject<>(false, null, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseObject<>(false, null, "Lỗi khi lấy lịch sử đăng nhập: " + e.getMessage()));
        }
    }

    /**
     * GET /api/tai-khoan/search?tenDangNhap=... - Tìm kiếm tài khoản theo tên đăng nhập
     */
    @GetMapping("/search")
    public ResponseEntity<ResponseObject<List<TaiKhoanDto>>> searchByTenDangNhap(
            @RequestParam(required = false) String tenDangNhap) {
        try {
            List<TaiKhoanDto> taiKhoanList = taiKhoanService.searchByTenDangNhap(tenDangNhap);
            return ResponseEntity.ok(new ResponseObject<>(taiKhoanList, "Tìm kiếm tài khoản thành công"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseObject<>(false, null, "Lỗi khi tìm kiếm tài khoản: " + e.getMessage()));
        }
    }

    /**
     * GET /api/tai-khoan/search-advanced?tenDangNhap=...&tenVaiTro=...&trangThai=... 
     * - Tìm kiếm tài khoản với nhiều điều kiện
     */
    @GetMapping("/search-advanced")
    public ResponseEntity<ResponseObject<List<TaiKhoanDto>>> searchTaiKhoan(
            @RequestParam(required = false) String tenDangNhap,
            @RequestParam(required = false) String tenVaiTro,
            @RequestParam(required = false) Integer trangThai) {
        try {
            List<TaiKhoanDto> taiKhoanList = taiKhoanService.searchTaiKhoan(tenDangNhap, tenVaiTro, trangThai);
            return ResponseEntity.ok(new ResponseObject<>(taiKhoanList, "Tìm kiếm tài khoản thành công"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseObject<>(false, null, "Lỗi khi tìm kiếm tài khoản: " + e.getMessage()));
        }
    }
}

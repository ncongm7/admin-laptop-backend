package com.example.backendlaptop.service.PhanQuyenSer;

import com.example.backendlaptop.dto.phanQuyenDto.TaiKhoan.TaiKhoanDto;
import com.example.backendlaptop.dto.phanQuyenDto.TaiKhoan.TaiKhoanRequest;
import com.example.backendlaptop.entity.TaiKhoan;
import com.example.backendlaptop.repository.TaiKhoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class TaiKhoanService {
    @Autowired
    private TaiKhoanRepository taiKhoanRepository;

    public List<TaiKhoanDto> findAllTK() {
        return taiKhoanRepository.getAllTK();
    }

    public TaiKhoan getOne(UUID id){
        return taiKhoanRepository.findById(id).orElse(null);
    }

    /**
     * Thêm tài khoản mới
     * @param taiKhoanRequest Thông tin tài khoản cần thêm
     * @return Tài khoản đã được lưu
     */
    public TaiKhoan addTK(TaiKhoanRequest taiKhoanRequest){
        // Kiểm tra tên đăng nhập đã tồn tại chưa
        if (taiKhoanRequest.getTenDangNhap() != null) {
            taiKhoanRepository.findByTenDangNhap(taiKhoanRequest.getTenDangNhap())
                    .ifPresent(tk -> {
                        throw new RuntimeException("Tên đăng nhập đã tồn tại");
                    });
        }

        TaiKhoan taiKhoan = new TaiKhoan();
        taiKhoan.setId(UUID.randomUUID());
        taiKhoan.setMaVaiTro(taiKhoanRequest.getMaVaiTro());
        taiKhoan.setTenDangNhap(taiKhoanRequest.getTenDangNhap());
        taiKhoan.setMatKhau(taiKhoanRequest.getMatKhau());
        taiKhoan.setEmail(taiKhoanRequest.getEmail());
        taiKhoan.setTrangThai(taiKhoanRequest.getTrangThai() != null ? taiKhoanRequest.getTrangThai() : 1);
        taiKhoan.setNgayTao(taiKhoanRequest.getNgayTao() != null ? taiKhoanRequest.getNgayTao() : Instant.now());
        taiKhoan.setLanDangNhapCuoi(taiKhoanRequest.getLanDangNhapCuoi());

        return taiKhoanRepository.save(taiKhoan);
    }

    /**
     * Cập nhật tài khoản
     * @param id ID của tài khoản cần cập nhật
     * @param taiKhoanRequest Thông tin tài khoản cần cập nhật
     * @return Tài khoản đã được cập nhật
     */
    public TaiKhoan updateTK(UUID id, TaiKhoanRequest taiKhoanRequest){
        TaiKhoan taiKhoan = taiKhoanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại"));

        // Kiểm tra tên đăng nhập đã tồn tại chưa (trừ chính nó)
        if (taiKhoanRequest.getTenDangNhap() != null && 
            !taiKhoanRequest.getTenDangNhap().equals(taiKhoan.getTenDangNhap())) {
            taiKhoanRepository.findByTenDangNhap(taiKhoanRequest.getTenDangNhap())
                    .ifPresent(tk -> {
                        throw new RuntimeException("Tên đăng nhập đã tồn tại");
                    });
        }

        // Cập nhật thông tin
        if (taiKhoanRequest.getMaVaiTro() != null) {
            taiKhoan.setMaVaiTro(taiKhoanRequest.getMaVaiTro());
        }
        if (taiKhoanRequest.getTenDangNhap() != null) {
            taiKhoan.setTenDangNhap(taiKhoanRequest.getTenDangNhap());
        }
        if (taiKhoanRequest.getMatKhau() != null && !taiKhoanRequest.getMatKhau().trim().isEmpty()) {
            taiKhoan.setMatKhau(taiKhoanRequest.getMatKhau());
        }
        if (taiKhoanRequest.getEmail() != null) {
            taiKhoan.setEmail(taiKhoanRequest.getEmail());
        }
        if (taiKhoanRequest.getTrangThai() != null) {
            taiKhoan.setTrangThai(taiKhoanRequest.getTrangThai());
        }
        if (taiKhoanRequest.getLanDangNhapCuoi() != null) {
            taiKhoan.setLanDangNhapCuoi(taiKhoanRequest.getLanDangNhapCuoi());
        }

        return taiKhoanRepository.save(taiKhoan);
    }

    /**
     * Xóa tài khoản
     * @param id ID của tài khoản cần xóa
     */
    public void deleteTK(UUID id){
        TaiKhoan taiKhoan = taiKhoanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại"));
        
        taiKhoanRepository.delete(taiKhoan);
    }

    /**
     * Reset mật khẩu tài khoản
     * @param id ID của tài khoản
     * @param newPassword Mật khẩu mới
     * @return Tài khoản đã được cập nhật
     */
    public TaiKhoan resetPassword(UUID id, String newPassword){
        TaiKhoan taiKhoan = taiKhoanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại"));
        
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new RuntimeException("Mật khẩu mới không được để trống");
        }
        
        taiKhoan.setMatKhau(newPassword);
        return taiKhoanRepository.save(taiKhoan);
    }

    /**
     * Khóa/Mở khóa tài khoản (toggle status)
     * @param id ID của tài khoản
     * @return Tài khoản đã được cập nhật
     */
    public TaiKhoan toggleStatus(UUID id){
        TaiKhoan taiKhoan = taiKhoanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại"));
        
        // Toggle status: 1 -> 0 hoặc 0 -> 1
        Integer newStatus = (taiKhoan.getTrangThai() == null || taiKhoan.getTrangThai() == 0) ? 1 : 0;
        taiKhoan.setTrangThai(newStatus);
        
        return taiKhoanRepository.save(taiKhoan);
    }

    /**
     * Lấy lịch sử đăng nhập (thông tin lần đăng nhập cuối)
     * @param id ID của tài khoản
     * @return Thông tin lần đăng nhập cuối
     */
    public Instant getLoginHistory(UUID id){
        TaiKhoan taiKhoan = taiKhoanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại"));
        
        return taiKhoan.getLanDangNhapCuoi();
    }

    /**
     * Tìm kiếm tài khoản theo tên đăng nhập (tương thích ngược)
     * @param tenDangNhap Tên đăng nhập (có thể là một phần)
     * @return Danh sách tài khoản khớp
     */
    public List<TaiKhoanDto> searchByTenDangNhap(String tenDangNhap){
        return searchTaiKhoan(tenDangNhap, null, null);
    }

    /**
     * Tìm kiếm tài khoản với nhiều điều kiện
     * @param tenDangNhap Tên đăng nhập (có thể là một phần, nullable)
     * @param tenVaiTro Tên vai trò (ADMIN, NHAN_VIEN, KHACH_HANG, nullable)
     * @param trangThai Trạng thái (0 hoặc 1, nullable)
     * @return Danh sách tài khoản khớp
     */
    public List<TaiKhoanDto> searchTaiKhoan(String tenDangNhap, String tenVaiTro, Integer trangThai){
        // Chuẩn hóa tham số
        String tenDangNhapParam = (tenDangNhap != null && !tenDangNhap.trim().isEmpty()) 
                ? tenDangNhap.trim() 
                : null;
        String tenVaiTroParam = (tenVaiTro != null && !tenVaiTro.trim().isEmpty()) 
                ? tenVaiTro.trim() 
                : null;
        
        return taiKhoanRepository.searchTaiKhoan(tenDangNhapParam, tenVaiTroParam, trangThai);
    }

}

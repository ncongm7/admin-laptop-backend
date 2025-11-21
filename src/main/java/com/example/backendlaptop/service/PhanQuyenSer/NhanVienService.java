package com.example.backendlaptop.service.PhanQuyenSer;


import com.example.backendlaptop.dto.phanQuyenDto.nhanVien.NhanVienDto;
import com.example.backendlaptop.dto.phanQuyenDto.nhanVien.NhanVienRequest;

import com.example.backendlaptop.entity.NhanVien;
import com.example.backendlaptop.entity.TaiKhoan;
import com.example.backendlaptop.entity.VaiTro;
import com.example.backendlaptop.repository.NhanVienRepository;
import com.example.backendlaptop.repository.TaiKhoanRepository;
import com.example.backendlaptop.repository.VaiTroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class NhanVienService {
    @Autowired
    private NhanVienRepository nhanVienRepository;
    
    @Autowired
    private TaiKhoanRepository taiKhoanRepository;
    
    @Autowired
    private VaiTroRepository vaiTroRepository;


    //    tìm full danh sach
    public List<NhanVienDto> findAllNV() {
        return nhanVienRepository.findNhanViensBy();
    }

    //tìm 1 khavh hang
    public NhanVien getOne(UUID id) {
        return nhanVienRepository.findById(id).orElse(null);
    }

    @Transactional
    public Map<String, String> addNV(NhanVienRequest nhanVienRequest) {
        TaiKhoan taiKhoan = null;
        String tenDangNhap = null;
        String matKhau = null;
        
        // 1. Tạo tài khoản nếu có flag createTaiKhoan = true
        if (Boolean.TRUE.equals(nhanVienRequest.getCreateTaiKhoan())) {
            // Xác định tên đăng nhập
            if (nhanVienRequest.getTenDangNhap() != null && !nhanVienRequest.getTenDangNhap().trim().isEmpty()) {
                tenDangNhap = nhanVienRequest.getTenDangNhap().trim();
            } else {
                // Mặc định dùng SĐT làm username
                tenDangNhap = nhanVienRequest.getSoDienThoai();
            }
            
            // Kiểm tra tên đăng nhập đã tồn tại chưa
            if (taiKhoanRepository.findByTenDangNhap(tenDangNhap).isPresent()) {
                throw new IllegalArgumentException("Tên đăng nhập đã được sử dụng");
            }
            
            // Xác định mật khẩu
            if (nhanVienRequest.getMatKhau() != null && !nhanVienRequest.getMatKhau().trim().isEmpty()) {
                matKhau = nhanVienRequest.getMatKhau().trim();
            } else {
                matKhau = "123456"; // Mật khẩu mặc định
            }
            
            // Xác định email - luôn dùng email nhân viên để tránh vi phạm UNIQUE constraint với NULL
            // Email nhân viên đã được set trong nhanVienRequest.getEmail()
            String email = nhanVienRequest.getEmail();
            if (email == null || email.trim().isEmpty()) {
                // Nếu không có email, tạo email tạm từ SĐT để tránh NULL
                email = tenDangNhap + "@temp.local"; // Dùng username + domain tạm
            } else {
                email = email.trim();
            }
            
            // Tạo tài khoản
            taiKhoan = new TaiKhoan();
            taiKhoan.setId(UUID.randomUUID());
            taiKhoan.setTenDangNhap(tenDangNhap);
            taiKhoan.setMatKhau(matKhau);
            taiKhoan.setEmail(email); // Luôn có giá trị để tránh UNIQUE constraint với NULL
            taiKhoan.setTrangThai(1); // Active
            taiKhoan.setNgayTao(Instant.now());
            
            // Gán vai trò
            VaiTro vaiTro = null;
            if (nhanVienRequest.getMaVaiTro() != null) {
                // Sử dụng vai trò từ request
                vaiTro = vaiTroRepository.findById(nhanVienRequest.getMaVaiTro())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy vai trò với ID: " + nhanVienRequest.getMaVaiTro()));
            } else {
                // Mặc định là NHAN_VIEN
                vaiTro = vaiTroRepository.findByMaVaiTro("NHAN_VIEN")
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy vai trò NHAN_VIEN trong database"));
            }
            taiKhoan.setMaVaiTro(vaiTro);
            
            taiKhoan = taiKhoanRepository.save(taiKhoan);
        }
        
        // 2. Tạo nhân viên
        NhanVien nhanVien = new NhanVien();
        if (taiKhoan != null) {
            nhanVien.setMaTaiKhoan(taiKhoan);
        }
        nhanVien.setMaNhanVien(nhanVienRequest.getMaNhanVien());
        nhanVien.setHoTen(nhanVienRequest.getHoTen());
        nhanVien.setSoDienThoai(nhanVienRequest.getSoDienThoai());
        nhanVien.setEmail(nhanVienRequest.getEmail());
        nhanVien.setGioiTinh(nhanVienRequest.getGioiTinh());
        nhanVien.setAnhNhanVien(nhanVienRequest.getAnhNhanVien());
        nhanVien.setChucVu(nhanVienRequest.getChucVu());
        nhanVien.setDiaChi(nhanVienRequest.getDiaChi());
        nhanVien.setDanhGia(nhanVienRequest.getDanhGia());
        nhanVien.setTrangThai(nhanVienRequest.getTrangThai());
        nhanVienRepository.save(nhanVien);
        
        // 3. Trả về thông tin đăng nhập nếu có tạo tài khoản
        Map<String, String> loginInfo = new HashMap<>();
        if (taiKhoan != null && tenDangNhap != null && matKhau != null) {
            loginInfo.put("tenDangNhap", tenDangNhap);
            loginInfo.put("matKhau", matKhau);
        }
        return loginInfo;
    }

    public void deleteNV(UUID id) {
        nhanVienRepository.deleteById(id);
    }
    @Transactional
    public void suaNV(UUID id, NhanVienRequest nhanVienRequest) {
        var nhanVien = nhanVienRepository.findById(id).orElseThrow(()-> new RuntimeException("Nhân viên k tồn tại"));
        
        // Lưu SĐT cũ để so sánh
        String soDienThoaiCu = nhanVien.getSoDienThoai();
        
        // Update thông tin nhân viên
        nhanVien.setMaNhanVien(nhanVienRequest.getMaNhanVien());
        nhanVien.setHoTen(nhanVienRequest.getHoTen());
        nhanVien.setSoDienThoai(nhanVienRequest.getSoDienThoai());
        nhanVien.setEmail(nhanVienRequest.getEmail());
        nhanVien.setGioiTinh(nhanVienRequest.getGioiTinh());
        nhanVien.setAnhNhanVien(nhanVienRequest.getAnhNhanVien());
        nhanVien.setChucVu(nhanVienRequest.getChucVu());
        nhanVien.setDiaChi(nhanVienRequest.getDiaChi());
        nhanVien.setDanhGia(nhanVienRequest.getDanhGia());
        nhanVien.setTrangThai(nhanVienRequest.getTrangThai());
        nhanVienRepository.save(nhanVien);
        
        // Update tài khoản nếu có thay đổi
        if (nhanVien.getMaTaiKhoan() != null) {
            TaiKhoan taiKhoan = nhanVien.getMaTaiKhoan();
            
            // Update tên đăng nhập nếu có
            if (nhanVienRequest.getTenDangNhap() != null && !nhanVienRequest.getTenDangNhap().trim().isEmpty()) {
                String tenDangNhapMoi = nhanVienRequest.getTenDangNhap().trim();
                // Kiểm tra xem username mới có bị trùng không (trừ chính nó)
                taiKhoanRepository.findByTenDangNhap(tenDangNhapMoi)
                    .ifPresent(tk -> {
                        if (!tk.getId().equals(taiKhoan.getId())) {
                            throw new IllegalArgumentException("Tên đăng nhập đã được sử dụng");
                        }
                    });
                taiKhoan.setTenDangNhap(tenDangNhapMoi);
            } else if (!nhanVienRequest.getSoDienThoai().equals(soDienThoaiCu)) {
                // Nếu không có tenDangNhap mới nhưng SĐT thay đổi, update username = SĐT mới
                String soDienThoaiMoi = nhanVienRequest.getSoDienThoai();
                // Kiểm tra xem SĐT mới có bị trùng không (trừ chính nó)
                taiKhoanRepository.findByTenDangNhap(soDienThoaiMoi)
                    .ifPresent(tk -> {
                        if (!tk.getId().equals(taiKhoan.getId())) {
                            throw new IllegalArgumentException("Số điện thoại đã được sử dụng làm tên đăng nhập");
                        }
                    });
                taiKhoan.setTenDangNhap(soDienThoaiMoi);
            }
            
            // Update mật khẩu nếu có (chỉ update nếu có giá trị)
            if (nhanVienRequest.getMatKhau() != null && !nhanVienRequest.getMatKhau().trim().isEmpty()) {
                taiKhoan.setMatKhau(nhanVienRequest.getMatKhau().trim());
            }
            
            // Update email trong tài khoản
            if (nhanVienRequest.getEmail() != null) {
                taiKhoan.setEmail(nhanVienRequest.getEmail());
            }
            
            taiKhoanRepository.save(taiKhoan);
        }
    }

    public Page<NhanVienDto> phanTrangNV(Integer pageNo, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<NhanVienDto> khachHangDtoPage =nhanVienRepository.phanTrangNV(pageable);
        return khachHangDtoPage;
    }
}

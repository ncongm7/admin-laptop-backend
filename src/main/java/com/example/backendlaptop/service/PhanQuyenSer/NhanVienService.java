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
        // 1. Kiểm tra SĐT đã được dùng làm username chưa
        String soDienThoai = nhanVienRequest.getSoDienThoai();
        if (taiKhoanRepository.findByTenDangNhap(soDienThoai).isPresent()) {
            throw new IllegalArgumentException("Số điện thoại đã được sử dụng làm tên đăng nhập");
        }
        
        // 2. Tạo tài khoản tự động
        TaiKhoan taiKhoan = new TaiKhoan();
        taiKhoan.setId(UUID.randomUUID());
        taiKhoan.setTenDangNhap(soDienThoai); // Username = SĐT
        // Mật khẩu mặc định: "123456" (có thể thay đổi)
        String defaultPassword = "123456";
        taiKhoan.setMatKhau(defaultPassword);
        taiKhoan.setEmail(nhanVienRequest.getEmail());
        taiKhoan.setTrangThai(1); // Active
        taiKhoan.setNgayTao(Instant.now());
        
        // 3. Gán role NHAN_VIEN
        VaiTro vaiTro = vaiTroRepository.findByMaVaiTro("NHAN_VIEN")
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vai trò NHAN_VIEN trong database"));
        taiKhoan.setMaVaiTro(vaiTro);
        
        taiKhoan = taiKhoanRepository.save(taiKhoan);
        
        // 4. Tạo nhân viên và liên kết với tài khoản
        NhanVien nhanVien = new NhanVien();
        nhanVien.setMaTaiKhoan(taiKhoan);
        nhanVien.setMaNhanVien(nhanVienRequest.getMaNhanVien());
        nhanVien.setHoTen(nhanVienRequest.getHoTen());
        nhanVien.setSoDienThoai(soDienThoai);
        nhanVien.setEmail(nhanVienRequest.getEmail());
        nhanVien.setGioiTinh(nhanVienRequest.getGioiTinh());
        nhanVien.setAnhNhanVien(nhanVienRequest.getAnhNhanVien());
        nhanVien.setChucVu(nhanVienRequest.getChucVu());
        nhanVien.setDiaChi(nhanVienRequest.getDiaChi());
        nhanVien.setDanhGia(nhanVienRequest.getDanhGia());
        nhanVien.setTrangThai(nhanVienRequest.getTrangThai());
        nhanVienRepository.save(nhanVien);
        
        // 5. Trả về thông tin đăng nhập
        Map<String, String> loginInfo = new HashMap<>();
        loginInfo.put("tenDangNhap", soDienThoai);
        loginInfo.put("matKhau", defaultPassword);
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

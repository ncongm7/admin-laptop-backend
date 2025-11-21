package com.example.backendlaptop.service.PhanQuyenSer;

import com.example.backendlaptop.dto.phanQuyenDto.khachHang.KhachHangDto;
import com.example.backendlaptop.dto.phanQuyenDto.khachHang.KhachHangRequest;
import com.example.backendlaptop.entity.KhachHang;
import com.example.backendlaptop.entity.TaiKhoan;
import com.example.backendlaptop.entity.VaiTro;
import com.example.backendlaptop.repository.KhachHangRepository;
import com.example.backendlaptop.repository.TaiKhoanRepository;
import com.example.backendlaptop.repository.VaiTroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class KhachHangService {

    @Autowired
    private KhachHangRepository khachHangRepository;
    
    @Autowired
    private TaiKhoanRepository taiKhoanRepository;
    
    @Autowired
    private VaiTroRepository vaiTroRepository;

    //    tìm full danh sach
    public List<KhachHangDto> findAllKH() {
        return khachHangRepository.findAllKhachHang();
    }

    //tìm 1 khavh hang
    public KhachHang getOne(UUID id) {
        return khachHangRepository.findById(id).orElse(null);
    }

    //tìm khách hàng theo mã
    public KhachHangDto findByMaKhachHang(String maKhachHang) {
        return khachHangRepository.findByMaKhachHangDto(maKhachHang);
    }

//    tìm kiếm khách hàng
    public List<KhachHangDto> timKiem(String ten, String sdt){
        return khachHangRepository.searchByMultiField(ten,sdt);
    }

//   Tuwju tao ma
public String generateMaKhachHang() {
    String lastCode = khachHangRepository.findLastMaKhachHangOfYear();
    int year = java.time.Year.now().getValue();
    int nextNumber = 1;

    if (lastCode != null && lastCode.startsWith("KH" + year)) {
        // Lấy phần số cuối cùng sau dấu '-'
        String numberPart = lastCode.substring(lastCode.lastIndexOf('-') + 1);
        nextNumber = Integer.parseInt(numberPart) + 1;
    }

    // Định dạng mã: KH2025-001
    return String.format("KH%d-%03d", year, nextNumber);
}
//Thêm khách hàng
    @Transactional
    public Map<String, String> addKH(KhachHangRequest khachHangRequest) {
        TaiKhoan taiKhoan = null;
        String tenDangNhap = null;
        String matKhau = null;
        
        // 1. Tạo tài khoản nếu có flag createTaiKhoan = true
        if (Boolean.TRUE.equals(khachHangRequest.getCreateTaiKhoan())) {
            // Xác định tên đăng nhập
            if (khachHangRequest.getTenDangNhap() != null && !khachHangRequest.getTenDangNhap().trim().isEmpty()) {
                tenDangNhap = khachHangRequest.getTenDangNhap().trim();
            } else {
                // Mặc định dùng SĐT làm username
                tenDangNhap = khachHangRequest.getSoDienThoai();
            }
            
            // Kiểm tra tên đăng nhập đã tồn tại chưa
            if (taiKhoanRepository.findByTenDangNhap(tenDangNhap).isPresent()) {
                throw new IllegalArgumentException("Tên đăng nhập đã được sử dụng");
            }
            
            // Xác định mật khẩu
            if (khachHangRequest.getMatKhau() != null && !khachHangRequest.getMatKhau().trim().isEmpty()) {
                matKhau = khachHangRequest.getMatKhau().trim();
            } else {
                matKhau = "123456"; // Mật khẩu mặc định
            }
            
            // Xác định email - luôn dùng email khách hàng để tránh vi phạm UNIQUE constraint với NULL
            // Email khách hàng đã được set trong khachHangRequest.getEmail()
            String email = khachHangRequest.getEmail();
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
            if (khachHangRequest.getMaVaiTro() != null) {
                // Sử dụng vai trò từ request
                vaiTro = vaiTroRepository.findById(khachHangRequest.getMaVaiTro())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy vai trò với ID: " + khachHangRequest.getMaVaiTro()));
            } else {
                // Mặc định là KHACH_HANG
                vaiTro = vaiTroRepository.findByMaVaiTro("KHACH_HANG")
                        .orElse(null); // Khách hàng có thể không có vai trò
            }
            if (vaiTro != null) {
                taiKhoan.setMaVaiTro(vaiTro);
            }
            
            taiKhoan = taiKhoanRepository.save(taiKhoan);
        }
        
        // 2. Tạo khách hàng
        var khachHang = new KhachHang();
        if (taiKhoan != null) {
            khachHang.setMaTaiKhoan(taiKhoan);
        }
        khachHang.setMaKhachHang(khachHangRequest.getMaKhachHang());
        khachHang.setHoTen(khachHangRequest.getHoTen());
        khachHang.setSoDienThoai(khachHangRequest.getSoDienThoai());
        khachHang.setEmail(khachHangRequest.getEmail());
        khachHang.setGioiTinh(khachHangRequest.getGioiTinh());
        khachHang.setNgaySinh(khachHangRequest.getNgaySinh());
        khachHang.setTrangThai(khachHangRequest.getTrangThai());

        khachHangRepository.save(khachHang);
        
        // 3. Trả về thông tin đăng nhập nếu có tạo tài khoản
        Map<String, String> loginInfo = new HashMap<>();
        if (taiKhoan != null && tenDangNhap != null && matKhau != null) {
            loginInfo.put("tenDangNhap", tenDangNhap);
            loginInfo.put("matKhau", matKhau);
        }
        return loginInfo;
    }

    public void updateKH( UUID id, KhachHangRequest khachHangRequest) {
//        tìm id cânf xoá
        var khachHang =khachHangRepository.findById(id).orElseThrow(()-> new RuntimeException("Khách hàng k tồn tại"));
        khachHang.setMaKhachHang(khachHangRequest.getMaKhachHang());
        khachHang.setHoTen(khachHangRequest.getHoTen());
        khachHang.setSoDienThoai(khachHangRequest.getSoDienThoai());
        khachHang.setEmail(khachHangRequest.getEmail());
        khachHang.setGioiTinh(khachHangRequest.getGioiTinh());
        khachHang.setNgaySinh(khachHangRequest.getNgaySinh());
        khachHang.setTrangThai(khachHangRequest.getTrangThai());
        khachHangRepository.save(khachHang);
    }

    public void xoaKH(UUID id) {
        khachHangRepository.deleteById(id);
    }

    public Page<KhachHangDto> phanTrangKH(Integer pageNo, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<KhachHangDto> khachHangDtoPage =khachHangRepository.phanTrangKH(pageable);
        return khachHangDtoPage;
    }

//    tính tổng tiền sử dụng
    public BigDecimal tongTienMua(UUID id) {
      BigDecimal result = khachHangRepository.tongTienMua(id);
      return result != null ? result : BigDecimal.ZERO;
    }


}

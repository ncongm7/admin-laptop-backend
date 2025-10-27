package com.example.backendlaptop.service;

import com.example.backendlaptop.dto.diaChi.DiaChiDto;
import com.example.backendlaptop.dto.diaChi.DiaChiRequest;
import com.example.backendlaptop.entity.DiaChi;
import com.example.backendlaptop.entity.KhachHang;
import com.example.backendlaptop.repository.DiaChiRepository;
import com.example.backendlaptop.repository.KhachHangRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class DiaChiService {
    @Autowired
    private DiaChiRepository diaChiRepository;
    
    @Autowired
    private KhachHangRepository khachHangRepository;

    // Lấy tất cả địa chỉ
    public List<DiaChiDto> getAllDiaChi() {
        return diaChiRepository.lstDiaChi();
    }

    // Lấy địa chỉ theo mã khách hàng
    public List<DiaChiDto> findByMaKhachHang(String maKhachHang) {
        return diaChiRepository.findByMaKhachHang(maKhachHang);
    }

    // Lấy 1 địa chỉ theo ID
    public DiaChi getOne(UUID id) {
        return diaChiRepository.findById(id).orElse(null);
    }

    // Thêm địa chỉ mới
    @Transactional
    public void addDiaChi(DiaChiRequest diaChiRequest) {
        // Tìm khách hàng theo mã
        KhachHang khachHang = khachHangRepository.findByMaKhachHang(diaChiRequest.getMaKhachHang())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng với mã: " + diaChiRequest.getMaKhachHang()));

        // Nếu đánh dấu là mặc định, hủy tất cả địa chỉ mặc định khác của khách hàng
        if (diaChiRequest.getMacDinh() != null && diaChiRequest.getMacDinh()) {
            diaChiRepository.findMacDinhByMaKhachHang(diaChiRequest.getMaKhachHang())
                    .ifPresent(dc -> dc.setMacDinh(false));
        }

        // Tạo địa chỉ mới
        DiaChi diaChi = new DiaChi();
        diaChi.setUser(khachHang);
        diaChi.setHoTen(diaChiRequest.getHoTen());
        diaChi.setSdt(diaChiRequest.getSdt());
        diaChi.setDiaChi(diaChiRequest.getDiaChi());
        diaChi.setXa(diaChiRequest.getXa());
        diaChi.setTinh(diaChiRequest.getTinh());
        diaChi.setMacDinh(diaChiRequest.getMacDinh() != null ? diaChiRequest.getMacDinh() : false);

        diaChiRepository.save(diaChi);
    }

    // Cập nhật địa chỉ
    @Transactional
    public void updateDiaChi(UUID id, DiaChiRequest diaChiRequest) {
        DiaChi diaChi = diaChiRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Địa chỉ không tồn tại"));

        // Nếu đánh dấu là mặc định và khác với mặc định hiện tại, hủy các địa chỉ mặc định khác
        if (diaChiRequest.getMacDinh() != null && diaChiRequest.getMacDinh()) {
            String maKhachHang = diaChi.getUser().getMaKhachHang();
            diaChiRepository.findMacDinhByMaKhachHang(maKhachHang)
                    .ifPresent(dc -> {
                        if (!dc.getId().equals(id)) {
                            dc.setMacDinh(false);
                        }
                    });
        }

        diaChi.setHoTen(diaChiRequest.getHoTen());
        diaChi.setSdt(diaChiRequest.getSdt());
        diaChi.setDiaChi(diaChiRequest.getDiaChi());
        diaChi.setXa(diaChiRequest.getXa());
        diaChi.setTinh(diaChiRequest.getTinh());
        diaChi.setMacDinh(diaChiRequest.getMacDinh() != null ? diaChiRequest.getMacDinh() : false);

        diaChiRepository.save(diaChi);
    }

    // Xóa địa chỉ
    public void deleteDiaChi(UUID id) {
        diaChiRepository.deleteById(id);
    }
}

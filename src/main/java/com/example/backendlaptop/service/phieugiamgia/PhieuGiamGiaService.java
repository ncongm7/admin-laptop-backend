package com.example.backendlaptop.service.phieugiamgia;

import com.example.backendlaptop.entity.PhieuGiamGia;
import com.example.backendlaptop.expection.ApiException;
import com.example.backendlaptop.model.request.phieugiamgia.PhieuGiamGiaRequest;
import com.example.backendlaptop.model.response.phieugiamgia.PhieuGiamGiaResponse;
import com.example.backendlaptop.repository.PhieuGiamGiaRepository;
import com.example.backendlaptop.until.CheckNgayBatDauKetThuc;
import com.example.backendlaptop.until.MapperUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PhieuGiamGiaService {

    @Autowired
    private PhieuGiamGiaRepository repository;

    private void validate(PhieuGiamGiaRequest req, UUID idForUpdate) {
        // 1) Validate ngày (ném lỗi nếu sai)
        CheckNgayBatDauKetThuc.status(req.getNgayBatDau(), req.getNgayKetThuc()); // chỉ để validate

        // 2) Unique code
        String ma = req.getMa().trim();
        var dup = repository.findByMaIgnoreCase(ma);
        if (dup.isPresent() && (idForUpdate == null || !dup.get().getId().equals(idForUpdate))) {
            throw new IllegalArgumentException("Trùng mã");
        }

        // 3) % thì 0..100
        if (Integer.valueOf(0).equals(req.getLoaiPhieuGiamGia())) {
            if (req.getGiaTriGiamGia() == null)
                throw new IllegalArgumentException("Giá trị giảm (%) không được để trống");
            var v = req.getGiaTriGiamGia();
            if (v.compareTo(new java.math.BigDecimal("0")) < 0 ||
                v.compareTo(new java.math.BigDecimal("100")) > 0) {
                throw new IllegalArgumentException("Giá trị giảm (%) phải trong khoảng 0 đến 100");
            }
        }
    }

    public void add(PhieuGiamGiaRequest req) {
        validate(req, null);

        PhieuGiamGia e = MapperUtils.map(req, PhieuGiamGia.class);
        e.setMa(req.getMa().trim());

        // Công tắc quản trị
        e.setTrangThai(req.getTrangThai() == null ? 1 : req.getTrangThai()); // 1=Bật mặc định

        // VND: cap = value (giữ đúng với FE hiện tại)
        if (Integer.valueOf(1).equals(e.getLoaiPhieuGiamGia())) {
            e.setSoTienGiamToiDa(e.getGiaTriGiamGia());
        }
        repository.save(e);
    }

    public void update(PhieuGiamGiaRequest req, UUID id) {
        PhieuGiamGia existed = repository.findById(id)
                .orElseThrow(() -> new ApiException("Not Found", "NF"));

        validate(req, id);

        MapperUtils.mapToExisting(req, existed);
        existed.setMa(req.getMa().trim());

        // Công tắc quản trị: nhận từ request (nếu null thì giữ nguyên)
        if (req.getTrangThai() != null) {
            existed.setTrangThai(req.getTrangThai());
        }

        if (Integer.valueOf(1).equals(existed.getLoaiPhieuGiamGia())) {
            existed.setSoTienGiamToiDa(existed.getGiaTriGiamGia());
        }
        repository.save(existed);
    }

    // --- Các hàm CRUD khác giữ nguyên ---
    public List<PhieuGiamGiaResponse> getAll(){
        return repository.findAll().stream().map(PhieuGiamGiaResponse::new).toList();
    }

    public void delete(UUID id) {
        repository.findById(id).orElseThrow(
                () -> new ApiException("Not Found", "NF")
        );
        repository.deleteById(id);
    }

    public PhieuGiamGiaResponse detail(UUID id){
        return new PhieuGiamGiaResponse(repository.findById(id).orElseThrow(() -> new ApiException("Not Found","NF")));
    }
}

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
import java.util.Optional;
import java.util.UUID;

@Service
public class PhieuGiamGiaService {

    @Autowired
    private PhieuGiamGiaRepository repository;

    // --- Phương thức Validate Dùng Chung ---
    /**
     * Kiểm tra tính hợp lệ của request, bao gồm ngày tháng và tính duy nhất của mã.
     *
     * @param request Dữ liệu voucher.
     * @param id ID của voucher (null nếu là add, có giá trị nếu là update).
     * @throws IllegalArgumentException nếu có lỗi logic (ngày tháng/trùng mã).
     */

    /** Validate chung và trả về trạng thái 0/1/2 (dùng hàm bạn đã có). */
    private int validateAndStatus(PhieuGiamGiaRequest req, UUID idForUpdate) {
        // 1) Ngày: dùng hàm đã có (ném lỗi nếu sai) + nhận trạng thái
        int status = CheckNgayBatDauKetThuc.status(req.getNgayBatDau(), req.getNgayKetThuc());

        // 2) Mã: trim + unique (cho add/update)
        String ma = req.getMa().trim();
        var dup = repository.findByMaIgnoreCase(ma);
        if (dup.isPresent() && (idForUpdate == null || !dup.get().getId().equals(idForUpdate))) {
            throw new IllegalArgumentException("Trùng mã");
        }

        // 3) Loại % (0) ⇒ giá trị giảm ∈ [0, 100]
        if (Integer.valueOf(0).equals(req.getLoaiPhieuGiamGia())) {
            if (req.getGiaTriGiamGia() == null) {
                throw new IllegalArgumentException("Giá trị giảm (%) không được để trống");
            }
            var v = req.getGiaTriGiamGia();
            if (v.compareTo(new java.math.BigDecimal("0")) < 0 ||
                    v.compareTo(new java.math.BigDecimal("100")) > 0) {
                throw new IllegalArgumentException("Giá trị giảm (%) phải trong khoảng 0 đến 100");
            }
        }
        return status;
    }

    public void add(PhieuGiamGiaRequest req) {
        int trangThai = validateAndStatus(req, null);

        PhieuGiamGia e = MapperUtils.map(req, PhieuGiamGia.class);
        e.setMa(req.getMa().trim());
        e.setTrangThai(trangThai);

        // 1 = VND ⇒ cap = value
        if (Integer.valueOf(1).equals(e.getLoaiPhieuGiamGia())) {
            e.setSoTienGiamToiDa(e.getGiaTriGiamGia());
        }
        repository.save(e);
    }

    public void update(PhieuGiamGiaRequest req, UUID id) {
        PhieuGiamGia existed = repository.findById(id)
                .orElseThrow(() -> new ApiException("Not Found", "NF"));

        int trangThai = validateAndStatus(req, id);

        MapperUtils.mapToExisting(req, existed);
        existed.setMa(req.getMa().trim());
        existed.setTrangThai(trangThai);

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
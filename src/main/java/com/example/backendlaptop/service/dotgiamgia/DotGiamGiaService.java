package com.example.backendlaptop.service.dotgiamgia;

import com.example.backendlaptop.entity.DotGiamGia;
import com.example.backendlaptop.expection.ApiException;
import com.example.backendlaptop.model.request.dotgiamgia.DotGiamGiaRequest;
import com.example.backendlaptop.model.response.dotgiamgia.DotGiamGiaResponse;
import com.example.backendlaptop.repository.DotGiamGiaChiTietRepository;
import com.example.backendlaptop.repository.DotGiamGiaRepository;
import com.example.backendlaptop.until.CheckNgayBatDauKetThuc;
import com.example.backendlaptop.until.MapperUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class DotGiamGiaService {
    @Autowired
    private DotGiamGiaRepository repository;

    @Autowired
    private DotGiamGiaChiTietRepository dggctRepository;

    private void validate(DotGiamGiaRequest req) {
        // 1) Validate ngày (ném lỗi nếu sai)
        CheckNgayBatDauKetThuc.status(req.getNgayBatDau(), req.getNgayKetThuc()); // chỉ để validate

        // 2) Validate loại giảm giá và giá trị
        if (req.getLoaiDotGiamGia() == null) {
            throw new IllegalArgumentException("Loại đợt giảm giá không được để trống");
        }

        // 3) Nếu là giảm theo % (loai = 1), validate giá trị 0-100
        if (Integer.valueOf(1).equals(req.getLoaiDotGiamGia())) {
            if (req.getGiaTri() == null) {
                throw new IllegalArgumentException("Giá trị giảm (%) không được để trống");
            }
            BigDecimal v = req.getGiaTri();
            if (v.compareTo(BigDecimal.ZERO) < 0 || v.compareTo(new BigDecimal("100")) > 0) {
                throw new IllegalArgumentException("Giá trị giảm (%) phải trong khoảng 0 đến 100");
            }
        }
        // Nếu là giảm theo VND (loai = 2), chỉ cần >= 0
        else if (Integer.valueOf(2).equals(req.getLoaiDotGiamGia())) {
            if (req.getGiaTri() == null) {
                throw new IllegalArgumentException("Giá trị giảm (VND) không được để trống");
            }
            if (req.getGiaTri().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Giá trị giảm (VND) phải >= 0");
            }
        } else {
            throw new IllegalArgumentException("Loại đợt giảm giá không hợp lệ (chỉ chấp nhận 1: % hoặc 2: VND)");
        }
    }

    public List<DotGiamGiaResponse> getAll() {
        return repository.findAll().stream().map(DotGiamGiaResponse::new).toList();
    }

    public void delete(UUID id) {
        repository.findById(id).orElseThrow(
                () -> new ApiException("Not Found", "NF")
        );
        repository.deleteById(id);
    }

    public void add(DotGiamGiaRequest req) {
        validate(req);
        DotGiamGia dotGiamGia = MapperUtils.map(req, DotGiamGia.class);

        // Công tắc quản trị
        dotGiamGia.setTrangThai(req.getTrangThai() == null ? 1 : req.getTrangThai()); // 1=Bật mặc định

        // Nếu là giảm theo VND (loai = 2), set soTienGiamToiDa = giaTri (giống PhieuGiamGia)
        if (Integer.valueOf(2).equals(dotGiamGia.getLoaiDotGiamGia())) {
            dotGiamGia.setSoTienGiamToiDa(dotGiamGia.getGiaTri());
        }

        repository.save(dotGiamGia);
    }

    @Transactional
    public void update(DotGiamGiaRequest request, UUID id) {
        DotGiamGia dot = repository.findById(id).orElseThrow(() -> new ApiException("Not Found", "NF"));

        validate(request);

        MapperUtils.mapToExisting(request, dot);

        // Công tắc quản trị: nhận từ request (nếu null thì giữ nguyên)
        if (request.getTrangThai() != null) {
            dot.setTrangThai(request.getTrangThai());
        }

        // Nếu là giảm theo VND (loai = 2), set soTienGiamToiDa = giaTri
        if (Integer.valueOf(2).equals(dot.getLoaiDotGiamGia())) {
            dot.setSoTienGiamToiDa(dot.getGiaTri());
        }

        repository.save(dot);

        // Reprice toàn bộ chi tiết thuộc đợt (sử dụng loại giảm giá mới)
        dggctRepository.bulkRepriceByDot(dot.getId(), dot.getGiaTri(), dot.getLoaiDotGiamGia(), dot.getSoTienGiamToiDa());
    }

    public DotGiamGiaResponse detail(UUID id) {
        return new DotGiamGiaResponse(repository.findById(id).orElseThrow(() -> new ApiException("Not Found", "NF")));
    }
}

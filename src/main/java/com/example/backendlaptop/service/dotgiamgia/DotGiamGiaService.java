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

        repository.save(dot);

        // Reprice toàn bộ chi tiết thuộc đợt
        var newGiaTri = java.math.BigDecimal.valueOf(dot.getGiaTri() == null ? 0 : dot.getGiaTri());
        dggctRepository.bulkRepriceByDot(dot.getId(), newGiaTri);
    }

    public DotGiamGiaResponse detail(UUID id) {
        return new DotGiamGiaResponse(repository.findById(id).orElseThrow(() -> new ApiException("Not Found", "NF")));
    }
}

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

import java.util.List;
import java.util.UUID;

@Service
public class DotGiamGiaService {
    @Autowired
    private DotGiamGiaRepository repository;
    @Autowired
    private DotGiamGiaChiTietService dggctService;
    @Autowired
    private DotGiamGiaChiTietRepository dggctRepository;


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
        DotGiamGia dotGiamGia = MapperUtils.map(req, DotGiamGia.class);
        int status = CheckNgayBatDauKetThuc.status(req.getNgayBatDau(), req.getNgayKetThuc());
        dotGiamGia.setTrangThai(status);
        repository.save(dotGiamGia);
    }

    @org.springframework.transaction.annotation.Transactional
    public void update(DotGiamGiaRequest request, UUID id) {
        var dot = repository.findById(id).orElseThrow(() -> new ApiException("Not Found", "NF"));
        MapperUtils.mapToExisting(request, dot);
        dot.setId(id);
        int status = CheckNgayBatDauKetThuc.status(request.getNgayBatDau(), request.getNgayKetThuc());
        dot.setTrangThai(status);
        repository.save(dot);

        // Reprice toàn bộ chi tiết thuộc đợt + làm đẹp giá xuống 1.000
        var newGiaTri = java.math.BigDecimal.valueOf(dot.getGiaTri() == null ? 0 : dot.getGiaTri());
        dggctRepository.bulkRepriceByDot(dot.getId(), newGiaTri);
    }

    public DotGiamGiaResponse detail(UUID id) {
        return new DotGiamGiaResponse(repository.findById(id).orElseThrow(() -> new ApiException("Not Found", "NF")));
    }
}

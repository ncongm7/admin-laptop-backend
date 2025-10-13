package com.example.backendlaptop.service.baohanh;

import com.example.backendlaptop.entity.LichSuBaoHanh;
import com.example.backendlaptop.entity.PhieuBaoHanh;
import com.example.backendlaptop.entity.PhieuGiamGia;
import com.example.backendlaptop.expection.ApiException;
import com.example.backendlaptop.model.request.phieugiamgia.PhieuGiamGiaRequest;
import com.example.backendlaptop.model.response.baohanh.LichSuBaoHanhResponse;
import com.example.backendlaptop.model.response.baohanh.PhieuBaoHanhResponse;
import com.example.backendlaptop.model.response.phieugiamgia.PhieuGiamGiaResponse;
import com.example.backendlaptop.repository.LichSuBaoHanhRepository;
import com.example.backendlaptop.repository.PhieuBaoHanhRepository;
import com.example.backendlaptop.until.MapperUtils;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class LichSuBaoHanhService {
    @Autowired
    LichSuBaoHanhRepository repository;
    @Autowired
    PhieuBaoHanhRepository phieuBaoHanhRepository;

    public List<LichSuBaoHanhResponse> listByPhieuBaoHanhId(UUID phieuBaoHanhId) {
        if (phieuBaoHanhId == null) {
            throw new ApiException("Thiếu id phiếu bảo hành", "NULL");
        }

        // Kiểm tra tồn tại PBH để báo lỗi rõ ràng nếu nhập sai id
        if (!phieuBaoHanhRepository.existsById(phieuBaoHanhId)) {
            throw new ApiException("Không tìm thấy phiếu bảo hành", "NOT_FOUND");
        }

        return repository.findAllByIdBaoHanh_Id(phieuBaoHanhId)
                .stream()
                .map(LichSuBaoHanhResponse::new)
                .toList();
    }
    public void delete(UUID id) {
        repository.findById(id).orElseThrow(
                () -> new ApiException("Not Found", "NF")
        );
        repository.deleteById(id);
    }
    @Transactional
    public LichSuBaoHanhResponse add(UUID idPhieuBaoHanh, String moTaRaw) {
        if (idPhieuBaoHanh == null) {
            throw new ApiException("Thiếu id phiếu bảo hành", "NULL");
        }
        if (moTaRaw == null || moTaRaw.trim().isEmpty()) {
            throw new ApiException("Thiếu mô tả lỗi", "NULL");
        }

        PhieuBaoHanh pbh = phieuBaoHanhRepository.findById(idPhieuBaoHanh)
                .orElseThrow(() -> new ApiException("Không tìm thấy phiếu bảo hành", "NOT_FOUND"));

        Instant start = Instant.now();
        Instant end = start.plus(14, ChronoUnit.DAYS); // +2 tuần

        LichSuBaoHanh e = new LichSuBaoHanh();
        // @UuidGenerator trên entity sẽ tự sinh id
        e.setIdBaoHanh(pbh);
        e.setNgayTiepNhan(start);
        e.setNgayHoanThanh(end);
        e.setMoTaLoi(moTaRaw.trim());
        e.setTrangThai(1); // Đang xử lý

        LichSuBaoHanh saved = repository.save(e);
        return new LichSuBaoHanhResponse(saved);
    }

}

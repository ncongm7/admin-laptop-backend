package com.example.backendlaptop.service.baohanh;

import com.example.backendlaptop.expection.ApiException;
import com.example.backendlaptop.model.response.baohanh.LichSuBaoHanhResponse;
import com.example.backendlaptop.model.response.baohanh.PhieuBaoHanhResponse;
import com.example.backendlaptop.model.response.phieugiamgia.PhieuGiamGiaResponse;
import com.example.backendlaptop.repository.LichSuBaoHanhRepository;
import com.example.backendlaptop.repository.PhieuBaoHanhRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

}

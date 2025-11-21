package com.example.backendlaptop.service.trahang;

import com.example.backendlaptop.dto.trahang.YeuCauTraHangResponse;
import com.example.backendlaptop.entity.NhanVien;
import com.example.backendlaptop.entity.YeuCauTraHang;
import com.example.backendlaptop.expection.ApiException;
import com.example.backendlaptop.repository.NhanVienRepository;
import com.example.backendlaptop.repository.YeuCauTraHangRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class YeuCauTraHangQuanLyService {
    
    @Autowired
    private YeuCauTraHangRepository repository;
    
    @Autowired
    private NhanVienRepository nhanVienRepository;

    @Transactional(readOnly = true)
    public List<YeuCauTraHangResponse> getAll() {
        return repository.findAll().stream()
                .map(YeuCauTraHangResponse::new)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public YeuCauTraHangResponse getById(UUID id) {
        YeuCauTraHang entity = repository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy yêu cầu trả hàng", "NOT_FOUND"));
        return new YeuCauTraHangResponse(entity);
    }
    
    @Transactional
    public YeuCauTraHangResponse updateTrangThai(UUID id, Integer trangThai, String lyDoTuChoi, UUID idNhanVienXuLy) {
        YeuCauTraHang entity = repository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy yêu cầu trả hàng", "NOT_FOUND"));
        
        // Validate trạng thái hợp lệ: 0=Chờ duyệt, 1=Đã duyệt, 2=Từ chối, 3=Hoàn tất
        if (trangThai < 0 || trangThai > 3) {
            throw new ApiException("Trạng thái không hợp lệ", "INVALID_STATUS");
        }
        
        // Cập nhật trạng thái
        entity.setTrangThai(trangThai);
        entity.setNgaySua(Instant.now());
        
        // Nếu từ chối, lưu lý do từ chối
        if (trangThai == 2 && lyDoTuChoi != null && !lyDoTuChoi.trim().isEmpty()) {
            entity.setLyDoTuChoi(lyDoTuChoi.trim());
        }
        
        // Nếu duyệt, lưu ngày duyệt và nhân viên xử lý
        if (trangThai == 1) {
            entity.setNgayDuyet(Instant.now());
            if (idNhanVienXuLy != null) {
                NhanVien nhanVien = nhanVienRepository.findById(idNhanVienXuLy)
                        .orElseThrow(() -> new ApiException("Không tìm thấy nhân viên", "NOT_FOUND"));
                entity.setIdNhanVienXuLy(nhanVien);
            }
        }
        
        // Nếu hoàn tất, lưu ngày hoàn tất
        if (trangThai == 3) {
            entity.setNgayHoanTat(Instant.now());
        }
        
        repository.save(entity);
        return new YeuCauTraHangResponse(repository.findById(id).orElseThrow());
    }
    
    public void delete(UUID id) {
        repository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy yêu cầu trả hàng", "NOT_FOUND"));
        repository.deleteById(id);
    }
}


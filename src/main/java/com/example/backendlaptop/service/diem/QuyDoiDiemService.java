package com.example.backendlaptop.service.diem;

import com.example.backendlaptop.entity.QuyDoiDiem;
import com.example.backendlaptop.model.response.diem.QuyDoiDiemResponse;
import com.example.backendlaptop.model.request.diem.QuyDoiDiemRequest;
import com.example.backendlaptop.repository.QuyDoiDiemRepository;
import com.example.backendlaptop.expection.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class QuyDoiDiemService {
    
    @Autowired
    private QuyDoiDiemRepository quyDoiDiemRepository;
    
    /**
     * Lấy tất cả quy đổi điểm
     */
    public List<QuyDoiDiemResponse> getAllQuyDoiDiem() {
        List<QuyDoiDiem> quyDoiDiemList = quyDoiDiemRepository.findAll();
        return quyDoiDiemList.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Lấy quy đổi điểm đang hoạt động
     * Trả về null nếu không có quy đổi điểm đang hoạt động (thay vì throw exception)
     */
    public QuyDoiDiemResponse getQuyDoiDiemDangHoatDong() {
        QuyDoiDiem quyDoiDiem = quyDoiDiemRepository.findFirstByTrangThaiOrderByIdAsc(1);
        if (quyDoiDiem == null) {
            return null; // Trả về null thay vì throw exception để frontend có thể xử lý
        }
        return convertToResponse(quyDoiDiem);
    }
    
    /**
     * Tạo quy đổi điểm mới (admin)
     */
    @Transactional
    public QuyDoiDiemResponse createQuyDoiDiem(QuyDoiDiemRequest request) {
        // Validate
        if (request.getTienTichDiem() == null || request.getTienTichDiem().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new ApiException("Tiền tích điểm phải lớn hơn 0", "INVALID_TIEN_TICH_DIEM");
        }
        if (request.getTienTieuDiem() == null || request.getTienTieuDiem().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new ApiException("Tiền tiêu điểm phải lớn hơn 0", "INVALID_TIEN_TIEU_DIEM");
        }
        
        // Nếu đang set làm active, vô hiệu hóa các quy đổi điểm active khác
        if (request.getTrangThai() != null && request.getTrangThai() == 1) {
            List<QuyDoiDiem> activeList = quyDoiDiemRepository.findByTrangThai(1);
            for (QuyDoiDiem qdd : activeList) {
                qdd.setTrangThai(0);
                quyDoiDiemRepository.save(qdd);
            }
        }
        
        QuyDoiDiem quyDoiDiem = new QuyDoiDiem();
        quyDoiDiem.setId(UUID.randomUUID());
        quyDoiDiem.setTienTichDiem(request.getTienTichDiem());
        quyDoiDiem.setTienTieuDiem(request.getTienTieuDiem());
        quyDoiDiem.setTrangThai(request.getTrangThai() != null ? request.getTrangThai() : 0);
        
        quyDoiDiem = quyDoiDiemRepository.save(quyDoiDiem);
        return convertToResponse(quyDoiDiem);
    }
    
    /**
     * Cập nhật quy đổi điểm (admin)
     */
    @Transactional
    public QuyDoiDiemResponse updateQuyDoiDiem(UUID id, QuyDoiDiemRequest request) {
        QuyDoiDiem quyDoiDiem = quyDoiDiemRepository.findById(id)
            .orElseThrow(() -> new ApiException("Không tìm thấy quy đổi điểm", "NOT_FOUND"));
        
        // Validate
        if (request.getTienTichDiem() != null && request.getTienTichDiem().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new ApiException("Tiền tích điểm phải lớn hơn 0", "INVALID_TIEN_TICH_DIEM");
        }
        if (request.getTienTieuDiem() != null && request.getTienTieuDiem().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new ApiException("Tiền tiêu điểm phải lớn hơn 0", "INVALID_TIEN_TIEU_DIEM");
        }
        
        // Nếu đang set làm active, vô hiệu hóa các quy đổi điểm active khác (trừ chính nó)
        if (request.getTrangThai() != null && request.getTrangThai() == 1) {
            List<QuyDoiDiem> activeList = quyDoiDiemRepository.findByTrangThai(1);
            for (QuyDoiDiem qdd : activeList) {
                if (!qdd.getId().equals(id)) {
                    qdd.setTrangThai(0);
                    quyDoiDiemRepository.save(qdd);
                }
            }
        }
        
        if (request.getTienTichDiem() != null) {
            quyDoiDiem.setTienTichDiem(request.getTienTichDiem());
        }
        if (request.getTienTieuDiem() != null) {
            quyDoiDiem.setTienTieuDiem(request.getTienTieuDiem());
        }
        if (request.getTrangThai() != null) {
            quyDoiDiem.setTrangThai(request.getTrangThai());
        }
        
        quyDoiDiem = quyDoiDiemRepository.save(quyDoiDiem);
        return convertToResponse(quyDoiDiem);
    }
    
    /**
     * Vô hiệu hóa quy đổi điểm (admin)
     */
    @Transactional
    public void deactivateQuyDoiDiem(UUID id) {
        QuyDoiDiem quyDoiDiem = quyDoiDiemRepository.findById(id)
            .orElseThrow(() -> new ApiException("Không tìm thấy quy đổi điểm", "NOT_FOUND"));
        
        quyDoiDiem.setTrangThai(0);
        quyDoiDiemRepository.save(quyDoiDiem);
    }
    
    /**
     * Convert entity to response DTO
     */
    private QuyDoiDiemResponse convertToResponse(QuyDoiDiem quyDoiDiem) {
        QuyDoiDiemResponse response = new QuyDoiDiemResponse();
        response.setId(quyDoiDiem.getId());
        response.setTienTichDiem(quyDoiDiem.getTienTichDiem());
        response.setTienTieuDiem(quyDoiDiem.getTienTieuDiem());
        response.setTrangThai(quyDoiDiem.getTrangThai());
        return response;
    }
}


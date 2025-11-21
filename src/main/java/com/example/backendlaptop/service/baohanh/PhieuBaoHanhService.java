package com.example.backendlaptop.service.baohanh;

import com.example.backendlaptop.expection.ApiException;
import com.example.backendlaptop.model.response.baohanh.PhieuBaoHanhResponse;
import com.example.backendlaptop.repository.PhieuBaoHanhRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class PhieuBaoHanhService {
    @Autowired
    private PhieuBaoHanhRepository repository;

    @Transactional(readOnly = true)
    public List<PhieuBaoHanhResponse> getAll(){
        return repository.findAllWithRelations().stream().map(PhieuBaoHanhResponse::new).toList();
    }
    
    public void delete(UUID id) {
        repository.findById(id).orElseThrow(
                () -> new ApiException("Not Found", "NF")
        );
        repository.deleteById(id);
    }
    
    @Transactional(readOnly = true)
    public PhieuBaoHanhResponse detail(UUID id){
        return new PhieuBaoHanhResponse(repository.findByIdWithRelations(id).orElseThrow(() -> new ApiException("Not Found","NF")));
    }
    
    @Transactional
    public PhieuBaoHanhResponse updateTrangThai(UUID id, Integer trangThai) {
        var entity = repository.findById(id)
                .orElseThrow(() -> new ApiException("Not Found", "NF"));
        
        // Validate trạng thái hợp lệ: 0=Từ chối, 1=Chờ xác nhận, 2=Xác nhận, 3=Hoàn thành
        if (trangThai < 0 || trangThai > 3) {
            throw new ApiException("Trạng thái không hợp lệ", "INVALID_STATUS");
        }
        
        entity.setTrangThaiBaoHanh(trangThai);
        repository.save(entity);
        
        return new PhieuBaoHanhResponse(repository.findByIdWithRelations(id).orElseThrow());
    }
}

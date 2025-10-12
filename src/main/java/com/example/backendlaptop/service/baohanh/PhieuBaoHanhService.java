package com.example.backendlaptop.service.baohanh;

import com.example.backendlaptop.expection.ApiException;
import com.example.backendlaptop.model.response.baohanh.PhieuBaoHanhResponse;
import com.example.backendlaptop.repository.PhieuBaoHanhRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PhieuBaoHanhService {
    @Autowired
    private PhieuBaoHanhRepository repository;

    public List<PhieuBaoHanhResponse> getAll(){
        return repository.findAll().stream().map(PhieuBaoHanhResponse::new).toList();
    }
    public void delete(UUID id) {
        repository.findById(id).orElseThrow(
                () -> new ApiException("Not Found", "NF")
        );
        repository.deleteById(id);
    }
}

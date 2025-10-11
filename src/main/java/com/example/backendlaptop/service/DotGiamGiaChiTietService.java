package com.example.backendlaptop.service;

import com.example.backendlaptop.expection.ApiException;
import com.example.backendlaptop.model.response.DotGiamGiaChiTietResponse;
import com.example.backendlaptop.repository.DotGiamGiaChiTietRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DotGiamGiaChiTietService {
    @Autowired
    private DotGiamGiaChiTietRepository repository;

    public List<DotGiamGiaChiTietResponse> findByDotGiamGiaId(UUID dotGiamGiaId) {
        return repository.findAllByDotGiamGia_Id(dotGiamGiaId).stream()
                .map(DotGiamGiaChiTietResponse::new)
                .collect(Collectors.toList());
    }

    public void delete(UUID id) {
        repository.findById(id).orElseThrow(
                () -> new ApiException("Not Found", "NF")
        );
        repository.deleteById(id);
    }
}

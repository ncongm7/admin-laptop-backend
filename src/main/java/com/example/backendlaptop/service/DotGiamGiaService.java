package com.example.backendlaptop.service;

import com.example.backendlaptop.entity.DotGiamGia;
import com.example.backendlaptop.expection.ApiException;
import com.example.backendlaptop.model.request.DotGiamGiaRequest;
import com.example.backendlaptop.model.response.DotGiamGiaResponse;
import com.example.backendlaptop.repository.DotGiamGiaRepository;
import com.example.backendlaptop.until.MapperUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class DotGiamGiaService {
    @Autowired
    private DotGiamGiaRepository repository;

    public List<DotGiamGiaResponse> getAll() {
        return repository.findAll().stream().map(DotGiamGiaResponse::new).toList();
    }

    public void delete(UUID id) {
        repository.findById(id).orElseThrow(
                () -> new ApiException("Not Found", "NF")
        );
        repository.deleteById(id);
    }

    public void add(DotGiamGiaRequest request) {
        DotGiamGia dotGiamGia = MapperUtils.map(request, DotGiamGia.class);
        repository.save(dotGiamGia);
    }

    public void update(DotGiamGiaRequest request, UUID id) {
        DotGiamGia dotGiamGiaExist = repository.findById(id).orElseThrow(
                () -> new ApiException("Not Found", "NF")
        );
        MapperUtils.mapToExisting(request, dotGiamGiaExist);
        dotGiamGiaExist.setId(id);
        repository.save(dotGiamGiaExist);
    }

    public DotGiamGiaResponse detail(UUID id) {
        return new DotGiamGiaResponse(repository.findById(id).orElseThrow(() -> new ApiException("Not Found", "NF")));
    }
}

package com.example.backendlaptop.service.phieugiamgia;

import com.example.backendlaptop.entity.PhieuGiamGia;
import com.example.backendlaptop.expection.ApiException;
import com.example.backendlaptop.model.request.phieugiamgia.PhieuGiamGiaRequest;
import com.example.backendlaptop.model.response.phieugiamgia.PhieuGiamGiaResponse;
import com.example.backendlaptop.repository.PhieuGiamGiaRepository;
import com.example.backendlaptop.until.MapperUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PhieuGiamGiaService {
    @Autowired
    private PhieuGiamGiaRepository repository;
    public List<PhieuGiamGiaResponse> getAll(){
        return repository.findAll().stream().map(PhieuGiamGiaResponse::new).toList();
    }

    public void delete(UUID id) {
        repository.findById(id).orElseThrow(
                () -> new ApiException("Not Found", "NF")
        );
        repository.deleteById(id);
    }
    public void add(PhieuGiamGiaRequest request){
        PhieuGiamGia phieuGiamGia = MapperUtils.map(request,PhieuGiamGia.class);
        repository.save(phieuGiamGia);
    }
    public void update(PhieuGiamGiaRequest request, UUID id){
        MapperUtils.mapToExisting(request,PhieuGiamGia.class);
        PhieuGiamGia   phieuGiamGiaExist = repository.findById(id).get();
        MapperUtils.mapToExisting(request,phieuGiamGiaExist);
        phieuGiamGiaExist.setId(id);
        repository.save(phieuGiamGiaExist);
    }
    public PhieuGiamGiaResponse detail(UUID id){
        return new PhieuGiamGiaResponse(repository.findById(id).orElseThrow(() -> new ApiException("Not Found","NF")));
    }



}

package com.example.backendlaptop.service;

import com.example.backendlaptop.entity.PhieuGiamGia;
import com.example.backendlaptop.expection.ApiException;
import com.example.backendlaptop.model.request.PhieuGiamGiaRequest;
import com.example.backendlaptop.model.response.PageableObject;
import com.example.backendlaptop.model.response.PhieuGiamGiaResponse;
import com.example.backendlaptop.repository.PhieuGiamGiaRepository;
import com.example.backendlaptop.until.MapperUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import java.rmi.server.UID;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PhieuGiamGiaService {
    @Autowired
    private PhieuGiamGiaRepository repository;
    public List<PhieuGiamGiaResponse> getAll(){
        return repository.findAll().stream().map(PhieuGiamGiaResponse::new).toList();
    }
    public List<PhieuGiamGiaResponse> searchByName3Case(String keyword) {
        String q = (keyword == null) ? "" : keyword.trim();
        List<PhieuGiamGia> list = q.isEmpty()
                ? repository.findAll()
                : repository.findByMaContainingIgnoreCase(q);

        return list.stream().map(PhieuGiamGiaResponse::new).toList();
    }

    public PageableObject<PhieuGiamGiaResponse> phanTrang(Integer pageNo) {
        Pageable pageable = PageRequest.of(pageNo, 5);
        Page<PhieuGiamGia>pagePhieuGiamGia = repository.findAll(pageable);
        // map tu PhieuGiamGia => PhieuGiamGia Response
        Page<PhieuGiamGiaResponse>pageResponse = pagePhieuGiamGia.map(PhieuGiamGiaResponse::new);
        return new PageableObject<>(pageResponse);
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

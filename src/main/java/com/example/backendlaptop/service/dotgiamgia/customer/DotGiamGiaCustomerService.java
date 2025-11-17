package com.example.backendlaptop.service.dotgiamgia.customer;

import com.example.backendlaptop.dto.dotgiamgia.customer.DotGiamGiaDTOCustomer;
import com.example.backendlaptop.entity.DotGiamGia;
import com.example.backendlaptop.repository.DotGiamGiaChiTietRepository;
import com.example.backendlaptop.repository.DotGiamGiaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DotGiamGiaCustomerService {
    @Autowired
    private DotGiamGiaRepository repository;

    @Autowired
    private DotGiamGiaChiTietRepository dotGiamGiaChiTietRepository;

    public Page<DotGiamGiaDTOCustomer> getAll(Integer pageNo) {
        Pageable pageable = PageRequest.of(pageNo, 5);
        Page<DotGiamGia> dotGiamGias = repository.findByTrangThai(1, pageable);
        return dotGiamGias.map(DotGiamGiaDTOCustomer::new);
    }

    public Page<UUID> getIdCtspByIdDotGiamGia(UUID idDotGiamGia, Integer pageNo) {
        Pageable pageable = PageRequest.of(pageNo, 10); // Or any other page size
        return dotGiamGiaChiTietRepository.findIdCtspByIdDotGiamGia(idDotGiamGia, pageable);
    }
}

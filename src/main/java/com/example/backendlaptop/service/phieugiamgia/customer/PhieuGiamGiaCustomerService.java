package com.example.backendlaptop.service.phieugiamgia.customer;

import com.example.backendlaptop.dto.phieugiamgia.customer.PhieuGiamGiaDTOCustomer;
import com.example.backendlaptop.entity.PhieuGiamGia;
import com.example.backendlaptop.repository.PhieuGiamGiaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class PhieuGiamGiaCustomerService {
    @Autowired
    private PhieuGiamGiaRepository repository;

    public Page<PhieuGiamGiaDTOCustomer> getAll(Integer pageNo) {
        Pageable pageable = PageRequest.of(pageNo, 5); // Or any other page size
        Page<PhieuGiamGia> phieuGiamGias = repository.findByTrangThaiAndRiengTu(1, false, pageable);
        return phieuGiamGias.map(PhieuGiamGiaDTOCustomer::new);
    }
}

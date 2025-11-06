package com.example.backendlaptop.service.sanpham.customer;

import com.example.backendlaptop.dto.sanpham.customer.CTSPResponseCustomer;
import com.example.backendlaptop.dto.sanpham.customer.CTSPTTKTResponseCustomer;
import com.example.backendlaptop.entity.ChiTietSanPham;
import com.example.backendlaptop.repository.ChiTietSanPhamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CTSPCustomerService {
    @Autowired
    private ChiTietSanPhamRepository chiTietSanPhamRepository;

    public CTSPResponseCustomer findById(UUID id){
        Optional<ChiTietSanPham> chiTietSanPham = chiTietSanPhamRepository.findById(id);
        if(chiTietSanPham.isPresent()){
            return new CTSPResponseCustomer(chiTietSanPham.get());
        }
        return null;
    }

    public CTSPTTKTResponseCustomer findThongSoKyThuatById(UUID id){
        Optional<ChiTietSanPham> chiTietSanPham = chiTietSanPhamRepository.findById(id);
        if(chiTietSanPham.isPresent()){
            return new CTSPTTKTResponseCustomer(chiTietSanPham.get());
        }
        return null;
    }

    public List<CTSPResponseCustomer> findAllBySanPhamId(UUID sanPhamId) {
        List<ChiTietSanPham> chiTietSanPhams = chiTietSanPhamRepository.findBySanPham_Id(sanPhamId);
        return chiTietSanPhams.stream()
                .map(CTSPResponseCustomer::new)
                .collect(Collectors.toList());
    }

}

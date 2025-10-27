package com.example.backendlaptop.service;

import com.example.backendlaptop.dto.hinhanh.HinhAnhRequest;
import com.example.backendlaptop.dto.hinhanh.HinhAnhResponse;
import com.example.backendlaptop.entity.ChiTietSanPham;
import com.example.backendlaptop.entity.HinhAnh;
import com.example.backendlaptop.repository.ChiTietSanPhamRepository;
import com.example.backendlaptop.repository.HinhAnhRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class HinhAnhService {
    
    private final HinhAnhRepository hinhAnhRepository;
    private final ChiTietSanPhamRepository chiTietSanPhamRepository;
    
    public HinhAnhResponse createHinhAnh(HinhAnhRequest request) {
        // Check if CTSP exists
        ChiTietSanPham ctsp = chiTietSanPhamRepository.findById(request.getIdSpct())
                .orElseThrow(() -> new RuntimeException("Chi tiết sản phẩm không tồn tại"));
        
        // If this is set as main image, remove main flag from other images
        if (request.getAnhChinhDaiDien()) {
            hinhAnhRepository.findMainImageByCtspId(request.getIdSpct())
                    .ifPresent(existingMain -> {
                        existingMain.setAnhChinhDaiDien(false);
                        hinhAnhRepository.save(existingMain);
                    });
        }
        
        HinhAnh hinhAnh = new HinhAnh();
        hinhAnh.setId(UUID.randomUUID());
        hinhAnh.setIdSpct(ctsp);
        hinhAnh.setUrl(request.getUrl());
        hinhAnh.setAnhChinhDaiDien(request.getAnhChinhDaiDien());
        hinhAnh.setNgayTao(Instant.now());
        hinhAnh.setNgaySua(Instant.now());
        
        HinhAnh savedHinhAnh = hinhAnhRepository.save(hinhAnh);
        return mapToResponse(savedHinhAnh);
    }
    
    public List<HinhAnhResponse> createHinhAnhBatch(List<HinhAnhRequest> requests) {
        return requests.stream()
                .map(this::createHinhAnh)
                .toList();
    }
    
    public List<HinhAnhResponse> getHinhAnhByCtspId(UUID ctspId) {
        List<HinhAnh> hinhAnhs = hinhAnhRepository.findByIdSpctId(ctspId);
        return hinhAnhs.stream().map(this::mapToResponse).toList();
    }
    
    public HinhAnhResponse getMainImageByCtspId(UUID ctspId) {
        HinhAnh hinhAnh = hinhAnhRepository.findMainImageByCtspId(ctspId)
                .orElse(null);
        return hinhAnh != null ? mapToResponse(hinhAnh) : null;
    }
    
    public List<HinhAnhResponse> getGalleryImagesByCtspId(UUID ctspId) {
        List<HinhAnh> hinhAnhs = hinhAnhRepository.findGalleryImagesByCtspId(ctspId);
        return hinhAnhs.stream().map(this::mapToResponse).toList();
    }
    
    public HinhAnhResponse updateHinhAnh(UUID id, HinhAnhRequest request) {
        HinhAnh hinhAnh = hinhAnhRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hình ảnh không tồn tại"));
        
        // If this is set as main image, remove main flag from other images
        if (request.getAnhChinhDaiDien() && !hinhAnh.getAnhChinhDaiDien()) {
            hinhAnhRepository.findMainImageByCtspId(hinhAnh.getIdSpct().getId())
                    .ifPresent(existingMain -> {
                        existingMain.setAnhChinhDaiDien(false);
                        hinhAnhRepository.save(existingMain);
                    });
        }
        
        hinhAnh.setUrl(request.getUrl());
        hinhAnh.setAnhChinhDaiDien(request.getAnhChinhDaiDien());
        hinhAnh.setNgaySua(Instant.now());
        
        HinhAnh updatedHinhAnh = hinhAnhRepository.save(hinhAnh);
        return mapToResponse(updatedHinhAnh);
    }
    
    public void deleteHinhAnh(UUID id) {
        HinhAnh hinhAnh = hinhAnhRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hình ảnh không tồn tại"));
        
        hinhAnhRepository.delete(hinhAnh);
    }
    
    public void deleteAllByCtspId(UUID ctspId) {
        hinhAnhRepository.deleteByIdSpctId(ctspId);
    }
    
    private HinhAnhResponse mapToResponse(HinhAnh hinhAnh) {
        HinhAnhResponse response = new HinhAnhResponse();
        response.setId(hinhAnh.getId());
        response.setIdSpct(hinhAnh.getIdSpct().getId());
        response.setUrl(hinhAnh.getUrl());
        response.setAnhChinhDaiDien(hinhAnh.getAnhChinhDaiDien());
        response.setNgayTao(hinhAnh.getNgayTao());
        response.setNgaySua(hinhAnh.getNgaySua());
        
        // Add product info
        if (hinhAnh.getIdSpct() != null) {
            response.setMaCtsp(hinhAnh.getIdSpct().getMaCtsp());
            if (hinhAnh.getIdSpct().getSanPham() != null) {
                response.setTenSanPham(hinhAnh.getIdSpct().getSanPham().getTenSanPham());
            }
        }
        
        return response;
    }
}

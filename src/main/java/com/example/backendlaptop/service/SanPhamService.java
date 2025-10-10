package com.example.backendlaptop.service;

import com.example.backendlaptop.dto.sanpham.SanPhamRequest;
import com.example.backendlaptop.dto.sanpham.SanPhamResponse;
import com.example.backendlaptop.entity.SanPham;
import com.example.backendlaptop.expection.ApiException;
import com.example.backendlaptop.repository.SanPhamRepository;
import com.example.backendlaptop.until.MapperUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SanPhamService {
    
    private final SanPhamRepository sanPhamRepository;
    
    public SanPhamResponse createSanPham(SanPhamRequest request) {
        // Kiểm tra mã sản phẩm đã tồn tại
        if (sanPhamRepository.existsByMaSanPham(request.getMaSanPham())) {
            throw new ApiException("Mã sản phẩm đã tồn tại: " + request.getMaSanPham());
        }
        
        SanPham sanPham = new SanPham();
        sanPham.setId(UUID.randomUUID());
        sanPham.setMaSanPham(request.getMaSanPham());
        sanPham.setTenSanPham(request.getTenSanPham());
        sanPham.setMoTa(request.getMoTa());
        sanPham.setTrangThai(request.getTrangThai());
        sanPham.setNguoiTao(request.getNguoiTao());
        sanPham.setNguoiSua(request.getNguoiSua());
        sanPham.setNgayTao(Instant.now());
        sanPham.setNgaySua(Instant.now());
        
        SanPham savedSanPham = sanPhamRepository.save(sanPham);
        return convertToResponse(savedSanPham);
    }
    
    public SanPhamResponse updateSanPham(UUID id, SanPhamRequest request) {
        SanPham sanPham = sanPhamRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy sản phẩm với ID: " + id));
        
        // Kiểm tra mã sản phẩm đã tồn tại (trừ sản phẩm hiện tại)
        if (!sanPham.getMaSanPham().equals(request.getMaSanPham()) && 
            sanPhamRepository.existsByMaSanPham(request.getMaSanPham())) {
            throw new ApiException("Mã sản phẩm đã tồn tại: " + request.getMaSanPham());
        }
        
        sanPham.setMaSanPham(request.getMaSanPham());
        sanPham.setTenSanPham(request.getTenSanPham());
        sanPham.setMoTa(request.getMoTa());
        sanPham.setTrangThai(request.getTrangThai());
        sanPham.setNguoiSua(request.getNguoiSua());
        sanPham.setNgaySua(Instant.now());
        
        SanPham savedSanPham = sanPhamRepository.save(sanPham);
        return convertToResponse(savedSanPham);
    }
    
    @Transactional(readOnly = true)
    public SanPhamResponse getSanPhamById(UUID id) {
        SanPham sanPham = sanPhamRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy sản phẩm với ID: " + id));
        return convertToResponse(sanPham);
    }
    
    @Transactional(readOnly = true)
    public SanPhamResponse getSanPhamByMaSanPham(String maSanPham) {
        SanPham sanPham = sanPhamRepository.findByMaSanPham(maSanPham)
                .orElseThrow(() -> new ApiException("Không tìm thấy sản phẩm với mã: " + maSanPham));
        return convertToResponse(sanPham);
    }
    
    @Transactional(readOnly = true)
    public List<SanPhamResponse> getAllSanPham() {
        List<SanPham> sanPhams = sanPhamRepository.findAll();
        return sanPhams.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Page<SanPhamResponse> getAllSanPham(Pageable pageable) {
        Page<SanPham> sanPhams = sanPhamRepository.findAll(pageable);
        return sanPhams.map(this::convertToResponse);
    }
    
    @Transactional(readOnly = true)
    public List<SanPhamResponse> getSanPhamByTrangThai(Integer trangThai) {
        List<SanPham> sanPhams = sanPhamRepository.findByTrangThai(trangThai);
        return sanPhams.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Page<SanPhamResponse> getSanPhamByTrangThai(Integer trangThai, Pageable pageable) {
        Page<SanPham> sanPhams = sanPhamRepository.findByTrangThai(trangThai, pageable);
        return sanPhams.map(this::convertToResponse);
    }
    
    @Transactional(readOnly = true)
    public List<SanPhamResponse> searchSanPhamByTen(String tenSanPham) {
        List<SanPham> sanPhams = sanPhamRepository.findByTenSanPhamContaining(tenSanPham);
        return sanPhams.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Page<SanPhamResponse> searchSanPhamByTen(String tenSanPham, Pageable pageable) {
        Page<SanPham> sanPhams = sanPhamRepository.findByTenSanPhamContaining(tenSanPham, pageable);
        return sanPhams.map(this::convertToResponse);
    }
    
    @Transactional(readOnly = true)
    public List<SanPhamResponse> searchSanPhamByTrangThaiAndTen(Integer trangThai, String tenSanPham) {
        List<SanPham> sanPhams = sanPhamRepository.findByTrangThaiAndTenSanPhamContaining(trangThai, tenSanPham);
        return sanPhams.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Page<SanPhamResponse> searchSanPhamByTrangThaiAndTen(Integer trangThai, String tenSanPham, Pageable pageable) {
        Page<SanPham> sanPhams = sanPhamRepository.findByTrangThaiAndTenSanPhamContaining(trangThai, tenSanPham, pageable);
        return sanPhams.map(this::convertToResponse);
    }
    
    public void deleteSanPham(UUID id) {
        if (!sanPhamRepository.existsById(id)) {
            throw new ApiException("Không tìm thấy sản phẩm với ID: " + id);
        }
        sanPhamRepository.deleteById(id);
    }
    
    public void updateTrangThaiSanPham(UUID id, Integer trangThai) {
        SanPham sanPham = sanPhamRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy sản phẩm với ID: " + id));
        
        sanPham.setTrangThai(trangThai);
        sanPham.setNgaySua(Instant.now());
        sanPhamRepository.save(sanPham);
    }
    
    private SanPhamResponse convertToResponse(SanPham sanPham) {
        return MapperUtils.map(sanPham, SanPhamResponse.class);
    }
}

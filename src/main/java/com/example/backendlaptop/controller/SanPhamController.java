package com.example.backendlaptop.controller;

import com.example.backendlaptop.dto.sanpham.SanPhamRequest;
import com.example.backendlaptop.dto.sanpham.SanPhamResponse;
import com.example.backendlaptop.service.SanPhamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/san-pham")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SanPhamController {
    
    private final SanPhamService sanPhamService;
    
    @PostMapping
    public ResponseEntity<SanPhamResponse> createSanPham(@Valid @RequestBody SanPhamRequest request) {
        SanPhamResponse response = sanPhamService.createSanPham(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<SanPhamResponse> updateSanPham(@PathVariable UUID id, @Valid @RequestBody SanPhamRequest request) {
        SanPhamResponse response = sanPhamService.updateSanPham(id, request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<SanPhamResponse> getSanPhamById(@PathVariable UUID id) {
        SanPhamResponse response = sanPhamService.getSanPhamById(id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/ma/{maSanPham}")
    public ResponseEntity<SanPhamResponse> getSanPhamByMaSanPham(@PathVariable String maSanPham) {
        SanPhamResponse response = sanPhamService.getSanPhamByMaSanPham(maSanPham);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    public ResponseEntity<List<SanPhamResponse>> getAllSanPham() {
        List<SanPhamResponse> responses = sanPhamService.getAllSanPham();
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/page")
    public ResponseEntity<Page<SanPhamResponse>> getAllSanPham(Pageable pageable) {
        Page<SanPhamResponse> responses = sanPhamService.getAllSanPham(pageable);
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/trang-thai/{trangThai}")
    public ResponseEntity<List<SanPhamResponse>> getSanPhamByTrangThai(@PathVariable Integer trangThai) {
        List<SanPhamResponse> responses = sanPhamService.getSanPhamByTrangThai(trangThai);
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/trang-thai/{trangThai}/page")
    public ResponseEntity<Page<SanPhamResponse>> getSanPhamByTrangThai(@PathVariable Integer trangThai, Pageable pageable) {
        Page<SanPhamResponse> responses = sanPhamService.getSanPhamByTrangThai(trangThai, pageable);
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<SanPhamResponse>> searchSanPhamByTen(@RequestParam String tenSanPham) {
        List<SanPhamResponse> responses = sanPhamService.searchSanPhamByTen(tenSanPham);
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/search/page")
    public ResponseEntity<Page<SanPhamResponse>> searchSanPhamByTen(@RequestParam String tenSanPham, Pageable pageable) {
        Page<SanPhamResponse> responses = sanPhamService.searchSanPhamByTen(tenSanPham, pageable);
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/search/advanced")
    public ResponseEntity<List<SanPhamResponse>> searchSanPhamByTrangThaiAndTen(
            @RequestParam Integer trangThai, 
            @RequestParam String tenSanPham) {
        List<SanPhamResponse> responses = sanPhamService.searchSanPhamByTrangThaiAndTen(trangThai, tenSanPham);
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/search/advanced/page")
    public ResponseEntity<Page<SanPhamResponse>> searchSanPhamByTrangThaiAndTen(
            @RequestParam Integer trangThai, 
            @RequestParam String tenSanPham, 
            Pageable pageable) {
        Page<SanPhamResponse> responses = sanPhamService.searchSanPhamByTrangThaiAndTen(trangThai, tenSanPham, pageable);
        return ResponseEntity.ok(responses);
    }
    
    @PutMapping("/{id}/trang-thai")
    public ResponseEntity<Void> updateTrangThaiSanPham(@PathVariable UUID id, @RequestParam Integer trangThai) {
        sanPhamService.updateTrangThaiSanPham(id, trangThai);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSanPham(@PathVariable UUID id) {
        sanPhamService.deleteSanPham(id);
        return ResponseEntity.noContent().build();
    }
    
    // Tìm kiếm theo mã hoặc tên
    @GetMapping("/search/keyword")
    public ResponseEntity<List<SanPhamResponse>> searchByMaOrTen(@RequestParam(required = false) String keyword) {
        List<SanPhamResponse> responses = sanPhamService.searchByMaOrTen(keyword);
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/search/keyword/page")
    public ResponseEntity<Page<SanPhamResponse>> searchByMaOrTen(
            @RequestParam(required = false) String keyword, 
            Pageable pageable) {
        Page<SanPhamResponse> responses = sanPhamService.searchByMaOrTen(keyword, pageable);
        return ResponseEntity.ok(responses);
    }
    
    // Lấy sản phẩm còn hàng (trangThai = 1 và soLuongTon > 0)
    @GetMapping("/con-hang")
    public ResponseEntity<List<SanPhamResponse>> getSanPhamConHang() {
        List<SanPhamResponse> responses = sanPhamService.getSanPhamConHang();
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/con-hang/page")
    public ResponseEntity<Page<SanPhamResponse>> getSanPhamConHang(Pageable pageable) {
        Page<SanPhamResponse> responses = sanPhamService.getSanPhamConHang(pageable);
        return ResponseEntity.ok(responses);
    }
    
    // Tìm kiếm nâng cao - Tìm kiếm theo mã/tên, trạng thái, khoảng giá
    @GetMapping("/search/advanced-filter")
    public ResponseEntity<List<SanPhamResponse>> advancedSearch(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer trangThai,
            @RequestParam(required = false) Long minPrice,
            @RequestParam(required = false) Long maxPrice) {
        
        List<SanPhamResponse> responses = sanPhamService.advancedSearch(
            keyword, 
            trangThai, 
            minPrice, 
            maxPrice
        );
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/search/advanced-filter/page")
    public ResponseEntity<Page<SanPhamResponse>> advancedSearch(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer trangThai,
            @RequestParam(required = false) Long minPrice,
            @RequestParam(required = false) Long maxPrice,
            Pageable pageable) {
        
        Page<SanPhamResponse> responses = sanPhamService.advancedSearch(
            keyword, 
            trangThai, 
            minPrice, 
            maxPrice,
            pageable
        );
        return ResponseEntity.ok(responses);
    }
    
    /**
     * API cho BÁN HÀNG: Lấy danh sách sản phẩm còn hàng (kèm chi tiết biến thể)
     * Endpoint: GET /api/san-pham/ban-hang/con-hang
     * 
     * Trả về danh sách SẢN PHẨM (cha) còn hàng, mỗi sản phẩm có kèm:
     * - Danh sách chiTietSanPhams (các biến thể)
     * - Số lượng biến thể
     * - Trạng thái còn hàng
     * 
     * @param pageable - Phân trang (page, size, sort)
     * @return Page<SanPhamResponse> - Danh sách sản phẩm còn hàng kèm biến thể
     */
    @GetMapping("/ban-hang/con-hang")
    public ResponseEntity<Page<SanPhamResponse>> getSanPhamConHangForBanHang(Pageable pageable) {
        Page<SanPhamResponse> responses = sanPhamService.getSanPhamConHangWithVariants(pageable);
        return ResponseEntity.ok(responses);
    }
    
    /**
     * API cho BÁN HÀNG: Tìm kiếm sản phẩm (kèm chi tiết biến thể)
     * Endpoint: GET /api/san-pham/ban-hang/tim-kiem
     * 
     * Tìm kiếm SẢN PHẨM theo từ khóa và trả về kèm danh sách biến thể
     * 
     * @param keyword - Từ khóa tìm kiếm (tên sản phẩm, mã sản phẩm)
     * @param pageable - Phân trang (page, size, sort)
     * @return Page<SanPhamResponse> - Danh sách sản phẩm tìm được kèm biến thể
     */
    @GetMapping("/ban-hang/tim-kiem")
    public ResponseEntity<Page<SanPhamResponse>> searchSanPhamForBanHang(
            @RequestParam(required = false) String keyword,
            Pageable pageable) {
        Page<SanPhamResponse> responses = sanPhamService.searchSanPhamWithVariants(keyword, pageable);
        return ResponseEntity.ok(responses);
    }
}

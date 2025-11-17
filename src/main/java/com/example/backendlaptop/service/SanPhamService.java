package com.example.backendlaptop.service;

import com.example.backendlaptop.dto.sanpham.ChiTietSanPhamResponse;
import com.example.backendlaptop.dto.sanpham.SanPhamRequest;
import com.example.backendlaptop.dto.sanpham.SanPhamResponse;
import com.example.backendlaptop.entity.ChiTietSanPham;
import com.example.backendlaptop.entity.SanPham;
import com.example.backendlaptop.expection.ApiException;
import com.example.backendlaptop.repository.ChiTietSanPhamRepository;
import com.example.backendlaptop.repository.SanPhamRepository;
import com.example.backendlaptop.until.MapperUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
    private final ChiTietSanPhamRepository chiTietSanPhamRepository;
    
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
    
    // Tìm kiếm theo mã hoặc tên
    @Transactional(readOnly = true)
    public List<SanPhamResponse> searchByMaOrTen(String keyword) {
        List<SanPham> sanPhams = sanPhamRepository.searchByMaOrTen(keyword);
        return sanPhams.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Page<SanPhamResponse> searchByMaOrTen(String keyword, Pageable pageable) {
        Page<SanPham> sanPhams = sanPhamRepository.searchByMaOrTen(keyword, pageable);
        return sanPhams.map(this::convertToResponse);
    }
    
    // Tìm kiếm nâng cao với validation
    @Transactional(readOnly = true)
    public List<SanPhamResponse> advancedSearch(
            String keyword,
            Integer trangThai,
            Long minPrice,
            Long maxPrice) {
        
        // Validate price range
        validatePriceRange(minPrice, maxPrice);
        
        List<SanPham> sanPhams = sanPhamRepository.advancedSearch(
            keyword, 
            trangThai, 
            minPrice, 
            maxPrice
        );
        
        return sanPhams.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Page<SanPhamResponse> advancedSearch(
            String keyword,
            Integer trangThai,
            Long minPrice,
            Long maxPrice,
            Pageable pageable) {
        
        // Validate price range
        validatePriceRange(minPrice, maxPrice);
        
        Page<SanPham> sanPhams = sanPhamRepository.advancedSearch(
            keyword, 
            trangThai, 
            minPrice, 
            maxPrice,
            pageable
        );
        
        return sanPhams.map(this::convertToResponse);
    }
    
    // Lấy sản phẩm còn hàng (trangThai = 1 và soLuongTon > 0)
    @Transactional(readOnly = true)
    public List<SanPhamResponse> getSanPhamConHang() {
        List<SanPham> sanPhams = sanPhamRepository.findSanPhamConHang();
        return sanPhams.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Page<SanPhamResponse> getSanPhamConHang(Pageable pageable) {
        Page<SanPham> sanPhams = sanPhamRepository.findSanPhamConHang(pageable);
        return sanPhams.map(this::convertToResponse);
    }
    
    /**
     * Lấy danh sách sản phẩm còn hàng KÈM CHI TIẾT BIẾN THỂ (cho module Bán Hàng)
     * 
     * API này trả về:
     * - Danh sách SẢN PHẨM cha (không phải chi tiết)
     * - Mỗi sản phẩm có kèm danh sách chiTietSanPhams (các biến thể)
     * - Chỉ lấy sản phẩm có trangThai = 1 (đang bán)
     * - Chỉ lấy biến thể có soLuongTon > 0
     * 
     * @param pageable - Phân trang
     * @return Page<SanPhamResponse> - Danh sách sản phẩm còn hàng kèm biến thể
     */
    @Transactional(readOnly = true)
    public Page<SanPhamResponse> getSanPhamConHangWithVariants(Pageable pageable) {
        // Lấy danh sách sản phẩm còn hàng (trangThai = 1)
        Page<SanPham> sanPhamPage = sanPhamRepository.findSanPhamConHang(pageable);
        
        // Convert và thêm chi tiết biến thể vào từng sản phẩm
        List<SanPhamResponse> sanPhamResponses = sanPhamPage.getContent().stream()
                .map(this::enrichSanPhamWithVariants)
                .filter(response -> response.getChiTietSanPhams() != null && !response.getChiTietSanPhams().isEmpty())
                .collect(Collectors.toList());
        
        // Trả về Page với danh sách đã được enrich
        return new PageImpl<>(sanPhamResponses, pageable, sanPhamPage.getTotalElements());
    }
    
    /**
     * Tìm kiếm sản phẩm theo keyword KÈM CHI TIẾT BIẾN THỂ (cho module Bán Hàng)
     * 
     * @param keyword - Từ khóa tìm kiếm (tên hoặc mã sản phẩm)
     * @param pageable - Phân trang
     * @return Page<SanPhamResponse> - Danh sách sản phẩm tìm được kèm biến thể
     */
    @Transactional(readOnly = true)
    public Page<SanPhamResponse> searchSanPhamWithVariants(String keyword, Pageable pageable) {
        Page<SanPham> sanPhamPage;
        
        if (keyword == null || keyword.trim().isEmpty()) {
            // Nếu không có keyword, trả về sản phẩm còn hàng
            sanPhamPage = sanPhamRepository.findSanPhamConHang(pageable);
        } else {
            // Tìm kiếm theo keyword
            sanPhamPage = sanPhamRepository.searchByMaOrTen(keyword.trim(), pageable);
        }
        
        // Convert và thêm chi tiết biến thể
        List<SanPhamResponse> sanPhamResponses = sanPhamPage.getContent().stream()
                .filter(sp -> sp.getTrangThai() != null && sp.getTrangThai() == 1) // Chỉ lấy sản phẩm đang bán
                .map(this::enrichSanPhamWithVariants)
                .filter(response -> response.getChiTietSanPhams() != null && !response.getChiTietSanPhams().isEmpty())
                .collect(Collectors.toList());
        
        return new PageImpl<>(sanPhamResponses, pageable, sanPhamPage.getTotalElements());
    }
    
    /**
     * Helper method: Enrich SanPham với danh sách ChiTietSanPham (biến thể)
     * 
     * @param sanPham - Entity SanPham
     * @return SanPhamResponse - Response đã được enrich với danh sách biến thể
     */
    private SanPhamResponse enrichSanPhamWithVariants(SanPham sanPham) {
        SanPhamResponse response = convertToResponse(sanPham);
        
        // Lấy danh sách chi tiết sản phẩm (biến thể)
        List<ChiTietSanPham> chiTietSanPhams = chiTietSanPhamRepository.findBySanPham_Id(sanPham.getId());
        
        // Chỉ lấy biến thể đang bán (trangThai = 1)
        List<ChiTietSanPhamResponse> chiTietResponses = chiTietSanPhams.stream()
                .filter(ctsp -> ctsp.getTrangThai() != null && ctsp.getTrangThai() == 1)
                .map(this::convertChiTietToResponse)
                .collect(Collectors.toList());
        
        response.setChiTietSanPhams(chiTietResponses);
        
        return response;
    }
    
    // Validate price range
    private void validatePriceRange(Long minPrice, Long maxPrice) {
        if (minPrice != null && minPrice < 0) {
            throw new ApiException("Giá tối thiểu không được nhỏ hơn 0");
        }
        
        if (maxPrice != null && maxPrice < 0) {
            throw new ApiException("Giá tối đa không được nhỏ hơn 0");
        }
        
        if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
            throw new ApiException("Giá tối thiểu không được lớn hơn giá tối đa");
        }
    }
    
    private SanPhamResponse convertToResponse(SanPham sanPham) {
        return MapperUtils.map(sanPham, SanPhamResponse.class);
    }
    
    /**
     * Convert ChiTietSanPham entity sang ChiTietSanPhamResponse DTO
     * Helper method cho getSanPhamConHangWithVariants
     * 
     * Method này populate đầy đủ thông tin từ các entity liên quan
     * (CPU, RAM, GPU, Màu sắc, v.v.)
     */
    private ChiTietSanPhamResponse convertChiTietToResponse(ChiTietSanPham chiTietSanPham) {
        ChiTietSanPhamResponse response = MapperUtils.map(chiTietSanPham, ChiTietSanPhamResponse.class);
        
        // Set thông tin sản phẩm
        if (chiTietSanPham.getSanPham() != null) {
            response.setIdSanPham(chiTietSanPham.getSanPham().getId());
            response.setTenSanPham(chiTietSanPham.getSanPham().getTenSanPham());
            response.setMaSanPham(chiTietSanPham.getSanPham().getMaSanPham());
        }
        
        // Set thông tin CPU
        if (chiTietSanPham.getCpu() != null) {
            response.setIdCpu(chiTietSanPham.getCpu().getId());
            response.setTenCpu(chiTietSanPham.getCpu().getTenCpu());
            response.setMaCpu(chiTietSanPham.getCpu().getMaCpu());
        }
        
        // Set thông tin GPU
        if (chiTietSanPham.getGpu() != null) {
            response.setIdGpu(chiTietSanPham.getGpu().getId());
            response.setTenGpu(chiTietSanPham.getGpu().getTenGpu());
            response.setMaGpu(chiTietSanPham.getGpu().getMaGpu());
        }
        
        // Set thông tin RAM
        if (chiTietSanPham.getRam() != null) {
            response.setIdRam(chiTietSanPham.getRam().getId());
            response.setTenRam(chiTietSanPham.getRam().getTenRam());
            response.setMaRam(chiTietSanPham.getRam().getMaRam());
        }
        
        // Set thông tin ổ cứng
        if (chiTietSanPham.getOCung() != null) {
            response.setIdOCung(chiTietSanPham.getOCung().getId());
            response.setDungLuongOCung(chiTietSanPham.getOCung().getDungLuong());
            response.setMaOCung(chiTietSanPham.getOCung().getMaOCung());
        }
        
        // Set thông tin màu sắc
        if (chiTietSanPham.getMauSac() != null) {
            response.setIdMauSac(chiTietSanPham.getMauSac().getId());
            response.setTenMauSac(chiTietSanPham.getMauSac().getTenMau());
            response.setMaMauSac(chiTietSanPham.getMauSac().getMaMau());
            response.setHexCode(chiTietSanPham.getMauSac().getHexCode());
        }
        
        // Set thông tin loại màn hình
        if (chiTietSanPham.getLoaiManHinh() != null) {
            response.setIdLoaiManHinh(chiTietSanPham.getLoaiManHinh().getId());
            response.setKichThuocManHinh(chiTietSanPham.getLoaiManHinh().getKichThuoc());
            response.setMaLoaiManHinh(chiTietSanPham.getLoaiManHinh().getMaLoaiManHinh());
        }
        
        // Set thông tin pin
        if (chiTietSanPham.getPin() != null) {
            response.setIdPin(chiTietSanPham.getPin().getId());
            response.setDungLuongPin(chiTietSanPham.getPin().getDungLuongPin());
            response.setMaPin(chiTietSanPham.getPin().getMaPin());
        }
        
        // Set giá mặc định (không tính giảm giá ở đây)
        response.setCoGiamGia(false);
        response.setGiaGoc(chiTietSanPham.getGiaBan());
        response.setGiaGiam(chiTietSanPham.getGiaBan());
        response.setPhanTramGiam(0);
        
        return response;
    }
}

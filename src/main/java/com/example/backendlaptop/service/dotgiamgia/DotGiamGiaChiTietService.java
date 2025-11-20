package com.example.backendlaptop.service.dotgiamgia;

import com.example.backendlaptop.entity.ChiTietSanPham;
import com.example.backendlaptop.entity.DotGiamGia;
import com.example.backendlaptop.entity.DotGiamGiaChiTiet;
import com.example.backendlaptop.expection.ApiException;
import com.example.backendlaptop.model.request.dotgiamgia.DotGiamGiaChiTietRequest;
import com.example.backendlaptop.model.response.dotgiamgia.CTSPDotGiamGiaResponse;
import com.example.backendlaptop.model.response.dotgiamgia.DotGiamGiaChiTietResponse;
import com.example.backendlaptop.model.response.dotgiamgia.SanPhamDotGiamGiaResponse;
import com.example.backendlaptop.repository.ChiTietSanPhamRepository;
import com.example.backendlaptop.repository.DotGiamGiaChiTietRepository;
import com.example.backendlaptop.repository.DotGiamGiaRepository;
import com.example.backendlaptop.repository.SanPhamRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DotGiamGiaChiTietService {
    @Autowired
    private DotGiamGiaChiTietRepository repository;
    @Autowired
    private ChiTietSanPhamRepository  chiTietSanPhamRepository;
    @Autowired
    private SanPhamRepository sanPhamRepository;
    @Autowired
    private DotGiamGiaRepository dotGiamGiaRepository;
    //bảng ctsp đã thêm
    public List<DotGiamGiaChiTietResponse> findByDotGiamGiaId(UUID dotGiamGiaId) {
        return repository.findAllByDotGiamGia_Id(dotGiamGiaId).stream()
                .map(DotGiamGiaChiTietResponse::new)
                .collect(Collectors.toList());
    }

    //Combobox 1
    public List<SanPhamDotGiamGiaResponse> findAllSanPhaṃ(){
        return sanPhamRepository.findAll().stream().map(SanPhamDotGiamGiaResponse::new).toList();
    }
    //bảng ctsp chưa thêm
    public List<CTSPDotGiamGiaResponse> getAvailableProductsBySanPham(UUID dotGiamGiaId, UUID sanPhamId) {
        if (sanPhamId == null) {
            return List.of();
        }

        List<ChiTietSanPham> entities = chiTietSanPhamRepository.findAvailableProductsBySanPhamId(dotGiamGiaId, sanPhamId);

        // Ánh xạ Entity sang DTO bằng Constructor
        return entities.stream()
                .map(CTSPDotGiamGiaResponse::new)
                .collect(Collectors.toList());
    }
    //xóa
    public void delete(UUID id) {
        repository.findById(id).orElseThrow(
                () -> new ApiException("Not Found", "NF")
        );
        repository.deleteById(id);
    }
    //thêm 1 sản phầm mới
//    public void add(PhieuGiamGiaRequest request){
//        PhieuGiamGia phieuGiamGia = MapperUtils.map(request,PhieuGiamGia.class);
//        repository.save(phieuGiamGia);
//    }
    @Transactional
    public void addSelectedProducts(DotGiamGiaChiTietRequest req) {
        // 1) Lấy đợt giảm giá
        DotGiamGia dot = dotGiamGiaRepository.findById(req.getDotGiamGiaId())
                .orElseThrow(() -> new ApiException("Đợt giảm giá không tồn tại.", "NOT_FOUND"));
        
        final BigDecimal giaTri = dot.getGiaTri() == null ? BigDecimal.ZERO : dot.getGiaTri();
        final Integer loaiDotGiamGia = dot.getLoaiDotGiamGia() == null ? 2 : dot.getLoaiDotGiamGia(); // Mặc định là VND nếu null
        final BigDecimal soTienGiamToiDa = dot.getSoTienGiamToiDa();

        // 2) Lấy CTSP hợp lệ
        List<ChiTietSanPham> ctsps = chiTietSanPhamRepository.findAllById(req.getCtspIds());
        if (ctsps.isEmpty()) throw new ApiException("Không tìm thấy CTSP hợp lệ.", "BAD_REQUEST");

        // 3) Lọc bỏ CTSP đã nằm trong đợt (chống overlap)
        Set<UUID> existed = repository.findCtspIdsByDotId(dot.getId()); // custom query trả về Set<UUID>
        List<DotGiamGiaChiTiet> toInsert = ctsps.stream()
                .filter(ct -> !existed.contains(ct.getId()))
                .map(ct -> {
                    BigDecimal giaBan = ct.getGiaBan();
                    BigDecimal giaSau = calculateGiaSauKhiGiam(giaBan, giaTri, loaiDotGiamGia, soTienGiamToiDa);

                    DotGiamGiaChiTiet d = new DotGiamGiaChiTiet();
                    d.setId(UUID.randomUUID());                               // đảm bảo có id phía BE
                    d.setDotGiamGia(dot);                                     // id_km
                    d.setIdCtsp(ct);

                    d.setGiaBanDau(giaBan);
                    d.setGiaSauKhiGiam(giaSau);
                    return d;
                })
                .toList();

        if (toInsert.isEmpty())
            throw new ApiException("Tất cả CTSP đã thuộc đợt hoặc không hợp lệ.", "BAD_REQUEST");

        repository.saveAll(toInsert);
    }

    /**
     * Tính giá sau khi giảm dựa trên loại giảm giá
     * @param giaBan Giá ban đầu
     * @param giaTri Giá trị giảm (% hoặc VND)
     * @param loaiDotGiamGia 1: Giảm theo %, 2: Giảm theo VND
     * @param soTienGiamToiDa Giới hạn số tiền giảm tối đa (chỉ dùng khi loai = 1)
     * @return Giá sau khi giảm
     */
    private BigDecimal calculateGiaSauKhiGiam(BigDecimal giaBan, BigDecimal giaTri, Integer loaiDotGiamGia, BigDecimal soTienGiamToiDa) {
        BigDecimal giaSau;
        
        if (Integer.valueOf(1).equals(loaiDotGiamGia)) {
            // Giảm theo %
            BigDecimal soTienGiam = giaBan.multiply(giaTri).divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
            
            // Áp dụng giới hạn số tiền giảm tối đa (nếu có)
            if (soTienGiamToiDa != null && soTienGiamToiDa.compareTo(BigDecimal.ZERO) > 0) {
                soTienGiam = soTienGiam.min(soTienGiamToiDa);
            }
            
            giaSau = giaBan.subtract(soTienGiam);
        } else {
            // Giảm theo VND (mặc định)
            giaSau = giaBan.subtract(giaTri);
        }
        
        // Chặn giá âm
        if (giaSau.signum() < 0) {
            giaSau = BigDecimal.ZERO;
        }
        
        // Làm tròn xuống bội số của 1000 (giữ nguyên logic cũ)
        if (giaSau.compareTo(BigDecimal.ZERO) > 0) {
            long giaSauLong = giaSau.longValue();
            giaSau = BigDecimal.valueOf((giaSauLong / 1000) * 1000);
        }
        
        return giaSau;
    }

}

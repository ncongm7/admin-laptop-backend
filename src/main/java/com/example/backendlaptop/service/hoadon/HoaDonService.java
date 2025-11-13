package com.example.backendlaptop.service.hoadon;

import com.example.backendlaptop.dto.hoadon.HoaDonDetailResponse;
import com.example.backendlaptop.dto.hoadon.HoaDonListResponse;
import com.example.backendlaptop.dto.hoadon.HoaDonSearchRequest;
import com.example.backendlaptop.entity.HoaDon;
import com.example.backendlaptop.expection.ApiException;
import com.example.backendlaptop.repository.banhang.HoaDonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.persistence.criteria.Predicate;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service xử lý logic quản lý hóa đơn
 */
@Service
@RequiredArgsConstructor
public class HoaDonService {

    private final HoaDonRepository hoaDonRepository;

    /**
     * Tìm kiếm và lọc hóa đơn với phân trang
     */
    public Page<HoaDonListResponse> searchHoaDon(HoaDonSearchRequest request) {
        try {
            System.out.println("🔍 [HoaDonService] Bắt đầu tìm kiếm hóa đơn với request: " + request);
            
            // Tạo Pageable
            Pageable pageable = PageRequest.of(
                request.getPage() != null ? request.getPage() : 0,
                request.getSize() != null ? request.getSize() : 10,
                Sort.by(Sort.Direction.DESC, "ngayTao") // Sắp xếp mới nhất trước
            );

            // Tạo Specification để build query động
            Specification<HoaDon> spec = buildSpecification(request);

            System.out.println("📊 [HoaDonService] Thực hiện query với spec...");
            
            // Thực hiện query
            Page<HoaDon> hoaDonPage = hoaDonRepository.findAll(spec, pageable);

            System.out.println("✅ [HoaDonService] Query thành công, số lượng: " + hoaDonPage.getTotalElements());

            // Map sang DTO
            Page<HoaDonListResponse> result = hoaDonPage.map(hoaDon -> {
                try {
                    return new HoaDonListResponse(hoaDon);
                } catch (Exception e) {
                    System.err.println("❌ [HoaDonService] Lỗi khi map HoaDon sang HoaDonListResponse: " + e.getMessage());
                    e.printStackTrace();
                    throw new RuntimeException("Lỗi khi chuyển đổi dữ liệu hóa đơn: " + e.getMessage(), e);
                }
            });

            System.out.println("✅ [HoaDonService] Map thành công!");
            return result;
        } catch (Exception e) {
            System.err.println("❌ [HoaDonService] Lỗi khi tìm kiếm hóa đơn:");
            System.err.println("  - Error: " + e.getClass().getName());
            System.err.println("  - Message: " + e.getMessage());
            e.printStackTrace();
            throw new ApiException("Lỗi khi tìm kiếm hóa đơn: " + e.getMessage(), "SEARCH_ERROR");
        }
    }

    /**
     * Lấy chi tiết một hóa đơn
     */
    public HoaDonDetailResponse getHoaDonDetail(UUID idHoaDon) {
        try {
            System.out.println("🔍 [HoaDonService] Lấy chi tiết hóa đơn: " + idHoaDon);
            
            HoaDon hoaDon = hoaDonRepository.findById(idHoaDon)
                .orElseThrow(() -> new ApiException("Không tìm thấy hóa đơn với ID: " + idHoaDon, "NOT_FOUND"));

            System.out.println("✅ [HoaDonService] Tìm thấy hóa đơn: " + hoaDon.getMa());
            
            return new HoaDonDetailResponse(hoaDon);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("❌ [HoaDonService] Lỗi khi lấy chi tiết hóa đơn:");
            System.err.println("  - Error: " + e.getClass().getName());
            System.err.println("  - Message: " + e.getMessage());
            e.printStackTrace();
            throw new ApiException("Lỗi khi lấy chi tiết hóa đơn: " + e.getMessage(), "DETAIL_ERROR");
        }
    }

    /**
     * Xây dựng Specification để tìm kiếm động
     */
    private Specification<HoaDon> buildSpecification(HoaDonSearchRequest request) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Tìm kiếm theo keyword (mã HĐ, tên KH, SĐT)
            if (request.getKeyword() != null && !request.getKeyword().trim().isEmpty()) {
                String keyword = "%" + request.getKeyword().trim().toLowerCase() + "%";
                
                Predicate maPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("ma")), keyword
                );
                Predicate tenKhPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("tenKhachHang")), keyword
                );
                Predicate sdtPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("sdt")), keyword
                );
                
                predicates.add(criteriaBuilder.or(maPredicate, tenKhPredicate, sdtPredicate));
            }

            // 2. Lọc theo trạng thái
            if (request.getTrangThai() != null) {
                predicates.add(criteriaBuilder.equal(root.get("trangThai"), request.getTrangThai()));
            }

            // 3. Lọc theo loại hóa đơn
            if (request.getLoaiHoaDon() != null) {
                predicates.add(criteriaBuilder.equal(root.get("loaiHoaDon"), request.getLoaiHoaDon()));
            }

            // 4. Lọc theo trạng thái thanh toán
            if (request.getTrangThaiThanhToan() != null) {
                predicates.add(criteriaBuilder.equal(root.get("trangThaiThanhToan"), request.getTrangThaiThanhToan()));
            }

            // 5. Lọc theo khoảng thời gian
            if (request.getStartDate() != null) {
                try {
                    Instant startInstant = request.getStartDate()
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant();
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("ngayTao"),
                        startInstant
                    ));
                } catch (Exception e) {
                    System.err.println("❌ [HoaDonService] Lỗi khi convert startDate: " + e.getMessage());
                    // Bỏ qua filter này nếu có lỗi
                }
            }

            if (request.getEndDate() != null) {
                try {
                    Instant endInstant = request.getEndDate()
                        .atTime(23, 59, 59)
                        .atZone(ZoneId.systemDefault())
                        .toInstant();
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("ngayTao"),
                        endInstant
                    ));
                } catch (Exception e) {
                    System.err.println("❌ [HoaDonService] Lỗi khi convert endDate: " + e.getMessage());
                    // Bỏ qua filter này nếu có lỗi
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}


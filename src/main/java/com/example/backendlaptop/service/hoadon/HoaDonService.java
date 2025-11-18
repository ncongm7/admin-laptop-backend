package com.example.backendlaptop.service.hoadon;

import com.example.backendlaptop.dto.hoadon.HoaDonDetailResponse;
import com.example.backendlaptop.dto.hoadon.HoaDonListResponse;
import com.example.backendlaptop.dto.hoadon.HoaDonSearchRequest;
import com.example.backendlaptop.entity.*;
import com.example.backendlaptop.expection.ApiException;
import com.example.backendlaptop.model.TrangThaiHoaDon;
import com.example.backendlaptop.repository.banhang.HoaDonRepository;
import com.example.backendlaptop.repository.banhang.HoaDonChiTietRepository;
import com.example.backendlaptop.repository.ChiTietSanPhamRepository;
import com.example.backendlaptop.repository.SerialRepository;
import com.example.backendlaptop.repository.SerialDaBanRepository;
import org.springframework.transaction.annotation.Transactional;
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
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final ChiTietSanPhamRepository chiTietSanPhamRepository;
    private final SerialRepository serialRepository;
    private final SerialDaBanRepository serialDaBanRepository;

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

    /**
     * Cập nhật trạng thái đơn hàng
     */
    public HoaDonDetailResponse capNhatTrangThai(UUID idHoaDon, Integer trangThai) {
        try {
            System.out.println("🔄 [HoaDonService] Cập nhật trạng thái hóa đơn: " + idHoaDon + " -> " + trangThai);
            
            HoaDon hoaDon = hoaDonRepository.findById(idHoaDon)
                .orElseThrow(() -> new ApiException("Không tìm thấy hóa đơn với ID: " + idHoaDon, "NOT_FOUND"));

            // Convert integer to enum
            TrangThaiHoaDon newTrangThai = TrangThaiHoaDon.values()[trangThai];
            hoaDon.setTrangThai(newTrangThai);

            // Nếu trạng thái là "Đã thanh toán", cập nhật ngày thanh toán
            if (newTrangThai == TrangThaiHoaDon.DA_THANH_TOAN && hoaDon.getNgayThanhToan() == null) {
                hoaDon.setNgayThanhToan(Instant.now());
                hoaDon.setTrangThaiThanhToan(1); // Đã thanh toán
            }

            hoaDon = hoaDonRepository.save(hoaDon);
            
            System.out.println("✅ [HoaDonService] Cập nhật trạng thái thành công");
            
            return new HoaDonDetailResponse(hoaDon);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("❌ [HoaDonService] Lỗi khi cập nhật trạng thái:");
            System.err.println("  - Error: " + e.getClass().getName());
            System.err.println("  - Message: " + e.getMessage());
            e.printStackTrace();
            throw new ApiException("Lỗi khi cập nhật trạng thái: " + e.getMessage(), "UPDATE_STATUS_ERROR");
        }
    }

    /**
     * Lấy danh sách đơn hàng của khách hàng (cho customer)
     */
    public Page<HoaDonListResponse> getCustomerOrders(UUID khachHangId, String trangThai, Pageable pageable) {
        try {
            System.out.println("🔍 [HoaDonService] Lấy đơn hàng khách: " + khachHangId + ", trạng thái: " + trangThai);
            
            if (khachHangId == null) {
                throw new ApiException("Thiếu thông tin khách hàng", "MISSING_CUSTOMER_ID");
            }

            Specification<HoaDon> spec = (root, query, criteriaBuilder) -> {
                List<Predicate> predicates = new ArrayList<>();
                
                // Filter theo khách hàng
                predicates.add(criteriaBuilder.equal(root.get("idKhachHang"), khachHangId));
                
                // Filter theo trạng thái nếu có
                if (trangThai != null && !trangThai.trim().isEmpty()) {
                    try {
                        TrangThaiHoaDon trangThaiEnum = TrangThaiHoaDon.valueOf(trangThai.trim().toUpperCase());
                        predicates.add(criteriaBuilder.equal(root.get("trangThai"), trangThaiEnum));
                    } catch (IllegalArgumentException e) {
                        System.err.println("⚠️ [HoaDonService] Trạng thái không hợp lệ: " + trangThai);
                    }
                }
                
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            };

            Page<HoaDon> hoaDonPage = hoaDonRepository.findAll(spec, pageable);
            return hoaDonPage.map(HoaDonListResponse::new);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("❌ [HoaDonService] Lỗi khi lấy đơn hàng khách: " + e.getMessage());
            e.printStackTrace();
            throw new ApiException("Lỗi khi lấy danh sách đơn hàng: " + e.getMessage(), "GET_CUSTOMER_ORDERS_ERROR");
        }
    }

    /**
     * Lấy chi tiết đơn hàng cho customer (có kiểm tra quyền)
     */
    public HoaDonDetailResponse getOrderDetailForCustomer(UUID orderId, UUID khachHangId) {
        try {
            System.out.println("🔍 [HoaDonService] Lấy chi tiết đơn: " + orderId + ", khách: " + khachHangId);
            
            HoaDon hoaDon = hoaDonRepository.findById(orderId)
                .orElseThrow(() -> new ApiException("Không tìm thấy đơn hàng", "NOT_FOUND"));

            // Kiểm tra quyền: chỉ cho phép xem đơn hàng của mình
            if (khachHangId != null && !hoaDon.getIdKhachHang().equals(khachHangId)) {
                throw new SecurityException("Bạn không có quyền xem đơn hàng này");
            }

            return new HoaDonDetailResponse(hoaDon);
        } catch (SecurityException e) {
            throw e;
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("❌ [HoaDonService] Lỗi khi lấy chi tiết đơn: " + e.getMessage());
            e.printStackTrace();
            throw new ApiException("Lỗi khi lấy chi tiết đơn hàng: " + e.getMessage(), "GET_ORDER_DETAIL_ERROR");
        }
    }

    /**
     * Hủy đơn hàng cho customer (chỉ cho phép khi CHO_THANH_TOAN)
     */
    public boolean cancelOrderForCustomer(UUID orderId, UUID khachHangId) {
        try {
            System.out.println("🚫 [HoaDonService] Hủy đơn: " + orderId + ", khách: " + khachHangId);
            
            HoaDon hoaDon = hoaDonRepository.findById(orderId)
                .orElseThrow(() -> new ApiException("Không tìm thấy đơn hàng", "NOT_FOUND"));

            // Kiểm tra quyền
            if (khachHangId != null && !hoaDon.getIdKhachHang().equals(khachHangId)) {
                throw new SecurityException("Bạn không có quyền hủy đơn hàng này");
            }

            // Chỉ cho phép hủy khi trạng thái = CHO_THANH_TOAN
            if (hoaDon.getTrangThai() != TrangThaiHoaDon.CHO_THANH_TOAN) {
                System.out.println("⚠️ [HoaDonService] Không thể hủy đơn ở trạng thái: " + hoaDon.getTrangThai());
                return false;
            }

            hoaDon.setTrangThai(TrangThaiHoaDon.DA_HUY);
            hoaDonRepository.save(hoaDon);
            
            System.out.println("✅ [HoaDonService] Đã hủy đơn hàng");
            return true;
        } catch (SecurityException e) {
            throw e;
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("❌ [HoaDonService] Lỗi khi hủy đơn: " + e.getMessage());
            e.printStackTrace();
            throw new ApiException("Lỗi khi hủy đơn hàng: " + e.getMessage(), "CANCEL_ORDER_ERROR");
        }
    }

    /**
     * Mua lại đơn hàng (thêm sản phẩm vào giỏ hàng)
     * TODO: Cần implement logic thêm vào giỏ hàng
     */
    public boolean reorderForCustomer(UUID orderId, UUID khachHangId) {
        try {
            System.out.println("🔄 [HoaDonService] Mua lại đơn: " + orderId + ", khách: " + khachHangId);
            
            HoaDon hoaDon = hoaDonRepository.findById(orderId)
                .orElseThrow(() -> new ApiException("Không tìm thấy đơn hàng", "NOT_FOUND"));

            // Kiểm tra quyền
            if (khachHangId != null && !hoaDon.getIdKhachHang().equals(khachHangId)) {
                throw new SecurityException("Bạn không có quyền thực hiện thao tác này");
            }

            // TODO: Implement logic thêm các sản phẩm trong đơn vào giỏ hàng
            // Cần inject GioHangService và thêm từng chi tiết hóa đơn vào giỏ
            
            System.out.println("⚠️ [HoaDonService] Chức năng mua lại chưa được implement đầy đủ");
            return true;
        } catch (SecurityException e) {
            throw e;
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("❌ [HoaDonService] Lỗi khi mua lại đơn: " + e.getMessage());
            e.printStackTrace();
            throw new ApiException("Lỗi khi mua lại đơn hàng: " + e.getMessage(), "REORDER_ERROR");
        }
    }

    /**
     * Xác nhận đơn hàng online và trừ kho
     * Chỉ áp dụng cho đơn hàng online (loaiHoaDon = 1) ở trạng thái CHO_THANH_TOAN
     * 
     * @param idHoaDon ID của hóa đơn
     * @param nhanVienId ID nhân viên xác nhận
     * @return HoaDonDetailResponse
     */
    @Transactional
    public HoaDonDetailResponse xacNhanDonHangOnline(UUID idHoaDon, UUID nhanVienId) {
        try {
            System.out.println("✅ [HoaDonService] Xác nhận đơn hàng online: " + idHoaDon + ", nhân viên: " + nhanVienId);
            
            // 1. Tìm hóa đơn
            HoaDon hoaDon = hoaDonRepository.findById(idHoaDon)
                .orElseThrow(() -> new ApiException("Không tìm thấy hóa đơn với ID: " + idHoaDon, "NOT_FOUND"));

            // 2. Kiểm tra loại hóa đơn (phải là online = 1)
            if (hoaDon.getLoaiHoaDon() == null || hoaDon.getLoaiHoaDon() != 1) {
                throw new ApiException("Chỉ có thể xác nhận đơn hàng online", "INVALID_ORDER_TYPE");
            }

            // 3. Kiểm tra trạng thái (phải là CHO_THANH_TOAN)
            if (hoaDon.getTrangThai() != TrangThaiHoaDon.CHO_THANH_TOAN) {
                throw new ApiException("Chỉ có thể xác nhận đơn hàng ở trạng thái 'Chờ thanh toán'. Trạng thái hiện tại: " + hoaDon.getTrangThai(), "INVALID_STATUS");
            }

            // 4. Lấy danh sách chi tiết hóa đơn từ quan hệ OneToMany
            List<HoaDonChiTiet> chiTietList = new ArrayList<>(hoaDon.getHoaDonChiTiets());
            if (chiTietList.isEmpty()) {
                throw new ApiException("Hóa đơn không có sản phẩm", "EMPTY_ORDER");
            }

            // 5. Xử lý từng sản phẩm: Trừ kho bằng Serial
            for (HoaDonChiTiet hdct : chiTietList) {
                ChiTietSanPham ctsp = hdct.getChiTietSanPham();
                int soLuongCan = hdct.getSoLuong();

                // 5.1. Kiểm tra số lượng Serial có sẵn
                int soLuongKhaDung = serialRepository.countByCtspIdAndTrangThai(ctsp.getId(), 1);
                if (soLuongKhaDung < soLuongCan) {
                    String tenSanPham = ctsp.getSanPham() != null ? ctsp.getSanPham().getTenSanPham() : "Sản phẩm";
                    throw new ApiException(
                        "Sản phẩm " + tenSanPham + " không đủ số lượng. Cần: " + soLuongCan + ", Có sẵn: " + soLuongKhaDung,
                        "INSUFFICIENT_STOCK"
                    );
                }

                // 5.2. Lấy danh sách Serial có sẵn (trangThai = 1)
                List<Serial> serials = serialRepository.findByCtspIdAndTrangThai(ctsp.getId(), 1);
                
                // 5.3. Xử lý từng Serial cần trừ
                for (int i = 0; i < soLuongCan; i++) {
                    if (i >= serials.size()) {
                        throw new ApiException("Không đủ Serial để trừ kho cho sản phẩm: " + ctsp.getId(), "INSUFFICIENT_SERIAL");
                    }

                    Serial serial = serials.get(i);

                    // 5.4. Kiểm tra Serial chưa được bán
                    if (serialDaBanRepository.existsBySerialId(serial.getId())) {
                        throw new ApiException("Serial " + serial.getSerialNo() + " đã được sử dụng", "SERIAL_ALREADY_SOLD");
                    }

                    // 5.5. Cập nhật trạng thái Serial thành "Đã bán" (2)
                    serial.setTrangThai(2);
                    serialRepository.save(serial);

                    // 5.6. Tạo bản ghi SerialDaBan
                    SerialDaBan serialDaBan = new SerialDaBan();
                    serialDaBan.setId(UUID.randomUUID());
                    serialDaBan.setIdHoaDonChiTiet(hdct);
                    serialDaBan.setIdSerial(serial);
                    serialDaBan.setNgayTao(Instant.now());
                    serialDaBanRepository.save(serialDaBan);

                    // 5.7. Cập nhật tồn kho (trừ 1 cho mỗi serial)
                    int soLuongTon = ctsp.getSoLuongTon() != null ? ctsp.getSoLuongTon() : 0;
                    ctsp.setSoLuongTon(Math.max(0, soLuongTon - 1));
                    chiTietSanPhamRepository.save(ctsp);
                }
            }

            // 6. Cập nhật trạng thái hóa đơn
            hoaDon.setTrangThai(TrangThaiHoaDon.DA_THANH_TOAN);
            hoaDon.setTrangThaiThanhToan(1); // Đã thanh toán
            hoaDon.setNgayThanhToan(Instant.now());
            
            // 7. Gán nhân viên xác nhận (nếu có)
            if (nhanVienId != null) {
                // TODO: Inject NhanVienRepository và set nhân viên
                // NhanVien nhanVien = nhanVienRepository.findById(nhanVienId).orElse(null);
                // hoaDon.setIdNhanVien(nhanVien);
            }

            hoaDon = hoaDonRepository.save(hoaDon);

            System.out.println("✅ [HoaDonService] Xác nhận đơn hàng thành công, đã trừ kho");
            
            return new HoaDonDetailResponse(hoaDon);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("❌ [HoaDonService] Lỗi khi xác nhận đơn hàng online:");
            System.err.println("  - Error: " + e.getClass().getName());
            System.err.println("  - Message: " + e.getMessage());
            e.printStackTrace();
            throw new ApiException("Lỗi khi xác nhận đơn hàng: " + e.getMessage(), "CONFIRM_ORDER_ERROR");
        }
    }

    /**
     * Hủy đơn hàng online (admin)
     * Chỉ hủy được khi trạng thái = CHO_THANH_TOAN (chưa trừ kho)
     */
    @Transactional
    public HoaDonDetailResponse huyDonHangOnline(UUID idHoaDon, UUID nhanVienId) {
        try {
            System.out.println("🚫 [HoaDonService] Hủy đơn hàng online: " + idHoaDon);
            
            HoaDon hoaDon = hoaDonRepository.findById(idHoaDon)
                .orElseThrow(() -> new ApiException("Không tìm thấy hóa đơn với ID: " + idHoaDon, "NOT_FOUND"));

            // Kiểm tra loại hóa đơn
            if (hoaDon.getLoaiHoaDon() == null || hoaDon.getLoaiHoaDon() != 1) {
                throw new ApiException("Chỉ có thể hủy đơn hàng online", "INVALID_ORDER_TYPE");
            }

            // Kiểm tra trạng thái - chỉ hủy được khi CHO_THANH_TOAN (chưa trừ kho)
            if (hoaDon.getTrangThai() != TrangThaiHoaDon.CHO_THANH_TOAN) {
                throw new ApiException("Chỉ có thể hủy đơn hàng ở trạng thái 'Chờ thanh toán'", "INVALID_STATUS");
            }

            // Cập nhật trạng thái thành DA_HUY
            hoaDon.setTrangThai(TrangThaiHoaDon.DA_HUY);
            hoaDon = hoaDonRepository.save(hoaDon);

            System.out.println("✅ [HoaDonService] Đã hủy đơn hàng online");
            
            return new HoaDonDetailResponse(hoaDon);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("❌ [HoaDonService] Lỗi khi hủy đơn hàng:");
            System.err.println("  - Error: " + e.getClass().getName());
            System.err.println("  - Message: " + e.getMessage());
            e.printStackTrace();
            throw new ApiException("Lỗi khi hủy đơn hàng: " + e.getMessage(), "CANCEL_ORDER_ERROR");
        }
    }
}


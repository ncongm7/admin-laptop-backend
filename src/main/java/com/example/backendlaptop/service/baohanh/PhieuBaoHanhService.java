package com.example.backendlaptop.service.baohanh;

import com.example.backendlaptop.expection.ApiException;
import com.example.backendlaptop.model.response.baohanh.PhieuBaoHanhResponse;
import com.example.backendlaptop.repository.PhieuBaoHanhRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class PhieuBaoHanhService {
    @Autowired
    private PhieuBaoHanhRepository repository;
    @Autowired
    private com.example.backendlaptop.repository.LichSuBaoHanhRepository lichSuBaoHanhRepository;

    @Transactional(readOnly = true)
    public List<PhieuBaoHanhResponse> getAll() {
        return repository.findAllWithRelations().stream().map(PhieuBaoHanhResponse::new).toList();
    }

    public void delete(UUID id) {
        repository.findById(id).orElseThrow(
                () -> new ApiException("Not Found", "NF"));
        repository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public PhieuBaoHanhResponse detail(UUID id) {
        return new PhieuBaoHanhResponse(
                repository.findByIdWithRelations(id).orElseThrow(() -> new ApiException("Not Found", "NF")));
    }

    @Transactional
    public PhieuBaoHanhResponse updateTrangThai(UUID id, Integer trangThai) {
        var entity = repository.findById(id)
                .orElseThrow(() -> new ApiException("Not Found", "NF"));

        // Get current status
        Integer currentStatus = entity.getTrangThaiBaoHanh();

        // Validate sequential transition
        validateStatusTransition(currentStatus, trangThai);

        entity.setTrangThaiBaoHanh(trangThai);
        entity.setNgayCapNhat(java.time.Instant.now());
        repository.save(entity);

        // Update latest LichSuBaoHanh status to match (Sync)
        try {
            java.util.List<com.example.backendlaptop.entity.LichSuBaoHanh> historyList = lichSuBaoHanhRepository
                    .findByIdBaoHanh_IdOrderByNgayTiepNhanDesc(id);
            if (!historyList.isEmpty()) {
                com.example.backendlaptop.entity.LichSuBaoHanh latestHistory = historyList.get(0);
                // Only update if current history is not completed/cancelled
                if (latestHistory.getTrangThai() != 4 && latestHistory.getTrangThai() != 5) {
                    latestHistory.setTrangThai(trangThai);
                    lichSuBaoHanhRepository.save(latestHistory);
                }
            }
        } catch (Exception e) {
            // Log error but don't fail the transaction just for history sync
            System.err.println("Error syncing LichSuBaoHanh status: " + e.getMessage());
        }

        return new PhieuBaoHanhResponse(repository.findByIdWithRelations(id).orElseThrow());
    }

    /**
     * Validate that status transitions are sequential
     * Enforces workflow: 0 → 1 → 2 → 3 → 4 (cannot skip)
     * Status 5 (cancelled) can be set from any non-completed status
     */
    private void validateStatusTransition(Integer currentStatus, Integer newStatus) {
        if (currentStatus == null || newStatus == null) {
            throw new ApiException("Trạng thái không được null", "INVALID_STATUS");
        }

        // Validate range 0-5
        if (newStatus < 0 || newStatus > 5) {
            throw new ApiException("Trạng thái không hợp lệ. Giá trị phải từ 0-5", "INVALID_STATUS");
        }

        // Cannot change status if already completed
        if (currentStatus == 4) {
            throw new ApiException("Không thể thay đổi trạng thái của phiếu đã hoàn thành", "STATUS_LOCKED");
        }

        // Cannot change status if already cancelled
        if (currentStatus == 5) {
            throw new ApiException("Không thể thay đổi trạng thái của phiếu đã hủy", "STATUS_LOCKED");
        }

        // Allow cancellation (5) from any non-completed/non-cancelled status
        if (newStatus == 5) {
            return; // OK to cancel
        }

        // Same status - no change needed
        if (currentStatus.equals(newStatus)) {
            return; // OK, no change
        }

        // Must be sequential: only +1 or -1 (for corrections)
        int diff = newStatus - currentStatus;

        // Only allow forward (+1) or backward (-1) for corrections
        if (Math.abs(diff) != 1) {
            String currentName = getStatusName(currentStatus);
            String allowedNext = currentStatus < 4 ? getStatusName(currentStatus + 1) : "không có";
            String allowedPrev = currentStatus > 0 ? getStatusName(currentStatus - 1) : "không có";

            throw new ApiException(
                    String.format(
                            "Trạng thái phải chuyển tuần tự. Từ '%s' chỉ có thể chuyển sang '%s' (tiến) hoặc '%s' (lùi)",
                            currentName, allowedNext, allowedPrev),
                    "INVALID_STATUS_TRANSITION");
        }

        // Additional validation: cannot move backwards past initial state
        if (newStatus < 0) {
            throw new ApiException("Không thể quay lại trước trạng thái ban đầu", "INVALID_STATUS");
        }
    }

    /**
     * Get status name for error messages
     */
    private String getStatusName(Integer status) {
        return switch (status) {
            case 0 -> "Chờ xử lý";
            case 1 -> "Đã tiếp nhận";
            case 2 -> "Đang sửa chữa";
            case 3 -> "Chờ bàn giao";
            case 4 -> "Hoàn thành";
            case 5 -> "Đã hủy";
            default -> "Không xác định";
        };
    }

    @Autowired
    private com.example.backendlaptop.repository.SerialDaBanRepository serialDaBanRepository;
    @Autowired
    private com.example.backendlaptop.repository.KhachHangRepository khachHangRepository;

    @Transactional(readOnly = true)
    public List<java.util.Map<String, Object>> getEligibleSerials(UUID customerId) {
        // Get all serials bought by customer
        var result = new java.util.ArrayList<java.util.Map<String, Object>>();

        var allSerials = serialDaBanRepository.findAll();
        for (var serialDaBan : allSerials) {
            try {
                // Check if belongs to customer
                if (serialDaBan.getIdHoaDonChiTiet() != null &&
                        serialDaBan.getIdHoaDonChiTiet().getHoaDon() != null &&
                        serialDaBan.getIdHoaDonChiTiet().getHoaDon().getIdKhachHang() != null &&
                        serialDaBan.getIdHoaDonChiTiet().getHoaDon().getIdKhachHang().getId().equals(customerId)) {

                    java.util.Map<String, Object> item = new java.util.HashMap<>();
                    item.put("id", serialDaBan.getIdSerial().getId());
                    item.put("serialNo", serialDaBan.getIdSerial().getSerialNo());

                    // Get product details safely
                    if (serialDaBan.getIdSerial().getCtsp() != null) {
                        item.put("maCtsp", serialDaBan.getIdSerial().getCtsp().getMaCtsp());
                        if (serialDaBan.getIdSerial().getCtsp().getSanPham() != null) {
                            item.put("tenSanPham", serialDaBan.getIdSerial().getCtsp().getSanPham().getTenSanPham());
                        }
                    }
                    if (serialDaBan.getIdHoaDonChiTiet().getHoaDon() != null) {
                        item.put("idHoaDon", serialDaBan.getIdHoaDonChiTiet().getHoaDon().getId());
                    }
                    item.put("trangThai", 2);
                    result.add(item);
                }
            } catch (Exception e) {
                // Skip this serial if error
            }
        }

        return result;
    }

    @Transactional
    public PhieuBaoHanhResponse createWarrantyRequest(
            com.example.backendlaptop.dto.warranty.WarrantyCreateRequest request, UUID userId) {
        // 1. Find SerialDaBan
        var serialDaBan = serialDaBanRepository.findByIdSerial_Id(request.getSerialId())
                .orElseThrow(() -> new ApiException("Sản phẩm không tồn tại hoặc chưa được bán", "SERIAL_NOT_FOUND"));

        // 2. Verify Ownership
        if (!serialDaBan.getIdHoaDonChiTiet().getHoaDon().getIdKhachHang().getId().equals(userId)) {
            throw new ApiException("Bạn không sở hữu sản phẩm này", "ACCESS_DENIED");
        }

        // 3. Create Request
        var entity = new com.example.backendlaptop.entity.PhieuBaoHanh();
        entity.setId(UUID.randomUUID());
        entity.setIdKhachHang(serialDaBan.getIdHoaDonChiTiet().getHoaDon().getIdKhachHang());
        entity.setIdSerialDaBan(serialDaBan);
        entity.setNgayBatDau(java.time.Instant.now());
        // Default 1 year warranty or calculate based on Product
        entity.setNgayKetThuc(java.time.Instant.now().plus(365, java.time.temporal.ChronoUnit.DAYS));

        entity.setTrangThaiBaoHanh(0); // 0: Chờ xử lý (initial status)

        // Generate Code
        String maPhieu = "BH" + System.currentTimeMillis();
        entity.setMaPhieuBaoHanh(maPhieu);

        entity.setMoTa(request.getMoTa());
        entity.setHinhAnh(request.getHinhAnh());
        entity.setChiPhi(java.math.BigDecimal.ZERO);
        entity.setSoLanSuaChua(0);
        entity.setNgayTao(java.time.Instant.now());

        repository.save(entity);

        // Return response with relations
        return new PhieuBaoHanhResponse(repository.findByIdWithRelations(entity.getId()).orElse(entity));
    }

    /**
     * Get warranty statistics
     * Status values: 0=Chờ xử lý, 1=Đã tiếp nhận, 2=Đang sửa chữa, 3=Chờ bàn giao,
     * 4=Hoàn thành, 5=Đã hủy
     */
    @Transactional(readOnly = true)
    public java.util.Map<String, Object> getStatistics() {
        var allWarranties = repository.findAll();

        java.util.Map<String, Object> stats = new java.util.HashMap<>();

        // Total warranties
        stats.put("total", allWarranties.size());

        // Count by status (0=Chờ xử lý, 1=Đã tiếp nhận, 2=Đang sửa chữa, 3=Chờ bàn
        // giao, 4=Hoàn thành, 5=Đã hủy)
        long pending = allWarranties.stream().filter(w -> w.getTrangThaiBaoHanh() == 0).count(); // Chờ xử lý
        long received = allWarranties.stream().filter(w -> w.getTrangThaiBaoHanh() == 1).count(); // Đã tiếp nhận
        long repairing = allWarranties.stream().filter(w -> w.getTrangThaiBaoHanh() == 2).count(); // Đang sửa chữa
        long waitingHandover = allWarranties.stream().filter(w -> w.getTrangThaiBaoHanh() == 3).count(); // Chờ bàn giao
        long completed = allWarranties.stream().filter(w -> w.getTrangThaiBaoHanh() == 4).count(); // Hoàn thành
        long cancelled = allWarranties.stream().filter(w -> w.getTrangThaiBaoHanh() == 5).count(); // Đã hủy

        stats.put("pending", pending); // Chờ xử lý
        stats.put("received", received); // Đã tiếp nhận
        stats.put("repairing", repairing); // Đang sửa chữa
        stats.put("waitingHandover", waitingHandover); // Chờ bàn giao
        stats.put("completed", completed); // Hoàn thành
        stats.put("cancelled", cancelled); // Đã hủy

        // Monthly statistics (last 12 months)
        var monthlyData = new java.util.ArrayList<java.util.Map<String, Object>>();
        var now = java.time.Instant.now();
        for (int i = 11; i >= 0; i--) {
            var monthStart = now.minus(i * 30, java.time.temporal.ChronoUnit.DAYS);
            var monthEnd = now.minus((i - 1) * 30, java.time.temporal.ChronoUnit.DAYS);

            long count = allWarranties.stream()
                    .filter(w -> w.getNgayTao() != null &&
                            w.getNgayTao().isAfter(monthStart) &&
                            w.getNgayTao().isBefore(monthEnd))
                    .count();

            java.util.Map<String, Object> monthData = new java.util.HashMap<>();
            monthData.put("month",
                    java.time.YearMonth.from(monthStart.atZone(java.time.ZoneId.systemDefault())).toString());
            monthData.put("count", count);
            monthlyData.add(monthData);
        }
        stats.put("monthlyData", monthlyData);

        return stats;
    }

    /**
     * Search and filter warranties
     */
    @Transactional(readOnly = true)
    public List<PhieuBaoHanhResponse> searchWarranties(
            Integer trangThai,
            java.time.Instant fromDate,
            java.time.Instant toDate,
            String keyword) {

        var all = repository.findAllWithRelations();

        return all.stream()
                .filter(w -> {
                    // Filter by status
                    if (trangThai != null && !w.getTrangThaiBaoHanh().equals(trangThai)) {
                        return false;
                    }

                    // Filter by date range
                    if (fromDate != null && w.getNgayTao() != null && w.getNgayTao().isBefore(fromDate)) {
                        return false;
                    }
                    if (toDate != null && w.getNgayTao() != null && w.getNgayTao().isAfter(toDate)) {
                        return false;
                    }

                    // Filter by keyword (search in code, customer name, product)
                    if (keyword != null && !keyword.isEmpty()) {
                        String lowerKeyword = keyword.toLowerCase();
                        boolean matchCode = w.getMaPhieuBaoHanh() != null &&
                                w.getMaPhieuBaoHanh().toLowerCase().contains(lowerKeyword);
                        boolean matchCustomer = w.getIdKhachHang() != null &&
                                w.getIdKhachHang().getHoTen() != null &&
                                w.getIdKhachHang().getHoTen().toLowerCase().contains(lowerKeyword);

                        if (!matchCode && !matchCustomer) {
                            return false;
                        }
                    }

                    return true;
                })
                .map(PhieuBaoHanhResponse::new)
                .toList();
    }
}

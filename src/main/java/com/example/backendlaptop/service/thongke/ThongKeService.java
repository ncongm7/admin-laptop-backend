package com.example.backendlaptop.service.thongke;

import com.example.backendlaptop.dto.thongke.*;
import com.example.backendlaptop.entity.HoaDon;
import com.example.backendlaptop.model.TrangThaiHoaDon;
import com.example.backendlaptop.repository.banhang.HoaDonRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service xử lý logic thống kê cho Dashboard
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ThongKeService {

    private final EntityManager entityManager;
    private final HoaDonRepository hoaDonRepository;

    /**
     * Lấy thống kê tổng quan cho Dashboard
     * Tính toán tất cả các chỉ số trong một lần truy vấn
     */
    public ThongKeTongQuanResponse getThongKeTongQuan(LocalDate startDate, LocalDate endDate) {
        log.info("🔍 [ThongKeService] Lấy thống kê tổng quan từ {} đến {}", startDate, endDate);

        // Convert LocalDate sang Instant (start of day và end of day)
        Instant startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endInstant = endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();

        // Tính khoảng thời gian kỳ trước (cùng độ dài)
        long daysBetween = java.time.Duration.between(startInstant, endInstant).toDays();
        Instant previousStartInstant = startInstant.minusSeconds(daysBetween * 86400);
        Instant previousEndInstant = startInstant.minusSeconds(1); // Trước 1 giây so với kỳ hiện tại

        // Thực hiện các truy vấn song song
        Long doanhSo = countHoaDonHoanThanh(startInstant, endInstant);
        Long doanhSoKyTruoc = countHoaDonHoanThanh(previousStartInstant, previousEndInstant);

        BigDecimal doanhThu = tinhTongDoanhThu(startInstant, endInstant);
        BigDecimal doanhThuKyTruoc = tinhTongDoanhThu(previousStartInstant, previousEndInstant);

        BigDecimal loiNhuan = tinhLoiNhuan(startInstant, endInstant);

        Long khachHangMoi = countKhachHangMoi(startInstant, endInstant);
        Long khachHangMoiKyTruoc = countKhachHangMoi(previousStartInstant, previousEndInstant);

        Long khachHangHoatDong = countKhachHangHoatDong(startInstant, endInstant);

        // Tồn kho - không phụ thuộc thời gian
        Long sapHetHang = countSanPhamSapHet();
        Long canBoSung = countSanPhamCanBoSung();

        // Tính toán % thay đổi
        Double salesGrowth = calculatePercentageChange(doanhSo, doanhSoKyTruoc);
        Double revenueGrowth = calculatePercentageChange(doanhThu, doanhThuKyTruoc);
        Double customerGrowth = calculatePercentageChange(khachHangMoi, khachHangMoiKyTruoc);

        log.info("✅ [ThongKeService] Thống kê tổng quan:");
        log.info("   - Doanh số: {} (tăng trưởng: {}%)", doanhSo, salesGrowth);
        log.info("   - Doanh thu: {} (tăng trưởng: {}%)", doanhThu, revenueGrowth);
        log.info("   - Khách hàng mới: {} (tăng trưởng: {}%)", khachHangMoi, customerGrowth);
        log.info("   - Lợi nhuận: {}", loiNhuan);

        // Xây dựng response
        return ThongKeTongQuanResponse.builder()
                .doanhSo(ThongKeTongQuanResponse.DoanhSoInfo.builder()
                        .giaTri(doanhSo)
                        .soSanhKyTruoc(salesGrowth)
                        .build())
                .doanhThu(ThongKeTongQuanResponse.DoanhThuInfo.builder()
                        .giaTri(doanhThu)
                        .soSanhKyTruoc(revenueGrowth)
                        .loiNhuan(loiNhuan)
                        .build())
                .khachHang(ThongKeTongQuanResponse.KhachHangInfo.builder()
                        .giaTri(khachHangMoi)
                        .soSanhKyTruoc(customerGrowth)
                        .moiThangNay(calculateKhachHangMoiThangNay())
                        .hoatDong(khachHangHoatDong)
                        .build())
                .tonKho(ThongKeTongQuanResponse.TonKhoInfo.builder()
                        .sapHetHang(sapHetHang)
                        .canBoSung(canBoSung)
                        .build())
                .build();
    }

    /**
     * Lấy dữ liệu biểu đồ doanh số
     */
    public List<BieuDoDoanhSoResponse> getBieuDoDoanhSo(LocalDate startDate, LocalDate endDate, String groupBy) {
        log.info("🔍 [ThongKeService] Lấy dữ liệu biểu đồ doanh số, groupBy: {}", groupBy);

        Instant startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endInstant = endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();

        List<Object[]> rawData;

        switch (groupBy.toLowerCase()) {
            case "day":
                rawData = getBieuDoDoanhSoTheoNgay(startInstant, endInstant);
                break;
            case "week":
                rawData = getBieuDoDoanhSoTheoTuan(startInstant, endInstant);
                break;
            case "month":
                rawData = getBieuDoDoanhSoTheoThang(startInstant, endInstant);
                break;
            case "quarter":
                rawData = getBieuDoDoanhSoTheoQuy(startInstant, endInstant);
                break;
            case "year":
                rawData = getBieuDoDoanhSoTheoNam(startInstant, endInstant);
                break;
            default:
                rawData = getBieuDoDoanhSoTheoNgay(startInstant, endInstant);
                log.warn("⚠️ [ThongKeService] groupBy không hợp lệ: {}, sử dụng mặc định: day", groupBy);
        }

        List<BieuDoDoanhSoResponse> results = new ArrayList<>();
        for (Object[] row : rawData) {
            results.add(BieuDoDoanhSoResponse.builder()
                    .thoiGian((String) row[0])
                    .doanhThu((BigDecimal) row[1])
                    .soHoaDon(((Number) row[2]).longValue())
                    .build());
        }

        log.info("✅ [ThongKeService] Trả về {} điểm dữ liệu biểu đồ", results.size());
        return results;
    }

    /**
     * Lấy top sản phẩm bán chạy
     */
    public List<SanPhamBanChayResponse> getSanPhamBanChay(LocalDate startDate, LocalDate endDate, Integer limit) {
        log.info("🔍 [ThongKeService] Lấy top {} sản phẩm bán chạy từ {} đến {}", limit, startDate, endDate);

        Instant startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endInstant = endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();

        List<Object[]> rawData = getTopSanPhamBanChay(startInstant, endInstant, limit);

        List<SanPhamBanChayResponse> results = new ArrayList<>();
        for (Object[] row : rawData) {
            // Convert String UUID sang UUID object
            UUID id = row[0] instanceof String ? UUID.fromString((String) row[0]) : (UUID) row[0];

            results.add(SanPhamBanChayResponse.builder()
                    .id(id)
                    .tenSanPham((String) row[1])
                    .maCtsp((String) row[2])
                    .anhDaiDien((String) row[3])
                    .soLuongBan(((Number) row[4]).longValue())
                    .doanhThu((BigDecimal) row[5])
                    .build());
        }

        log.info("✅ [ThongKeService] Trả về {} sản phẩm bán chạy", results.size());
        return results;
    }

    // ==================== Helper methods using EntityManager ====================

    private Long countHoaDonHoanThanh(Instant startDate, Instant endDate) {
        // Đếm đơn đã thanh toán (DA_THANH_TOAN = 1) hoặc hoàn thành (HOAN_THANH = 4)
        // Vì cả 2 đều là đơn đã được thanh toán và xử lý thành công
        Query query = entityManager.createNativeQuery("""
                SELECT COUNT(*)
                FROM hoa_don
                WHERE (trang_thai = 1 OR trang_thai = 4)
                  AND ngay_tao >= ?1
                  AND ngay_tao <= ?2
                """);
        query.setParameter(1, startDate);
        query.setParameter(2, endDate);
        Object result = query.getSingleResult();
        return ((Number) result).longValue();
    }

    private BigDecimal tinhTongDoanhThu(Instant startDate, Instant endDate) {
        // Tính doanh thu từ đơn đã thanh toán (DA_THANH_TOAN = 1) hoặc hoàn thành
        // (HOAN_THANH = 4)
        // Vì cả 2 đều là đơn đã được thanh toán thành công
        Query query = entityManager.createNativeQuery("""
                SELECT COALESCE(SUM(tong_tien_sau_giam), 0)
                FROM hoa_don
                WHERE (trang_thai = 1 OR trang_thai = 4)
                  AND ngay_tao >= ?1
                  AND ngay_tao <= ?2
                """);
        query.setParameter(1, startDate);
        query.setParameter(2, endDate);
        Object result = query.getSingleResult();
        return (BigDecimal) result;
    }

    private BigDecimal tinhLoiNhuan(Instant startDate, Instant endDate) {
        // Tính lợi nhuận từ đơn đã thanh toán (DA_THANH_TOAN = 1) hoặc hoàn thành
        // (HOAN_THANH = 4)
        Query query = entityManager.createNativeQuery("""
                SELECT COALESCE(SUM((hdct.don_gia - ctsp.gia_nhap) * hdct.so_luong), 0)
                FROM hoa_don hd
                JOIN hoa_don_chi_tiet hdct ON hd.id = hdct.id_don_hang
                JOIN chi_tiet_san_pham ctsp ON hdct.id_ctsp = ctsp.id
                WHERE (hd.trang_thai = 1 OR hd.trang_thai = 4)
                  AND hd.ngay_tao >= ?1
                  AND hd.ngay_tao <= ?2
                """);
        query.setParameter(1, startDate);
        query.setParameter(2, endDate);
        Object result = query.getSingleResult();
        return (BigDecimal) result;
    }

    private Long countKhachHangMoi(Instant startDate, Instant endDate) {
        // Đếm khách hàng mới từ đơn đã thanh toán (DA_THANH_TOAN = 1) hoặc hoàn thành
        // (HOAN_THANH = 4)
        Query query = entityManager.createNativeQuery("""
                SELECT COUNT(DISTINCT id_khach_hang)
                FROM hoa_don
                WHERE (trang_thai = 1 OR trang_thai = 4)
                  AND ngay_tao >= ?1
                  AND ngay_tao <= ?2
                  AND id_khach_hang NOT IN (
                      SELECT DISTINCT id_khach_hang
                      FROM hoa_don
                      WHERE (trang_thai = 1 OR trang_thai = 4)
                        AND ngay_tao < ?3
                  )
                """);
        query.setParameter(1, startDate);
        query.setParameter(2, endDate);
        query.setParameter(3, startDate);
        Object result = query.getSingleResult();
        return ((Number) result).longValue();
    }

    private Long countKhachHangHoatDong(Instant startDate, Instant endDate) {
        // Đếm khách hàng hoạt động từ đơn đã thanh toán (DA_THANH_TOAN = 1) hoặc hoàn
        // thành (HOAN_THANH = 4)
        Query query = entityManager.createNativeQuery("""
                SELECT COUNT(DISTINCT id_khach_hang)
                FROM hoa_don
                WHERE (trang_thai = 1 OR trang_thai = 4)
                  AND ngay_tao >= ?1
                  AND ngay_tao <= ?2
                """);
        query.setParameter(1, startDate);
        query.setParameter(2, endDate);
        Object result = query.getSingleResult();
        return ((Number) result).longValue();
    }

    private Long countSanPhamSapHet() {
        Query query = entityManager.createNativeQuery("""
                SELECT COUNT(*)
                FROM chi_tiet_san_pham
                WHERE so_luong_ton > 0 AND so_luong_ton <= 5
                """);
        Object result = query.getSingleResult();
        return ((Number) result).longValue();
    }

    private Long countSanPhamCanBoSung() {
        Query query = entityManager.createNativeQuery("""
                SELECT COUNT(*)
                FROM chi_tiet_san_pham
                WHERE so_luong_ton = 0
                """);
        Object result = query.getSingleResult();
        return ((Number) result).longValue();
    }

    private List<Object[]> getBieuDoDoanhSoTheoNgay(Instant startDate, Instant endDate) {
        // Tính doanh thu từ đơn đã thanh toán (DA_THANH_TOAN = 1) hoặc hoàn thành
        // (HOAN_THANH = 4)
        Query query = entityManager.createNativeQuery("""
                SELECT
                    CONVERT(VARCHAR(10), hd.ngay_tao, 120) AS thoiGian,
                    COALESCE(SUM(hd.tong_tien_sau_giam), 0) AS doanhThu,
                    COUNT(*) AS soHoaDon
                FROM hoa_don hd
                WHERE (hd.trang_thai = 1 OR hd.trang_thai = 4)
                  AND hd.ngay_tao >= ?1
                  AND hd.ngay_tao <= ?2
                GROUP BY CONVERT(VARCHAR(10), hd.ngay_tao, 120)
                ORDER BY thoiGian ASC
                """);
        query.setParameter(1, startDate);
        query.setParameter(2, endDate);
        @SuppressWarnings("unchecked")
        List<Object[]> result = query.getResultList();
        return result;
    }

    private List<Object[]> getBieuDoDoanhSoTheoThang(Instant startDate, Instant endDate) {
        // Tính doanh thu từ đơn đã thanh toán (DA_THANH_TOAN = 1) hoặc hoàn thành
        // (HOAN_THANH = 4)
        Query query = entityManager.createNativeQuery("""
                SELECT
                    FORMAT(hd.ngay_tao, 'yyyy-MM') AS thoiGian,
                    COALESCE(SUM(hd.tong_tien_sau_giam), 0) AS doanhThu,
                    COUNT(*) AS soHoaDon
                FROM hoa_don hd
                WHERE (hd.trang_thai = 1 OR hd.trang_thai = 4)
                  AND hd.ngay_tao >= ?1
                  AND hd.ngay_tao <= ?2
                GROUP BY FORMAT(hd.ngay_tao, 'yyyy-MM')
                ORDER BY thoiGian ASC
                """);
        query.setParameter(1, startDate);
        query.setParameter(2, endDate);
        @SuppressWarnings("unchecked")
        List<Object[]> result = query.getResultList();
        return result;
    }

    private List<Object[]> getBieuDoDoanhSoTheoNam(Instant startDate, Instant endDate) {
        // Tính doanh thu từ đơn đã thanh toán (DA_THANH_TOAN = 1) hoặc hoàn thành
        // (HOAN_THANH = 4)
        Query query = entityManager.createNativeQuery("""
                SELECT
                    FORMAT(hd.ngay_tao, 'yyyy') AS thoiGian,
                    COALESCE(SUM(hd.tong_tien_sau_giam), 0) AS doanhThu,
                    COUNT(*) AS soHoaDon
                FROM hoa_don hd
                WHERE (hd.trang_thai = 1 OR hd.trang_thai = 4)
                  AND hd.ngay_tao >= ?1
                  AND hd.ngay_tao <= ?2
                GROUP BY FORMAT(hd.ngay_tao, 'yyyy')
                ORDER BY thoiGian ASC
                """);
        query.setParameter(1, startDate);
        query.setParameter(2, endDate);
        @SuppressWarnings("unchecked")
        List<Object[]> result = query.getResultList();
        return result;
    }

    private List<Object[]> getBieuDoDoanhSoTheoTuan(Instant startDate, Instant endDate) {
        // Tính doanh thu từ đơn đã thanh toán theo tuần (ISO week)
        Query query = entityManager.createNativeQuery("""
                SELECT
                    CONCAT(DATEPART(YEAR, hd.ngay_tao), '-W',
                           FORMAT(DATEPART(ISO_WEEK, hd.ngay_tao), '00')) AS thoiGian,
                    COALESCE(SUM(hd.tong_tien_sau_giam), 0) AS doanhThu,
                    COUNT(*) AS soHoaDon
                FROM hoa_don hd
                WHERE (hd.trang_thai = 1 OR hd.trang_thai = 4)
                  AND hd.ngay_tao >= ?1
                  AND hd.ngay_tao <= ?2
                GROUP BY DATEPART(YEAR, hd.ngay_tao), DATEPART(ISO_WEEK, hd.ngay_tao)
                ORDER BY DATEPART(YEAR, hd.ngay_tao) ASC, DATEPART(ISO_WEEK, hd.ngay_tao) ASC
                """);
        query.setParameter(1, startDate);
        query.setParameter(2, endDate);
        @SuppressWarnings("unchecked")
        List<Object[]> result = query.getResultList();
        return result;
    }

    private List<Object[]> getBieuDoDoanhSoTheoQuy(Instant startDate, Instant endDate) {
        // Tính doanh thu từ đơn đã thanh toán theo quý
        Query query = entityManager.createNativeQuery("""
                SELECT
                    CONCAT(DATEPART(YEAR, hd.ngay_tao), '-Q', DATEPART(QUARTER, hd.ngay_tao)) AS thoiGian,
                    COALESCE(SUM(hd.tong_tien_sau_giam), 0) AS doanhThu,
                    COUNT(*) AS soHoaDon
                FROM hoa_don hd
                WHERE (hd.trang_thai = 1 OR hd.trang_thai = 4)
                  AND hd.ngay_tao >= ?1
                  AND hd.ngay_tao <= ?2
                GROUP BY DATEPART(YEAR, hd.ngay_tao), DATEPART(QUARTER, hd.ngay_tao)
                ORDER BY DATEPART(YEAR, hd.ngay_tao) ASC, DATEPART(QUARTER, hd.ngay_tao) ASC
                """);
        query.setParameter(1, startDate);
        query.setParameter(2, endDate);
        @SuppressWarnings("unchecked")
        List<Object[]> result = query.getResultList();
        return result;
    }

    private List<Object[]> getTopSanPhamBanChay(Instant startDate, Instant endDate, Integer limit) {
        Query query = entityManager.createNativeQuery(
                """
                        SELECT TOP (?3)
                            ctsp.id AS id,
                            sp.ten_san_pham AS tenSanPham,
                            ctsp.ma_ctsp AS maCtsp,
                            hi.url AS anhDaiDien,
                            SUM(hdct.so_luong) AS soLuongBan,
                            SUM(hdct.don_gia * hdct.so_luong) AS doanhThu
                        FROM hoa_don_chi_tiet hdct
                        INNER JOIN chi_tiet_san_pham ctsp ON hdct.id_ctsp = ctsp.id
                        INNER JOIN hoa_don hd ON hdct.id_don_hang = hd.id
                        LEFT JOIN san_pham sp ON ctsp.sp_id = sp.id
                        LEFT JOIN (
                            SELECT id_spct, url,
                                   ROW_NUMBER() OVER (PARTITION BY id_spct ORDER BY anh_chinh_dai_dien DESC, ngay_tao DESC) AS rn
                            FROM hinh_anh
                        ) hi ON ctsp.id = hi.id_spct AND hi.rn = 1
                        WHERE (hd.trang_thai = 1 OR hd.trang_thai = 4)
                          AND hd.ngay_tao >= ?1
                          AND hd.ngay_tao <= ?2
                        GROUP BY ctsp.id, sp.ten_san_pham, ctsp.ma_ctsp, hi.url
                        ORDER BY soLuongBan DESC
                        """);
        query.setParameter(1, startDate);
        query.setParameter(2, endDate);
        query.setParameter(3, limit);
        @SuppressWarnings("unchecked")
        List<Object[]> result = query.getResultList();
        return result;
    }

    // ==================== Private helper methods ====================

    /**
     * Tính % thay đổi giữa 2 giá trị
     */
    private Double calculatePercentageChange(Number current, Number previous) {
        if (previous == null || previous.doubleValue() == 0) {
            return current != null && current.doubleValue() > 0 ? 100.0 : 0.0;
        }
        return ((current.doubleValue() - previous.doubleValue()) / previous.doubleValue()) * 100;
    }

    /**
     * Tính số khách hàng mới tháng này
     */
    private Long calculateKhachHangMoiThangNay() {
        LocalDate firstDayOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate lastDayOfMonth = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());

        Instant start = firstDayOfMonth.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant end = lastDayOfMonth.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();

        return countKhachHangMoi(start, end);
    }

    /**
     * Lấy danh sách giao dịch gần đây
     * 
     * @param limit Số lượng giao dịch (mặc định: 10)
     */
    public List<GiaoDichGanDayResponse> getGiaoDichGanDay(Integer limit) {
        log.info("🔍 [ThongKeService] Lấy {} giao dịch gần đây", limit);

        List<HoaDon> hoaDons = hoaDonRepository.findAll()
                .stream()
                .filter(hd -> hd.getTrangThai() == TrangThaiHoaDon.DA_THANH_TOAN ||
                        hd.getTrangThai() == TrangThaiHoaDon.DA_HUY)
                .sorted((a, b) -> {
                    Instant timeA = a.getNgayTao() != null ? a.getNgayTao() : Instant.MIN;
                    Instant timeB = b.getNgayTao() != null ? b.getNgayTao() : Instant.MIN;
                    return timeB.compareTo(timeA); // Sắp xếp mới nhất trước
                })
                .limit(limit != null ? limit : 10)
                .collect(Collectors.toList());

        List<GiaoDichGanDayResponse> results = new ArrayList<>();
        for (HoaDon hd : hoaDons) {
            String loai = hd.getTrangThai() == TrangThaiHoaDon.DA_HUY ? "refund" : "sale";
            String tenKhachHang = hd.getTenKhachHang();
            if (tenKhachHang == null && hd.getIdKhachHang() != null) {
                tenKhachHang = hd.getIdKhachHang().getHoTen() != null ? hd.getIdKhachHang().getHoTen() : "Khách lẻ";
            }
            if (tenKhachHang == null) {
                tenKhachHang = "Khách lẻ";
            }

            results.add(GiaoDichGanDayResponse.builder()
                    .id(hd.getId())
                    .maHoaDon(hd.getMa())
                    .tenKhachHang(tenKhachHang)
                    .tongTien(hd.getTongTienSauGiam() != null ? hd.getTongTienSauGiam()
                            : (hd.getTongTien() != null ? hd.getTongTien() : BigDecimal.ZERO))
                    .ngayTao(hd.getNgayTao())
                    .trangThai(hd.getTrangThai() != null ? hd.getTrangThai().name() : "UNKNOWN")
                    .loai(loai)
                    .build());
        }

        log.info("✅ [ThongKeService] Trả về {} giao dịch gần đây", results.size());
        return results;
    }

    /**
     * Lấy danh sách hoạt động khách hàng gần đây
     * 
     * @param limit Số lượng hoạt động (mặc định: 10)
     */
    public List<HoatDongKhachHangResponse> getHoatDongKhachHang(Integer limit) {
        log.info("🔍 [ThongKeService] Lấy {} hoạt động khách hàng gần đây", limit);

        // Lấy các hóa đơn gần đây để tạo hoạt động "purchase"
        List<HoaDon> hoaDons = hoaDonRepository.findAll()
                .stream()
                .filter(hd -> hd.getTrangThai() == TrangThaiHoaDon.DA_THANH_TOAN)
                .sorted((a, b) -> {
                    Instant timeA = a.getNgayTao() != null ? a.getNgayTao() : Instant.MIN;
                    Instant timeB = b.getNgayTao() != null ? b.getNgayTao() : Instant.MIN;
                    return timeB.compareTo(timeA); // Sắp xếp mới nhất trước
                })
                .limit(limit != null ? limit : 10)
                .collect(Collectors.toList());

        List<HoatDongKhachHangResponse> results = new ArrayList<>();
        for (HoaDon hd : hoaDons) {
            String tenKhachHang = hd.getTenKhachHang();
            if (tenKhachHang == null && hd.getIdKhachHang() != null) {
                tenKhachHang = hd.getIdKhachHang().getHoTen() != null ? hd.getIdKhachHang().getHoTen() : "Khách lẻ";
            }
            if (tenKhachHang == null) {
                tenKhachHang = "Khách lẻ";
            }

            // Đếm số lượng sản phẩm
            int soLuongSanPham = hd.getHoaDonChiTiets() != null ? hd.getHoaDonChiTiets().size() : 0;
            String moTa = soLuongSanPham > 0 ? String.format("Đã mua %d sản phẩm", soLuongSanPham) : "Đã mua sản phẩm";

            results.add(HoatDongKhachHangResponse.builder()
                    .id(hd.getId())
                    .tenKhachHang(tenKhachHang)
                    .moTa(moTa)
                    .thoiGian(hd.getNgayTao())
                    .loai("purchase")
                    .build());
        }

        log.info("✅ [ThongKeService] Trả về {} hoạt động khách hàng", results.size());
        return results;
    }
}

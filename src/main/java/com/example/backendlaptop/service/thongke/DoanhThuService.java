package com.example.backendlaptop.service.thongke;

import com.example.backendlaptop.dto.thongke.*;
import com.example.backendlaptop.model.PaymentMethod;
import com.example.backendlaptop.model.SalesChannel;
import com.example.backendlaptop.model.TrangThaiHoaDon;
import com.example.backendlaptop.repository.banhang.HoaDonRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Service mới cho thống kê doanh thu CHÍNH XÁC theo nghiệp vụ
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DoanhThuService {
    
    private final EntityManager entityManager;
    private final HoaDonRepository hoaDonRepository;
    
    private BigDecimal safeBigDecimal(Object obj) {
        if (obj == null) return BigDecimal.ZERO;
        if (obj instanceof BigDecimal) return (BigDecimal) obj;
        if (obj instanceof Number) return BigDecimal.valueOf(((Number) obj).doubleValue());
        try {
            return new BigDecimal(obj.toString());
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    /**
     * ===== LOGIC DOANH THU THỰC (ĐÃ XÁC NHẬN) =====
     * CHỈ tính các đơn đã xác nhận thanh toán:
     * - CASH: Xác nhận ngay (có payment_confirmed_at)
     * - QR: Chỉ khi payment_confirmed_at IS NOT NULL
     * - COD: Chỉ khi status = HOAN_THANH (4)
     */
    public BigDecimal tinhDoanhThuThuc(Instant startDate, Instant endDate) {
        Query query = entityManager.createNativeQuery("""
            SELECT COALESCE(SUM(tong_tien_sau_giam), 0)
            FROM hoa_don
            WHERE (
                -- CASH: đã confirm (POS thường confirm ngay)
                (payment_method = 'CASH' AND payment_confirmed_at IS NOT NULL)
                OR
                -- QR: CHỈ khi đã confirm
                (payment_method = 'QR' AND payment_confirmed_at IS NOT NULL)
                OR
                -- COD: CHỈ khi đã giao (status = 4 = HOAN_THANH)
                (payment_method = 'COD' AND trang_thai = 4 AND payment_confirmed_at IS NOT NULL)
            )
            AND ngay_tao >= ?1
            AND ngay_tao <= ?2
            AND trang_thai != 2  -- Loại trừ đơn hủy
        """);
        query.setParameter(1, startDate);
        query.setParameter(2, endDate);
        return safeBigDecimal(query.getSingleResult());
    }
    
    /**
     * ===== LOGIC DOANH THU KỲ VỌNG (ĐANG CHỜ) =====
     * Tiền đang chờ xác nhận:
     * - QR pending (payment_confirmed_at IS NULL)
     * - COD đang giao (status = 3 = DANG_GIAO)
     */
    public BigDecimal tinhDoanhThuKyVong(Instant startDate, Instant endDate) {
        Query query = entityManager.createNativeQuery("""
            SELECT COALESCE(SUM(tong_tien_sau_giam), 0)
            FROM hoa_don
            WHERE (
                -- QR chưa confirm
                (payment_method = 'QR' AND payment_confirmed_at IS NULL AND trang_thai != 2)
                OR
                -- COD đang giao
                (payment_method = 'COD' AND trang_thai = 3)
            )
            AND ngay_tao >= ?1
            AND ngay_tao <= ?2
        """);
        query.setParameter(1, startDate);
        query.setParameter(2, endDate);
        return safeBigDecimal(query.getSingleResult());
    }
    
    /**
     * Tiền mặt tại quầy (CASH confirmed)
     */
    public BigDecimal tinhTienMatTaiQuay(Instant startDate, Instant endDate) {
        Query query = entityManager.createNativeQuery("""
            SELECT COALESCE(SUM(tong_tien_sau_giam), 0)
            FROM hoa_don
            WHERE payment_method = 'CASH'
              AND payment_confirmed_at IS NOT NULL
              AND ngay_tao >= ?1
              AND ngay_tao <= ?2
              AND trang_thai != 2
        """);
        query.setParameter(1, startDate);
        query.setParameter(2, endDate);
        return safeBigDecimal(query.getSingleResult());
    }
    
    /**
     * QR đã xác nhận (có payment_confirmed_at)
     */
    public BigDecimal tinhQRDaXacNhan(Instant startDate, Instant endDate) {
        Query query = entityManager.createNativeQuery("""
            SELECT COALESCE(SUM(tong_tien_sau_giam), 0)
            FROM hoa_don
            WHERE payment_method = 'QR'
              AND payment_confirmed_at IS NOT NULL
              AND ngay_tao >= ?1
              AND ngay_tao <= ?2
              AND trang_thai != 2
        """);
        query.setParameter(1, startDate);
        query.setParameter(2, endDate);
        return safeBigDecimal(query.getSingleResult());
    }
    
    /**
     * COD đã giao (status = 4)
     */
    public BigDecimal tinhCODDaGiao(Instant startDate, Instant endDate) {
        Query query = entityManager.createNativeQuery("""
            SELECT COALESCE(SUM(tong_tien_sau_giam), 0)
            FROM hoa_don
            WHERE payment_method = 'COD'
              AND trang_thai = 4
              AND payment_confirmed_at IS NOT NULL
              AND ngay_tao >= ?1
              AND ngay_tao <= ?2
        """);
        query.setParameter(1, startDate);
        query.setParameter(2, endDate);
        return safeBigDecimal(query.getSingleResult());
    }
    
    /**
     * QR chưa xác nhận
     */
    public BigDecimal tinhQRChuaXacNhan(Instant startDate, Instant endDate) {
        Query query = entityManager.createNativeQuery("""
            SELECT COALESCE(SUM(tong_tien_sau_giam), 0)
            FROM hoa_don
            WHERE payment_method = 'QR'
              AND payment_confirmed_at IS NULL
              AND trang_thai != 2
              AND ngay_tao >= ?1
              AND ngay_tao <= ?2
        """);
        query.setParameter(1, startDate);
        query.setParameter(2, endDate);
        return safeBigDecimal(query.getSingleResult());
    }
    
    /**
     * COD đang giao (status = 3)
     */
    public BigDecimal tinhCODDangGiao(Instant startDate, Instant endDate) {
        Query query = entityManager.createNativeQuery("""
            SELECT COALESCE(SUM(tong_tien_sau_giam), 0)
            FROM hoa_don
            WHERE payment_method = 'COD'
              AND trang_thai = 3
              AND ngay_tao >= ?1
              AND ngay_tao <= ?2
        """);
        query.setParameter(1, startDate);
        query.setParameter(2, endDate);
        return safeBigDecimal(query.getSingleResult());
    }
    
    /**
     * Doanh thu theo kênh (POS/ONLINE) - CHỈ tính đã confirm
     */
    public BigDecimal tinhDoanhThuTheoKenh(SalesChannel channel, Instant startDate, Instant endDate) {
        Query query = entityManager.createNativeQuery("""
            SELECT COALESCE(SUM(tong_tien_sau_giam), 0)
            FROM hoa_don
            WHERE sales_channel = ?1
              AND (
                  (payment_method = 'CASH' AND payment_confirmed_at IS NOT NULL)
                  OR (payment_method = 'QR' AND payment_confirmed_at IS NOT NULL)
                  OR (payment_method = 'COD' AND trang_thai = 4 AND payment_confirmed_at IS NOT NULL)
              )
              AND ngay_tao >= ?2
              AND ngay_tao <= ?3
              AND trang_thai != 2
        """);
        query.setParameter(1, channel.name());
        query.setParameter(2, startDate);
        query.setParameter(3, endDate);
        return safeBigDecimal(query.getSingleResult());
    }
    
    /**
     * Doanh thu theo phương thức - đã xác nhận
     */
    public BigDecimal tinhDoanhThuTheoPhuongThuc(PaymentMethod method, Instant startDate, Instant endDate) {
        String condition = switch (method) {
            case CASH -> "payment_method = 'CASH' AND payment_confirmed_at IS NOT NULL";
            case QR -> "payment_method = 'QR' AND payment_confirmed_at IS NOT NULL";
            case COD -> "payment_method = 'COD' AND trang_thai = 4 AND payment_confirmed_at IS NOT NULL";
        };
        
        Query query = entityManager.createNativeQuery(String.format("""
            SELECT COALESCE(SUM(tong_tien_sau_giam), 0)
            FROM hoa_don
            WHERE %s
              AND ngay_tao >= ?1
              AND ngay_tao <= ?2
              AND trang_thai != 2
        """, condition));
        query.setParameter(1, startDate);
        query.setParameter(2, endDate);
        return safeBigDecimal(query.getSingleResult());
    }
    
    /**
     * Số đơn thành công (đã confirm thanh toán)
     */
    public Long demSoDonThanhCong(Instant startDate, Instant endDate) {
        Query query = entityManager.createNativeQuery("""
            SELECT COUNT(*)
            FROM hoa_don
            WHERE (
                (payment_method = 'CASH' AND payment_confirmed_at IS NOT NULL)
                OR (payment_method = 'QR' AND payment_confirmed_at IS NOT NULL)
                OR (payment_method = 'COD' AND trang_thai = 4 AND payment_confirmed_at IS NOT NULL)
            )
            AND ngay_tao >= ?1
            AND ngay_tao <= ?2
            AND trang_thai != 2
        """);
        query.setParameter(1, startDate);
        query.setParameter(2, endDate);
        return ((Number) query.getSingleResult()).longValue();
    }
    
    /**
     * Số đơn đã hủy
     */
    public Long demSoDonDaHuy(Instant startDate, Instant endDate) {
        Query query = entityManager.createNativeQuery("""
            SELECT COUNT(*)
            FROM hoa_don
            WHERE trang_thai = 2
              AND ngay_tao >= ?1
              AND ngay_tao <= ?2
        """);
        query.setParameter(1, startDate);
        query.setParameter(2, endDate);
        return ((Number) query.getSingleResult()).longValue();
    }
    
    /**
     * Số đơn đang xử lý (chưa hoàn thành)
     */
    public Long demSoDonDangXuLy(Instant startDate, Instant endDate) {
        Query query = entityManager.createNativeQuery("""
            SELECT COUNT(*)
            FROM hoa_don
            WHERE trang_thai IN (0, 1, 3)  -- CHO_THANH_TOAN, DA_THANH_TOAN, DANG_GIAO
              AND ngay_tao >= ?1
              AND ngay_tao <= ?2
        """);
        query.setParameter(1, startDate);
        query.setParameter(2, endDate);
        return ((Number) query.getSingleResult()).longValue();
    }

    /**
     * Số đơn QR đang chờ xác nhận
     */
    public Long demSoDonQRChoXacNhan(Instant startDate, Instant endDate) {
        Query query = entityManager.createNativeQuery("""
            SELECT COUNT(*)
            FROM hoa_don
            WHERE payment_method = 'QR'
              AND payment_confirmed_at IS NULL
              AND trang_thai != 2
              AND ngay_tao >= ?1
              AND ngay_tao <= ?2
        """);
        query.setParameter(1, startDate);
        query.setParameter(2, endDate);
        return ((Number) query.getSingleResult()).longValue();
    }

    /**
     * Số đơn COD đang giao
     */
    public Long demSoDonCODDangGiao(Instant startDate, Instant endDate) {
        Query query = entityManager.createNativeQuery("""
            SELECT COUNT(*)
            FROM hoa_don
            WHERE payment_method = 'COD'
              AND trang_thai = 3 -- DANG_GIAO
              AND ngay_tao >= ?1
              AND ngay_tao <= ?2
        """);
        query.setParameter(1, startDate);
        query.setParameter(2, endDate);
        return ((Number) query.getSingleResult()).longValue();
    }
    
    /**
     * Lấy doanh thu chi tiết đầy đủ
     */
    public DoanhThuChiTietDTO layDoanhThuChiTiet(LocalDate startDate, LocalDate endDate) {
        Instant start = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant end = endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
        
        DoanhThuChiTietDTO dto = new DoanhThuChiTietDTO();
        
        // Doanh thu thực
        dto.setTienMatTaiQuay(tinhTienMatTaiQuay(start, end));
        dto.setQrDaXacNhan(tinhQRDaXacNhan(start, end));
        dto.setCodDaGiao(tinhCODDaGiao(start, end));
        dto.setDoanhThuThuc(dto.getTienMatTaiQuay()
            .add(dto.getQrDaXacNhan())
            .add(dto.getCodDaGiao()));
        
        // Doanh thu kỳ vọng


        dto.setQrChuaXacNhan(tinhQRChuaXacNhan(start, end));
        dto.setCodDangGiao(tinhCODDangGiao(start, end));
        dto.setDoanhThuKyVong(dto.getQrChuaXacNhan().add(dto.getCodDangGiao()));
        
        // Theo kênh
        dto.setDoanhThuPOS(tinhDoanhThuTheoKenh(SalesChannel.POS, start, end));
        dto.setDoanhThuOnline(tinhDoanhThuTheoKenh(SalesChannel.ONLINE, start, end));
        
        // Theo phương thức
        dto.setDoanhThuCash(tinhDoanhThuTheoPhuongThuc(PaymentMethod.CASH, start, end));
        dto.setDoanhThuQR(tinhDoanhThuTheoPhuongThuc(PaymentMethod.QR, start, end));
        dto.setDoanhThuCOD(tinhDoanhThuTheoPhuongThuc(PaymentMethod.COD, start, end));
        
        // Số lượng đơn
        dto.setSoDonThanhCong(demSoDonThanhCong(start, end));
        dto.setSoDonDaHuy(demSoDonDaHuy(start, end));
        dto.setSoDonDangXuLy(demSoDonDangXuLy(start, end));
        dto.setSoDonQRChoXacNhan(demSoDonQRChoXacNhan(start, end));
        dto.setSoDonCODDangGiao(demSoDonCODDangGiao(start, end));
        
        // Tỷ lệ hủy
        long tongDon = dto.getSoDonThanhCong() + dto.getSoDonDaHuy() + dto.getSoDonDangXuLy();
        if (tongDon > 0) {
            dto.setTyLeHuyDon((dto.getSoDonDaHuy() * 100.0) / tongDon);
        } else {
            dto.setTyLeHuyDon(0.0);
        }
        
        return dto;
    }
    
    /**
     * Thống kê theo kênh
     */
    public List<ThongKeTheoKenhDTO> thongKeTheoKenh(LocalDate startDate, LocalDate endDate) {
        Instant start = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant end = endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
        
        List<ThongKeTheoKenhDTO> result = new ArrayList<>();
        
        for (SalesChannel channel : SalesChannel.values()) {
            ThongKeTheoKenhDTO dto = new ThongKeTheoKenhDTO();
            dto.setTenKenh(channel.name());
            dto.setDoanhThu(tinhDoanhThuTheoKenh(channel, start, end));
            
            // Đếm số đơn theo kênh
            Query query = entityManager.createNativeQuery("""
                SELECT 
                    COUNT(*) as tong,
                    COALESCE(SUM(CASE WHEN trang_thai = 2 THEN 1 ELSE 0 END), 0) as huy,
                    COALESCE(SUM(CASE WHEN payment_confirmed_at IS NOT NULL AND trang_thai != 2 THEN 1 ELSE 0 END), 0) as thanh_cong
                FROM hoa_don
                WHERE sales_channel = ?1
                  AND ngay_tao >= ?2
                  AND ngay_tao <= ?3
            """);
            query.setParameter(1, channel.name());
            query.setParameter(2, start);
            query.setParameter(3, end);
            
            Object[] counts = (Object[]) query.getSingleResult();
            Long tongDon = ((Number) counts[0]).longValue();
            Long donHuy = ((Number) counts[1]).longValue();
            Long donThanhCong = ((Number) counts[2]).longValue();
            
            dto.setSoDon(tongDon);
            dto.setSoDonHuy(donHuy);
            dto.setSoDonThanhCong(donThanhCong);
            dto.setTyLeHuy(tongDon > 0 ? (donHuy * 100.0) / tongDon : 0.0);
            
            if (donThanhCong > 0) {
                dto.setGiaTriTrungBinh(dto.getDoanhThu().divide(
                    BigDecimal.valueOf(donThanhCong), 2, RoundingMode.HALF_UP));
            } else {
                dto.setGiaTriTrungBinh(BigDecimal.ZERO);
            }
            
            result.add(dto);
        }
        
        return result;
    }
    
    /**
     * Thống kê theo phương thức thanh toán
     */
    public List<ThongKeTheoPhuongThucDTO> thongKeTheoPhuongThuc(LocalDate startDate, LocalDate endDate) {
        Instant start = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant end = endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
        
        List<ThongKeTheoPhuongThucDTO> result = new ArrayList<>();
        BigDecimal tongDoanhThu = tinhDoanhThuThuc(start, end);
        
        for (PaymentMethod method : PaymentMethod.values()) {
            ThongKeTheoPhuongThucDTO dto = new ThongKeTheoPhuongThucDTO();
            dto.setPhuongThuc(method.name());
            
            BigDecimal doanhThuDaXacNhan = tinhDoanhThuTheoPhuongThuc(method, start, end);
            dto.setDoanhThuDaXacNhan(doanhThuDaXacNhan);
            
            // Tính doanh thu chưa xác nhận
            BigDecimal chuaXacNhan = BigDecimal.ZERO;
            if (method == PaymentMethod.QR) {
                chuaXacNhan = tinhQRChuaXacNhan(start, end);
            } else if (method == PaymentMethod.COD) {
                chuaXacNhan = tinhCODDangGiao(start, end);
            }
            dto.setDoanhThuChuaXacNhan(chuaXacNhan);
            dto.setDoanhThu(doanhThuDaXacNhan.add(chuaXacNhan));
            
            // Đếm số đơn
            Query query = entityManager.createNativeQuery("""
                SELECT COUNT(*)
                FROM hoa_don
                WHERE payment_method = ?1
                  AND ngay_tao >= ?2
                  AND ngay_tao <= ?3
                  AND trang_thai != 2
            """);
            query.setParameter(1, method.name());
            query.setParameter(2, start);
            query.setParameter(3, end);
            dto.setSoDon(((Number) query.getSingleResult()).longValue());
            
            // Tỷ lệ sử dụng
            if (tongDoanhThu.compareTo(BigDecimal.ZERO) > 0) {
                dto.setTyLeSuDung(doanhThuDaXacNhan
                    .multiply(BigDecimal.valueOf(100))
                    .divide(tongDoanhThu, 2, RoundingMode.HALF_UP)
                    .doubleValue());
            } else {
                dto.setTyLeSuDung(0.0);
            }
            
            result.add(dto);
        }
        
        return result;
    }
    
    /**
     * Phân tích dòng tiền
     */
    public DongTienDTO phanTichDongTien(LocalDate startDate, LocalDate endDate) {
        Instant start = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant end = endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
        
        DongTienDTO dto = new DongTienDTO();
        
        // Tiền đã về
        dto.setTienMatVe(tinhTienMatTaiQuay(start, end));
        dto.setQrDaVe(tinhQRDaXacNhan(start, end));
        dto.setCodDaThu(tinhCODDaGiao(start, end));
        dto.setTongTienDaVe(dto.getTienMatVe()
            .add(dto.getQrDaVe())
            .add(dto.getCodDaThu()));
        
        // Tiền đang chờ
        dto.setQrDangCho(tinhQRChuaXacNhan(start, end));
        dto.setCodDangGiao(tinhCODDangGiao(start, end));
        dto.setTongTienDangCho(dto.getQrDangCho().add(dto.getCodDangGiao()));
        
        // Tiền mất do hủy
        Query queryHuy = entityManager.createNativeQuery("""
            SELECT COALESCE(SUM(tong_tien_sau_giam), 0)
            FROM hoa_don
            WHERE trang_thai = 2
              AND ngay_tao >= ?1
              AND ngay_tao <= ?2
        """);
        queryHuy.setParameter(1, start);
        queryHuy.setParameter(2, end);
        dto.setTienMatDoHuy(safeBigDecimal(queryHuy.getSingleResult()));
        
        // Tiền hoàn trả (tính từ payment_logs nếu có)
        // TODO: Implement refund tracking
        dto.setTienHoanTra(BigDecimal.ZERO);
        
        return dto;
    }
}

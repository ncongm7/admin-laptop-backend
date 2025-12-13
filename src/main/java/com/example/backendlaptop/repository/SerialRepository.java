package com.example.backendlaptop.repository;

import com.example.backendlaptop.entity.Serial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SerialRepository extends JpaRepository<Serial, UUID> {

       List<Serial> findByCtspId(UUID ctspId);

       boolean existsBySerialNo(String serialNo);

       @Query("SELECT COUNT(s) FROM Serial s WHERE s.ctsp.id = :ctspId AND s.trangThai = :trangThai")
       int countByCtspIdAndTrangThai(@Param("ctspId") UUID ctspId, @Param("trangThai") Integer trangThai);

       @Query("SELECT s FROM Serial s WHERE s.ctsp.id = :ctspId AND s.trangThai = :trangThai")
       List<Serial> findByCtspIdAndTrangThai(@Param("ctspId") UUID ctspId, @Param("trangThai") Integer trangThai);

       @Query("SELECT s FROM Serial s WHERE s.serialNo LIKE %:keyword% OR s.ctsp.maCtsp LIKE %:keyword%")
       List<Serial> findByKeyword(@Param("keyword") String keyword);

       /**
        * Tìm serial theo số serial
        */
       Optional<Serial> findBySerialNo(String serialNo);

       /**
        * Tìm serial theo số serial và ID chi tiết sản phẩm
        */
       @Query("SELECT s FROM Serial s WHERE s.serialNo = :serialNo AND s.ctsp.id = :ctspId")
       Optional<Serial> findBySerialNoAndCtspId(@Param("serialNo") String serialNo, @Param("ctspId") UUID ctspId);

       // Tìm serial có thể bán (chưa bán VÀ chưa bị giữ bởi đơn khác)
       @Query("SELECT s FROM Serial s WHERE s.ctsp.id = :ctspId AND s.trangThai = 1 AND s.reservedInOrder IS NULL")
       List<Serial> findAvailableAndNotReserved(@Param("ctspId") UUID ctspId);

       // Tìm serial đang bị giữ bởi đơn Online COD (có thể "cướp" nếu là POS Cash)
       // Dieu kien: TrangThai=1 (chua ban thuc su), Reserved boi Don Online (Loai=1),
       // Chua thanh toan (TTTT=0)
       // Tìm serial đang bị giữ bởi đơn Online COD (có thể "cướp" nếu là POS Cash)
       @Query("SELECT s FROM Serial s WHERE s.ctsp.id = :ctspId AND s.trangThai = 1 AND s.reservedInOrder IS NOT NULL "
                     +
                     "AND s.reservedInOrder.loaiHoaDon = 1 AND s.reservedInOrder.trangThaiThanhToan = 0")
       List<Serial> findStealableSerials(@Param("ctspId") UUID ctspId);

       // Tìm các đơn hàng có serial hết hạn giữ
       @Query("SELECT DISTINCT s.reservedInOrder FROM Serial s WHERE s.reservedExpiredAt < :now")
       List<com.example.backendlaptop.entity.HoaDon> findOrdersWithExpiredReservations(
                     @Param("now") java.time.Instant now);

       // Tìm serial đã mua của khách hàng (theo KhachHang ID)
       // Query thông qua HoaDonChiTiet: Tìm các CTSP đã được mua trong đơn hàng của
       // khách hàng
       // Sau đó tìm các serial thuộc CTSP đó và đã được bán (trangThai = 2)
       @Query("SELECT s FROM Serial s " +
                     "JOIN SerialDaBan sdb ON s.id = sdb.idSerial.id " +
                     "JOIN sdb.idHoaDonChiTiet hdct " +
                     "JOIN hdct.hoaDon hd " +
                     "WHERE hd.idKhachHang.id = :userId " +
                     "AND hd.trangThai IN (1, 3, 4)") // DA_THANH_TOAN, DANG_GIAO, HOAN_THANH
       List<Serial> findByUserId(@Param("userId") UUID userId);
}

package com.example.backendlaptop.repository;

import com.example.backendlaptop.entity.DotGiamGiaChiTiet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface DotGiamGiaChiTietRepository extends JpaRepository<DotGiamGiaChiTiet, UUID> {
    List<DotGiamGiaChiTiet> findAllByDotGiamGia_Id(UUID dotGiamGiaId);
    @Query("select d.idCtsp.id from DotGiamGiaChiTiet d where d.dotGiamGia.id = :dotId") // Note: idCtsp is the entity, not the ID. Corrected in logic.
    Set<UUID> findCtspIdsByDotId(@Param("dotId") UUID dotId);

    List<DotGiamGiaChiTiet> findByDotGiamGia_Id(UUID dotGiamGiaId);


    // DotGiamGiaChiTietRepository.java
    @Modifying
    @jakarta.transaction.Transactional
    @Query(value = """
    UPDATE dot_giam_gia_chi_tiet
    SET gia_sau_khi_giam =
        CASE
            WHEN (gia_ban_dau - :newGiaTri) <= 0 THEN 0
            ELSE FLOOR( (gia_ban_dau - :newGiaTri) / 1000.0 ) * 1000
        END
    WHERE id_km = :dotId
    """, nativeQuery = true)
    int bulkRepriceByDot(@Param("dotId") UUID dotId,
                         @Param("newGiaTri") java.math.BigDecimal newGiaTri);

    @Query("SELECT d.idCtsp.id FROM DotGiamGiaChiTiet d WHERE d.dotGiamGia.id = :idDotGiamGia")
    Page<UUID> findIdCtspByIdDotGiamGia(@Param("idDotGiamGia") UUID idDotGiamGia, Pageable pageable);

    /**
     * Tìm đợt giảm giá đang hoạt động cho một sản phẩm chi tiết cụ thể.
     * Một đợt giảm giá được coi là hoạt động nếu:
     * 1. Trạng thái của đợt giảm giá là 1 (đang chạy).
     * 2. Thời gian hiện tại nằm trong khoảng từ ngày bắt đầu đến ngày kết thúc.
     */
    // ================================= LOGIC MỚI: TÌM KHUYẾN MÃI ĐANG HOẠT ĐỘNG =================================
    @Query("SELECT dgg FROM DotGiamGia dgg JOIN DotGiamGiaChiTiet dggct ON dgg.id = dggct.dotGiamGia.id " +
           "WHERE dggct.idCtsp.id = :chiTietSanPhamId AND dgg.trangThai = 1 " +
           "AND dgg.ngayBatDau <= :currentTime AND dgg.ngayKetThuc >= :currentTime")
    java.util.List<com.example.backendlaptop.entity.DotGiamGia> findActiveDiscountsForProduct(@Param("chiTietSanPhamId") UUID chiTietSanPhamId, @Param("currentTime") Instant currentTime);
}

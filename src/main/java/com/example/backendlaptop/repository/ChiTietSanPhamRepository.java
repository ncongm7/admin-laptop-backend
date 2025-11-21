package com.example.backendlaptop.repository;

import com.example.backendlaptop.entity.ChiTietSanPham;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChiTietSanPhamRepository extends JpaRepository<ChiTietSanPham, UUID> {

    List<ChiTietSanPham> findBySanPham_Id(UUID sanPhamId);

    @Query("""
        SELECT c FROM ChiTietSanPham c
        LEFT JOIN c.cpu cpu
        LEFT JOIN c.gpu gpu
        LEFT JOIN c.ram ram
        LEFT JOIN c.oCung oCung
        LEFT JOIN c.mauSac mauSac
        LEFT JOIN c.loaiManHinh loaiManHinh
        LEFT JOIN c.pin pin
        LEFT JOIN c.sanPham sp
        WHERE (:keyword IS NULL OR :keyword = '' OR 
               LOWER(c.maCtsp) LIKE LOWER(CONCAT('%',:keyword,'%')) OR 
               LOWER(sp.tenSanPham) LIKE LOWER(CONCAT('%',:keyword,'%')) OR
               LOWER(cpu.tenCpu) LIKE LOWER(CONCAT('%',:keyword,'%')) OR
               LOWER(gpu.tenGpu) LIKE LOWER(CONCAT('%',:keyword,'%')) OR
               LOWER(ram.tenRam) LIKE LOWER(CONCAT('%',:keyword,'%')) OR
               LOWER(mauSac.tenMau) LIKE LOWER(CONCAT('%',:keyword,'%')) OR
               LOWER(oCung.dungLuong) LIKE LOWER(CONCAT('%',:keyword,'%')) OR
               LOWER(loaiManHinh.kichThuoc) LIKE LOWER(CONCAT('%',:keyword,'%')))
          AND (:cpu IS NULL OR :cpu = '' OR LOWER(cpu.tenCpu) LIKE LOWER(CONCAT('%',:cpu,'%')))
          AND (:gpu IS NULL OR :gpu = '' OR LOWER(gpu.tenGpu) LIKE LOWER(CONCAT('%',:gpu,'%')))
          AND (:ram IS NULL OR :ram = '' OR LOWER(ram.tenRam) LIKE LOWER(CONCAT('%',:ram,'%')))
          AND (:color IS NULL OR :color = '' OR LOWER(mauSac.tenMau) LIKE LOWER(CONCAT('%',:color,'%')))
          AND (:storage IS NULL OR :storage = '' OR LOWER(oCung.dungLuong) LIKE LOWER(CONCAT('%',:storage,'%')))
          AND (:screen IS NULL OR :screen = '' OR LOWER(loaiManHinh.kichThuoc) LIKE LOWER(CONCAT('%',:screen,'%')))
          AND (:minPrice IS NULL OR c.giaBan >= :minPrice)
          AND (:maxPrice IS NULL OR c.giaBan <= :maxPrice)
        """)
    Page<ChiTietSanPham> search(
            @Param("keyword") String keyword,
            @Param("cpu") String cpu,
            @Param("gpu") String gpu,
            @Param("ram") String ram,
            @Param("color") String color,
            @Param("storage") String storage,
            @Param("screen") String screen,
            @Param("minPrice") java.math.BigDecimal minPrice,
            @Param("maxPrice") java.math.BigDecimal maxPrice,
            Pageable pageable);


    boolean existsByMaCtsp(String maCtsp);

    // code của long
    @Query("SELECT ctsp FROM ChiTietSanPham ctsp "
            + "WHERE ctsp.sanPham.id = :sanPhamId " // Lọc theo Sản Phẩm (từ combobox)
            + "AND ctsp.id NOT IN ("
            + "    SELECT dggct.idCtsp.id FROM DotGiamGiaChiTiet dggct"
            + "    WHERE dggct.dotGiamGia.id = :dotGiamGiaId" // Lọc các CTSP đã tham gia DGGD hiện tại
            + ")")
    List<ChiTietSanPham> findAvailableProductsBySanPhamId(
            @Param("dotGiamGiaId") UUID dotGiamGiaId,
            @Param("sanPhamId") UUID sanPhamId
    );

    // Tìm sản phẩm bán chạy nhất dựa trên số lượng bán từ hoa_don_chi_tiet
    @Query(value = """
        SELECT TOP (:limit) ctsp.sp_id, SUM(hdct.so_luong) as total_sold
        FROM chi_tiet_san_pham ctsp
        INNER JOIN hoa_don_chi_tiet hdct ON ctsp.id = hdct.id_ctsp
        INNER JOIN hoa_don hd ON hdct.id_don_hang = hd.id
        WHERE hd.trang_thai = 2 AND hd.trang_thai_thanh_toan = 1
        GROUP BY ctsp.sp_id
        ORDER BY total_sold DESC
        """, nativeQuery = true)
    List<Object[]> findBestSellingProducts(@Param("limit") Integer limit);
}

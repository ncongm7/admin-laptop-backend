package com.example.backendlaptop.repository;

import com.example.backendlaptop.entity.ChiTietSanPham;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChiTietSanPhamRepository extends JpaRepository<ChiTietSanPham, UUID> {
    
    List<ChiTietSanPham> findBySanPham_Id(UUID sanPhamId);
    
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
}

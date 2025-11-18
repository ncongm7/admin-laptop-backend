package com.example.backendlaptop.repository;

import com.example.backendlaptop.entity.DanhMuc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DanhMucRepository extends JpaRepository<DanhMuc, UUID> {

    @Query(value = """
            SELECT
                dm.id AS id,
                dm.ten_danh_muc AS name,
                dm.slug AS slug,
                dm.icon_url AS iconUrl,
                COUNT(spdm.id_san_pham) AS productCount
            FROM danh_muc dm
            LEFT JOIN sanpham_danhmuc spdm ON dm.id = spdm.id_danh_muc
            WHERE dm.trang_thai = 1
            GROUP BY dm.id, dm.ten_danh_muc, dm.slug, dm.icon_url
            """, nativeQuery = true)
    List<Object[]> findCategoriesWithProductCount();
}

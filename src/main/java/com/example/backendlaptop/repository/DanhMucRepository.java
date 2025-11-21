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
                ISNULL(dm.slug, '') AS slug,
                ISNULL(dm.icon_url, '') AS iconUrl,
                COUNT(spdm.san_pham_id) AS productCount
            FROM danh_muc dm
            LEFT JOIN sanpham_danhmuc spdm ON dm.id = spdm.danh_muc_id
            WHERE dm.trang_thai = 1
            GROUP BY dm.id, dm.ten_danh_muc, dm.slug, dm.icon_url
            """, nativeQuery = true)
    List<Object[]> findCategoriesWithProductCount();

    @Query(value = """
            SELECT
                dm.id AS id,
                dm.ten_danh_muc AS name,
                ISNULL(dm.slug, '') AS slug,
                ISNULL(dm.icon_url, '') AS iconUrl,
                COUNT(spdm.san_pham_id) AS productCount
            FROM danh_muc dm
            LEFT JOIN sanpham_danhmuc spdm ON dm.id = spdm.danh_muc_id
            WHERE dm.trang_thai = 1 AND (dm.featured = 1 OR dm.featured IS NULL)
            GROUP BY dm.id, dm.ten_danh_muc, dm.slug, dm.icon_url
            ORDER BY dm.ten_danh_muc
            """, nativeQuery = true)
    List<Object[]> findFeaturedCategoriesWithProductCount();
    
    // Fallback: Nếu không có featured, lấy top 6 categories
    @Query(value = """
            SELECT TOP 6
                dm.id AS id,
                dm.ten_danh_muc AS name,
                ISNULL(dm.slug, '') AS slug,
                ISNULL(dm.icon_url, '') AS iconUrl,
                COUNT(spdm.san_pham_id) AS productCount
            FROM danh_muc dm
            LEFT JOIN sanpham_danhmuc spdm ON dm.id = spdm.danh_muc_id
            WHERE dm.trang_thai = 1
            GROUP BY dm.id, dm.ten_danh_muc, dm.slug, dm.icon_url
            ORDER BY COUNT(spdm.san_pham_id) DESC
            """, nativeQuery = true)
    List<Object[]> findTopCategoriesWithProductCount();
}

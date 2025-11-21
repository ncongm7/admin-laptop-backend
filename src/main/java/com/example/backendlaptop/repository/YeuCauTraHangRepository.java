package com.example.backendlaptop.repository;

import com.example.backendlaptop.entity.YeuCauTraHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface YeuCauTraHangRepository extends JpaRepository<YeuCauTraHang, UUID> {
    
    /**
     * Tìm yêu cầu trả hàng theo hóa đơn
     */
    List<YeuCauTraHang> findByIdHoaDon_Id(UUID idHoaDon);
    
    /**
     * Tìm yêu cầu trả hàng theo khách hàng
     */
    List<YeuCauTraHang> findByIdKhachHang_Id(UUID idKhachHang);
    
    /**
     * Tìm yêu cầu trả hàng theo mã yêu cầu
     */
    Optional<YeuCauTraHang> findByMaYeuCau(String maYeuCau);
    
    /**
     * Kiểm tra xem hóa đơn đã có yêu cầu trả hàng chưa
     */
    @Query("SELECT COUNT(y) > 0 FROM YeuCauTraHang y WHERE y.idHoaDon.id = :idHoaDon AND y.trangThai IN (0, 1)")
    boolean existsByHoaDonIdAndTrangThaiPending(@Param("idHoaDon") UUID idHoaDon);
}


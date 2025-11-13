package com.example.backendlaptop.repository;

import com.example.backendlaptop.entity.SerialDaBan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SerialDaBanRepository extends JpaRepository<SerialDaBan, UUID> {
    
    /**
     * Tìm serial đã bán theo ID hóa đơn chi tiết
     */
    List<SerialDaBan> findByIdHoaDonChiTiet_Id(UUID idHoaDonChiTiet);
    
    /**
     * Tìm serial đã bán theo ID serial
     */
    Optional<SerialDaBan> findByIdSerial_Id(UUID idSerial);
    
    /**
     * Kiểm tra serial đã được bán chưa
     */
    @Query("SELECT CASE WHEN COUNT(sdb) > 0 THEN true ELSE false END FROM SerialDaBan sdb WHERE sdb.idSerial.id = :serialId")
    boolean existsBySerialId(@Param("serialId") UUID serialId);
}


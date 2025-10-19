package com.example.backendlaptop.repository;

import com.example.backendlaptop.entity.Serial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
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
}

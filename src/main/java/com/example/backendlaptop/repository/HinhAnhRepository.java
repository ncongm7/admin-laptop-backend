package com.example.backendlaptop.repository;

import com.example.backendlaptop.entity.HinhAnh;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HinhAnhRepository extends JpaRepository<HinhAnh, UUID> {
    
    List<HinhAnh> findByIdSpctId(UUID idSpct);
    
    @Query("SELECT h FROM HinhAnh h WHERE h.idSpct.id = :idSpct AND h.anhChinhDaiDien = true")
    Optional<HinhAnh> findMainImageByCtspId(@Param("idSpct") UUID idSpct);
    
    @Query("SELECT h FROM HinhAnh h WHERE h.idSpct.id = :idSpct AND h.anhChinhDaiDien = false")
    List<HinhAnh> findGalleryImagesByCtspId(@Param("idSpct") UUID idSpct);
    
    void deleteByIdSpctId(UUID idSpct);
}

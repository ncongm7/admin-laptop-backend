package com.example.backendlaptop.repository;

import com.example.backendlaptop.entity.OCung;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OCungRepository extends JpaRepository<OCung, UUID> {
    
    Optional<OCung> findByMaOCung(String maOCung);
    
    boolean existsByMaOCung(String maOCung);
    
    List<OCung> findByTrangThai(Integer trangThai);
}

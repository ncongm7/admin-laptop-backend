package com.example.backendlaptop.repository;

import com.example.backendlaptop.entity.Pin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PinRepository extends JpaRepository<Pin, UUID> {
    
    Optional<Pin> findByMaPin(String maPin);
    
    boolean existsByMaPin(String maPin);
    
    List<Pin> findByTrangThai(Integer trangThai);
}

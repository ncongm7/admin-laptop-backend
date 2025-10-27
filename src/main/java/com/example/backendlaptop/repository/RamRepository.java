package com.example.backendlaptop.repository;

import com.example.backendlaptop.entity.Ram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RamRepository extends JpaRepository<Ram, UUID> {
    
    Optional<Ram> findByMaRam(String maRam);
    
    boolean existsByMaRam(String maRam);
    
    List<Ram> findByTrangThai(Integer trangThai);
}

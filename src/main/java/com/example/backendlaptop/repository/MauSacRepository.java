package com.example.backendlaptop.repository;

import com.example.backendlaptop.entity.MauSac;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MauSacRepository extends JpaRepository<MauSac, UUID> {
    
    Optional<MauSac> findByMaMau(String maMau);
    
    boolean existsByMaMau(String maMau);
    
    List<MauSac> findByTrangThai(Integer trangThai);
}

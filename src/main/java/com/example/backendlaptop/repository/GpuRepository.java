package com.example.backendlaptop.repository;

import com.example.backendlaptop.entity.Gpu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GpuRepository extends JpaRepository<Gpu, UUID> {
    
    Optional<Gpu> findByMaGpu(String maGpu);
    
    boolean existsByMaGpu(String maGpu);
    
    List<Gpu> findByTrangThai(Integer trangThai);
}

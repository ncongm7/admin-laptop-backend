package com.example.backendlaptop.repository;

import com.example.backendlaptop.entity.Cpu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CpuRepository extends JpaRepository<Cpu, UUID> {
    
    Optional<Cpu> findByMaCpu(String maCpu);
    
    boolean existsByMaCpu(String maCpu);
    
    List<Cpu> findByTrangThai(Integer trangThai);
}

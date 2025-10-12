package com.example.backendlaptop.repository;

import com.example.backendlaptop.entity.LichSuBaoHanh;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LichSuBaoHanhRepository extends JpaRepository<LichSuBaoHanh, UUID> {
    List<LichSuBaoHanh> findAllByIdBaoHanh_Id(UUID phieuBaoHanhId);

}

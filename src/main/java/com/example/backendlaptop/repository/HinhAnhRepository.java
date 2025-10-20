package com.example.backendlaptop.repository;

import com.example.backendlaptop.entity.HinhAnh;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface HinhAnhRepository extends JpaRepository<HinhAnh, UUID> {
    List<HinhAnh> findByIdSpct_Id(UUID idSpct);
    List<HinhAnh> findByAnhChinhDaiDienTrue();
}

package com.example.backendlaptop.repository;

import com.example.backendlaptop.entity.VaiTro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VaiTroRepository extends JpaRepository<VaiTro, UUID> {
    Optional<VaiTro> findByMaVaiTro(String maVaiTro);
}


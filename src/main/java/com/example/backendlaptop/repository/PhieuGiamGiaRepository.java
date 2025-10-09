package com.example.backendlaptop.repository;

import com.example.backendlaptop.entity.PhieuGiamGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
@Repository
public interface PhieuGiamGiaRepository extends JpaRepository<PhieuGiamGia, UUID> {

    Collection<Object> findByMa(String ma);
    List<PhieuGiamGia> findByMaContainingIgnoreCase(String keyword);
}

package com.example.backendlaptop.repository;

import com.example.backendlaptop.entity.PhieuGiamGia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface PhieuGiamGiaRepository extends JpaRepository<PhieuGiamGia, UUID> {
    boolean existsByMaIgnoreCase(String ma);
    Collection<Object> findByMa(String ma);

    Optional<PhieuGiamGia> findByMaIgnoreCase(String ma);

    Page<PhieuGiamGia> findByTrangThaiAndRiengTu(Integer trangThai, Boolean riengTu, Pageable pageable);
}

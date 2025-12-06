package com.example.backendlaptop.repository;

import com.example.backendlaptop.entity.PhieuHenBaoHanh;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PhieuHenBaoHanhRepository extends JpaRepository<PhieuHenBaoHanh, UUID> {
    Optional<PhieuHenBaoHanh> findByMaPhieuHen(String maPhieuHen);

    List<PhieuHenBaoHanh> findByIdBaoHanh_IdOrderByNgayHenDesc(UUID idBaoHanh);

    List<PhieuHenBaoHanh> findByTrangThai(Integer trangThai);

    @Query("SELECT p FROM PhieuHenBaoHanh p WHERE p.ngayHen >= :fromDate AND p.ngayHen <= :toDate")
    List<PhieuHenBaoHanh> findByNgayHenBetween(@Param("fromDate") Instant fromDate, @Param("toDate") Instant toDate);
}


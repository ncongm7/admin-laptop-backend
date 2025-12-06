package com.example.backendlaptop.repository;

import com.example.backendlaptop.entity.EmailLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface EmailLogRepository extends JpaRepository<EmailLog, UUID> {
    List<EmailLog> findByIdBaoHanh_IdOrderByNgayGuiDesc(UUID idBaoHanh);

    List<EmailLog> findByLoaiEmailAndTrangThai(String loaiEmail, Integer trangThai);

    @Query("SELECT e FROM EmailLog e WHERE e.ngayGui >= :fromDate AND e.ngayGui <= :toDate")
    List<EmailLog> findByNgayGuiBetween(@Param("fromDate") Instant fromDate, @Param("toDate") Instant toDate);

    @Query("SELECT COUNT(e) FROM EmailLog e WHERE e.idBaoHanh.id = :idBaoHanh AND e.loaiEmail = :loaiEmail AND e.trangThai = 1")
    Long countSuccessfulEmailsByBaoHanhAndType(@Param("idBaoHanh") UUID idBaoHanh, @Param("loaiEmail") String loaiEmail);
}


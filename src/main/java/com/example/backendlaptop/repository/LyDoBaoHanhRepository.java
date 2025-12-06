package com.example.backendlaptop.repository;

import com.example.backendlaptop.entity.LyDoBaoHanh;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LyDoBaoHanhRepository extends JpaRepository<LyDoBaoHanh, UUID> {
    List<LyDoBaoHanh> findByIsActiveTrueOrderByThuTuAsc();

    List<LyDoBaoHanh> findByLoaiLyDoAndIsActiveTrueOrderByThuTuAsc(String loaiLyDo);

    Optional<LyDoBaoHanh> findByMaLyDo(String maLyDo);

    @Query("SELECT DISTINCT l.loaiLyDo FROM LyDoBaoHanh l WHERE l.isActive = true ORDER BY l.loaiLyDo")
    List<String> findDistinctLoaiLyDo();
}


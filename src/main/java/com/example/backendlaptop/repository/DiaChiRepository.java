package com.example.backendlaptop.repository;

import com.example.backendlaptop.dto.diaChi.DiaChiDto;
import com.example.backendlaptop.entity.DiaChi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DiaChiRepository extends JpaRepository<DiaChi, UUID> {

    @Query("select new com.example.backendlaptop.dto.diaChi.DiaChiDto" +
            "(dc.id, dc.user.maKhachHang, dc.hoTen, dc.sdt, dc.diaChi, dc.xa, dc.tinh, dc.macDinh) from DiaChi dc ")
    List<DiaChiDto> lstDiaChi();

    @Query("select new com.example.backendlaptop.dto.diaChi.DiaChiDto" +
            "(dc.id, dc.user.maKhachHang, dc.hoTen, dc.sdt, dc.diaChi, dc.xa, dc.tinh, dc.macDinh) " +
            "from DiaChi dc where dc.user.maKhachHang = :maKhachHang")
    List<DiaChiDto> findByMaKhachHang(@Param("maKhachHang") String maKhachHang);

    @Query("select dc from DiaChi dc where dc.user.maKhachHang = :maKhachHang and dc.macDinh = true")
    Optional<DiaChi> findMacDinhByMaKhachHang(@Param("maKhachHang") String maKhachHang);
}

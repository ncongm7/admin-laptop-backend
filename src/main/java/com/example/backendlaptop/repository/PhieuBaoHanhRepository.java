package com.example.backendlaptop.repository;

import com.example.backendlaptop.entity.PhieuBaoHanh;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PhieuBaoHanhRepository extends JpaRepository<PhieuBaoHanh, UUID> {
    
    @Query("SELECT DISTINCT pbh FROM PhieuBaoHanh pbh " +
           "LEFT JOIN FETCH pbh.idKhachHang " +
           "LEFT JOIN FETCH pbh.idSerialDaBan sdb " +
           "LEFT JOIN FETCH sdb.idSerial " +
           "LEFT JOIN FETCH sdb.idHoaDonChiTiet hdct " +
           "LEFT JOIN FETCH hdct.chiTietSanPham ctsp " +
           "LEFT JOIN FETCH ctsp.sanPham")
    List<PhieuBaoHanh> findAllWithRelations();
    
    @Query("SELECT DISTINCT pbh FROM PhieuBaoHanh pbh " +
           "LEFT JOIN FETCH pbh.idKhachHang " +
           "LEFT JOIN FETCH pbh.idSerialDaBan sdb " +
           "LEFT JOIN FETCH sdb.idSerial " +
           "LEFT JOIN FETCH sdb.idHoaDonChiTiet hdct " +
           "LEFT JOIN FETCH hdct.chiTietSanPham ctsp " +
           "LEFT JOIN FETCH ctsp.sanPham " +
           "WHERE pbh.id = :id")
    Optional<PhieuBaoHanh> findByIdWithRelations(UUID id);
}

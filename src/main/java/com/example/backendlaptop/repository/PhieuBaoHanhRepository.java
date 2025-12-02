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

    Optional<PhieuBaoHanh> findByIdSerialDaBan_Id(UUID idSerialDaBan);
    
    // Tìm các bảo hành theo idHoaDonChiTiet thông qua SerialDaBan
    @Query("SELECT pbh FROM PhieuBaoHanh pbh " +
           "JOIN pbh.idSerialDaBan sdb " +
           "WHERE sdb.idHoaDonChiTiet.id = :idHoaDonChiTiet " +
           "AND pbh.trangThaiBaoHanh != 3")
    List<PhieuBaoHanh> findByHoaDonChiTietAndNotCompleted(UUID idHoaDonChiTiet);
    
    // Tìm tất cả bảo hành theo hóa đơn thông qua SerialDaBan và HoaDonChiTiet
    @Query("SELECT DISTINCT pbh FROM PhieuBaoHanh pbh " +
           "JOIN pbh.idSerialDaBan sdb " +
           "JOIN sdb.idHoaDonChiTiet hdct " +
           "JOIN hdct.hoaDon hd " +
           "WHERE hd.id = :idHoaDon")
    List<PhieuBaoHanh> findByHoaDonId(UUID idHoaDon);
}

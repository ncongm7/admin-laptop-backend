package com.example.backendlaptop.repository;

import com.example.backendlaptop.dto.phanQuyenDto.khachHang.KhachHangDto;
import com.example.backendlaptop.entity.KhachHang;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface KhachHangRepository extends JpaRepository<KhachHang, UUID> {
    @Query("select new com.example.backendlaptop.dto.phanQuyenDto.khachHang.KhachHangDto" +
            "(kh.id, kh.maKhachHang, kh.hoTen, kh.soDienThoai, kh.email, kh.gioiTinh, kh.ngaySinh, kh.trangThai)" +
            "from KhachHang kh")
    List<KhachHangDto> findAllKhachHang();


    @Query("select new com.example.backendlaptop.dto.phanQuyenDto.khachHang.KhachHangDto" +
            "(kh.id, kh.maKhachHang, kh.hoTen, kh.soDienThoai, kh.email, kh.gioiTinh, kh.ngaySinh, kh.trangThai)" +
            "from KhachHang kh")
    Page<KhachHangDto> phanTrangKH(Pageable pageable);


    @Query("SELECT new com.example.backendlaptop.dto.phanQuyenDto.khachHang.KhachHangDto(" +
            "kh.id, kh.maKhachHang, kh.hoTen, kh.soDienThoai, kh.email, kh.gioiTinh, kh.ngaySinh, kh.trangThai) " +
            "FROM KhachHang kh " +
            "WHERE (:hoTen IS NULL OR kh.hoTen LIKE CONCAT('%', :hoTen, '%')) " +
            "OR (:soDienThoai IS NULL OR kh.soDienThoai LIKE CONCAT('%', :soDienThoai, '%'))")
    List<KhachHangDto> searchByMultiField(@Param("hoTen") String hoTen, @Param("soDienThoai") String soDienThoai);

    @Query(value = "SELECT TOP 1 ma_khach_hang FROM khach_hang " +
            "WHERE ma_khach_hang LIKE CONCAT('KH', YEAR(GETDATE()), '%') " +
            "ORDER BY ma_khach_hang DESC",
            nativeQuery = true)
    String findLastMaKhachHangOfYear();

    Optional<KhachHang> findByMaKhachHang(String maKhachHang);

    @Query("select new com.example.backendlaptop.dto.phanQuyenDto.khachHang.KhachHangDto" +
            "(kh.id, kh.maKhachHang, kh.hoTen, kh.soDienThoai, kh.email, kh.gioiTinh, kh.ngaySinh, kh.trangThai)" +
            "from KhachHang kh where kh.maKhachHang = :maKhachHang")
    KhachHangDto findByMaKhachHangDto(@Param("maKhachHang") String maKhachHang);

}

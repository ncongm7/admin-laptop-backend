package com.example.backendlaptop.repository.phanQuyenRe;

import com.example.backendlaptop.dto.phanQuyenDto.khachHang.KhachHangDto;
import com.example.backendlaptop.entity.phanQuyen.KhachHang;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface KhachHangRepository extends JpaRepository<KhachHang, UUID> {


    @Query("select new com.example.backendlaptop.dto.phanQuyenDto.khachHang.KhachHangDto" +
            "(kh.maKhachHang, kh.hoTen, kh.soDienThoai, kh.email, kh.gioiTinh, kh.ngaySinh, kh.trangThai)" +
            "from KhachHang kh")
    List<KhachHangDto> findAllKhachHang();


@Query("select new com.example.backendlaptop.dto.phanQuyenDto.khachHang.KhachHangDto" +
        "(kh.maKhachHang, kh.hoTen, kh.soDienThoai, kh.email, kh.gioiTinh, kh.ngaySinh, kh.trangThai)" +
        "from KhachHang kh")
    Page<KhachHangDto> phanTrangKH(Pageable pageable);
}

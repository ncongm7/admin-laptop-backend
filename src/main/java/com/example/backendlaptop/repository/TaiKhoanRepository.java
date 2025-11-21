package com.example.backendlaptop.repository;

import com.example.backendlaptop.dto.phanQuyenDto.TaiKhoan.TaiKhoanDto;
import com.example.backendlaptop.entity.TaiKhoan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaiKhoanRepository extends JpaRepository<TaiKhoan, UUID> {
    Optional<TaiKhoan> findByTenDangNhap(String tenDangNhap);
    
    @Query("SELECT tk FROM TaiKhoan tk LEFT JOIN FETCH tk.maVaiTro WHERE tk.tenDangNhap = :tenDangNhap")
    Optional<TaiKhoan> findByTenDangNhapWithRole(String tenDangNhap);

    @Query("select new com.example.backendlaptop.dto.phanQuyenDto.TaiKhoan.TaiKhoanDto" +
            "(tk.id, tk.maVaiTro.tenVaiTro, tk.tenDangNhap, tk.matKhau," +
            " tk.email, tk.trangThai, tk.ngayTao, tk.lanDangNhapCuoi) " +
            "from TaiKhoan tk")
    List<TaiKhoanDto> getAllTK();

    @Query("select new com.example.backendlaptop.dto.phanQuyenDto.TaiKhoan.TaiKhoanDto" +
            "(tk.id, tk.maVaiTro.tenVaiTro, tk.tenDangNhap, tk.matKhau," +
            " tk.email, tk.trangThai, tk.ngayTao, tk.lanDangNhapCuoi) " +
            "from TaiKhoan tk " +
            "where (:tenDangNhap IS NULL OR tk.tenDangNhap LIKE CONCAT('%', :tenDangNhap, '%')) " +
            "AND (:tenVaiTro IS NULL OR tk.maVaiTro.tenVaiTro LIKE CONCAT('%', :tenVaiTro, '%')) " +
            "AND (:trangThai IS NULL OR tk.trangThai = :trangThai)")
    List<TaiKhoanDto> searchTaiKhoan(
            @org.springframework.data.repository.query.Param("tenDangNhap") String tenDangNhap,
            @org.springframework.data.repository.query.Param("tenVaiTro") String tenVaiTro,
            @org.springframework.data.repository.query.Param("trangThai") Integer trangThai);
}

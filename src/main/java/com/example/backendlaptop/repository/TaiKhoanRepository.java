package com.example.backendlaptop.repository;

import com.example.backendlaptop.entity.TaiKhoan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaiKhoanRepository extends JpaRepository<TaiKhoan, UUID> {
    Optional<TaiKhoan> findByTenDangNhap(String tenDangNhap);
    
    @Query("SELECT tk FROM TaiKhoan tk LEFT JOIN FETCH tk.maVaiTro WHERE tk.tenDangNhap = :tenDangNhap")
    Optional<TaiKhoan> findByTenDangNhapWithRole(String tenDangNhap);
}

package com.example.backendlaptop.repository.phanQuyenRe;

import com.example.backendlaptop.entity.phanQuyen.TaiKhoan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.net.http.HttpResponse;
import java.util.UUID;

public interface TaiKhoanRepository extends JpaRepository<TaiKhoan, UUID> {
}

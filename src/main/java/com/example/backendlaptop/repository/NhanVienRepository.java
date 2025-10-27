package com.example.backendlaptop.repository;

import com.example.backendlaptop.dto.phanQuyenDto.nhanVien.NhanVienDto;
import com.example.backendlaptop.entity.NhanVien;
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
public interface NhanVienRepository extends JpaRepository<NhanVien, UUID> {
    @Query(" select new " +
            "com.example.backendlaptop.dto.phanQuyenDto.nhanVien." +
            "NhanVienDto(nv.maNhanVien, nv.hoTen, nv.soDienThoai, nv.email, nv.gioiTinh, nv.anhNhanVien, nv.chucVu, nv.diaChi, nv.danhGia, nv.trangThai) " +
            "from NhanVien nv")
    List<NhanVienDto> findNhanViensBy();

    @Query(" select new " +
            "com.example.backendlaptop.dto.phanQuyenDto.nhanVien." +
            "NhanVienDto(nv.maNhanVien, nv.hoTen, nv.soDienThoai, nv.email, nv.gioiTinh, nv.anhNhanVien, nv.chucVu, nv.diaChi, nv.danhGia, nv.trangThai) " +
            "from NhanVien nv")
    Page<NhanVienDto> phanTrangNV(Pageable pageable);
    
    @Query("SELECT nv FROM NhanVien nv WHERE nv.maTaiKhoan.id = :taiKhoanId")
    Optional<NhanVien> findByTaiKhoanId(@Param("taiKhoanId") UUID taiKhoanId);
}

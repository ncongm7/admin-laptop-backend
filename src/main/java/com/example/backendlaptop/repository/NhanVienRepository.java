package com.example.backendlaptop.repository;

import com.example.backendlaptop.dto.phanQuyenDto.nhanVien.NhanVienDto;
import com.example.backendlaptop.entity.NhanVien;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface NhanVienRepository extends JpaRepository<NhanVien, UUID> {
    @Query(" select new " +
            "com.example.backendlaptop.dto.phanQuyenDto.nhanVien." +
            "NhanVienDto(nv.id, nv.maNhanVien, nv.hoTen, nv.soDienThoai, nv.email, nv.gioiTinh, nv.anhNhanVien, nv.chucVu, nv.diaChi, nv.danhGia, nv.trangThai) " +
            "from NhanVien nv")
    List<NhanVienDto> findNhanViensBy();

    @Query(" select new " +
            "com.example.backendlaptop.dto.phanQuyenDto.nhanVien." +
            "NhanVienDto(nv.id, nv.maNhanVien, nv.hoTen, nv.soDienThoai, nv.email, nv.gioiTinh, nv.anhNhanVien, nv.chucVu, nv.diaChi, nv.danhGia, nv.trangThai) " +
            "from NhanVien nv")
    Page<NhanVienDto> phanTrangNV(Pageable pageable);

    boolean existsByMaNhanVienIgnoreCase(String maNhanVien);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsBySoDienThoai(String soDienThoai);

    @Query("select (count(nv)>0) from NhanVien nv where lower(nv.maNhanVien)=lower(?1) and nv.id<>?2")
    boolean existsMaNhanVienForUpdate(String maNhanVien, UUID excludeId);

    @Query("select (count(nv)>0) from NhanVien nv where lower(nv.email)=lower(?1) and nv.id<>?2")
    boolean existsEmailForUpdate(String email, UUID excludeId);

    @Query("select (count(nv)>0) from NhanVien nv where nv.soDienThoai=?1 and nv.id<>?2")
    boolean existsPhoneForUpdate(String soDienThoai, UUID excludeId);
<<<<<<< HEAD
}
=======
}
>>>>>>> main

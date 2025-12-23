package com.example.backendlaptop.repository.banhang;

import com.example.backendlaptop.entity.HoaDon;
import com.example.backendlaptop.model.TrangThaiHoaDon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface HoaDonRepository extends JpaRepository<HoaDon, UUID>, JpaSpecificationExecutor<HoaDon> {
        // Tìm các hóa đơn theo trạng thái
        List<HoaDon> findByTrangThai(TrangThaiHoaDon trangThai);

        // Tìm các hóa đơn theo trạng thái và loại hóa đơn
        List<HoaDon> findByTrangThaiAndLoaiHoaDon(TrangThaiHoaDon trangThai, Integer loaiHoaDon);

        // Đếm số lượng hóa đơn theo trạng thái
        Long countByTrangThai(TrangThaiHoaDon trangThai);

        // Tìm hóa đơn theo mã
        java.util.Optional<HoaDon> findByMa(String ma);

        // Tìm đơn hàng theo khách hàng
        org.springframework.data.domain.Page<HoaDon> findByIdKhachHang_IdOrderByNgayTaoDesc(UUID khachHangId,
                        org.springframework.data.domain.Pageable pageable);

        boolean existsByIdKhachHang_IdAndIdPhieuGiamGia_IdAndTrangThaiNot(UUID khachHangId, UUID phieuGiamGiaId,
                        TrangThaiHoaDon trangThai);

        // Kiểm tra đã sử dụng voucher (trừ hóa đơn hiện tại)
        boolean existsByIdKhachHang_IdAndIdPhieuGiamGia_IdAndTrangThaiNotAndIdNot(UUID khachHangId, UUID phieuGiamGiaId,
                        TrangThaiHoaDon trangThai, UUID idHoaDon);

        // Đếm số đơn hàng theo trạng thái của khách hàng
        long countByIdKhachHang_IdAndTrangThai(UUID khachHangId, TrangThaiHoaDon trangThai);
}

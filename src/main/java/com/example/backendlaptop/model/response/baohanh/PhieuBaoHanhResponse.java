package com.example.backendlaptop.model.response.baohanh;

import com.example.backendlaptop.entity.HoaDonChiTiet;
import com.example.backendlaptop.entity.PhieuBaoHanh;
import com.example.backendlaptop.entity.SerialDaBan;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Getter
@Setter
public class PhieuBaoHanhResponse {
    private UUID id;
    private String soSerial;
    private String tenSP;
    private String hoTenKhachHang;
    private String soDienThoai;
    private Instant ngayBatDau;
    private Instant ngayKetThuc;
    private Integer trangThai;
    private List<String> hinhAnh; // Danh sách URLs hình ảnh
    private String maPhieuBaoHanh;
    private UUID idHoaDonChiTiet;
    private String moTa;
    private java.math.BigDecimal chiPhi;
    private Integer soLanSuaChua;
    private java.time.Instant ngayTao;
    private java.time.Instant ngayCapNhat;

    public PhieuBaoHanhResponse(PhieuBaoHanh entity){
        this.id = entity.getId();


        if (entity.getIdKhachHang() != null) {
            this.hoTenKhachHang = entity.getIdKhachHang().getHoTen();
            this.soDienThoai = entity.getIdKhachHang().getSoDienThoai();
        } else {
            // Gán giá trị an toàn nếu Khách hàng là null
            this.hoTenKhachHang = "Khách hàng rông";
            this.soDienThoai = null;
        }


        // Sửa Serial
        SerialDaBan serialDaBan = entity.getIdSerialDaBan();
        if (serialDaBan != null && serialDaBan.getIdSerial() != null) {
            this.soSerial = serialDaBan.getIdSerial().getSerialNo();

            // Sửa Tên Sản Phẩm (truy cập sâu)
            HoaDonChiTiet hdct = serialDaBan.getIdHoaDonChiTiet();
            if (hdct != null &&
                    hdct.getChiTietSanPham() != null &&
                    hdct.getChiTietSanPham().getSanPham() != null) {

                this.tenSP = hdct.getChiTietSanPham().getSanPham().getTenSanPham();
            } else {
                this.tenSP = "Sản phẩm không xác định";
            }
        } else {
            this.soSerial = null;
            this.tenSP = "Sản phẩm không xác định";
        }

        this.ngayBatDau = entity.getNgayBatDau();
        this.ngayKetThuc = entity.getNgayKetThuc();
        this.trangThai = entity.getTrangThaiBaoHanh();
        
        // Parse JSON array từ hinhAnh
        if (entity.getHinhAnh() != null && !entity.getHinhAnh().trim().isEmpty()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                this.hinhAnh = mapper.readValue(entity.getHinhAnh(), new TypeReference<List<String>>() {});
            } catch (Exception e) {
                // Nếu parse lỗi, để null hoặc empty list
                this.hinhAnh = null;
            }
        } else {
            this.hinhAnh = null;
        }

        this.maPhieuBaoHanh = entity.getMaPhieuBaoHanh();
        this.idHoaDonChiTiet = entity.getIdHoaDonChiTiet() != null ? entity.getIdHoaDonChiTiet().getId() : null;
        this.moTa = entity.getMoTa();
        this.chiPhi = entity.getChiPhi();
        this.soLanSuaChua = entity.getSoLanSuaChua();
        this.ngayTao = entity.getNgayTao();
        this.ngayCapNhat = entity.getNgayCapNhat();
    }

}

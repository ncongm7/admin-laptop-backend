package com.example.backendlaptop.model.response.baohanh;

import com.example.backendlaptop.entity.HoaDonChiTiet;
import com.example.backendlaptop.entity.PhieuBaoHanh;
import com.example.backendlaptop.entity.SerialDaBan;
import lombok.Getter;
import lombok.Setter;

import java.rmi.server.UID;
import java.time.Instant;
import java.util.UUID;
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

    public PhieuBaoHanhResponse(PhieuBaoHanh entity){
        this.id = entity.getId();

        // ⚠️ PHẦN SỬA LỖI KHÁCH HÀNG (Nguyên nhân trực tiếp gây lỗi hiện tại)
        if (entity.getIdKhachHang() != null) {
            this.hoTenKhachHang = entity.getIdKhachHang().getHoTen();
            this.soDienThoai = entity.getIdKhachHang().getSoDienThoai();
        } else {
            // Gán giá trị an toàn nếu Khách hàng là null
            this.hoTenKhachHang = "Khách hàng rông";
            this.soDienThoai = null;
        }

        // ✅ NÊN SỬA LUÔN CẢ PHẦN SẢN PHẨM VÀ SERIAL (dễ bị null tương tự)

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
    }

}

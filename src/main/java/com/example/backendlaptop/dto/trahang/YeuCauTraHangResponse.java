package com.example.backendlaptop.dto.trahang;

import com.example.backendlaptop.entity.YeuCauTraHang;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
public class YeuCauTraHangResponse {
    private UUID id;
    private String maYeuCau;
    private UUID idHoaDon;
    private String maHoaDon;
    private UUID idKhachHang;
    private String tenKhachHang;
    private UUID idNhanVienXuLy;
    private String tenNhanVienXuLy;
    private String lyDoTraHang;
    private Instant ngayMua;
    private Instant ngayYeuCau;
    private Instant ngayDuyet;
    private Instant ngayHoanTat;
    private Integer trangThai;
    private String trangThaiDisplay;
    private Integer soNgaySauMua;
    private Integer loaiYeuCau;
    private String loaiYeuCauDisplay;
    private Integer hinhThucHoanTien;
    private String hinhThucHoanTienDisplay;
    private BigDecimal soTienHoan;
    private String lyDoTuChoi;
    private String ghiChu;
    private Instant ngayTao;
    private Instant ngaySua;
    private List<ChiTietTraHangResponse> chiTietList;

    public YeuCauTraHangResponse(YeuCauTraHang ycth) {
        this.id = ycth.getId();
        this.maYeuCau = ycth.getMaYeuCau();
        
        if (ycth.getIdHoaDon() != null) {
            this.idHoaDon = ycth.getIdHoaDon().getId();
            this.maHoaDon = ycth.getIdHoaDon().getMa();
        }
        
        if (ycth.getIdKhachHang() != null) {
            this.idKhachHang = ycth.getIdKhachHang().getId();
            this.tenKhachHang = ycth.getIdKhachHang().getHoTen();
        }
        
        if (ycth.getIdNhanVienXuLy() != null) {
            this.idNhanVienXuLy = ycth.getIdNhanVienXuLy().getId();
            this.tenNhanVienXuLy = ycth.getIdNhanVienXuLy().getHoTen();
        }
        
        this.lyDoTraHang = ycth.getLyDoTraHang();
        this.ngayMua = ycth.getNgayMua();
        this.ngayYeuCau = ycth.getNgayYeuCau();
        this.ngayDuyet = ycth.getNgayDuyet();
        this.ngayHoanTat = ycth.getNgayHoanTat();
        this.trangThai = ycth.getTrangThai();
        this.trangThaiDisplay = getTrangThaiDisplay(ycth.getTrangThai());
        this.soNgaySauMua = ycth.getSoNgaySauMua();
        this.loaiYeuCau = ycth.getLoaiYeuCau();
        this.loaiYeuCauDisplay = getLoaiYeuCauDisplay(ycth.getLoaiYeuCau());
        this.hinhThucHoanTien = ycth.getHinhThucHoanTien();
        this.hinhThucHoanTienDisplay = getHinhThucHoanTienDisplay(ycth.getHinhThucHoanTien());
        this.soTienHoan = ycth.getSoTienHoan();
        this.lyDoTuChoi = ycth.getLyDoTuChoi();
        this.ghiChu = ycth.getGhiChu();
        this.ngayTao = ycth.getNgayTao();
        this.ngaySua = ycth.getNgaySua();
        
        // Map chi tiết trả hàng
        if (ycth.getChiTietTraHangs() != null) {
            this.chiTietList = ycth.getChiTietTraHangs().stream()
                .map(ChiTietTraHangResponse::new)
                .collect(Collectors.toList());
        }
    }

    private String getTrangThaiDisplay(Integer trangThai) {
        if (trangThai == null) return "Không xác định";
        switch (trangThai) {
            case 0:
                return "Chờ duyệt";
            case 1:
                return "Đã duyệt";
            case 2:
                return "Từ chối";
            case 3:
                return "Hoàn tất";
            default:
                return "Không xác định";
        }
    }

    private String getLoaiYeuCauDisplay(Integer loaiYeuCau) {
        if (loaiYeuCau == null) return "Không xác định";
        return loaiYeuCau == 0 ? "Đổi trả (hoàn tiền)" : "Bảo hành";
    }

    private String getHinhThucHoanTienDisplay(Integer hinhThucHoanTien) {
        if (hinhThucHoanTien == null) return "Không xác định";
        switch (hinhThucHoanTien) {
            case 0:
                return "Theo phương thức gốc";
            case 1:
                return "Tiền mặt";
            case 2:
                return "Chuyển khoản";
            default:
                return "Không xác định";
        }
    }
}

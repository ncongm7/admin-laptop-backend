package com.example.backendlaptop.dto.sanpham.customer;

import com.example.backendlaptop.entity.ChiTietSanPham;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CTSPResponseCustomer {

    private UUID idctsp;
    private UUID idsp;
    private String tenSp;
    private String tenCpu;
    private String tenGpu;
    private String tenRam;
    private String dungLuongOCung;
    private String kichThuocManHinh;
    private BigDecimal giaBan;
    private BigDecimal giaTruocGiam; // show gạch
    private BigDecimal giaSauGiam;
    private Integer soLuongTon;

    public CTSPResponseCustomer(ChiTietSanPham ctsp){
        this.idctsp = ctsp.getId();
        this.idsp = ctsp.getSanPham().getId();
        this.tenSp = ctsp.getSanPham().getTenSanPham();
        this.tenCpu = ctsp.getCpu().getTenCpu();
        this.tenGpu = ctsp.getGpu().getTenGpu();
        this.tenRam = ctsp.getRam().getTenRam();
        this.giaBan = ctsp.getGiaBan();
        this.giaTruocGiam = this.giaBan; // mặc định không KM
        this.giaSauGiam   = this.giaBan;
        this.dungLuongOCung = ctsp.getOCung().getDungLuong();
        this.kichThuocManHinh = ctsp.getLoaiManHinh().getKichThuoc();
        this.soLuongTon=ctsp.getSoLuongTon();
    }
}

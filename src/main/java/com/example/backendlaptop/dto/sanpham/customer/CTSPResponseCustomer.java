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
    private String tenMauSac;
    private BigDecimal giaBan;

    public CTSPResponseCustomer(ChiTietSanPham ctsp){
        this.idctsp = ctsp.getId();
        this.idsp = ctsp.getSanPham().getId();
        this.tenSp = ctsp.getSanPham().getTenSanPham();
        this.tenCpu = ctsp.getCpu().getTenCpu();
        this.tenGpu = ctsp.getGpu().getTenGpu();
        this.tenRam = ctsp.getRam().getTenRam();
        this.giaBan = ctsp.getGiaBan();
        this.dungLuongOCung = ctsp.getOCung().getDungLuong();
        this.kichThuocManHinh = ctsp.getLoaiManHinh().getKichThuoc();
        this.tenMauSac = ctsp.getMauSac().getTenMau();
    }
}

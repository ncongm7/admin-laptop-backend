package com.example.backendlaptop.dto.sanpham.customer;

import com.example.backendlaptop.entity.ChiTietSanPham;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CTSPTTKTResponseCustomer {

    private UUID idctsp;
    private UUID idsp;
    private String tenSp;
    private String tenCpu;
    private String moTaCpu;
    private String tenGpu;
    private String moTaGpu;
    private String tenRam;
    private String moTaRam;
    private String dungLuongOCung;
    private String moTaOCung;
    private String kichThuocManHinh;
    private String moTaManHinh;
    private String dungLuongPin;
    private String moTaPin;
    private String tenMauSac;
    private String moTaMauSac;
    private String hexCodeMauSac;
    private BigDecimal giaBan;

    public CTSPTTKTResponseCustomer(ChiTietSanPham ctsp) {
        this.idctsp = ctsp.getId();
        this.idsp = ctsp.getSanPham().getId();
        this.tenSp = ctsp.getSanPham().getTenSanPham();
        this.tenCpu = ctsp.getCpu().getTenCpu();
        this.moTaCpu = ctsp.getCpu().getMoTa();
        this.tenGpu = ctsp.getGpu().getTenGpu();
        this.moTaGpu = ctsp.getGpu().getMoTa();
        this.tenRam = ctsp.getRam().getTenRam();
        this.moTaRam = ctsp.getRam().getMoTa();
        this.dungLuongOCung = ctsp.getOCung().getDungLuong();
        this.moTaOCung = ctsp.getOCung().getMoTa();
        this.kichThuocManHinh = ctsp.getLoaiManHinh().getKichThuoc();
        this.moTaManHinh = ctsp.getLoaiManHinh().getMoTa();
        this.dungLuongPin = ctsp.getPin().getDungLuongPin();
        this.moTaPin = ctsp.getPin().getMoTa();
        this.tenMauSac = ctsp.getMauSac().getTenMau();
        this.moTaMauSac = ctsp.getMauSac().getMoTa();
        this.hexCodeMauSac = ctsp.getMauSac().getHexCode();
        this.giaBan = ctsp.getGiaBan();
    }
}

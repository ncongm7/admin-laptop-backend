package com.example.backendlaptop.dto.sanpham.customer;

import com.example.backendlaptop.entity.ChiTietSanPham;
import com.example.backendlaptop.entity.HinhAnh;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
public class CTSPResponseCustomer {

    private UUID idctsp;
    private UUID idsp;
    private String maSanPham;
    private String tenSp;
    private String tenCpu;
    private String tenGpu;
    private String tenRam;
    private String dungLuongOCung;
    private String kichThuocManHinh;
    private String tenMauSac;
    private Integer soLuongTamGiu;
    private Integer soLuongTon;
    private BigDecimal giaBan;
    private List<String> hinhAnhs;

    public CTSPResponseCustomer(ChiTietSanPham ctsp, List<HinhAnh> hinhAnhs){
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
        this.maSanPham = ctsp.getSanPham().getMaSanPham();
        this.soLuongTamGiu = ctsp.getSoLuongTamGiu();
        this.soLuongTon = ctsp.getSoLuongTon();
        if(hinhAnhs != null){
            this.hinhAnhs = hinhAnhs.stream().map(HinhAnh::getUrl).collect(Collectors.toList());
        }
    }
}

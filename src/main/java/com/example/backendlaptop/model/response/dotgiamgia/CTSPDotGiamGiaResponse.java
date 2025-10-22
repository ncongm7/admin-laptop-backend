package com.example.backendlaptop.model.response.dotgiamgia;

import com.example.backendlaptop.entity.ChiTietSanPham;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CTSPDotGiamGiaResponse {
    private UUID id;
    private String ma;
    private String tenCPU;
    private String dungLuong;
    private String tenRam;
    private String tenGPU;
    private String tenMau;

    public CTSPDotGiamGiaResponse(ChiTietSanPham entity){
        this.id = entity.getId();
        this.ma = entity.getMaCtsp();
        this.tenCPU = entity.getCpu().getTenCpu();
        this.dungLuong = entity.getOCung().getDungLuong();
        this.tenRam = entity.getRam().getTenRam();
        this.tenGPU = entity.getGpu().getTenGpu();
        this.tenMau = entity.getMauSac().getTenMau();
    }
}

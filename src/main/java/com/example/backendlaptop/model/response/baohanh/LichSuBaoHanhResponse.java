package com.example.backendlaptop.model.response.baohanh;

import com.example.backendlaptop.entity.LichSuBaoHanh;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;
@Getter
@Setter
public class LichSuBaoHanhResponse {
    private UUID id;
    private Instant ngayTiepNhan;
    private Instant ngayHoanThanh;
    private Integer trangThai;
    private String  moTaLoi;

    public  LichSuBaoHanhResponse(LichSuBaoHanh entity) {
        this.id= entity.getId();
        this.ngayTiepNhan = entity.getNgayTiepNhan();
        this.ngayHoanThanh = entity.getNgayHoanThanh();
        this.trangThai = entity.getTrangThai();
        this.moTaLoi = entity.getMoTaLoi();
    }
}

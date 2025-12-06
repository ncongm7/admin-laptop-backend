package com.example.backendlaptop.model.response.baohanh;

import com.example.backendlaptop.entity.LyDoBaoHanh;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class LyDoBaoHanhResponse {
    private UUID id;
    private String maLyDo;
    private String tenLyDo;
    private String moTa;
    private String loaiLyDo;
    private Boolean isActive;
    private Integer thuTu;
    private Instant ngayTao;

    public LyDoBaoHanhResponse(LyDoBaoHanh entity) {
        this.id = entity.getId();
        this.maLyDo = entity.getMaLyDo();
        this.tenLyDo = entity.getTenLyDo();
        this.moTa = entity.getMoTa();
        this.loaiLyDo = entity.getLoaiLyDo();
        this.isActive = entity.getIsActive();
        this.thuTu = entity.getThuTu();
        this.ngayTao = entity.getNgayTao();
    }
}


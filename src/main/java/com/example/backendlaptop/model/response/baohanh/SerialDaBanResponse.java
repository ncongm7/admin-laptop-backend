package com.example.backendlaptop.model.response.baohanh;

import com.example.backendlaptop.entity.SerialDaBan;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
public class SerialDaBanResponse {
    private UUID idSerialDaBan;
    private UUID idSerial;
    private UUID idHoaDonChiTiet;
    private String serialNo;
    private String imei;
    private Instant ngayBan;

    public SerialDaBanResponse(SerialDaBan entity) {
        this.idSerialDaBan = entity.getId();
        this.ngayBan = entity.getNgayTao();

        if (entity.getIdHoaDonChiTiet() != null) {
            this.idHoaDonChiTiet = entity.getIdHoaDonChiTiet().getId();
        }

        if (entity.getIdSerial() != null) {
            this.idSerial = entity.getIdSerial().getId();
            this.serialNo = entity.getIdSerial().getSerialNo();
            this.imei = this.serialNo; // Serial entity does not have imei field, use serialNo
        }
    }
}

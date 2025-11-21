// FILE: src/main/java/com/example/backendlaptop/model/request/phieugiamgia/GanPhieuGiamGiaChoKhachHangRequest.java
package com.example.backendlaptop.model.request.phieugiamgia;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class GanPhieuGiamGiaChoKhachHangRequest {
    @NotNull(message = "ID phiếu giảm giá không được để trống")
    private UUID phieuGiamGiaId;
    
    @NotEmpty(message = "Danh sách khách hàng không được để trống")
    private List<UUID> khachHangIds;
}


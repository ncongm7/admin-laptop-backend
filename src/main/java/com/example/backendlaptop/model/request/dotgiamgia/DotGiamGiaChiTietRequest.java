package com.example.backendlaptop.model.request.dotgiamgia;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class DotGiamGiaChiTietRequest {

    // ID của đợt giảm giá mà các CTSP này sẽ được thêm vào
    @NotNull(message = "ID đợt giảm giá không được để trống")
    private UUID dotGiamGiaId;

    // Danh sách ID của các Chi Tiết Sản Phẩm đã được chọn trên Frontend
    @NotNull(message = "Danh sách chi tiết sản phẩm không được để trống")
    private List<UUID> ctspIds;

}

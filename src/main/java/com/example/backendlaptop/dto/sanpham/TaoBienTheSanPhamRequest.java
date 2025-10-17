package com.example.backendlaptop.dto.sanpham;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class TaoBienTheSanPhamRequest {
    
    @NotNull(message = "ID sản phẩm không được để trống")
    private UUID idSanPham;
    
    @NotNull(message = "Giá bán không được để trống")
    private java.math.BigDecimal giaBan;
    
    private String ghiChu;
    
    @NotNull(message = "Số lượng tồn không được để trống")
    private Integer soLuongTon;
    
    private Integer soLuongTamGiu;
    
    @NotNull(message = "Trạng thái không được để trống")
    private Integer trangThai;
    
    // Danh sách các thuộc tính đặc trưng được chọn để tạo biến thể
    private List<UUID> selectedCpuIds;
    private List<UUID> selectedGpuIds;
    private List<UUID> selectedRamIds;
    private List<UUID> selectedOCungIds;
    private List<UUID> selectedMauSacIds;
    private List<UUID> selectedLoaiManHinhIds;
    private List<UUID> selectedPinIds;
}

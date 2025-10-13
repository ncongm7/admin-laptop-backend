package com.example.backendlaptop.dto.sanpham;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
public class ChiTietSanPhamRequest {
    
    @NotNull(message = "ID sản phẩm không được để trống")
    private UUID idSanPham;
    
    @NotBlank(message = "Mã chi tiết sản phẩm không được để trống")
    @Size(max = 50, message = "Mã chi tiết sản phẩm không được vượt quá 50 ký tự")
    private String maCtsp;
    
    @NotNull(message = "Giá bán không được để trống")
    @DecimalMin(value = "0.0", message = "Giá bán phải lớn hơn hoặc bằng 0")
    private BigDecimal giaBan;
    
    private String ghiChu;
    
    @NotNull(message = "Số lượng tồn không được để trống")
    private Integer soLuongTon;
    
    private Integer soLuongTamGiu;
    
    @NotNull(message = "Trạng thái không được để trống")
    private Integer trangThai;
    
    // Các thuộc tính đặc trưng
    private UUID idCpu;
    private UUID idGpu;
    private UUID idRam;
    private UUID idOCung;
    private UUID idMauSac;
    private UUID idLoaiManHinh;
    private UUID idPin;
    
    // Danh sách các thuộc tính đặc trưng được chọn để tạo biến thể
    private List<UUID> selectedCpuIds;
    private List<UUID> selectedGpuIds;
    private List<UUID> selectedRamIds;
    private List<UUID> selectedOCungIds;
    private List<UUID> selectedMauSacIds;
    private List<UUID> selectedLoaiManHinhIds;
    private List<UUID> selectedPinIds;
}

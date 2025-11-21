package com.example.backendlaptop.controller.trahang;

import com.example.backendlaptop.dto.trahang.KiemTraDieuKienResponse;
import com.example.backendlaptop.dto.trahang.YeuCauTraHangResponse;
import com.example.backendlaptop.model.request.trahang.TaoYeuCauTraHangRequest;
import com.example.backendlaptop.model.response.ResponseObject;
import com.example.backendlaptop.service.trahang.TraHangService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tra-hang")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TraHangController {

    private final TraHangService traHangService;

    /**
     * Kiểm tra điều kiện trả hàng/bảo hành
     * GET /api/v1/tra-hang/kiem-tra-dieu-kien/{idHoaDon}
     */
    @GetMapping("/kiem-tra-dieu-kien/{idHoaDon}")
    public ResponseEntity<ResponseObject<KiemTraDieuKienResponse>> kiemTraDieuKien(
            @PathVariable UUID idHoaDon
    ) {
        KiemTraDieuKienResponse response = traHangService.kiemTraDieuKien(idHoaDon);
        return ResponseEntity.ok(new ResponseObject<>(response, "Kiểm tra điều kiện thành công"));
    }

    /**
     * Tạo yêu cầu trả hàng
     * POST /api/v1/tra-hang/tao-yeu-cau
     * 
     * Content-Type: multipart/form-data
     * 
     * Form data:
     * - idHoaDon: UUID
     * - idKhachHang: UUID
     * - idHoaDonChiTiet: UUID
     * - idSerialDaBan: UUID (optional)
     * - lyDoTraHang: String
     * - tinhTrangLucTra: String (Tốt, Hỏng, Trầy xước, Khác)
     * - moTaTinhTrang: String (optional)
     * - loaiYeuCau: Integer (0: Đổi trả, 1: Bảo hành)
     * - soLuong: Integer
     * - hinhAnh: MultipartFile[] (optional)
     */
    @PostMapping("/tao-yeu-cau")
    public ResponseEntity<ResponseObject<YeuCauTraHangResponse>> taoYeuCau(
            @RequestParam("idHoaDon") UUID idHoaDon,
            @RequestParam("idKhachHang") UUID idKhachHang,
            @RequestParam("idHoaDonChiTiet") UUID idHoaDonChiTiet,
            @RequestParam(value = "idSerialDaBan", required = false) UUID idSerialDaBan,
            @RequestParam("lyDoTraHang") String lyDoTraHang,
            @RequestParam("tinhTrangLucTra") String tinhTrangLucTra,
            @RequestParam(value = "moTaTinhTrang", required = false) String moTaTinhTrang,
            @RequestParam("loaiYeuCau") Integer loaiYeuCau,
            @RequestParam("soLuong") Integer soLuong,
            @RequestParam(value = "hinhAnh", required = false) List<MultipartFile> hinhAnh
    ) {
        // Tạo request object
        TaoYeuCauTraHangRequest request = new TaoYeuCauTraHangRequest();
        request.setIdHoaDon(idHoaDon);
        request.setIdKhachHang(idKhachHang);
        request.setIdHoaDonChiTiet(idHoaDonChiTiet);
        request.setIdSerialDaBan(idSerialDaBan);
        request.setLyDoTraHang(lyDoTraHang);
        request.setTinhTrangLucTra(tinhTrangLucTra);
        request.setMoTaTinhTrang(moTaTinhTrang);
        request.setLoaiYeuCau(loaiYeuCau);
        request.setSoLuong(soLuong);

        YeuCauTraHangResponse response = traHangService.taoYeuCau(request, hinhAnh);
        return ResponseEntity.ok(new ResponseObject<>(response, "Tạo yêu cầu trả hàng thành công"));
    }
}

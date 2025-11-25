package com.example.backendlaptop.controller.baohanh;

import com.example.backendlaptop.dto.trahang.KiemTraDieuKienResponse;
import com.example.backendlaptop.model.request.baohanh.TaoYeuCauBaoHanhRequest;
import com.example.backendlaptop.model.response.ResponseObject;
import com.example.backendlaptop.model.response.baohanh.PhieuBaoHanhResponse;
import com.example.backendlaptop.service.baohanh.BaoHanhService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bao-hanh")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class BaoHanhController {

    private final BaoHanhService baoHanhService;

    @GetMapping("/kiem-tra/{idHoaDon}")
    public ResponseEntity<ResponseObject<KiemTraDieuKienResponse>> kiemTraDieuKien(
            @PathVariable UUID idHoaDon
    ) {
        KiemTraDieuKienResponse response = baoHanhService.kiemTraDieuKien(idHoaDon);
        return ResponseEntity.ok(new ResponseObject<>(response, "Kiểm tra điều kiện thành công"));
    }

    @PostMapping("/tao-yeu-cau")
    public ResponseEntity<ResponseObject<PhieuBaoHanhResponse>> taoYeuCau(
            @RequestParam("idHoaDon") UUID idHoaDon,
            @RequestParam("idKhachHang") UUID idKhachHang,
            @RequestParam("idHoaDonChiTiet") UUID idHoaDonChiTiet,
            @RequestParam(value = "idSerialDaBan", required = false) UUID idSerialDaBan,
            @RequestParam("lyDoTraHang") String lyDoTraHang,
            @RequestParam("tinhTrangLucTra") String tinhTrangLucTra,
            @RequestParam(value = "moTaTinhTrang", required = false) String moTaTinhTrang,
            @RequestParam("soLuong") Integer soLuong,
            @RequestParam(value = "hinhAnh", required = false) List<MultipartFile> hinhAnh
    ) {
        TaoYeuCauBaoHanhRequest request = new TaoYeuCauBaoHanhRequest();
        request.setIdHoaDon(idHoaDon);
        request.setIdKhachHang(idKhachHang);
        request.setIdHoaDonChiTiet(idHoaDonChiTiet);
        request.setIdSerialDaBan(idSerialDaBan);
        request.setLyDoTraHang(lyDoTraHang);
        request.setTinhTrangLucTra(tinhTrangLucTra);
        request.setMoTaTinhTrang(moTaTinhTrang);
        request.setSoLuong(soLuong);

        PhieuBaoHanhResponse response = baoHanhService.taoYeuCau(request, hinhAnh);
        return ResponseEntity.ok(new ResponseObject<>(response, "Tạo phiếu bảo hành thành công"));
    }
}


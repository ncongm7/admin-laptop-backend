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

    @GetMapping("/hoa-don/{idHoaDon}")
    public ResponseEntity<ResponseObject<List<PhieuBaoHanhResponse>>> getWarrantiesByInvoice(
            @PathVariable UUID idHoaDon
    ) {
        List<PhieuBaoHanhResponse> warranties = baoHanhService.getWarrantiesByInvoice(idHoaDon);
        return ResponseEntity.ok(new ResponseObject<>(warranties, "Lấy danh sách bảo hành thành công"));
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

    @PostMapping("/tiep-nhan/{idBaoHanh}")
    public ResponseEntity<ResponseObject<PhieuBaoHanhResponse>> tiepNhanSanPham(
            @PathVariable UUID idBaoHanh,
            @ModelAttribute com.example.backendlaptop.model.request.baohanh.TiepNhanRequest request
    ) {
        PhieuBaoHanhResponse response = baoHanhService.tiepNhanSanPham(idBaoHanh, request);
        return ResponseEntity.ok(new ResponseObject<>(response, "Tiếp nhận sản phẩm thành công"));
    }

    @PostMapping("/chi-phi/{idLichSuBaoHanh}")
    public ResponseEntity<ResponseObject<com.example.backendlaptop.entity.LichSuBaoHanh>> themChiPhiPhatSinh(
            @PathVariable UUID idLichSuBaoHanh,
            @RequestBody com.example.backendlaptop.model.request.baohanh.ChiPhiPhatSinhRequest request
    ) {
        com.example.backendlaptop.entity.LichSuBaoHanh response = baoHanhService.themChiPhiPhatSinh(idLichSuBaoHanh, request);
        return ResponseEntity.ok(new ResponseObject<>(response, "Thêm chi phí phát sinh thành công"));
    }

    @PostMapping("/ban-giao/{idBaoHanh}")
    public ResponseEntity<ResponseObject<PhieuBaoHanhResponse>> banGiaoSanPham(
            @PathVariable UUID idBaoHanh,
            @ModelAttribute com.example.backendlaptop.model.request.baohanh.BanGiaoRequest request
    ) {
        PhieuBaoHanhResponse response = baoHanhService.banGiaoSanPham(idBaoHanh, request);
        return ResponseEntity.ok(new ResponseObject<>(response, "Bàn giao sản phẩm thành công"));
    }
}


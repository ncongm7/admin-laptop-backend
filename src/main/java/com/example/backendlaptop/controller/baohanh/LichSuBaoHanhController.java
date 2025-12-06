package com.example.backendlaptop.controller.baohanh;

import com.example.backendlaptop.model.request.baohanh.LichSuBaoHanhAddRequest;
import com.example.backendlaptop.model.request.baohanh.LichSuBaoHanhUpdateRequest;
import com.example.backendlaptop.model.request.phieugiamgia.PhieuGiamGiaRequest;
import com.example.backendlaptop.model.response.ResponseObject;
import com.example.backendlaptop.model.response.baohanh.LichSuBaoHanhResponse;
import com.example.backendlaptop.service.baohanh.LichSuBaoHanhService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/lich-su-bao-hanh-quan-ly")
@CrossOrigin(origins = "*")
public class LichSuBaoHanhController {
    @Autowired
    LichSuBaoHanhService service;



    @GetMapping("/danh-sach/{phieuBaoHanhId}")
    public ResponseObject<?> danhSach(@PathVariable UUID phieuBaoHanhId){
        return new ResponseObject<>(service.listByPhieuBaoHanhId(phieuBaoHanhId));
    }
    @DeleteMapping("/delete/{id1}")
    public ResponseObject<?> delete(@PathVariable("id1") UUID id1){
        service.delete(id1);
        return new ResponseObject<>(null, "Xoa thanh cong");
    }
    @PostMapping(value = "/add", consumes = "application/json", produces = "application/json")
    public ResponseObject<LichSuBaoHanhResponse> add(@Valid @RequestBody LichSuBaoHanhAddRequest req) {
        LichSuBaoHanhResponse res = service.add(req.idPhieuBaoHanh, req.moTaLoi);
        return new ResponseObject<>(res, "Thêm thành công");
    }
    @PutMapping("/update/{id}")
    public ResponseObject<?> update(@PathVariable("id") UUID id1,@Valid @RequestBody LichSuBaoHanhUpdateRequest request){
        service.update(request,id1);
        return new ResponseObject<>(null, "Update thanh cong");
    }

    @PostMapping("/cap-nhat-hinh-anh/{id}")
    public ResponseObject<LichSuBaoHanhResponse> capNhatHinhAnh(
            @PathVariable UUID id,
            @RequestParam(value = "hinhAnhTruoc", required = false) java.util.List<org.springframework.web.multipart.MultipartFile> hinhAnhTruoc,
            @RequestParam(value = "hinhAnhSau", required = false) java.util.List<org.springframework.web.multipart.MultipartFile> hinhAnhSau
    ) {
        LichSuBaoHanhResponse response = service.capNhatHinhAnh(id, hinhAnhTruoc, hinhAnhSau);
        return new ResponseObject<>(response, "Cập nhật hình ảnh thành công");
    }

    @PutMapping("/cap-nhat-chi-phi/{id}")
    public ResponseObject<LichSuBaoHanhResponse> capNhatChiPhi(
            @PathVariable UUID id,
            @RequestParam(value = "chiPhiPhatSinh", required = false) java.math.BigDecimal chiPhiPhatSinh,
            @RequestParam(value = "daThanhToan", required = false) Boolean daThanhToan
    ) {
        LichSuBaoHanhResponse response = service.capNhatChiPhi(id, chiPhiPhatSinh, daThanhToan);
        return new ResponseObject<>(response, "Cập nhật chi phí thành công");
    }

}

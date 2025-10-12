package com.example.backendlaptop.controller.dotgiamgia;

import com.example.backendlaptop.model.request.dotgiamgia.DotGiamGiaChiTietRequest;
import com.example.backendlaptop.model.response.ResponseObject;
import com.example.backendlaptop.service.dotgiamgia.DotGiamGiaChiTietService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/dot-giam-gia-chi-tiet-quan-ly")
@CrossOrigin(origins = "*")
public class DotGiamGiaChiTietController {
    @Autowired
    DotGiamGiaChiTietService service;

    @GetMapping("/danh-sach/{iddotgiamgia}")
    public ResponseObject<?> danhSach(@PathVariable("iddotgiamgia") UUID iddotgiamgia){
        return new ResponseObject<>(service.findByDotGiamGiaId(iddotgiamgia));
    }

    @GetMapping("/san-pham-combobox")
    public ResponseObject<?> getSanPhamsForCombobox() {
        return new ResponseObject<>(service.findAllSanPhaṃ());
    }

    /**
     * Endpoint 2: Lấy danh sách CTSP chưa khuyến mãi theo SP ID
     * URL: GET /api/dot-giam-gia-chi-tiet/available-ctsp/{dotGiamGiaId}?sanPhamId=...
     */
    @GetMapping("/available-ctsp/{dotGiamGiaId}")
    public ResponseObject<?> getAvailableCtsp(
            @PathVariable UUID dotGiamGiaId,
            @RequestParam(required = false) UUID sanPhamId) {

        return new ResponseObject<>(service.getAvailableProductsBySanPham(dotGiamGiaId, sanPhamId));
    }
    @DeleteMapping("/delete/{id}")
    public ResponseObject<?> delete(@PathVariable("id") UUID id){
        service.delete(id);
        return new ResponseObject<>(null, "Xoa thanh cong");
    }
    @PostMapping("/add-ctsp")
    public ResponseObject<?> addProductsToDiscount(@RequestBody DotGiamGiaChiTietRequest request) {
        service.addSelectedProducts(request);
        return new ResponseObject<>(null, "Add thanh cong");
    }
}

package com.example.backendlaptop.controller.phanQuyenCon;

import com.example.backendlaptop.dto.phanQuyenDto.khachHang.KhachHangRequest;
import com.example.backendlaptop.service.PhanQuyenSer.KhachHangService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/khach-hang")
@CrossOrigin("*")

public class KhachHangController {
    @Autowired
    public KhachHangService khachHangService;

    @GetMapping("/hien-thi")
    public ResponseEntity<Object> hienThi() {
        return ResponseEntity.ok(khachHangService.findAllKH());

    }
    @GetMapping("/tim-kiem")
    public ResponseEntity<Object> timKiem(
            @RequestParam(value = "ten", required = false) String hoTen,
            @RequestParam(value = "sdt", required = false) String soDienThoai
    ){
        return ResponseEntity.ok(khachHangService.timKiem(hoTen,soDienThoai));
    }

    @DeleteMapping("/xoa/{id}")
    public ResponseEntity<Object> delete(@PathVariable("id") UUID id) {
        khachHangService.xoaKH(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/sua-khach-hang/{id}")
    public ResponseEntity<Object> update(@Valid @PathVariable("id") UUID id, @RequestBody KhachHangRequest khachHangRequest) {
        khachHangService.updateKH(id, khachHangRequest);
        return ResponseEntity.ok().build();

    }

    @PostMapping("/add-khach-hang")
    public ResponseEntity<Object> add(@Valid @RequestBody KhachHangRequest khachHangRequest) {
        try {
            Map<String, String> loginInfo = khachHangService.addKH(khachHangRequest);
            return ResponseEntity.ok(loginInfo);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }

    @GetMapping("/phan-trang/{pageOne}/{pageSize}")
    public ResponseEntity<Object> phanTrangKhachHang(@PathVariable("pageOne") Integer pageOne, @PathVariable("pageSize") Integer pageSize) {
        Page<?> page = khachHangService.phanTrangKH(pageOne, pageSize);
        
        Map<String, Object> response = new HashMap<>();
        response.put("data", page.getContent());
        response.put("total", page.getTotalElements());
        response.put("totalPages", page.getTotalPages());
        response.put("currentPage", page.getNumber());
        response.put("pageSize", page.getSize());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/getOne/{id}")
    public ResponseEntity<Object> getOne(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(khachHangService.getOne(id));
    }

    @GetMapping("/by-ma/{maKhachHang}")
    public ResponseEntity<Object> getByMaKhachHang(@PathVariable("maKhachHang") String maKhachHang) {
        return ResponseEntity.ok(khachHangService.findByMaKhachHang(maKhachHang));
    }

//Thêm mã
@GetMapping("/generate-code")
public ResponseEntity<String> generateMaKhachHang() {
    String newCode = khachHangService.generateMaKhachHang();
    return ResponseEntity.ok(newCode);
}

//tính tổng tiền
    @GetMapping("/tong-tien/{id}")
    public ResponseEntity<Object> tongTien(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(khachHangService.tongTienMua(id));
    }

}

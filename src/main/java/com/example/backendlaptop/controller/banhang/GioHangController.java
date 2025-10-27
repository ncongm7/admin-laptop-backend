package com.example.backendlaptop.controller.banhang;

import com.example.backendlaptop.dto.giohang.GioHangRequest;
import com.example.backendlaptop.model.response.ResponseObject;
import com.example.backendlaptop.service.banhang.GioHangService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/gio-hang")
@CrossOrigin(origins = "*")
public class GioHangController {

    @Autowired
    private GioHangService gioHangService;

    @GetMapping("")
    public ResponseObject<?> getAll() {
        return new ResponseObject<>(gioHangService.getAll());
    }

    @GetMapping("/page")
    public ResponseObject<?> getAll(@RequestParam(value = "pageNo", defaultValue = "0") Integer pageNo) {
        return new ResponseObject<>(gioHangService.getAll(pageNo));
    }

    @GetMapping("/{id}")
    public ResponseObject<?> findById(@PathVariable UUID id) {
        return new ResponseObject<>(gioHangService.findById(id));
    }

    @PostMapping("")
    public ResponseObject<?> create(@RequestBody @Valid GioHangRequest gioHangRequest) {
        return new ResponseObject<>(gioHangService.create(gioHangRequest));
    }

    @PutMapping("/{id}")
    public ResponseObject<?> update(@PathVariable UUID id, @RequestBody @Valid GioHangRequest gioHangRequest) {
        return new ResponseObject<>(gioHangService.update(id, gioHangRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseObject<?> delete(@PathVariable UUID id) {
        gioHangService.delete(id);
        return new ResponseObject<>("Success");
    }
}

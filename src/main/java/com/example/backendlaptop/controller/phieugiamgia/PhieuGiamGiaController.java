package com.example.backendlaptop.controller.phieugiamgia;

import com.example.backendlaptop.model.request.phieugiamgia.PhieuGiamGiaRequest;
import com.example.backendlaptop.model.response.ResponseObject;
import com.example.backendlaptop.service.phieugiamgia.PhieuGiamGiaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/phieu-giam-gia-quan-ly")
@CrossOrigin(origins = "*")
public class PhieuGiamGiaController {
    @Autowired
    PhieuGiamGiaService service;

    @GetMapping("/danh-sach")
    public ResponseObject<?> danhSach(){
        return new ResponseObject<>(service.getAll());
    }


    @DeleteMapping("/delete/{id1}")
    public ResponseObject<?> delete(@PathVariable("id1") UUID id1){
        service.delete(id1);
        return new ResponseObject<>(null, "Xoa thanh cong");
    }

    @GetMapping("/detail/{id1}")
    public ResponseObject<?> detail(@PathVariable("id1") UUID id1){
        return new ResponseObject<>(service.detail(id1));
    }

    @PostMapping("/add")
    public ResponseObject<?> add(@Valid @RequestBody PhieuGiamGiaRequest request){
        service.add(request);
        return new ResponseObject<>(null, "Add thanh cong");
    }

    @PutMapping("/update/{id}")
    public ResponseObject<?> update(@PathVariable("id") UUID id1,@Valid @RequestBody PhieuGiamGiaRequest request){
        service.update(request,id1);
        return new ResponseObject<>(null, "Update thanh cong");
    }
}

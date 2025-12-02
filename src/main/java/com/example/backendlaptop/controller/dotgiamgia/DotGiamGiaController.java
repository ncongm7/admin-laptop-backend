package com.example.backendlaptop.controller.dotgiamgia;

import com.example.backendlaptop.model.request.dotgiamgia.DotGiamGiaRequest;
import com.example.backendlaptop.model.response.ResponseObject;
import com.example.backendlaptop.service.dotgiamgia.DotGiamGiaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/dot-giam-gia-quan-ly")
@CrossOrigin(origins = "*")
public class DotGiamGiaController {
    @Autowired
    DotGiamGiaService service;

    @GetMapping("/danh-sach")
    public ResponseObject<?> danhSach(){
        return new ResponseObject<>(service.getAll());
    }


    @DeleteMapping("/delete/{id}")
    public ResponseObject<?> delete(@PathVariable("id") UUID id){
        service.delete(id);
        return new ResponseObject<>(null, "Xoa thanh cong");
    }

    @GetMapping("/detail/{id}")
    public ResponseObject<?> detail(@PathVariable("id") UUID id){
        return new ResponseObject<>(service.detail(id));
    }

    @PostMapping("/add")
    public ResponseObject<?> add(@Valid @RequestBody DotGiamGiaRequest request){
        service.add(request);
        return new ResponseObject<>(null, "Add thanh cong");
    }

    @PutMapping("/update/{id}")
    public ResponseObject<?> update(@PathVariable("id") UUID id,@Valid @RequestBody DotGiamGiaRequest request){
        service.update(request,id);
        return new ResponseObject<>(null, "Update thanh cong");
    }

    @PutMapping("/toggle-status/{id}")
    public ResponseObject<?> toggleStatus(@PathVariable("id") UUID id){
        service.toggleStatus(id);
        return new ResponseObject<>(null, "Chuyển trạng thái thành công");
    }
}

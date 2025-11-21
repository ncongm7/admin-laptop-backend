package com.example.backendlaptop.controller.trahang;

import com.example.backendlaptop.model.response.ResponseObject;
import com.example.backendlaptop.service.trahang.YeuCauTraHangQuanLyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/yeu-cau-tra-hang-quan-ly")
@CrossOrigin(origins = "*")
public class YeuCauTraHangQuanLyController {
    
    @Autowired
    private YeuCauTraHangQuanLyService service;

    @GetMapping("/danh-sach")
    public ResponseObject<?> danhSach() {
        return new ResponseObject<>(service.getAll());
    }

    @GetMapping("/detail/{id}")
    public ResponseObject<?> detail(@PathVariable("id") UUID id) {
        return new ResponseObject<>(service.getById(id));
    }
    
    @PutMapping("/update-trang-thai/{id}")
    public ResponseObject<?> updateTrangThai(
            @PathVariable("id") UUID id,
            @RequestParam("trangThai") Integer trangThai,
            @RequestParam(value = "lyDoTuChoi", required = false) String lyDoTuChoi,
            @RequestParam(value = "idNhanVienXuLy", required = false) UUID idNhanVienXuLy) {
        return new ResponseObject<>(
            service.updateTrangThai(id, trangThai, lyDoTuChoi, idNhanVienXuLy), 
            "Cập nhật trạng thái thành công"
        );
    }
    
    @DeleteMapping("/delete/{id}")
    public ResponseObject<?> delete(@PathVariable("id") UUID id) {
        service.delete(id);
        return new ResponseObject<>(null, "Xóa thành công");
    }
}


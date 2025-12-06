package com.example.backendlaptop.controller.baohanh;

import com.example.backendlaptop.model.response.ResponseObject;
import com.example.backendlaptop.model.response.baohanh.LyDoBaoHanhResponse;
import com.example.backendlaptop.service.baohanh.LyDoBaoHanhService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ly-do-bao-hanh")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class LyDoBaoHanhController {
    private final LyDoBaoHanhService lyDoBaoHanhService;

    @GetMapping
    public ResponseEntity<ResponseObject<List<LyDoBaoHanhResponse>>> getAllLyDo() {
        List<LyDoBaoHanhResponse> lyDoList = lyDoBaoHanhService.getAllLyDo();
        return ResponseEntity.ok(new ResponseObject<>(lyDoList, "Lấy danh sách lý do bảo hành thành công"));
    }

    @GetMapping("/loai/{loai}")
    public ResponseEntity<ResponseObject<List<LyDoBaoHanhResponse>>> getLyDoByLoai(
            @PathVariable String loai
    ) {
        List<LyDoBaoHanhResponse> lyDoList = lyDoBaoHanhService.getLyDoByLoai(loai);
        return ResponseEntity.ok(new ResponseObject<>(lyDoList, "Lấy danh sách lý do theo loại thành công"));
    }
}


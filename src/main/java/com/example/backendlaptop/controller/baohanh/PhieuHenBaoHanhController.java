package com.example.backendlaptop.controller.baohanh;

import com.example.backendlaptop.model.request.baohanh.PhieuHenBaoHanhRequest;
import com.example.backendlaptop.model.response.ResponseObject;
import com.example.backendlaptop.model.response.baohanh.PhieuHenBaoHanhResponse;
import com.example.backendlaptop.service.baohanh.PhieuHenBaoHanhService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/phieu-hen-bao-hanh")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class PhieuHenBaoHanhController {
    private final PhieuHenBaoHanhService phieuHenBaoHanhService;

    @PostMapping("/bao-hanh/{idBaoHanh}")
    public ResponseEntity<ResponseObject<PhieuHenBaoHanhResponse>> taoPhieuHen(
            @PathVariable UUID idBaoHanh,
            @Valid @RequestBody PhieuHenBaoHanhRequest request
    ) {
        PhieuHenBaoHanhResponse response = phieuHenBaoHanhService.taoPhieuHen(idBaoHanh, request);
        return ResponseEntity.ok(new ResponseObject<>(response, "Tạo phiếu hẹn thành công"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseObject<PhieuHenBaoHanhResponse>> getPhieuHenById(
            @PathVariable UUID id
    ) {
        PhieuHenBaoHanhResponse response = phieuHenBaoHanhService.getPhieuHenById(id);
        return ResponseEntity.ok(new ResponseObject<>(response, "Lấy chi tiết phiếu hẹn thành công"));
    }

    @GetMapping("/bao-hanh/{idBaoHanh}")
    public ResponseEntity<ResponseObject<List<PhieuHenBaoHanhResponse>>> getPhieuHenByBaoHanh(
            @PathVariable UUID idBaoHanh
    ) {
        List<PhieuHenBaoHanhResponse> response = phieuHenBaoHanhService.getPhieuHenByBaoHanh(idBaoHanh);
        return ResponseEntity.ok(new ResponseObject<>(response, "Lấy danh sách phiếu hẹn thành công"));
    }

    @PutMapping("/{id}/xac-nhan")
    public ResponseEntity<ResponseObject<PhieuHenBaoHanhResponse>> xacNhanPhieuHen(
            @PathVariable UUID id
    ) {
        PhieuHenBaoHanhResponse response = phieuHenBaoHanhService.xacNhanPhieuHen(id);
        return ResponseEntity.ok(new ResponseObject<>(response, "Xác nhận phiếu hẹn thành công"));
    }

    @PostMapping("/{id}/gui-email")
    public ResponseEntity<ResponseObject<String>> guiEmailPhieuHen(
            @PathVariable UUID id
    ) {
        phieuHenBaoHanhService.guiEmailPhieuHen(id);
        return ResponseEntity.ok(new ResponseObject<>("Email đã được gửi thành công", "Gửi email thành công"));
    }
}


package com.example.backendlaptop.service;

import com.example.backendlaptop.dto.giohang.GioHangRequest;
import com.example.backendlaptop.dto.giohang.GioHangResponse;
import com.example.backendlaptop.entity.GioHang;
import com.example.backendlaptop.entity.KhachHang;
import com.example.backendlaptop.expection.ApiException;
import com.example.backendlaptop.repository.GioHangRepository;
import com.example.backendlaptop.repository.KhachHangRepository;
import com.example.backendlaptop.until.MapperUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GioHangService {

    @Autowired
    private GioHangRepository gioHangRepository;

    @Autowired
    private KhachHangRepository khachHangRepository;

    public List<GioHangResponse> getAll() {
        return gioHangRepository.findAllWithKhachHang().stream().map(GioHangResponse::new).collect(Collectors.toList());
    }

    public Page<GioHangResponse> getAll(Integer pageNo) {
        Pageable pageable = PageRequest.of(pageNo, 5);
        Page<GioHang> gioHangPage = gioHangRepository.findAll(pageable);
        return gioHangPage.map(GioHangResponse::new);
    }

    public GioHangResponse findById(UUID id) {
        Optional<GioHang> gioHang = gioHangRepository.findById(id);
        if (gioHang.isEmpty()) {
            throw new ApiException("Not found", "NOT_FOUND");
        }
        return new GioHangResponse(gioHang.get());
    }

    public GioHang create(GioHangRequest request) {
        Optional<KhachHang> khachHang = khachHangRepository.findById(request.getKhachHangId());
        if (khachHang.isEmpty()) {
            throw new ApiException("Khach hang not found", "NOT_FOUND");
        }
        GioHang gioHang = MapperUtils.map(request, GioHang.class);
        gioHang.setKhachHang(khachHang.get());
        return gioHangRepository.save(gioHang);
    }

    public GioHang update(UUID id, GioHangRequest request) {
        Optional<GioHang> optionalGioHang = gioHangRepository.findById(id);
        if (optionalGioHang.isEmpty()) {
            throw new ApiException("Not found", "NOT_FOUND");
        }
        Optional<KhachHang> khachHang = khachHangRepository.findById(request.getKhachHangId());
        if (khachHang.isEmpty()) {
            throw new ApiException("Khach hang not found", "NOT_FOUND");
        }
        GioHang gioHang = optionalGioHang.get();
        gioHang.setKhachHang(khachHang.get());
        gioHang.setNgayCapNhat(request.getNgayCapNhat());
        gioHang.setTrangThaiGioHang(request.getTrangThaiGioHang());
        return gioHangRepository.save(gioHang);
    }

    public void delete(UUID id) {
        if (!gioHangRepository.existsById(id)) {
            throw new ApiException("Not found", "NOT_FOUND");
        }
        gioHangRepository.deleteById(id);
    }
}

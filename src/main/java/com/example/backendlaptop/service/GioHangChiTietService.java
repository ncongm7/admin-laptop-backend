package com.example.backendlaptop.service;

import com.example.backendlaptop.dto.giohang.GioHangChiTietRequest;
import com.example.backendlaptop.dto.giohang.GioHangChiTietResponse;
import com.example.backendlaptop.entity.ChiTietSanPham;
import com.example.backendlaptop.entity.GioHang;
import com.example.backendlaptop.entity.GioHangChiTiet;
import com.example.backendlaptop.expection.ApiException;
import com.example.backendlaptop.repository.ChiTietSanPhamRepository;
import com.example.backendlaptop.repository.GioHangChiTietRepository;
import com.example.backendlaptop.repository.GioHangRepository;
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
public class GioHangChiTietService {

    @Autowired
    private GioHangChiTietRepository gioHangChiTietRepository;

    @Autowired
    private GioHangRepository gioHangRepository;

    @Autowired
    private ChiTietSanPhamRepository chiTietSanPhamRepository;

    public List<GioHangChiTietResponse> getAll() {
        return gioHangChiTietRepository.findAll().stream().map(GioHangChiTietResponse::new).collect(Collectors.toList());
    }

    public Page<GioHangChiTietResponse> getAll(Integer pageNo) {
        Pageable pageable = PageRequest.of(pageNo, 5);
        Page<GioHangChiTiet> gioHangChiTietPage = gioHangChiTietRepository.findAll(pageable);
        return gioHangChiTietPage.map(GioHangChiTietResponse::new);
    }

    public GioHangChiTietResponse findById(UUID id) {
        Optional<GioHangChiTiet> gioHangChiTiet = gioHangChiTietRepository.findById(id);
        if (gioHangChiTiet.isEmpty()) {
            throw new ApiException("Not found", "NOT_FOUND");
        }
        return new GioHangChiTietResponse(gioHangChiTiet.get());
    }

    public GioHangChiTiet create(GioHangChiTietRequest request) {
        Optional<GioHang> gioHang = gioHangRepository.findById(request.getGioHangId());
        if (gioHang.isEmpty()) {
            throw new ApiException("Gio hang not found", "NOT_FOUND");
        }
        Optional<ChiTietSanPham> chiTietSanPham = chiTietSanPhamRepository.findById(request.getChiTietSanPhamId());
        if (chiTietSanPham.isEmpty()) {
            throw new ApiException("Chi tiet san pham not found", "NOT_FOUND");
        }
        GioHangChiTiet gioHangChiTiet = MapperUtils.map(request, GioHangChiTiet.class);
        gioHangChiTiet.setGioHang(gioHang.get());
        gioHangChiTiet.setChiTietSanPham(chiTietSanPham.get());
        return gioHangChiTietRepository.save(gioHangChiTiet);
    }

    public GioHangChiTiet update(UUID id, GioHangChiTietRequest request) {
        Optional<GioHangChiTiet> optionalGioHangChiTiet = gioHangChiTietRepository.findById(id);
        if (optionalGioHangChiTiet.isEmpty()) {
            throw new ApiException("Not found", "NOT_FOUND");
        }
        Optional<GioHang> gioHang = gioHangRepository.findById(request.getGioHangId());
        if (gioHang.isEmpty()) {
            throw new ApiException("Gio hang not found", "NOT_FOUND");
        }
        Optional<ChiTietSanPham> chiTietSanPham = chiTietSanPhamRepository.findById(request.getChiTietSanPhamId());
        if (chiTietSanPham.isEmpty()) {
            throw new ApiException("Chi tiet san pham not found", "NOT_FOUND");
        }
        GioHangChiTiet gioHangChiTiet = optionalGioHangChiTiet.get();
        gioHangChiTiet.setGioHang(gioHang.get());
        gioHangChiTiet.setChiTietSanPham(chiTietSanPham.get());
        gioHangChiTiet.setSoLuong(request.getSoLuong());
        gioHangChiTiet.setDonGia(request.getDonGia());
        gioHangChiTiet.setNgayThem(request.getNgayThem());
        return gioHangChiTietRepository.save(gioHangChiTiet);
    }

    public void delete(UUID id) {
        if (!gioHangChiTietRepository.existsById(id)) {
            throw new ApiException("Not found", "NOT_FOUND");
        }
        gioHangChiTietRepository.deleteById(id);
    }
}

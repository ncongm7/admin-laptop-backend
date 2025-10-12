package com.example.backendlaptop.service.PhanQuyenSer;


import com.example.backendlaptop.dto.phanQuyenDto.nhanVien.NhanVienDto;
import com.example.backendlaptop.dto.phanQuyenDto.nhanVien.NhanVienRequest;

import com.example.backendlaptop.entity.NhanVien;
import com.example.backendlaptop.repository.NhanVienRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class NhanVienService {
    @Autowired
    private NhanVienRepository nhanVienRepository;


    //    tìm full danh sach
    public List<NhanVienDto> findAllNV() {
        return nhanVienRepository.findNhanViensBy();
    }

    //tìm 1 khavh hang
    public NhanVien getOne(UUID id) {
        return nhanVienRepository.findById(id).orElse(null);
    }

    public void addNV(NhanVienRequest nhanVienRequest) {
        NhanVien nhanVien = new NhanVien();
        nhanVien.setMaNhanVien(nhanVienRequest.getMaNhanVien());
        nhanVien.setHoTen(nhanVienRequest.getHoTen());
        nhanVien.setSoDienThoai(nhanVienRequest.getSoDienThoai());
        nhanVien.setEmail(nhanVienRequest.getEmail());
        nhanVien.setGioiTinh(nhanVienRequest.getGioiTinh());
        nhanVien.setAnhNhanVien(nhanVienRequest.getAnhNhanVien());
        nhanVien.setChucVu(nhanVienRequest.getChucVu());
        nhanVien.setDiaChi(nhanVienRequest.getDiaChi());
        nhanVien.setDanhGia(nhanVienRequest.getDanhGia());
        nhanVien.setTrangThai(nhanVienRequest.getTrangThai());
        nhanVienRepository.save(nhanVien);

    }

    public void deleteNV(UUID id) {
        nhanVienRepository.deleteById(id);
    }
    public void suaNV(UUID id, NhanVienRequest nhanVienRequest) {
        var nhanVien = nhanVienRepository.findById(id).orElseThrow(()-> new RuntimeException("Nhân viên k tồn tại"));
        nhanVien.setMaNhanVien(nhanVienRequest.getMaNhanVien());
        nhanVien.setHoTen(nhanVienRequest.getHoTen());
        nhanVien.setSoDienThoai(nhanVienRequest.getSoDienThoai());
        nhanVien.setEmail(nhanVienRequest.getEmail());
        nhanVien.setGioiTinh(nhanVienRequest.getGioiTinh());
        nhanVien.setAnhNhanVien(nhanVienRequest.getAnhNhanVien());
        nhanVien.setChucVu(nhanVienRequest.getChucVu());
        nhanVien.setDiaChi(nhanVienRequest.getDiaChi());
        nhanVien.setDanhGia(nhanVienRequest.getDanhGia());
        nhanVien.setTrangThai(nhanVienRequest.getTrangThai());
        nhanVienRepository.save(nhanVien);

    }

    public Page<NhanVienDto> phanTrangNV(Integer pageNo, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<NhanVienDto> khachHangDtoPage =nhanVienRepository.phanTrangNV(pageable);
        return khachHangDtoPage;
    }
}

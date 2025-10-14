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
        // Normalize
        String ma = nhanVienRequest.getMaNhanVien() != null ? nhanVienRequest.getMaNhanVien().trim() : null;
        String email = nhanVienRequest.getEmail() != null ? nhanVienRequest.getEmail().trim().toLowerCase() : null;
        String sdt = nhanVienRequest.getSoDienThoai() != null ? nhanVienRequest.getSoDienThoai().trim() : null;

        if (ma == null || ma.isEmpty()) {
            throw new IllegalArgumentException("Mã nhân viên không được để trống");
        }
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Email không được để trống");
        }
        if (sdt == null || sdt.isEmpty()) {
            throw new IllegalArgumentException("Số điện thoại không được để trống");
        }

        // Duplicate checks
        if (nhanVienRepository.existsByMaNhanVienIgnoreCase(ma)) {
            throw new IllegalArgumentException("Mã nhân viên đã tồn tại");
        }
        if (nhanVienRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Email đã tồn tại");
        }
        if (nhanVienRepository.existsBySoDienThoai(sdt)) {
            throw new IllegalArgumentException("Số điện thoại đã tồn tại");
        }

        NhanVien nhanVien = new NhanVien();
        nhanVien.setMaNhanVien(ma);
        nhanVien.setHoTen(nhanVienRequest.getHoTen());
        nhanVien.setSoDienThoai(sdt);
        nhanVien.setEmail(email);
        nhanVien.setGioiTinh(nhanVienRequest.getGioiTinh());
        nhanVien.setAnhNhanVien(nhanVienRequest.getAnhNhanVien());
        nhanVien.setChucVu(nhanVienRequest.getChucVu());
        nhanVien.setDiaChi(nhanVienRequest.getDiaChi());
        nhanVien.setDanhGia(nhanVienRequest.getDanhGia());
        nhanVien.setTrangThai(nhanVienRequest.getTrangThai());
        nhanVienRepos
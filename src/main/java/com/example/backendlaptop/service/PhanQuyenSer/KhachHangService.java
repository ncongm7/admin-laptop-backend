package com.example.backendlaptop.service.PhanQuyenSer;

import com.example.backendlaptop.dto.phanQuyenDto.khachHang.KhachHangDto;
import com.example.backendlaptop.dto.phanQuyenDto.khachHang.KhachHangRequest;
import com.example.backendlaptop.entity.KhachHang;
import com.example.backendlaptop.repository.KhachHangRepository;
import com.example.backendlaptop.repository.TaiKhoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class KhachHangService {

    @Autowired
    private KhachHangRepository khachHangRepository;
    @Autowired
    private TaiKhoanRepository taiKhoanRepository;

    //    tìm full danh sach
    public List<KhachHangDto> findAllKH() {
        return khachHangRepository.findAllKhachHang();
    }

    //tìm 1 khavh hang
    public KhachHang getOne(UUID id) {
        return khachHangRepository.findById(id).orElse(null);
    }

    public void addKH(KhachHangRequest khachHangRequest) {
        var khachHang = new KhachHang();
        khachHang.setMaKhachHang(khachHangRequest.getMaKhachHang());
        khachHang.setHoTen(khachHangRequest.getHoTen());
        khachHang.setSoDienThoai(khachHangRequest.getSoDienThoai());
        khachHang.setEmail(khachHangRequest.getEmail());
        khachHang.setGioiTinh(khachHangRequest.getGioiTinh());
        khachHang.setNgaySinh(khachHangRequest.getNgaySinh());
        khachHang.setTrangThai(khachHangRequest.getTrangThai());

        khachHangRepository.save(khachHang);

    }

    public void updateKH( UUID id, KhachHangRequest khachHangRequest) {
//        tìm id cânf xoá
        var khachHang =khachHangRepository.findById(id).orElseThrow(()-> new RuntimeException("Khách hàng k tồn tại"));
        khachHang.setMaKhachHang(khachHangRequest.getMaKhachHang());
        khachHang.setHoTen(khachHangRequest.getHoTen());
        khachHang.setSoDienThoai(khachHangRequest.getSoDienThoai());
        khachHang.setEmail(khachHangRequest.getEmail());
        khachHang.setGioiTinh(khachHangRequest.getGioiTinh());
        khachHang.setNgaySinh(khachHangRequest.getNgaySinh());
        khachHang.setTrangThai(khachHangRequest.getTrangThai());
        khachHangRepository.save(khachHang);
    }

    public void xoaKH(UUID id) {
        khachHangRepository.deleteById(id);
    }

    public Page<KhachHangDto> phanTrangKH(Integer pageNo, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<KhachHangDto> khachHangDtoPage =khachHangRepository.phanTrangKH(pageable);
        return khachHangDtoPage;
    }


}

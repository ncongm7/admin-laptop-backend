package com.example.backendlaptop.service;

import com.example.backendlaptop.dto.banhang.ThemSanPhamRequest;
import com.example.backendlaptop.entity.*;
import com.example.backendlaptop.expection.ApiException;
import com.example.backendlaptop.model.TrangThaiHoaDon;
import com.example.backendlaptop.repository.ChiTietSanPhamRepository;
import com.example.backendlaptop.repository.HoaDonChiTietRepository;
import com.example.backendlaptop.repository.HoaDonRepository;
import com.example.backendlaptop.repository.KhachHangRepository;
import com.example.backendlaptop.repository.NhanVienRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Service
public class BanHangTaiQuayService {

//    @Autowired
//    private HoaDonRepository hoaDonRepository;
//
//    @Autowired
//    private HoaDonChiTietRepository hoaDonChiTietRepository;
//
//    @Autowired
//    private ChiTietSanPhamRepository chiTietSanPhamRepository;
//
//    @Autowired
//    private NhanVienRepository nhanVienRepository;
//
//    @Autowired
//    private KhachHangRepository khachHangRepository;
//
//    @Transactional
//    public HoaDon taoHoaDonCho(UUID nhanVienId, UUID khachHangId) {
//        HoaDon hoaDon = new HoaDon();
//        hoaDon.setNgayTao(Instant.now());
//        hoaDon.setTrangThai(TrangThaiHoaDon.CHO_THANH_TOAN);
//        hoaDon.setHoaDonChiTiets(new HashSet<>());
//
//        NhanVien nv = nhanVienRepository.findById(nhanVienId)
//                .orElseThrow(() -> new ApiException("Không tìm thấy nhân viên với ID: " + nhanVienId, String.valueOf(HttpStatus.NOT_FOUND.value())));
//        hoaDon.setIdNhanVien(nv);
//
//        if (khachHangId != null) {
//            KhachHang kh = khachHangRepository.findById(khachHangId)
//                    .orElseThrow(() -> new ApiException("Không tìm thấy khách hàng với ID: " + khachHangId, String.valueOf(HttpStatus.NOT_FOUND.value())));
//            hoaDon.setIdKhachHang(kh);
//            hoaDon.setTenKhachHang(kh.getHoTen());
//        } else {
//            hoaDon.setTenKhachHang("Khách lẻ");
//        }
//
//        return hoaDonRepository.save(hoaDon);
//    }
//
//    public List<HoaDon> getDanhSachHoaDonCho() {
//        return hoaDonRepository.findByTrangThai(TrangThaiHoaDon.CHO_THANH_TOAN);
//    }
//
//    @Transactional
//    public HoaDon themSanPhamVaoHoaDon(UUID hoaDonId, ThemSanPhamRequest request) {
//        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
//                .orElseThrow(() -> new ApiException("Không tìm thấy hóa đơn với ID: " + hoaDonId, String.valueOf(HttpStatus.NOT_FOUND.value())));
//
//        if (hoaDon.getTrangThai() != TrangThaiHoaDon.CHO_THANH_TOAN) {
//            throw new ApiException("Chỉ có thể thêm sản phẩm vào hóa đơn đang chờ thanh toán", String.valueOf(HttpStatus.BAD_REQUEST.value()));
//        }
//
//        ChiTietSanPham ctsp = chiTietSanPhamRepository.findById(request.getChiTietSanPhamId())
//                .orElseThrow(() -> new ApiException("Không tìm thấy chi tiết sản phẩm với ID: " + request.getChiTietSanPhamId(), String.valueOf(HttpStatus.NOT_FOUND.value())));
//
//        int soLuongTon = ctsp.getSoLuongTon() != null ? ctsp.getSoLuongTon() : 0;
//        int soLuongTamGiu = ctsp.getSoLuongTamGiu() != null ? ctsp.getSoLuongTamGiu() : 0;
//
//        if ((soLuongTon - soLuongTamGiu) < request.getSoLuong()) {
//            throw new ApiException("Không đủ số lượng tồn kho cho sản phẩm: " + ctsp.getSanPham().getTenSanPham(), String.valueOf(HttpStatus.BAD_REQUEST.value()));
//        }
//
//        ctsp.setSoLuongTamGiu(soLuongTamGiu + request.getSoLuong());
//
//        HoaDonChiTiet hoaDonChiTiet = new HoaDonChiTiet();
//        hoaDonChiTiet.setHoaDon(hoaDon);
//        hoaDonChiTiet.setChiTietSanPham(ctsp);
//        hoaDonChiTiet.setSoLuong(request.getSoLuong());
//        hoaDonChiTiet.setDonGia(ctsp.getGiaBan());
//
//        hoaDon.getHoaDonChiTiets().add(hoaDonChiTiet);
//        hoaDon.setTongTien(tinhLaiTongTien(hoaDon));
//
//        chiTietSanPhamRepository.save(ctsp);
//        return hoaDonRepository.save(hoaDon);
//    }
//
//    @Transactional
//    public HoaDon xoaSanPhamKhoiHoaDon(UUID hoaDonChiTietId) {
//        HoaDonChiTiet hdct = hoaDonChiTietRepository.findById(hoaDonChiTietId)
//                .orElseThrow(() -> new ApiException("Không tìm thấy sản phẩm trong hóa đơn với ID: " + hoaDonChiTietId, String.valueOf(HttpStatus.NOT_FOUND.value())));
//
//        HoaDon hoaDon = hdct.getHoaDon();
//        if (hoaDon.getTrangThai() != TrangThaiHoaDon.CHO_THANH_TOAN) {
//            throw new ApiException("Không thể xóa sản phẩm khỏi hóa đơn đã xử lý", String.valueOf(HttpStatus.BAD_REQUEST.value()));
//        }
//
//        ChiTietSanPham ctsp = hdct.getChiTietSanPham();
//        int soLuongTamGiu = ctsp.getSoLuongTamGiu() != null ? ctsp.getSoLuongTamGiu() : 0;
//        ctsp.setSoLuongTamGiu(soLuongTamGiu - hdct.getSoLuong());
//
//        hoaDon.getHoaDonChiTiets().remove(hdct);
//        hoaDon.setTongTien(tinhLaiTongTien(hoaDon));
//
//        chiTietSanPhamRepository.save(ctsp);
//        hoaDonChiTietRepository.delete(hdct);
//
//        return hoaDonRepository.save(hoaDon);
//    }
//
//    @Transactional
//    public HoaDon huyHoaDon(UUID hoaDonId) {
//        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
//                .orElseThrow(() -> new ApiException("Không tìm thấy hóa đơn với ID: " + hoaDonId, String.valueOf(HttpStatus.NOT_FOUND.value())));
//
//        if (hoaDon.getTrangThai() != TrangThaiHoaDon.CHO_THANH_TOAN) {
//            throw new ApiException("Chỉ có thể hủy hóa đơn đang chờ thanh toán", String.valueOf(HttpStatus.BAD_REQUEST.value()));
//        }
//
//        for (HoaDonChiTiet hdct : hoaDon.getHoaDonChiTiets()) {
//            ChiTietSanPham ctsp = hdct.getChiTietSanPham();
//            int soLuongTamGiu = ctsp.getSoLuongTamGiu() != null ? ctsp.getSoLuongTamGiu() : 0;
//            ctsp.setSoLuongTamGiu(soLuongTamGiu - hdct.getSoLuong());
//            chiTietSanPhamRepository.save(ctsp);
//        }
//
//        hoaDon.setTrangThai(TrangThaiHoaDon.DA_HUY);
//        return hoaDonRepository.save(hoaDon);
//    }
//
//    @Transactional
//    public HoaDon thanhToanHoaDon(UUID hoaDonId) {
//        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
//                .orElseThrow(() -> new ApiException("Không tìm thấy hóa đơn với ID: " + hoaDonId, String.valueOf(HttpStatus.NOT_FOUND.value())));
//
//        if (hoaDon.getTrangThai() != TrangThaiHoaDon.CHO_THANH_TOAN) {
//            throw new ApiException("Hóa đơn này không ở trạng thái chờ thanh toán", String.valueOf(HttpStatus.BAD_REQUEST.value()));
//        }
//
//        if (hoaDon.getHoaDonChiTiets().isEmpty()) {
//            throw new ApiException("Không thể thanh toán hóa đơn trống", String.valueOf(HttpStatus.BAD_REQUEST.value()));
//        }
//
//        for (HoaDonChiTiet hdct : hoaDon.getHoaDonChiTiets()) {
//            ChiTietSanPham ctsp = hdct.getChiTietSanPham();
//
//            int soLuongTon = ctsp.getSoLuongTon() != null ? ctsp.getSoLuongTon() : 0;
//            int soLuongTamGiu = ctsp.getSoLuongTamGiu() != null ? ctsp.getSoLuongTamGiu() : 0;
//            int soLuongMua = hdct.getSoLuong();
//
//            ctsp.setSoLuongTamGiu(soLuongTamGiu - soLuongMua);
//            ctsp.setSoLuongTon(soLuongTon - soLuongMua);
//            chiTietSanPhamRepository.save(ctsp);
//        }
//
//        hoaDon.setTrangThai(TrangThaiHoaDon.DA_THANH_TOAN);
//        hoaDon.setNgayThanhToan(Instant.now());
//        return hoaDonRepository.save(hoaDon);
//    }
//
//    private BigDecimal tinhLaiTongTien(HoaDon hoaDon) {
//        return hoaDon.getHoaDonChiTiets().stream()
//                .map(hdct -> hdct.getDonGia().multiply(new BigDecimal(hdct.getSoLuong())))
//                .reduce(BigDecimal.ZERO, BigDecimal::add);
//    }
}
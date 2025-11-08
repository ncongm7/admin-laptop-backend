package com.example.backendlaptop.service.banhang;

import com.example.backendlaptop.dto.giohang.customer.*;
import com.example.backendlaptop.entity.*;
import com.example.backendlaptop.expection.ApiException;
import com.example.backendlaptop.model.response.phieugiamgia.PhieuGiamGiaResponse;
import com.example.backendlaptop.repository.*;
import com.example.backendlaptop.repository.banhang.GioHangChiTietRepository;
import com.example.backendlaptop.repository.banhang.GioHangRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CustomerGioHangService {

    @Autowired
    private GioHangRepository gioHangRepository;

    @Autowired
    private GioHangChiTietRepository gioHangChiTietRepository;

    @Autowired
    private ChiTietSanPhamRepository chiTietSanPhamRepository;

    @Autowired
    private KhachHangRepository khachHangRepository;

    @Autowired
    private SerialRepository serialRepository;

    @Autowired
    private HinhAnhRepository hinhAnhRepository;

    /**
     * Lấy giỏ hàng của khách hàng
     */
    public CartResponse getCart(UUID khachHangId) {
        // Tìm hoặc tạo giỏ hàng cho khách hàng
        GioHang gioHang = gioHangRepository.findByKhachHangId(khachHangId)
                .orElseGet(() -> createNewCart(khachHangId));

        // Lấy danh sách items trong giỏ
        List<GioHangChiTiet> chiTietList = gioHangChiTietRepository.findByGioHangId(gioHang.getId());
        
        List<CartItemResponse> items = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (GioHangChiTiet chiTiet : chiTietList) {
            CartItemResponse item = convertToCartItemResponse(chiTiet);
            items.add(item);
            subtotal = subtotal.add(item.getSubtotal());
        }

        // Tạo response
        CartResponse response = new CartResponse();
        response.setId(gioHang.getId());
        response.setItems(items);
        response.setSubtotal(subtotal);
        response.setDiscount(BigDecimal.ZERO);
        response.setShippingFee(BigDecimal.ZERO); // Miễn phí vận chuyển
        response.setTotal(subtotal);
        response.setAppliedVoucher(null);
        response.setAvailablePoints(0); // TODO: Implement points logic

        return response;
    }

    /**
     * Thêm sản phẩm vào giỏ hàng
     */
    @Transactional
    public CartResponse addToCart(UUID khachHangId, AddToCartRequest request) {
        // Validate sản phẩm
        ChiTietSanPham ctsp = chiTietSanPhamRepository.findById(request.getCtspId())
                .orElseThrow(() -> new ApiException("Sản phẩm không tồn tại", "NOT_FOUND"));

        // Kiểm tra tồn kho
        int availableQuantity = getAvailableQuantity(ctsp);
        if (availableQuantity < request.getQuantity()) {
            throw new ApiException("Số lượng sản phẩm không đủ. Còn lại: " + availableQuantity, "INSUFFICIENT_STOCK");
        }

        // Tìm hoặc tạo giỏ hàng
        GioHang gioHang = gioHangRepository.findByKhachHangId(khachHangId)
                .orElseGet(() -> createNewCart(khachHangId));

        // Kiểm tra xem sản phẩm đã có trong giỏ chưa
        GioHangChiTiet existingItem = gioHangChiTietRepository
                .findByGioHangIdAndChiTietSanPhamId(gioHang.getId(), request.getCtspId())
                .orElse(null);

        if (existingItem != null) {
            // Cập nhật số lượng
            int newQuantity = existingItem.getSoLuong() + request.getQuantity();
            if (newQuantity > availableQuantity) {
                throw new ApiException("Số lượng sản phẩm không đủ. Còn lại: " + availableQuantity, "INSUFFICIENT_STOCK");
            }
            existingItem.setSoLuong(newQuantity);
            gioHangChiTietRepository.save(existingItem);
        } else {
            // Thêm mới
            GioHangChiTiet chiTiet = new GioHangChiTiet();
            chiTiet.setId(UUID.randomUUID());
            chiTiet.setGioHang(gioHang);
            chiTiet.setChiTietSanPham(ctsp);
            chiTiet.setSoLuong(request.getQuantity());
            chiTiet.setDonGia(ctsp.getGiaBan());
            chiTiet.setNgayThem(Instant.now());
            gioHangChiTietRepository.save(chiTiet);
        }

        // Cập nhật thời gian giỏ hàng
        gioHang.setNgayCapNhat(Instant.now());
        gioHangRepository.save(gioHang);

        return getCart(khachHangId);
    }

    /**
     * Cập nhật số lượng sản phẩm trong giỏ
     */
    @Transactional
    public CartResponse updateCartItem(UUID khachHangId, UUID itemId, UpdateCartItemRequest request) {
        GioHangChiTiet chiTiet = gioHangChiTietRepository.findById(itemId)
                .orElseThrow(() -> new ApiException("Item không tồn tại trong giỏ hàng", "NOT_FOUND"));

        // Verify ownership
        if (!chiTiet.getGioHang().getKhachHang().getId().equals(khachHangId)) {
            throw new ApiException("Unauthorized", "UNAUTHORIZED");
        }

        // Kiểm tra tồn kho
        int availableQuantity = getAvailableQuantity(chiTiet.getChiTietSanPham());
        if (request.getQuantity() > availableQuantity) {
            throw new ApiException("Số lượng sản phẩm không đủ. Còn lại: " + availableQuantity, "INSUFFICIENT_STOCK");
        }

        // Cập nhật số lượng
        chiTiet.setSoLuong(request.getQuantity());
        gioHangChiTietRepository.save(chiTiet);

        // Cập nhật thời gian giỏ hàng
        GioHang gioHang = chiTiet.getGioHang();
        gioHang.setNgayCapNhat(Instant.now());
        gioHangRepository.save(gioHang);

        return getCart(khachHangId);
    }

    /**
     * Xóa sản phẩm khỏi giỏ hàng
     */
    @Transactional
    public CartResponse removeCartItem(UUID khachHangId, UUID itemId) {
        GioHangChiTiet chiTiet = gioHangChiTietRepository.findById(itemId)
                .orElseThrow(() -> new ApiException("Item không tồn tại trong giỏ hàng", "NOT_FOUND"));

        // Verify ownership
        if (!chiTiet.getGioHang().getKhachHang().getId().equals(khachHangId)) {
            throw new ApiException("Unauthorized", "UNAUTHORIZED");
        }

        gioHangChiTietRepository.delete(chiTiet);

        // Cập nhật thời gian giỏ hàng
        GioHang gioHang = chiTiet.getGioHang();
        gioHang.setNgayCapNhat(Instant.now());
        gioHangRepository.save(gioHang);

        return getCart(khachHangId);
    }

    /**
     * Xóa toàn bộ giỏ hàng
     */
    @Transactional
    public void clearCart(UUID khachHangId) {
        GioHang gioHang = gioHangRepository.findByKhachHangId(khachHangId)
                .orElse(null);

        if (gioHang != null) {
            gioHangChiTietRepository.deleteByGioHangId(gioHang.getId());
            gioHang.setNgayCapNhat(Instant.now());
            gioHangRepository.save(gioHang);
        }
    }

    // ========== HELPER METHODS ==========

    /**
     * Tạo giỏ hàng mới cho khách hàng
     */
    private GioHang createNewCart(UUID khachHangId) {
        KhachHang khachHang = khachHangRepository.findById(khachHangId)
                .orElseThrow(() -> new ApiException("Khách hàng không tồn tại", "NOT_FOUND"));

        GioHang gioHang = new GioHang();
        gioHang.setId(UUID.randomUUID());
        gioHang.setKhachHang(khachHang);
        gioHang.setNgayTao(Instant.now());
        gioHang.setNgayCapNhat(Instant.now());
        gioHang.setTrangThaiGioHang(1); // Active

        return gioHangRepository.save(gioHang);
    }

    /**
     * Convert GioHangChiTiet entity sang CartItemResponse
     */
    private CartItemResponse convertToCartItemResponse(GioHangChiTiet chiTiet) {
        ChiTietSanPham ctsp = chiTiet.getChiTietSanPham();
        SanPham sanPham = ctsp.getSanPham();

        CartItemResponse item = new CartItemResponse();
        item.setId(chiTiet.getId());
        item.setCtspId(ctsp.getId());
        item.setTenSanPham(sanPham.getTenSanPham());
        item.setMaSanPham(sanPham.getMaSanPham());
        
        // Get main image from repository
        String imageUrl = hinhAnhRepository.findMainImageByCtspId(ctsp.getId())
                .map(HinhAnh::getUrl)
                .orElseGet(() -> {
                    // If no main image, get first image
                    return hinhAnhRepository.findByIdSpctId(ctsp.getId()).stream()
                            .findFirst()
                            .map(HinhAnh::getUrl)
                            .orElse("https://via.placeholder.com/80");
                });
        item.setImageUrl(imageUrl);
        
        // Build variant name
        StringBuilder variantName = new StringBuilder();
        if (ctsp.getMauSac() != null) {
            variantName.append(ctsp.getMauSac().getTenMau());
        }
        if (ctsp.getCpu() != null) {
            if (variantName.length() > 0) variantName.append(" - ");
            variantName.append(ctsp.getCpu().getTenCpu());
        }
        if (ctsp.getRam() != null) {
            if (variantName.length() > 0) variantName.append("/");
            variantName.append(ctsp.getRam().getTenRam());
        }
        if (ctsp.getOCung() != null) {
            if (variantName.length() > 0) variantName.append("/");
            variantName.append(ctsp.getOCung().getDungLuong());
        }
        item.setVariantName(variantName.toString());
        
        item.setPrice(chiTiet.getDonGia());
        item.setQuantity(chiTiet.getSoLuong());
        item.setMaxQuantity(getAvailableQuantity(ctsp));
        item.setSubtotal(chiTiet.getDonGia().multiply(BigDecimal.valueOf(chiTiet.getSoLuong())));
        item.setSelected(false); // Default not selected

        return item;
    }

    /**
     * Lấy số lượng tồn kho có sẵn
     */
    private int getAvailableQuantity(ChiTietSanPham ctsp) {
        // Count available serials (trangThai = 1 means available)
        return serialRepository.countByCtspIdAndTrangThai(ctsp.getId(), 1);
    }
}


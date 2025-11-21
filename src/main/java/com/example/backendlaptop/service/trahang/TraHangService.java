package com.example.backendlaptop.service.trahang;

import com.example.backendlaptop.dto.trahang.KiemTraDieuKienResponse;
import com.example.backendlaptop.dto.trahang.YeuCauTraHangResponse;
import com.example.backendlaptop.entity.*;
import com.example.backendlaptop.expection.ApiException;
import com.example.backendlaptop.model.request.trahang.TaoYeuCauTraHangRequest;
import com.example.backendlaptop.repository.*;
import com.example.backendlaptop.repository.banhang.HoaDonChiTietRepository;
import com.example.backendlaptop.repository.banhang.HoaDonRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TraHangService {

    private final YeuCauTraHangRepository yeuCauTraHangRepository;
    private final ChiTietTraHangRepository chiTietTraHangRepository;
    private final LichSuTraHangRepository lichSuTraHangRepository;
    private final HoaDonRepository hoaDonRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final SerialDaBanRepository serialDaBanRepository;
    private final KhachHangRepository khachHangRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Kiểm tra điều kiện trả hàng/bảo hành
     * Tính số ngày sau khi mua và gợi ý loại yêu cầu
     */
    public KiemTraDieuKienResponse kiemTraDieuKien(UUID idHoaDon) {
        try {
            System.out.println("🔍 [TraHangService] Kiểm tra điều kiện cho hóa đơn: " + idHoaDon);

            // 1. Tìm hóa đơn
            HoaDon hoaDon = hoaDonRepository.findById(idHoaDon)
                    .orElseThrow(() -> new ApiException("Không tìm thấy hóa đơn với ID: " + idHoaDon, "NOT_FOUND"));

            // 2. Tính số ngày sau khi mua
            Instant ngayMua = hoaDon.getNgayTao();
            if (ngayMua == null) {
                throw new ApiException("Hóa đơn không có ngày tạo", "INVALID_DATE");
            }

            Instant now = Instant.now();
            long soNgay = Duration.between(ngayMua, now).toDays();

            // 3. Lấy danh sách sản phẩm trong hóa đơn
            List<KiemTraDieuKienResponse.SanPhamInfo> danhSachSanPham = new ArrayList<>();
            if (hoaDon.getHoaDonChiTiets() != null) {
                for (HoaDonChiTiet hdct : hoaDon.getHoaDonChiTiets()) {
                    KiemTraDieuKienResponse.SanPhamInfo spInfo = new KiemTraDieuKienResponse.SanPhamInfo();
                    spInfo.setIdHoaDonChiTiet(hdct.getId());
                    spInfo.setSoLuong(hdct.getSoLuong());

                    if (hdct.getChiTietSanPham() != null) {
                        ChiTietSanPham ctsp = hdct.getChiTietSanPham();
                        spInfo.setMaCtsp(ctsp.getMaCtsp());
                        if (ctsp.getSanPham() != null) {
                            spInfo.setTenSanPham(ctsp.getSanPham().getTenSanPham());
                        }

                        // Kiểm tra xem có serial không
                        boolean coSerial = serialDaBanRepository.findByIdHoaDonChiTiet_Id(hdct.getId()).size() > 0;
                        spInfo.setCoSerial(coSerial);
                    }

                    danhSachSanPham.add(spInfo);
                }
            }

            // 4. Lấy danh sách serial/IMEI
            List<KiemTraDieuKienResponse.SerialInfo> danhSachSerial = new ArrayList<>();
            if (hoaDon.getHoaDonChiTiets() != null) {
                for (HoaDonChiTiet hdct : hoaDon.getHoaDonChiTiets()) {
                    List<SerialDaBan> serials = serialDaBanRepository.findByIdHoaDonChiTiet_Id(hdct.getId());
                    for (SerialDaBan sdb : serials) {
                        KiemTraDieuKienResponse.SerialInfo serialInfo = new KiemTraDieuKienResponse.SerialInfo();
                        serialInfo.setIdSerialDaBan(sdb.getId());
                        serialInfo.setIdHoaDonChiTiet(hdct.getId());
                        if (sdb.getIdSerial() != null) {
                            serialInfo.setSerialNo(sdb.getIdSerial().getSerialNo());
                            serialInfo.setImei(sdb.getIdSerial().getSerialNo()); // Giả sử serialNo cũng là IMEI
                        }
                        danhSachSerial.add(serialInfo);
                    }
                }
            }

            // 5. Tạo response
            KiemTraDieuKienResponse response = new KiemTraDieuKienResponse();
            response.setIdHoaDon(hoaDon.getId());
            response.setMaHoaDon(hoaDon.getMa());
            response.setNgayMua(ngayMua);
            response.setSoNgaySauMua((int) soNgay);

            // Logic gợi ý:
            // - Đổi trả: ≤ 7 ngày và hỏng hóc
            // - Bảo hành: > 7 ngày hoặc hỏng hóc
            boolean coTheTraHang = soNgay <= 7;
            boolean coTheBaoHanh = soNgay > 7;

            response.setCoTheTraHang(coTheTraHang);
            response.setCoTheBaoHanh(coTheBaoHanh);

            // Gợi ý: Nếu ≤ 7 ngày thì gợi ý "Đổi trả", nếu > 7 ngày thì gợi ý "Bảo hành"
            if (soNgay <= 7) {
                response.setGoiY("Đổi trả (nếu sản phẩm hỏng hóc)");
            } else {
                response.setGoiY("Bảo hành");
            }

            response.setDanhSachSanPham(danhSachSanPham);
            response.setDanhSachSerial(danhSachSerial);

            System.out.println("✅ [TraHangService] Kiểm tra điều kiện thành công. Số ngày: " + soNgay);
            return response;
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("❌ [TraHangService] Lỗi khi kiểm tra điều kiện: " + e.getMessage());
            e.printStackTrace();
            throw new ApiException("Lỗi khi kiểm tra điều kiện: " + e.getMessage(), "CHECK_CONDITION_ERROR");
        }
    }

    /**
     * Tạo yêu cầu trả hàng
     */
    @Transactional
    public YeuCauTraHangResponse taoYeuCau(TaoYeuCauTraHangRequest request, List<MultipartFile> hinhAnhFiles) {
        try {
            System.out.println("📝 [TraHangService] Tạo yêu cầu trả hàng cho hóa đơn: " + request.getIdHoaDon());

            // 1. Validate và lấy các entity
            HoaDon hoaDon = hoaDonRepository.findById(request.getIdHoaDon())
                    .orElseThrow(() -> new ApiException("Không tìm thấy hóa đơn", "NOT_FOUND"));

            KhachHang khachHang = khachHangRepository.findById(request.getIdKhachHang())
                    .orElseThrow(() -> new ApiException("Không tìm thấy khách hàng", "NOT_FOUND"));

            // Kiểm tra quyền: khách hàng chỉ có thể tạo yêu cầu cho hóa đơn của mình
            if (!hoaDon.getIdKhachHang().getId().equals(request.getIdKhachHang())) {
                throw new ApiException("Bạn không có quyền tạo yêu cầu cho hóa đơn này", "UNAUTHORIZED");
            }

            HoaDonChiTiet hoaDonChiTiet = hoaDonChiTietRepository.findById(request.getIdHoaDonChiTiet())
                    .orElseThrow(() -> new ApiException("Không tìm thấy chi tiết hóa đơn", "NOT_FOUND"));

            // Kiểm tra chi tiết hóa đơn thuộc về hóa đơn này
            if (!hoaDonChiTiet.getHoaDon().getId().equals(request.getIdHoaDon())) {
                throw new ApiException("Chi tiết hóa đơn không thuộc về hóa đơn này", "INVALID_DETAIL");
            }

            SerialDaBan serialDaBan = null;
            if (request.getIdSerialDaBan() != null) {
                serialDaBan = serialDaBanRepository.findById(request.getIdSerialDaBan())
                        .orElseThrow(() -> new ApiException("Không tìm thấy serial", "NOT_FOUND"));
            }

            // 2. Tính số ngày sau khi mua
            Instant ngayMua = hoaDon.getNgayTao();
            Instant now = Instant.now();
            long soNgay = Duration.between(ngayMua, now).toDays();

            // 3. Validate logic: Nếu ≤ 7 ngày và hỏng -> Đổi trả, nếu > 7 ngày -> Bảo hành
            if (request.getLoaiYeuCau() == 0) { // Đổi trả
                if (soNgay > 7) {
                    throw new ApiException("Chỉ có thể đổi trả trong vòng 7 ngày kể từ ngày mua", "INVALID_RETURN_PERIOD");
                }
                if ("Tốt".equals(request.getTinhTrangLucTra())) {
                    throw new ApiException("Sản phẩm còn tốt không thể đổi trả. Vui lòng chọn bảo hành nếu cần", "INVALID_CONDITION");
                }
            }

            // 4. Upload ảnh minh chứng
            List<String> hinhAnhUrls = new ArrayList<>();
            if (hinhAnhFiles != null && !hinhAnhFiles.isEmpty()) {
                String uploadDir = "uploads/tra-hang/";
                File directory = new File(uploadDir);
                if (!directory.exists()) {
                    directory.mkdirs();
                }

                for (MultipartFile file : hinhAnhFiles) {
                    if (!file.isEmpty()) {
                        try {
                            String originalFilename = file.getOriginalFilename();
                            String extension = originalFilename != null && originalFilename.contains(".")
                                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                                    : ".jpg";
                            String filename = UUID.randomUUID().toString() + extension;
                            String filePath = uploadDir + filename;

                            file.transferTo(new File(filePath));
                            hinhAnhUrls.add("/" + filePath);
                        } catch (IOException e) {
                            System.err.println("❌ [TraHangService] Lỗi khi upload ảnh: " + e.getMessage());
                            // Không throw exception, chỉ log lỗi
                        }
                    }
                }
            }

            // 5. Tạo mã yêu cầu: YCTR-YYYYMMDD-XXX
            String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            long count = yeuCauTraHangRepository.count();
            String maYeuCau = String.format("YCTR-%s-%03d", dateStr, (count % 1000) + 1);

            // 6. Tạo entity YeuCauTraHang
            YeuCauTraHang yeuCau = new YeuCauTraHang();
            yeuCau.setId(UUID.randomUUID());
            yeuCau.setIdHoaDon(hoaDon);
            yeuCau.setIdKhachHang(khachHang);
            yeuCau.setMaYeuCau(maYeuCau);
            yeuCau.setLyDoTraHang(request.getLyDoTraHang());
            yeuCau.setNgayMua(ngayMua);
            yeuCau.setNgayYeuCau(now);
            yeuCau.setSoNgaySauMua((int) soNgay);
            yeuCau.setLoaiYeuCau(request.getLoaiYeuCau());
            yeuCau.setTrangThai(0); // Chờ duyệt
            yeuCau.setNgayTao(now);
            yeuCau.setNgaySua(now);

            // 7. Lưu YeuCauTraHang
            YeuCauTraHang savedYeuCau = yeuCauTraHangRepository.save(yeuCau);

            // 8. Tạo ChiTietTraHang
            ChiTietTraHang chiTiet = new ChiTietTraHang();
            chiTiet.setId(UUID.randomUUID());
            chiTiet.setIdYeuCauTraHang(savedYeuCau);
            chiTiet.setIdHoaDonChiTiet(hoaDonChiTiet);
            if (serialDaBan != null) {
                chiTiet.setIdSerialDaBan(serialDaBan);
            }
            chiTiet.setSoLuong(request.getSoLuong());
            chiTiet.setDonGia(hoaDonChiTiet.getDonGia());
            chiTiet.setThanhTien(hoaDonChiTiet.getDonGia().multiply(new BigDecimal(request.getSoLuong())));
            chiTiet.setTinhTrangLucTra(request.getTinhTrangLucTra());
            chiTiet.setMoTaTinhTrang(request.getMoTaTinhTrang());
            
            // Lưu ảnh dưới dạng JSON array
            if (!hinhAnhUrls.isEmpty()) {
                try {
                    String hinhAnhJson = objectMapper.writeValueAsString(hinhAnhUrls);
                    chiTiet.setHinhAnh(hinhAnhJson);
                } catch (Exception e) {
                    // Fallback: lưu dạng comma-separated
                    chiTiet.setHinhAnh(String.join(",", hinhAnhUrls));
                }
            }
            
            chiTiet.setNgayTao(now);
            chiTietTraHangRepository.save(chiTiet);

            // 9. Tạo LichSuTraHang
            LichSuTraHang lichSu = new LichSuTraHang();
            lichSu.setId(UUID.randomUUID());
            lichSu.setIdYeuCauTraHang(savedYeuCau);
            lichSu.setHanhDong("CREATE");
            lichSu.setMoTa("Khách hàng tạo yêu cầu trả hàng");
            lichSu.setThoiGian(now);
            lichSuTraHangRepository.save(lichSu);

            System.out.println("✅ [TraHangService] Tạo yêu cầu trả hàng thành công: " + maYeuCau);
            return new YeuCauTraHangResponse(savedYeuCau);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("❌ [TraHangService] Lỗi khi tạo yêu cầu trả hàng: " + e.getMessage());
            e.printStackTrace();
            throw new ApiException("Lỗi khi tạo yêu cầu trả hàng: " + e.getMessage(), "CREATE_REQUEST_ERROR");
        }
    }
}

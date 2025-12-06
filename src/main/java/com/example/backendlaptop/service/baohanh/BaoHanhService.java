package com.example.backendlaptop.service.baohanh;

import com.example.backendlaptop.dto.trahang.KiemTraDieuKienResponse;
import com.example.backendlaptop.entity.HoaDon;
import com.example.backendlaptop.entity.HoaDonChiTiet;
import com.example.backendlaptop.entity.KhachHang;
import com.example.backendlaptop.entity.PhieuBaoHanh;
import com.example.backendlaptop.entity.SerialDaBan;
import com.example.backendlaptop.expection.ApiException;
import com.example.backendlaptop.model.request.baohanh.TaoYeuCauBaoHanhRequest;
import com.example.backendlaptop.model.response.baohanh.PhieuBaoHanhResponse;
import com.example.backendlaptop.repository.KhachHangRepository;
import com.example.backendlaptop.repository.PhieuBaoHanhRepository;
import com.example.backendlaptop.repository.SerialDaBanRepository;
import com.example.backendlaptop.repository.banhang.HoaDonChiTietRepository;
import com.example.backendlaptop.repository.banhang.HoaDonRepository;
import com.example.backendlaptop.entity.LichSuBaoHanh;
import com.example.backendlaptop.entity.NhanVien;
import com.example.backendlaptop.model.TrangThaiBaoHanh;
import com.example.backendlaptop.model.request.baohanh.BanGiaoRequest;
import com.example.backendlaptop.model.request.baohanh.ChiPhiPhatSinhRequest;
import com.example.backendlaptop.model.request.baohanh.TiepNhanRequest;
import com.example.backendlaptop.repository.LichSuBaoHanhRepository;
import com.example.backendlaptop.repository.LyDoBaoHanhRepository;
import com.example.backendlaptop.repository.NhanVienRepository;
import com.example.backendlaptop.service.EmailService;
import com.example.backendlaptop.service.trahang.TraHangService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BaoHanhService {

    private final TraHangService traHangService;
    private final PhieuBaoHanhRepository phieuBaoHanhRepository;
    private final HoaDonRepository hoaDonRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final KhachHangRepository khachHangRepository;
    private final SerialDaBanRepository serialDaBanRepository;
    private final EmailService emailService;
    private final NhanVienRepository nhanVienRepository;
    private final LichSuBaoHanhRepository lichSuBaoHanhRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public KiemTraDieuKienResponse kiemTraDieuKien(UUID idHoaDon) {
        return traHangService.kiemTraDieuKien(idHoaDon);
    }

    public List<PhieuBaoHanhResponse> getWarrantiesByInvoice(UUID idHoaDon) {
        List<PhieuBaoHanh> warranties = phieuBaoHanhRepository.findByHoaDonId(idHoaDon);
        return warranties.stream()
                .map(PhieuBaoHanhResponse::new)
                .toList();
    }

    @Transactional
    public PhieuBaoHanhResponse taoYeuCau(
            TaoYeuCauBaoHanhRequest request,
            List<MultipartFile> hinhAnhFiles
    ) {
        try {
            Instant now = Instant.now();

            HoaDon hoaDon = hoaDonRepository.findById(request.getIdHoaDon())
                    .orElseThrow(() -> new ApiException("Không tìm thấy hóa đơn", "NOT_FOUND"));

            KhachHang khachHang = khachHangRepository.findById(request.getIdKhachHang())
                    .orElseThrow(() -> new ApiException("Không tìm thấy khách hàng", "NOT_FOUND"));

            if (!hoaDon.getIdKhachHang().getId().equals(request.getIdKhachHang())) {
                throw new ApiException("Bạn không có quyền tạo bảo hành cho hóa đơn này", "UNAUTHORIZED");
            }

            HoaDonChiTiet hoaDonChiTiet = hoaDonChiTietRepository.findById(request.getIdHoaDonChiTiet())
                    .orElseThrow(() -> new ApiException("Không tìm thấy chi tiết hóa đơn", "NOT_FOUND"));

            if (!hoaDonChiTiet.getHoaDon().getId().equals(request.getIdHoaDon())) {
                throw new ApiException("Chi tiết hóa đơn không thuộc đơn hàng này", "INVALID_DETAIL");
            }

            SerialDaBan serialDaBan = resolveSerialDaBan(request, hoaDonChiTiet.getId());

            // Kiểm tra xem có bảo hành nào đang active (chưa hoàn thành) cho sản phẩm này không
            // Kiểm tra theo idHoaDonChiTiet thông qua serial
            List<PhieuBaoHanh> activeWarranties = phieuBaoHanhRepository.findByHoaDonChiTietAndNotCompleted(hoaDonChiTiet.getId());
            if (!activeWarranties.isEmpty()) {
                throw new ApiException(
                    "Sản phẩm này đang có bảo hành chưa hoàn thành (trạng thái: chờ xác nhận, xác nhận, hoặc từ chối). " +
                    "Vui lòng đợi bảo hành hiện tại hoàn thành trước khi tạo bảo hành mới.",
                    "WARRANTY_ALREADY_ACTIVE"
                );
            }

            List<String> uploadedImages = uploadImages(hinhAnhFiles);

            // Serial chỉ được thêm, không được xóa hay chuyển
            // Cho phép nhiều bảo hành cùng sử dụng một serial
            // Serial sẽ được lưu vào bảo hành mới bất kể đã được sử dụng hay chưa
            
            // Đảm bảo serial được set vào entity trước khi lưu
            System.out.println("SerialDaBan before save: " + (serialDaBan != null ? serialDaBan.getId() : "null"));

            PhieuBaoHanh phieuBaoHanh = new PhieuBaoHanh();
            phieuBaoHanh.setId(UUID.randomUUID());
            phieuBaoHanh.setIdKhachHang(khachHang);
            phieuBaoHanh.setIdSerialDaBan(serialDaBan); // Set serial vào entity
            phieuBaoHanh.setNgayBatDau(now);
            phieuBaoHanh.setNgayKetThuc(now.plus(365, ChronoUnit.DAYS));
            phieuBaoHanh.setTrangThaiBaoHanh(1);
            phieuBaoHanh.setMoTa(
                    request.getMoTaTinhTrang() != null && !request.getMoTaTinhTrang().isBlank()
                            ? request.getMoTaTinhTrang()
                            : request.getLyDoTraHang()
            );
            phieuBaoHanh.setChiPhi(BigDecimal.ZERO);
            phieuBaoHanh.setSoLanSuaChua(0);
            phieuBaoHanh.setIdHoaDonChiTiet(hoaDonChiTiet);
            phieuBaoHanh.setNgayTao(now);
            
            // Generate mã phiếu bảo hành
            String maPhieuBaoHanh = generateMaPhieuBaoHanh();
            phieuBaoHanh.setMaPhieuBaoHanh(maPhieuBaoHanh);

            if (!uploadedImages.isEmpty()) {
                try {
                    phieuBaoHanh.setHinhAnh(objectMapper.writeValueAsString(uploadedImages));
                } catch (Exception e) {
                    phieuBaoHanh.setHinhAnh(String.join(",", uploadedImages));
                }
            }

            // Verify serial is set before saving
            System.out.println("PhieuBaoHanh serial before save: " + 
                (phieuBaoHanh.getIdSerialDaBan() != null ? phieuBaoHanh.getIdSerialDaBan().getId() : "null"));
            
            // Lưu entity với serial
            PhieuBaoHanh saved = phieuBaoHanhRepository.save(phieuBaoHanh);
            
            // Flush để đảm bảo serial được lưu vào database ngay lập tức
            phieuBaoHanhRepository.flush();
            
            // Reload entity với relations để đảm bảo serial được load từ database
            PhieuBaoHanh reloaded = phieuBaoHanhRepository.findByIdWithRelations(saved.getId())
                    .orElse(saved);
            
            System.out.println("PhieuBaoHanh serial after save: " + 
                (reloaded.getIdSerialDaBan() != null ? reloaded.getIdSerialDaBan().getId() : "null"));
            
            // Tự động gửi email xác nhận
            try {
                String emailKhachHang = khachHang.getEmail();
                if (emailKhachHang != null && !emailKhachHang.isBlank()) {
                    emailService.guiEmailXacNhan(reloaded.getId(), emailKhachHang, 
                        khachHang.getHoTen(), maPhieuBaoHanh);
                }
            } catch (Exception e) {
                // Log lỗi nhưng không throw để không ảnh hưởng đến việc tạo phiếu
                System.err.println("Lỗi khi gửi email xác nhận: " + e.getMessage());
            }
            
            return new PhieuBaoHanhResponse(reloaded);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Lỗi khi tạo phiếu bảo hành: " + e.getMessage(), "CREATE_WARRANTY_ERROR");
        }
    }

    private SerialDaBan resolveSerialDaBan(TaoYeuCauBaoHanhRequest request, UUID idHoaDonChiTiet) {
        SerialDaBan serialDaBan = null;
        
        // Ưu tiên sử dụng serial được chỉ định trong request
        if (request.getIdSerialDaBan() != null) {
            serialDaBan = serialDaBanRepository.findById(request.getIdSerialDaBan())
                    .orElse(null);
            System.out.println("Resolved serial from request: " + 
                (serialDaBan != null ? serialDaBan.getId() : "null"));
        }
        
        // Nếu không có serial được chỉ định, lấy serial đầu tiên của hóa đơn chi tiết
        if (serialDaBan == null) {
            List<SerialDaBan> serials = serialDaBanRepository.findByIdHoaDonChiTiet_Id(idHoaDonChiTiet);
            if (!serials.isEmpty()) {
                serialDaBan = serials.get(0);
                System.out.println("Resolved serial from hoaDonChiTiet: " + serialDaBan.getId());
            } else {
                System.out.println("No serial found for hoaDonChiTiet: " + idHoaDonChiTiet);
            }
        }
        
        return serialDaBan;
    }

    private List<String> uploadImages(List<MultipartFile> files) throws IOException {
        List<String> urls = new ArrayList<>();
        if (files == null || files.isEmpty()) {
            return urls;
        }

        String uploadDir = "uploads/bao-hanh/";
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".jpg";
            String filename = UUID.randomUUID() + extension;
            String filePath = uploadDir + filename;
            file.transferTo(new File(filePath));
            urls.add("/" + filePath);
        }
        return urls;
    }

    private String generateMaPhieuBaoHanh() {
        String prefix = "WB";
        String dateStr = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        String sequence = String.format("%03d", (int) (Math.random() * 1000));
        return prefix + "-" + dateStr + "-" + sequence;
    }

    @Transactional
    public PhieuBaoHanhResponse tiepNhanSanPham(UUID idBaoHanh, TiepNhanRequest request) {
        PhieuBaoHanh phieuBaoHanh = phieuBaoHanhRepository.findById(idBaoHanh)
                .orElseThrow(() -> new ApiException("Không tìm thấy phiếu bảo hành", "NOT_FOUND"));

        NhanVien nhanVien = nhanVienRepository.findById(request.getIdNhanVienTiepNhan())
                .orElseThrow(() -> new ApiException("Không tìm thấy nhân viên", "NOT_FOUND"));

        // Tạo lịch sử bảo hành mới
        LichSuBaoHanh lichSu = new LichSuBaoHanh();
        lichSu.setId(UUID.randomUUID());
        lichSu.setIdBaoHanh(phieuBaoHanh);
        lichSu.setIdNhanVienTiepNhan(nhanVien);
        lichSu.setNgayTiepNhan(Instant.now());
        lichSu.setNgayNhanHang(Instant.now());
        lichSu.setTrangThai(TrangThaiBaoHanh.DA_TIEP_NHAN.getValue());
        lichSu.setMoTaLoi(request.getGhiChu());

        // Upload ảnh tình trạng
        if (request.getHinhAnhTinhTrang() != null && !request.getHinhAnhTinhTrang().isEmpty()) {
            try {
                List<String> imageUrls = uploadImages(request.getHinhAnhTinhTrang());
                lichSu.setHinhAnhTruoc(objectMapper.writeValueAsString(imageUrls));
            } catch (Exception e) {
                throw new ApiException("Lỗi khi upload ảnh: " + e.getMessage(), "UPLOAD_ERROR");
            }
        }

        lichSu = lichSuBaoHanhRepository.save(lichSu);

        // Cập nhật trạng thái phiếu bảo hành
        phieuBaoHanh.setTrangThaiBaoHanh(TrangThaiBaoHanh.DA_TIEP_NHAN.getValue());
        phieuBaoHanh.setNgayCapNhat(Instant.now());
        phieuBaoHanhRepository.save(phieuBaoHanh);

        return new PhieuBaoHanhResponse(phieuBaoHanh);
    }

    @Transactional
    public LichSuBaoHanh themChiPhiPhatSinh(UUID idLichSuBaoHanh, ChiPhiPhatSinhRequest request) {
        LichSuBaoHanh lichSu = lichSuBaoHanhRepository.findById(idLichSuBaoHanh)
                .orElseThrow(() -> new ApiException("Không tìm thấy lịch sử bảo hành", "NOT_FOUND"));

        lichSu.setChiPhiPhatSinh(request.getChiPhiPhatSinh());
        if (lichSu.getMoTaLoi() != null && !lichSu.getMoTaLoi().isEmpty()) {
            lichSu.setMoTaLoi(lichSu.getMoTaLoi() + "\nLý do chi phí: " + request.getLyDo());
        } else {
            lichSu.setMoTaLoi("Lý do chi phí: " + request.getLyDo());
        }

        lichSu = lichSuBaoHanhRepository.save(lichSu);

        // Gửi email thông báo chi phí
        try {
            String emailKhachHang = lichSu.getIdBaoHanh().getIdKhachHang().getEmail();
            String tenKhachHang = lichSu.getIdBaoHanh().getIdKhachHang().getHoTen();
            if (emailKhachHang != null && !emailKhachHang.isBlank()) {
                emailService.guiEmailChiPhi(idLichSuBaoHanh, emailKhachHang, tenKhachHang,
                    request.getChiPhiPhatSinh(), request.getLyDo());
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi gửi email chi phí: " + e.getMessage());
        }

        return lichSu;
    }

    @Transactional
    public PhieuBaoHanhResponse banGiaoSanPham(UUID idBaoHanh, BanGiaoRequest request) {
        PhieuBaoHanh phieuBaoHanh = phieuBaoHanhRepository.findById(idBaoHanh)
                .orElseThrow(() -> new ApiException("Không tìm thấy phiếu bảo hành", "NOT_FOUND"));

        // Tìm lịch sử bảo hành gần nhất
        List<LichSuBaoHanh> lichSuList = lichSuBaoHanhRepository.findByIdBaoHanh_IdOrderByNgayTiepNhanDesc(idBaoHanh);
        if (lichSuList.isEmpty()) {
            throw new ApiException("Không tìm thấy lịch sử bảo hành", "NOT_FOUND");
        }

        LichSuBaoHanh lichSu = lichSuList.get(0);

        if (request.getIdNhanVienBanGiao() != null) {
            NhanVien nhanVien = nhanVienRepository.findById(request.getIdNhanVienBanGiao())
                    .orElseThrow(() -> new ApiException("Không tìm thấy nhân viên", "NOT_FOUND"));
            lichSu.setIdNhanVienSuaChua(nhanVien);
        }

        lichSu.setNgayBanGiao(Instant.now());
        lichSu.setNgayHoanThanh(Instant.now());
        lichSu.setTrangThai(TrangThaiBaoHanh.HOAN_THANH.getValue());
        lichSu.setXacNhanKhachHang(request.getXacNhanKhachHang() != null ? request.getXacNhanKhachHang() : false);

        if (request.getGhiChu() != null) {
            lichSu.setMoTaLoi(lichSu.getMoTaLoi() != null ? 
                lichSu.getMoTaLoi() + "\nGhi chú bàn giao: " + request.getGhiChu() : 
                "Ghi chú bàn giao: " + request.getGhiChu());
        }

        // Upload ảnh sau sửa
        if (request.getHinhAnhSauSua() != null && !request.getHinhAnhSauSua().isEmpty()) {
            try {
                List<String> imageUrls = uploadImages(request.getHinhAnhSauSua());
                lichSu.setHinhAnhSau(objectMapper.writeValueAsString(imageUrls));
            } catch (Exception e) {
                throw new ApiException("Lỗi khi upload ảnh: " + e.getMessage(), "UPLOAD_ERROR");
            }
        }

        lichSuBaoHanhRepository.save(lichSu);

        // Cập nhật trạng thái phiếu bảo hành
        phieuBaoHanh.setTrangThaiBaoHanh(TrangThaiBaoHanh.HOAN_THANH.getValue());
        phieuBaoHanh.setNgayCapNhat(Instant.now());
        phieuBaoHanh = phieuBaoHanhRepository.save(phieuBaoHanh);

        // Gửi email hoàn thành
        try {
            String emailKhachHang = phieuBaoHanh.getIdKhachHang().getEmail();
            String tenKhachHang = phieuBaoHanh.getIdKhachHang().getHoTen();
            if (emailKhachHang != null && !emailKhachHang.isBlank()) {
                emailService.guiEmailHoanThanh(idBaoHanh, emailKhachHang, tenKhachHang,
                    phieuBaoHanh.getMaPhieuBaoHanh());
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi gửi email hoàn thành: " + e.getMessage());
        }

        return new PhieuBaoHanhResponse(phieuBaoHanh);
    }
}

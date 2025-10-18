package com.example.backendlaptop.service;

import com.example.backendlaptop.dto.sanpham.ChiTietSanPhamRequest;
import com.example.backendlaptop.dto.sanpham.ChiTietSanPhamResponse;
import com.example.backendlaptop.dto.sanpham.TaoBienTheSanPhamRequest;
import com.example.backendlaptop.entity.*;
import com.example.backendlaptop.expection.ApiException;
import com.example.backendlaptop.repository.*;
import com.example.backendlaptop.until.MapperUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ChiTietSanPhamService {
    
    private final ChiTietSanPhamRepository chiTietSanPhamRepository;
    private final SanPhamRepository sanPhamRepository;
    private final CpuRepository cpuRepository;
    private final GpuRepository gpuRepository;
    private final RamRepository ramRepository;
    private final OCungRepository oCungRepository;
    private final MauSacRepository mauSacRepository;
    private final LoaiManHinhRepository loaiManHinhRepository;
    private final PinRepository pinRepository;
    
    public ChiTietSanPhamResponse createChiTietSanPham(ChiTietSanPhamRequest request) {
        // Kiểm tra sản phẩm tồn tại
        SanPham sanPham = sanPhamRepository.findById(request.getIdSanPham())
                .orElseThrow(() -> new ApiException("Không tìm thấy sản phẩm với ID: " + request.getIdSanPham()));
        
        // Kiểm tra mã chi tiết sản phẩm đã tồn tại
        if (chiTietSanPhamRepository.existsByMaCtsp(request.getMaCtsp())) {
            throw new ApiException("Mã chi tiết sản phẩm đã tồn tại: " + request.getMaCtsp());
        }
        
        ChiTietSanPham chiTietSanPham = new ChiTietSanPham();
        chiTietSanPham.setSanPham(sanPham);
        chiTietSanPham.setMaCtsp(request.getMaCtsp());
        chiTietSanPham.setGiaBan(request.getGiaBan());
        chiTietSanPham.setGhiChu(request.getGhiChu());
        chiTietSanPham.setSoLuongTon(request.getSoLuongTon());
        chiTietSanPham.setSoLuongTamGiu(request.getSoLuongTamGiu());
        chiTietSanPham.setTrangThai(request.getTrangThai());
        
        // Set các thuộc tính đặc trưng
        setThuocTinhDacTrung(chiTietSanPham, request);
        
        ChiTietSanPham savedChiTietSanPham = chiTietSanPhamRepository.save(chiTietSanPham);
        return convertToResponse(savedChiTietSanPham);
    }
    
    public List<ChiTietSanPhamResponse> taoBienTheSanPham(TaoBienTheSanPhamRequest request) {
        // Kiểm tra sản phẩm tồn tại
        SanPham sanPham = sanPhamRepository.findById(request.getIdSanPham())
                .orElseThrow(() -> new ApiException("Không tìm thấy sản phẩm với ID: " + request.getIdSanPham()));
        
        // Tạo tất cả các tổ hợp có thể từ các thuộc tính đặc trưng được chọn
        List<List<UUID>> allCombinations = generateAllCombinations(request);
        
        List<ChiTietSanPhamResponse> createdVariants = new ArrayList<>();
        
        for (List<UUID> combination : allCombinations) {
            ChiTietSanPham chiTietSanPham = new ChiTietSanPham();
            chiTietSanPham.setSanPham(sanPham);
            chiTietSanPham.setMaCtsp(generateMaCtsp(sanPham.getMaSanPham(), combination));
            chiTietSanPham.setGiaBan(request.getGiaBan());
            chiTietSanPham.setGhiChu(request.getGhiChu());
            chiTietSanPham.setSoLuongTon(request.getSoLuongTon());
            chiTietSanPham.setSoLuongTamGiu(request.getSoLuongTamGiu());
            chiTietSanPham.setTrangThai(request.getTrangThai());
            
            // Set các thuộc tính đặc trưng từ combination
            setThuocTinhDacTrungFromCombination(chiTietSanPham, combination, request);
            
            ChiTietSanPham savedChiTietSanPham = chiTietSanPhamRepository.save(chiTietSanPham);
            createdVariants.add(convertToResponse(savedChiTietSanPham));
        }
        
        return createdVariants;
    }
    
    private List<List<UUID>> generateAllCombinations(TaoBienTheSanPhamRequest request) {
        List<List<UUID>> allCombinations = new ArrayList<>();
        
        // Lấy tất cả các thuộc tính đặc trưng được chọn
        List<UUID> cpuIds = request.getSelectedCpuIds() != null ? request.getSelectedCpuIds() : new ArrayList<>();
        List<UUID> gpuIds = request.getSelectedGpuIds() != null ? request.getSelectedGpuIds() : new ArrayList<>();
        List<UUID> ramIds = request.getSelectedRamIds() != null ? request.getSelectedRamIds() : new ArrayList<>();
        List<UUID> oCungIds = request.getSelectedOCungIds() != null ? request.getSelectedOCungIds() : new ArrayList<>();
        List<UUID> mauSacIds = request.getSelectedMauSacIds() != null ? request.getSelectedMauSacIds() : new ArrayList<>();
        List<UUID> loaiManHinhIds = request.getSelectedLoaiManHinhIds() != null ? request.getSelectedLoaiManHinhIds() : new ArrayList<>();
        List<UUID> pinIds = request.getSelectedPinIds() != null ? request.getSelectedPinIds() : new ArrayList<>();
        
        // Tạo tất cả các tổ hợp có thể
        for (UUID cpuId : cpuIds) {
            for (UUID gpuId : gpuIds) {
                for (UUID ramId : ramIds) {
                    for (UUID oCungId : oCungIds) {
                        for (UUID mauSacId : mauSacIds) {
                            for (UUID loaiManHinhId : loaiManHinhIds) {
                                for (UUID pinId : pinIds) {
                                    List<UUID> combination = Arrays.asList(cpuId, gpuId, ramId, oCungId, mauSacId, loaiManHinhId, pinId);
                                    allCombinations.add(combination);
                                }
                            }
                        }
                    }
                }
            }
        }
        
        return allCombinations;
    }
    
    private String generateMaCtsp(String maSanPham, List<UUID> combination) {
        // Tạo mã chi tiết sản phẩm dựa trên mã sản phẩm và các thuộc tính đặc trưng
        StringBuilder maCtsp = new StringBuilder(maSanPham);
        for (UUID id : combination) {
            maCtsp.append("-").append(id.toString().substring(0, 8));
        }
        return maCtsp.toString();
    }
    
    private void setThuocTinhDacTrungFromCombination(ChiTietSanPham chiTietSanPham, List<UUID> combination, TaoBienTheSanPhamRequest request) {
        // Set CPU
        if (combination.get(0) != null) {
            Cpu cpu = cpuRepository.findById(combination.get(0))
                    .orElseThrow(() -> new ApiException("Không tìm thấy CPU với ID: " + combination.get(0)));
            chiTietSanPham.setCpu(cpu);
        }
        
        // Set GPU
        if (combination.get(1) != null) {
            Gpu gpu = gpuRepository.findById(combination.get(1))
                    .orElseThrow(() -> new ApiException("Không tìm thấy GPU với ID: " + combination.get(1)));
            chiTietSanPham.setGpu(gpu);
        }
        
        // Set RAM
        if (combination.get(2) != null) {
            Ram ram = ramRepository.findById(combination.get(2))
                    .orElseThrow(() -> new ApiException("Không tìm thấy RAM với ID: " + combination.get(2)));
            chiTietSanPham.setRam(ram);
        }
        
        // Set Ổ cứng
        if (combination.get(3) != null) {
            OCung oCung = oCungRepository.findById(combination.get(3))
                    .orElseThrow(() -> new ApiException("Không tìm thấy ổ cứng với ID: " + combination.get(3)));
            chiTietSanPham.setOCung(oCung);
        }
        
        // Set Màu sắc
        if (combination.get(4) != null) {
            MauSac mauSac = mauSacRepository.findById(combination.get(4))
                    .orElseThrow(() -> new ApiException("Không tìm thấy màu sắc với ID: " + combination.get(4)));
            chiTietSanPham.setMauSac(mauSac);
        }
        
        // Set Loại màn hình
        if (combination.get(5) != null) {
            LoaiManHinh loaiManHinh = loaiManHinhRepository.findById(combination.get(5))
                    .orElseThrow(() -> new ApiException("Không tìm thấy loại màn hình với ID: " + combination.get(5)));
            chiTietSanPham.setLoaiManHinh(loaiManHinh);
        }
        
        // Set Pin
        if (combination.get(6) != null) {
            Pin pin = pinRepository.findById(combination.get(6))
                    .orElseThrow(() -> new ApiException("Không tìm thấy pin với ID: " + combination.get(6)));
            chiTietSanPham.setPin(pin);
        }
    }
    
    private void setThuocTinhDacTrung(ChiTietSanPham chiTietSanPham, ChiTietSanPhamRequest request) {
        // Set CPU
        if (request.getIdCpu() != null) {
            Cpu cpu = cpuRepository.findById(request.getIdCpu())
                    .orElseThrow(() -> new ApiException("Không tìm thấy CPU với ID: " + request.getIdCpu()));
            chiTietSanPham.setCpu(cpu);
        }
        
        // Set GPU
        if (request.getIdGpu() != null) {
            Gpu gpu = gpuRepository.findById(request.getIdGpu())
                    .orElseThrow(() -> new ApiException("Không tìm thấy GPU với ID: " + request.getIdGpu()));
            chiTietSanPham.setGpu(gpu);
        }
        
        // Set RAM
        if (request.getIdRam() != null) {
            Ram ram = ramRepository.findById(request.getIdRam())
                    .orElseThrow(() -> new ApiException("Không tìm thấy RAM với ID: " + request.getIdRam()));
            chiTietSanPham.setRam(ram);
        }
        
        // Set Ổ cứng
        if (request.getIdOCung() != null) {
            OCung oCung = oCungRepository.findById(request.getIdOCung())
                    .orElseThrow(() -> new ApiException("Không tìm thấy ổ cứng với ID: " + request.getIdOCung()));
            chiTietSanPham.setOCung(oCung);
        }
        
        // Set Màu sắc
        if (request.getIdMauSac() != null) {
            MauSac mauSac = mauSacRepository.findById(request.getIdMauSac())
                    .orElseThrow(() -> new ApiException("Không tìm thấy màu sắc với ID: " + request.getIdMauSac()));
            chiTietSanPham.setMauSac(mauSac);
        }
        
        // Set Loại màn hình
        if (request.getIdLoaiManHinh() != null) {
            LoaiManHinh loaiManHinh = loaiManHinhRepository.findById(request.getIdLoaiManHinh())
                    .orElseThrow(() -> new ApiException("Không tìm thấy loại màn hình với ID: " + request.getIdLoaiManHinh()));
            chiTietSanPham.setLoaiManHinh(loaiManHinh);
        }
        
        // Set Pin
        if (request.getIdPin() != null) {
            Pin pin = pinRepository.findById(request.getIdPin())
                    .orElseThrow(() -> new ApiException("Không tìm thấy pin với ID: " + request.getIdPin()));
            chiTietSanPham.setPin(pin);
        }
    }
    
    public ChiTietSanPhamResponse updateChiTietSanPham(UUID id, ChiTietSanPhamRequest request) {
        ChiTietSanPham chiTietSanPham = chiTietSanPhamRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy chi tiết sản phẩm với ID: " + id));
        
        // Kiểm tra mã chi tiết sản phẩm đã tồn tại (trừ chi tiết sản phẩm hiện tại)
        if (!chiTietSanPham.getMaCtsp().equals(request.getMaCtsp()) && 
            chiTietSanPhamRepository.existsByMaCtsp(request.getMaCtsp())) {
            throw new ApiException("Mã chi tiết sản phẩm đã tồn tại: " + request.getMaCtsp());
        }
        
        // Cập nhật thông tin cơ bản
        chiTietSanPham.setMaCtsp(request.getMaCtsp());
        chiTietSanPham.setGiaBan(request.getGiaBan());
        chiTietSanPham.setGhiChu(request.getGhiChu());
        chiTietSanPham.setSoLuongTon(request.getSoLuongTon());
        chiTietSanPham.setSoLuongTamGiu(request.getSoLuongTamGiu());
        chiTietSanPham.setTrangThai(request.getTrangThai());
        
        // Cập nhật các thuộc tính đặc trưng
        setThuocTinhDacTrung(chiTietSanPham, request);
        
        ChiTietSanPham savedChiTietSanPham = chiTietSanPhamRepository.save(chiTietSanPham);
        return convertToResponse(savedChiTietSanPham);
    }
    
    @Transactional(readOnly = true)
    public ChiTietSanPhamResponse getChiTietSanPhamById(UUID id) {
        ChiTietSanPham chiTietSanPham = chiTietSanPhamRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy chi tiết sản phẩm với ID: " + id));
        return convertToResponse(chiTietSanPham);
    }
    
    @Transactional(readOnly = true)
    public List<ChiTietSanPhamResponse> getChiTietSanPhamBySanPhamId(UUID sanPhamId) {
        List<ChiTietSanPham> chiTietSanPhams = chiTietSanPhamRepository.findBySanPham_Id(sanPhamId);
        return chiTietSanPhams.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<ChiTietSanPhamResponse> getAllChiTietSanPham() {
        List<ChiTietSanPham> chiTietSanPhams = chiTietSanPhamRepository.findAll();
        return chiTietSanPhams.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Page<ChiTietSanPhamResponse> getAllChiTietSanPham(Pageable pageable) {
        Page<ChiTietSanPham> chiTietSanPhams = chiTietSanPhamRepository.findAll(pageable);
        return chiTietSanPhams.map(this::convertToResponse);
    }
    
    @Transactional(readOnly = true)
    public Page<ChiTietSanPhamResponse> searchChiTietSanPham(
            String keyword,
            String cpu,
            String gpu,
            String ram,
            String color,
            String storage,
            String screen,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable) {

        Page<ChiTietSanPham> page = chiTietSanPhamRepository.search(
                blankToNull(keyword),
                blankToNull(cpu),
                blankToNull(gpu),
                blankToNull(ram),
                blankToNull(color),
                blankToNull(storage),
                blankToNull(screen),
                minPrice,
                maxPrice,
                pageable);

        return page.map(this::convertToResponse);
    }

    private String blankToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
    
    public void deleteChiTietSanPham(UUID id) {
        if (!chiTietSanPhamRepository.existsById(id)) {
            throw new ApiException("Không tìm thấy chi tiết sản phẩm với ID: " + id);
        }
        chiTietSanPhamRepository.deleteById(id);
    }
    
    public void updateTrangThaiChiTietSanPham(UUID id, Integer trangThai) {
        ChiTietSanPham chiTietSanPham = chiTietSanPhamRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy chi tiết sản phẩm với ID: " + id));
        
        chiTietSanPham.setTrangThai(trangThai);
        chiTietSanPhamRepository.save(chiTietSanPham);
    }
    
    private ChiTietSanPhamResponse convertToResponse(ChiTietSanPham chiTietSanPham) {
        ChiTietSanPhamResponse response = MapperUtils.map(chiTietSanPham, ChiTietSanPhamResponse.class);
        
        // Set thông tin sản phẩm
        if (chiTietSanPham.getSanPham() != null) {
            response.setIdSanPham(chiTietSanPham.getSanPham().getId());
            response.setTenSanPham(chiTietSanPham.getSanPham().getTenSanPham());
            response.setMaSanPham(chiTietSanPham.getSanPham().getMaSanPham());
        }
        
        // Set thông tin CPU
        if (chiTietSanPham.getCpu() != null) {
            response.setIdCpu(chiTietSanPham.getCpu().getId());
            response.setTenCpu(chiTietSanPham.getCpu().getTenCpu());
            response.setMaCpu(chiTietSanPham.getCpu().getMaCpu());
        }
        
        // Set thông tin GPU
        if (chiTietSanPham.getGpu() != null) {
            response.setIdGpu(chiTietSanPham.getGpu().getId());
            response.setTenGpu(chiTietSanPham.getGpu().getTenGpu());
            response.setMaGpu(chiTietSanPham.getGpu().getMaGpu());
        }
        
        // Set thông tin RAM
        if (chiTietSanPham.getRam() != null) {
            response.setIdRam(chiTietSanPham.getRam().getId());
            response.setTenRam(chiTietSanPham.getRam().getTenRam());
            response.setMaRam(chiTietSanPham.getRam().getMaRam());
        }
        
        // Set thông tin ổ cứng
        if (chiTietSanPham.getOCung() != null) {
            response.setIdOCung(chiTietSanPham.getOCung().getId());
            response.setDungLuongOCung(chiTietSanPham.getOCung().getDungLuong());
            response.setMaOCung(chiTietSanPham.getOCung().getMaOCung());
        }
        
        // Set thông tin màu sắc
        if (chiTietSanPham.getMauSac() != null) {
            response.setIdMauSac(chiTietSanPham.getMauSac().getId());
            response.setTenMauSac(chiTietSanPham.getMauSac().getTenMau());
            response.setMaMauSac(chiTietSanPham.getMauSac().getMaMau());
            response.setHexCode(chiTietSanPham.getMauSac().getHexCode());
        }
        
        // Set thông tin loại màn hình
        if (chiTietSanPham.getLoaiManHinh() != null) {
            response.setIdLoaiManHinh(chiTietSanPham.getLoaiManHinh().getId());
            response.setKichThuocManHinh(chiTietSanPham.getLoaiManHinh().getKichThuoc());
            response.setMaLoaiManHinh(chiTietSanPham.getLoaiManHinh().getMaLoaiManHinh());
        }
        
        // Set thông tin pin
        if (chiTietSanPham.getPin() != null) {
            response.setIdPin(chiTietSanPham.getPin().getId());
            response.setDungLuongPin(chiTietSanPham.getPin().getDungLuongPin());
            response.setMaPin(chiTietSanPham.getPin().getMaPin());
        }
        
        // Set timestamp fields
        response.setCreatedAt(chiTietSanPham.getNgayTao());
        response.setUpdatedAt(chiTietSanPham.getNgaySua());
        
        return response;
    }
}

package com.example.backendlaptop.service;

import com.example.backendlaptop.dto.serial.SerialRequest;
import com.example.backendlaptop.dto.serial.SerialResponse;
import com.example.backendlaptop.entity.ChiTietSanPham;
import com.example.backendlaptop.entity.Serial;
import com.example.backendlaptop.repository.ChiTietSanPhamRepository;
import com.example.backendlaptop.repository.SerialRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class SerialService {
    
    private final SerialRepository serialRepository;
    private final ChiTietSanPhamRepository chiTietSanPhamRepository;
    
    public SerialResponse createSerial(SerialRequest request) {
        // Check if CTSP exists
        ChiTietSanPham ctsp = chiTietSanPhamRepository.findById(request.getCtspId())
                .orElseThrow(() -> new RuntimeException("Chi tiết sản phẩm không tồn tại"));
        
        // Check if serial already exists
        if (serialRepository.existsBySerialNo(request.getSerialNo())) {
            throw new RuntimeException("Serial number đã tồn tại: " + request.getSerialNo());
        }
        
        Serial serial = new Serial();
        serial.setId(UUID.randomUUID());
        serial.setCtsp(ctsp);
        serial.setSerialNo(request.getSerialNo());
        serial.setTrangThai(request.getTrangThai());
        serial.setNgayNhap(Instant.now());
        
        Serial savedSerial = serialRepository.save(serial);
        
        // Update stock count
        updateStockCount(request.getCtspId());
        
        return mapToResponse(savedSerial);
    }
    
    public List<SerialResponse> createSerialsBatch(List<SerialRequest> requests) {
        List<SerialResponse> responses = new ArrayList<>();
        
        for (SerialRequest request : requests) {
            try {
                SerialResponse response = createSerial(request);
                responses.add(response);
            } catch (Exception e) {
                // Log error but continue with other serials
                System.err.println("Error creating serial " + request.getSerialNo() + ": " + e.getMessage());
            }
        }
        
        return responses;
    }
    
    public List<SerialResponse> importSerialsFromExcel(UUID ctspId, MultipartFile file) throws IOException {
        List<String> serialNumbers = new ArrayList<>();
        
        if (file.getOriginalFilename().endsWith(".csv")) {
            // Parse CSV
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
                String line;
                boolean isFirstLine = true;
                while ((line = reader.readLine()) != null) {
                    if (isFirstLine) {
                        isFirstLine = false;
                        continue; // Skip header
                    }
                    String[] parts = line.split(",");
                    if (parts.length > 0 && !parts[0].trim().isEmpty()) {
                        serialNumbers.add(parts[0].trim());
                    }
                }
            }
        } else if (file.getOriginalFilename().endsWith(".xlsx") || file.getOriginalFilename().endsWith(".xls")) {
            // Parse Excel
            try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
                Sheet sheet = workbook.getSheetAt(0);
                boolean isFirstRow = true;
                
                for (Row row : sheet) {
                    if (isFirstRow) {
                        isFirstRow = false;
                        continue; // Skip header
                    }
                    
                    Cell cell = row.getCell(0);
                    if (cell != null) {
                        String serialNo = "";
                        if (cell.getCellType() == CellType.STRING) {
                            serialNo = cell.getStringCellValue().trim();
                        } else if (cell.getCellType() == CellType.NUMERIC) {
                            serialNo = String.valueOf((long) cell.getNumericCellValue());
                        }
                        
                        if (!serialNo.isEmpty()) {
                            serialNumbers.add(serialNo);
                        }
                    }
                }
            }
        } else {
            throw new RuntimeException("Định dạng file không được hỗ trợ. Chỉ hỗ trợ .csv, .xlsx, .xls");
        }
        
        // Create serial requests
        List<SerialRequest> requests = serialNumbers.stream()
                .map(serialNo -> {
                    SerialRequest request = new SerialRequest();
                    request.setCtspId(ctspId);
                    request.setSerialNo(serialNo);
                    request.setTrangThai(1); // Chưa bán
                    return request;
                })
                .toList();
        
        return createSerialsBatch(requests);
    }
    
    public List<SerialResponse> getAllSerial() {
        System.out.println("SerialService: getAllSerial called");
        List<Serial> serials = serialRepository.findAll();
        System.out.println("Found " + serials.size() + " total serials");
        
        List<SerialResponse> responses = serials.stream().map(this::mapToResponse).toList();
        System.out.println("Returning " + responses.size() + " serial responses");
        return responses;
    }
    
    public List<SerialResponse> getSerialsByCtspId(UUID ctspId) {
        System.out.println("SerialService: getSerialsByCtspId called for ctspId: " + ctspId);
        List<Serial> serials = serialRepository.findByCtspId(ctspId);
        System.out.println("Found " + serials.size() + " serials");
        
        for (Serial serial : serials) {
            System.out.println("Serial: " + serial.getSerialNo() + ", Status: " + serial.getTrangThai());
        }
        
        List<SerialResponse> responses = serials.stream().map(this::mapToResponse).toList();
        System.out.println("Returning " + responses.size() + " serial responses");
        return responses;
    }
    
    public SerialResponse getSerialById(UUID id) {
        Serial serial = serialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Serial không tồn tại"));
        return mapToResponse(serial);
    }
    
    public SerialResponse updateSerial(UUID id, SerialRequest request) {
        Serial serial = serialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Serial không tồn tại"));
        
        // Check if new serial number already exists (if changed)
        if (!serial.getSerialNo().equals(request.getSerialNo()) && 
            serialRepository.existsBySerialNo(request.getSerialNo())) {
            throw new RuntimeException("Serial number đã tồn tại: " + request.getSerialNo());
        }
        
        serial.setSerialNo(request.getSerialNo());
        serial.setTrangThai(request.getTrangThai());
        
        Serial updatedSerial = serialRepository.save(serial);
        return mapToResponse(updatedSerial);
    }
    
    public void updateSerialStatus(UUID id, Integer trangThai) {
        Serial serial = serialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Serial không tồn tại"));

        serial.setTrangThai(trangThai);
        serialRepository.save(serial);
        // Update stock count for the variant (subtract hidden serials)
        updateStockCount(serial.getCtsp().getId());
    }
    
    public void deleteSerial(UUID id) {
        Serial serial = serialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Serial không tồn tại"));
        
        UUID ctspId = serial.getCtsp().getId();
        serialRepository.delete(serial);
        
        // Update stock count
        updateStockCount(ctspId);
    }
    
    private void updateStockCount(UUID ctspId) {
        int count = serialRepository.countByCtspIdAndTrangThai(ctspId, 1); // Count available serials
        ChiTietSanPham ctsp = chiTietSanPhamRepository.findById(ctspId)
                .orElseThrow(() -> new RuntimeException("Chi tiết sản phẩm không tồn tại"));
        
        ctsp.setSoLuongTon(count);
        chiTietSanPhamRepository.save(ctsp);
    }
    
    private SerialResponse mapToResponse(Serial serial) {
        SerialResponse response = new SerialResponse();
        response.setId(serial.getId());
        response.setCtspId(serial.getCtsp().getId());
        response.setSerialNo(serial.getSerialNo());
        response.setTrangThai(serial.getTrangThai());
        response.setNgayNhap(serial.getNgayNhap());
        
        // Add product info
        if (serial.getCtsp() != null) {
            response.setMaCtsp(serial.getCtsp().getMaCtsp());
            if (serial.getCtsp().getSanPham() != null) {
                response.setTenSanPham(serial.getCtsp().getSanPham().getTenSanPham());
            }
        }
        
        return response;
    }
}

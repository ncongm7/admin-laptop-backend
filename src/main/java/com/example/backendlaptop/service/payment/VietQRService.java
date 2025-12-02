package com.example.backendlaptop.service.payment;

import com.example.backendlaptop.config.VietQRConfig;
import com.example.backendlaptop.dto.payment.QRCodeRequest;
import com.example.backendlaptop.dto.payment.QRCodeResponse;
import com.example.backendlaptop.expection.ApiException;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

/**
 * Service xử lý tạo QR code thanh toán qua VietQR
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VietQRService {
    
    private final VietQRConfig vietQRConfig;
    
    /**
     * Generate QR code thanh toán sử dụng VietQR API
     * 
     * @param request Thông tin thanh toán
     * @return Response chứa URL QR code và thông tin thanh toán
     */
    public QRCodeResponse generateQRCode(QRCodeRequest request) {
        try {
            log.info("🔄 [VietQRService] Tạo QR code cho đơn hàng: {}, Số tiền: {}", 
                    request.getOrderCode(), request.getAmount());
            
            // Validate input
            if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ApiException("Số tiền thanh toán phải lớn hơn 0");
            }
            
            if (request.getOrderCode() == null || request.getOrderCode().trim().isEmpty()) {
                throw new ApiException("Mã đơn hàng không được để trống");
            }
            
            // Convert amount to VND (VietQR yêu cầu số nguyên, không có phần thập phân)
            Long amountInVND = request.getAmount().longValue();
            
            // Encode nội dung chuyển khoản
            String description = request.getDescription() != null 
                    ? request.getDescription() 
                    : request.getOrderCode();
            String encodedDescription = URLEncoder.encode(description, StandardCharsets.UTF_8);
            
            // Build VietQR URL theo format:
            // https://img.vietqr.io/image/{BANK_BIN}-{ACCOUNT_NO}-{TEMPLATE}.png?amount={AMOUNT}&addInfo={DESCRIPTION}&accountName={ACCOUNT_NAME}
            StringBuilder urlBuilder = new StringBuilder();
            urlBuilder.append(vietQRConfig.getApi().getUrl())
                    .append("/").append(vietQRConfig.getBank().getBin())
                    .append("-").append(vietQRConfig.getBank().getAccountNo())
                    .append("-").append(vietQRConfig.getBank().getTemplate())
                    .append(".png")
                    .append("?amount=").append(amountInVND)
                    .append("&addInfo=").append(encodedDescription)
                    .append("&accountName=").append(URLEncoder.encode(vietQRConfig.getBank().getAccountName(), StandardCharsets.UTF_8));
            
            String qrCodeUrl = urlBuilder.toString();
            
            log.info("✅ [VietQRService] QR URL: {}", qrCodeUrl);
            
            // Tính thời gian hết hạn (15 phút từ bây giờ)
            Instant expiryTime = Instant.now().plusSeconds(vietQRConfig.getPayment().getTimeout());
            
            // Build response
            QRCodeResponse response = QRCodeResponse.builder()
                    .qrCodeUrl(qrCodeUrl)
                    .qrCodeDataUrl(null) // VietQR trả về URL trực tiếp, không cần encode base64
                    .paymentUrl(qrCodeUrl) // URL để mở app ngân hàng
                    .expiryTime(expiryTime)
                    .orderCode(request.getOrderCode())
                    .amount(amountInVND)
                    .description(description)
                    .bankInfo(QRCodeResponse.BankInfo.builder()
                            .bankName(getBankName(vietQRConfig.getBank().getBin()))
                            .accountNo(vietQRConfig.getBank().getAccountNo())
                            .accountName(vietQRConfig.getBank().getAccountName())
                            .build())
                    .build();
            
            log.info("✅ [VietQRService] Tạo QR code thành công cho đơn hàng: {}", request.getOrderCode());
            
            return response;
            
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("❌ [VietQRService] Lỗi khi tạo QR code: {}", e.getMessage(), e);
            throw new ApiException("Không thể tạo QR code: " + e.getMessage());
        }
    }
    
    /**
     * Generate QR code dạng base64 (dự phòng nếu cần embed trực tiếp vào HTML)
     * 
     * @param content Nội dung QR code
     * @param width Chiều rộng
     * @param height Chiều cao
     * @return Base64 encoded QR code image
     */
    public String generateQRCodeBase64(String content, int width, int height) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height);
            
            BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "PNG", baos);
            byte[] imageBytes = baos.toByteArray();
            
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            
            return "data:image/png;base64," + base64Image;
            
        } catch (WriterException | IOException e) {
            log.error("❌ [VietQRService] Lỗi khi tạo QR code base64: {}", e.getMessage(), e);
            throw new ApiException("Không thể tạo QR code: " + e.getMessage());
        }
    }
    
    /**
     * Verify payment (đơn giản hóa - trong thực tế cần webhook từ bank hoặc API check transaction)
     * Hiện tại chỉ check trong database xem đã cập nhật chưa
     * 
     * @param orderCode Mã đơn hàng
     * @param amount Số tiền
     * @return true nếu đã thanh toán
     */
    public boolean verifyPayment(String orderCode, BigDecimal amount) {
        // TODO: Implement logic check transaction từ bank
        // Hiện tại dựa vào webhook hoặc admin confirm thủ công
        log.info("🔍 [VietQRService] Verify payment cho đơn hàng: {}, Số tiền: {}", orderCode, amount);
        return false; // Default chưa thanh toán, đợi webhook update
    }
    
    /**
     * Get bank name từ BIN code
     * 
     * @param bin Mã BIN ngân hàng
     * @return Tên ngân hàng
     */
    private String getBankName(String bin) {
        // Mapping một số ngân hàng phổ biến
        return switch (bin) {
            case "970415" -> "VietinBank";
            case "970436" -> "Vietcombank";
            case "970418" -> "BIDV";
            case "970422" -> "MB Bank";
            case "970407" -> "Techcombank";
            case "970432" -> "VPBank";
            case "970423" -> "TPBank";
            case "970416" -> "ACB";
            case "970403" -> "Sacombank";
            case "970405" -> "Agribank";
            case "970441" -> "VIB";
            default -> "Ngân hàng";
        };
    }
}

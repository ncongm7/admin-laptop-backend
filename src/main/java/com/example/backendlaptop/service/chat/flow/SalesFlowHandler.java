package com.example.backendlaptop.service.chat.flow;

import com.example.backendlaptop.dto.chat.ChatbotResponse;
import com.example.backendlaptop.dto.chat.QuickReplyDTO;
import com.example.backendlaptop.entity.ChatSession;
import com.example.backendlaptop.entity.ChiTietSanPham;
import com.example.backendlaptop.entity.SanPham;
import com.example.backendlaptop.model.ConversationState;
import com.example.backendlaptop.repository.ChiTietSanPhamRepository;
import com.example.backendlaptop.repository.SanPhamRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class SalesFlowHandler {
    
    private final SanPhamRepository sanPhamRepo;
    private final ChiTietSanPhamRepository chiTietSanPhamRepo;
    private final ObjectMapper objectMapper;
    
    public ChatbotResponse handle(ChatSession session, ConversationState state, String userMessage, UUID khachHangId) {
        switch (state) {
            case SALES_IDENTIFY_NEED:
                return handleIdentifyNeed(session);
            case SALES_SHOW_OPTIONS:
                return handleShowOptions(session, userMessage);
            case SALES_COMPARE:
                return handleCompare(session);
            case SALES_DETAIL:
                return handleDetail(session, userMessage);
            case SALES_CTA:
                return handleCTA(session);
            default:
                return ChatbotResponse.simpleResponse("Bạn muốn tìm laptop như thế nào?", state.name(), BigDecimal.ONE);
        }
    }
    
    private ChatbotResponse handleIdentifyNeed(ChatSession session) {
        List<QuickReplyDTO> needOptions = Arrays.asList(
            QuickReplyDTO.builder()
                .replyText("💼 Làm việc văn phòng")
                .replyValue("OFFICE")
                .replyType("NEED")
                .icon("💼")
                .displayOrder(1)
                .build(),
            QuickReplyDTO.builder()
                .replyText("🎮 Chơi game")
                .replyValue("GAMING")
                .replyType("NEED")
                .icon("🎮")
                .displayOrder(2)
                .build(),
            QuickReplyDTO.builder()
                .replyText("🎨 Đồ họa / Design")
                .replyValue("DESIGN")
                .replyType("NEED")
                .icon("🎨")
                .displayOrder(3)
                .build(),
            QuickReplyDTO.builder()
                .replyText("💻 Đa năng")
                .replyValue("ALL_PURPOSE")
                .replyType("NEED")
                .icon("💻")
                .displayOrder(4)
                .build()
        );
        
        return ChatbotResponse.builder()
                .responseText("Bạn cần laptop để làm gì nhỉ? 🤔\n\n" +
                             "Mỗi nhu cầu sẽ có cấu hình phù hợp khác nhau đấy!")
                .quickReplies(needOptions)
                .intentCode("SALES_IDENTIFY_NEED")
                .confidence(BigDecimal.ONE)
                .shouldSave(true)
                .shouldEscalate(false)
                .build();
    }
    
    private ChatbotResponse handleShowOptions(ChatSession session, String userMessage) {
        // Extract need from user message or progress data
        String need = extractNeedCategory(session, userMessage);
        
        // Get products based on need
        List<SanPham> products = getProductsByNeed(need);
        
        if (products.isEmpty()) {
            return ChatbotResponse.simpleResponse(
                "Xin lỗi, hiện tại mình chưa có sản phẩm phù hợp. Bạn có thể liên hệ nhân viên để được tư vấn thêm nhé!",
                "NO_PRODUCTS",
                BigDecimal.valueOf(0.8)
            );
        }
        
        // Take top 3
        List<SanPham> topProducts = products.stream().limit(3).collect(Collectors.toList());
        
        // Save to progress
        saveProductsToProgress(session, topProducts, need);
        
        // Build response
        StringBuilder response = new StringBuilder();
        response.append(String.format("Dựa trên nhu cầu **%s**, mình gợi ý:\n\n", getNeedLabel(need)));
        
        int index = 1;
        List<QuickReplyDTO> productOptions = new ArrayList<>();
        
        for (SanPham product : topProducts) {
            // Get price range
            String priceInfo = getProductPriceRange(product.getId());
            
            response.append(String.format("%d. **%s**\n", index, product.getTenSanPham()));
            response.append(String.format("   💰 %s\n", priceInfo));
            
            // Add specs if available
            if (product.getMoTa() != null && !product.getMoTa().isEmpty()) {
                String shortDesc = product.getMoTa().length() > 80 
                    ? product.getMoTa().substring(0, 80) + "..." 
                    : product.getMoTa();
                response.append(String.format("   ⚡ %s\n\n", shortDesc));
            }
            
            // Quick reply
            productOptions.add(QuickReplyDTO.builder()
                .replyText(String.format("Xem %s", product.getTenSanPham()))
                .replyValue(product.getId().toString())
                .replyType("PRODUCT_ID")
                .displayOrder(index)
                .build());
            
            index++;
        }
        
        // Add compare option if more than 1
        if (topProducts.size() > 1) {
            productOptions.add(QuickReplyDTO.builder()
                .replyText("🔍 So sánh")
                .replyValue("COMPARE")
                .replyType("ACTION")
                .icon("🔍")
                .displayOrder(99)
                .build());
        }
        
        response.append("\nBạn muốn xem chi tiết sản phẩm nào? 👆");
        
        return ChatbotResponse.builder()
                .responseText(response.toString())
                .quickReplies(productOptions)
                .intentCode("SALES_SHOW_OPTIONS")
                .confidence(BigDecimal.ONE)
                .shouldSave(true)
                .shouldEscalate(false)
                .build();
    }
    
    private ChatbotResponse handleCompare(ChatSession session) {
        List<SanPham> products = getProductsFromProgress(session);
        
        if (products.size() < 2) {
            return ChatbotResponse.simpleResponse(
                "Mình cần ít nhất 2 sản phẩm để so sánh. Bạn muốn xem thêm sản phẩm khác không?",
                "COMPARE_ERROR",
                BigDecimal.valueOf(0.8)
            );
        }
        
        StringBuilder comparison = new StringBuilder("📊 **So sánh sản phẩm:**\n\n");
        
        for (int i = 0; i < Math.min(products.size(), 3); i++) {
            SanPham p = products.get(i);
            comparison.append(String.format("%d. **%s**\n", (i+1), p.getTenSanPham()));
            comparison.append(String.format("   💰 %s\n", getProductPriceRange(p.getId())));
            comparison.append(String.format("   📝 %s\n\n", 
                p.getMoTa() != null && !p.getMoTa().isEmpty() ? p.getMoTa() : "Đang cập nhật..."));
        }
        
        comparison.append("\nBạn thích sản phẩm nào nhất? 😊");
        
        List<QuickReplyDTO> options = new ArrayList<>();
        for (int i = 0; i < Math.min(products.size(), 3); i++) {
            SanPham p = products.get(i);
            options.add(QuickReplyDTO.builder()
                .replyText(String.format("Chọn %s", p.getTenSanPham()))
                .replyValue(p.getId().toString())
                .replyType("PRODUCT_ID")
                .displayOrder(i + 1)
                .build());
        }
        
        return ChatbotResponse.builder()
                .responseText(comparison.toString())
                .quickReplies(options)
                .intentCode("SALES_COMPARE")
                .confidence(BigDecimal.ONE)
                .shouldSave(true)
                .shouldEscalate(false)
                .build();
    }
    
    private ChatbotResponse handleDetail(ChatSession session, String userMessage) {
        UUID productId = extractProductId(session, userMessage);
        
        if (productId == null) {
            return ChatbotResponse.simpleResponse(
                "Bạn muốn xem sản phẩm nào? Vui lòng chọn từ danh sách trên nhé!",
                "NO_PRODUCT_SELECTED",
                BigDecimal.valueOf(0.5)
            );
        }
        
        Optional<SanPham> productOpt = sanPhamRepo.findById(productId);
        if (productOpt.isEmpty()) {
            return ChatbotResponse.simpleResponse(
                "Sản phẩm không tồn tại. Vui lòng chọn lại!",
                "PRODUCT_NOT_FOUND",
                BigDecimal.valueOf(0.5)
            );
        }
        
        SanPham product = productOpt.get();
        saveSelectedProduct(session, productId);
        
        StringBuilder detail = new StringBuilder();
        detail.append(String.format("📱 **%s**\n\n", product.getTenSanPham()));
        detail.append(String.format("💰 **Giá:** %s\n\n", getProductPriceRange(product.getId())));
        
        if (product.getMoTa() != null && !product.getMoTa().isEmpty()) {
            detail.append(String.format("📝 **Mô tả:**\n%s\n\n", product.getMoTa()));
        }
        
        detail.append("✨ Sản phẩm này có vẻ phù hợp với bạn đấy!");
        
        List<QuickReplyDTO> actions = Arrays.asList(
            QuickReplyDTO.builder()
                .replyText("🛒 Thêm vào giỏ")
                .replyValue(String.format("add_to_cart|%s", productId))
                .replyType("ACTION")
                .icon("🛒")
                .displayOrder(1)
                .build(),
            QuickReplyDTO.builder()
                .replyText("📱 Xem chi tiết")
                .replyValue(String.format("view_product|%s", productId))
                .replyType("ACTION")
                .icon("📱")
                .displayOrder(2)
                .build(),
            QuickReplyDTO.builder()
                .replyText("💬 Hỏi thêm")
                .replyValue("SALES")
                .replyType("INTENT")
                .icon("💬")
                .displayOrder(3)
                .build()
        );
        
        return ChatbotResponse.builder()
                .responseText(detail.toString())
                .quickReplies(actions)
                .intentCode("SALES_DETAIL")
                .confidence(BigDecimal.ONE)
                .shouldSave(true)
                .shouldEscalate(false)
                .build();
    }
    
    private ChatbotResponse handleCTA(ChatSession session) {
        UUID productId = getSelectedProduct(session);
        
        if (productId == null) {
            return handleIdentifyNeed(session); // Restart
        }
        
        Optional<SanPham> productOpt = sanPhamRepo.findById(productId);
        if (productOpt.isEmpty()) {
            return handleIdentifyNeed(session);
        }
        
        SanPham product = productOpt.get();
        
        List<QuickReplyDTO> finalActions = Arrays.asList(
            QuickReplyDTO.builder()
                .replyText("🛒 Thêm vào giỏ hàng")
                .replyValue(String.format("add_to_cart|%s", productId))
                .replyType("ACTION")
                .icon("🛒")
                .displayOrder(1)
                .build(),
            QuickReplyDTO.builder()
                .replyText("📱 Xem chi tiết")
                .replyValue(String.format("view_product|%s", productId))
                .replyType("ACTION")
                .icon("📱")
                .displayOrder(2)
                .build(),
            QuickReplyDTO.builder()
                .replyText("🔙 Xem sản phẩm khác")
                .replyValue("SALES")
                .replyType("INTENT")
                .icon("🔙")
                .displayOrder(3)
                .build()
        );
        
        return ChatbotResponse.builder()
                .responseText(
                    String.format("**%s** rất phù hợp với nhu cầu của bạn! 🎯\n\n" +
                                 "Bạn muốn:", product.getTenSanPham())
                )
                .quickReplies(finalActions)
                .intentCode("SALES_CTA")
                .confidence(BigDecimal.ONE)
                .shouldSave(true)
                .shouldEscalate(false)
                .build();
    }
    
    // Helper Methods
    
    private String extractNeedCategory(ChatSession session, String userMessage) {
        // Try from message first
        String msg = userMessage.toLowerCase();
        if (msg.contains("văn phòng") || msg.contains("office")) return "OFFICE";
        if (msg.contains("game") || msg.contains("gaming")) return "GAMING";
        if (msg.contains("đồ họa") || msg.contains("design")) return "DESIGN";
        
        // Try from progress data
        try {
            if (session.getProgressData() != null) {
                Map<String, Object> progress = objectMapper.readValue(
                    session.getProgressData(),
                    new TypeReference<Map<String, Object>>() {}
                );
                return (String) progress.get("need_category");
            }
        } catch (Exception e) {
            log.debug("Could not extract need from progress", e);
        }
        
        return "ALL_PURPOSE";
    }
    
    private List<SanPham> getProductsByNeed(String need) {
        // Simple filtering based on product name/description
        // In production, use proper categorization
        List<SanPham> allProducts = sanPhamRepo.findByTrangThai(1, PageRequest.of(0, 10)).getContent();
        
        return allProducts.stream()
            .filter(p -> {
                String name = (p.getTenSanPham() + " " + (p.getMoTa() != null ? p.getMoTa() : "")).toLowerCase();
                switch (need) {
                    case "GAMING":
                        return name.contains("gaming") || name.contains("game") || name.contains("rtx") || name.contains("gtx");
                    case "DESIGN":
                        return name.contains("creator") || name.contains("studio") || name.contains("workstation");
                    case "OFFICE":
                        return !name.contains("gaming") && !name.contains("game");
                    default:
                        return true;
                }
            })
            .collect(Collectors.toList());
    }
    
    private String getProductPriceRange(UUID productId) {
        List<ChiTietSanPham> variants = chiTietSanPhamRepo.findBySanPham_Id(productId);
        
        if (variants.isEmpty()) {
            return "Liên hệ";
        }
        
        long minPrice = variants.stream()
            .filter(v -> v.getGiaBan() != null)
            .mapToLong(v -> v.getGiaBan().longValue())
            .min()
            .orElse(0);
        
        long maxPrice = variants.stream()
            .filter(v -> v.getGiaBan() != null)
            .mapToLong(v -> v.getGiaBan().longValue())
            .max()
            .orElse(0);
        
        if (minPrice == 0) return "Liên hệ";
        
        if (minPrice == maxPrice) {
            return formatPrice(minPrice) + " VNĐ";
        }
        
        return formatPrice(minPrice) + " - " + formatPrice(maxPrice) + " VNĐ";
    }
    
    private String formatPrice(long price) {
        return String.format("%,d", price).replace(",", ".");
    }
    
    private String getNeedLabel(String need) {
        switch (need) {
            case "OFFICE": return "Làm việc văn phòng";
            case "GAMING": return "Chơi game";
            case "DESIGN": return "Đồ họa / Design";
            default: return "Đa năng";
        }
    }
    
    private void saveProductsToProgress(ChatSession session, List<SanPham> products, String need) {
        try {
            Map<String, Object> progress = new HashMap<>();
            progress.put("need_category", need);
            progress.put("product_ids", products.stream().map(p -> p.getId().toString()).collect(Collectors.toList()));
            progress.put("timestamp", System.currentTimeMillis());
            
            session.setProgressData(objectMapper.writeValueAsString(progress));
        } catch (Exception e) {
            log.error("Error saving products to progress", e);
        }
    }
    
    private List<SanPham> getProductsFromProgress(ChatSession session) {
        try {
            if (session.getProgressData() != null) {
                Map<String, Object> progress = objectMapper.readValue(
                    session.getProgressData(),
                    new TypeReference<Map<String, Object>>() {}
                );
                
                @SuppressWarnings("unchecked")
                List<String> productIds = (List<String>) progress.get("product_ids");
                
                if (productIds != null) {
                    return productIds.stream()
                        .map(id -> sanPhamRepo.findById(UUID.fromString(id)))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList());
                }
            }
        } catch (Exception e) {
            log.error("Error getting products from progress", e);
        }
        
        return new ArrayList<>();
    }
    
    private UUID extractProductId(ChatSession session, String userMessage) {
        // Try to parse from message
        try {
            return UUID.fromString(userMessage.trim());
        } catch (Exception e) {
            // Not a UUID
        }
        
        // Try from progress
        return getSelectedProduct(session);
    }
    
    private void saveSelectedProduct(ChatSession session, UUID productId) {
        try {
            Map<String, Object> progress;
            if (session.getProgressData() != null) {
                progress = objectMapper.readValue(
                    session.getProgressData(),
                    new TypeReference<Map<String, Object>>() {}
                );
            } else {
                progress = new HashMap<>();
            }
            
            progress.put("selected_product_id", productId.toString());
            session.setProgressData(objectMapper.writeValueAsString(progress));
        } catch (Exception e) {
            log.error("Error saving selected product", e);
        }
    }
    
    private UUID getSelectedProduct(ChatSession session) {
        try {
            if (session.getProgressData() != null) {
                Map<String, Object> progress = objectMapper.readValue(
                    session.getProgressData(),
                    new TypeReference<Map<String, Object>>() {}
                );
                
                String productId = (String) progress.get("selected_product_id");
                if (productId != null) {
                    return UUID.fromString(productId);
                }
            }
        } catch (Exception e) {
            log.debug("Could not get selected product", e);
        }
        
        return null;
    }
}

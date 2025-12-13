package com.example.backendlaptop.model;

/**
 * Conversation State Machine cho Chatbot
 * Định nghĩa các trạng thái hội thoại có mục đích rõ ràng
 */
public enum ConversationState {
    // ========== INITIAL STATES ==========
    IDLE("Chờ người dùng", "INITIAL"),
    GREETING("Chào hỏi ban đầu", "INITIAL"),
    
    // ========== SALES FLOW ==========
    SALES_IDENTIFY_NEED("Xác định nhu cầu mua hàng", "SALES"),
    SALES_SHOW_OPTIONS("Hiển thị sản phẩm gợi ý", "SALES"),
    SALES_COMPARE("So sánh sản phẩm", "SALES"),
    SALES_DETAIL("Xem chi tiết sản phẩm", "SALES"),
    SALES_CTA("Gợi ý hành động mua", "SALES"),
    
    // ========== ORDER TRACKING FLOW ==========
    ORDER_REQUEST_CODE("Yêu cầu mã đơn hàng", "ORDER"),
    ORDER_SHOW_STATUS("Hiển thị trạng thái đơn", "ORDER"),
    ORDER_ACTIONS("Gợi ý hành động cho đơn hàng", "ORDER"),
    
    // ========== WARRANTY FLOW ==========
    WARRANTY_CHECK_LOGIN("Kiểm tra đăng nhập", "WARRANTY"),
    WARRANTY_SELECT_PRODUCT("Chọn sản phẩm cần bảo hành", "WARRANTY"),
    WARRANTY_CHECK_STATUS("Kiểm tra trạng thái bảo hành", "WARRANTY"),
    WARRANTY_CREATE_REQUEST("Tạo yêu cầu bảo hành", "WARRANTY"),
    WARRANTY_CONFIRM("Xác nhận tạo phiếu", "WARRANTY"),
    
    // ========== ACCOUNT FLOW ==========
    ACCOUNT_CHECK_LOGIN("Kiểm tra đăng nhập tài khoản", "ACCOUNT"),
    ACCOUNT_GUIDE("Hướng dẫn quản lý tài khoản", "ACCOUNT"),
    ACCOUNT_UPDATE("Cập nhật thông tin", "ACCOUNT"),
    
    // ========== SUPPORT STATES ==========
    UNCLEAR("Không hiểu yêu cầu", "SUPPORT"),
    ESCALATED("Chuyển nhân viên", "SUPPORT"),
    RESOLVED("Đã giải quyết", "SUPPORT");
    
    private final String description;
    private final String category;
    
    ConversationState(String description, String category) {
        this.description = description;
        this.category = category;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getCategory() {
        return category;
    }
    
    /**
     * Check if state requires authentication
     */
    public boolean requiresAuth() {
        return this == WARRANTY_SELECT_PRODUCT 
            || this == WARRANTY_CHECK_STATUS 
            || this == WARRANTY_CREATE_REQUEST
            || this == ACCOUNT_GUIDE
            || this == ACCOUNT_UPDATE;
    }
    
    /**
     * Check if state is a terminal state (end of flow)
     */
    public boolean isTerminal() {
        return this == SALES_CTA 
            || this == ORDER_ACTIONS 
            || this == WARRANTY_CONFIRM
            || this == ESCALATED
            || this == RESOLVED;
    }
    
    /**
     * Check if state is initial
     */
    public boolean isInitial() {
        return category.equals("INITIAL");
    }
}

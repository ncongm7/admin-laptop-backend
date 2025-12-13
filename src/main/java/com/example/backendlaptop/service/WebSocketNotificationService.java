package com.example.backendlaptop.service;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Service để gửi WebSocket notifications
 */
@Service
@RequiredArgsConstructor
public class WebSocketNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Gửi thông báo khi có đơn hàng online mới
     */
    public void notifyNewOnlineOrder(UUID orderId, String orderCode, String customerName) {
        try {
            System.out.println("📨 [WebSocketNotificationService] Gửi thông báo đơn hàng mới: " + orderCode);
            
            OrderNotificationMessage message = new OrderNotificationMessage();
            message.setType("new_online_order");
            message.setEventType("new_online_order");
            message.setOrderId(orderId);
            message.setOrderCode(orderCode);
            message.setCustomerName(customerName);
            message.setTimestamp(Instant.now().toString());
            
            // Gửi đến topic chung
            messagingTemplate.convertAndSend("/topic/orders", message);
            
            // Gửi đến topic riêng
            messagingTemplate.convertAndSend("/topic/new-online-order", message);
            
            System.out.println("✅ [WebSocketNotificationService] Đã gửi thông báo đơn hàng mới");
        } catch (Exception e) {
            System.err.println("❌ [WebSocketNotificationService] Lỗi khi gửi thông báo đơn hàng mới: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Gửi thông báo khi trạng thái đơn hàng thay đổi
     */
    public void notifyOrderStatusChanged(UUID orderId, Integer oldStatus, Integer newStatus) {
        try {
            System.out.println("🔄 [WebSocketNotificationService] Gửi thông báo thay đổi trạng thái: " + orderId);
            
            OrderStatusChangeMessage message = new OrderStatusChangeMessage();
            message.setType("order_status_changed");
            message.setEventType("order_status_changed");
            message.setOrderId(orderId);
            message.setOldStatus(oldStatus);
            message.setNewStatus(newStatus);
            message.setTimestamp(Instant.now().toString());
            
            // Gửi đến topic chung
            messagingTemplate.convertAndSend("/topic/orders", message);
            
            // Gửi đến topic riêng
            messagingTemplate.convertAndSend("/topic/order-status-changed", message);
            
            System.out.println("✅ [WebSocketNotificationService] Đã gửi thông báo thay đổi trạng thái");
        } catch (Exception e) {
            System.err.println("❌ [WebSocketNotificationService] Lỗi khi gửi thông báo thay đổi trạng thái: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Gửi thông báo khi thanh toán QR được xác nhận
     */
    public void sendPaymentConfirmation(UUID orderId, String transactionId, BigDecimal amount) {
        try {
            System.out.println("💰 [WebSocketNotificationService] Gửi thông báo thanh toán QR: " + orderId);
            
            PaymentConfirmationMessage message = new PaymentConfirmationMessage();
            message.setType("payment_confirmed");
            message.setEventType("payment_confirmed");
            message.setOrderId(orderId);
            message.setTransactionId(transactionId);
            message.setAmount(amount);
            message.setTimestamp(Instant.now().toString());
            
            // Gửi đến topic chung
            messagingTemplate.convertAndSend("/topic/payment-confirmed", message);
            
            // Gửi đến topic riêng cho từng đơn hàng
            messagingTemplate.convertAndSend("/topic/payment-confirmed/" + orderId, message);
            
            System.out.println("✅ [WebSocketNotificationService] Đã gửi thông báo thanh toán QR");
        } catch (Exception e) {
            System.err.println("❌ [WebSocketNotificationService] Lỗi khi gửi thông báo thanh toán: " + e.getMessage());
            e.printStackTrace();
        }

    }

    /**
     * Gửi thông báo khi đơn hàng bị hủy
     */
    public void notifyOrderCancelled(UUID orderId, String reason) {
        try {
            System.out.println("🚫 [WebSocketNotificationService] Gửi thông báo hủy đơn: " + orderId);
            
            OrderCancelledMessage message = new OrderCancelledMessage();
            message.setType("order_cancelled");
            message.setEventType("order_cancelled");
            message.setOrderId(orderId);
            message.setReason(reason);
            message.setTimestamp(Instant.now().toString());
            
            // Gửi đến topic chung (để admin biết)
            messagingTemplate.convertAndSend("/topic/orders", message);
            
            // Gửi đến topic riêng (cho khách hàng)
            messagingTemplate.convertAndSend("/topic/order-cancelled/" + orderId, message);
            
            System.out.println("✅ [WebSocketNotificationService] Đã gửi thông báo hủy đơn");
        } catch (Exception e) {
            System.err.println("❌ [WebSocketNotificationService] Lỗi khi gửi thông báo hủy đơn: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * DTO cho message đơn hàng mới
     */
    public static class OrderNotificationMessage {
        private String type;
        private String eventType;
        private UUID orderId;
        private String orderCode;
        private String customerName;
        private String timestamp;

        // Getters and Setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }
        public UUID getOrderId() { return orderId; }
        public void setOrderId(UUID orderId) { this.orderId = orderId; }
        public String getOrderCode() { return orderCode; }
        public void setOrderCode(String orderCode) { this.orderCode = orderCode; }
        public String getCustomerName() { return customerName; }
        public void setCustomerName(String customerName) { this.customerName = customerName; }
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    }
    
    /**
     * DTO cho message xác nhận thanh toán QR
     */
    public static class PaymentConfirmationMessage {
        private String type;
        private String eventType;
        private UUID orderId;
        private String transactionId;
        private BigDecimal amount;
        private String timestamp;

        // Getters and Setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }
        public UUID getOrderId() { return orderId; }
        public void setOrderId(UUID orderId) { this.orderId = orderId; }
        public String getTransactionId() { return transactionId; }
        public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    }

    /**
     * DTO cho message thay đổi trạng thái
     */
    public static class OrderStatusChangeMessage {
        private String type;
        private String eventType;
        private UUID orderId;
        private Integer oldStatus;
        private Integer newStatus;
        private String timestamp;

        // Getters and Setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }
        public UUID getOrderId() { return orderId; }
        public void setOrderId(UUID orderId) { this.orderId = orderId; }
        public Integer getOldStatus() { return oldStatus; }
        public void setOldStatus(Integer oldStatus) { this.oldStatus = oldStatus; }
        public Integer getNewStatus() { return newStatus; }
        public void setNewStatus(Integer newStatus) { this.newStatus = newStatus; }
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    }

    /**
     * DTO cho message hủy đơn hàng
     */
    public static class OrderCancelledMessage {
        private String type;
        private String eventType;
        private UUID orderId;
        private String reason;
        private String timestamp;

        // Getters and Setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }
        public UUID getOrderId() { return orderId; }
        public void setOrderId(UUID orderId) { this.orderId = orderId; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    }
}


package com.example.backendlaptop.service.chat;

import com.example.backendlaptop.dto.chat.ChatbotResponse;
import com.example.backendlaptop.dto.chat.QuickReplyDTO;
import com.example.backendlaptop.entity.ChatIntent;
import com.example.backendlaptop.entity.ChatSession;
import com.example.backendlaptop.model.ConversationState;
import com.example.backendlaptop.repository.ChatSessionRepository;
import com.example.backendlaptop.service.chat.flow.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

/**
 * Core Conversation Flow Engine
 * Quản lý state transitions và điều phối flow handlers
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ConversationFlowService {
    
    private final ChatSessionRepository sessionRepo;
    private final ObjectMapper objectMapper;
    
    // Flow Handlers
    private final SalesFlowHandler salesFlowHandler;
    private final OrderFlowHandler orderFlowHandler;
    private final WarrantyFlowHandler warrantyFlowHandler;
    private final AccountFlowHandler accountFlowHandler;
    
    private static final int MAX_STEPS_PER_FLOW = 10;
    private static final Set<String> ALLOWED_BUSINESS_ROLES = Set.of(
        "SALES", "ORDER", "WARRANTY", "ACCOUNT", "SUPPORT"
    );
    
    /**
     * Main entry point: Process message in context of current flow
     */
    @Transactional
    public ChatbotResponse processFlow(
            ChatSession session,
            IntentMatch intent,
            String userMessage,
            UUID khachHangId
    ) {
        log.info("🔄 [ConversationFlow] Processing flow - Current State: {}, Intent: {}", 
                session.getCurrentState(), intent != null ? intent.getIntentCode() : "NONE");
        
        try {
            // 1. Validate business role
            if (intent != null && !isAllowedRole(intent.getIntent().getBusinessRole())) {
                return redirectToSupport(session);
            }
            
            // 2. Determine next state
            ConversationState currentState = session.getCurrentState() != null 
                ? session.getCurrentState() 
                : ConversationState.IDLE;
            
            ConversationState nextState = determineNextState(currentState, intent, session);
            
            // 3. Validate transition
            if (!isValidTransition(currentState, nextState)) {
                log.warn("⚠️ [ConversationFlow] Invalid transition {} -> {}", currentState, nextState);
                return handleInvalidTransition(session);
            }
            
            // 4. Check authentication requirements
            if (nextState.requiresAuth() && khachHangId == null) {
                return requestLogin(session, nextState);
            }
            
            // 5. Update session state
            updateSessionState(session, nextState, intent);
            
            // 6. Execute state handler
            return executeState(session, nextState, userMessage, khachHangId, intent);
            
        } catch (Exception e) {
            log.error("❌ [ConversationFlow] Error processing flow", e);
            return ChatbotResponse.simpleResponse(
                "Xin lỗi, mình gặp sự cố kỹ thuật. Vui lòng thử lại!",
                "ERROR",
                BigDecimal.ZERO
            );
        }
    }
    
    /**
     * Determine next state based on current state and intent
     */
    private ConversationState determineNextState(
            ConversationState currentState,
            IntentMatch intent,
            ChatSession session
    ) {
        // If intent explicitly defines next state
        if (intent != null && Boolean.TRUE.equals(intent.getIntent().getTriggersStateChange())) {
            return intent.getIntent().getNextState();
        }
        
        // State-based routing
        switch (currentState) {
            case IDLE:
            case GREETING:
                // First interaction - route based on intent
                if (intent != null && intent.getIntent().getNextState() != null) {
                    return intent.getIntent().getNextState();
                }
                return ConversationState.GREETING;
                
            case SALES_IDENTIFY_NEED:
                return ConversationState.SALES_SHOW_OPTIONS;
                
            case SALES_SHOW_OPTIONS:
                // User selected a product or wants to compare
                String message = getLastUserMessage(session);
                if (message != null && message.toLowerCase().contains("so sánh")) {
                    return ConversationState.SALES_COMPARE;
                }
                return ConversationState.SALES_DETAIL;
                
            case SALES_DETAIL:
            case SALES_COMPARE:
                return ConversationState.SALES_CTA;
                
            case ORDER_REQUEST_CODE:
                return ConversationState.ORDER_SHOW_STATUS;
                
            case ORDER_SHOW_STATUS:
                return ConversationState.ORDER_ACTIONS;
                
            case WARRANTY_CHECK_LOGIN:
                return ConversationState.WARRANTY_SELECT_PRODUCT;
                
            case WARRANTY_SELECT_PRODUCT:
                return ConversationState.WARRANTY_CHECK_STATUS;
                
            case WARRANTY_CHECK_STATUS:
                return ConversationState.WARRANTY_CREATE_REQUEST;
                
            case ACCOUNT_CHECK_LOGIN:
                return ConversationState.ACCOUNT_GUIDE;
                
            default:
                // Terminal states go back to IDLE
                if (currentState.isTerminal()) {
                    return ConversationState.IDLE;
                }
                return currentState;
        }
    }
    
    /**
     * Validate if state transition is allowed
     */
    private boolean isValidTransition(ConversationState from, ConversationState to) {
        // Same state is always valid (retry)
        if (from == to) return true;
        
        // From terminal to IDLE is valid
        if (from.isTerminal() && to == ConversationState.IDLE) return true;
        
        // Can always go to ESCALATED
        if (to == ConversationState.ESCALATED) return true;
        
        // Must stay within same category (can't jump from SALES to WARRANTY)
        if (!from.getCategory().equals(to.getCategory()) && 
            !to.getCategory().equals("INITIAL") &&
            !from.getCategory().equals("INITIAL")) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Execute the current state handler
     */
    private ChatbotResponse executeState(
            ChatSession session,
            ConversationState state,
            String userMessage,
            UUID khachHangId,
            IntentMatch intent
    ) {
        log.info("▶️ [ConversationFlow] Executing state: {}", state);
        
        switch (state.getCategory()) {
            case "SALES":
                return salesFlowHandler.handle(session, state, userMessage, khachHangId);
                
            case "ORDER":
                return orderFlowHandler.handle(session, state, userMessage, khachHangId);
                
            case "WARRANTY":
                return warrantyFlowHandler.handle(session, state, userMessage, khachHangId);
                
            case "ACCOUNT":
                return accountFlowHandler.handle(session, state, userMessage, khachHangId);
                
            case "INITIAL":
                return handleInitialState(session, state);
                
            case "SUPPORT":
                return handleSupportState(session, state);
                
            default:
                return ChatbotResponse.simpleResponse(
                    "Mình có thể giúp bạn về: Sản PhẩmĐơn hàng, Bảo hành, Tài khoản. Bạn cần gì ạ?",
                    state.name(),
                    BigDecimal.valueOf(0.5)
                );
        }
    }
    
    /**
     * Handle initial states (IDLE, GREETING)
     */
    private ChatbotResponse handleInitialState(ChatSession session, ConversationState state) {
        List<QuickReplyDTO> mainMenu = Arrays.asList(
            QuickReplyDTO.builder()
                .replyText("🛒 Mua laptop")
                .replyValue("SALES")
                .replyType("INTENT")
                .icon("🛒")
                .build(),
            QuickReplyDTO.builder()
                .replyText("📦 Kiểm tra đơn hàng")
                .replyValue("ORDER")
                .replyType("INTENT")
                .icon("📦")
                .build(),
            QuickReplyDTO.builder()
                .replyText("🛠️ Bảo hành")
                .replyValue("WARRANTY")
                .replyType("INTENT")
                .icon("🛠️")
                .build(),
            QuickReplyDTO.builder()
                .replyText("👤 Tài khoản")
                .replyValue("ACCOUNT")
                .replyType("INTENT")
                .icon("👤")
                .build()
        );
        
        return ChatbotResponse.builder()
                .responseText(
                    "Xin chào! Mình là trợ lý ảo của LaptopShop 👋\n\n" +
                    "Mình có thể giúp bạn:\n" +
                    "🛒 Tư vấn mua laptop\n" +
                    "📦 Tra cứu đơn hàng\n" +
                    "🛠️ Bảo hành sản phẩm\n" +
                    "👤 Quản lý tài khoản\n\n" +
                    "Bạn cần hỗ trợ gì ạ?"
                )
                .quickReplies(mainMenu)
                .intentCode("GREETING")
                .confidence(BigDecimal.ONE)
                .shouldSave(true)
                .shouldEscalate(false)
                .build();
    }
    
    /**
     * Handle support states
     */
    private ChatbotResponse handleSupportState(ChatSession session, ConversationState state) {
        if (state == ConversationState.ESCALATED) {
            return ChatbotResponse.escalateResponse(
                "Mình sẽ kết nối bạn với nhân viên tư vấn. Vui lòng đợi trong giây lát...",
                "User request human support"
            );
        }
        
        return handleInitialState(session, ConversationState.GREETING);
    }
    
    /**
     * Update session with new state
     */
    @Transactional
    public void updateSessionState(
            ChatSession session,
            ConversationState newState,
            IntentMatch intent
    ) {
        session.setCurrentState(newState);
        session.setLastActivity(Instant.now());
        
        // Update goal based on state category
        if (!newState.getCategory().equals("INITIAL") && !newState.getCategory().equals("SUPPORT")) {
            session.setGoal(newState.getCategory());
        }
        
        // Increment step count
        Integer currentSteps = session.getStepCount() != null ? session.getStepCount() : 0;
        session.setStepCount(currentSteps + 1);
        
        // Check if stuck (too many steps)
        if (currentSteps >= MAX_STEPS_PER_FLOW) {
            session.setIsStuck(true);
            log.warn("⚠️ [ConversationFlow] Session {} appears stuck after {} steps", 
                    session.getId(), currentSteps);
        }
        
        sessionRepo.save(session);
    }
    
    /**
     * Request user to login
     */
    private ChatbotResponse requestLogin(ChatSession session, ConversationState targetState) {
        List<QuickReplyDTO> loginOptions = Arrays.asList(
            QuickReplyDTO.builder()
                .replyText("🔓 Đăng nhập")
                .replyValue("LOGIN")
                .replyType("ACTION")
                .icon("🔓")
                .build(),
            QuickReplyDTO.builder()
                .replyText("💬 Tư vấn với nhân viên")
                .replyValue("ESCALATE")
                .replyType("ACTION")
                .icon("💬")
                .build()
        );
        
        return ChatbotResponse.builder()
                .responseText(
                    "Để sử dụng tính năng này, bạn cần đăng nhập nhé! 🔐\n\n" +
                    "Sau khi đăng nhập, bạn có thể:\n" +
                    "• Xem lịch sử đơn hàng\n" +
                    "• Quản lý bảo hành\n" +
                    "• Cập nhật thông tin tài khoản"
                )
                .quickReplies(loginOptions)
                .intentCode("REQUIRE_AUTH")
                .confidence(BigDecimal.ONE)
                .shouldSave(false)
                .shouldEscalate(false)
                .build();
    }
    
    /**
     * Redirect to human support
     */
    private ChatbotResponse redirectToSupport(ChatSession session) {
        return ChatbotResponse.escalateResponse(
            "Câu hỏi này nằm ngoài phạm vi hỗ trợ tự động của mình.\n" +
            "Mình sẽ kết nối bạn với nhân viên tư vấn để được hỗ trợ tốt hơn! 👨‍💼",
            "Out of scope question"
        );
    }
    
    /**
     * Handle invalid transition
     */
    private ChatbotResponse handleInvalidTransition(ChatSession session) {
        // Reset to IDLE
        session.setCurrentState(ConversationState.IDLE);
        session.setGoal(null);
        session.setStepCount(0);
        sessionRepo.save(session);
        
        return handleInitialState(session, ConversationState.GREETING);
    }
    
    /**
     * Check if business role is allowed
     */
    private boolean isAllowedRole(String role) {
        return role != null && ALLOWED_BUSINESS_ROLES.contains(role);
    }
    
    /**
     * Get last user message from progress data
     */
    private String getLastUserMessage(ChatSession session) {
        try {
            if (session.getProgressData() != null) {
                Map<String, Object> progress = objectMapper.readValue(
                    session.getProgressData(),
                    new TypeReference<Map<String, Object>>() {}
                );
                return (String) progress.get("last_user_message");
            }
        } catch (Exception e) {
            log.debug("Could not parse progress data", e);
        }
        return null;
    }
}

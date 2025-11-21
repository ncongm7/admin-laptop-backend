package com.example.backendlaptop.expection;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Bắt lỗi validate @Valid
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        Map<String, Object> response = new HashMap<>();
        response.put("status", "FAILED");
        response.put("code", "VALIDATION_ERROR");
        response.put("errors", errors);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // Bắt lỗi do bạn tự throw new ApiException
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<?> handleApiException(ApiException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "FAILED");
        response.put("code", ex.getCode());
        response.put("message", ex.getMessage());

        // Xác định HTTP status code dựa trên error code
        HttpStatus status = HttpStatus.BAD_REQUEST; // Mặc định
        if ("ACCOUNT_LOCKED".equals(ex.getCode()) || 
            "ACCOUNT_NOT_ACTIVATED".equals(ex.getCode()) || 
            "ACCOUNT_DISABLED".equals(ex.getCode())) {
            status = HttpStatus.FORBIDDEN; // 403 Forbidden cho tài khoản bị khóa
        } else if ("INVALID_CREDENTIALS".equals(ex.getCode()) || 
                   "INVALID_TOKEN".equals(ex.getCode())) {
            status = HttpStatus.UNAUTHORIZED; // 401 Unauthorized cho sai thông tin đăng nhập
        } else if ("EMAIL_AUTH_FAILED".equals(ex.getCode()) || 
                   "EMAIL_SEND_FAILED".equals(ex.getCode())) {
            status = HttpStatus.INTERNAL_SERVER_ERROR; // 500 cho lỗi email
        }

        return new ResponseEntity<>(response, status);
    }

    // Bắt tất cả lỗi còn lại
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleAll(Exception ex) {
        // Log chi tiết lỗi
        System.err.println("[GlobalExceptionHandler] Lỗi không mong đợi:");
        System.err.println("  - Exception Type: " + ex.getClass().getName());
        System.err.println("  - Message: " + ex.getMessage());
        System.err.println("  - Stack Trace:");
        ex.printStackTrace();
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "FAILED");
        response.put("code", "INTERNAL_ERROR");
        response.put("message", ex.getMessage());
        response.put("exceptionType", ex.getClass().getSimpleName());

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}

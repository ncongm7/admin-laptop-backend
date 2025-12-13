-- ===================================================================
-- CHATBOT REDESIGN - SEED DATA (SIMPLE VERSION)
-- ===================================================================

-- SALES_LAPTOP_NEED
IF NOT EXISTS (SELECT 1 FROM chat_intents WHERE intent_code = 'SALES_LAPTOP_NEED')
BEGIN
    INSERT INTO chat_intents (intent_code, intent_name, category, business_role, keywords, sample_questions, auto_response_template, confidence_threshold, requires_data, is_active, priority, triggers_state_change, next_state, requires_auth)
    VALUES ('SALES_LAPTOP_NEED', 'Muốn mua laptop', 'SALES', 'SALES', '["mua laptop","muốn mua","tìm laptop","cần laptop","laptop nào tốt","mua máy tính"]', '["Tôi muốn mua laptop","Tìm laptop","Cần mua laptop"]', 'Bạn cần laptop để làm gì nhỉ?', 0.70, 0, 1, 10, 1, 'SALES_IDENTIFY_NEED', 0);
END
ELSE
BEGIN
    UPDATE chat_intents SET business_role = 'SALES', next_state = 'SALES_IDENTIFY_NEED', triggers_state_change = 1 WHERE intent_code = 'SALES_LAPTOP_NEED';
END

-- SALES_GAMING
IF NOT EXISTS (SELECT 1 FROM chat_intents WHERE intent_code = 'SALES_GAMING')
BEGIN
    INSERT INTO chat_intents (intent_code, intent_name, category, business_role, keywords, sample_questions, auto_response_template, confidence_threshold, requires_data, is_active, priority, triggers_state_change, next_state, requires_auth)
    VALUES ('SALES_GAMING', 'Laptop gaming', 'SALES', 'SALES', '["laptop gaming","laptop chơi game","máy chơi game","gaming laptop","laptop game"]', '["Laptop gaming","Laptop chơi game nào tốt?"]', 'Mình sẽ gợi ý laptop gaming phù hợp cho bạn!', 0.70, 1, 1, 10, 1, 'SALES_SHOW_OPTIONS', 0);
END
ELSE
BEGIN
    UPDATE chat_intents SET business_role = 'SALES', next_state = 'SALES_SHOW_OPTIONS', triggers_state_change = 1 WHERE intent_code = 'SALES_GAMING';
END

-- SALES_OFFICE
IF NOT EXISTS (SELECT 1 FROM chat_intents WHERE intent_code = 'SALES_OFFICE')
BEGIN
    INSERT INTO chat_intents (intent_code, intent_name, category, business_role, keywords, sample_questions, auto_response_template, confidence_threshold, requires_data, is_active, priority, triggers_state_change, next_state, requires_auth)
    VALUES ('SALES_OFFICE', 'Laptop văn phòng', 'SALES', 'SALES', '["laptop văn phòng","máy văn phòng","laptop làm việc","office laptop"]', '["Laptop văn phòng","Laptop để làm việc"]', 'Mình sẽ gợi ý laptop văn phòng cho bạn!', 0.70, 1, 1, 10, 1, 'SALES_SHOW_OPTIONS', 0);
END
ELSE
BEGIN
    UPDATE chat_intents SET business_role = 'SALES', next_state = 'SALES_SHOW_OPTIONS', triggers_state_change = 1 WHERE intent_code = 'SALES_OFFICE';
END

-- PRODUCT_PRICE
IF NOT EXISTS (SELECT 1 FROM chat_intents WHERE intent_code = 'PRODUCT_PRICE')
BEGIN
    INSERT INTO chat_intents (intent_code, intent_name, category, business_role, keywords, sample_questions, auto_response_template, confidence_threshold, requires_data, is_active, priority, triggers_state_change, next_state, requires_auth)
    VALUES ('PRODUCT_PRICE', 'Hỏi giá sản phẩm', 'SALES', 'SALES', '["giá laptop","giá bao nhiêu","price","cost","giá máy","bao nhiêu tiền"]', '["Giá laptop này bao nhiêu?","Giá sản phẩm?"]', '{product_info}', 0.70, 1, 1, 8, 0, NULL, 0);
END
ELSE
BEGIN
    UPDATE chat_intents SET business_role = 'SALES', triggers_state_change = 0 WHERE intent_code = 'PRODUCT_PRICE';
END

-- ORDER_CHECK_STATUS
IF NOT EXISTS (SELECT 1 FROM chat_intents WHERE intent_code = 'ORDER_CHECK_STATUS')
BEGIN
    INSERT INTO chat_intents (intent_code, intent_name, category, business_role, keywords, sample_questions, auto_response_template, confidence_threshold, requires_data, is_active, priority, triggers_state_change, next_state, requires_auth)
    VALUES ('ORDER_CHECK_STATUS', 'Kiểm tra đơn hàng', 'ORDER', 'ORDER', '["kiểm tra đơn","đơn hàng","tra cứu đơn","order status","đơn của tôi","xem đơn hàng"]', '["Kiểm tra đơn hàng","Đơn hàng của tôi ở đâu?","Xem đơn hàng"]', 'Vui lòng cung cấp mã đơn hàng để mình tra cứu giúp bạn!', 0.70, 0, 1, 10, 1, 'ORDER_REQUEST_CODE', 0);
END
ELSE
BEGIN
    UPDATE chat_intents SET business_role = 'ORDER', next_state = 'ORDER_REQUEST_CODE', triggers_state_change = 1 WHERE intent_code = 'ORDER_CHECK_STATUS';
END

-- ORDER_TRACK_DELIVERY
IF NOT EXISTS (SELECT 1 FROM chat_intents WHERE intent_code = 'ORDER_TRACK_DELIVERY')
BEGIN
    INSERT INTO chat_intents (intent_code, intent_name, category, business_role, keywords, sample_questions, auto_response_template, confidence_threshold, requires_data, is_active, priority, triggers_state_change, next_state, requires_auth)
    VALUES ('ORDER_TRACK_DELIVERY', 'Theo dõi giao hàng', 'ORDER', 'ORDER', '["giao hàng","shipper","vận chuyển","delivery","khi nào đến","đã giao chưa"]', '["Đơn hàng khi nào đến?","Kiểm tra giao hàng"]', 'Mình sẽ kiểm tra tình trạng giao hàng cho bạn!', 0.70, 1, 1, 9, 1, 'ORDER_REQUEST_CODE', 0);
END
ELSE
BEGIN
    UPDATE chat_intents SET business_role = 'ORDER', next_state = 'ORDER_REQUEST_CODE', triggers_state_change = 1 WHERE intent_code = 'ORDER_TRACK_DELIVERY';
END

-- WARRANTY_CHECK
IF NOT EXISTS (SELECT 1 FROM chat_intents WHERE intent_code = 'WARRANTY_CHECK')
BEGIN
    INSERT INTO chat_intents (intent_code, intent_name, category, business_role, keywords, sample_questions, auto_response_template, confidence_threshold, requires_data, is_active, priority, triggers_state_change, next_state, requires_auth)
    VALUES ('WARRANTY_CHECK', 'Kiểm tra bảo hành', 'WARRANTY', 'WARRANTY', '["bảo hành","warranty","bảo hàng","bh","thời gian bảo hành","còn bảo hành"]', '["Kiểm tra bảo hành","Sản phẩm còn bảo hành không?"]', 'Để kiểm tra bảo hành, bạn cần đăng nhập nhé!', 0.70, 0, 1, 10, 1, 'WARRANTY_CHECK_LOGIN', 1);
END
ELSE
BEGIN
    UPDATE chat_intents SET business_role = 'WARRANTY', next_state = 'WARRANTY_CHECK_LOGIN', requires_auth = 1, triggers_state_change = 1 WHERE intent_code = 'WARRANTY_CHECK';
END

-- WARRANTY_CREATE
IF NOT EXISTS (SELECT 1 FROM chat_intents WHERE intent_code = 'WARRANTY_CREATE')
BEGIN
    INSERT INTO chat_intents (intent_code, intent_name, category, business_role, keywords, sample_questions, auto_response_template, confidence_threshold, requires_data, is_active, priority, triggers_state_change, next_state, requires_auth)
    VALUES ('WARRANTY_CREATE', 'Tạo yêu cầu bảo hành', 'WARRANTY', 'WARRANTY', '["tạo phiếu bảo hành","yêu cầu bảo hành","gửi bảo hành","máy hỏng","laptop lỗi"]', '["Tạo phiếu bảo hành","Laptop bị lỗi cần bảo hành"]', 'Mình sẽ hướng dẫn bạn tạo yêu cầu bảo hành!', 0.70, 0, 1, 10, 1, 'WARRANTY_CHECK_LOGIN', 1);
END
ELSE
BEGIN
    UPDATE chat_intents SET business_role = 'WARRANTY', next_state = 'WARRANTY_CHECK_LOGIN', requires_auth = 1, triggers_state_change = 1 WHERE intent_code = 'WARRANTY_CREATE';
END

-- ACCOUNT_MANAGE
IF NOT EXISTS (SELECT 1 FROM chat_intents WHERE intent_code = 'ACCOUNT_MANAGE')
BEGIN
    INSERT INTO chat_intents (intent_code, intent_name, category, business_role, keywords, sample_questions, auto_response_template, confidence_threshold, requires_data, is_active, priority, triggers_state_change, next_state, requires_auth)
    VALUES ('ACCOUNT_MANAGE', 'Quản lý tài khoản', 'ACCOUNT', 'ACCOUNT', '["tài khoản","account","profile","thông tin cá nhân","cập nhật thông tin"]', '["Quản lý tài khoản","Cập nhật thông tin","Xem profile"]', 'Bạn muốn quản lý thông tin tài khoản ạ?', 0.70, 0, 1, 8, 1, 'ACCOUNT_CHECK_LOGIN', 1);
END
ELSE
BEGIN
    UPDATE chat_intents SET business_role = 'ACCOUNT', next_state = 'ACCOUNT_CHECK_LOGIN', requires_auth = 1, triggers_state_change = 1 WHERE intent_code = 'ACCOUNT_MANAGE';
END

-- ACCOUNT_PASSWORD
IF NOT EXISTS (SELECT 1 FROM chat_intents WHERE intent_code = 'ACCOUNT_PASSWORD')
BEGIN
    INSERT INTO chat_intents (intent_code, intent_name, category, business_role, keywords, sample_questions, auto_response_template, confidence_threshold, requires_data, is_active, priority, triggers_state_change, next_state, requires_auth)
    VALUES ('ACCOUNT_PASSWORD', 'Đổi mật khẩu', 'ACCOUNT', 'ACCOUNT', '["đổi mật khẩu","change password","quên mật khẩu","reset password","password"]', '["Đổi mật khẩu","Quên mật khẩu","Reset password"]', 'Để đổi mật khẩu, bạn cần đăng nhập nhé!', 0.70, 0, 1, 9, 1, 'ACCOUNT_CHECK_LOGIN', 1);
END
ELSE
BEGIN
    UPDATE chat_intents SET business_role = 'ACCOUNT', next_state = 'ACCOUNT_CHECK_LOGIN', requires_auth = 1, triggers_state_change = 1 WHERE intent_code = 'ACCOUNT_PASSWORD';
END

-- ACCOUNT_ADDRESS
IF NOT EXISTS (SELECT 1 FROM chat_intents WHERE intent_code = 'ACCOUNT_ADDRESS')
BEGIN
    INSERT INTO chat_intents (intent_code, intent_name, category, business_role, keywords, sample_questions, auto_response_template, confidence_threshold, requires_data, is_active, priority, triggers_state_change, next_state, requires_auth)
    VALUES ('ACCOUNT_ADDRESS', 'Quản lý địa chỉ', 'ACCOUNT', 'ACCOUNT', '["địa chỉ","address","giao hàng","shipping address","địa chỉ giao hàng"]', '["Cập nhật địa chỉ","Thêm địa chỉ giao hàng"]', 'Bạn muốn cập nhật địa chỉ giao hàng?', 0.70, 0, 1, 8, 1, 'ACCOUNT_CHECK_LOGIN', 1);
END
ELSE
BEGIN
    UPDATE chat_intents SET business_role = 'ACCOUNT', next_state = 'ACCOUNT_CHECK_LOGIN', requires_auth = 1, triggers_state_change = 1 WHERE intent_code = 'ACCOUNT_ADDRESS';
END

-- GREETING
IF NOT EXISTS (SELECT 1 FROM chat_intents WHERE intent_code = 'GREETING')
BEGIN
    INSERT INTO chat_intents (intent_code, intent_name, category, business_role, keywords, sample_questions, auto_response_template, confidence_threshold, requires_data, is_active, priority, triggers_state_change, next_state, requires_auth)
    VALUES ('GREETING', 'Chào hỏi', 'SUPPORT', 'SUPPORT', '["xin chào","chào","hello","hi","hey","chào bạn"]', '["Xin chào","Hi","Hello","Chào bạn"]', 'Xin chào! Mình là trợ lý ảo của LaptopShop. Mình có thể giúp bạn về: Sản phẩm, Đơn hàng, Bảo hành, Tài khoản. Bạn cần gì ạ?', 0.70, 0, 1, 5, 1, 'GREETING', 0);
END
ELSE
BEGIN
    UPDATE chat_intents SET business_role = 'SUPPORT', next_state = 'GREETING', triggers_state_change = 1 WHERE intent_code = 'GREETING';
END

-- QUICK REPLIES
DELETE FROM chat_quick_replies WHERE intent_code = 'GREETING';

INSERT INTO chat_quick_replies (intent_code, reply_text, reply_value, reply_type, display_order, icon, is_active)
VALUES ('GREETING', '🛒 Mua laptop', 'SALES', 'INTENT', 1, '🛒', 1);
INSERT INTO chat_quick_replies (intent_code, reply_text, reply_value, reply_type, display_order, icon, is_active)
VALUES ('GREETING', '📦 Kiểm tra đơn hàng', 'ORDER', 'INTENT', 2, '📦', 1);
INSERT INTO chat_quick_replies (intent_code, reply_text, reply_value, reply_type, display_order, icon, is_active)
VALUES ('GREETING', '🛠️ Bảo hành', 'WARRANTY', 'INTENT', 3, '🛠️', 1);
INSERT INTO chat_quick_replies (intent_code, reply_text, reply_value, reply_type, display_order, icon, is_active)
VALUES ('GREETING', '👤 Tài khoản', 'ACCOUNT', 'INTENT', 4, '👤', 1);

PRINT 'Seed data completed!';
GO

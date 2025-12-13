package com.example.backendlaptop.scheduler;

import com.example.backendlaptop.entity.HoaDon;
import com.example.backendlaptop.repository.SerialRepository;
import com.example.backendlaptop.service.customer.CustomerOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ReservationCleanupScheduler {

    private final SerialRepository serialRepository;
    private final CustomerOrderService customerOrderService;

    // Chạy mỗi 1 phút (60000 ms)
    @Scheduled(fixedRate = 60000)
    public void cleanupExpiredReservations() {
        try {
            Instant now = Instant.now();
            List<HoaDon> expiredOrders = serialRepository.findOrdersWithExpiredReservations(now);

            if (!expiredOrders.isEmpty()) {
                System.out.println("⏰ [Scheduler] Tìm thấy " + expiredOrders.size() + " đơn hàng hết hạn giữ chỗ.");
                for (HoaDon order : expiredOrders) {
                    customerOrderService.cancelOrderSystem(order.getId(), "Hết thời gian giữ hàng (30 phút)");
                }
            }
        } catch (Exception e) {
            System.err.println("❌ [Scheduler] Lỗi khi chạy cleanup task: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

package com.dattakrupa.laundry.serviceImpl;

import com.dattakrupa.laundry.dto.DashboardResponseDTO;
import com.dattakrupa.laundry.enums.OrderStatus;
import com.dattakrupa.laundry.repository.*;
import com.dattakrupa.laundry.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private UdhariLedgerRepository udhariLedgerRepository;

    @Override
    public DashboardResponseDTO getDashboardData() {

        // Today ka start aur end time
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().plusDays(1).atStartOfDay();

        return DashboardResponseDTO.builder()
                // ✅ Fix — Today orders count
                .todayOrders((long) orderRepository
                        .findByCreatedAtBetween(todayStart, todayEnd).size())

                .todayRevenue(orderRepository.getTodayRevenue())
                .todayOnlinePayments(paymentRepository.getTodayOnlinePayments())
                .todayCashPayments(paymentRepository.getTodayCashPayments())

                .receivedOrders((long) orderRepository
                        .findByStatus(OrderStatus.RECEIVED).size())
                .washingOrders((long) orderRepository
                        .findByStatus(OrderStatus.WASHING).size())
                .dryingOrders((long) orderRepository
                        .findByStatus(OrderStatus.DRYING).size())
                .readyOrders((long) orderRepository
                        .findByStatus(OrderStatus.READY).size())
                .deliveredOrders((long) orderRepository
                        .findByStatus(OrderStatus.DELIVERED).size())

                .totalPendingUdhari(udhariLedgerRepository.getTotalPendingUdhari())
                .customersWithUdhari((long) customerRepository
                        .findByTotalDueGreaterThan(0.0).size())

                .totalCustomers(customerRepository.count())
                .totalOrders(orderRepository.count())
                .build();
    }
}
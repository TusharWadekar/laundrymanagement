package com.dattakrupa.laundry.serviceImpl;

import com.dattakrupa.laundry.dto.DashboardResponseDTO;
import com.dattakrupa.laundry.enums.OrderStatus;
import com.dattakrupa.laundry.repository.*;
import com.dattakrupa.laundry.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private  OrderRepository orderRepository;
    @Autowired
    private  CustomerRepository customerRepository;
    @Autowired
    private  PaymentRepository paymentRepository;
    @Autowired
    private  UdhariLedgerRepository udhariLedgerRepository;

    @Override
    public DashboardResponseDTO getDashboardData() {
        return DashboardResponseDTO.builder()
                // Aaj ka
                .todayRevenue(orderRepository.getTodayRevenue())
                .todayOnlinePayments(paymentRepository.getTodayOnlinePayments())
                .todayCashPayments(paymentRepository.getTodayCashPayments())

                // Orders status
                .receivedOrders((long) orderRepository.findByStatus(OrderStatus.RECEIVED).size())
                .washingOrders((long) orderRepository.findByStatus(OrderStatus.WASHING).size())
                .dryingOrders((long) orderRepository.findByStatus(OrderStatus.DRYING).size())
                .readyOrders((long) orderRepository.findByStatus(OrderStatus.READY).size())
                .deliveredOrders((long) orderRepository.findByStatus(OrderStatus.DELIVERED).size())

                // Udhari
                .totalPendingUdhari(udhariLedgerRepository.getTotalPendingUdhari())
                .customersWithUdhari((long) customerRepository.findByTotalDueGreaterThan(0.0).size())

                // Total
                .totalCustomers(customerRepository.count())
                .totalOrders(orderRepository.count())
                .build();
    }
}
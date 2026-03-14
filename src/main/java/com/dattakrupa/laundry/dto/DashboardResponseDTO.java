package com.dattakrupa.laundry.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardResponseDTO {

    // Aaj ka
    private Long todayOrders;
    private Double todayRevenue;
    private Double todayOnlinePayments;
    private Double todayCashPayments;

    // Orders status count
    private Long receivedOrders;
    private Long washingOrders;
    private Long dryingOrders;
    private Long readyOrders;
    private Long deliveredOrders;

    // Udhari
    private Double totalPendingUdhari;
    private Long customersWithUdhari;

    // Total
    private Long totalCustomers;
    private Long totalOrders;
}
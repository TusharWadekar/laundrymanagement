package com.dattakrupa.laundry.service;

import com.dattakrupa.laundry.dto.OrderRequestDTO;
import com.dattakrupa.laundry.dto.OrderResponseDTO;
import com.dattakrupa.laundry.enums.OrderStatus;

import java.util.List;

public interface OrderService {

    // Naya order banao
    OrderResponseDTO createOrder(OrderRequestDTO request);

    // Sab orders lo
    List<OrderResponseDTO> getAllOrders();

    // ID se order dhundo
    OrderResponseDTO getOrderById(Long id);

    // Customer ke orders
    List<OrderResponseDTO> getOrdersByCustomer(Long customerId);

    // Status se filter karo
    List<OrderResponseDTO> getOrdersByStatus(OrderStatus status);

    // Order status update karo
    OrderResponseDTO updateOrderStatus(Long id, OrderStatus status);

    // WhatsApp pe bill bhejo + Razorpay link banao
    void sendBillOnWhatsApp(Long orderId);

    // Cash payment mark karo
    OrderResponseDTO markCashPayment(Long orderId, Double amount);

    // Order delete karo
    void deleteOrder(Long id);
}
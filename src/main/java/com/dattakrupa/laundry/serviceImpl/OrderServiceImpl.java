package com.dattakrupa.laundry.serviceImpl;

import com.dattakrupa.laundry.dto.*;
import com.dattakrupa.laundry.enums.OrderStatus;
import com.dattakrupa.laundry.enums.PaymentStatus;
import com.dattakrupa.laundry.model.*;
import com.dattakrupa.laundry.repository.*;
import com.dattakrupa.laundry.service.OrderService;
import com.dattakrupa.laundry.service.PaymentService;
import com.dattakrupa.laundry.service.WhatsAppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private  OrderRepository orderRepository;
    @Autowired
    private  CustomerRepository customerRepository;

    @Autowired
    private  OrderItemRepository orderItemRepository;

    @Autowired
    private  WhatsAppService whatsAppService;
    @Autowired
    private  PaymentService paymentService;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Override
    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO request) {

        // Customer dhundo
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException(
                        "Customer nahi mila ID: " + request.getCustomerId()));

        // Order banao
        Order order = Order.builder()
                .customer(customer)
                .status(OrderStatus.RECEIVED)
                .paymentStatus(PaymentStatus.UNPAID)
                .expectedDelivery(request.getExpectedDelivery())
                .notes(request.getNotes())
                .totalAmount(0.0)
                .paidAmount(0.0)
                .build();

        Order savedOrder = orderRepository.save(order);

        // Items add karo aur total calculate karo
        double total = 0.0;
        for (OrderItemRequestDTO itemDTO : request.getItems()) {
            OrderItem item = OrderItem.builder()
                    .order(savedOrder)
                    .itemName(itemDTO.getItemName())
                    .quantity(itemDTO.getQuantity())
                    .pricePerItem(itemDTO.getPricePerItem())
                    .subtotal(itemDTO.getQuantity() * itemDTO.getPricePerItem())
                    .build();
            orderItemRepository.save(item);
            total += item.getSubtotal();
        }

        // Total update karo
        savedOrder.setTotalAmount(total);
        orderRepository.save(savedOrder);

        log.info("Naya order create hua #{} customer: {}",
                savedOrder.getId(), customer.getName());

        return mapToResponse(savedOrder);
    }

    @Override
    public List<OrderResponseDTO> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public OrderResponseDTO getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order nahi mila ID: " + id));
        return mapToResponse(order);
    }

    @Override
    public List<OrderResponseDTO> getOrdersByCustomer(Long customerId) {
        return orderRepository.findByCustomerId(customerId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderResponseDTO> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrderResponseDTO updateOrderStatus(Long id, OrderStatus status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order nahi mila ID: " + id));

        order.setStatus(status);
        Order updated = orderRepository.save(order);

        log.info("Order #{} status update hua: {}", id, status);
        return mapToResponse(updated);
    }

    @Override
    public void sendBillOnWhatsApp(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException(
                        "Order nahi mila ID: " + orderId));

        // Razorpay payment link banao
        Map<String, Object> razorpayData =
                paymentService.createRazorpayOrder(orderId);

        String paymentLink = frontendUrl + "/pay/" + orderId;

        // WhatsApp bill bhejo
        whatsAppService.sendBillMessage(order, paymentLink);
        log.info("WhatsApp bill bheja jayega Order #{}", orderId);
    }

    @Override
    @Transactional
    public OrderResponseDTO markCashPayment(Long orderId, Double amount) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order nahi mila ID: " + orderId));

        order.setPaidAmount(order.getPaidAmount() + amount);

        // Payment status update karo
        if (order.getPaidAmount() >= order.getTotalAmount()) {
            order.setPaymentStatus(PaymentStatus.PAID);
            order.setPaidAmount(order.getTotalAmount());

            // Customer ki udhari update karo
            Customer customer = order.getCustomer();
            if (customer.getTotalDue() > 0) {
                customer.setTotalDue(Math.max(0, customer.getTotalDue() - amount));
                customerRepository.save(customer);
            }
        } else {
            order.setPaymentStatus(PaymentStatus.PARTIAL);
        }

        Order updated = orderRepository.save(order);
        log.info("Cash payment mark hua Order #{}: ₹{}", orderId, amount);

        return mapToResponse(updated);
    }

    @Override
    public void deleteOrder(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new RuntimeException("Order nahi mila ID: " + id);
        }
        orderRepository.deleteById(id);
        log.info("Order delete hua ID: {}", id);
    }

    // Entity → DTO
    private OrderResponseDTO mapToResponse(Order order) {
        List<OrderItemResponseDTO> itemDTOs = order.getItems() != null
                ? order.getItems().stream().map(item ->
                    OrderItemResponseDTO.builder()
                        .id(item.getId())
                        .itemName(item.getItemName())
                        .quantity(item.getQuantity())
                        .pricePerItem(item.getPricePerItem())
                        .subtotal(item.getSubtotal())
                        .build()
                ).collect(Collectors.toList())
                : List.of();

        return OrderResponseDTO.builder()
                .id(order.getId())
                .customerId(order.getCustomer().getId())
                .customerName(order.getCustomer().getName())
                .customerPhone(order.getCustomer().getPhoneNumber())
                .status(order.getStatus())
                .paymentStatus(order.getPaymentStatus())
                .totalAmount(order.getTotalAmount())
                .paidAmount(order.getPaidAmount())
                .dueAmount(order.getTotalAmount() - order.getPaidAmount())
                .items(itemDTOs)
                .notes(order.getNotes())
                .expectedDelivery(order.getExpectedDelivery())
                .createdAt(order.getCreatedAt())
                .razorpayOrderId(order.getRazorpayOrderId())
                .build();
    }

}
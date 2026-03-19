package com.dattakrupa.laundry.serviceImpl;

import com.dattakrupa.laundry.dto.*;
import com.dattakrupa.laundry.enums.OrderStatus;
import com.dattakrupa.laundry.enums.PaymentStatus;
import com.dattakrupa.laundry.exception.BadRequestException;
import com.dattakrupa.laundry.exception.ResourceNotFoundException;
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

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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

    @Autowired
    private  PaymentRepository paymentRepository;

    @Override
    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO request) {

        // Customer dhundo
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException(
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
                .orElseThrow(() -> new ResourceNotFoundException("Order nahi mila ID: " + id));
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
        // ✅ findById use karo
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order", orderId));

        // Payment link banao
        String paymentLink = frontendUrl + "/pay/" + orderId;

        // WhatsApp bill bhejo
        whatsAppService.sendBillMessage(order, paymentLink);

        log.info("✅ WhatsApp bill bheja Order #{}", orderId);
    }

    @Override
    @Transactional
    public OrderResponseDTO markCashPayment(Long orderId, Double amount) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order", orderId));

        // ✅ Already paid check karo
        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            throw new BadRequestException(
                    "Yeh order already paid hai!");
        }

        // ✅ Amount valid hai kya
        if (amount <= 0) {
            throw new BadRequestException(
                    "Amount valid hona chahiye!");
        }

        double newPaidAmount = order.getPaidAmount() + amount;

        if (newPaidAmount >= order.getTotalAmount()) {
            order.setPaidAmount(order.getTotalAmount());
            order.setPaymentStatus(PaymentStatus.PAID);

            // Customer udhari update
            Customer customer = order.getCustomer();
            double customerDue = customer.getTotalDue();
            if (customerDue > 0) {
                customer.setTotalDue(Math.max(0, customerDue - amount));
                customerRepository.save(customer);
            }
        } else {
            order.setPaidAmount(newPaidAmount);
            order.setPaymentStatus(PaymentStatus.PARTIAL);
        }

        Order updated = orderRepository.save(order);

        // ✅ Payment record save karo
        Payment cashPayment = Payment.builder()
                .order(order)
                .customer(order.getCustomer())
                .amount(amount)
                .status("SUCCESS")
                .paymentMode("CASH")
                .build();
        paymentRepository.save(cashPayment);

        log.info("✅ Cash payment saved Order #{}: ₹{}",
                orderId, amount);

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

        // Kitne din purana hai
        long daysSinceCreated = 0;
        if (order.getCreatedAt() != null) {
            daysSinceCreated = ChronoUnit.DAYS.between(
                    order.getCreatedAt().toLocalDate(),
                    LocalDate.now()
            );
        }

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
                .createdAt(order.getCreatedAt())       // ✅
                .daysSinceCreated(daysSinceCreated)     // ✅ Naya
                .razorpayOrderId(order.getRazorpayOrderId())
                .build();
    }

}
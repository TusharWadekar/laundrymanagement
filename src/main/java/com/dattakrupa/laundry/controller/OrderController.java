package com.dattakrupa.laundry.controller;

import com.dattakrupa.laundry.dto.ApiResponseDTO;
import com.dattakrupa.laundry.dto.OrderRequestDTO;
import com.dattakrupa.laundry.dto.OrderResponseDTO;
import com.dattakrupa.laundry.enums.OrderStatus;
import com.dattakrupa.laundry.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@Slf4j
@CrossOrigin(origins = "*")
public class OrderController {

    @Autowired
    private  OrderService orderService;

    // ── POST /orders ── Naya order banao
    @PostMapping
    public ResponseEntity<ApiResponseDTO<OrderResponseDTO>> createOrder(
            @Valid @RequestBody OrderRequestDTO request) {

        OrderResponseDTO response = orderService.createOrder(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(
                        "Order successfully create hua! 🧺", response));
    }

    // ── GET /orders ── Sab orders lo
    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<OrderResponseDTO>>> getAllOrders() {

        List<OrderResponseDTO> orders = orderService.getAllOrders();
        return ResponseEntity.ok(
                ApiResponseDTO.success("Saare orders", orders));
    }

    // ── GET /orders/{id} ── ID se order
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<OrderResponseDTO>> getOrderById(
            @PathVariable Long id) {

        OrderResponseDTO order = orderService.getOrderById(id);
        return ResponseEntity.ok(
                ApiResponseDTO.success("Order mila!", order));
    }

    // ── GET /orders/customer/{customerId} ── Customer ke orders
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponseDTO<List<OrderResponseDTO>>> getOrdersByCustomer(
            @PathVariable Long customerId) {

        List<OrderResponseDTO> orders = orderService.getOrdersByCustomer(customerId);
        return ResponseEntity.ok(
                ApiResponseDTO.success("Customer ke orders", orders));
    }

    // ── GET /orders/status/{status} ── Status se filter
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponseDTO<List<OrderResponseDTO>>> getOrdersByStatus(
            @PathVariable OrderStatus status) {

        List<OrderResponseDTO> orders = orderService.getOrdersByStatus(status);
        return ResponseEntity.ok(
                ApiResponseDTO.success(status + " orders", orders));
    }

    // ── PUT /orders/{id}/status ── Status update karo
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponseDTO<OrderResponseDTO>> updateStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus status) {

        OrderResponseDTO updated = orderService.updateOrderStatus(id, status);
        return ResponseEntity.ok(
                ApiResponseDTO.success("Order status update hua: " + status, updated));
    }

    // ── POST /orders/{id}/send-bill ── WhatsApp bill bhejo
    @PostMapping("/{id}/send-bill")
    public ResponseEntity<ApiResponseDTO<Void>> sendBill(
            @PathVariable Long id) {

        orderService.sendBillOnWhatsApp(id);
        return ResponseEntity.ok(
                ApiResponseDTO.success("WhatsApp bill bheja ja raha hai! 📱", null));
    }

    // ── PUT /orders/{id}/cash-payment ── Cash payment mark karo
    @PutMapping("/{id}/cash-payment")
    public ResponseEntity<ApiResponseDTO<OrderResponseDTO>> cashPayment(
            @PathVariable Long id,
            @RequestParam Double amount) {

        OrderResponseDTO updated = orderService.markCashPayment(id, amount);
        return ResponseEntity.ok(
                ApiResponseDTO.success("Cash payment mark ho gaya! ₹" + amount, updated));
    }

    // ── DELETE /orders/{id} ── Order delete karo
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteOrder(
            @PathVariable Long id) {

        orderService.deleteOrder(id);
        return ResponseEntity.ok(
                ApiResponseDTO.success("Order delete ho gaya!", null));
    }
}
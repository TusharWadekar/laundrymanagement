package com.dattakrupa.laundry.controller;

import com.dattakrupa.laundry.dto.ApiResponseDTO;
import com.dattakrupa.laundry.service.WhatsAppService;
import com.dattakrupa.laundry.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/whatsapp")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class WhatsAppController {

    private final WhatsAppService whatsAppService;
    private final OrderService orderService;

    // ── POST /whatsapp/send-bill/{orderId} ──
    @PostMapping("/send-bill/{orderId}")
    public ResponseEntity<ApiResponseDTO<Void>> sendBill(
            @PathVariable Long orderId) {

        orderService.sendBillOnWhatsApp(orderId);
        return ResponseEntity.ok(
                ApiResponseDTO.success(
                        "WhatsApp bill bheja gaya! 📱", null));
    }

    // ── POST /whatsapp/udhari-reminder/{customerId} ──
    @PostMapping("/udhari-reminder/{customerId}")
    public ResponseEntity<ApiResponseDTO<Void>> sendReminder(
            @PathVariable Long customerId) {

        whatsAppService.sendUdhariReminder(customerId);
        return ResponseEntity.ok(
                ApiResponseDTO.success(
                        "Udhari reminder bheja gaya! 📱", null));
    }
}
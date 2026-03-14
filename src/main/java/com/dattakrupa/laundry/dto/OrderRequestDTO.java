package com.dattakrupa.laundry.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class OrderRequestDTO {

    @NotNull(message = "Customer ID required hai")
    private Long customerId;

    // Kapdo ki list
    @NotNull(message = "Kam se kam ek item hona chahiye")
    private List<OrderItemRequestDTO> items;

    private LocalDate expectedDelivery;

    private String notes;
}
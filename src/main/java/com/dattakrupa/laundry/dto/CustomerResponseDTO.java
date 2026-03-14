package com.dattakrupa.laundry.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CustomerResponseDTO {

    private Long id;
    private String name;
    private String phoneNumber;
    private String address;
    private Double totalDue;
    private LocalDateTime createdAt;

    // Kitne orders hain total
    private Integer totalOrders;
}
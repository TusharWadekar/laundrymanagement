package com.dattakrupa.laundry.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderItemResponseDTO {

    private Long id;
    private String itemName;
    private Integer quantity;
    private Double pricePerItem;
    private Double subtotal;
}
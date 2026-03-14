package com.dattakrupa.laundry.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderItemRequestDTO {

    @NotBlank(message = "Item name required hai")
    private String itemName; // Shirt, Pant, Saree...

    @NotNull
    @Min(value = 1, message = "Quantity kam se kam 1 honi chahiye")
    private Integer quantity;

    @NotNull
    @Min(value = 1, message = "Price valid honi chahiye")
    private Double pricePerItem;
}
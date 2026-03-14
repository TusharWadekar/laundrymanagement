package com.dattakrupa.laundry.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CustomerRequestDTO {

    @NotBlank(message = "Name required hai")
    private String name;

    @NotBlank(message = "Phone number required hai")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Valid Indian phone number daalo")
    private String phoneNumber;

    private String address;
}
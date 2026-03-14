package com.dattakrupa.laundry.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class ErrorResponseDTO {

    private boolean success;
    private String message;
    private String errorCode;
    private int status;
    private LocalDateTime timestamp;

    // Validation errors ke liye
    private Map<String, String> errors;
}
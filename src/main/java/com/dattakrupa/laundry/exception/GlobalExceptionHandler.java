package com.dattakrupa.laundry.exception;

import com.dattakrupa.laundry.dto.ErrorResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // ── 404 — Resource Not Found ──
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ErrorResponseDTO> handleNotFound(
            ResourceNotFoundException ex) {
        log.error("❌ Resource not found: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(buildError(ex.getMessage(), "RESOURCE_NOT_FOUND", 404));
    }

    // ── 409 — Duplicate Resource ──
    @ExceptionHandler(DuplicateResourceException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ErrorResponseDTO> handleDuplicate(
            DuplicateResourceException ex) {
        log.error("❌ Duplicate resource: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(buildError(ex.getMessage(), "DUPLICATE_RESOURCE", 409));
    }

    // ── 400 — Bad Request ──
    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponseDTO> handleBadRequest(
            BadRequestException ex) {
        log.error("❌ Bad request: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(buildError(ex.getMessage(), "BAD_REQUEST", 400));
    }

    // ── 400 — Validation Errors ──
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponseDTO> handleValidation(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult()
                .getAllErrors()
                .forEach(error -> {
                    String field = ((FieldError) error).getField();
                    String msg   = error.getDefaultMessage();
                    errors.put(field, msg);
                });

        log.error("❌ Validation failed: {}", errors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponseDTO.builder()
                        .success(false)
                        .message("Validation failed — fields check karo!")
                        .errorCode("VALIDATION_ERROR")
                        .status(400)
                        .timestamp(LocalDateTime.now())
                        .errors(errors)
                        .build());
    }

    // ── 402 — Payment Exception ──
    @ExceptionHandler(PaymentException.class)
    @ResponseStatus(HttpStatus.PAYMENT_REQUIRED)
    public ResponseEntity<ErrorResponseDTO> handlePayment(
            PaymentException ex) {
        log.error("❌ Payment error: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.PAYMENT_REQUIRED)
                .body(buildError(ex.getMessage(), "PAYMENT_ERROR", 402));
    }

    // ── 503 — WhatsApp Exception ──
    @ExceptionHandler(WhatsAppException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ResponseEntity<ErrorResponseDTO> handleWhatsApp(
            WhatsAppException ex) {
        log.error("❌ WhatsApp error: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(buildError(ex.getMessage(), "WHATSAPP_ERROR", 503));
    }

    // ── 500 — Generic Exception ──
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorResponseDTO> handleGeneric(
            Exception ex) {
        log.error("❌ Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildError(
                        "Kuch galat ho gaya! Dobara try karo.",
                        "INTERNAL_SERVER_ERROR", 500));
    }

    // ── Helper Method ──
    private ErrorResponseDTO buildError(
            String message, String errorCode, int status) {
        return ErrorResponseDTO.builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .status(status)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
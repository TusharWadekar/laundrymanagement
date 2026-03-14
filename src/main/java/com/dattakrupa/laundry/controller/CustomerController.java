package com.dattakrupa.laundry.controller;

import com.dattakrupa.laundry.dto.ApiResponseDTO;
import com.dattakrupa.laundry.dto.CustomerRequestDTO;
import com.dattakrupa.laundry.dto.CustomerResponseDTO;
import com.dattakrupa.laundry.service.CustomerService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@CrossOrigin(origins = "*")
public class CustomerController {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(CustomerController.class);
    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    // ── POST /customers ── Naya customer banao
    @PostMapping
    public ResponseEntity<ApiResponseDTO<CustomerResponseDTO>> createCustomer(
            @Valid @RequestBody CustomerRequestDTO request) {

        CustomerResponseDTO response = customerService.createCustomer(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success("Customer successfully add hua!", response));
    }

    // ── GET /customers ── Sab customers lo
    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<CustomerResponseDTO>>> getAllCustomers() {

        List<CustomerResponseDTO> customers = customerService.getAllCustomers();
        return ResponseEntity.ok(
                ApiResponseDTO.success("Customers fetch ho gaye", customers));
    }

    // ── GET /customers/{id} ── ID se customer
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<CustomerResponseDTO>> getCustomerById(
            @PathVariable Long id) {

        CustomerResponseDTO customer = customerService.getCustomerById(id);
        return ResponseEntity.ok(
                ApiResponseDTO.success("Customer mila!", customer));
    }

    // ── GET /customers/phone/{phone} ── Phone se customer
    @GetMapping("/phone/{phone}")
    public ResponseEntity<ApiResponseDTO<CustomerResponseDTO>> getByPhone(
            @PathVariable String phone) {

        CustomerResponseDTO customer = customerService.getCustomerByPhone(phone);
        return ResponseEntity.ok(
                ApiResponseDTO.success("Customer mila!", customer));
    }

    // ── GET /customers/search?name=amit ── Name se search
    @GetMapping("/search")
    public ResponseEntity<ApiResponseDTO<List<CustomerResponseDTO>>> searchCustomers(
            @RequestParam String name) {

        List<CustomerResponseDTO> customers = customerService.searchCustomers(name);
        return ResponseEntity.ok(
                ApiResponseDTO.success("Search results", customers));
    }

    // ── GET /customers/udhari ── Jinki udhari baki hai
    @GetMapping("/udhari")
    public ResponseEntity<ApiResponseDTO<List<CustomerResponseDTO>>> getUdhariCustomers() {

        List<CustomerResponseDTO> customers = customerService.getCustomersWithUdhari();
        return ResponseEntity.ok(
                ApiResponseDTO.success("Udhari wale customers", customers));
    }

    // ── PUT /customers/{id} ── Customer update karo
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<CustomerResponseDTO>> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody CustomerRequestDTO request) {

        CustomerResponseDTO updated = customerService.updateCustomer(id, request);
        return ResponseEntity.ok(
                ApiResponseDTO.success("Customer update ho gaya!", updated));
    }

    // ── DELETE /customers/{id} ── Customer delete karo
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteCustomer(
            @PathVariable Long id) {

        customerService.deleteCustomer(id);
        return ResponseEntity.ok(
                ApiResponseDTO.success("Customer delete ho gaya!", null));
    }
}
package com.dattakrupa.laundry.service;

import com.dattakrupa.laundry.dto.CustomerRequestDTO;
import com.dattakrupa.laundry.dto.CustomerResponseDTO;

import java.util.List;

public interface CustomerService {

    // Naya customer banao
    CustomerResponseDTO createCustomer(CustomerRequestDTO request);

    // Sab customers lo
    List<CustomerResponseDTO> getAllCustomers();

    // ID se customer dhundo
    CustomerResponseDTO getCustomerById(Long id);

    // Phone se customer dhundo
    CustomerResponseDTO getCustomerByPhone(String phone);

    // Customer update karo
    CustomerResponseDTO updateCustomer(Long id, CustomerRequestDTO request);

    // Customer delete karo
    void deleteCustomer(Long id);

    // Jinki udhari baki hai
    List<CustomerResponseDTO> getCustomersWithUdhari();

    // Name se search karo
    List<CustomerResponseDTO> searchCustomers(String name);
}
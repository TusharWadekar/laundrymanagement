package com.dattakrupa.laundry.serviceImpl;

import com.dattakrupa.laundry.dto.CustomerRequestDTO;
import com.dattakrupa.laundry.dto.CustomerResponseDTO;
import com.dattakrupa.laundry.model.Customer;
import com.dattakrupa.laundry.repository.CustomerRepository;
import com.dattakrupa.laundry.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    private  CustomerRepository customerRepository;

    @Override
    public CustomerResponseDTO createCustomer(CustomerRequestDTO request) {
        // Phone already exist karta hai kya?
            if (customerRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new RuntimeException("Is phone number se customer already exist karta hai: "
                    + request.getPhoneNumber());
        }

        Customer customer = Customer.builder()
                .name(request.getName())
                .phoneNumber(request.getPhoneNumber())
                .address(request.getAddress())
                .totalDue(0.0)
                .build();

        Customer saved = customerRepository.save(customer);
        log.info("Naya customer add hua: {}", saved.getName());

        return mapToResponse(saved);
    }

    @Override
    public List<CustomerResponseDTO> getAllCustomers() {
        return customerRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public CustomerResponseDTO getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer nahi mila ID: " + id));
        return mapToResponse(customer);
    }

    @Override
    public CustomerResponseDTO getCustomerByPhone(String phone) {
        Customer customer = customerRepository.findByPhoneNumber(phone)
                .orElseThrow(() -> new RuntimeException("Customer nahi mila phone: " + phone));
        return mapToResponse(customer);
    }

    @Override
    public CustomerResponseDTO updateCustomer(Long id, CustomerRequestDTO request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer nahi mila ID: " + id));

        customer.setName(request.getName());
        customer.setAddress(request.getAddress());
        // Phone update nahi karenge (unique key hai)

        Customer updated = customerRepository.save(customer);
        log.info("Customer update hua: {}", updated.getName());

        return mapToResponse(updated);
    }

    @Override
    public void deleteCustomer(Long id) {
        if (!customerRepository.existsById(id)) {
            throw new RuntimeException("Customer nahi mila ID: " + id);
        }
        customerRepository.deleteById(id);
        log.info("Customer delete hua ID: {}", id);
    }

    @Override
    public List<CustomerResponseDTO> getCustomersWithUdhari() {
        return customerRepository.findByTotalDueGreaterThan(0.0)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CustomerResponseDTO> searchCustomers(String name) {
        return customerRepository.findByNameContainingIgnoreCase(name)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Entity → DTO convert karo
    private CustomerResponseDTO mapToResponse(Customer customer) {
        return CustomerResponseDTO.builder()
                .id(customer.getId())
                .name(customer.getName())
                .phoneNumber(customer.getPhoneNumber())
                .address(customer.getAddress())
                .totalDue(customer.getTotalDue())
                .createdAt(customer.getCreatedAt())
                .totalOrders(customer.getOrders() != null
                        ? customer.getOrders().size() : 0)
                .build();
    }
}
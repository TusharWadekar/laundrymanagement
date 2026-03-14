package com.dattakrupa.laundry.service;

import com.dattakrupa.laundry.model.Order;

public interface WhatsAppService {

    // Bill message bhejo
    void sendBillMessage(Order order, String paymentLink);

    // Payment confirmation bhejo
    void sendPaymentConfirmation(Order order);

    // Udhari reminder bhejo
    void sendUdhariReminder(Long customerId);
}
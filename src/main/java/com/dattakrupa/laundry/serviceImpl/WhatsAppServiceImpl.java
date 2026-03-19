package com.dattakrupa.laundry.serviceImpl;

import com.dattakrupa.laundry.enums.PaymentStatus;
import com.dattakrupa.laundry.exception.BadRequestException;
import com.dattakrupa.laundry.exception.ResourceNotFoundException;
import com.dattakrupa.laundry.exception.WhatsAppException;
import com.dattakrupa.laundry.model.Customer;
import com.dattakrupa.laundry.model.Order;
import com.dattakrupa.laundry.repository.CustomerRepository;
import com.dattakrupa.laundry.repository.OrderRepository;
import com.dattakrupa.laundry.service.WhatsAppService;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.dattakrupa.laundry.enums.PaymentStatus;
import com.dattakrupa.laundry.model.Order;
import com.dattakrupa.laundry.repository.OrderRepository;
import java.util.List;
import java.util.stream.Collectors;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class WhatsAppServiceImpl implements WhatsAppService {

    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.whatsapp.from}")
    private String fromNumber;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    private final CustomerRepository customerRepository;

    private final OrderRepository orderRepository;

    public WhatsAppServiceImpl(CustomerRepository customerRepository,OrderRepository orderRepository) {
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
    }

    // Twilio initialize karo
    @PostConstruct
    public void init() {
        Twilio.init(accountSid, authToken);
        log.info("✅ Twilio WhatsApp initialized!");
    }

    // ── Bill Message Bhejo ──
    @Override
    public void sendBillMessage(Order order, String paymentLink) {
        try {
            String customerPhone = "whatsapp:+91"
                    + order.getCustomer().getPhoneNumber();

            StringBuilder itemsList = new StringBuilder();
            order.getItems().forEach(item -> {
                int subtotal = (int) Math.round(item.getSubtotal());
                itemsList.append("   • ")
                        .append(item.getItemName())
                        .append(" × ")
                        .append(item.getQuantity())
                        .append(" = ₹")
                        .append(subtotal)
                        .append("\n");
            });

            int totalAmount = (int) Math.round(order.getTotalAmount());

            // ✅ Payment link cleanly format karo
            String message = "🧺 *DattaKrupa Laundry*\n\n"
                    + "Namaste *" + order.getCustomer().getName() + "* ji! 🙏\n"
                    + "Aapke kapde taiyar hain! ✅\n\n"
                    + "━━━━━━━━━━━━━━━\n"
                    + "📋 *Bill Details:*\n"
                    + itemsList
                    + "━━━━━━━━━━━━━━━\n"
                    + "💰 *Total: ₹" + totalAmount + "*\n\n"
                    + "💳 *Online Pay Karein:*\n"
                    + paymentLink + "\n\n"  // ← Yeh link
                    + "⏰ *Link 24 ghante valid hai*\n\n"
                    + "🙏 Thank you!\n"
                    + "_DattaKrupa Laundry_ 🧺";

            sendMessage(customerPhone, message);

            log.info("✅ Bill bheja: {} Order #{}",
                    order.getCustomer().getName(), order.getId());

        } catch (Exception e) {
            log.error("❌ Bill failed: {}", e.getMessage());
            throw new WhatsAppException(
                    "WhatsApp message nahi gaya: " + e.getMessage());
        }
    }

    // ── Payment Confirmation Bhejo ──
    @Override
    public void sendPaymentConfirmation(Order order) {
        try {
            String customerPhone = "whatsapp:+91"
                    + order.getCustomer().getPhoneNumber();

            // ✅ Decimal remove karo
            int totalAmount = (int) Math.round(order.getTotalAmount());

            String message = "✅ *Payment Successful!*\n\n"
                    + "🙏 Shukriya *"
                    + order.getCustomer().getName() + "* ji!\n\n"
                    + "━━━━━━━━━━━━━━━\n"
                    + "📦 Order #" + order.getId() + "\n"
                    + "💰 Amount: *₹" + totalAmount + "*\n" // ✅ Fix
                    + "✅ Status: *Paid*\n"
                    + "━━━━━━━━━━━━━━━\n\n"
                    + "Aapka swagat hai! 😊\n"
                    + "_DattaKrupa Laundry_ 🧺";

            sendMessage(customerPhone, message);

            log.info("✅ Payment confirmation bheja: {}",
                    order.getCustomer().getName());

        } catch (Exception e) {
            log.error("❌ Payment confirmation failed: {}",
                    e.getMessage());
        }
    }

    // ── Udhari Reminder Bhejo ──
    @Override
    public void sendUdhariReminder(Long customerId) {
        try {
            Customer customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Customer", customerId));

            // Orders se actual due calculate karo
            List<Order> unpaidOrders = orderRepository
                    .findByCustomerId(customerId)
                    .stream()
                    .filter(o -> o.getPaymentStatus() != PaymentStatus.PAID)
                    .toList();

            // Total due calculate karo
            double actualTotalDue = unpaidOrders.stream()
                    .mapToDouble(Order::getDueAmount)
                    .sum();

            // Agar koi due nahi hai
            if (actualTotalDue <= 0) {
                log.info("Customer {} ki koi udhari nahi hai",
                        customer.getName());
                throw new BadRequestException(
                        customer.getName() + " ki koi udhari nahi hai!");
            }

            String customerPhone = "whatsapp:+91"
                    + customer.getPhoneNumber();

            // ✅ Orders list — Total + Paid + Due teeno dikhao
            StringBuilder ordersList = new StringBuilder();
            unpaidOrders.forEach(order -> {
                int total = (int) Math.round(order.getTotalAmount());
                int paid  = (int) Math.round(order.getPaidAmount());
                int due   = (int) Math.round(order.getDueAmount());

                ordersList.append("   📦 *Order #")
                        .append(order.getId())
                        .append("*\n")
                        .append("      💰 Total:  ₹").append(total).append("\n")
                        .append("      ✅ Paid:   ₹").append(paid).append("\n")
                        .append("      ⏳ Due:    ₹").append(due).append("\n")
                        .append("      Status: ").append(order.getStatus()).append("\n\n");
            });

            // Total due
            int totalDue = (int) Math.round(actualTotalDue);

            String message = "📒 *DattaKrupa Laundry — Reminder*\n\n"
                    + "Namaste *" + customer.getName() + "* ji! 🙏\n\n"
                    + "━━━━━━━━━━━━━━━\n"
                    + "Aapki pending payments:\n\n"
                    + ordersList
                    + "━━━━━━━━━━━━━━━\n"
                    + "💰 *Total Due: ₹" + totalDue + "*\n\n"
                    + "Kripya jaldi payment karein.\n"
                    + "💳 Online ya shop pe aakar milein.\n\n"
                    + "Shukriya! 🙏\n"
                    + "_DattaKrupa Laundry_ 🧺";

            sendMessage(customerPhone, message);

            // Customer totalDue update karo
            customer.setTotalDue(actualTotalDue);
            customerRepository.save(customer);

            log.info("✅ Udhari reminder bheja: {} ₹{}",
                    customer.getName(), totalDue);

        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            log.error("❌ Reminder failed: {}", e.getMessage());
            throw new WhatsAppException(
                    "Reminder nahi gaya: " + e.getMessage());
        }
    }

    // ── Private Helper — Message Bhejo ──
    private void sendMessage(String to, String body) {
        try {
            Message.creator(
                    new PhoneNumber(to),
                    new PhoneNumber(fromNumber),
                    body
            ).create();
            log.info("✅ Message sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("❌ Failed to send message to {}: {}", to, e.getMessage());
            throw new RuntimeException("Message sending failed: " + e.getMessage(), e);
        }
    }
}
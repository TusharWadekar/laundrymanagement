package com.dattakrupa.laundry.serviceImpl;

import com.dattakrupa.laundry.model.Customer;
import com.dattakrupa.laundry.model.Order;
import com.dattakrupa.laundry.repository.CustomerRepository;
import com.dattakrupa.laundry.service.WhatsAppService;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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

    public WhatsAppServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
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

            // Items list banao
            StringBuilder itemsList = new StringBuilder();
            order.getItems().forEach(item ->
                itemsList.append("   • ")
                         .append(item.getItemName())
                         .append(" × ")
                         .append(item.getQuantity())
                         .append(" = ₹")
                         .append(item.getSubtotal())
                         .append("\n")
            );

            // WhatsApp message banao
            String message = "🧺 *DattaKrupa Laundry*\n\n"
                    + "Namaste *" + order.getCustomer().getName() + "* ji! 🙏\n"
                    + "Aapke kapde taiyar hain! ✅\n\n"
                    + "━━━━━━━━━━━━━━━\n"
                    + "📋 *Bill Details:*\n"
                    + itemsList
                    + "━━━━━━━━━━━━━━━\n"
                    + "💰 *Total: ₹" + order.getTotalAmount() + "*\n\n"
                    + "💳 *Online Pay Karein:*\n"
                    + paymentLink + "\n\n"
                    + "🙏 Thank you!\n"
                    + "_DattaKrupa Laundry_";

            // Message bhejo
            sendMessage(customerPhone, message);

            log.info("✅ Bill WhatsApp bheja: {} ko Order #{}",
                    order.getCustomer().getName(), order.getId());

        } catch (Exception e) {
            log.error("❌ WhatsApp bill send failed: {}", e.getMessage());
            throw new RuntimeException("WhatsApp message nahi gaya: " + e.getMessage());
        }
    }

    // ── Payment Confirmation Bhejo ──
    @Override
    public void sendPaymentConfirmation(Order order) {
        try {
            String customerPhone = "whatsapp:+91"
                    + order.getCustomer().getPhoneNumber();

            String message = "✅ *Payment Successful!*\n\n"
                    + "🙏 Shukriya *" + order.getCustomer().getName() + "* ji!\n\n"
                    + "━━━━━━━━━━━━━━━\n"
                    + "📦 Order #" + order.getId() + "\n"
                    + "💰 Amount: *₹" + order.getTotalAmount() + "*\n"
                    + "✅ Status: *Paid*\n"
                    + "━━━━━━━━━━━━━━━\n\n"
                    + "Aapka swagat hai! 😊\n"
                    + "_DattaKrupa Laundry_ 🧺";

            sendMessage(customerPhone, message);

            log.info("✅ Payment confirmation bheja: {}",
                    order.getCustomer().getName());

        } catch (Exception e) {
            log.error("❌ Payment confirmation failed: {}", e.getMessage());
        }
    }

    // ── Udhari Reminder Bhejo ──
    @Override
    public void sendUdhariReminder(Long customerId) {
        try {
            Customer customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new RuntimeException(
                            "Customer nahi mila ID: " + customerId));

            String customerPhone = "whatsapp:+91" + customer.getPhoneNumber();

            String message = "📒 *DattaKrupa Laundry — Reminder*\n\n"
                    + "Namaste *" + customer.getName() + "* ji! 🙏\n\n"
                    + "Aapki pending payment hai:\n"
                    + "💰 *₹" + customer.getTotalDue() + "*\n\n"
                    + "Kripya jaldi payment karein.\n"
                    + "Online pay karein ya shop pe aakar milein.\n\n"
                    + "Shukriya! 🙏\n"
                    + "_DattaKrupa Laundry_ 🧺";

            sendMessage(customerPhone, message);

            log.info("✅ Udhari reminder bheja: {} ₹{}",
                    customer.getName(), customer.getTotalDue());

        } catch (Exception e) {
            log.error("❌ Udhari reminder failed: {}", e.getMessage());
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
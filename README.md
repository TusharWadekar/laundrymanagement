# 🧺 DattaKrupa Laundry Management System

A complete laundry management system with WhatsApp billing
and Razorpay payment integration.

## 🚀 Tech Stack
- **Backend:** Java 17 + Spring Boot 3.2
- **Database:** MySQL 8.0
- **Payment:** Razorpay
- **WhatsApp:** Twilio
- **Frontend:** React.js (Coming Soon)

## ⚙️ Setup

### 1. Clone karo
```bash
git clone https://github.com/YOUR_USERNAME/dattakrupa-laundry.git
cd dattakrupa-laundry
```

### 2. application.properties banao
```bash
cp src/main/resources/application.properties.example \
   src/main/resources/application.properties
# Apni values fill karo
```

### 3. MySQL Database banao
```sql
CREATE DATABASE laundry_db;
```

### 4. Run karo
```bash
mvn spring-boot:run
```

## 📋 API Endpoints
| Method | URL | Description |
|--------|-----|-------------|
| POST | /api/customers | Customer add karo |
| GET | /api/customers | Sab customers lo |
| POST | /api/orders | Order banao |
| PUT | /api/orders/{id}/status | Status update karo |
| POST | /api/payment/create-order/{id} | Payment link banao |
| POST | /api/whatsapp/send-bill/{id} | WhatsApp bill bhejo |
| GET | /api/dashboard | Dashboard data lo |
```

---

## 📁 Step 6 — Git Commands Run Karo

IntelliJ mein **Terminal** open karo:
```
View → Tool Windows → Terminal

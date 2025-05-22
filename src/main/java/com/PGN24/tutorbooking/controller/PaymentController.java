package com.PGN24.tutorbooking.controller;

import com.PGN24.tutorbooking.model.Payment;
import com.PGN24.tutorbooking.model.CreditCardPayment; // For request body mapping if needed
import com.PGN24.tutorbooking.model.PayPalPayment;   // For request body mapping if needed
import com.PGN24.tutorbooking.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST Controller for managing Payment-related operations.
 */
@RestController
@RequestMapping("/api/payments") // Base path for all payment-related endpoints
public class PaymentController {

    private final PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Creates a new payment.
     * The request body should contain all common payment fields, plus fields specific
     * to the payment type (e.g., cardNumber for CreditCardPayment, payPalEmail for PayPalPayment).
     * A 'paymentType' field in the JSON request body will indicate CreditCard or PayPal.
     * HTTP POST to /api/payments
     * @param paymentDetails A Map representing the payment JSON object from request body.
     * It should include a "paymentType" field ("CREDIT_CARD" or "PAYPAL").
     * @return ResponseEntity containing the created Payment (201 Created), or 400 (Bad Request).
     */
    @PostMapping
    public ResponseEntity<Payment> createPayment(@RequestBody Map<String, Object> paymentDetails) {
        String paymentType = (String) paymentDetails.get("paymentType");
        if (paymentType == null) {
            return ResponseEntity.badRequest().body(null); // Or a custom error object
        }

        Payment paymentToCreate = null;
        String paymentId = (String) paymentDetails.getOrDefault("paymentId", null);
        String bookingId = (String) paymentDetails.get("bookingId");
        // Amount might be Integer or Double from JSON, robustly parse to double
        double amount = 0;
        Object amountObj = paymentDetails.get("amount");
        if (amountObj instanceof Number) {
            amount = ((Number) amountObj).doubleValue();
        } else {
            return ResponseEntity.badRequest().body(null); // Invalid amount
        }

        String paymentDate = (String) paymentDetails.getOrDefault("paymentDate", null);
        String status = (String) paymentDetails.getOrDefault("status", "PENDING");


        if (CreditCardPayment.TYPE_IDENTIFIER.equalsIgnoreCase(paymentType)) {
            paymentToCreate = new CreditCardPayment(
                    paymentId, bookingId, amount, paymentDate, status,
                    (String) paymentDetails.get("cardNumber"),
                    (String) paymentDetails.get("cardHolderName"),
                    (String) paymentDetails.get("expiryDate")
            );
        } else if (PayPalPayment.TYPE_IDENTIFIER.equalsIgnoreCase(paymentType)) {
            paymentToCreate = new PayPalPayment(
                    paymentId, bookingId, amount, paymentDate, status,
                    (String) paymentDetails.get("payPalEmail")
            );
        } else {
            return ResponseEntity.badRequest().body(null); // Unknown payment type
        }

        Payment createdPayment = paymentService.createPayment(paymentToCreate);
        if (createdPayment != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(createdPayment);
        }
        return ResponseEntity.badRequest().build(); // Or a more specific error from service
    }


    /**
     * Retrieves a payment by its ID.
     * HTTP GET to /api/payments/{paymentId}
     * @param paymentId The ID of the payment.
     * @return ResponseEntity containing the Payment (200 OK), or 404 (Not Found).
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<Payment> getPaymentById(@PathVariable String paymentId) {
        Optional<Payment> paymentOpt = paymentService.getPaymentById(paymentId);
        return paymentOpt.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Retrieves all payments.
     * HTTP GET to /api/payments
     * @return ResponseEntity containing a List of all Payments (200 OK).
     */
    @GetMapping
    public ResponseEntity<List<Payment>> getAllPayments() {
        List<Payment> payments = paymentService.getAllPayments();
        return ResponseEntity.ok(payments);
    }

    /**
     * Retrieves all payments for a specific booking.
     * HTTP GET to /api/payments/booking/{bookingId}
     * @param bookingId The ID of the booking.
     * @return ResponseEntity with a list of payments (200 OK).
     */
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<List<Payment>> getPaymentsByBookingId(@PathVariable String bookingId) {
        List<Payment> payments = paymentService.getPaymentsByBookingId(bookingId);
        return ResponseEntity.ok(payments);
    }

    /**
     * Updates the status of a payment.
     * HTTP PUT to /api/payments/{paymentId}/status
     * @param paymentId The ID of the payment to update.
     * @param statusUpdate A Map containing the new status, e.g., {"status": "COMPLETED"}
     * @return ResponseEntity with the updated payment (200 OK), or 404 (Not Found) / 400 (Bad Request).
     */
    @PutMapping("/{paymentId}/status")
    public ResponseEntity<Payment> updatePaymentStatus(@PathVariable String paymentId, @RequestBody Map<String, String> statusUpdate) {
        String newStatus = statusUpdate.get("status");
        if (newStatus == null || newStatus.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        Payment updatedPayment = paymentService.updatePaymentStatus(paymentId, newStatus);
        if (updatedPayment != null) {
            return ResponseEntity.ok(updatedPayment);
        }
        return ResponseEntity.notFound().build();
    }


    /**
     * Deletes a payment by its ID.
     * HTTP DELETE to /api/payments/{paymentId}
     * @param paymentId The ID of the payment to delete.
     * @return ResponseEntity with 204 (No Content) if successful, or 404 (Not Found).
     */
    @DeleteMapping("/{paymentId}")
    public ResponseEntity<Void> deletePayment(@PathVariable String paymentId) {
        boolean deleted = paymentService.deletePayment(paymentId);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}

package com.PGN24.tutorbooking.service;


import com.PGN24.tutorbooking.model.Payment;
import com.PGN24.tutorbooking.model.CreditCardPayment;
import com.PGN24.tutorbooking.model.PayPalPayment;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.Objects;
import java.util.ArrayList;

/**
 * Service class for managing Payment data.
 * Handles different types of payments (CreditCard, PayPal) demonstrating polymorphism.
 */
@Service
public class PaymentService {
    private static final String PAYMENT_FILE = "payments.txt";
    private final FileService fileService;

    public PaymentService() {
        this.fileService = new FileService();
    }

    /**
     * Creates a new payment record.
     * The specific type of payment (CreditCardPayment, PayPalPayment) is passed in.
     * This demonstrates polymorphism as the method handles any object that is a Payment.
     * @param payment The Payment object (can be CreditCardPayment or PayPalPayment).
     * @return The created Payment object with a generated ID if not provided, or null if creation failed.
     */
    public Payment createPayment(Payment payment) {
        if (payment.getBookingId() == null || payment.getBookingId().trim().isEmpty() || payment.getAmount() <= 0) {
            System.err.println("Payment creation failed: Booking ID and valid amount are required.");
            return null;
        }

        if (payment.getPaymentId() == null || payment.getPaymentId().trim().isEmpty()) {
            payment.setPaymentId("payment-" + UUID.randomUUID().toString());
        }
        if (payment.getStatus() == null || payment.getStatus().trim().isEmpty()) {
            payment.setStatus("PENDING"); // Default status
        }
        if (payment.getPaymentDate() == null || payment.getPaymentDate().trim().isEmpty()){
            // Set current date, for simplicity using a placeholder string
            payment.setPaymentDate(new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()));
        }


        fileService.appendToFile(PAYMENT_FILE, payment.toString());
        return payment;
    }

    /**
     * Retrieves a payment by its ID.
     * This method demonstrates polymorphism by returning the correct subclass of Payment.
     * @param paymentId The ID of the payment.
     * @return An Optional containing the Payment (as CreditCardPayment or PayPalPayment) if found.
     */
    public Optional<Payment> getPaymentById(String paymentId) {
        if (paymentId == null || paymentId.trim().isEmpty()) {
            return Optional.empty();
        }
        return fileService.readFile(PAYMENT_FILE).stream()
                .map(this::parsePaymentFromString) // Use helper for polymorphic parsing
                .filter(Objects::nonNull)
                .filter(p -> paymentId.equals(p.getPaymentId()))
                .findFirst();
    }

    /**
     * Retrieves all payment records.
     * This method demonstrates polymorphism by returning a list of different Payment subclasses.
     * @return A List of all Payment objects (CreditCardPayment, PayPalPayment).
     */
    public List<Payment> getAllPayments() {
        return fileService.readFile(PAYMENT_FILE).stream()
                .map(this::parsePaymentFromString)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all payments associated with a specific booking ID.
     * @param bookingId The ID of the booking.
     * @return A List of Payment objects for that booking.
     */
    public List<Payment> getPaymentsByBookingId(String bookingId) {
        if (bookingId == null || bookingId.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return fileService.readFile(PAYMENT_FILE).stream()
                .map(this::parsePaymentFromString)
                .filter(Objects::nonNull)
                .filter(p -> bookingId.equals(p.getBookingId()))
                .collect(Collectors.toList());
    }

    /**
     * Updates the status of an existing payment.
     * @param paymentId The ID of the payment to update.
     * @param newStatus The new status (e.g., "COMPLETED", "FAILED").
     * @return The updated Payment object, or null if not found or update failed.
     */
    public Payment updatePaymentStatus(String paymentId, String newStatus) {
        Optional<Payment> existingPaymentOpt = getPaymentById(paymentId);
        if (existingPaymentOpt.isEmpty()) {
            System.err.println("Update failed: Payment with ID '" + paymentId + "' not found.");
            return null;
        }

        Payment existingPayment = existingPaymentOpt.get();
        String oldPaymentString = existingPayment.toString(); // Original string for file update

        existingPayment.setStatus(newStatus);
        // Potentially update paymentDate if status changes to COMPLETED
        if ("COMPLETED".equalsIgnoreCase(newStatus) && "PENDING".equalsIgnoreCase(oldPaymentString.split(",")[4])) {
            existingPayment.setPaymentDate(new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()));
        }


        fileService.updateLineInFile(PAYMENT_FILE, oldPaymentString, existingPayment.toString());
        return existingPayment;
    }

    /**
     * Deletes a payment record by its ID.
     * @param paymentId The ID of the payment to delete.
     * @return true if the payment was successfully deleted, false otherwise.
     */
    public boolean deletePayment(String paymentId) {
        Optional<Payment> paymentOpt = getPaymentById(paymentId);
        if (paymentOpt.isPresent()) {
            fileService.deleteLineFromFile(PAYMENT_FILE, paymentOpt.get().toString());
            return true;
        }
        System.err.println("Deletion failed: Payment with ID '" + paymentId + "' not found.");
        return false;
    }


    /**
     * Helper method to parse a line from the payment file into the correct Payment subclass.
     * This is a key part of demonstrating polymorphism in action: deciding the object type at runtime.
     * @param csvLine A line from the payments.txt file.
     * @return A Payment object (CreditCardPayment or PayPalPayment), or null if parsing fails.
     */
    private Payment parsePaymentFromString(String csvLine) {
        if (csvLine == null || csvLine.isEmpty()) {
            return null;
        }
        String[] parts = csvLine.split(",", -1); // -1 limit to include trailing empty strings

        // The type identifier is expected at a specific index.
        // From Payment.toString(): paymentId,bookingId,amount,paymentDate,status,PAYMENT_TYPE_IDENTIFIER,...
        // So, parts[5] should be the type identifier.
        if (parts.length < 6) {
            System.err.println("Invalid payment CSV line (too few parts): " + csvLine);
            return null;
        }
        String typeIdentifier = parts[5];

        if (CreditCardPayment.TYPE_IDENTIFIER.equals(typeIdentifier)) {
            return CreditCardPayment.fromStringParts(parts);
        } else if (PayPalPayment.TYPE_IDENTIFIER.equals(typeIdentifier)) {
            return PayPalPayment.fromStringParts(parts);
        } else {
            System.err.println("Unknown payment type identifier in CSV: " + typeIdentifier + " in line: " + csvLine);
            return null;
        }
    }
}

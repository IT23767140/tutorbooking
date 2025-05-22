package com.PGN24.tutorbooking.model;

import java.util.Objects;

/**
 * Abstract base class for payments.
 * Demonstrates Encapsulation for common payment attributes.
 * Designed for Inheritance and Polymorphism (subclasses will define specific payment details).
 */
public abstract class Payment {
    // Private attributes common to all payment types (Encapsulation)
    private String paymentId;
    private String bookingId; // Link to the booking this payment is for
    private double amount;
    private String paymentDate; // e.g., "2024-05-20"
    private String status; // e.g., "PENDING", "COMPLETED", "FAILED"

    // Constructor for common attributes
    public Payment(String paymentId, String bookingId, double amount, String paymentDate, String status) {
        this.paymentId = paymentId;
        this.bookingId = bookingId;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.status = status;
    }

    // Getters and Setters for common attributes
    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(String paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Abstract method to be implemented by subclasses to provide details specific to the payment type.
     * This demonstrates Polymorphism.
     * @return A string describing the payment type and its specific details.
     */
    public abstract String getPaymentTypeDetails();

    /**
     * Abstract method to get the payment type identifier (e.g., "CREDIT_CARD", "PAYPAL")
     * Useful for file storage and reconstruction.
     * @return String identifier for the payment type.
     */
    public abstract String getPaymentTypeIdentifier();


    /**
     * Common part of toString for file storage. Subclasses will append their specific details.
     * Format: paymentId,bookingId,amount,paymentDate,status,PAYMENT_TYPE_IDENTIFIER
     * @return A CSV string representation of the common payment attributes.
     */
    @Override
    public String toString() {
        // This will be prepended by subclasses with their specific fields
        return String.join(",",
                Objects.toString(paymentId, ""),
                Objects.toString(bookingId, ""),
                String.valueOf(amount),
                Objects.toString(paymentDate, ""),
                Objects.toString(status, ""),
                getPaymentTypeIdentifier() // Crucial for knowing which subclass to instantiate
        );
    }

    // Note: fromString will need to be handled carefully, likely in PaymentService,
    // to decide which subclass to instantiate based on a type identifier in the CSV string.
    // For simplicity in the model, we won't put a static fromString here that tries to guess the type.
    // Instead, each subclass will have its own fromString, and the service will use a type field.

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Payment payment = (Payment) o;
        return Objects.equals(paymentId, payment.paymentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(paymentId);
    }
}

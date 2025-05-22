package com.PGN24.tutorbooking.model;

import java.util.Objects;

/**
 * Represents a payment made via PayPal.
 * Inherits from Payment and demonstrates Polymorphism.
 */
public class PayPalPayment extends Payment {
    // Specific attribute for PayPal Payment (Encapsulation)
    private String payPalEmail;

    public static final String TYPE_IDENTIFIER = "PAYPAL";

    // Constructor
    public PayPalPayment(String paymentId, String bookingId, double amount, String paymentDate, String status,
                         String payPalEmail) {
        super(paymentId, bookingId, amount, paymentDate, status); // Call to superclass constructor
        this.payPalEmail = payPalEmail;
    }

    // Getter and Setter for specific attribute
    public String getPayPalEmail() {
        return payPalEmail;
    }

    public void setPayPalEmail(String payPalEmail) {
        this.payPalEmail = payPalEmail;
    }

    /**
     * Implementation of the abstract method from Payment.
     * Demonstrates Polymorphism.
     * @return Details specific to PayPal payment.
     */
    @Override
    public String getPaymentTypeDetails() {
        return "PayPal Payment: Email - " + payPalEmail;
    }

    @Override
    public String getPaymentTypeIdentifier() {
        return TYPE_IDENTIFIER;
    }

    /**
     * Converts the PayPalPayment object to a CSV string for file storage.
     * Format: common_payment_fields,payPalEmail
     * The common_payment_fields are from Payment.toString() which already includes the TYPE_IDENTIFIER.
     * @return A CSV string representation of the PayPal payment.
     */
    @Override
    public String toString() {
        return String.join(",",
                super.toString(), // Gets common fields + type identifier
                Objects.toString(payPalEmail, "")
        );
    }

    /**
     * Creates a PayPalPayment object from CSV string parts.
     * Assumes the CSV parts array starts with common payment fields, followed by PayPal specific fields.
     * The type identifier (parts[5]) should already be checked by the calling service.
     * Common parts: paymentId, bookingId, amount, paymentDate, status, TYPE_IDENTIFIER
     * Specific part: payPalEmail
     * @param parts Array of strings from CSV line.
     * @return A PayPalPayment object, or null if parts are invalid.
     */
    public static PayPalPayment fromStringParts(String[] parts) {
        // parts[0] = paymentId, parts[1] = bookingId, parts[2] = amount (String)
        // parts[3] = paymentDate, parts[4] = status, parts[5] = TYPE_IDENTIFIER (already verified)
        // parts[6] = payPalEmail
        if (parts.length == 7) {
            try {
                double amount = Double.parseDouble(parts[2]);
                return new PayPalPayment(parts[0], parts[1], amount, parts[3], parts[4], parts[6]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid amount format in PayPalPayment CSV: " + parts[2]);
                return null;
            }
        }
        System.err.println("Invalid PayPalPayment CSV parts length: " + parts.length + " (Expected 7)");
        return null;
    }
}

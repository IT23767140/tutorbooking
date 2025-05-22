package com.PGN24.tutorbooking.model;

import java.util.Objects;

/**
 * Represents a payment made via Credit Card.
 * Inherits from Payment and demonstrates Polymorphism.
 */
public class CreditCardPayment extends Payment {
    // Specific attributes for Credit Card Payment (Encapsulation)
    private String cardNumber; // Store only last 4 digits in a real app
    private String cardHolderName;
    private String expiryDate; // e.g., "MM/YY"

    public static final String TYPE_IDENTIFIER = "CREDIT_CARD";

    // Constructor
    public CreditCardPayment(String paymentId, String bookingId, double amount, String paymentDate, String status,
                             String cardNumber, String cardHolderName, String expiryDate) {
        super(paymentId, bookingId, amount, paymentDate, status); // Call to superclass constructor
        this.cardNumber = cardNumber;
        this.cardHolderName = cardHolderName;
        this.expiryDate = expiryDate;
    }

    // Getters and Setters for specific attributes
    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCardHolderName() {
        return cardHolderName;
    }

    public void setCardHolderName(String cardHolderName) {
        this.cardHolderName = cardHolderName;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    /**
     * Implementation of the abstract method from Payment.
     * Demonstrates Polymorphism.
     * @return Details specific to credit card payment.
     */
    @Override
    public String getPaymentTypeDetails() {
        // For security, don't show full card number. Showing last 4 digits as an example.
        String lastFourDigits = cardNumber.length() > 4 ? cardNumber.substring(cardNumber.length() - 4) : cardNumber;
        return "Credit Card Payment: XXXX-XXXX-XXXX-" + lastFourDigits + ", Holder: " + cardHolderName;
    }

    @Override
    public String getPaymentTypeIdentifier() {
        return TYPE_IDENTIFIER;
    }

    /**
     * Converts the CreditCardPayment object to a CSV string for file storage.
     * Format: common_payment_fields,cardNumber,cardHolderName,expiryDate
     * The common_payment_fields are from Payment.toString() which already includes the TYPE_IDENTIFIER.
     * @return A CSV string representation of the credit card payment.
     */
    @Override
    public String toString() {
        return String.join(",",
                super.toString(), // Gets common fields + type identifier
                Objects.toString(cardNumber, ""),
                Objects.toString(cardHolderName, ""),
                Objects.toString(expiryDate, "")
        );
    }

    /**
     * Creates a CreditCardPayment object from a CSV string parts.
     * Assumes the CSV parts array starts with common payment fields, followed by credit card specific fields.
     * The type identifier (parts[5]) should already be checked by the calling service.
     * Common parts: paymentId, bookingId, amount, paymentDate, status, TYPE_IDENTIFIER
     * Specific parts: cardNumber, cardHolderName, expiryDate
     * @param parts Array of strings from CSV line.
     * @return A CreditCardPayment object, or null if parts are invalid.
     */
    public static CreditCardPayment fromStringParts(String[] parts) {
        // parts[0] = paymentId, parts[1] = bookingId, parts[2] = amount (String)
        // parts[3] = paymentDate, parts[4] = status, parts[5] = TYPE_IDENTIFIER (already verified)
        // parts[6] = cardNumber, parts[7] = cardHolderName, parts[8] = expiryDate
        if (parts.length == 9) {
            try {
                double amount = Double.parseDouble(parts[2]);
                return new CreditCardPayment(parts[0], parts[1], amount, parts[3], parts[4],
                        parts[6], parts[7], parts[8]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid amount format in CreditCardPayment CSV: " + parts[2]);
                return null;
            }
        }
        System.err.println("Invalid CreditCardPayment CSV parts length: " + parts.length + " (Expected 9)");
        return null;
    }
}

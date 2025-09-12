package com.PGN24.tutorbooking.model;

import java.util.List;
import java.util.Objects;
import java.util.ArrayList;
import java.util.Arrays;

public class PremiumStudent extends Student {

    public static final String TYPE_IDENTIFIER = "PREMIUM";
    private double discountPercentage; // Example field for PremiumStudent

    // Default constructor
    public PremiumStudent() {
        super(); // Calls Student's default constructor which sets type to REGULAR initially
        this.studentTypeIdentifier = TYPE_IDENTIFIER; // Override to PREMIUM
        this.discountPercentage = 10.0; // Default discount for premium students
    }

    // Parameterized constructor
    public PremiumStudent(String studentId, String name, List<String> preferredSubjects, String availability, String learningPreference, double discountPercentage) {
        super(studentId, name, preferredSubjects, availability, learningPreference, TYPE_IDENTIFIER);
        this.discountPercentage = discountPercentage;
    }

    public double getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(double discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    /**
     * Creates a PremiumStudent object from CSV string parts.
     * Assumes parts[5] is the type identifier.
     * Assumes parts[6] is the discountPercentage.
     */
    public static PremiumStudent fromStringParts(String[] parts) {
        // studentId,name,subjects,availability,preference,TYPE_IDENTIFIER,discount
        if (parts.length >= 7 && TYPE_IDENTIFIER.equals(parts[5])) { // Check type
            List<String> preferredSubjects = new ArrayList<>();
            if (parts[2] != null && !parts[2].isEmpty()) {
                preferredSubjects.addAll(Arrays.asList(parts[2].split(";")));
            }
            double discount = 0.0;
            try {
                discount = Double.parseDouble(parts[6]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid discount format for PremiumStudent: " + parts[6]);
                // Default to 0 or handle error
            }
            return new PremiumStudent(parts[0], parts[1], preferredSubjects, parts[3], parts[4], discount);
        }
        return null;
    }

    // Override toString to include discountPercentage
    @Override
    public String toString() {
        return String.join(",",
                super.toString(), // This already includes the TYPE_IDENTIFIER as the last common part
                String.valueOf(this.discountPercentage)
        );
    }

    @Override
    public String getMembershipType() {
        return "Premium Student (Enjoys " + discountPercentage + "% discount)";
    }
}
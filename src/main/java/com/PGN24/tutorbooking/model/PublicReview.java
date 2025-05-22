package com.PGN24.tutorbooking.model;

import java.util.Objects;

/**
 * Represents a public review, visible to all users.
 * Inherits from Review.
 */
public class PublicReview extends Review {

    public static final String TYPE_IDENTIFIER = "PUBLIC";

    // Default constructor
    public PublicReview() {
        super(); // Call superclass constructor
        setReviewTypeIdentifier(TYPE_IDENTIFIER); // Set the type
    }

    // Parameterized constructor
    public PublicReview(String reviewId, String studentId, String tutorId, int rating, String comment, String reviewDate) {
        super(reviewId, studentId, tutorId, rating, comment, reviewDate, TYPE_IDENTIFIER);
    }

    /**
     * Overridden method to provide display details specific to a public review.
     * Demonstrates Polymorphism.
     * @return Display details for a public review.
     */
    @Override
    public String getDisplayDetails() {
        return "[Public] " + super.getDisplayDetails();
    }

    /**
     * Converts the PublicReview object to a CSV string.
     * For PublicReview, it's the same format as the base Review class.
     * @return A CSV string representation of the public review.
     */
    @Override
    public String toString() {
        return super.toString(); // Uses Review's toString, which includes type identifier
    }

    /**
     * Creates a PublicReview object from CSV string parts.
     * Assumes the CSV parts array contains all fields for a base review.
     * The type identifier (parts[6]) should already be checked by the calling service.
     * Format: reviewId,studentId,tutorId,rating,comment,reviewDate,TYPE_IDENTIFIER
     * @param parts Array of strings from CSV line.
     * @return A PublicReview object, or null if parts are invalid.
     */
    public static PublicReview fromStringParts(String[] parts) {
        if (parts.length == 7) { // Expects 7 parts for a PublicReview (base review fields)
            try {
                int rating = Integer.parseInt(parts[3]);
                // parts[6] is the TYPE_IDENTIFIER, already confirmed by service to be "PUBLIC"
                return new PublicReview(parts[0], parts[1], parts[2], rating, parts[4].replace(";", ","), parts[5]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid rating format in PublicReview CSV: " + parts[3]);
                return null;
            }
        }
        System.err.println("Invalid PublicReview CSV parts length: " + parts.length + " (Expected 7)");
        return null;
    }
}

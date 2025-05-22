package com.PGN24.tutorbooking.model;
import java.util.Objects;

/**
 * Represents a review that has been verified by an administrator.
 * Inherits from Review and demonstrates Polymorphism.
 */
public class VerifiedReview extends Review {
    // Specific attribute for VerifiedReview (Encapsulation)
    private boolean isVerifiedByAdmin;
    private String adminVerifierId; // ID of the admin who verified it

    public static final String TYPE_IDENTIFIER = "VERIFIED";

    // Default constructor
    public VerifiedReview() {
        super();
        setReviewTypeIdentifier(TYPE_IDENTIFIER);
        this.isVerifiedByAdmin = false; // Default to not verified
    }

    // Parameterized constructor
    public VerifiedReview(String reviewId, String studentId, String tutorId, int rating, String comment, String reviewDate,
                          boolean isVerifiedByAdmin, String adminVerifierId) {
        super(reviewId, studentId, tutorId, rating, comment, reviewDate, TYPE_IDENTIFIER);
        this.isVerifiedByAdmin = isVerifiedByAdmin;
        this.adminVerifierId = adminVerifierId;
    }

    // Getters and Setters for specific attributes
    public boolean isVerifiedByAdmin() {
        return isVerifiedByAdmin;
    }

    public void setVerifiedByAdmin(boolean verifiedByAdmin) {
        this.isVerifiedByAdmin = verifiedByAdmin;
    }

    public String getAdminVerifierId() {
        return adminVerifierId;
    }

    public void setAdminVerifierId(String adminVerifierId) {
        this.adminVerifierId = adminVerifierId;
    }

    /**
     * Overridden method to provide display details specific to a verified review.
     * Demonstrates Polymorphism.
     * @return Display details for a verified review.
     */
    @Override
    public String getDisplayDetails() {
        String verificationStatus = isVerifiedByAdmin ? "(Verified by Admin: " + Objects.toString(adminVerifierId, "N/A") + ")" : "(Not Verified)";
        return "[Verified Review] " + super.getDisplayDetails() + " " + verificationStatus;
    }

    /**
     * Converts the VerifiedReview object to a CSV string for file storage.
     * Format: common_review_fields,isVerifiedByAdmin,adminVerifierId
     * The common_review_fields are from Review.toString() which includes the TYPE_IDENTIFIER.
     * @return A CSV string representation of the verified review.
     */
    @Override
    public String toString() {
        return String.join(",",
                super.toString(), // Gets common fields + type identifier
                String.valueOf(isVerifiedByAdmin),
                Objects.toString(adminVerifierId, "")
        );
    }

    /**
     * Creates a VerifiedReview object from CSV string parts.
     * Assumes the CSV parts array starts with common review fields, followed by verified review specific fields.
     * The type identifier (parts[6]) should already be checked by the calling service.
     * Common parts: reviewId,studentId,tutorId,rating,comment,reviewDate,TYPE_IDENTIFIER
     * Specific parts: isVerifiedByAdmin,adminVerifierId
     * @param parts Array of strings from CSV line.
     * @return A VerifiedReview object, or null if parts are invalid.
     */
    public static VerifiedReview fromStringParts(String[] parts) {
        // parts[0]-[5] are base review fields, parts[6] is TYPE_IDENTIFIER
        // parts[7] = isVerifiedByAdmin, parts[8] = adminVerifierId
        if (parts.length == 9) {
            try {
                int rating = Integer.parseInt(parts[3]);
                boolean isVerified = Boolean.parseBoolean(parts[7]);
                // parts[6] is the TYPE_IDENTIFIER, already confirmed by service to be "VERIFIED"
                return new VerifiedReview(parts[0], parts[1], parts[2], rating, parts[4].replace(";", ","), parts[5],
                        isVerified, parts[8]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid rating format in VerifiedReview CSV: " + parts[3]);
                return null;
            }
        }
        System.err.println("Invalid VerifiedReview CSV parts length: " + parts.length + " (Expected 9)");
        return null;
    }
}

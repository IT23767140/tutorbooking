package com.PGN24.tutorbooking.model;
import java.util.Objects;

/**
 * Base class for reviews. Can be extended for different types of reviews.
 * Demonstrates Encapsulation for common review attributes.
 * Designed for Inheritance and Polymorphism.
 */
public class Review { // Made concrete to allow direct instantiation if needed, or can be abstract
    // Private attributes common to all review types (Encapsulation)
    private String reviewId;
    private String studentId; // ID of the student who wrote the review
    private String tutorId;   // ID of the tutor being reviewed
    private int rating;       // e.g., 1 to 5 stars
    private String comment;
    private String reviewDate;  // e.g., "2024-05-20"
    private String reviewTypeIdentifier; // To distinguish subclasses in file

    // Default constructor (useful for subclasses)
    public Review() {}

    // Constructor for common attributes
    public Review(String reviewId, String studentId, String tutorId, int rating, String comment, String reviewDate, String reviewTypeIdentifier) {
        this.reviewId = reviewId;
        this.studentId = studentId;
        this.tutorId = tutorId;
        this.rating = rating;
        this.comment = comment;
        this.reviewDate = reviewDate;
        this.reviewTypeIdentifier = reviewTypeIdentifier;
    }

    // Getters and Setters
    public String getReviewId() {
        return reviewId;
    }

    public void setReviewId(String reviewId) {
        this.reviewId = reviewId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getTutorId() {
        return tutorId;
    }

    public void setTutorId(String tutorId) {
        this.tutorId = tutorId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getReviewDate() {
        return reviewDate;
    }

    public void setReviewDate(String reviewDate) {
        this.reviewDate = reviewDate;
    }

    public String getReviewTypeIdentifier() {
        return reviewTypeIdentifier;
    }

    protected void setReviewTypeIdentifier(String reviewTypeIdentifier) {
        this.reviewTypeIdentifier = reviewTypeIdentifier;
    }


    /**
     * Method that can be overridden by subclasses to provide specific display details.
     * Demonstrates Polymorphism.
     * @return A string describing how the review should be displayed or its status.
     */
    public String getDisplayDetails() {
        return "Review by Student " + studentId + " for Tutor " + tutorId + ": " + rating + " stars. Comment: " + comment;
    }


    /**
     * Converts the Review object to a CSV string for file storage.
     * Format: reviewId,studentId,tutorId,rating,comment,reviewDate,reviewTypeIdentifier
     * Subclasses might add more fields.
     * @return A CSV string representation of the review.
     */
    @Override
    public String toString() {
        return String.join(",",
                Objects.toString(reviewId, ""),
                Objects.toString(studentId, ""),
                Objects.toString(tutorId, ""),
                String.valueOf(rating),
                Objects.toString(comment, "").replace(",", ";"), // Replace commas in comment to avoid CSV issues
                Objects.toString(reviewDate, ""),
                Objects.toString(reviewTypeIdentifier, "PUBLIC") // Default if not set by subclass
        );
    }

    // A generic fromString for the base Review, assuming no extra fields.
    // Services will need to look at reviewTypeIdentifier to call the correct subclass's fromStringParts.
    public static Review fromString(String csvLine) {
        if (csvLine == null || csvLine.isEmpty()) {
            return null;
        }
        String[] parts = csvLine.split(",", -1);
        // This basic fromString is for the Review class itself if it were ever directly instantiated
        // and stored with a generic type identifier. Subclasses will have more specific fromStringParts.
        if (parts.length >= 7) { // At least 7 parts for base Review
            try {
                int rating = Integer.parseInt(parts[3]);
                // The last part is the type identifier
                String typeIdentifier = parts[6];
                // If it's a PublicReview or VerifiedReview, the service layer should call their specific fromStringParts
                // This basic one is just for a generic Review object.
                return new Review(parts[0], parts[1], parts[2], rating, parts[4].replace(";", ","), parts[5], typeIdentifier);
            } catch (NumberFormatException e) {
                System.err.println("Invalid rating format in review CSV: " + parts[3]);
                return null;
            }
        }
        System.err.println("Invalid review CSV line: " + csvLine + " (Expected at least 7 parts, got " + parts.length + ")");
        return null;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Review review = (Review) o;
        return Objects.equals(reviewId, review.reviewId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reviewId);
    }
}

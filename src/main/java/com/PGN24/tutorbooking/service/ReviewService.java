package com.PGN24.tutorbooking.service;

import com.PGN24.tutorbooking.model.Review;
import com.PGN24.tutorbooking.model.PublicReview;
import com.PGN24.tutorbooking.model.VerifiedReview;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.Objects;
import java.util.ArrayList;

/**
 * Service class for managing Review data.
 * Handles different types of reviews (Public, Verified) demonstrating polymorphism.
 */
@Service
public class ReviewService {
    private static final String REVIEW_FILE = "reviews.txt";
    private final FileService fileService;

    public ReviewService() {
        this.fileService = new FileService();
    }

    /**
     * Creates a new review.
     * The specific type of review (PublicReview, VerifiedReview) is passed in.
     * @param review The Review object (can be PublicReview or VerifiedReview).
     * @return The created Review object with a generated ID if not provided, or null if creation failed.
     */
    public Review createReview(Review review) {
        if (review.getStudentId() == null || review.getStudentId().trim().isEmpty() ||
                review.getTutorId() == null || review.getTutorId().trim().isEmpty() ||
                review.getRating() < 1 || review.getRating() > 5) {
            System.err.println("Review creation failed: Student ID, Tutor ID, and a valid rating (1-5) are required.");
            return null;
        }

        if (review.getReviewId() == null || review.getReviewId().trim().isEmpty()) {
            review.setReviewId("review-" + UUID.randomUUID().toString());
        }
        if (review.getReviewDate() == null || review.getReviewDate().trim().isEmpty()){
            review.setReviewDate(new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()));
        }
        // The reviewTypeIdentifier should be set by the subclass constructor.

        fileService.appendToFile(REVIEW_FILE, review.toString());
        return review;
    }

    /**
     * Retrieves a review by its ID.
     * @param reviewId The ID of the review.
     * @return An Optional containing the Review (as PublicReview or VerifiedReview) if found.
     */
    public Optional<Review> getReviewById(String reviewId) {
        if (reviewId == null || reviewId.trim().isEmpty()) {
            return Optional.empty();
        }
        return fileService.readFile(REVIEW_FILE).stream()
                .map(this::parseReviewFromString) // Polymorphic parsing
                .filter(Objects::nonNull)
                .filter(r -> reviewId.equals(r.getReviewId()))
                .findFirst();
    }

    /**
     * Retrieves all reviews.
     * @return A List of all Review objects (PublicReview, VerifiedReview).
     */
    public List<Review> getAllReviews() {
        return fileService.readFile(REVIEW_FILE).stream()
                .map(this::parseReviewFromString)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all reviews for a specific tutor.
     * @param tutorId The ID of the tutor.
     * @return A List of Review objects for that tutor.
     */
    public List<Review> getReviewsByTutorId(String tutorId) {
        if (tutorId == null || tutorId.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return fileService.readFile(REVIEW_FILE).stream()
                .map(this::parseReviewFromString)
                .filter(Objects::nonNull)
                .filter(r -> tutorId.equals(r.getTutorId()))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all reviews submitted by a specific student.
     * @param studentId The ID of the student.
     * @return A List of Review objects submitted by that student.
     */
    public List<Review> getReviewsByStudentId(String studentId) {
        if (studentId == null || studentId.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return fileService.readFile(REVIEW_FILE).stream()
                .map(this::parseReviewFromString)
                .filter(Objects::nonNull)
                .filter(r -> studentId.equals(r.getStudentId()))
                .collect(Collectors.toList());
    }


    /**
     * Updates an existing review (e.g., comment or rating).
     * Only the student who wrote it or an admin should be able to update. (Logic for this check would be in controller).
     * @param reviewId The ID of the review to update.
     * @param updatedReviewDetails A Review object containing the new details.
     * @return The updated Review object, or null if not found or update failed.
     */
    public Review updateReview(String reviewId, Review updatedReviewDetails) {
        Optional<Review> existingReviewOpt = getReviewById(reviewId);
        if (existingReviewOpt.isEmpty()) {
            System.err.println("Update failed: Review with ID '" + reviewId + "' not found.");
            return null;
        }

        Review existingReview = existingReviewOpt.get();
        // Ensure the type doesn't change if updating specific fields of subclasses
        if (!existingReview.getReviewTypeIdentifier().equals(updatedReviewDetails.getReviewTypeIdentifier())) {
            System.err.println("Update failed: Review type cannot be changed.");
            return null;
        }

        String oldReviewString = existingReview.toString();

        // Update common fields
        if (updatedReviewDetails.getRating() >= 1 && updatedReviewDetails.getRating() <= 5) {
            existingReview.setRating(updatedReviewDetails.getRating());
        }
        if (updatedReviewDetails.getComment() != null) {
            existingReview.setComment(updatedReviewDetails.getComment());
        }
        // reviewDate might be updated to reflect edit time
        existingReview.setReviewDate(new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()));


        // Handle subclass-specific fields if necessary (e.g., for VerifiedReview)
        if (existingReview instanceof VerifiedReview && updatedReviewDetails instanceof VerifiedReview) {
            VerifiedReview existingVerified = (VerifiedReview) existingReview;
            VerifiedReview updatedVerified = (VerifiedReview) updatedReviewDetails;
            existingVerified.setVerifiedByAdmin(updatedVerified.isVerifiedByAdmin());
            existingVerified.setAdminVerifierId(updatedVerified.getAdminVerifierId());
        }

        fileService.updateLineInFile(REVIEW_FILE, oldReviewString, existingReview.toString());
        return existingReview;
    }

    /**
     * Deletes a review by its ID. (Typically an admin function).
     * @param reviewId The ID of the review to delete.
     * @return true if the review was successfully deleted, false otherwise.
     */
    public boolean deleteReview(String reviewId) {
        Optional<Review> reviewOpt = getReviewById(reviewId);
        if (reviewOpt.isPresent()) {
            fileService.deleteLineFromFile(REVIEW_FILE, reviewOpt.get().toString());
            // After deleting a review, you might want to update the Tutor's average rating.
            // This would involve:
            // 1. Getting the tutorId from the deleted review.
            // 2. Recalculating the average rating for that tutor from remaining reviews.
            // 3. Updating the Tutor object in tutors.txt.
            // For simplicity, this step is omitted here but is an important consideration.
            return true;
        }
        System.err.println("Deletion failed: Review with ID '" + reviewId + "' not found.");
        return false;
    }

    /**
     * Helper method to parse a line from the review file into the correct Review subclass.
     * Demonstrates polymorphism: deciding the object type at runtime based on file data.
     * @param csvLine A line from the reviews.txt file.
     * @return A Review object (PublicReview or VerifiedReview), or null if parsing fails.
     */
    private Review parseReviewFromString(String csvLine) {
        if (csvLine == null || csvLine.isEmpty()) {
            return null;
        }
        String[] parts = csvLine.split(",", -1);

        // Review.toString(): reviewId,studentId,tutorId,rating,comment,reviewDate,reviewTypeIdentifier,...
        // parts[6] should be the type identifier.
        if (parts.length < 7) {
            System.err.println("Invalid review CSV line (too few parts): " + csvLine);
            return null;
        }
        String typeIdentifier = parts[6];

        if (PublicReview.TYPE_IDENTIFIER.equals(typeIdentifier)) {
            return PublicReview.fromStringParts(parts);
        } else if (VerifiedReview.TYPE_IDENTIFIER.equals(typeIdentifier)) {
            return VerifiedReview.fromStringParts(parts);
        } else {
            // Fallback or handle as generic Review if applicable, or log error
            System.err.println("Unknown review type identifier in CSV: " + typeIdentifier + " in line: " + csvLine);
            // return Review.fromString(csvLine); // If you have a generic Review.fromString
            return null;
        }
    }
}


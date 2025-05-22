package com.PGN24.tutorbooking.controller;

import com.PGN24.tutorbooking.model.Review;
import com.PGN24.tutorbooking.model.PublicReview;     // For request body mapping if needed
import com.PGN24.tutorbooking.model.VerifiedReview; // For request body mapping if needed
import com.PGN24.tutorbooking.service.ReviewService;
// Make sure TutorService is available if needed for updating tutor ratings
// import com.tutorbooking.service.TutorService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST Controller for managing Review-related operations.
 */
@RestController
@RequestMapping("/api/reviews") // Base path for all review-related endpoints
public class ReviewController {

    private final ReviewService reviewService;
    // Optional: Inject TutorService if review creation/deletion should trigger tutor rating updates
    // private final TutorService tutorService;

    @Autowired
    public ReviewController(ReviewService reviewService /*, TutorService tutorService */) {
        this.reviewService = reviewService;
        // this.tutorService = tutorService;
    }

    /**
     * Creates a new review.
     * Request body should include a 'reviewType' field ("PUBLIC" or "VERIFIED").
     * HTTP POST to /api/reviews
     * @param reviewDetails A Map representing the review JSON object from request body.
     * @return ResponseEntity containing the created Review (201 Created), or 400 (Bad Request).
     */
    @PostMapping
    public ResponseEntity<Review> createReview(@RequestBody Map<String, Object> reviewDetails) {
        String reviewType = (String) reviewDetails.get("reviewType");
        if (reviewType == null) {
            // Default to PublicReview if not specified, or return bad request
            reviewType = PublicReview.TYPE_IDENTIFIER;
        }

        Review reviewToCreate = null;
        String reviewId = (String) reviewDetails.getOrDefault("reviewId", null);
        String studentId = (String) reviewDetails.get("studentId");
        String tutorId = (String) reviewDetails.get("tutorId");

        int rating = 0;
        Object ratingObj = reviewDetails.get("rating");
        if (ratingObj instanceof Number) {
            rating = ((Number) ratingObj).intValue();
        } else {
            return ResponseEntity.badRequest().body(null); // Invalid rating
        }

        String comment = (String) reviewDetails.get("comment");
        String reviewDate = (String) reviewDetails.getOrDefault("reviewDate", null);


        if (PublicReview.TYPE_IDENTIFIER.equalsIgnoreCase(reviewType)) {
            reviewToCreate = new PublicReview(reviewId, studentId, tutorId, rating, comment, reviewDate);
        } else if (VerifiedReview.TYPE_IDENTIFIER.equalsIgnoreCase(reviewType)) {
            boolean isVerified = (Boolean) reviewDetails.getOrDefault("isVerifiedByAdmin", false);
            String adminVerifierId = (String) reviewDetails.get("adminVerifierId");
            reviewToCreate = new VerifiedReview(reviewId, studentId, tutorId, rating, comment, reviewDate, isVerified, adminVerifierId);
        } else {
            return ResponseEntity.badRequest().body(null); // Unknown review type
        }

        Review createdReview = reviewService.createReview(reviewToCreate);
        if (createdReview != null) {
            // Optional: After creating a review, update the tutor's average rating.
            // tutorService.updateTutorRating(tutorId); // This method would need to be implemented in TutorService
            return ResponseEntity.status(HttpStatus.CREATED).body(createdReview);
        }
        return ResponseEntity.badRequest().build();
    }

    /**
     * Retrieves a review by its ID.
     * HTTP GET to /api/reviews/{reviewId}
     * @param reviewId The ID of the review.
     * @return ResponseEntity containing the Review (200 OK), or 404 (Not Found).
     */
    @GetMapping("/{reviewId}")
    public ResponseEntity<Review> getReviewById(@PathVariable String reviewId) {
        Optional<Review> reviewOpt = reviewService.getReviewById(reviewId);
        return reviewOpt.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Retrieves all reviews.
     * HTTP GET to /api/reviews
     * @return ResponseEntity containing a List of all Reviews (200 OK).
     */
    @GetMapping
    public ResponseEntity<List<Review>> getAllReviews() {
        List<Review> reviews = reviewService.getAllReviews();
        return ResponseEntity.ok(reviews);
    }

    /**
     * Retrieves all reviews for a specific tutor.
     * HTTP GET to /api/reviews/tutor/{tutorId}
     * @param tutorId The ID of the tutor.
     * @return ResponseEntity with a list of reviews (200 OK).
     */
    @GetMapping("/tutor/{tutorId}")
    public ResponseEntity<List<Review>> getReviewsByTutorId(@PathVariable String tutorId) {
        List<Review> reviews = reviewService.getReviewsByTutorId(tutorId);
        return ResponseEntity.ok(reviews);
    }

    /**
     * Retrieves all reviews submitted by a specific student.
     * HTTP GET to /api/reviews/student/{studentId}
     * @param studentId The ID of the student.
     * @return ResponseEntity with a list of reviews (200 OK).
     */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<Review>> getReviewsByStudentId(@PathVariable String studentId) {
        List<Review> reviews = reviewService.getReviewsByStudentId(studentId);
        return ResponseEntity.ok(reviews);
    }

    /**
     * Updates an existing review.
     * HTTP PUT to /api/reviews/{reviewId}
     * @param reviewId The ID of the review to update.
     * @param reviewDetails The Review object with updated information.
     * The request body should match the structure of the specific review type.
     * @return ResponseEntity containing the updated Review (200 OK), or 404 (Not Found) / 400 (Bad Request).
     */
    @PutMapping("/{reviewId}")
    public ResponseEntity<Review> updateReview(@PathVariable String reviewId, @RequestBody Review reviewDetails) {
        // Note: The reviewDetails object from @RequestBody will be a base Review type.
        // The service layer's updateReview method would need to handle potential casting
        // or receive a Map and reconstruct the object like in createReview if type-specific fields are updated.
        // For simplicity, this example assumes reviewDetails contains all necessary fields and
        // the service layer can handle the update based on the existing review's type.

        Optional<Review> existingReviewOpt = reviewService.getReviewById(reviewId);
        if (existingReviewOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        // Pass the type from the existing review to ensure it's not changed.
        // And ensure the incoming details are compatible.
        // This is a simplification; a more robust update would involve DTOs or specific endpoints for specific review types.
        reviewDetails.setReviewId(reviewId); // Ensure ID is set for update method

        Review updatedReview = reviewService.updateReview(reviewId, reviewDetails);

        if (updatedReview != null) {
            // Optional: After updating a review, update the tutor's average rating.
            // tutorService.updateTutorRating(updatedReview.getTutorId());
            return ResponseEntity.ok(updatedReview);
        }
        return ResponseEntity.badRequest().build(); // Or notFound if update logic determines it
    }

    /**
     * Deletes a review by its ID.
     * HTTP DELETE to /api/reviews/{reviewId}
     * @param reviewId The ID of the review to delete.
     * @return ResponseEntity with 204 (No Content) if successful, or 404 (Not Found).
     */
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable String reviewId) {
        // String tutorIdToUpdate = null;
        // Optional<Review> reviewOpt = reviewService.getReviewById(reviewId);
        // if (reviewOpt.isPresent()) {
        //     tutorIdToUpdate = reviewOpt.get().getTutorId();
        // }

        boolean deleted = reviewService.deleteReview(reviewId);
        if (deleted) {
            // if (tutorIdToUpdate != null) {
            //     tutorService.updateTutorRating(tutorIdToUpdate);
            // }
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}

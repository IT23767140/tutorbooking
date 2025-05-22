// js/review.js

document.addEventListener('DOMContentLoaded', () => {
    const currentPage = window.location.pathname;
    // console.log("Review.js loaded for page:", currentPage);

    // Star rating interaction helper
    function setupStarRating(starContainerId) {
        const container = document.getElementById(starContainerId);
        if (!container) {
            // console.error("Star rating container not found:", starContainerId);
            return;
        }
        const stars = container.querySelectorAll('.star');
        const ratingInput = document.getElementById(container.dataset.ratingInputId);

        if (!ratingInput) {
            // console.error("Rating input not found for container:", starContainerId, "Expected ID:", container.dataset.ratingInputId);
            return;
        }
        // console.log("Setting up star rating for:", starContainerId, "Rating input ID:", ratingInput.id);

        stars.forEach(star => {
            star.addEventListener('click', () => {
                const rating = star.dataset.value;
                // console.log("Star clicked, value:", rating);
                if (ratingInput) ratingInput.value = rating;
                stars.forEach(s => {
                    s.classList.toggle('selected', s.dataset.value <= rating);
                });
            });
            star.addEventListener('mouseover', () => {
                stars.forEach(s => {
                     s.style.color = s.dataset.value <= star.dataset.value ? '#FBBF24' : '#6b7280';
                });
            });
            star.addEventListener('mouseout', () => {
                const currentRating = ratingInput ? ratingInput.value : 0;
                 stars.forEach(s => {
                    s.style.color = s.dataset.value <= currentRating ? '#FBBF24' : '#6b7280';
                });
            });
        });
    }


    // Review Submission Page Logic (submit-review.html)
    if (currentPage.includes('submit-review.html')) {
        // console.log("Executing submit-review.html logic");

        if (typeof isLoggedIn !== 'function' || typeof getUserSession !== 'function' || typeof getQueryParam !== 'function' || typeof fetchData !== 'function') {
            console.error("One or more required functions (isLoggedIn, getUserSession, getQueryParam, fetchData) are not defined. Ensure main.js and auth.js are loaded before review.js");
            alert("A critical error occurred. Please try refreshing the page.");
            return;
        }

        if (!isLoggedIn()) {
            alert("Please login to submit a review.");
            window.location.href = '/login.html';
            return;
        }

        const tutorId = getQueryParam('tutorId');
        const reviewForm = document.getElementById('reviewForm');
        const tutorNameDisplay = document.getElementById('tutorNameForReview');
        const errorMessageDiv = document.getElementById('errorMessage');

        // console.log("Tutor ID from URL:", tutorId);
        // console.log("Review form element:", reviewForm);

        setupStarRating('starRatingContainer');

        if (!tutorId) {
            if (errorMessageDiv) errorMessageDiv.textContent = 'Tutor ID not provided for review.';
            if (reviewForm) reviewForm.style.display = 'none';
            // console.error("Tutor ID is missing.");
            return;
        }

        fetchData(`/tutors/${tutorId}`)
            .then(tutor => {
                if (tutorNameDisplay) tutorNameDisplay.textContent = tutor.name;
                // console.log("Fetched tutor name:", tutor.name);
            })
            .catch(error => {
                if (tutorNameDisplay) tutorNameDisplay.textContent = 'Unknown Tutor';
                // console.error("Error fetching tutor name:", error);
            });

        if (reviewForm) {
            reviewForm.addEventListener('submit', async (event) => {
                event.preventDefault();
                // console.log("Review form submitted!");

                const formData = new FormData(reviewForm);
                const userSession = getUserSession();

                if (!userSession) {
                    alert("Your session seems to have expired. Please login again.");
                    // console.error("User session not found during review submission.");
                    window.location.href = '/login.html';
                    return;
                }

                const reviewData = {
                    studentId: userSession.userId,
                    tutorId: tutorId,
                    rating: parseInt(formData.get('rating'), 10),
                    comment: formData.get('comment'),
                    // For user-submitted reviews, they are typically "PUBLIC".
                    // The backend ReviewController defaults to "PUBLIC" if "reviewType" is not in the request map.
                    // So, we can omit sending "reviewType" and let backend handle it,
                    // or explicitly send "PUBLIC". Let's be explicit for clarity.
                    reviewType: "PUBLIC"
                };

                // console.log("Review data to submit:", reviewData);

                if (isNaN(reviewData.rating) || reviewData.rating < 1 || reviewData.rating > 5) {
                    if(errorMessageDiv) errorMessageDiv.textContent = "Please select a rating between 1 and 5.";
                    // console.warn("Invalid rating selected:", reviewData.rating);
                    return;
                }
                if (!reviewData.comment || reviewData.comment.trim() === "") {
                     if(errorMessageDiv) errorMessageDiv.textContent = "Please enter a comment.";
                     // console.warn("Comment is empty.");
                    return;
                }

                try {
                    // console.log("Attempting to POST review data...");
                    const response = await fetchData('/reviews', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify(reviewData)
                    });
                    // console.log("Review submission response:", response);
                    alert('Review submitted successfully!');
                    window.location.href = `/tutor-profile.html?id=${tutorId}`;
                } catch (error) {
                    if (errorMessageDiv) errorMessageDiv.textContent = error.message || 'Failed to submit review. Please try again.';
                    // console.error("Review submission error from API:", error);
                }
            });
            // console.log("Event listener attached to review form.");
        } else {
            // console.error("Review form with ID 'reviewForm' not found!");
        }
    }

    // Review Edit Page Logic (edit-review.html)
    if (currentPage.includes('edit-review.html')) {
        if (!isLoggedIn()) {
            alert("Please login to edit your review.");
            window.location.href = '/login.html';
            return;
        }

        const reviewId = getQueryParam('reviewId');
        const editReviewForm = document.getElementById('editReviewForm');
        const tutorNameDisplay = document.getElementById('tutorNameForEditReview');
        const errorMessageDiv = document.getElementById('errorMessage');
        setupStarRating('editStarRatingContainer');

        if (!reviewId) {
            if (errorMessageDiv) errorMessageDiv.textContent = 'Review ID not provided.';
            if (editReviewForm) editReviewForm.style.display = 'none';
            return;
        }

        let currentReviewData = null;

        fetchData(`/reviews/${reviewId}`)
            .then(review => {
                currentReviewData = review;
                if (!currentReviewData) { // Handle case where review might not be found
                    if (errorMessageDiv) errorMessageDiv.textContent = 'Review not found.';
                    if (editReviewForm) editReviewForm.style.display = 'none';
                    throw new Error('Review not found for editing.');
                }
                const user = getUserSession();
                if (user.userId !== review.studentId && user.role !== 'ADMIN') {
                    alert("You are not authorized to edit this review.");
                    window.location.href = `/tutor-profile.html?id=${review.tutorId}`;
                    return;
                }

                if (editReviewForm) {
                    populateForm(editReviewForm, { rating: review.rating, comment: review.comment });
                    const stars = document.querySelectorAll('#editStarRatingContainer .star');
                    const ratingInput = document.getElementById('editRatingValue'); // Ensure this input is populated for hover effect
                    if(ratingInput) ratingInput.value = review.rating;
                    stars.forEach(s => s.classList.toggle('selected', s.dataset.value <= review.rating));
                }
                return fetchData(`/tutors/${review.tutorId}`);
            })
            .then(tutor => {
                if (tutor && tutorNameDisplay) tutorNameDisplay.textContent = tutor.name;
            })
            .catch(error => {
                // Avoid overwriting specific error if already set (e.g. "Review not found")
                if (errorMessageDiv && !errorMessageDiv.textContent) {
                    errorMessageDiv.textContent = `Could not load review details. ${error.message || ''}`;
                }
                if (editReviewForm) editReviewForm.style.display = 'none';
                console.error("Error loading data for review edit:", error);
            });

        if (editReviewForm) {
            editReviewForm.addEventListener('submit', async (event) => {
                event.preventDefault();
                if (!currentReviewData) {
                    if(errorMessageDiv) errorMessageDiv.textContent = "Original review data not loaded. Cannot update.";
                    return;
                }

                const formData = new FormData(editReviewForm);
                const updatedReviewData = {
                    reviewId: currentReviewData.reviewId, // Include reviewId for the PUT request body
                    studentId: currentReviewData.studentId,
                    tutorId: currentReviewData.tutorId,
                    rating: parseInt(formData.get('rating'), 10),
                    comment: formData.get('comment'),
                    reviewTypeIdentifier: currentReviewData.reviewTypeIdentifier, // Preserve original type
                    reviewDate: currentReviewData.reviewDate // Preserve original date, backend might update edit date
                };

                // **FIXED PART HERE**
                // Compare with the string "VERIFIED" instead of VerifiedReview.TYPE_IDENTIFIER
                if (currentReviewData.reviewTypeIdentifier === "VERIFIED") {
                    updatedReviewData.isVerifiedByAdmin = currentReviewData.isVerifiedByAdmin;
                    updatedReviewData.adminVerifierId = currentReviewData.adminVerifierId;
                    // If admin is editing, they might change these. For simplicity, we're preserving them.
                    // If admin needs to change isVerifiedByAdmin, the form would need a field for it,
                    // and this logic would need to read from that form field.
                }


                if (isNaN(updatedReviewData.rating) || updatedReviewData.rating < 1 || updatedReviewData.rating > 5) {
                    if(errorMessageDiv) errorMessageDiv.textContent = "Please select a rating between 1 and 5.";
                    return;
                }
                 if (!updatedReviewData.comment || updatedReviewData.comment.trim() === "") {
                     if(errorMessageDiv) errorMessageDiv.textContent = "Please enter a comment.";
                    return;
                }


                try {
                    await fetchData(`/reviews/${reviewId}`, {
                        method: 'PUT',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify(updatedReviewData)
                    });
                    alert('Review updated successfully!');
                    window.location.href = `/tutor-profile.html?id=${currentReviewData.tutorId}`;
                } catch (error) {
                    if (errorMessageDiv) errorMessageDiv.textContent = error.message || 'Failed to update review.';
                    console.error("Review update error:", error);
                }
            });
        }
    }
});
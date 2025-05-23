// js/tutor.js

document.addEventListener('DOMContentLoaded', () => {
    const currentPage = window.location.pathname;

    // Tutor Listing Page Logic (tutors.html)
    if (currentPage.includes('tutors.html')) {
        const tutorListContainer = document.getElementById('tutorListContainer');
        const searchSubjectInput = document.getElementById('searchSubject');
        const searchNameInput = document.getElementById('searchName'); // Assuming you add this
        const sortOptions = document.getElementById('sortOptions');
        const loadingIndicator = document.getElementById('loadingIndicator');

        function displayLoading(isLoading) {
            if (loadingIndicator) {
                loadingIndicator.style.display = isLoading ? 'block' : 'none';
            }
        }

        async function loadTutors(apiUrl = '/tutors') {
            if (!tutorListContainer) return;
            displayLoading(true);
            tutorListContainer.innerHTML = '';
            try {
                const tutors = await fetchData(apiUrl);
                renderTutors(tutors);
            } catch (error) {
                tutorListContainer.innerHTML = `<p class="text-red-400 text-center col-span-full">Could not load tutors. ${error.message || ''}</p>`;
            } finally {
                displayLoading(false);
            }
        }

        function renderTutors(tutors) {
            if (!tutorListContainer) return;
            if (!tutors || tutors.length === 0) {
                tutorListContainer.innerHTML = '<p class="text-gray-400 text-center col-span-full">No tutors found matching your criteria.</p>';
                return;
            }
            tutors.forEach(tutor => {
                const subjects = tutor.subjects && tutor.subjects.length > 0 ? tutor.subjects.join(', ') : 'N/A';
                const rating = tutor.rating ? tutor.rating.toFixed(1) : 'N/A';
                const qualifications = tutor.qualifications || 'Experienced and qualified tutor.';
                const placeholderImageUrl = `https://placehold.co/400x250/1E293B/FFFFFF?text=${encodeURIComponent(tutor.name.charAt(0))}&font=inter`;

                const tutorCard = `
                    <div class="card flex flex-col justify-between transform hover:shadow-blue-500/30 hover:scale-[1.03] transition-all duration-300 ease-out">
                        <div>
                            <img src="${placeholderImageUrl}" alt="${tutor.name}" class="w-full h-40 object-cover rounded-t-md mb-4 bg-gray-700">
                            <h3 class="text-xl font-semibold text-white mb-2 truncate" title="${tutor.name}">${tutor.name}</h3>
                            <p class="text-blue-400 mb-1 text-sm truncate" title="Subjects: ${subjects}">Subjects: ${subjects}</p>
                            <p class="text-gray-300 mb-1 text-sm">Rating: ${rating} ⭐</p>
                            <p class="text-gray-400 text-xs mb-4 h-10 line-clamp-2" title="${qualifications}">${qualifications}</p>
                        </div>
                        <a href="/tutor-profile.html?id=${tutor.tutorId}" class="btn-primary w-full text-center block mt-auto">View Profile</a>
                    </div>
                `;
                tutorListContainer.innerHTML += tutorCard;
            });
        }

        loadTutors();

        if (searchSubjectInput) {
            searchSubjectInput.addEventListener('input', () => {
                const query = searchSubjectInput.value.trim();
                if (query) {
                    loadTutors(`/tutors/search/subject?subject=${encodeURIComponent(query)}`);
                } else {
                    loadTutors();
                }
            });
        }

        if (searchNameInput) {
             searchNameInput.addEventListener('input', () => {
                const query = searchNameInput.value.trim();
                if (query) {
                     // Assuming backend endpoint /api/tutors/search/name?name=query
                     // If not, this would require client-side filtering or implementation.
                     loadTutors(`/tutors/search/name?name=${encodeURIComponent(query)}`);
                } else {
                    loadTutors();
                }
            });
        }

        if (sortOptions) {
            sortOptions.addEventListener('change', () => {
                const value = sortOptions.value;
                if(value) {
                    const [by, order] = value.split('_');
                    loadTutors(`/tutors/sorted?by=${by}&order=${order}`);
                } else {
                    loadTutors();
                }
            });
        }
    }

    // Tutor Profile Page Logic (tutor-profile.html)
    if (currentPage.includes('tutor-profile.html')) {
        const tutorId = getQueryParam('id');
        const tutorDetailsContainer = document.getElementById('tutorDetailsContainer');
        const reviewsContainer = document.getElementById('reviewsContainer');
        // Removed: const bookSessionButton = document.getElementById('bookSessionButton');
        // Removed: const submitReviewButton = document.getElementById('submitReviewButton');
        // These buttons will now be part of the dynamic HTML.

        if (!tutorId) {
            if (tutorDetailsContainer) tutorDetailsContainer.innerHTML = '<p class="text-red-400">Tutor ID not provided.</p>';
            return;
        }

        async function loadTutorProfile() {
            if (!tutorDetailsContainer || !reviewsContainer) return;
            tutorDetailsContainer.innerHTML = '<p class="text-gray-400">Loading tutor profile...</p>'; // Loading state
            reviewsContainer.innerHTML = '<p class="text-gray-400">Loading reviews...</p>'; // Loading state

            try {
                // Fetch tutor details
                const tutor = await fetchData(`/tutors/${tutorId}`);
                const placeholderImageUrl = `https://placehold.co/600x400/1E293B/FFFFFF?text=${encodeURIComponent(tutor.name.charAt(0))}&font=inter`;

                // Determine if "Write a Review" button should be shown
                // (User logged in, and perhaps hasn't reviewed this tutor yet, or is eligible)
                let showWriteReviewButton = false;
                const currentUser = getUserSession(); // from auth.js
                if (currentUser && currentUser.role === 'STUDENT') { // Only students can write reviews
                    // Add more complex logic here if needed (e.g., check if student had a session with tutor)
                    showWriteReviewButton = true;
                }

                tutorDetailsContainer.innerHTML = `
                    <div class="md:flex gap-8">
                        <div class="md:w-1/3 mb-6 md:mb-0">
                            <img src="${placeholderImageUrl}" alt="${tutor.name}" class="rounded-lg shadow-lg w-full bg-gray-700 object-cover aspect-[3/4]">
                        </div>
                        <div class="md:w-2/3">
                            <h1 class="text-3xl sm:text-4xl font-bold text-white mb-3">${tutor.name}</h1>
                            <p class="text-blue-400 text-lg mb-2">Subjects: ${tutor.subjects && tutor.subjects.length > 0 ? tutor.subjects.join(', ') : 'N/A'}</p>
                            <p class="text-gray-300 mb-2">Rating: ${tutor.rating ? tutor.rating.toFixed(1) : 'N/A'} ⭐</p>
                            <p class="text-gray-300 mb-2">Availability: ${tutor.availability || 'Contact for availability'}</p>
                            <p class="text-gray-400 mb-6 leading-relaxed">${tutor.qualifications || 'Highly qualified and experienced.'}</p>
                            <div class="flex flex-col sm:flex-row gap-3">
                                ${isLoggedIn() ? `<a href="/booking-form.html?tutorId=${tutor.tutorId}" class="btn-primary w-full sm:w-auto">Book Session</a>` : '<a href="/login.html" class="btn-primary w-full sm:w-auto">Login to Book</a>'}
                                ${showWriteReviewButton ? `<a href="/submit-review.html?tutorId=${tutor.tutorId}" class="btn-secondary w-full sm:w-auto">Write a Review</a>` : ''}
                            </div>
                        </div>
                    </div>
                `;

                // Fetch reviews for this tutor
                const reviews = await fetchData(`/reviews/tutor/${tutorId}`);
                renderReviews(reviews, tutor.tutorId); // Pass tutorId for edit link

            } catch (error) {
                tutorDetailsContainer.innerHTML = `<p class="text-red-400">Could not load tutor profile. ${error.message}</p>`;
                reviewsContainer.innerHTML = ''; // Clear reviews loading message on error
            }
        }

        function renderReviews(reviews, tutorIdForEditLink) {
            reviewsContainer.innerHTML = '<h2 class="text-2xl font-semibold text-white mb-6 border-b border-slate-700 pb-3">Student Reviews</h2>';
            if (!reviews || reviews.length === 0) {
                reviewsContainer.innerHTML += '<p class="text-gray-400">No reviews yet for this tutor.</p>';
                return;
            }
            const currentUser = getUserSession();
            reviews.forEach(review => {
                let editButtonHtml = '';
                if (currentUser && (currentUser.userId === review.studentId || currentUser.role === 'ADMIN')) {
                    editButtonHtml = `<a href="/edit-review.html?reviewId=${review.reviewId}" class="text-blue-400 hover:text-blue-300 text-xs ml-auto">Edit</a>`;
                }

                let reviewCard = `
                    <div class="card mb-4">
                        <div class="flex justify-between items-center mb-2">
                            <h4 class="font-semibold text-blue-400">Student ID: ${review.studentId}</h4>
                            <span class="text-gray-400 text-sm">${new Date(review.reviewDate).toLocaleDateString()}</span>
                        </div>
                        <p class="text-gray-300 mb-1">Rating: ${'⭐'.repeat(review.rating)}${'☆'.repeat(5-review.rating)} (${review.rating}/5)</p>
                        <p class="text-gray-400 leading-relaxed">${review.comment}</p>
                `;
                if (review.reviewTypeIdentifier === 'VERIFIED' && review.isVerifiedByAdmin) {
                     reviewCard += `<p class="text-xs text-green-400 mt-1">Verified by Admin: ${review.adminVerifierId || ''}</p>`;
                }
                if(editButtonHtml) {
                    reviewCard += `<div class="mt-2 text-right">${editButtonHtml}</div>`;
                }
                 reviewCard += `</div>`;
                reviewsContainer.innerHTML += reviewCard;
            });
        }
        loadTutorProfile();
    }
});

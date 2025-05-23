// js/admin.js

document.addEventListener('DOMContentLoaded', () => {
    const user = getUserSession(); // from auth.js
    const currentPage = window.location.pathname;

    // Global Admin Page Protection
    if (currentPage.includes('admin-')) {
        if (!isLoggedIn() || !user || user.role !== 'ADMIN') {
            alert("Access Denied. Administrator privileges required.");
            window.location.href = '/login.html'; // Redirect to login
            return; // Stop further script execution on this page
        }
        console.log("Admin page accessed by:", user.username);
    }

    // --- Admin User Management (admin-users.html) ---
    if (currentPage.includes('admin-users.html')) {
        loadAdminUsers();
    }

    // --- Admin Tutor Management (admin-tutors.html) ---
    if (currentPage.includes('admin-tutors.html')) {
        setupTutorModalEventListeners();
        loadAdminTutors();
    }

    // --- Admin Student Profile Management (admin-students.html) ---
    if (currentPage.includes('admin-students.html')) {
        // This is where the call originates (around line 30 in your file structure)
        setupStudentModalEventListeners();
        loadAdminStudents(); // This function is likely not being called due to the error above
    }

    // --- Admin Review Moderation (admin-reviews.html) ---
    if (currentPage.includes('admin-reviews.html')) {
        loadAdminReviews();
    }

    // --- Admin Booking Management (admin-bookings.html) ---
    if (currentPage.includes('admin-bookings.html')) {
        loadAdminBookings();
    }

    // --- Admin Payment Management (admin-payments.html) ---
    if (currentPage.includes('admin-payments.html')) {
        loadAdminPayments();
    }
});


// ===================================================================================
// USER MANAGEMENT FUNCTIONS (admin-users.html)
// ===================================================================================
async function loadAdminUsers() {
    const usersTableBody = document.getElementById('usersTableBody');
    const loadingMessage = document.getElementById('loadingMessage');
    const errorMessage = document.getElementById('errorMessage');

    if (!usersTableBody) {
        console.error("Element with ID 'usersTableBody' not found.");
        return;
    }
    if (loadingMessage) loadingMessage.style.display = 'block';
    if (errorMessage) errorMessage.textContent = '';
    usersTableBody.innerHTML = '';

    try {
        const users = await fetchData('/users');
        if (loadingMessage) loadingMessage.style.display = 'none';
        if (!users || users.length === 0) {
            usersTableBody.innerHTML = '<tr><td colspan="6" class="text-center py-4 text-gray-400">No users found.</td></tr>';
            return;
        }
        users.forEach(u => {
            const row = `
                <tr class="border-b border-slate-700 hover:bg-slate-700/50">
                    <td class="py-3 px-4 text-sm text-gray-300">${u.userId || 'N/A'}</td>
                    <td class="py-3 px-4 text-sm text-gray-200">${u.username || 'N/A'}</td>
                    <td class="py-3 px-4 text-sm text-gray-300">${u.email || 'N/A'}</td>
                    <td class="py-3 px-4 text-sm text-gray-300">${u.contactNumber || 'N/A'}</td>
                    <td class="py-3 px-4 text-sm text-gray-200">
                        <select data-user-id="${u.userId}" class="role-select input-field py-1 px-2 text-xs w-auto bg-slate-700 border-slate-600">
                            <option value="STUDENT" ${u.role === 'STUDENT' ? 'selected' : ''}>STUDENT</option>
                            <option value="TUTOR" ${u.role === 'TUTOR' ? 'selected' : ''}>TUTOR</option>
                            <option value="ADMIN" ${u.role === 'ADMIN' ? 'selected' : ''}>ADMIN</option>
                        </select>
                    </td>
                    <td class="py-3 px-4 text-sm">
                        <button data-user-id="${u.userId}" class="delete-user-btn text-red-400 hover:text-red-300 text-xs">Delete</button>
                    </td>
                </tr>
            `;
            usersTableBody.innerHTML += row;
        });
        attachAdminUserActionListeners();
    } catch (error) {
        if (loadingMessage) loadingMessage.style.display = 'none';
        if (errorMessage) errorMessage.textContent = `Error loading users: ${error.message || 'Unknown error'}`;
        console.error("Error in loadAdminUsers:", error);
    }
}

function attachAdminUserActionListeners() {
    document.querySelectorAll('.delete-user-btn').forEach(button => {
        button.addEventListener('click', async (e) => {
            const userIdToDelete = e.target.dataset.userId;
            if (confirm(`Are you sure you want to delete user ${userIdToDelete}? This will also delete their associated student/tutor profile if applicable.`)) {
                try {
                    await fetchData(`/users/${userIdToDelete}`, { method: 'DELETE' });
                    alert('User deleted successfully.');
                    loadAdminUsers();
                } catch (error) {
                    alert(`Failed to delete user: ${error.message}`);
                }
            }
        });
    });

    document.querySelectorAll('.role-select').forEach(select => {
        select.addEventListener('change', async (e) => {
            const userIdToUpdate = e.target.dataset.userId;
            const newRole = e.target.value;
            if (confirm(`Change user ${userIdToUpdate}'s role to ${newRole}?`)) {
                try {
                    const currentUserData = await fetchData(`/users/${userIdToUpdate}`);
                    const updatePayload = { ...currentUserData, role: newRole };
                    delete updatePayload.password;

                    await fetchData(`/users/${userIdToUpdate}`, {
                        method: 'PUT',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify(updatePayload)
                    });
                    alert(`User ${userIdToUpdate}'s role updated to ${newRole}.`);
                    loadAdminUsers();
                } catch (error) {
                    alert(`Failed to update role: ${error.message}`);
                    loadAdminUsers();
                }
            } else {
                loadAdminUsers();
            }
        });
    });
}


// ===================================================================================
// TUTOR MANAGEMENT FUNCTIONS (admin-tutors.html)
// ===================================================================================
let isEditModeTutor = false;

function setupTutorModalEventListeners() {
    const openModalBtn = document.getElementById('openAddTutorModalBtn');
    const tutorModal = document.getElementById('tutorModal');
    const closeModalBtn = document.getElementById('closeModalBtn'); // Matches HTML ID for tutor modal
    const cancelModalBtn = document.getElementById('cancelModalBtn'); // Matches HTML ID for tutor modal
    const tutorForm = document.getElementById('tutorForm');

    if (openModalBtn) openModalBtn.addEventListener('click', () => openTutorModal('add'));
    if (closeModalBtn) closeModalBtn.addEventListener('click', closeTutorModal);
    if (cancelModalBtn) cancelModalBtn.addEventListener('click', closeTutorModal);

    if (tutorForm) {
        tutorForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const formData = new FormData(tutorForm);
            const tutorData = {
                name: formData.get('name'),
                subjects: formData.get('subjects').split(',').map(s => s.trim()).filter(s => s),
                rating: parseFloat(formData.get('rating')) || 0.0,
                availability: formData.get('availability'),
                qualifications: formData.get('qualifications')
            };
            const editTutorId = document.getElementById('editTutorId').value;
            const modalErrorMessage = document.getElementById('modalErrorMessage'); // Specific to tutor modal

            try {
                if (isEditModeTutor && editTutorId) {
                    tutorData.tutorId = editTutorId;
                    await fetchData(`/tutors/${editTutorId}`, {
                        method: 'PUT',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify(tutorData)
                    });
                    alert('Tutor updated successfully.');
                } else {
                    await fetchData('/tutors', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify(tutorData)
                    });
                    alert('Tutor added successfully.');
                }
                closeTutorModal();
                loadAdminTutors();
            } catch (error) {
                if(modalErrorMessage) modalErrorMessage.textContent = `Failed: ${error.message}`;
            }
        });
    }
}

window.openTutorModal = async function(mode = 'add', tutorId = null) {
    isEditModeTutor = mode === 'edit';
    const tutorModal = document.getElementById('tutorModal');
    const modalTitle = document.getElementById('modalTitle');
    const tutorForm = document.getElementById('tutorForm');
    const editTutorIdField = document.getElementById('editTutorId');
    const modalErrorMessage = document.getElementById('modalErrorMessage');

    if (!tutorForm || !modalTitle || !editTutorIdField || !tutorModal) {
        console.error("One or more tutor modal elements are missing from the DOM.");
        return;
    }

    tutorForm.reset();
    if(modalErrorMessage) modalErrorMessage.textContent = '';
    editTutorIdField.value = '';

    if (isEditModeTutor && tutorId) {
        modalTitle.textContent = 'Edit Tutor';
        try {
            const tutor = await fetchData(`/tutors/${tutorId}`);
            editTutorIdField.value = tutor.tutorId;
            populateForm(tutorForm, {
                name: tutor.name,
                subjects: tutor.subjects ? tutor.subjects.join(', ') : '',
                rating: tutor.rating,
                availability: tutor.availability,
                qualifications: tutor.qualifications
            });
        } catch (error) {
            if(modalErrorMessage) modalErrorMessage.textContent = `Error loading tutor data: ${error.message}`;
            console.error("Error fetching tutor for edit:", error);
            return;
        }
    } else {
        modalTitle.textContent = 'Add New Tutor';
    }
    tutorModal.style.display = 'flex';
}

window.closeTutorModal = function() {
    const tutorModal = document.getElementById('tutorModal');
    if (tutorModal) tutorModal.style.display = 'none';
}

async function loadAdminTutors() {
    const tutorsTableBody = document.getElementById('tutorsTableBody');
    const loadingMessage = document.getElementById('loadingMessageTutors');
    const errorMessage = document.getElementById('errorMessageTutors');

    if (!tutorsTableBody) {
        console.error("Element with ID 'tutorsTableBody' not found.");
        return;
    }
    if (loadingMessage) loadingMessage.style.display = 'block';
    if (errorMessage) errorMessage.textContent = '';
    tutorsTableBody.innerHTML = '';

    try {
        const tutors = await fetchData('/tutors');
        if (loadingMessage) loadingMessage.style.display = 'none';
        if (!tutors || tutors.length === 0) {
            tutorsTableBody.innerHTML = '<tr><td colspan="6" class="text-center py-4 text-gray-400">No tutors found.</td></tr>';
            return;
        }
        tutors.forEach(tutor => {
            if (!tutor) {
                console.warn("Encountered a null tutor object in the list from backend.");
                return;
            }
            const row = `
                <tr class="border-b border-slate-700 hover:bg-slate-700/50">
                    <td class="py-3 px-4 text-sm text-gray-300">${tutor.tutorId || 'N/A'}</td>
                    <td class="py-3 px-4 text-sm text-gray-200">${tutor.name || 'N/A'}</td>
                    <td class="py-3 px-4 text-sm text-gray-300 wrap-text">${tutor.subjects ? tutor.subjects.join(', ') : 'N/A'}</td>
                    <td class="py-3 px-4 text-sm text-gray-300">${tutor.rating ? tutor.rating.toFixed(1) : 'N/A'}</td>
                    <td class="py-3 px-4 text-sm text-gray-300 wrap-text">${tutor.qualifications || 'N/A'}</td>
                    <td class="py-3 px-4 text-sm">
                        <button onclick="openTutorModal('edit', '${tutor.tutorId}')" class="text-blue-400 hover:text-blue-300 text-xs mr-2">Edit</button>
                        <button data-tutor-id="${tutor.tutorId}" class="delete-tutor-btn text-red-400 hover:text-red-300 text-xs">Delete</button>
                    </td>
                </tr>
            `;
            tutorsTableBody.innerHTML += row;
        });
        attachAdminTutorActionListeners();
    } catch (error) {
        if (loadingMessage) loadingMessage.style.display = 'none';
        if (errorMessage) errorMessage.textContent = `Error loading tutors: ${error.message || 'Unknown error'}`;
        console.error("Error in loadAdminTutors:", error);
    }
}

function attachAdminTutorActionListeners() {
    document.querySelectorAll('.delete-tutor-btn').forEach(button => {
        button.addEventListener('click', async (e) => {
            const tutorIdToDelete = e.target.dataset.tutorId;
            if (confirm(`Are you sure you want to delete tutor ${tutorIdToDelete}?`)) {
                try {
                    await fetchData(`/tutors/${tutorIdToDelete}`, { method: 'DELETE' });
                    alert('Tutor deleted successfully.');
                    loadAdminTutors();
                } catch (error) {
                    alert(`Failed to delete tutor: ${error.message}`);
                }
            }
        });
    });
}

// ===================================================================================
// STUDENT PROFILE MANAGEMENT FUNCTIONS (admin-students.html)
// ===================================================================================
let isEditModeStudent = false;

function setupStudentModalEventListeners() {
    const studentModal = document.getElementById('studentModal');
    // **CRITICAL CHECK**: Ensure the button ID here matches your admin-students.html
    // The error message referred to `closestudentmodal` (lowercase).
    // The button ID in your HTML for closing the student modal is `closeStudentModalBtn`.
    const closeModalBtn = document.getElementById('closeStudentModalBtn');
    const cancelModalBtn = document.getElementById('cancelStudentModalBtn');
    const studentForm = document.getElementById('studentForm');

    if (!studentModal || !closeModalBtn || !cancelModalBtn || !studentForm) {
        console.error("One or more student modal elements (studentModal, closeStudentModalBtn, cancelStudentModalBtn, studentForm) are missing from the DOM for admin-students.html.");
        // If any of these are null, the script might have already failed or will fail when trying to add listeners.
        // This check itself won't fix the "closestudentmodal is not defined" error if that error is due to a typo
        // in the event listener attachment itself, but it's good practice.
        return;
    }

    // **THE FIX IS HERE**: Ensure you are calling the correctly cased `closeStudentModal` function.
    // The error `closestudentmodal is not defined` implies this was previously misspelled.
    if (closeModalBtn) {
        closeModalBtn.addEventListener('click', closeStudentModal); // Ensure this is 'closeStudentModal' (camelCase)
    }
    if (cancelModalBtn) {
        cancelModalBtn.addEventListener('click', closeStudentModal); // Ensure this is 'closeStudentModal' (camelCase)
    }

    if (studentForm) {
        studentForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const formData = new FormData(studentForm);
            const editStudentId = document.getElementById('editStudentId').value;
            const studentModalErrorMessage = document.getElementById('studentModalErrorMessage');

            const studentData = {
                studentId: editStudentId,
                name: formData.get('name'),
                preferredSubjects: formData.get('preferredSubjects').split(',').map(s => s.trim()).filter(s => s),
                availability: formData.get('availability'),
                learningPreference: formData.get('learningPreference')
            };

            if (!editStudentId) {
                if(studentModalErrorMessage) studentModalErrorMessage.textContent = 'Student ID is missing for update.';
                return;
            }

            try {
                const existingStudent = await fetchData(`/students/${editStudentId}`);
                if (existingStudent) {
                    studentData.studentTypeIdentifier = existingStudent.studentTypeIdentifier;

                    if (existingStudent.studentTypeIdentifier === 'PREMIUM') {
                        const discountValue = formData.get('discountPercentage');
                        studentData.discountPercentage = discountValue ? parseFloat(discountValue) : existingStudent.discountPercentage; // Preserve if not in form or empty
                    }
                } else {
                     if(studentModalErrorMessage) studentModalErrorMessage.textContent = 'Could not retrieve existing student type.';
                     return;
                }

                await fetchData(`/students/${editStudentId}`, {
                    method: 'PUT',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(studentData)
                });
                alert('Student profile updated successfully.');
                closeStudentModal();
                loadAdminStudents();
            } catch (error) {
                if(studentModalErrorMessage) studentModalErrorMessage.textContent = `Failed: ${error.message || 'Unknown error'}`;
            }
        });
    }
}

window.openStudentModal = async function(studentId) {
    const studentModal = document.getElementById('studentModal');
    const modalTitle = document.getElementById('studentModalTitle');
    const studentForm = document.getElementById('studentForm');
    const editStudentIdField = document.getElementById('editStudentId');
    const modalErrorMessage = document.getElementById('studentModalErrorMessage');
    const adminPremiumFieldsDiv = document.getElementById('adminPremiumStudentFields');
    const adminStudentDiscountInput = document.getElementById('adminStudentDiscount');

    if (!studentForm || !modalTitle || !editStudentIdField || !studentModal || !adminPremiumFieldsDiv || !adminStudentDiscountInput) {
        console.error("One or more elements for the student edit modal are missing.");
        return;
    }

    studentForm.reset();
    if(modalErrorMessage) modalErrorMessage.textContent = '';
    adminPremiumFieldsDiv.style.display = 'none';
    adminStudentDiscountInput.value = '';

    modalTitle.textContent = 'Edit Student Profile';
    try {
        const student = await fetchData(`/students/${studentId}`);
        if (!student) {
            throw new Error("Student profile not found or error fetching.");
        }
        editStudentIdField.value = student.studentId;
        populateForm(studentForm, {
            name: student.name,
            preferredSubjects: student.preferredSubjects ? student.preferredSubjects.join(', ') : '',
            availability: student.availability,
            learningPreference: student.learningPreference
        });

        if (student.studentTypeIdentifier === 'PREMIUM' && student.hasOwnProperty('discountPercentage')) {
            adminPremiumFieldsDiv.style.display = 'block';
            adminStudentDiscountInput.value = student.discountPercentage;
        }

        studentModal.style.display = 'flex';
    } catch (error) {
        displayGlobalErrorMessage(`Error loading student data for editing: ${error.message || 'Unknown error'}`);
        console.error("Error fetching student for admin edit modal:", error);
    }
}

window.closeStudentModal = function() {
    const studentModal = document.getElementById('studentModal');
    if (studentModal) {
        studentModal.style.display = 'none';
    } else {
        console.error("Student modal (ID: studentModal) not found when trying to close.");
    }
}

async function loadAdminStudents() {
    const studentsTableBody = document.getElementById('studentsTableBody');
    const loadingMessage = document.getElementById('loadingMessageStudents');
    const errorMessage = document.getElementById('errorMessageStudents');

    if (!studentsTableBody) {
        console.error("Element with ID 'studentsTableBody' not found.");
        return;
    }
    if (loadingMessage) loadingMessage.style.display = 'block';
    if (errorMessage) errorMessage.textContent = '';
    studentsTableBody.innerHTML = '';

    try {
        const students = await fetchData('/students');

        if (loadingMessage) loadingMessage.style.display = 'none';

        if (!students || students.length === 0) {
            studentsTableBody.innerHTML = '<tr><td colspan="8" class="text-center py-4 text-gray-400">No student profiles found.</td></tr>';
            return;
        }
        students.forEach(student => {
            if (!student) {
                console.warn("Encountered a null student object in the list from backend during admin load.");
                return;
            }
            const type = student.studentTypeIdentifier || 'Regular';
            let details = '';
            if (student.studentTypeIdentifier === 'PREMIUM' && student.hasOwnProperty('discountPercentage')) {
                details = `${student.discountPercentage}% Disc.`;
            }

            const row = `
                <tr class="border-b border-slate-700 hover:bg-slate-700/50">
                    <td class="py-3 px-4 text-sm text-gray-300">${student.studentId || 'N/A'}</td>
                    <td class="py-3 px-4 text-sm text-gray-200">${student.name || 'N/A'}</td>
                    <td class="py-3 px-4 text-sm text-gray-300 wrap-text">${student.preferredSubjects ? student.preferredSubjects.join(', ') : 'N/A'}</td>
                    <td class="py-3 px-4 text-sm text-gray-300 wrap-text">${student.availability || 'N/A'}</td>
                    <td class="py-3 px-4 text-sm text-gray-300">${student.learningPreference || 'N/A'}</td>
                    <td class="py-3 px-4 text-sm text-gray-300 capitalize">${type.toLowerCase()}</td>
                    <td class="py-3 px-4 text-sm text-green-400">${details}</td>
                    <td class="py-3 px-4 text-sm">
                        <button onclick="openStudentModal('${student.studentId}')" class="text-blue-400 hover:text-blue-300 text-xs mr-2">Edit</button>
                        <button data-student-id="${student.studentId}" class="delete-student-btn text-red-400 hover:text-red-300 text-xs">Delete</button>
                    </td>
                </tr>
            `;
            studentsTableBody.innerHTML += row;
        });
        attachAdminStudentActionListeners();
    } catch (error) {
        console.error("Error in loadAdminStudents:", error);
        if (loadingMessage) loadingMessage.style.display = 'none';
        if (errorMessage) errorMessage.textContent = `Error loading student profiles: ${error.message || 'Unknown error'}`;
    }
}

function attachAdminStudentActionListeners() {
    document.querySelectorAll('.delete-student-btn').forEach(button => {
        button.addEventListener('click', async (e) => {
            const studentIdToDelete = e.target.dataset.studentId;
            if (confirm(`Are you sure you want to delete student profile ${studentIdToDelete}? This does NOT delete the user account, only the student-specific profile.`)) {
                try {
                    await fetchData(`/students/${studentIdToDelete}`, { method: 'DELETE' });
                    alert('Student profile deleted successfully.');
                    loadAdminStudents();
                } catch (error) {
                    alert(`Failed to delete student profile: ${error.message}`);
                }
            }
        });
    });
}


// ===================================================================================
// REVIEW MODERATION FUNCTIONS (admin-reviews.html)
// ===================================================================================
async function loadAdminReviews() {
    const reviewsTableBody = document.getElementById('reviewsTableBody');
    const loadingMessage = document.getElementById('loadingMessageReviews');
    const errorMessage = document.getElementById('errorMessageReviews');

    if (!reviewsTableBody) {
        console.error("Element 'reviewsTableBody' not found.");
        return;
    }
    if (loadingMessage) loadingMessage.style.display = 'block';
    if (errorMessage) errorMessage.textContent = '';
    reviewsTableBody.innerHTML = '';

    try {
        const reviews = await fetchData('/reviews');
        if (loadingMessage) loadingMessage.style.display = 'none';

        if (!reviews || reviews.length === 0) {
            reviewsTableBody.innerHTML = '<tr><td colspan="7" class="text-center py-4 text-gray-400">No reviews found.</td></tr>';
            return;
        }
        reviews.forEach(review => {
            if(!review) {
                console.warn("Null review object found in list from backend.");
                return;
            }
            let verificationInfo = review.reviewTypeIdentifier === 'VERIFIED'
                ? (review.isVerifiedByAdmin
                    ? `<span class="text-green-400 text-xs">(Verified by ${review.adminVerifierId || 'Admin'})</span>`
                    : `<button data-review-id="${review.reviewId}" class="verify-review-btn text-yellow-400 hover:text-yellow-300 text-xs">Verify</button>`)
                : '<span class="text-xs text-slate-500">(Public)</span>';

            const row = `
                <tr class="border-b border-slate-700 hover:bg-slate-700/50">
                    <td class="py-3 px-4 text-sm text-gray-300">${review.reviewId || 'N/A'}</td>
                    <td class="py-3 px-4 text-sm text-gray-300">${review.tutorId || 'N/A'}</td>
                    <td class="py-3 px-4 text-sm text-gray-300">${review.studentId || 'N/A'}</td>
                    <td class="py-3 px-4 text-sm text-gray-200">${'⭐'.repeat(review.rating || 0)} (${review.rating || 0})</td>
                    <td class="py-3 px-4 text-sm text-gray-400 comment-cell" title="${review.comment || ''}">${review.comment || 'N/A'}</td>
                    <td class="py-3 px-4 text-sm text-gray-300">${review.reviewDate ? new Date(review.reviewDate).toLocaleDateString() : 'N/A'} ${verificationInfo}</td>
                    <td class="py-3 px-4 text-sm">
                        <button data-review-id="${review.reviewId}" class="delete-review-btn text-red-400 hover:text-red-300 text-xs">Delete</button>
                    </td>
                </tr>
            `;
            reviewsTableBody.innerHTML += row;
        });
        attachAdminReviewActionListeners();
    } catch (error) {
        if (loadingMessage) loadingMessage.style.display = 'none';
        if (errorMessage) errorMessage.textContent = `Error loading reviews: ${error.message || 'Unknown error'}`;
        console.error("Error in loadAdminReviews:", error);
    }
}

function attachAdminReviewActionListeners() {
    document.querySelectorAll('.delete-review-btn').forEach(button => {
        button.addEventListener('click', async (e) => {
            const reviewIdToDelete = e.target.dataset.reviewId;
            if (confirm(`Are you sure you want to delete review ${reviewIdToDelete}?`)) {
                try {
                    await fetchData(`/reviews/${reviewIdToDelete}`, { method: 'DELETE' });
                    alert('Review deleted successfully.');
                    loadAdminReviews();
                } catch (error) {
                    alert(`Failed to delete review: ${error.message}`);
                }
            }
        });
    });

    document.querySelectorAll('.verify-review-btn').forEach(button => {
        button.addEventListener('click', async (e) => {
            const reviewIdToVerify = e.target.dataset.reviewId;
            const adminUser = getUserSession();
            if (confirm(`Are you sure you want to verify review ${reviewIdToVerify}?`)) {
                try {
                    const reviewToUpdate = await fetchData(`/reviews/${reviewIdToVerify}`);
                    if (!reviewToUpdate || reviewToUpdate.reviewTypeIdentifier !== 'VERIFIED') { // Check if reviewToUpdate is null
                        alert("This review is not of type 'VERIFIED' or could not be fetched, and cannot be verified this way.");
                        return;
                    }
                    const updatePayload = {
                        ...reviewToUpdate,
                        isVerifiedByAdmin: true,
                        adminVerifierId: adminUser ? adminUser.userId : 'ADMIN_SYSTEM'
                    };

                    await fetchData(`/reviews/${reviewIdToVerify}`, {
                        method: 'PUT',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify(updatePayload)
                    });
                    alert('Review verified successfully.');
                    loadAdminReviews();
                } catch (error) {
                    alert(`Failed to verify review: ${error.message}`);
                }
            }
        });
    });
}

// ===================================================================================
// BOOKING MANAGEMENT FUNCTIONS (admin-bookings.html)
// ===================================================================================
async function loadAdminBookings() {
    const bookingsTableBody = document.getElementById('bookingsTableBody');
    const loadingMessage = document.getElementById('loadingMessageBookings');
    const errorMessage = document.getElementById('errorMessageBookings');

    if (!bookingsTableBody) {
        console.error("Element 'bookingsTableBody' not found.");
        return;
    }
    if (loadingMessage) loadingMessage.style.display = 'block';
    if (errorMessage) errorMessage.textContent = '';
    bookingsTableBody.innerHTML = '';

    try {
        const bookings = await fetchData('/bookings');
        if (loadingMessage) loadingMessage.style.display = 'none';
        if (!bookings || bookings.length === 0) {
            bookingsTableBody.innerHTML = '<tr><td colspan="7" class="text-center py-4 text-gray-400">No bookings found.</td></tr>';
            return;
        }
        bookings.sort((a,b) => new Date(b.sessionDateTime) - new Date(a.sessionDateTime));

        bookings.forEach(booking => {
            if(!booking) {
                console.warn("Null booking object in list from backend.");
                return;
            }
            let statusClass = 'text-gray-400';
            if (booking.status === 'SCHEDULED') statusClass = 'status-scheduled';
            else if (booking.status === 'COMPLETED') statusClass = 'status-completed';
            else if (booking.status === 'CANCELLED') statusClass = 'status-cancelled';

            const row = `
                <tr class="hover:bg-slate-700/50">
                    <td class="py-3 px-4 text-sm text-gray-300">${booking.bookingId || 'N/A'}</td>
                    <td class="py-3 px-4 text-sm text-gray-300">${booking.studentId || 'N/A'}</td>
                    <td class="py-3 px-4 text-sm text-gray-300">${booking.tutorId || 'N/A'}</td>
                    <td class="py-3 px-4 text-sm text-gray-200">${booking.subject || 'N/A'}</td>
                    <td class="py-3 px-4 text-sm text-gray-300">${booking.sessionDateTime ? new Date(booking.sessionDateTime).toLocaleString() : 'N/A'}</td>
                    <td class="py-3 px-4 text-sm font-semibold ${statusClass}">${booking.status || 'N/A'}</td>
                    <td class="py-3 px-4 text-sm">
                        ${booking.status === 'SCHEDULED' ? `<button data-booking-id="${booking.bookingId}" class="admin-cancel-booking-btn text-red-400 hover:text-red-300 text-xs">Cancel</button>` : ''}
                    </td>
                </tr>
            `;
            bookingsTableBody.innerHTML += row;
        });
        attachAdminBookingActionListeners();
    } catch (error) {
        if (loadingMessage) loadingMessage.style.display = 'none';
        if (errorMessage) errorMessage.textContent = `Error loading bookings: ${error.message || 'Unknown error'}`;
        console.error("Error in loadAdminBookings:", error);
    }
}

function attachAdminBookingActionListeners() {
    document.querySelectorAll('.admin-cancel-booking-btn').forEach(button => {
        button.addEventListener('click', async (e) => {
            const bookingIdToCancel = e.target.dataset.bookingId;
            if (confirm(`Admin: Are you sure you want to cancel booking ${bookingIdToCancel}?`)) {
                try {
                    await fetchData(`/bookings/${bookingIdToCancel}/cancel`, { method: 'PUT' });
                    alert('Booking cancelled successfully by admin.');
                    loadAdminBookings();
                } catch (error) {
                    alert(`Failed to cancel booking: ${error.message}`);
                }
            }
        });
    });
}


// ===================================================================================
// PAYMENT MANAGEMENT FUNCTIONS (admin-payments.html)
// ===================================================================================
async function loadAdminPayments() {
    const paymentsTableBody = document.getElementById('paymentsTableBody');
    const loadingMessage = document.getElementById('loadingMessagePayments');
    const errorMessage = document.getElementById('errorMessagePayments');

    if (!paymentsTableBody) {
        console.error("Element 'paymentsTableBody' not found.");
        return;
    }
    if (loadingMessage) loadingMessage.style.display = 'block';
    if (errorMessage) errorMessage.textContent = '';
    paymentsTableBody.innerHTML = '';

    try {
        const payments = await fetchData('/payments');
        if (loadingMessage) loadingMessage.style.display = 'none';
        if (!payments || payments.length === 0) {
            paymentsTableBody.innerHTML = '<tr><td colspan="8" class="text-center py-4 text-gray-400">No payments found.</td></tr>';
            return;
        }
        payments.sort((a,b) => new Date(b.paymentDate) - new Date(a.paymentDate));

        payments.forEach(payment => {
            if(!payment){
                console.warn("Null payment object found in list from backend.");
                return;
            }
            let statusClass = 'text-gray-400';
            if (payment.status === 'PENDING') statusClass = 'status-pending'; // Use specific class if defined in CSS
            else if (payment.status === 'COMPLETED') statusClass = 'status-completed';
            else if (payment.status === 'FAILED') statusClass = 'status-failed'; // Use specific class

            let paymentDetailsText = '';
            if (payment.paymentTypeIdentifier === 'CREDIT_CARD') {
                paymentDetailsText = `Card: ...${(payment.cardNumber || 'xxxx').slice(-4)}`;
            } else if (payment.paymentTypeIdentifier === 'PAYPAL') {
                paymentDetailsText = `Email: ${payment.payPalEmail || 'N/A'}`;
            }

            const row = `
                <tr class="hover:bg-slate-700/50">
                    <td class="py-3 px-4 text-sm text-gray-300">${payment.paymentId || 'N/A'}</td>
                    <td class="py-3 px-4 text-sm text-gray-300">${payment.bookingId || 'N/A'}</td>
                    <td class="py-3 px-4 text-sm text-gray-200">$${payment.amount ? Number(payment.amount).toFixed(2) : '0.00'}</td>
                    <td class="py-3 px-4 text-sm text-gray-300">${payment.paymentDate ? new Date(payment.paymentDate).toLocaleDateString() : 'N/A'}</td>
                    <td class="py-3 px-4 text-sm font-semibold ${statusClass}">${payment.status || 'N/A'}</td>
                    <td class="py-3 px-4 text-sm text-gray-300">${payment.paymentTypeIdentifier || 'N/A'}</td>
                    <td class="py-3 px-4 text-sm text-gray-400">${paymentDetailsText}</td>
                    <td class="py-3 px-4 text-sm">
                        ${payment.status === 'PENDING' ? `
                            <button data-payment-id="${payment.paymentId}" data-new-status="COMPLETED" class="admin-update-payment-status-btn text-green-400 hover:text-green-300 text-xs mr-2">Mark Completed</button>
                            <button data-payment-id="${payment.paymentId}" data-new-status="FAILED" class="admin-update-payment-status-btn text-red-400 hover:text-red-300 text-xs">Mark Failed</button>
                        ` : ''}
                    </td>
                </tr>
            `;
            paymentsTableBody.innerHTML += row;
        });
        attachAdminPaymentActionListeners();
    } catch (error) {
        if (loadingMessage) loadingMessage.style.display = 'none';
        if (errorMessage) errorMessage.textContent = `Error loading payments: ${error.message || 'Unknown error'}`;
        console.error("Error in loadAdminPayments:", error);
    }
}

function attachAdminPaymentActionListeners() {
    document.querySelectorAll('.admin-update-payment-status-btn').forEach(button => {
        button.addEventListener('click', async (e) => {
            const paymentIdToUpdate = e.target.dataset.paymentId;
            const newStatus = e.target.dataset.newStatus;
            if (confirm(`Admin: Mark payment ${paymentIdToUpdate} as ${newStatus}?`)) {
                try {
                    await fetchData(`/payments/${paymentIdToUpdate}/status`, {
                        method: 'PUT',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({ status: newStatus })
                    });
                    alert(`Payment ${paymentIdToUpdate} status updated to ${newStatus}.`);
                    loadAdminPayments();
                } catch (error) {
                    alert(`Failed to update payment status: ${error.message}`);
                }
            }
        });
    });
}
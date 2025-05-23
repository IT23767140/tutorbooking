// js/auth.js

// --- User Session Management ---
// ... (saveUserSession, getUserSession, getUserRole, clearUserSession, isLoggedIn functions remain the same) ...
function saveUserSession(userData) {
    if (!userData || typeof userData.username === 'undefined' || typeof userData.role === 'undefined') {
        console.error("saveUserSession: Invalid or incomplete user data provided.", userData);
        return;
    }
    try {
        sessionStorage.setItem('loggedInUser', JSON.stringify(userData));
        sessionStorage.setItem('userRole', userData.role);
        console.log("User session saved:", userData.username, "Role:", userData.role);
    } catch (e) {
        console.error("Error saving user session to sessionStorage:", e);
        alert("Could not save your session. Please ensure cookies/site data are enabled and try again.");
    }
}
function getUserSession() {
    const userString = sessionStorage.getItem('loggedInUser');
    if (!userString) { return null; }
    try { return JSON.parse(userString); } catch (e) { console.error("Error parsing user session data:", e); sessionStorage.removeItem('loggedInUser'); sessionStorage.removeItem('userRole'); return null; }
}
function getUserRole() { return sessionStorage.getItem('userRole'); }
function clearUserSession() {
    sessionStorage.removeItem('loggedInUser');
    sessionStorage.removeItem('userRole');
    console.log("User session cleared.");
    updateNavOnLoginGlobal();
    window.location.href = '/index.html';
}
function isLoggedIn() { return getUserSession() !== null; }

// --- Navigation Bar Update with Avatar Dropdown ---
function updateNavOnLoginGlobal() {
    const user = getUserSession();
    const authLinksContainer = document.getElementById('authLinksContainer');
    const userAvatarDropdownContainer = document.getElementById('userAvatarDropdownContainer');
    const userAvatarButton = document.getElementById('userAvatarButton');
    const userDropdownMenu = document.getElementById('userDropdownMenu');
    const dropdownUsername = document.getElementById('dropdownUsername');
    const dropdownUserRole = document.getElementById('dropdownUserRole');
    const navAdminDashboardLinkInDropdown = document.getElementById('navAdminDashboardLink');

    if (user && user.username) {
        if (authLinksContainer) authLinksContainer.style.display = 'none';
        if (userAvatarDropdownContainer) userAvatarDropdownContainer.style.display = 'block';
        if (userAvatarButton) {
            const nameParts = user.username.split(' ');
            const initial = nameParts[0].charAt(0).toUpperCase();
            userAvatarButton.textContent = initial;
        }
        if (dropdownUsername) dropdownUsername.textContent = user.username;
        if (dropdownUserRole) dropdownUserRole.textContent = user.role.charAt(0).toUpperCase() + user.role.slice(1).toLowerCase();
        if (navAdminDashboardLinkInDropdown) {
            navAdminDashboardLinkInDropdown.style.display = user.role === 'ADMIN' ? 'block' : 'none';
        }
    } else {
        if (authLinksContainer) authLinksContainer.style.display = 'flex';
        if (userAvatarDropdownContainer) userAvatarDropdownContainer.style.display = 'none';
        if (userDropdownMenu && userDropdownMenu.classList.contains('open')) {
            userDropdownMenu.classList.remove('open');
        }
    }
}

// --- Event Listeners (DOM Ready) ---
document.addEventListener('DOMContentLoaded', () => {
    // ... (Navbar dropdown and logout listeners remain the same) ...
    const userAvatarButton = document.getElementById('userAvatarButton');
    const userDropdownMenu = document.getElementById('userDropdownMenu');
    const navDropdownLogoutLink = document.getElementById('navDropdownLogoutLink');

    if (userAvatarButton && userDropdownMenu) {
        userAvatarButton.addEventListener('click', (event) => {
            event.stopPropagation();
            userDropdownMenu.classList.toggle('open');
        });
        document.addEventListener('click', (event) => {
            if (userDropdownMenu.classList.contains('open') &&
                !userAvatarButton.contains(event.target) &&
                !userDropdownMenu.contains(event.target)) {
                userDropdownMenu.classList.remove('open');
            }
        });
    }
    if (navDropdownLogoutLink) {
        navDropdownLogoutLink.addEventListener('click', (e) => {
            e.preventDefault();
            clearUserSession();
        });
    }
    updateNavOnLoginGlobal(); // Initial Nav Update

    // --- Page Specific Logic ---
    const currentPage = window.location.pathname;

    // Registration Page Logic (register.html)
    // ... (remains the same) ...
    if (currentPage.includes('register.html')) {
        const registrationForm = document.getElementById('registrationForm');
        const errorMessageDiv = document.getElementById('errorMessage');
        if (registrationForm) {
            registrationForm.addEventListener('submit', async (event) => {
                event.preventDefault();
                const formData = new FormData(registrationForm);
                const userData = Object.fromEntries(formData.entries());
                if (errorMessageDiv) errorMessageDiv.textContent = '';
                if (!userData.username || !userData.password || !userData.email) {
                    if (errorMessageDiv) errorMessageDiv.textContent = 'Username, password, and email are required.';
                    return;
                }
                if (userData.password.length < 6) {
                    if (errorMessageDiv) errorMessageDiv.textContent = 'Password must be at least 6 characters long.';
                    return;
                }
                try {
                    await fetchData('/users/register', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(userData) });
                    alert('Registration successful! Please login.');
                    window.location.href = '/login.html';
                } catch (error) {
                    if (errorMessageDiv) errorMessageDiv.textContent = error.message || 'Registration failed.';
                }
            });
        }
    }

    // Login Page Logic (login.html)
    // ... (remains the same, ensure updateNavOnLoginGlobal() is called after saveUserSession) ...
    if (currentPage.includes('login.html')) {
        const loginForm = document.getElementById('loginForm');
        const errorMessageDiv = document.getElementById('errorMessage');
        if (loginForm) {
            loginForm.addEventListener('submit', async (event) => {
                event.preventDefault();
                const formData = new FormData(loginForm);
                const loginData = Object.fromEntries(formData.entries());
                if (errorMessageDiv) errorMessageDiv.textContent = '';
                if (!loginData.username || !loginData.password) {
                    if (errorMessageDiv) errorMessageDiv.textContent = 'Username and password are required.';
                    return;
                }
                try {
                    const user = await fetchData('/users/login', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(loginData) });
                    saveUserSession(user);
                    updateNavOnLoginGlobal(); // Update nav immediately
                    if (user.role === 'ADMIN') { window.location.href = '/admin-dashboard.html'; }
                    else { window.location.href = '/user-profile.html'; }
                } catch (error) {
                    if (errorMessageDiv) errorMessageDiv.textContent = error.message || 'Login failed.';
                }
            });
        }
    }


    // User Profile Page Logic (user-profile.html) - UPDATED
    if (currentPage.includes('user-profile.html')) {
            if (!isLoggedIn()) {
                alert("Please login to view your profile.");
                window.location.href = '/login.html';
                return;
            }

            const user = getUserSession();
            const profileDetailsDiv = document.getElementById('profileDetails');
            const studentProfileDetailsDiv = document.getElementById('studentProfileDetails');
            const studentSpecificFieldsDiv = document.getElementById('studentSpecificFields');

            // NEW: Get elements for displaying student type and discount
            const displayStudentType = document.getElementById('displayStudentType');
            const premiumStudentDiscountInfo = document.getElementById('premiumStudentDiscountInfo');
            const displayStudentDiscount = document.getElementById('displayStudentDiscount');
            const premiumStudentUpdateFields = document.getElementById('premiumStudentUpdateFields'); // For form

            const updateProfileForm = document.getElementById('updateProfileForm');
            const errorMessageDiv = document.getElementById('errorMessage');
            const successMessageDiv = document.getElementById('successMessage');

            async function loadUserProfile() {
                if (profileDetailsDiv) {
                    // ... (loading general user details remains the same) ...
                    profileDetailsDiv.innerHTML = `
                        <p class="mb-2"><strong class="font-semibold text-gray-300">User ID:</strong> ${user.userId}</p>
                        <p class="mb-2"><strong class="font-semibold text-gray-300">Username:</strong> ${user.username}</p>
                        <p class="mb-2"><strong class="font-semibold text-gray-300">Email:</strong> ${user.email}</p>
                        <p class="mb-2"><strong class="font-semibold text-gray-300">Contact:</strong> ${user.contactNumber || 'N/A'}</p>
                        <p class="mb-2"><strong class="font-semibold text-gray-300">Role:</strong> ${user.role}</p>
                    `;
                }
                if (updateProfileForm) {
                    populateForm(updateProfileForm, {
                        email: user.email,
                        contactNumber: user.contactNumber
                    });
                }

                if (user.role === 'STUDENT') {
                    if (studentSpecificFieldsDiv) studentSpecificFieldsDiv.style.display = 'block';
                    if (studentProfileDetailsDiv) studentProfileDetailsDiv.style.display = 'block';

                    try {
                        const studentData = await fetchData(`/students/${user.userId}`);
                        if (studentData) {
                            // Populate student display section
                            if(document.getElementById('displayPreferredSubjects')) document.getElementById('displayPreferredSubjects').textContent = studentData.preferredSubjects && studentData.preferredSubjects.length > 0 ? studentData.preferredSubjects.join(', ') : 'Not set';
                            if(document.getElementById('displayAvailability')) document.getElementById('displayAvailability').textContent = studentData.availability || 'Not set';
                            if(document.getElementById('displayLearningPreference')) document.getElementById('displayLearningPreference').textContent = studentData.learningPreference || 'Not set';

                            // NEW: Display student type and premium discount
                            if (displayStudentType && studentData.studentTypeIdentifier) {
                                displayStudentType.textContent = studentData.studentTypeIdentifier.charAt(0).toUpperCase() + studentData.studentTypeIdentifier.slice(1).toLowerCase();
                            } else if (displayStudentType) {
                                displayStudentType.textContent = "Regular"; // Default if somehow missing
                            }

                            if (studentData.studentTypeIdentifier === 'PREMIUM' && typeof studentData.discountPercentage !== 'undefined') {
                                if (displayStudentDiscount) displayStudentDiscount.textContent = `${studentData.discountPercentage}% discount on sessions.`;
                                if (premiumStudentDiscountInfo) premiumStudentDiscountInfo.style.display = 'block';
                                if (premiumStudentUpdateFields) { // Show discount field in form if premium (though user won't update it here)
                                    // premiumStudentUpdateFields.style.display = 'block';
                                    // For now, users don't edit their own discount. This field is more for an admin form.
                                }
                            } else {
                                if (premiumStudentDiscountInfo) premiumStudentDiscountInfo.style.display = 'none';
                                if (premiumStudentUpdateFields) premiumStudentUpdateFields.style.display = 'none';
                            }


                            if (updateProfileForm) {
                                populateForm(updateProfileForm, {
                                    preferredSubjects: studentData.preferredSubjects ? studentData.preferredSubjects.join(', ') : '',
                                    availability: studentData.availability,
                                    learningPreference: studentData.learningPreference,
                                    // discountPercentage: studentData.discountPercentage // If it were editable by user
                                });
                            }
                        } else {
                             if(document.getElementById('displayPreferredSubjects')) document.getElementById('displayPreferredSubjects').textContent = 'Profile not fully set up.';
                        }
                    } catch (error) {
                        console.error("Error fetching student profile data:", error);
                        if(studentProfileDetailsDiv) studentProfileDetailsDiv.innerHTML = `<p class="text-red-400 text-sm">Could not load student preferences. ${error.message}</p>`;
                    }
                } else {
                    if (studentSpecificFieldsDiv) studentSpecificFieldsDiv.style.display = 'none';
                    if (studentProfileDetailsDiv) studentProfileDetailsDiv.style.display = 'none';
                }
            }

            if (updateProfileForm) {
                updateProfileForm.addEventListener('submit', async (event) => {
                    event.preventDefault();
                    // ... (logic for submitting userUpdateData remains the same) ...

                    const formData = new FormData(updateProfileForm);
                    if (errorMessageDiv) errorMessageDiv.textContent = '';
                    if (successMessageDiv) successMessageDiv.textContent = '';

                    const userUpdateData = {
                        email: formData.get('email'),
                        contactNumber: formData.get('contactNumber')
                    };
                    const password = formData.get('password');
                    if (password && password.trim() !== '') {
                        if (password.length < 6) {
                             if (errorMessageDiv) errorMessageDiv.textContent = 'New password must be at least 6 characters.';
                            return;
                        }
                        userUpdateData.password = password;
                    }

                    let userUpdateSuccess = false;
                    try {
                        const updatedUserResponse = await fetchData(`/users/${user.userId}`, {
                            method: 'PUT',
                            headers: { 'Content-Type': 'application/json' },
                            body: JSON.stringify(userUpdateData)
                        });

                        const newSessionUser = {
                            ...user, // old user data
                            email: updatedUserResponse.email, // updated email
                            contactNumber: updatedUserResponse.contactNumber // updated contact
                        };
                        saveUserSession(newSessionUser); // save updated session
                        updateNavOnLoginGlobal(); // refresh nav
                        userUpdateSuccess = true;
                    } catch (error) {
                        if (errorMessageDiv) errorMessageDiv.textContent = `User profile update failed: ${error.message}`;
                    }


                    let studentUpdateSuccess = false;
                    if (user.role === 'STUDENT') {
                        const studentUpdateData = {
                            studentId: user.userId,
                            name: user.username,
                            preferredSubjects: formData.get('preferredSubjects').split(',').map(s => s.trim()).filter(s => s),
                            availability: formData.get('availability'),
                            learningPreference: formData.get('learningPreference'),
                            // studentTypeIdentifier is not updated by the user here.
                            // discountPercentage is also not updated by the user here.
                        };

                        // Important: Fetch the existing student data to get its type and specific fields not on the form
                        try {
                            const existingStudentData = await fetchData(`/students/${user.userId}`);
                            if (existingStudentData) {
                                studentUpdateData.studentTypeIdentifier = existingStudentData.studentTypeIdentifier; // Preserve original type
                                if (existingStudentData.studentTypeIdentifier === 'PREMIUM') {
                                    // Preserve existing discount if not editable, or get from a hidden form field if it were
                                    studentUpdateData.discountPercentage = existingStudentData.discountPercentage;
                                }
                            }

                            await fetchData(`/students/${user.userId}`, {
                                method: 'PUT',
                                headers: { 'Content-Type': 'application/json' },
                                body: JSON.stringify(studentUpdateData)
                            });
                            studentUpdateSuccess = true;
                        } catch (error) {
                            if (errorMessageDiv) errorMessageDiv.textContent += ` Student preferences update failed: ${error.message}`;
                        }
                    }

                    if (userUpdateSuccess && (user.role !== 'STUDENT' || studentUpdateSuccess)) {
                        if (successMessageDiv) successMessageDiv.textContent = 'Profile updated successfully!';
                        setTimeout(() => { if(successMessageDiv) successMessageDiv.textContent = ''; }, 3000);
                        loadUserProfile();
                        updateProfileForm.querySelector('input[name="password"]').value = '';
                    } else if (userUpdateSuccess && user.role === 'STUDENT' && !studentUpdateSuccess) {
                        if (successMessageDiv) successMessageDiv.textContent = 'User details updated, but student preferences failed to update.';
                    }
                });
            }
            loadUserProfile();

    }
});

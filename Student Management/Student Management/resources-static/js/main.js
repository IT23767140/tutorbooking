// js/main.js

// Base URL for your Spring Boot API. Adjust if your backend runs on a different port or path.
const API_BASE_URL = 'http://localhost:8080/api';

/**
 * A utility function to make fetch requests to the backend API.
 * @param {string} endpoint - The API endpoint (e.g., '/users/login').
 * @param {object} options - Optional fetch options (method, headers, body, etc.).
 * @returns {Promise<any>} - A promise that resolves with the JSON response from the API.
 * @throws {Error} - Throws an error if the fetch operation or response handling fails.
 */
async function fetchData(endpoint, options = {}) {
    try {
        const response = await fetch(`${API_BASE_URL}${endpoint}`, options);

        if (response.status === 204) { // No Content
            return null;
        }

        const responseData = await response.json(); // Try to parse JSON regardless of ok status for error messages

        if (!response.ok) {
            // Use message from backend if available, otherwise a generic one
            const errorMessage = responseData.message || responseData.error || `HTTP error! Status: ${response.status}`;
            console.error('API Error:', endpoint, response.status, responseData);
            throw new Error(errorMessage);
        }

        return responseData;
    } catch (error) {
        console.error('Fetch operation failed for endpoint:', endpoint, error);
        // Display a user-friendly error message on the UI
        // Ensure this function is robust and doesn't itself cause errors.
        if (typeof displayGlobalErrorMessage === 'function') {
            displayGlobalErrorMessage(error.message || 'An unexpected network error occurred.');
        } else {
            // Fallback if displayGlobalErrorMessage is not available (e.g. called too early)
            alert(error.message || 'An unexpected network error occurred.');
        }
        throw error; // Re-throw for further handling by the caller if needed
    }
}

/**
 * Displays a global error message to the user.
 * Creates a div at the bottom right of the screen if it doesn't exist.
 * @param {string} message - The error message to display.
 */
function displayGlobalErrorMessage(message) {
    let errorDiv = document.getElementById('globalErrorDisplay');
    if (!errorDiv) {
        errorDiv = document.createElement('div');
        errorDiv.id = 'globalErrorDisplay';
        // Apply Tailwind classes for styling if possible, or define them in CSS
        errorDiv.className = 'fixed bottom-4 right-4 bg-red-600 text-white p-4 rounded-lg shadow-xl z-[100] text-sm';
        // Ensure body is available
        if (document.body) {
            document.body.appendChild(errorDiv);
        } else {
            // Fallback if body is not yet loaded (though this function is usually called later)
            console.warn('Global error display: document.body not ready.');
            alert(`Error: ${message}`); // Fallback to alert
            return;
        }
    }
    errorDiv.textContent = message;
    errorDiv.style.display = 'block';
    errorDiv.style.opacity = '1'; // Make sure it's visible

    // Automatically hide the message after some time
    setTimeout(() => {
        errorDiv.style.opacity = '0';
        // Remove or hide after transition
        setTimeout(() => {
            errorDiv.style.display = 'none';
        }, 500); // Match this with CSS transition if any
    }, 5000); // Hide after 5 seconds
}

/**
 * Helper function to get query parameters from the URL.
 * @param {string} paramName - The name of the query parameter.
 * @returns {string|null} - The value of the parameter or null if not found.
 */
function getQueryParam(paramName) {
    const urlParams = new URLSearchParams(window.location.search);
    return urlParams.get(paramName);
}

// Global event listener for handling logout if a logout button exists on multiple pages
document.addEventListener('DOMContentLoaded', () => {
    const navLogoutButton = document.getElementById('navLogoutLink'); // From common navbar
    if (navLogoutButton) {
        navLogoutButton.addEventListener('click', (e) => {
            e.preventDefault();
            if (typeof clearUserSession === 'function') {
                clearUserSession(); // This function should be in auth.js
            } else {
                // Fallback if auth.js isn't loaded or clearUserSession isn't global
                sessionStorage.clear();
                window.location.href = '/index.html';
            }
        });
    }
});

// Utility to populate form fields from an object
function populateForm(formElement, data) {
    if (!formElement || !data) return;
    for (const key in data) {
        if (data.hasOwnProperty(key)) {
            const field = formElement.elements[key];
            if (field) {
                if (field.type === 'checkbox') {
                    field.checked = data[key];
                } else if (field.type === 'radio') {
                    // For radio groups, find the one with the matching value
                    const radioGroup = formElement.elements[key];
                    if (radioGroup.length) { // It's a NodeList
                        Array.from(radioGroup).forEach(radio => {
                            if (radio.value === String(data[key])) {
                                radio.checked = true;
                            }
                        });
                    } else { // Single radio (less common for groups)
                         if(field.value === String(data[key])) field.checked = true;
                    }
                } else {
                    field.value = data[key];
                }
            }
        }
    }
}

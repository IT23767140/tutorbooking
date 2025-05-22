// js/payment.js

document.addEventListener('DOMContentLoaded', () => {
    const currentPage = window.location.pathname;

    // Payment Checkout Page Logic (payment-checkout.html)
    if (currentPage.includes('payment-checkout.html')) {
        if (!isLoggedIn()) {
            alert("Please login to proceed with payment.");
            window.location.href = '/login.html';
            return;
        }

        const bookingId = getQueryParam('bookingId');
        const paymentForm = document.getElementById('paymentForm');
        const bookingDetailsDiv = document.getElementById('bookingDetailsForPayment');
        const paymentMethodSelect = document.getElementById('paymentMethod');
        const creditCardFields = document.getElementById('creditCardFields');
        const payPalButtonContainer = document.getElementById('payPalButtonContainer'); // For a dummy PayPal button
        const errorMessageDiv = document.getElementById('errorMessage');

        if (!bookingId) {
            if (bookingDetailsDiv) bookingDetailsDiv.innerHTML = '<p class="text-red-400">Booking ID not provided for payment.</p>';
            if (paymentForm) paymentForm.style.display = 'none';
            return;
        }

        let bookingAmount = 0; // Store booking amount

        // Fetch booking details to display amount
        fetchData(`/bookings/${bookingId}`)
            .then(booking => {
                // For this project, amount might be fixed or derived. Let's assume a fixed amount per session for simplicity.
                // In a real app, the booking object or tutor object would have pricing info.
                bookingAmount = 50.00; // Example fixed amount
                if (bookingDetailsDiv) {
                    bookingDetailsDiv.innerHTML = `
                        <p><strong class="text-gray-300">Booking ID:</strong> ${booking.bookingId}</p>
                        <p><strong class="text-gray-300">Subject:</strong> ${booking.subject}</p>
                        <p><strong class="text-gray-300">Session Date:</strong> ${new Date(booking.sessionDateTime).toLocaleString()}</p>
                        <p class="text-xl font-semibold mt-2 text-white">Amount to Pay: $${bookingAmount.toFixed(2)}</p>
                    `;
                }
            })
            .catch(error => {
                if (bookingDetailsDiv) bookingDetailsDiv.innerHTML = `<p class="text-red-400">Could not load booking details. ${error.message}</p>`;
                if (paymentForm) paymentForm.style.display = 'none';
            });

        if (paymentMethodSelect) {
            paymentMethodSelect.addEventListener('change', () => {
                if (paymentMethodSelect.value === 'CREDIT_CARD') {
                    if (creditCardFields) creditCardFields.style.display = 'block';
                    if (payPalButtonContainer) payPalButtonContainer.style.display = 'none';
                } else if (paymentMethodSelect.value === 'PAYPAL') {
                    if (creditCardFields) creditCardFields.style.display = 'none';
                    if (payPalButtonContainer) payPalButtonContainer.style.display = 'block';
                } else {
                    if (creditCardFields) creditCardFields.style.display = 'none';
                    if (payPalButtonContainer) payPalButtonContainer.style.display = 'none';
                }
            });
            // Trigger change on load to set initial state
            paymentMethodSelect.dispatchEvent(new Event('change'));
        }


        if (paymentForm) {
            paymentForm.addEventListener('submit', async (event) => {
                event.preventDefault();
                const formData = new FormData(paymentForm);
                const paymentType = formData.get('paymentMethod');

                const paymentData = {
                    bookingId: bookingId,
                    amount: bookingAmount, // Use the fetched/calculated amount
                    paymentType: paymentType,
                    // status and paymentDate will be set by backend
                };

                if (paymentType === 'CREDIT_CARD') {
                    paymentData.cardNumber = formData.get('cardNumber'); // Dummy
                    paymentData.cardHolderName = formData.get('cardHolderName');
                    paymentData.expiryDate = formData.get('expiryDate');
                    // Add basic validation for dummy card fields if desired
                    if(!paymentData.cardNumber || !paymentData.cardHolderName || !paymentData.expiryDate) {
                        if(errorMessageDiv) errorMessageDiv.textContent = "Please fill all credit card details.";
                        return;
                    }
                } else if (paymentType === 'PAYPAL') {
                    paymentData.payPalEmail = getUserSession()?.email || 'student@example.com'; // Dummy PayPal email
                } else {
                     if(errorMessageDiv) errorMessageDiv.textContent = "Please select a payment method.";
                    return;
                }

                try {
                    const createdPayment = await fetchData('/payments', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify(paymentData)
                    });
                    alert('Payment successful! (Simulated)');
                    // Update booking status to 'PAID' or similar if your backend supports it
                    // await fetchData(`/bookings/${bookingId}/status`, { method: 'PUT', body: JSON.stringify({ status: 'PAID' }) });
                    window.location.href = `/payment-history.html?lastPaymentId=${createdPayment.paymentId}`;
                } catch (error) {
                    if (errorMessageDiv) errorMessageDiv.textContent = error.message || 'Payment failed. Please try again.';
                    console.error("Payment error:", error);
                }
            });
        }
    }

    // Payment History Page Logic (payment-history.html)
    if (currentPage.includes('payment-history.html')) {
        if (!isLoggedIn()) {
            alert("Please login to view payment history.");
            window.location.href = '/login.html';
            return;
        }

        const paymentHistoryContainer = document.getElementById('paymentHistoryContainer');
        const user = getUserSession();

        async function loadPaymentHistory() {
            if (!paymentHistoryContainer) return;
            paymentHistoryContainer.innerHTML = '<p class="text-gray-400">Loading payment history...</p>';
            try {
                // Backend ideally provides an endpoint like /api/payments/user/{userId}
                // For now, let's assume we fetch all user's bookings, then payments for each.
                // This is inefficient but works for a demo.
                const bookings = await fetchData(`/bookings/student/${user.userId}`);
                let allUserPayments = [];

                for (const booking of bookings) {
                    try {
                        const paymentsForBooking = await fetchData(`/payments/booking/${booking.bookingId}`);
                        allUserPayments.push(...paymentsForBooking);
                    } catch (e) {
                        console.warn(`Could not load payments for booking ${booking.bookingId}`, e);
                    }
                }

                // Sort payments by date, most recent first
                allUserPayments.sort((a, b) => new Date(b.paymentDate) - new Date(a.paymentDate));


                if (allUserPayments.length === 0) {
                    paymentHistoryContainer.innerHTML = '<p class="text-gray-400">No payment history found.</p>';
                    return;
                }

                let historyHtml = '<div class="space-y-4">';
                allUserPayments.forEach(payment => {
                    historyHtml += `
                        <div class="card">
                            <p><strong class="text-gray-300">Payment ID:</strong> ${payment.paymentId}</p>
                            <p><strong class="text-gray-300">Booking ID:</strong> ${payment.bookingId}</p>
                            <p><strong class="text-gray-300">Amount:</strong> $${Number(payment.amount).toFixed(2)}</p>
                            <p><strong class="text-gray-300">Date:</strong> ${new Date(payment.paymentDate).toLocaleDateString()}</p>
                            <p><strong class="text-gray-300">Status:</strong> <span class="${payment.status === 'COMPLETED' ? 'text-green-400' : 'text-yellow-400'}">${payment.status}</span></p>
                            <p><strong class="text-gray-300">Type:</strong> ${payment.paymentTypeIdentifier || payment.paymentTypeDetails || 'N/A'}</p>
                            ${payment.paymentTypeIdentifier === 'CREDIT_CARD' && payment.cardNumber ? `<p class="text-xs text-gray-500">Card: ****${payment.cardNumber.slice(-4)}</p>` : ''}
                            ${payment.paymentTypeIdentifier === 'PAYPAL' && payment.payPalEmail ? `<p class="text-xs text-gray-500">PayPal Email: ${payment.payPalEmail}</p>` : ''}
                        </div>
                    `;
                });
                historyHtml += '</div>';
                paymentHistoryContainer.innerHTML = historyHtml;

            } catch (error) {
                paymentHistoryContainer.innerHTML = `<p class="text-red-400">Could not load payment history. ${error.message}</p>`;
            }
        }
        loadPaymentHistory();
    }
});

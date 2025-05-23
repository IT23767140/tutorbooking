package com.PGN24.tutorbooking.model;

import java.util.Objects;

/**
 * Represents a user in the tutor booking system.
 * Demonstrates Encapsulation.
 * This class also includes a 'role' attribute to differentiate user types (e.g., STUDENT, TUTOR, ADMIN)
 * without needing separate subclasses for User types, simplifying based on previous discussion.
 */
public class User {
    // Private attributes to encapsulate user data
    private String userId;
    private String username;
    private String password; // In a real app, this should be hashed
    private String email;
    private String contactNumber;
    private String role; // e.g., "STUDENT", "TUTOR", "ADMIN"

    // Default constructor
    public User() {
    }

    // Parameterized constructor
    public User(String userId, String username, String password, String email, String contactNumber, String role) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.email = email;
        this.contactNumber = contactNumber;
        this.role = role;
    }

    // Public getter for userId
    public String getUserId() {
        return userId;
    }

    // Public setter for userId
    public void setUserId(String userId) {
        this.userId = userId;
    }

    // Public getter for username
    public String getUsername() {
        return username;
    }

    // Public setter for username
    public void setUsername(String username) {
        this.username = username;
    }

    // Public getter for password
    public String getPassword() {
        return password;
    }

    // Public setter for password
    public void setPassword(String password) {
        this.password = password;
    }

    // Public getter for email
    public String getEmail() {
        return email;
    }

    // Public setter for email
    public void setEmail(String email) {
        this.email = email;
    }

    // Public getter for contactNumber
    public String getContactNumber() {
        return contactNumber;
    }

    // Public setter for contactNumber
    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    // Public getter for role
    public String getRole() {
        return role;
    }

    // Public setter for role
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * Converts the User object to a CSV string for file storage.
     * The order of fields is: userId,username,password,email,contactNumber,role
     * @return A CSV string representation of the user.
     */
    @Override
    public String toString() {
        return String.join(",",
                Objects.toString(userId, ""),
                Objects.toString(username, ""),
                Objects.toString(password, ""), // Be cautious with storing plain passwords
                Objects.toString(email, ""),
                Objects.toString(contactNumber, ""),
                Objects.toString(role, "")
        );
    }

    /**
     * Creates a User object from a CSV string.
     * Assumes the CSV string is in the format: userId,username,password,email,contactNumber,role
     * @param csvLine The CSV string.
     * @return A User object, or null if the string is invalid.
     */
    public static User fromString(String csvLine) {
        if (csvLine == null || csvLine.isEmpty()) {
            return null;
        }
        String[] parts = csvLine.split(",", -1); // -1 limit to include trailing empty strings
        if (parts.length == 6) {
            return new User(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5]);
        }
        System.err.println("Invalid user CSV line: " + csvLine + " (Expected 6 parts, got " + parts.length + ")");
        return null; // Or throw an IllegalArgumentException
    }

    // Optional: equals and hashCode for comparisons, e.g., in collections
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(userId, user.userId) &&
                Objects.equals(username, user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, username);
    }
}

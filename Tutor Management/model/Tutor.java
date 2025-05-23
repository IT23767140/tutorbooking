package com.PGN24.tutorbooking.model;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents a tutor in the tutor booking system.
 * Demonstrates Encapsulation.
 * This class will be used in the BinarySearchTree and MergeSort implementations.
 */
public class Tutor {
    // Private attributes to encapsulate tutor data
    private String tutorId; // Could be linked to a UserId
    private String name;
    private List<String> subjects; // List of subjects the tutor teaches
    private double rating; // Average rating from reviews
    private String availability; // e.g., "Mon-Fri 9am-5pm", "Flexible"
    private String qualifications; // e.g., "MSc Computer Science"

    // Default constructor
    public Tutor() {
        this.subjects = new ArrayList<>();
    }

    // Parameterized constructor
    public Tutor(String tutorId, String name, List<String> subjects, double rating, String availability, String qualifications) {
        this.tutorId = tutorId;
        this.name = name;
        this.subjects = subjects != null ? new ArrayList<>(subjects) : new ArrayList<>();
        this.rating = rating;
        this.availability = availability;
        this.qualifications = qualifications;
    }

    // Getters and Setters demonstrating encapsulation
    public String getTutorId() {
        return tutorId;
    }

    public void setTutorId(String tutorId) {
        this.tutorId = tutorId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getSubjects() {
        return new ArrayList<>(subjects); // Return a copy for encapsulation
    }

    public void setSubjects(List<String> subjects) {
        this.subjects = subjects != null ? new ArrayList<>(subjects) : new ArrayList<>();
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getAvailability() {
        return availability;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }

    public String getQualifications() {
        return qualifications;
    }

    public void setQualifications(String qualifications) {
        this.qualifications = qualifications;
    }

    /**
     * A simple way to get a primary subject, e.g., the first one in the list.
     * Useful for sorting or display.
     * @return The first subject in the list, or an empty string if no subjects.
     */
    public String getPrimarySubject() {
        if (subjects != null && !subjects.isEmpty()) {
            return subjects.get(0);
        }
        return "";
    }


    /**
     * Converts the Tutor object to a CSV string for file storage.
     * Subjects are joined by a semicolon.
     * Format: tutorId,name,subject1;subject2,rating,availability,qualifications
     * @return A CSV string representation of the tutor.
     */
    @Override
    public String toString() {
        String subjectsString = subjects.stream().collect(Collectors.joining(";"));
        return String.join(",",
                Objects.toString(tutorId, ""),
                Objects.toString(name, ""),
                Objects.toString(subjectsString, ""),
                String.valueOf(rating),
                Objects.toString(availability, ""),
                Objects.toString(qualifications, "")
        );
    }

    /**
     * Creates a Tutor object from a CSV string.
     * Format: tutorId,name,subject1;subject2,rating,availability,qualifications
     * @param csvLine The CSV string.
     * @return A Tutor object, or null if the string is invalid.
     */
    public static Tutor fromString(String csvLine) {
        if (csvLine == null || csvLine.isEmpty()) {
            return null;
        }
        String[] parts = csvLine.split(",", -1);
        if (parts.length == 6) {
            String tutorId = parts[0];
            String name = parts[1];
            List<String> subjectsList = new ArrayList<>();
            if (parts[2] != null && !parts[2].isEmpty()) {
                subjectsList.addAll(Arrays.asList(parts[2].split(";")));
            }
            double rating = 0.0;
            try {
                rating = Double.parseDouble(parts[3]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid rating format for tutor " + name + ": " + parts[3]);
            }
            String availability = parts[4];
            String qualifications = parts[5];
            return new Tutor(tutorId, name, subjectsList, rating, availability, qualifications);
        }
        System.err.println("Invalid tutor CSV line: " + csvLine + " (Expected 6 parts, got " + parts.length + ")");
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tutor tutor = (Tutor) o;
        return Objects.equals(tutorId, tutor.tutorId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tutorId);
    }
}

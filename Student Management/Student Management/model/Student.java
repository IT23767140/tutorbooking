// In: tutorbooking/src/main/java/com/PGN24/tutorbooking/model/Student.java
package com.PGN24.tutorbooking.model;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public class Student { // Remains a concrete class for now
    private String studentId;
    private String name;
    private List<String> preferredSubjects;
    private String availability;
    private String learningPreference;
    protected String studentTypeIdentifier; // New field for identifying student type

    // Default constructor
    public Student() {
        this.preferredSubjects = new ArrayList<>();
        this.studentTypeIdentifier = "REGULAR"; // Default to REGULAR if no type is specified
    }

    // Parameterized constructor
    public Student(String studentId, String name, List<String> preferredSubjects, String availability, String learningPreference) {
        this(); // Call default constructor to set type and initialize list
        this.studentId = studentId;
        this.name = name;
        this.preferredSubjects = preferredSubjects != null ? new ArrayList<>(preferredSubjects) : new ArrayList<>();
        this.availability = availability;
        this.learningPreference = learningPreference;
        // studentTypeIdentifier is already set to REGULAR by this()
    }

    // Constructor for subclasses to set their type
    protected Student(String studentId, String name, List<String> preferredSubjects, String availability, String learningPreference, String studentTypeIdentifier) {
        this.studentId = studentId;
        this.name = name;
        this.preferredSubjects = preferredSubjects != null ? new ArrayList<>(preferredSubjects) : new ArrayList<>();
        this.availability = availability;
        this.learningPreference = learningPreference;
        this.studentTypeIdentifier = studentTypeIdentifier;
    }


    // Getters and Setters for existing fields...
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<String> getPreferredSubjects() { return new ArrayList<>(preferredSubjects); }
    public void setPreferredSubjects(List<String> preferredSubjects) { this.preferredSubjects = preferredSubjects != null ? new ArrayList<>(preferredSubjects) : new ArrayList<>(); }
    public String getAvailability() { return availability; }
    public void setAvailability(String availability) { this.availability = availability; }
    public String getLearningPreference() { return learningPreference; }
    public void setLearningPreference(String learningPreference) { this.learningPreference = learningPreference; }

    // Getter for studentTypeIdentifier (setter might not be needed if set only by constructor/subclass)
    public String getStudentTypeIdentifier() {
        return studentTypeIdentifier;
    }

    // Protected setter for subclasses if they need to set it post-construction (usually not)
    protected void setStudentTypeIdentifier(String studentTypeIdentifier) {
        this.studentTypeIdentifier = studentTypeIdentifier;
    }


    /**
     * Converts the Student object to a CSV string for file storage.
     * Format: studentId,name,subject1;subject2,availability,learningPreference,studentTypeIdentifier
     * @return A CSV string representation of the student.
     */
    @Override
    public String toString() {
        String subjectsString = preferredSubjects.stream().collect(Collectors.joining(";"));
        return String.join(",",
                Objects.toString(studentId, ""),
                Objects.toString(name, ""),
                Objects.toString(subjectsString, ""),
                Objects.toString(availability, ""),
                Objects.toString(learningPreference, ""),
                Objects.toString(studentTypeIdentifier, "REGULAR") // Add student type, default to REGULAR
        );
    }

    /**
     * Creates a Student object from a CSV string.
     * This will be the base parser. StudentService will determine the specific type.
     * Format: studentId,name,subject1;subject2,availability,learningPreference,studentTypeIdentifier
     * @param csvLine The CSV string.
     * @return A Student object, or null if the string is invalid.
     */
    public static Student fromString(String csvLine) {
        if (csvLine == null || csvLine.isEmpty()) {
            return null;
        }
        String[] parts = csvLine.split(",", -1);
        // Now expecting 6 parts (or 5 if type is missing for old data)
        if (parts.length >= 5) { // Adjusted to handle old data potentially missing the type
            String studentId = parts[0];
            String name = parts[1];
            List<String> preferredSubjects = new ArrayList<>();
            if (parts[2] != null && !parts[2].isEmpty()) {
                preferredSubjects.addAll(Arrays.asList(parts[2].split(";")));
            }
            String availability = parts[3];
            String learningPreference = parts[4];
            String type = (parts.length > 5 && parts[5] != null && !parts[5].isEmpty()) ? parts[5] : "REGULAR"; // Default to REGULAR if type is missing

            // The StudentService will handle creating RegularStudent or PremiumStudent.
            // For now, this base fromString can just return a base Student type.
            // Or, if Student became abstract, this method would be removed or changed.
            // Given Student is concrete:
            Student student = new Student(studentId, name, preferredSubjects, availability, learningPreference);
            student.setStudentTypeIdentifier(type); // Set the type explicitly
            return student;

        }
        System.err.println("Invalid student CSV line: " + csvLine + " (Expected at least 5 parts, got " + parts.length + ")");
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Student student = (Student) o;
        return Objects.equals(studentId, student.studentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentId);
    }

    // Optional: Add a method that subclasses can override for polymorphic behavior
    public String getMembershipType() {
        return "Regular Member"; // Default implementation
    }
}
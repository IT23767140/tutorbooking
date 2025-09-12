package com.PGN24.tutorbooking.model;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class RegularStudent extends Student {

    public static final String TYPE_IDENTIFIER = "REGULAR";

    // Default constructor
    public RegularStudent() {
        super(); // Calls Student's default constructor
        this.studentTypeIdentifier = TYPE_IDENTIFIER; // Explicitly set type
    }

    // Parameterized constructor
    public RegularStudent(String studentId, String name, List<String> preferredSubjects, String availability, String learningPreference) {
        super(studentId, name, preferredSubjects, availability, learningPreference, TYPE_IDENTIFIER);
    }

    /**
     * Creates a RegularStudent object from CSV string parts.
     * Assumes parts[5] is the type identifier.
     */
    public static RegularStudent fromStringParts(String[] parts) {
        if (parts.length >= 6 && TYPE_IDENTIFIER.equals(parts[5])) { // Check type
            List<String> preferredSubjects = new ArrayList<>();
            if (parts[2] != null && !parts[2].isEmpty()) {
                preferredSubjects.addAll(Arrays.asList(parts[2].split(";")));
            }
            return new RegularStudent(parts[0], parts[1], preferredSubjects, parts[3], parts[4]);
        }
        return null; // Or throw exception if parts don't match
    }

    // Optional: Override methods for specific regular student behavior
    @Override
    public String getMembershipType() {
        return "Regular Student";
    }

    // toString() is inherited from Student, which now includes studentTypeIdentifier
}
// In: tutorbooking/src/main/java/com/PGN24/tutorbooking/service/StudentService.java
package com.PGN24.tutorbooking.service;

import com.PGN24.tutorbooking.model.Student;
import com.PGN24.tutorbooking.model.RegularStudent; // Import new class
import com.PGN24.tutorbooking.model.PremiumStudent; // Import new class
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.Objects;

@Service
public class StudentService {
    private static final String STUDENT_FILE = "students.txt";
    private final FileService fileService;

    public StudentService() {
        this.fileService = new FileService();
    }

    /**
     * Creates a new student record.
     * Accepts any Student subtype (RegularStudent, PremiumStudent).
     * @param student The Student object (can be RegularStudent or PremiumStudent).
     * @return The created Student object, or null if creation failed.
     */
    public Student createStudent(Student student) { // Method signature remains the same
        if (student.getName() == null || student.getName().trim().isEmpty()) {
            System.err.println("Student creation failed: Name cannot be empty.");
            return null;
        }
        if (student.getStudentId() == null || student.getStudentId().trim().isEmpty()) {
            student.setStudentId("student-" + UUID.randomUUID().toString());
        }
        if (student.getPreferredSubjects() == null) {
            student.setPreferredSubjects(new ArrayList<>());
        }
        // student.toString() will now correctly use the overridden version if it's a PremiumStudent,
        // or the base Student's toString (which includes the type identifier).
        fileService.appendToFile(STUDENT_FILE, student.toString());
        return student;
    }

    /**
     * Helper method to parse a line from the student file into the correct Student subclass.
     * @param csvLine A line from the students.txt file.
     * @return A Student object (RegularStudent or PremiumStudent), or null if parsing fails.
     */
    private Student parseStudentFromString(String csvLine) {
        if (csvLine == null || csvLine.isEmpty()) {
            return null;
        }
        String[] parts = csvLine.split(",", -1);
        // studentId,name,subjects,availability,preference,TYPE_IDENTIFIER,[specific_fields...]
        // parts[5] should be the type identifier.
        if (parts.length < 6) { // Minimum 6 parts: id, name, subjects, avail, pref, type
            System.err.println("Invalid student CSV line (too few parts for type identification): " + csvLine);
            // Try parsing as a generic/regular student if it matches the old format (5 parts)
            if (parts.length == 5) {
                System.out.println("Attempting to parse as old format (defaulting to RegularStudent): " + csvLine);
                // Student.fromString can handle this by defaulting to REGULAR
                return RegularStudent.fromStringParts(new String[]{parts[0],parts[1],parts[2],parts[3],parts[4],RegularStudent.TYPE_IDENTIFIER});
            }
            return null;
        }
        String typeIdentifier = parts[5];

        if (RegularStudent.TYPE_IDENTIFIER.equals(typeIdentifier)) {
            return RegularStudent.fromStringParts(parts);
        } else if (PremiumStudent.TYPE_IDENTIFIER.equals(typeIdentifier)) {
            return PremiumStudent.fromStringParts(parts);
        } else {
            System.err.println("Unknown student type identifier in CSV: " + typeIdentifier + " in line: " + csvLine);
            // Fallback to generic Student if it makes sense for your logic, or return null
            // Student baseStudent = Student.fromString(csvLine); // If Student.fromString is robust enough
            // return baseStudent;
            return null;
        }
    }


    public Optional<Student> getStudentById(String studentId) {
        if (studentId == null || studentId.trim().isEmpty()) {
            return Optional.empty();
        }
        return fileService.readFile(STUDENT_FILE).stream()
                .map(this::parseStudentFromString) // Use the new parsing method
                .filter(Objects::nonNull)
                .filter(s -> studentId.equals(s.getStudentId()))
                .findFirst();
    }

    public List<Student> getAllStudents() {
        return fileService.readFile(STUDENT_FILE).stream()
                .map(this::parseStudentFromString) // Use the new parsing method
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public Student updateStudent(String studentId, Student updatedStudentDetails) {
        Optional<Student> existingStudentOpt = getStudentById(studentId); // This will now get the correct subtype
        if (existingStudentOpt.isEmpty()) {
            System.err.println("Update failed: Student with ID '" + studentId + "' not found.");
            return null;
        }

        Student existingStudent = existingStudentOpt.get();
        String oldStudentString = existingStudent.toString(); // Get the string of the specific subtype

        // Update common fields from Student class
        if (updatedStudentDetails.getName() != null && !updatedStudentDetails.getName().trim().isEmpty()) {
            existingStudent.setName(updatedStudentDetails.getName());
        }
        if (updatedStudentDetails.getPreferredSubjects() != null) {
            existingStudent.setPreferredSubjects(updatedStudentDetails.getPreferredSubjects());
        }
        if (updatedStudentDetails.getAvailability() != null) {
            existingStudent.setAvailability(updatedStudentDetails.getAvailability());
        }
        if (updatedStudentDetails.getLearningPreference() != null) {
            existingStudent.setLearningPreference(updatedStudentDetails.getLearningPreference());
        }

        // Handle subclass-specific fields if necessary
        if (existingStudent instanceof PremiumStudent && updatedStudentDetails instanceof PremiumStudent) {
            PremiumStudent existingPremium = (PremiumStudent) existingStudent;
            PremiumStudent updatedPremium = (PremiumStudent) updatedStudentDetails;
            existingPremium.setDiscountPercentage(updatedPremium.getDiscountPercentage());
        }
        // Add similar blocks for other types if they have specific updatable fields

        // The toString() method of the specific subtype (existingStudent) will be called.
        fileService.updateLineInFile(STUDENT_FILE, oldStudentString, existingStudent.toString());
        return existingStudent;
    }

    // deleteStudent, findStudentsByName, findStudentsBySubjectPreference can remain largely the same
    // as they operate on the Student type and its common properties or use the stream with parseStudentFromString.
    public boolean deleteStudent(String studentId) {
        Optional<Student> studentOpt = getStudentById(studentId);
        if (studentOpt.isPresent()) {
            fileService.deleteLineFromFile(STUDENT_FILE, studentOpt.get().toString());
            return true;
        }
        System.err.println("Deletion failed: Student with ID '" + studentId + "' not found.");
        return false;
    }

    public List<Student> findStudentsByName(String nameQuery) {
        if (nameQuery == null || nameQuery.trim().isEmpty()) {
            return getAllStudents();
        }
        String lowerCaseQuery = nameQuery.toLowerCase();
        return fileService.readFile(STUDENT_FILE).stream()
                .map(this::parseStudentFromString)
                .filter(Objects::nonNull)
                .filter(student -> student.getName().toLowerCase().contains(lowerCaseQuery))
                .collect(Collectors.toList());
    }

    public List<Student> findStudentsBySubjectPreference(String subjectQuery) {
        if (subjectQuery == null || subjectQuery.trim().isEmpty()) {
            return new ArrayList<>();
        }
        String lowerCaseQuery = subjectQuery.toLowerCase();
        return fileService.readFile(STUDENT_FILE).stream()
                .map(this::parseStudentFromString)
                .filter(Objects::nonNull)
                .filter(student -> student.getPreferredSubjects().stream()
                        .anyMatch(subject -> subject.toLowerCase().equals(lowerCaseQuery)))
                .collect(Collectors.toList());
    }
}
package com.PGN24.tutorbooking.service;

import com.PGN24.tutorbooking.model.RegularStudent;
import com.PGN24.tutorbooking.model.Student; // Import Student model
import com.PGN24.tutorbooking.model.User;
import org.springframework.beans.factory.annotation.Autowired; // For constructor injection
import org.springframework.stereotype.Service;

import java.util.ArrayList; // For empty lists for student
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.Objects;

@Service
public class UserService {
    private static final String USER_FILE = "users.txt";
    private final FileService fileService;
    private final StudentService studentService; // Inject StudentService

    /**
     * Constructor for UserService with dependency injection.
     * @param studentService The StudentService instance.
     */
    @Autowired // Spring will inject StudentService here
    public UserService(StudentService studentService) {
        this.fileService = new FileService();
        this.studentService = studentService;
    }

    /**
     * Registers a new user.
     * If the user's role is "STUDENT", a corresponding student profile is also created.
     * @param user The User object to register. Username and password should be set.
     * @return The registered User object, or null if registration failed.
     */
    public User registerUser(User user) {
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            System.err.println("Registration failed: Username cannot be empty.");
            return null;
        }
        if (getUserByUsername(user.getUsername()).isPresent()) {
            System.err.println("Registration failed: Username '" + user.getUsername() + "' already exists.");
            return null;
        }

        if (user.getUserId() == null || user.getUserId().trim().isEmpty()) {
            user.setUserId("user-" + UUID.randomUUID().toString());
        }

        if (user.getRole() == null || user.getRole().trim().isEmpty()) {
            user.setRole("STUDENT"); // Default role
        }

        // Save the user to users.txt
        fileService.appendToFile(USER_FILE, user.toString());
        System.out.println("User registered: " + user.getUsername() + " with role " + user.getRole());

        if ("STUDENT".equalsIgnoreCase(user.getRole())) {
            System.out.println("Attempting to create student profile for user: " + user.getUsername());

            // Decide the type of student to create. For now, let's default to RegularStudent.
            // In a more complex app, this could come from the registration form.
            Student newStudentProfile = new RegularStudent(); // <-- Change here

            newStudentProfile.setStudentId(user.getUserId());
            newStudentProfile.setName(user.getUsername());
            newStudentProfile.setPreferredSubjects(new ArrayList<>());
            newStudentProfile.setAvailability("");
            newStudentProfile.setLearningPreference("");

            // If it's a PremiumStudent, you might set specific fields:
            // if (/* some condition for premium */) {
            //     PremiumStudent premiumProfile = new PremiumStudent();
            //     premiumProfile.setStudentId(user.getUserId());
            //     premiumProfile.setName(user.getUsername());
            //     // ... set other premium fields ...
            //     newStudentProfile = premiumProfile;
            // }


            Student createdStudentProfile = studentService.createStudent(newStudentProfile); // Pass the specific type
            if (createdStudentProfile != null) {
                System.out.println("Student profile created successfully for: " + user.getUsername() + " with studentId: " + createdStudentProfile.getStudentId() + " and Type: " + createdStudentProfile.getStudentTypeIdentifier());
            } else {
                System.err.println("Failed to create student profile for user: " + user.getUsername());
            }
        }
        return user;
    }

    public Optional<User> getUserByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return Optional.empty();
        }
        return fileService.readFile(USER_FILE).stream()
                .map(User::fromString)
                .filter(Objects::nonNull)
                .filter(u -> username.equals(u.getUsername()))
                .findFirst();
    }

    public Optional<User> getUserById(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return Optional.empty();
        }
        return fileService.readFile(USER_FILE).stream()
                .map(User::fromString)
                .filter(Objects::nonNull)
                .filter(u -> userId.equals(u.getUserId()))
                .findFirst();
    }

    public List<User> getAllUsers() {
        return fileService.readFile(USER_FILE).stream()
                .map(User::fromString)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public User updateUser(String userId, User updatedUserDetails) {
        Optional<User> existingUserOpt = getUserById(userId);
        if (existingUserOpt.isEmpty()) {
            System.err.println("Update failed: User with ID '" + userId + "' not found.");
            return null;
        }

        User existingUser = existingUserOpt.get();
        String oldUserString = existingUser.toString();

        if (updatedUserDetails.getPassword() != null && !updatedUserDetails.getPassword().isEmpty()) {
            existingUser.setPassword(updatedUserDetails.getPassword());
        }
        if (updatedUserDetails.getEmail() != null) {
            existingUser.setEmail(updatedUserDetails.getEmail());
        }
        if (updatedUserDetails.getContactNumber() != null) {
            existingUser.setContactNumber(updatedUserDetails.getContactNumber());
        }
        if (updatedUserDetails.getRole() != null && !updatedUserDetails.getRole().trim().isEmpty()) {
            // Consider implications if role changes from/to STUDENT regarding the students.txt entry
            existingUser.setRole(updatedUserDetails.getRole());
        }

        fileService.updateLineInFile(USER_FILE, oldUserString, existingUser.toString());

        // If user details that are also in Student profile (like name if linked) are updated,
        // you might need to update students.txt as well.
        // For now, assuming student name is only set at creation from username.
        if ("STUDENT".equalsIgnoreCase(existingUser.getRole())) {
            Optional<Student> studentOpt = studentService.getStudentById(existingUser.getUserId());
            if (studentOpt.isPresent()) {
                Student studentToUpdate = studentOpt.get();
                boolean studentNeedsUpdate = false;
                // If username changes and it's used as student name
                // if (!existingUser.getUsername().equals(studentToUpdate.getName())) {
                //    studentToUpdate.setName(existingUser.getUsername());
                //    studentNeedsUpdate = true;
                // }
                // If other user fields map to student fields that can be updated via user profile
                // For now, student-specific fields are updated via StudentService directly.
            } else {
                // If user role was changed to STUDENT, and no student profile exists, create one.
                System.out.println("User role updated to STUDENT, creating student profile for: " + existingUser.getUsername());
                Student newStudent = new Student();
                newStudent.setStudentId(existingUser.getUserId());
                newStudent.setName(existingUser.getUsername());
                newStudent.setPreferredSubjects(new ArrayList<>());
                newStudent.setAvailability("");
                newStudent.setLearningPreference("");
                studentService.createStudent(newStudent);
            }
        }


        return existingUser;
    }

    public boolean deleteUser(String userId) {
        Optional<User> userOpt = getUserById(userId);
        if (userOpt.isPresent()) {
            User userToDelete = userOpt.get();
            fileService.deleteLineFromFile(USER_FILE, userToDelete.toString());

            // If the user was a student, also delete their student profile
            if ("STUDENT".equalsIgnoreCase(userToDelete.getRole())) {
                boolean studentProfileDeleted = studentService.deleteStudent(userToDelete.getUserId());
                if (studentProfileDeleted) {
                    System.out.println("Student profile deleted for user: " + userToDelete.getUsername());
                } else {
                    System.err.println("Could not find or delete student profile for user: " + userToDelete.getUsername());
                }
            }
            return true;
        }
        System.err.println("Deletion failed: User with ID '" + userId + "' not found.");
        return false;
    }
}


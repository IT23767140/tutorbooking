package com.PGN24.tutorbooking.controller;

import com.PGN24.tutorbooking.model.User;
import com.PGN24.tutorbooking.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST Controller for managing User-related operations.
 * Handles HTTP requests for user registration, login, profile management, etc.
 */
@RestController
@RequestMapping("/api/users") // Base path for all user-related endpoints
public class UserController {

    private final UserService userService;

    /**
     * Constructor for UserController.
     * @param userService The UserService instance, injected by Spring.
     */
    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Handles user registration.
     * HTTP POST to /api/users/register
     * @param user The User object from the request body.
     * @return ResponseEntity containing the registered User and HTTP status 201 (Created),
     * or HTTP status 409 (Conflict) if username exists, or 400 (Bad Request) for invalid input.
     */
    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@RequestBody User user) {
        // Basic validation (more can be added)
        if (user.getUsername() == null || user.getUsername().trim().isEmpty() ||
                user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            return ResponseEntity.badRequest().build(); // Or a custom error response
        }
        User registeredUser = userService.registerUser(user);
        if (registeredUser != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(registeredUser);
        } else {
            // This typically means username conflict or other registration business rule failure
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    /**
     * Handles user login.
     * HTTP POST to /api/users/login
     * @param loginAttempt A User object containing username and password for login attempt.
     * @return ResponseEntity containing the logged-in User and HTTP status 200 (OK),
     * or HTTP status 401 (Unauthorized) if login fails.
     */
    @PostMapping("/login")
    public ResponseEntity<User> loginUser(@RequestBody User loginAttempt) {
        if (loginAttempt.getUsername() == null || loginAttempt.getPassword() == null) {
            return ResponseEntity.badRequest().build();
        }
        Optional<User> userOpt = userService.getUserByUsername(loginAttempt.getUsername());
        if (userOpt.isPresent() && userOpt.get().getPassword().equals(loginAttempt.getPassword())) {
            // In a real app, don't send the password back.
            // Create a UserDTO (Data Transfer Object) without sensitive info.
            User userToReturn = userOpt.get();
            // userToReturn.setPassword(null); // Mask password before sending
            return ResponseEntity.ok(userToReturn);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    /**
     * Retrieves a user by their ID.
     * HTTP GET to /api/users/{userId}
     * @param userId The ID of the user to retrieve.
     * @return ResponseEntity containing the User if found (200 OK), or 404 (Not Found).
     */
    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable String userId) {
        Optional<User> userOpt = userService.getUserById(userId);
        return userOpt.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Retrieves a user by their username (e.g., for profile viewing).
     * HTTP GET to /api/users/profile/{username}
     * @param username The username of the user to retrieve.
     * @return ResponseEntity containing the User if found (200 OK), or 404 (Not Found).
     */
    @GetMapping("/profile/{username}")
    public ResponseEntity<User> getUserProfileByUsername(@PathVariable String username) {
        Optional<User> userOpt = userService.getUserByUsername(username);
        return userOpt.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Retrieves all users. (Typically an admin-only function).
     * HTTP GET to /api/users
     * @return ResponseEntity containing a List of all Users (200 OK).
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * Updates an existing user's details.
     * HTTP PUT to /api/users/{userId}
     * @param userId The ID of the user to update.
     * @param userDetails The User object from the request body containing updated information.
     * @return ResponseEntity containing the updated User (200 OK), or 404 (Not Found) if user doesn't exist.
     */
    @PutMapping("/{userId}")
    public ResponseEntity<User> updateUser(@PathVariable String userId, @RequestBody User userDetails) {
        // Ensure the userDetails doesn't try to change the username if it's not allowed,
        // or handle it appropriately in the service layer.
        User updatedUser = userService.updateUser(userId, userDetails);
        if (updatedUser != null) {
            return ResponseEntity.ok(updatedUser);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Deletes a user by their ID. (Typically an admin-only function).
     * HTTP DELETE to /api/users/{userId}
     * @param userId The ID of the user to delete.
     * @return ResponseEntity with 204 (No Content) if successful, or 404 (Not Found) if user doesn't exist.
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable String userId) {
        boolean deleted = userService.deleteUser(userId);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}

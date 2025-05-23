package com.PGN24.tutorbooking.controller;

import com.PGN24.tutorbooking.model.Tutor;
import com.PGN24.tutorbooking.service.TutorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * REST Controller for managing Tutor-related operations.
 */
@RestController
@RequestMapping("/api/tutors") // Base path for all tutor-related endpoints
public class TutorController {

    private final TutorService tutorService;

    @Autowired
    public TutorController(TutorService tutorService) {
        this.tutorService = tutorService;
    }

    /**
     * Adds a new tutor.
     * HTTP POST to /api/tutors
     * @param tutor The Tutor object from the request body.
     * @return ResponseEntity containing the added Tutor (201 Created), or 400 (Bad Request) / 409 (Conflict).
     */
    @PostMapping
    public ResponseEntity<Tutor> addTutor(@RequestBody Tutor tutor) {
        if (tutor.getName() == null || tutor.getName().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        Tutor addedTutor = tutorService.addTutor(tutor);
        if (addedTutor != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(addedTutor);
        }
        // Could be a conflict (e.g., ID already exists) or other validation error in service
        return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }

    /**
     * Retrieves a tutor by their ID.
     * HTTP GET to /api/tutors/{tutorId}
     * @param tutorId The ID of the tutor.
     * @return ResponseEntity containing the Tutor (200 OK), or 404 (Not Found).
     */
    @GetMapping("/{tutorId}")
    public ResponseEntity<Tutor> getTutorById(@PathVariable String tutorId) {
        // Can use either findTutorByIdUsingBst or findTutorById from service
        Optional<Tutor> tutorOpt = tutorService.findTutorById(tutorId);
        return tutorOpt.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Retrieves all tutors.
     * HTTP GET to /api/tutors
     * @return ResponseEntity containing a List of all Tutors (200 OK).
     */
    @GetMapping
    public ResponseEntity<List<Tutor>> getAllTutors() {
        List<Tutor> tutors = tutorService.getAllTutors();
        return ResponseEntity.ok(tutors);
    }

    /**
     * Searches for tutors by subject.
     * HTTP GET to /api/tutors/search/subject?subject={subjectName}
     * @param subject The subject to search for.
     * @return ResponseEntity with a list of matching tutors (200 OK).
     */
    @GetMapping("/search/subject")
    public ResponseEntity<List<Tutor>> findTutorsBySubject(@RequestParam String subject) {
        List<Tutor> tutors = tutorService.findTutorsBySubject(subject);
        return ResponseEntity.ok(tutors);
    }

    /**
     * Retrieves tutors sorted by a specified criterion.
     * HTTP GET to /api/tutors/sorted?by={criteria}&order={asc/desc}
     * Example: /api/tutors/sorted?by=rating&order=desc
     * @param sortBy Criteria for sorting ("name", "rating", "subject").
     * @param order Sort order ("asc" for ascending, "desc" for descending). Defaults to "asc".
     * @return ResponseEntity with a sorted list of tutors (200 OK), or 400 (Bad Request) for invalid criteria.
     */
    @GetMapping("/sorted")
    public ResponseEntity<List<Tutor>> getSortedTutors(
            @RequestParam String by,
            @RequestParam(required = false, defaultValue = "asc") String order) {

        Comparator<Tutor> comparator;
        switch (by.toLowerCase()) {
            case "name":
                comparator = Comparator.comparing(Tutor::getName);
                break;
            case "rating":
                comparator = Comparator.comparingDouble(Tutor::getRating);
                break;
            case "subject": // Sorts by primary subject (first in list)
                comparator = Comparator.comparing(Tutor::getPrimarySubject);
                break;
            default:
                return ResponseEntity.badRequest().build(); // Invalid sort criteria
        }

        if ("desc".equalsIgnoreCase(order)) {
            comparator = comparator.reversed();
        }

        List<Tutor> sortedTutors = tutorService.getSortedTutors(comparator);
        return ResponseEntity.ok(sortedTutors);
    }


    /**
     * Updates an existing tutor's details.
     * HTTP PUT to /api/tutors/{tutorId}
     * @param tutorId The ID of the tutor to update.
     * @param tutorDetails The Tutor object with updated information.
     * @return ResponseEntity containing the updated Tutor (200 OK), or 404 (Not Found).
     */
    @PutMapping("/{tutorId}")
    public ResponseEntity<Tutor> updateTutor(@PathVariable String tutorId, @RequestBody Tutor tutorDetails) {
        Tutor updatedTutor = tutorService.updateTutor(tutorId, tutorDetails);
        if (updatedTutor != null) {
            return ResponseEntity.ok(updatedTutor);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Deletes a tutor by their ID.
     * HTTP DELETE to /api/tutors/{tutorId}
     * @param tutorId The ID of the tutor to delete.
     * @return ResponseEntity with 204 (No Content) if successful, or 404 (Not Found).
     */
    @DeleteMapping("/{tutorId}")
    public ResponseEntity<Void> deleteTutor(@PathVariable String tutorId) {
        boolean deleted = tutorService.deleteTutor(tutorId);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}

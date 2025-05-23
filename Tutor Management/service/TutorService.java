package com.PGN24.tutorbooking.service;

import com.PGN24.tutorbooking.model.Tutor;
import com.PGN24.tutorbooking.dsa.BinarySearchTree;
import com.PGN24.tutorbooking.dsa.MergeSort; // Assuming this is the correct name from your DSA layer
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct; // For Spring Boot 3+

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.Objects;

/**
 * Service class for managing Tutor data.
 * Utilizes BinarySearchTree for efficient searching and MergeSort for sorting tutors.
 */
@Service
public class TutorService {
    private static final String TUTOR_FILE = "tutors.txt";
    private final FileService fileService;

    // In-memory storage for Tutors
    private List<Tutor> allTutorsList; // Master list of all tutors
    private BinarySearchTree tutorBstById; // BST keyed by Tutor ID for fast lookups by ID

    // Default comparator for the BST (e.g., by Tutor ID)
    private final Comparator<Tutor> idComparator = Comparator.comparing(Tutor::getTutorId);

    public TutorService() {
        this.fileService = new FileService();
        this.allTutorsList = new ArrayList<>();
        // Initialize BST with a comparator (e.g., by Tutor ID)
        this.tutorBstById = new BinarySearchTree(idComparator);
    }

    /**
     * Initializes the tutor data by loading from the file when the service is created.
     * This method is called by Spring after the bean has been constructed.
     */
    @PostConstruct
    public void initializeTutorData() {
        loadTutorsFromFile();
    }

    /**
     * Loads tutors from the tutors.txt file into the in-memory list and BST.
     * This method encapsulates the data loading logic.
     */
    private synchronized void loadTutorsFromFile() {
        allTutorsList.clear();
        tutorBstById.clear(); // Assuming BST has a clear method

        List<String> lines = fileService.readFile(TUTOR_FILE);
        for (String line : lines) {
            Tutor tutor = Tutor.fromString(line);
            if (tutor != null) {
                allTutorsList.add(tutor);
                tutorBstById.insert(tutor);
            }
        }
    }

    /**
     * Adds a new tutor to the system.
     * @param tutor The Tutor object to add. A unique ID will be generated if not provided.
     * @return The added Tutor object, or null if a tutor with the same ID already exists.
     */
    public Tutor addTutor(Tutor tutor) {
        if (tutor.getName() == null || tutor.getName().trim().isEmpty()) {
            System.err.println("Tutor creation failed: Name cannot be empty.");
            return null;
        }
        if (tutor.getTutorId() == null || tutor.getTutorId().trim().isEmpty()) {
            tutor.setTutorId("tutor-" + UUID.randomUUID().toString());
        } else {
            // Check if tutor ID already exists in the list (more reliable than just BST for this check)
            if (allTutorsList.stream().anyMatch(t -> t.getTutorId().equals(tutor.getTutorId()))) {
                System.err.println("Add tutor failed: Tutor ID '" + tutor.getTutorId() + "' already exists.");
                return null;
            }
        }

        fileService.appendToFile(TUTOR_FILE, tutor.toString());
        allTutorsList.add(tutor);
        tutorBstById.insert(tutor);
        return tutor;
    }

    /**
     * Finds a tutor by their ID using the Binary Search Tree.
     * @param tutorId The ID of the tutor to find.
     * @return An Optional containing the Tutor if found, otherwise an empty Optional.
     */
    public Optional<Tutor> findTutorByIdUsingBst(String tutorId) {
        if (tutorId == null || tutorId.trim().isEmpty()) {
            return Optional.empty();
        }
        // Create a dummy tutor object with the ID for searching in BST
        // This relies on the BST's comparator using only the ID for comparison during search.
        Tutor searchKey = new Tutor();
        searchKey.setTutorId(tutorId);
        Tutor foundTutor = tutorBstById.search(searchKey);
        return Optional.ofNullable(foundTutor);
    }

    /**
     * Finds a tutor by their ID by searching the list (fallback or general purpose).
     * @param tutorId The ID of the tutor to find.
     * @return An Optional containing the Tutor if found, otherwise an empty Optional.
     */
    public Optional<Tutor> findTutorById(String tutorId) {
        if (tutorId == null || tutorId.trim().isEmpty()) {
            return Optional.empty();
        }
        return allTutorsList.stream()
                .filter(t -> tutorId.equals(t.getTutorId()))
                .findFirst();
    }


    /**
     * Retrieves all tutors.
     * @return A new List containing all Tutor objects (to prevent modification of the internal list).
     */
    public List<Tutor> getAllTutors() {
        return new ArrayList<>(allTutorsList); // Return a copy
    }

    /**
     * Searches for tutors by subject expertise (case-insensitive match).
     * @param subject The subject to search for.
     * @return A List of Tutors who teach the given subject.
     */
    public List<Tutor> findTutorsBySubject(String subject) {
        if (subject == null || subject.trim().isEmpty()) {
            return new ArrayList<>();
        }
        String lowerCaseSubject = subject.toLowerCase();
        return allTutorsList.stream()
                .filter(tutor -> tutor.getSubjects().stream()
                        .anyMatch(s -> s.toLowerCase().contains(lowerCaseSubject)))
                .collect(Collectors.toList());
    }

    /**
     * Sorts the list of all tutors using MergeSort based on the provided comparator.
     * This demonstrates polymorphism as different comparators can be used for different sorting strategies.
     * @param comparator The Comparator to define the sorting order.
     * @return A new List of Tutors sorted according to the comparator.
     */
    public List<Tutor> getSortedTutors(Comparator<Tutor> comparator) {
        if (comparator == null) {
            throw new IllegalArgumentException("Comparator cannot be null for sorting tutors.");
        }
        List<Tutor> tutorsToSort = new ArrayList<>(allTutorsList); // Sort a copy
        MergeSort.sort(tutorsToSort, comparator);
        return tutorsToSort;
    }

    /**
     * Updates an existing tutor's details.
     * @param tutorId The ID of the tutor to update.
     * @param updatedTutorDetails A Tutor object containing the new details.
     * @return The updated Tutor object, or null if not found or update failed.
     */
    public Tutor updateTutor(String tutorId, Tutor updatedTutorDetails) {
        Optional<Tutor> existingTutorOpt = findTutorById(tutorId); // Use list search for finding the object to update
        if (existingTutorOpt.isEmpty()) {
            System.err.println("Update failed: Tutor with ID '" + tutorId + "' not found.");
            return null;
        }

        Tutor existingTutor = existingTutorOpt.get();
        String oldTutorString = existingTutor.toString();

        // Update fields (ensure tutorId itself is not changed if it's the BST key and not handled)
        if (updatedTutorDetails.getName() != null && !updatedTutorDetails.getName().trim().isEmpty()) {
            existingTutor.setName(updatedTutorDetails.getName());
        }
        if (updatedTutorDetails.getSubjects() != null) {
            existingTutor.setSubjects(updatedTutorDetails.getSubjects());
        }
        existingTutor.setRating(updatedTutorDetails.getRating()); // Rating can be 0.0
        if (updatedTutorDetails.getAvailability() != null) {
            existingTutor.setAvailability(updatedTutorDetails.getAvailability());
        }
        if (updatedTutorDetails.getQualifications() != null) {
            existingTutor.setQualifications(updatedTutorDetails.getQualifications());
        }


        fileService.updateLineInFile(TUTOR_FILE, oldTutorString, existingTutor.toString());
        // After file update, reload data to ensure consistency in BST and list
        loadTutorsFromFile();

        // Return the updated tutor from the reloaded list
        return findTutorById(tutorId).orElse(null);
    }

    /**
     * Deletes a tutor by their ID.
     * @param tutorId The ID of the tutor to delete.
     * @return true if the tutor was successfully deleted, false otherwise.
     */
    public boolean deleteTutor(String tutorId) {
        Optional<Tutor> tutorOpt = findTutorById(tutorId); // Find in list first
        if (tutorOpt.isPresent()) {
            fileService.deleteLineFromFile(TUTOR_FILE, tutorOpt.get().toString());
            // After file deletion, reload data to refresh BST and list
            loadTutorsFromFile();
            return true;
        }
        System.err.println("Deletion failed: Tutor with ID '" + tutorId + "' not found.");
        return false;
    }
}

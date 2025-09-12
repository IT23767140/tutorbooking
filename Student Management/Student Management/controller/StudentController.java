package com.PGN24.tutorbooking.controller;


import com.PGN24.tutorbooking.model.Student;
import com.PGN24.tutorbooking.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST Controller for managing Student-related operations.
 */
@RestController
@RequestMapping("/api/students") // Base path for all student-related endpoints
public class StudentController {

    private final StudentService studentService;

    @Autowired
    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    /**
     * Creates a new student record.
     * HTTP POST to /api/students
     * @param student The Student object from the request body.
     * @return ResponseEntity containing the created Student (201 Created), or 400 (Bad Request).
     */
    @PostMapping
    public ResponseEntity<Student> createStudent(@RequestBody Student student) {
        if (student.getName() == null || student.getName().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        Student createdStudent = studentService.createStudent(student);
        if (createdStudent != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(createdStudent);
        }
        return ResponseEntity.badRequest().build(); // Or a more specific error
    }

    /**
     * Retrieves a student by their ID.
     * HTTP GET to /api/students/{studentId}
     * @param studentId The ID of the student.
     * @return ResponseEntity containing the Student (200 OK), or 404 (Not Found).
     */
    @GetMapping("/{studentId}")
    public ResponseEntity<Student> getStudentById(@PathVariable String studentId) {
        Optional<Student> studentOpt = studentService.getStudentById(studentId);
        return studentOpt.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Retrieves all students.
     * HTTP GET to /api/students
     * @return ResponseEntity containing a List of all Students (200 OK).
     */
    @GetMapping
    public ResponseEntity<List<Student>> getAllStudents() {
        List<Student> students = studentService.getAllStudents();
        return ResponseEntity.ok(students);
    }

    /**
     * Updates an existing student's details.
     * HTTP PUT to /api/students/{studentId}
     * @param studentId The ID of the student to update.
     * @param studentDetails The Student object with updated information.
     * @return ResponseEntity containing the updated Student (200 OK), or 404 (Not Found).
     */
    @PutMapping("/{studentId}")
    public ResponseEntity<Student> updateStudent(@PathVariable String studentId, @RequestBody Student studentDetails) {
        Student updatedStudent = studentService.updateStudent(studentId, studentDetails);
        if (updatedStudent != null) {
            return ResponseEntity.ok(updatedStudent);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Deletes a student by their ID.
     * HTTP DELETE to /api/students/{studentId}
     * @param studentId The ID of the student to delete.
     * @return ResponseEntity with 204 (No Content) if successful, or 404 (Not Found).
     */
    @DeleteMapping("/{studentId}")
    public ResponseEntity<Void> deleteStudent(@PathVariable String studentId) {
        boolean deleted = studentService.deleteStudent(studentId);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Searches for students by name.
     * HTTP GET to /api/students/search/name?query={nameQuery}
     * @param nameQuery The name (or part of it) to search for.
     * @return ResponseEntity with a list of matching students (200 OK).
     */
    @GetMapping("/search/name")
    public ResponseEntity<List<Student>> findStudentsByName(@RequestParam("query") String nameQuery) {
        List<Student> students = studentService.findStudentsByName(nameQuery);
        return ResponseEntity.ok(students);
    }

    /**
     * Searches for students by preferred subject.
     * HTTP GET to /api/students/search/subject?query={subjectQuery}
     * @param subjectQuery The subject to search for.
     * @return ResponseEntity with a list of matching students (200 OK).
     */
    @GetMapping("/search/subject")
    public ResponseEntity<List<Student>> findStudentsBySubjectPreference(@RequestParam("query") String subjectQuery) {
        List<Student> students = studentService.findStudentsBySubjectPreference(subjectQuery);
        return ResponseEntity.ok(students);
    }
}

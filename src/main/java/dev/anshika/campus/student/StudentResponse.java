package dev.anshika.campus.student;

import java.time.Instant;
import java.time.LocalDate;

public record StudentResponse(Long id, String fullName, String email, String course,
        int academicYear, StudentStatus status, LocalDate enrolledOn,
        Instant createdAt, Instant updatedAt) {
    static StudentResponse from(Student s) {
        return new StudentResponse(s.getId(), s.getFullName(), s.getEmail(), s.getCourse(),
                s.getAcademicYear(), s.getStatus(), s.getEnrolledOn(), s.getCreatedAt(), s.getUpdatedAt());
    }
}

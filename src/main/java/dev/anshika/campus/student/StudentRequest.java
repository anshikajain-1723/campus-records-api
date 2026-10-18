package dev.anshika.campus.student;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record StudentRequest(
        @NotBlank @Size(min = 2, max = 80) String fullName,
        @NotBlank @Email @Size(max = 254) String email,
        @NotBlank @Size(min = 2, max = 80) String course,
        @NotNull @Min(1) @Max(4) Integer academicYear,
        @NotNull StudentStatus status,
        @NotNull @PastOrPresent LocalDate enrolledOn) {
    public StudentRequest {
        fullName = fullName == null ? null : fullName.strip();
        email = email == null ? null : email.strip().toLowerCase(java.util.Locale.ROOT);
        course = course == null ? null : course.strip();
    }
}

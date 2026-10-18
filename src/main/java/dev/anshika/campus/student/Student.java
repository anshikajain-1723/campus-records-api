package dev.anshika.campus.student;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "students")
public class Student {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "full_name", nullable = false, length = 80)
    private String fullName;
    @Column(nullable = false, unique = true, length = 254)
    private String email;
    @Column(nullable = false, length = 80)
    private String course;
    @Column(name = "academic_year", nullable = false)
    private int academicYear;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private StudentStatus status;
    @Column(name = "enrolled_on", nullable = false)
    private LocalDate enrolledOn;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Student() { }
    Student(StudentRequest request) { update(request); }
    void update(StudentRequest request) {
        fullName = request.fullName();
        email = request.email();
        course = request.course();
        academicYear = request.academicYear();
        status = request.status();
        enrolledOn = request.enrolledOn();
    }
    @PrePersist void onCreate() { createdAt = updatedAt = Instant.now(); }
    @PreUpdate void onUpdate() { updatedAt = Instant.now(); }
    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getCourse() { return course; }
    public int getAcademicYear() { return academicYear; }
    public StudentStatus getStatus() { return status; }
    public LocalDate getEnrolledOn() { return enrolledOn; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}

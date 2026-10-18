CREATE TABLE students (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(80) NOT NULL,
    email VARCHAR(254) NOT NULL,
    course VARCHAR(80) NOT NULL,
    academic_year INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL,
    enrolled_on DATE NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT uk_students_email UNIQUE (email),
    CONSTRAINT ck_academic_year CHECK (academic_year BETWEEN 1 AND 4),
    CONSTRAINT ck_student_status CHECK (status IN ('ACTIVE','ON_LEAVE','GRADUATED'))
);

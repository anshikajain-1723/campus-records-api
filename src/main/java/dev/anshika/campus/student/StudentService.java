package dev.anshika.campus.student;

import dev.anshika.campus.web.ApiException;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class StudentService {
    private final StudentRepository repository;
    public StudentService(StudentRepository repository) { this.repository = repository; }

    public Page<StudentResponse> list(String search, StudentStatus status, int page, int size) {
        String literal = search.strip().toLowerCase(Locale.ROOT)
                .replace("!", "!!").replace("%", "!%").replace("_", "!_");
        Specification<Student> filter = (root, query, cb) -> {
            var name = cb.like(cb.lower(root.get("fullName")), "%" + literal + "%", '!');
            var email = cb.like(root.get("email"), "%" + literal + "%", '!');
            var matches = cb.or(name, email);
            return status == null ? matches : cb.and(matches, cb.equal(root.get("status"), status));
        };
        return repository.findAll(filter, PageRequest.of(page, size, Sort.by("id").ascending()))
                .map(StudentResponse::from);
    }
    public StudentResponse get(long id) { return StudentResponse.from(find(id)); }
    @Transactional public StudentResponse create(StudentRequest request) {
        if (repository.existsByEmail(request.email())) throw duplicate();
        return StudentResponse.from(repository.saveAndFlush(new Student(request)));
    }
    @Transactional public StudentResponse update(long id, StudentRequest request) {
        Student student = find(id);
        if (repository.existsByEmailAndIdNot(request.email(), id)) throw duplicate();
        student.update(request);
        return StudentResponse.from(repository.saveAndFlush(student));
    }
    @Transactional public void delete(long id) { repository.delete(find(id)); }
    private Student find(long id) {
        return repository.findById(id).orElseThrow(() ->
                new ApiException(HttpStatus.NOT_FOUND, "Student not found"));
    }
    private ApiException duplicate() {
        return new ApiException(HttpStatus.CONFLICT, "A student with this email already exists");
    }
}

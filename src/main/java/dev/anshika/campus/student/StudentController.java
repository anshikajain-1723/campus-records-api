package dev.anshika.campus.student;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/students")
public class StudentController {
    private final StudentService service;
    public StudentController(StudentService service) { this.service = service; }
    public record StudentPage(List<StudentResponse> items, int page, int size, long totalElements, int totalPages) { }

    @GetMapping public StudentPage list(@RequestParam(defaultValue = "") @Size(max = 100) String search,
            @RequestParam(required = false) StudentStatus status,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {
        var result = service.list(search, status, page, size);
        return new StudentPage(result.getContent(), page, size, result.getTotalElements(), result.getTotalPages());
    }
    @GetMapping("/{id}") public StudentResponse get(@PathVariable @Positive long id) { return service.get(id); }
    @PostMapping public ResponseEntity<StudentResponse> create(@Valid @RequestBody StudentRequest request) {
        var created = service.create(request);
        return ResponseEntity.created(URI.create("/api/v1/students/" + created.id())).body(created);
    }
    @PutMapping("/{id}") public StudentResponse update(@PathVariable @Positive long id,
            @Valid @RequestBody StudentRequest request) { return service.update(id, request); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable @Positive long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

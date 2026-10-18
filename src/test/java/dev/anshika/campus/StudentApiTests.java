package dev.anshika.campus;

import com.jayway.jsonpath.JsonPath;
import dev.anshika.campus.student.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class StudentApiTests {
    @Autowired WebApplicationContext context;
    @Autowired StudentRepository repository;
    MockMvc mvc;

    @BeforeEach void setup() {
        repository.deleteAll();
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    private String payload(String name, String email, String status) {
        return """
                {"fullName":"%s","email":"%s","course":"Computer Science",
                 "academicYear":2,"status":"%s","enrolledOn":"2025-08-10"}
                """.formatted(name, email, status);
    }
    private long createStudent(String name, String email, String status) throws Exception {
        String response = mvc.perform(post("/api/v1/students").contentType(MediaType.APPLICATION_JSON)
                        .content(payload(name, email, status)))
                .andExpect(status().isCreated()).andExpect(header().exists("Location"))
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(response, "$.id")).longValue();
    }

    @Test void completeCrudLifecyclePersistsAndDeletes() throws Exception {
        long id = createStudent("Alex Morgan", "alex@example.com", "ACTIVE");
        assertThat(repository.count()).isEqualTo(1);
        mvc.perform(get("/api/v1/students/{id}", id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Alex Morgan"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
        mvc.perform(put("/api/v1/students/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content(payload("Alex Updated", "alex@example.com", "ON_LEAVE")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("ON_LEAVE"));
        assertThat(repository.findById(id).orElseThrow().getFullName()).isEqualTo("Alex Updated");
        mvc.perform(delete("/api/v1/students/{id}", id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/v1/students/{id}", id)).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Student not found"));
        assertThat(repository.count()).isZero();
    }

    @Test void normalizesEmailAndRejectsDuplicateWithoutAddingRecord() throws Exception {
        createStudent("Alex Morgan", " Alex@Example.com ", "ACTIVE");
        mvc.perform(post("/api/v1/students").contentType(MediaType.APPLICATION_JSON)
                        .content(payload("Other Student", "alex@example.com", "ACTIVE")))
                .andExpect(status().isConflict());
        assertThat(repository.count()).isEqualTo(1);
        assertThat(repository.findAll().getFirst().getEmail()).isEqualTo("alex@example.com");
    }

    @Test void conflictingUpdateLeavesBothStudentsUnchanged() throws Exception {
        createStudent("Alex Morgan", "alex@example.com", "ACTIVE");
        long second = createStudent("Jordan Lee", "jordan@example.com", "ACTIVE");
        mvc.perform(put("/api/v1/students/{id}", second).contentType(MediaType.APPLICATION_JSON)
                        .content(payload("Changed Name", "ALEX@example.com", "GRADUATED")))
                .andExpect(status().isConflict());
        assertThat(repository.findById(second).orElseThrow().getFullName()).isEqualTo("Jordan Lee");
    }

    @Test void invalidFieldsReturnFieldErrorsAndDoNotPersist() throws Exception {
        String invalid = """
                {"fullName":" ","email":"bad-email","course":"A",
                 "academicYear":8,"status":"ACTIVE","enrolledOn":"2999-01-01"}
                """;
        mvc.perform(post("/api/v1/students").contentType(MediaType.APPLICATION_JSON).content(invalid))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors.fullName").exists())
                .andExpect(jsonPath("$.errors.email").exists()).andExpect(jsonPath("$.errors.academicYear").exists())
                .andExpect(jsonPath("$.errors.enrolledOn").exists());
        assertThat(repository.count()).isZero();
    }

    @Test void combinesSearchStatusAndStablePagination() throws Exception {
        createStudent("Alex Morgan", "alex@example.com", "ACTIVE");
        createStudent("Alex Lee", "lee@example.com", "ON_LEAVE");
        createStudent("Alex Scott", "scott@example.com", "ACTIVE");
        mvc.perform(get("/api/v1/students").param("search", "ALEX").param("status", "ACTIVE")
                        .param("size", "1").param("page", "1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.items[0].fullName").value("Alex Scott"));
        mvc.perform(get("/api/v1/students").param("search", "%"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test void rejectsInvalidParametersAndMalformedJson() throws Exception {
        mvc.perform(get("/api/v1/students").param("page", "-1")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/students").param("size", "101")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/students").param("status", "UNKNOWN")).andExpect(status().isBadRequest());
        mvc.perform(post("/api/v1/students").contentType(MediaType.APPLICATION_JSON).content("{bad"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400));
    }

    @Test void unknownUpdateAndDeleteReturnNotFound() throws Exception {
        mvc.perform(put("/api/v1/students/999999").contentType(MediaType.APPLICATION_JSON)
                        .content(payload("Alex Morgan", "alex@example.com", "ACTIVE")))
                .andExpect(status().isNotFound());
        mvc.perform(delete("/api/v1/students/999999")).andExpect(status().isNotFound());
    }
}

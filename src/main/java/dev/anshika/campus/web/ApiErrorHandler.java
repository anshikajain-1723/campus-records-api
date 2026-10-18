package dev.anshika.campus.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import java.net.URI;
import java.util.LinkedHashMap;

@RestControllerAdvice
public class ApiErrorHandler {
    @ExceptionHandler(ApiException.class)
    ResponseEntity<ProblemDetail> business(ApiException error, HttpServletRequest request) {
        return problem(error.status(), error.getMessage(), request);
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ProblemDetail> validation(MethodArgumentNotValidException error, HttpServletRequest request) {
        var response = problem(HttpStatus.BAD_REQUEST, "Check the highlighted fields", request);
        var fields = new LinkedHashMap<String, String>();
        error.getBindingResult().getFieldErrors().forEach(e -> fields.putIfAbsent(e.getField(), e.getDefaultMessage()));
        response.getBody().setProperty("errors", fields);
        return response;
    }
    @ExceptionHandler({HandlerMethodValidationException.class, MethodArgumentTypeMismatchException.class})
    ResponseEntity<ProblemDetail> parameters(Exception error, HttpServletRequest request) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid path or query parameter", request);
    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ProblemDetail> json(Exception error, HttpServletRequest request) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid JSON, date or status value", request);
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ProblemDetail> conflict(Exception error, HttpServletRequest request) {
        return problem(HttpStatus.CONFLICT, "The record conflicts with an existing record or database constraint", request);
    }
    private ResponseEntity<ProblemDetail> problem(HttpStatus status, String detail, HttpServletRequest request) {
        var body = ProblemDetail.forStatusAndDetail(status, detail);
        body.setInstance(URI.create(request.getRequestURI()));
        return ResponseEntity.status(status).body(body);
    }
}

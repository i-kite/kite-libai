package com.kite.libai.web;

import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Spring 5.x has no built-in method validation for controller arguments, so the
 * {@code @Validated} proxy raises {@link ConstraintViolationException}, which would otherwise
 * surface as HTTP 500. Map it to 400 instead.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex) {
        String detail = ex.getConstraintViolations()
                .stream()
                .map(this::formatViolation)
                .sorted()
                .collect(Collectors.joining("; "));
        return ResponseEntity.badRequest().body(new ApiError(HttpStatus.BAD_REQUEST.value(), detail));
    }

    private String formatViolation(ConstraintViolation<?> violation) {
        return violation.getPropertyPath() + " " + violation.getMessage();
    }

    /** Minimal error body. */
    public static class ApiError {

        private final int status;

        private final String message;

        public ApiError(int status, String message) {
            this.status = status;
            this.message = message;
        }

        public int getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }
    }
}

package za.co.ixengineers.legal.common;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.http.converter
        .HttpMessageNotReadableException;

import org.springframework.web.bind
        .MethodArgumentNotValidException;

import org.springframework.web.bind.annotation
        .ExceptionHandler;

import org.springframework.web.bind.annotation
        .RestControllerAdvice;

import org.springframework.web.method.annotation
        .MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(
            MethodArgumentNotValidException.class
    )
    ResponseEntity<Map<String, Object>> validation(
            MethodArgumentNotValidException exception
    ) {
        Map<String, String> fields =
                new LinkedHashMap<>();

        exception
                .getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        fields.put(
                                error.getField(),
                                error.getDefaultMessage()
                        )
                );

        Map<String, Object> body =
                new LinkedHashMap<>();

        body.put("timestamp", Instant.now());
        body.put("status", 400);
        body.put("error", "Bad Request");
        body.put(
                "message",
                "Request validation failed"
        );
        body.put("fields", fields);

        return ResponseEntity
                .badRequest()
                .body(body);
    }

    @ExceptionHandler(
            HttpMessageNotReadableException.class
    )
    ResponseEntity<Map<String, Object>> unreadable(
            HttpMessageNotReadableException exception
    ) {
        return problem(
                HttpStatus.BAD_REQUEST,
                "The request contains invalid JSON, " +
                        "an invalid enum, or an invalid date."
        );
    }

    @ExceptionHandler(
            MethodArgumentTypeMismatchException.class
    )
    ResponseEntity<Map<String, Object>> typeMismatch(
            MethodArgumentTypeMismatchException exception
    ) {
        return problem(
                HttpStatus.BAD_REQUEST,
                "Invalid value for parameter: " +
                        exception.getName()
        );
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<Map<String, Object>> unexpected(
            Exception exception
    ) {
        /*
         * Log the complete exception server-side.
         * Do not expose internal class names or stack traces
         * to Angular.
         */
        exception.printStackTrace();

        return problem(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected server error occurred."
        );
    }

    private ResponseEntity<Map<String, Object>> problem(
            HttpStatus status,
            String message
    ) {
        Map<String, Object> body =
                new LinkedHashMap<>();

        body.put("timestamp", Instant.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);

        return ResponseEntity
                .status(status)
                .body(body);
    }
}
package ca.on.oicr.gsi.dimsum.controller;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ResponseStatusException;

@ControllerAdvice(basePackages = "ca.on.oicr.gsi.dimsum.controller.rest")
public class RestExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleResponseStatusException(
            ResponseStatusException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("status", ex.getStatus().toString());
        error.put("error", ex.getReason());

        return new ResponseEntity<>(error, ex.getStatus());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneralException(Exception ex) {
        Map<String, String> error = new HashMap<>();
        error.put("status", "500 INTERNAL_SERVER_ERROR");
        error.put("error", "Unexpected error");

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("/error-trigger")
    public void throwError() {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This is a triggered error");
    }
}

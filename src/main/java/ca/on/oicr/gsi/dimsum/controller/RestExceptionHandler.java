package ca.on.oicr.gsi.dimsum.controller;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

@ControllerAdvice(basePackages = "ca.on.oicr.gsi.dimsum.controller.rest")
public class RestExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(RestExceptionHandler.class);

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleResponseStatusException(
            ResponseStatusException ex) {
        logException(ex.getStatus().value(), ex.getReason(), ex);
        return prepareErrorResponse(ex.getStatus().value(), ex.getReason(), ex.getStatus());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneralException(Exception ex) {
        int status = HttpStatus.INTERNAL_SERVER_ERROR.value();
        String message = "Unexpected error";
        logException(status, message, ex);
        return prepareErrorResponse(status, message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private void logException(int status, String message, Exception ex) {
        if (status >= 500) {
            logger.error("Error Status: " + status + ". Error Message: " + message, ex);
        } else {
            logger.info("Error Status: " + status + ". Error Message: " + message);
        }
    }

    private ResponseEntity<Map<String, String>> prepareErrorResponse(int status,
            String errorMessage, HttpStatus httpStatus) {
        Map<String, String> error = new HashMap<>();
        error.put("status", String.valueOf(status));
        error.put("error", errorMessage);
        return new ResponseEntity<>(error, httpStatus);
    }
}

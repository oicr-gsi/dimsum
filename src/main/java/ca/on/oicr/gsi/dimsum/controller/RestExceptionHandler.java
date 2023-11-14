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
    public ResponseEntity<Map<String, Object>> handleResponseStatusException(
            ResponseStatusException ex) {
        return prepareErrorResponse(ex.getStatus(), ex.getReason(), ex);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(Exception ex) {
        return prepareErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", ex);
    }

    private ResponseEntity<Map<String, Object>> prepareErrorResponse(HttpStatus httpStatus,
            String errorMessage, Exception ex) {
        logException(httpStatus.value(), errorMessage, ex);

        Map<String, Object> error = new HashMap<>();
        error.put("status", httpStatus.value());
        error.put("error", httpStatus.getReasonPhrase());
        error.put("message", errorMessage);

        return new ResponseEntity<>(error, httpStatus);
    }

    private void logException(int status, String message, Exception ex) {
        if (status >= 500) {
            logger.error("Error Status: {}. Error Message: {}", status, message, ex);
        } else {
            logger.warn("Error Status: {}. Error Message: {}", status, message);
        }
    }
}

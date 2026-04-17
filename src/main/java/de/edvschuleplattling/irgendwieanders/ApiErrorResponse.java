package de.edvschuleplattling.irgendwieanders;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ApiErrorResponse {

    private ApiErrorResponse() {
    }

    public static Map<String, Object> body(HttpStatus status, String message, String path) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", OffsetDateTime.now().toString());
        body.put("code", status.name());
        body.put("message", message);
        body.put("path", path);
        return body;
    }

    public static ResponseEntity<Map<String, Object>> entity(HttpStatus status, String message, HttpServletRequest request) {
        return ResponseEntity.status(status).body(body(status, message, request.getRequestURI()));
    }
}

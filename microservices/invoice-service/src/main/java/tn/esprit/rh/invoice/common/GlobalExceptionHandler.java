package tn.esprit.rh.invoice.common;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ApiException.class)
	public ResponseEntity<Map<String, Object>> handle(ApiException e, HttpServletRequest request) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("timestamp", Instant.now().toString());
		body.put("status", e.getStatus());
		body.put("code", e.getCode());
		body.put("message", e.getMessage());
		body.put("path", request.getRequestURI());
		return ResponseEntity.status(e.getStatus()).body(body);
	}
}

package tn.esprit.rh.auth.common;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ApiException.class)
	public ResponseEntity<Map<String, Object>> handle(ApiException e, HttpServletRequest request) {
		return ResponseEntity.status(e.getStatus()).body(body(e.getStatus(), e.getCode(), e.getMessage(), request, null));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, Object>> handle(MethodArgumentNotValidException e, HttpServletRequest request) {
		Map<String, String> fields = new LinkedHashMap<>();
		e.getBindingResult().getFieldErrors().forEach(fe -> fields.put(fe.getField(), fe.getDefaultMessage()));
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(body(400, "validation_failed", "Certains champs sont invalides.", request, fields));
	}

	private Map<String, Object> body(int status, String code, String message, HttpServletRequest request,
			Map<String, String> fields) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("timestamp", Instant.now().toString());
		body.put("status", status);
		body.put("code", code);
		body.put("message", message);
		body.put("path", request.getRequestURI());
		if (fields != null) {
			body.put("fields", fields);
		}
		return body;
	}
}

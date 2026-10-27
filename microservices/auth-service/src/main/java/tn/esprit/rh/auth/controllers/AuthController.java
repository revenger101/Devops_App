package tn.esprit.rh.auth.controllers;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import tn.esprit.rh.auth.dto.AuthDtos.LoginRequest;
import tn.esprit.rh.auth.dto.AuthDtos.RegisterRequest;
import tn.esprit.rh.auth.dto.AuthDtos.TokenResponse;
import tn.esprit.rh.auth.services.AuthService;

@RestController
@Tag(name = "Authentification")
@RequestMapping("/auth")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	// http://localhost:8080/auth/register
	@PostMapping("/register")
	@ResponseStatus(HttpStatus.CREATED)
	public TokenResponse register(@Valid @RequestBody RegisterRequest request) {
		return authService.register(request);
	}

	// http://localhost:8080/auth/login
	@PostMapping("/login")
	public TokenResponse login(@Valid @RequestBody LoginRequest request) {
		return authService.login(request);
	}
}

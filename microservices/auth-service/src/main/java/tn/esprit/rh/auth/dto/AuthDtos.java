package tn.esprit.rh.auth.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public final class AuthDtos {

	private AuthDtos() {
	}

	@Getter
	@Setter
	@NoArgsConstructor
	public static class RegisterRequest {
		@NotBlank
		@Size(min = 3, max = 50)
		private String username;

		@NotBlank
		@Size(min = 8, max = 100, message = "must contain at least 8 characters")
		private String password;
	}

	@Getter
	@Setter
	@NoArgsConstructor
	public static class LoginRequest {
		@NotBlank
		private String username;

		@NotBlank
		private String password;
	}

	@Getter
	@AllArgsConstructor
	public static class TokenResponse {
		private final String token;
		private final String tokenType;
		private final long expiresInSeconds;
		private final String username;
		private final String role;
	}
}

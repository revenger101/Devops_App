package tn.esprit.rh.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import tn.esprit.rh.auth.common.ApiException;
import tn.esprit.rh.auth.dto.AuthDtos.LoginRequest;
import tn.esprit.rh.auth.dto.AuthDtos.RegisterRequest;
import tn.esprit.rh.auth.dto.AuthDtos.TokenResponse;
import tn.esprit.rh.auth.entities.Role;
import tn.esprit.rh.auth.entities.User;
import tn.esprit.rh.auth.repositories.UserRepository;
import tn.esprit.rh.auth.services.AuthService;
import tn.esprit.rh.auth.services.JwtService;

class AuthServiceTest {

	private static final String SECRET = "unit-test-secret-unit-test-secret-1234";

	private final UserRepository users = mock(UserRepository.class);
	private final PasswordEncoder encoder = new BCryptPasswordEncoder();
	private final JwtService jwt = new JwtService(SECRET, 5);
	private final AuthService service = new AuthService(users, encoder, jwt);

	private RegisterRequest register(String username, String password) {
		RegisterRequest r = new RegisterRequest();
		r.setUsername(username);
		r.setPassword(password);
		return r;
	}

	private LoginRequest login(String username, String password) {
		LoginRequest r = new LoginRequest();
		r.setUsername(username);
		r.setPassword(password);
		return r;
	}

	@Test
	void registerHashesPasswordAndAlwaysCreatesPlainUser() {
		when(users.existsByUsername("alice")).thenReturn(false);
		when(users.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

		TokenResponse response = service.register(register("alice", "s3cret-pass"));

		assertEquals("USER", response.getRole());
		assertEquals("Bearer", response.getTokenType());
		org.mockito.ArgumentCaptor<User> saved = org.mockito.ArgumentCaptor.forClass(User.class);
		verify(users).save(saved.capture());
		assertNotEquals("s3cret-pass", saved.getValue().getPassword());
		assertEquals(Role.USER, saved.getValue().getRole());
	}

	@Test
	void registerRejectsDuplicateUsername() {
		when(users.existsByUsername("alice")).thenReturn(true);

		ApiException e = assertThrows(ApiException.class, () -> service.register(register("alice", "s3cret-pass")));

		assertEquals(409, e.getStatus());
		verify(users, never()).save(any());
	}

	@Test
	void loginReturnsParsableTokenCarryingTheRole() {
		User admin = new User("root", encoder.encode("s3cret-pass"), Role.ADMIN);
		when(users.findByUsername("root")).thenReturn(Optional.of(admin));

		TokenResponse response = service.login(login("root", "s3cret-pass"));

		assertEquals("root", jwt.parse(response.getToken()).getSubject());
		assertEquals("ADMIN", jwt.parse(response.getToken()).get("role"));
	}

	@Test
	void wrongPasswordAndUnknownUserGiveTheSameError() {
		User user = new User("bob", encoder.encode("right-password"), Role.USER);
		when(users.findByUsername("bob")).thenReturn(Optional.of(user));
		when(users.findByUsername("ghost")).thenReturn(Optional.empty());

		ApiException wrong = assertThrows(ApiException.class, () -> service.login(login("bob", "bad-password")));
		ApiException unknown = assertThrows(ApiException.class, () -> service.login(login("ghost", "whatever1")));

		assertEquals(401, wrong.getStatus());
		assertEquals(wrong.getCode(), unknown.getCode());
		assertEquals(wrong.getMessage(), unknown.getMessage());
	}

	@Test
	void shortSecretIsRefused() {
		assertThrows(IllegalStateException.class, () -> new JwtService("too-short", 5));
	}
}

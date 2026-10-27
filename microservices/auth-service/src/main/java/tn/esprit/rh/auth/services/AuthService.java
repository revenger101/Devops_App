package tn.esprit.rh.auth.services;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tn.esprit.rh.auth.common.ApiException;
import tn.esprit.rh.auth.dto.AuthDtos.LoginRequest;
import tn.esprit.rh.auth.dto.AuthDtos.RegisterRequest;
import tn.esprit.rh.auth.dto.AuthDtos.TokenResponse;
import tn.esprit.rh.auth.entities.Role;
import tn.esprit.rh.auth.entities.User;
import tn.esprit.rh.auth.repositories.UserRepository;

@Service
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
	}

	@Transactional
	public TokenResponse register(RegisterRequest request) {
		if (userRepository.existsByUsername(request.getUsername())) {
			throw new ApiException(409, "username_taken", "Ce nom d utilisateur est deja pris.");
		}
		// self-registration only ever creates plain users; admins are bootstrapped separately
		User user = userRepository.save(
				new User(request.getUsername(), passwordEncoder.encode(request.getPassword()), Role.USER));
		return toResponse(user);
	}

	@Transactional(readOnly = true)
	public TokenResponse login(LoginRequest request) {
		User user = userRepository.findByUsername(request.getUsername())
				.filter(u -> passwordEncoder.matches(request.getPassword(), u.getPassword()))
				// same error for unknown user and wrong password: do not leak which usernames exist
				.orElseThrow(() -> new ApiException(401, "bad_credentials", "Nom d utilisateur ou mot de passe incorrect."));
		return toResponse(user);
	}

	private TokenResponse toResponse(User user) {
		return new TokenResponse(jwtService.generate(user), "Bearer", jwtService.expiresInSeconds(),
				user.getUsername(), user.getRole().name());
	}
}

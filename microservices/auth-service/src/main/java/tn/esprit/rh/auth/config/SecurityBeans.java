package tn.esprit.rh.auth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tn.esprit.rh.auth.entities.Role;
import tn.esprit.rh.auth.entities.User;
import tn.esprit.rh.auth.repositories.UserRepository;

@Configuration
public class SecurityBeans {

	private static final Logger log = LoggerFactory.getLogger(SecurityBeans.class);

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	/** Creates the first admin from environment variables; does nothing when no password is configured. */
	@Bean
	CommandLineRunner adminBootstrap(UserRepository users, PasswordEncoder encoder,
			@Value("${auth.admin.username:admin}") String username,
			@Value("${auth.admin.password:}") String password) {
		return args -> {
			if (password.isEmpty() || users.existsByUsername(username)) {
				return;
			}
			users.save(new User(username, encoder.encode(password), Role.ADMIN));
			log.info("Bootstrap admin '{}' created", username);
		};
	}
}

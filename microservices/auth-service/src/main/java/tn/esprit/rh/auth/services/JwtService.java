package tn.esprit.rh.auth.services;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import tn.esprit.rh.auth.entities.User;

@Service
public class JwtService {

	private final SecretKey key;
	private final long expirationMinutes;

	public JwtService(@Value("${jwt.secret}") String secret,
			@Value("${jwt.expiration-minutes:60}") long expirationMinutes) {
		byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
		if (bytes.length < 32) {
			throw new IllegalStateException("jwt.secret must contain at least 32 characters");
		}
		this.key = Keys.hmacShaKeyFor(bytes);
		this.expirationMinutes = expirationMinutes;
	}

	public String generate(User user) {
		Date now = new Date();
		return Jwts.builder()
				.setSubject(user.getUsername())
				.claim("role", user.getRole().name())
				.setIssuedAt(now)
				.setExpiration(new Date(now.getTime() + expirationMinutes * 60_000))
				.signWith(key, SignatureAlgorithm.HS256)
				.compact();
	}

	public Claims parse(String token) {
		return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
	}

	public long expiresInSeconds() {
		return expirationMinutes * 60;
	}
}

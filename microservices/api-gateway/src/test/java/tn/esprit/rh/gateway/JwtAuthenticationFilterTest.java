package tn.esprit.rh.gateway;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class JwtAuthenticationFilterTest {

	private static final String SECRET = "test-secret-test-secret-test-secret-123456";

	private final JwtAuthenticationFilter filter = new JwtAuthenticationFilter(SECRET);

	private String token(String user, String role) {
		return Jwts.builder().setSubject(user).claim("role", role)
				.setExpiration(new Date(System.currentTimeMillis() + 60_000))
				.signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
				.compact();
	}

	@Test
	void rejectsRequestWithoutToken() {
		MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/produit/retrieve-all-produits"));
		filter.filter(exchange, e -> Mono.empty()).block();
		assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
	}

	@Test
	void rejectsForgedToken() {
		MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/produit/retrieve-all-produits")
				.header(HttpHeaders.AUTHORIZATION, "Bearer not-a-jwt"));
		filter.filter(exchange, e -> Mono.empty()).block();
		assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
	}

	@Test
	void letsAuthEndpointsThrough() {
		MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.post("/auth/login"));
		AtomicReference<Boolean> called = new AtomicReference<>(false);
		GatewayFilterChain chain = e -> {
			called.set(true);
			return Mono.empty();
		};
		filter.filter(exchange, chain).block();
		assertEquals(true, called.get());
	}

	@Test
	void forwardsIdentityHeadersAndStripsSpoofedOnes() {
		MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/stock/retrieve-all-stocks")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + token("alice", "ADMIN"))
				.header("X-Auth-User", "mallory"));
		AtomicReference<HttpHeaders> seen = new AtomicReference<>();
		filter.filter(exchange, e -> {
			seen.set(e.getRequest().getHeaders());
			return Mono.empty();
		}).block();
		assertNotNull(seen.get());
		assertEquals("alice", seen.get().getFirst("X-Auth-User"));
		assertEquals("ADMIN", seen.get().getFirst("X-Auth-Role"));
		assertNull(exchange.getResponse().getStatusCode());
	}
}

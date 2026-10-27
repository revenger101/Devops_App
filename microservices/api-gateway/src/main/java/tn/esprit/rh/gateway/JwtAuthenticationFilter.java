package tn.esprit.rh.gateway;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * Single place where JWTs are validated. Downstream services are not reachable from outside
 * the docker network and trust the X-Auth-* headers set here.
 */
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

	static final String USER_HEADER = "X-Auth-User";
	static final String ROLE_HEADER = "X-Auth-Role";

	private final SecretKey key;

	public JwtAuthenticationFilter(@Value("${jwt.secret}") String secret) {
		this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		ServerHttpRequest request = exchange.getRequest();
		String path = request.getURI().getPath();

		if (isPublic(path) || request.getMethod() == HttpMethod.OPTIONS) {
			return chain.filter(exchange);
		}

		String header = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
		if (header == null || !header.startsWith("Bearer ")) {
			return unauthorized(exchange, "missing_token",
					"Authentication required: send an Authorization header with a Bearer token.");
		}

		try {
			Claims claims = Jwts.parserBuilder().setSigningKey(key).build()
					.parseClaimsJws(header.substring(7)).getBody();
			ServerHttpRequest authenticated = request.mutate()
					// never trust identity headers coming from the outside
					.headers(h -> {
						h.remove(USER_HEADER);
						h.remove(ROLE_HEADER);
					})
					.header(USER_HEADER, claims.getSubject())
					.header(ROLE_HEADER, String.valueOf(claims.get("role")))
					.build();
			return chain.filter(exchange.mutate().request(authenticated).build());
		} catch (JwtException | IllegalArgumentException e) {
			return unauthorized(exchange, "invalid_token", "The token is invalid or expired. Log in again.");
		}
	}

	private boolean isPublic(String path) {
		return path.startsWith("/auth/") || path.startsWith("/actuator/health");
	}

	private Mono<Void> unauthorized(ServerWebExchange exchange, String code, String message) {
		ServerHttpResponse response = exchange.getResponse();
		response.setStatusCode(HttpStatus.UNAUTHORIZED);
		response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
		byte[] body = ("{\"status\":401,\"code\":\"" + code + "\",\"message\":\"" + message + "\"}")
				.getBytes(StandardCharsets.UTF_8);
		DataBuffer buffer = response.bufferFactory().wrap(body);
		return response.writeWith(Mono.just(buffer));
	}

	@Override
	public int getOrder() {
		return -1;
	}
}

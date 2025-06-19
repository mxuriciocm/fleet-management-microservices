package com.gateway.service.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * JWT Authentication Filter for API Gateway
 * This filter intercepts incoming requests to validate JWT tokens
 */
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Value("${authorization.jwt.secret}")
    private String jwtSecret;

    @Value("${authorization.routes.public-paths:/api/v1/authentication/**,/actuator/**,/v3/api-docs/**,/swagger-ui/**}")
    private List<String> publicPaths;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        log.debug("Processing request for path: {}", path);

        if (isPublicPath(path)) {
            log.debug("Path {} is public, skipping authentication", path);
            return chain.filter(exchange);
        }

        log.debug("Path {} requires authentication", path);
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null) {
            log.warn("No Authorization header found in request");
            return onError(exchange, "Missing Authorization header", HttpStatus.UNAUTHORIZED);
        }

        if (!authHeader.startsWith("Bearer ")) {
            log.warn("Authorization header does not start with 'Bearer '");
            return onError(exchange, "Invalid Authorization header format", HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);
        log.debug("Extracted token: {}", token.substring(0, Math.min(10, token.length())) + "...");

        try {
            Claims claims = validateToken(token);

            String userEmail = claims.getSubject();
            log.debug("Token subject (email): {}", userEmail);

            List<String> roles = getRolesFromClaims(claims);
            log.debug("Extracted roles: {}", roles);

            String rolesString = String.join(",", roles);

            String userId;
            if (claims.get("userId") != null) {
                userId = claims.get("userId").toString();
                log.debug("Using userId from token claim: {}", userId);
            } else {
                userId = String.valueOf(claims.get("id", Long.class));
                if (userId.equals("null")) {
                    log.debug("No userId/id found in token, using email as identifier: {}", userEmail);
                    userId = userEmail;
                } else {
                    log.debug("Using id from token claim: {}", userId);
                }
            }

            ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-User-Id", userId)
                .header("X-User-Email", userEmail)
                .header("X-User-Roles", rolesString)
                .build();

            log.debug("Added headers - X-User-Id: {}, X-User-Email: {}, X-User-Roles: {}",
                      userId, userEmail, rolesString);

            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
            return onError(exchange, "Expired JWT token", HttpStatus.UNAUTHORIZED);
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
            return onError(exchange, "Unsupported JWT token", HttpStatus.UNAUTHORIZED);
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return onError(exchange, "Invalid JWT token", HttpStatus.UNAUTHORIZED);
        } catch (SignatureException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
            return onError(exchange, "Invalid JWT signature", HttpStatus.UNAUTHORIZED);
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
            return onError(exchange, "JWT claims string is empty", HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            log.error("Could not validate JWT token: {}", e.getMessage(), e);
            return onError(exchange, "Could not validate token: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

    private boolean isPublicPath(String path) {
        boolean isPublic = publicPaths.stream().anyMatch(pattern -> {
            if (pattern.endsWith("/**")) {
                String prefix = pattern.substring(0, pattern.length() - 3);
                return path.startsWith(prefix);
            }
            return path.equals(pattern);
        });

        log.debug("Path {} is public: {}", path, isPublic);
        return isPublic;
    }

    private Claims validateToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        log.debug("Validating token with secret key of length: {}", jwtSecret.length());

        try {
            return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            throw e;
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> getRolesFromClaims(Claims claims) {
        try {
            List<String> roles = claims.get("roles", List.class);
            if (roles == null || roles.isEmpty()) {
                log.warn("No roles found in token claims");
                return List.of();
            }
            return roles;
        } catch (Exception e) {
            log.error("Error extracting roles from claims: {}", e.getMessage());
            return List.of();
        }
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        log.warn("Authentication error: {} - Status: {}", message, status);
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().add("WWW-Authenticate", "Bearer");
        return response.setComplete();
    }

    @Override
    public int getOrder() {
        return -200;
    }
}

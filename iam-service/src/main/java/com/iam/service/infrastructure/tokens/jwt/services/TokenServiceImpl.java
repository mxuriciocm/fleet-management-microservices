package com.iam.service.infrastructure.tokens.jwt.services;

import com.iam.service.domain.model.aggregates.User;
import com.iam.service.infrastructure.persistence.jpa.repositories.UserRepository;
import com.iam.service.infrastructure.tokens.jwt.BearerTokenService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * JWT Token Service Implementation.
 * This class is responsible for generating JWT tokens with user roles included.
 */
@Service
public class TokenServiceImpl implements BearerTokenService {
    private final Logger LOGGER = LoggerFactory.getLogger(TokenServiceImpl.class);

    @Value("${authorization.jwt.secret}")
    private String secret;

    @Value(("${authorization.jwt.expiration.days}"))
    private int expirationDays;

    private final UserRepository userRepository;

    public TokenServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Get the signing key.
     *
     * @return the {@link SecretKey} signing key
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Build the token with default parameters and include user roles.
     *
     * @param username the username
     * @return the token
     */
    private String buildTokenWithDefaultParameters(String username) {
        var issuedAt = new Date();
        var expiration = DateUtils.addDays(issuedAt, expirationDays);
        var key = getSigningKey();

        var userOptional = userRepository.findByEmail(username);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            List<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList());

            return Jwts.builder()
                    .subject(username)
                    .issuedAt(issuedAt)
                    .expiration(expiration)
                    .claim("id", user.getId())
                    .claim("roles", roles)
                    .signWith(key)
                    .compact();
        }

        return Jwts.builder()
                .subject(username)
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(key)
                .compact();
    }

    /**
     * Extract all claims from the token.
     *
     * @param token the token
     * @return the {@link Claims} claims
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Extract the claim from the token.
     *
     * @param token          the token
     * @param claimResolvers the {@link Function} claim resolvers
     * @param <T>            the type of the claim
     * @return the claim
     */
    private <T> T extractClaim(String token, Function<Claims, T> claimResolvers) {
        return claimResolvers.apply(extractAllClaims(token));
    }

    /**
     * Generate the bearer token.
     *
     * @param authentication the {@link Authentication} authentication
     * @return the bearer token
     */
    @Override
    public String generateToken(Authentication authentication) {
        return buildTokenWithDefaultParameters(authentication.getName());
    }

    /**
     * Generate a token for the given username.
     *
     * @param username the username
     * @return the generated token
     */
    @Override
    public String generateToken(String username) {
        return buildTokenWithDefaultParameters(username);
    }

    /**
     * Extract the username from the token.
     *
     * @param token the token
     * @return the username
     */
    @Override
    public String getUsernameFromToken(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Validate the token.
     *
     * @param token the token
     * @return true if the token is valid, false otherwise
     */
    @Override
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            LOGGER.info("Token is valid");
            return true;
        } catch (SignatureException e) {
            LOGGER.error("Invalid token signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            LOGGER.error("Invalid token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            LOGGER.error("Token has expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            LOGGER.error("Unsupported token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            LOGGER.error("Token is empty: {}", e.getMessage());
        }
        return false;
    }
}

package com.iam.service.application.internal.outboundservices.tokens;

/**
 * TokenService
 * <p>
 *     Interface for token service. This service is used to generate, validate and extract username from tokens.
 * </p>
 */
public interface TokenService {

    /**
     * Generate a token for the given username.
     *
     * @param username the username
     * @return the generated token
     */
    String generateToken(String username);

    /**
     * Extract the username from the token.
     *
     * @param token the token
     * @return the username
     */
    String getUsernameFromToken(String token);

    /**
     * Validate the token.
     *
     * @param token the token
     * @return true if the token is valid, false otherwise
     */
    boolean validateToken(String token);
}

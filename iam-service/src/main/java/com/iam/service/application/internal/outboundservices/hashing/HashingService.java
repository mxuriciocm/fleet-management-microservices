package com.iam.service.application.internal.outboundservices.hashing;

/**
 * HashingService
 * <p>
 *     Interface for hashing service. This service is used to encode and match passwords.
 * </p>
 */
public interface HashingService {
    /**
     * Encode the raw password.
     *
     * @param rawPassword the raw password
     * @return the encoded password
     */
    String encode(CharSequence rawPassword);

    /**
     * Matches the raw password with the encoded password.
     *
     * @param rawPassword the raw password
     * @param encodedPassword the encoded password
     * @return true if the raw password matches the encoded password, false otherwise
     */
    boolean matches(CharSequence rawPassword, String encodedPassword);
}

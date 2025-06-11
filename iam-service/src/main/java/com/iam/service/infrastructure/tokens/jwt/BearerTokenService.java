package com.iam.service.infrastructure.tokens.jwt;

import com.iam.service.application.internal.outboundservices.tokens.TokenService;
import org.springframework.security.core.Authentication;

public interface BearerTokenService extends TokenService {
    /**
     * Generate the bearer token.
     *
     * @param authentication the {@link Authentication} authentication
     * @return the bearer token
     */
    String generateToken(Authentication authentication);
}

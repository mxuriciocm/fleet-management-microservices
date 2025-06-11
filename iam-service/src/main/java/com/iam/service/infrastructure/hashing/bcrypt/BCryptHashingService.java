package com.iam.service.infrastructure.hashing.bcrypt;

import com.iam.service.application.internal.outboundservices.hashing.HashingService;
import org.springframework.security.crypto.password.PasswordEncoder;

public interface BCryptHashingService extends HashingService, PasswordEncoder {
}

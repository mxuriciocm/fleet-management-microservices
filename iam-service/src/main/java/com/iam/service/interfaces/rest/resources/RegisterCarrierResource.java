package com.iam.service.interfaces.rest.resources;

import java.util.List;

/**
 * Resource for registering a new carrier by a manager.
 */
public record RegisterCarrierResource(String email, String password, List<String> roles) {
}

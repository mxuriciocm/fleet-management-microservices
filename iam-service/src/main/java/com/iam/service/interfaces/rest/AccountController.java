package com.iam.service.interfaces.rest;

import com.iam.service.application.internal.outboundservices.tokens.TokenService;
import com.iam.service.domain.services.UserCommandService;
import com.iam.service.interfaces.rest.resources.AuthenticatedUserResource;
import com.iam.service.interfaces.rest.resources.ChangeEmailResource;
import com.iam.service.interfaces.rest.resources.ChangePasswordResource;
import com.iam.service.interfaces.rest.resources.UserResource;
import com.iam.service.interfaces.rest.transform.AuthenticatedUserResourceFromEntityAssembler;
import com.iam.service.interfaces.rest.transform.ChangeEmailCommandFromResourceAssembler;
import com.iam.service.interfaces.rest.transform.ChangePasswordCommandFromResourceAssembler;
import com.iam.service.interfaces.rest.transform.UserResourceFromEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller to handle user account management operations.
 */
@RestController
@RequestMapping(value = "/api/v1/account", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "User Account", description = "User Account Management Endpoints")
public class AccountController {
    private final UserCommandService userCommandService;
    private final TokenService tokenService;

    public AccountController(UserCommandService userCommandService, TokenService tokenService) {
        this.userCommandService = userCommandService;
        this.tokenService = tokenService;
    }

    /**
     * Change user password.
     *
     * @param userId ID of the user whose password is being changed
     * @param resource The change password request
     * @return The updated user resource
     */
    @PutMapping(value = "/{userId}/password", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Change user password", description = "Change the password of a user with the given ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Password changed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid password data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PreAuthorize("authentication.principal.email == @userRepository.findById(#userId).get().email")
    public ResponseEntity<UserResource> changePassword(
            @PathVariable Long userId,
            @RequestBody ChangePasswordResource resource) {
        try {
            var command = ChangePasswordCommandFromResourceAssembler.toCommandFromResource(resource, userId);
            var user = userCommandService.handle(command);

            if (user.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            var userResource = UserResourceFromEntityAssembler.toResourceFromEntity(user.get());
            return ResponseEntity.ok(userResource);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Change user email.
     *
     * @param userId ID of the user whose email is being changed
     * @param resource The change email request
     * @return The updated user resource with a new token
     */
    @PutMapping(value = "/{userId}/email", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Change user email", description = "Change the email of a user with the given ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Email changed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid email data or email already exists"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PreAuthorize("authentication.principal.email == @userRepository.findById(#userId).get().email")
    public ResponseEntity<AuthenticatedUserResource> changeEmail(
            @PathVariable Long userId,
            @RequestBody ChangeEmailResource resource) {
        try {
            var command = ChangeEmailCommandFromResourceAssembler.toCommandFromResource(resource, userId);
            var user = userCommandService.handle(command);

            if (user.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            var newToken = tokenService.generateToken(user.get().getEmail());

            var authenticatedUserResource = AuthenticatedUserResourceFromEntityAssembler
                .toResourceFromEntity(user.get(), newToken);

            return ResponseEntity.ok(authenticatedUserResource);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}

package com.iam.service.interfaces.rest;

import com.iam.service.domain.services.UserCommandService;
import com.iam.service.interfaces.rest.resources.RegisterCarrierResource;
import com.iam.service.interfaces.rest.resources.UserResource;
import com.iam.service.interfaces.rest.transform.RegisterCarrierCommandFromResourceAssembler;
import com.iam.service.interfaces.rest.transform.UserResourceFromEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller for operations related to carriers in the system.
 */
@RestController
@RequestMapping(value = "/api/v1/carriers", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Carriers", description = "Carrier Management Endpoints")
public class CarriersController {

    private final UserCommandService userCommandService;

    public CarriersController(UserCommandService userCommandService) {
        this.userCommandService = userCommandService;
    }

    /**
     * Register a new carrier by a manager.
     *
     * @param resource The carrier data
     * @param request The HTTP request containing X-User-Id header with the manager's ID
     * @return ResponseEntity with the created carrier user or an error
     */
    @PostMapping(value = "/sign-up", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Register a new carrier", description = "Allows a manager to register a new carrier user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Carrier registered successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request - Invalid data or email already exists"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Not logged in"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Not a manager")
    })
    public ResponseEntity<?> registerCarrier(
            @RequestBody RegisterCarrierResource resource,
            HttpServletRequest request) {

        // Get manager ID from header set by gateway
        String userIdHeader = request.getHeader("X-User-Id");
        if (userIdHeader == null || userIdHeader.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "message", "Unauthorized",
                            "error", "User not authenticated",
                            "errorCode", "NOT_AUTHENTICATED"
                    ));
        }

        Long managerId;
        try {
            managerId = Long.valueOf(userIdHeader);
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "message", "Bad request",
                            "error", "Invalid user ID",
                            "errorCode", "INVALID_USER_ID"
                    ));
        }

        // Verify user has manager role
        String rolesHeader = request.getHeader("X-User-Roles");
        if (rolesHeader == null || !rolesHeader.contains("MANAGER")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "message", "Access denied",
                            "error", "Manager role required for this operation",
                            "errorCode", "REQUIRES_MANAGER_ROLE"
                    ));
        }

        try {
            var command = RegisterCarrierCommandFromResourceAssembler.toCommandFromResource(resource, managerId);
            var carrier = userCommandService.handle(command);

            if (carrier.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "message", "Error registering carrier",
                                "error", "Invalid data",
                                "errorCode", "INVALID_DATA"
                        ));
            }

            var carrierResource = UserResourceFromEntityAssembler.toResourceFromEntity(carrier.get());
            return ResponseEntity.status(HttpStatus.CREATED).body(carrierResource);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Email already exists")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of(
                                "message", "Failed to register carrier",
                                "error", "Email already exists",
                                "errorCode", "EMAIL_EXISTS"
                        ));
            } else if (e.getMessage().contains("Role name not found")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of(
                                "message", "Error registering carrier",
                                "error", "Role not found",
                                "errorCode", "ROLE_NOT_FOUND"
                        ));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of(
                                "message", "Error registering carrier",
                                "error", e.getMessage(),
                                "errorCode", "REGISTRATION_ERROR"
                        ));
            }
        }
    }
}

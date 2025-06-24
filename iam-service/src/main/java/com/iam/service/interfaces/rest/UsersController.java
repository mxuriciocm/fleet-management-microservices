package com.iam.service.interfaces.rest;

import com.iam.service.domain.model.queries.GetAllUsersQuery;
import com.iam.service.domain.model.queries.GetUserByIdQuery;
import com.iam.service.domain.model.queries.GetCarriersByManagerQuery;
import com.iam.service.domain.services.UserCommandService;
import com.iam.service.domain.services.UserQueryService;
import com.iam.service.interfaces.rest.resources.UserResource;
import com.iam.service.interfaces.rest.transform.UserResourceFromEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller to handle user endpoints.
 * <p>
 *     This class is used to handle user endpoints.
 * </p>
 */
@RestController
@RequestMapping(value = "/api/v1/users", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Users", description = "Available User Endpoints")
public class UsersController {
    private final UserQueryService userQueryService;
    private final UserCommandService userCommandService;

    /**
     * Constructor.
     *
     * @param userQueryService The user query service.
     * @param userCommandService The user command service.
     */
    public UsersController(UserQueryService userQueryService, UserCommandService userCommandService) {
        this.userQueryService = userQueryService;
        this.userCommandService = userCommandService;
    }

    /**
     * Get all users.
     *
     * @return The list of users.
     */
    @GetMapping
    @Operation(summary = "Get all users", description = "Get all the users available in the system.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully."),
            @ApiResponse(responseCode = "401", description = "Unauthorized.")})
    public ResponseEntity<List<UserResource>> getAllUsers() {
        var getAllUsersQuery = new GetAllUsersQuery();
        var users = userQueryService.handle(getAllUsersQuery);
        var userResources = users.stream().map(UserResourceFromEntityAssembler::toResourceFromEntity).toList();
        return ResponseEntity.ok(userResources);
    }

    /**
     * Get user by id.
     *
     * @param userId The id of the user to retrieve.
     * @return The user.
     */
    @GetMapping(value = "/{userId}")
    @Operation(summary = "Get user by id", description = "Get the user with the given id.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User retrieved successfully."),
            @ApiResponse(responseCode = "404", description = "User not found."),
            @ApiResponse(responseCode = "401", description = "Unauthorized.")})
    public ResponseEntity<UserResource> getUserById(@PathVariable Long userId) {
        var getUserByIdQuery = new GetUserByIdQuery(userId);
        var user = userQueryService.handle(getUserByIdQuery);
        if (user.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var userResource = UserResourceFromEntityAssembler.toResourceFromEntity(user.get());
        return ResponseEntity.ok(userResource);
    }

    /**
     * Get all carriers for a manager.
     *
     * @param managerId The id of the manager to get carriers for.
     * @return The list of carriers.
     */
    @GetMapping(value = "/managers/{managerId}/carriers")
    @Operation(summary = "Get all carriers for a manager", description = "Get all carriers created by or associated with the specified manager.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Carriers retrieved successfully."),
            @ApiResponse(responseCode = "401", description = "Unauthorized.")})
    public ResponseEntity<List<UserResource>> getCarriersByManager(@PathVariable Long managerId) {
        var getCarriersByManagerQuery = new GetCarriersByManagerQuery(managerId);
        var carriers = userQueryService.handle(getCarriersByManagerQuery);
        var carrierResources = carriers.stream()
                .map(UserResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(carrierResources);
    }

    /**
     * Delete user by id.
     *
     * @param userId The id of the user to delete.
     * @return No content on successful deletion.
     */
    @DeleteMapping(value = "/{userId}")
    @Operation(summary = "Delete user by id", description = "Delete the user with the given id.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User deleted successfully."),
            @ApiResponse(responseCode = "404", description = "User not found."),
            @ApiResponse(responseCode = "400", description = "Failed to delete user."),
            @ApiResponse(responseCode = "401", description = "Unauthorized.")})
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        try {
            boolean deleted = userCommandService.deleteUser(userId);
            if (deleted) {
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}

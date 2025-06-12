package com.profiles.service.interfaces.rest;

import com.profiles.service.domain.model.queries.GetAllProfilesQuery;
import com.profiles.service.domain.model.queries.GetProfileByIdQuery;
import com.profiles.service.domain.services.ProfileQueryService;
import com.profiles.service.domain.services.ProfileCommandService;
import com.profiles.service.interfaces.rest.resources.ProfileResource;
import com.profiles.service.interfaces.rest.resources.UpdateProfileResource;
import com.profiles.service.interfaces.rest.transform.ProfileResourceFromEntityAssembler;
import com.profiles.service.interfaces.rest.transform.UpdateProfileCommandFromResourceAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/v1/profiles", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Profiles", description = "Profile Management Endpoints")
public class ProfilesController {
    private final ProfileQueryService profileQueryService;
    private final ProfileCommandService profileCommandService;

    public ProfilesController(ProfileQueryService profileQueryService, ProfileCommandService profileCommandService) {
        this.profileQueryService = profileQueryService;
        this.profileCommandService = profileCommandService;
    }

    /**
     * Get a profile by userId
     * @param userId the ID of the user whose profile is to be retrieved
     * @return ResponseEntity containing the ProfileResource or an error response
     */
    @GetMapping("/{userId}")
    @Operation(summary = "Get a profile by userId")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile found"),
            @ApiResponse(responseCode = "404", description = "Profile not found")})
    public ResponseEntity<ProfileResource> getProfileById(@PathVariable Long userId) {
        var query = new GetProfileByIdQuery(userId);
        var profile = profileQueryService.handle(query);
        if (profile.isEmpty()) { return ResponseEntity.notFound().build(); }
        var resource = ProfileResourceFromEntityAssembler.toResourceFromEntity(profile.get());
        return ResponseEntity.ok(resource);
    }

    /**
     * Get all profiles
     * @return List of ProfileResource
     */
    @GetMapping
    @Operation(summary = "Get all profiles")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profiles found"),
            @ApiResponse(responseCode = "204", description = "No profiles found")})
    public ResponseEntity<List<ProfileResource>> getAllProfiles() {
        var query = new GetAllProfilesQuery();
        var profiles = profileQueryService.handle(query);
        if (profiles.isEmpty()) { return ResponseEntity.noContent().build(); }
        var profileResources = profiles.stream()
                .map(ProfileResourceFromEntityAssembler::toResourceFromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(profileResources);
    }

    /**
     * Update a profile by userId
     * @param userId the ID of the user whose profile is to be updated
     * @param resource the resource containing the updated profile data
     * @return ResponseEntity containing the updated ProfileResource or an error response
     */
    @PutMapping(value = "/{userId}/profile", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update a profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
            @ApiResponse(responseCode = "404", description = "Profile not found"),
            @ApiResponse(responseCode = "400", description = "Invalid profile data")})
    public ResponseEntity<ProfileResource> updateProfile(@PathVariable Long userId, @RequestBody UpdateProfileResource resource) {
        var query = new GetProfileByIdQuery(userId);
        var existingProfile = profileQueryService.handle(query);
        if (existingProfile.isEmpty()) { return ResponseEntity.notFound().build(); }
        var command = UpdateProfileCommandFromResourceAssembler.toCommandFromResource(resource, userId);
        var updatedProfile = profileCommandService.handle(command);
        if (updatedProfile.isEmpty()) { return ResponseEntity.badRequest().build(); }
        var profileResource = ProfileResourceFromEntityAssembler.toResourceFromEntity(updatedProfile.get());
        return ResponseEntity.ok(profileResource);
    }
}

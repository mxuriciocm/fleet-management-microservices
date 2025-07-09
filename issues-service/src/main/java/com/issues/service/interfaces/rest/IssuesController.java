package com.issues.service.interfaces.rest;

import com.issues.service.application.events.EventsConsumer;
import com.issues.service.domain.model.queries.GetIssueByIdQuery;
import com.issues.service.domain.model.queries.GetIssuesByCarrierIdQuery;
import com.issues.service.domain.model.queries.GetIssuesByManagerIdQuery;
import com.issues.service.domain.model.queries.GetIssuesByTypeQuery;
import com.issues.service.domain.model.valueobjects.IssueType;
import com.issues.service.domain.services.IssueCommandService;
import com.issues.service.domain.services.IssueQueryService;
import com.issues.service.interfaces.rest.resources.CreateIssueResource;
import com.issues.service.interfaces.rest.resources.IssueResource;
import com.issues.service.interfaces.rest.resources.UpdateIssueResource;
import com.issues.service.interfaces.rest.transform.CreateIssueCommandFromResourceAssembler;
import com.issues.service.interfaces.rest.transform.IssueResourceFromEntityAssembler;
import com.issues.service.interfaces.rest.transform.UpdateIssueCommandFromResourceAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/issues", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Issues", description = "Issues Management Endpoints")
public class IssuesController {

    private final IssueCommandService issueCommandService;
    private final IssueQueryService issueQueryService;
    private final EventsConsumer eventsConsumer;

    public IssuesController(IssueCommandService issueCommandService,
                          IssueQueryService issueQueryService,
                          EventsConsumer eventsConsumer) {
        this.issueCommandService = issueCommandService;
        this.issueQueryService = issueQueryService;
        this.eventsConsumer = eventsConsumer;
    }

    @PostMapping
    @Operation(summary = "Create a new issue")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Issue created successfully"),
            @ApiResponse(responseCode = "400", description = "User not authenticated or not a carrier")
    })
    public ResponseEntity<IssueResource> createIssue(@Valid @RequestBody CreateIssueResource resource, 
                                                    HttpServletRequest request) {
        Long carrierId = getUserIdFromRequest(request);
        if (carrierId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!hasRole(request, "CARRIER")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Long managerId = eventsConsumer.getManagerForCarrier(carrierId);
        if (managerId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        Long vehicleId = null;
        if (resource.type() == IssueType.VEHICLE) {
            vehicleId = eventsConsumer.getVehicleIdForCarrier(carrierId);
            if (vehicleId == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }
        }

        var command = CreateIssueCommandFromResourceAssembler.toCommandFromResource(resource, carrierId, managerId, vehicleId);
        var issue = issueCommandService.handle(command);
        var issueResource = IssueResourceFromEntityAssembler.toResourceFromEntity(issue);
        return ResponseEntity.status(HttpStatus.CREATED).body(issueResource);
    }

    @GetMapping("/{issueId}")
    @Operation(summary = "Get an issue by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Issue retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Issue not found"),
            @ApiResponse(responseCode = "403", description = "User not authorized to access this issue")
    })
    public ResponseEntity<IssueResource> getIssueById(@PathVariable Long issueId, HttpServletRequest request) {
        var issueOptional = issueQueryService.handle(new GetIssueByIdQuery(issueId));
        if (issueOptional.isEmpty()) { return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); }
        var issue = issueOptional.get();
        if (!canAccessIssue(request, issue.getCarrierId(), issue.getManagerId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(IssueResourceFromEntityAssembler.toResourceFromEntity(issue));
    }

    @GetMapping("/carrier/issues")
    @Operation(summary = "Get all issues created by the authenticated carrier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Issues retrieved successfully"),
            @ApiResponse(responseCode = "204", description = "No issues found"),
            @ApiResponse(responseCode = "403", description = "User not authorized to access these issues")
    })
    public ResponseEntity<List<IssueResource>> getMyIssues(HttpServletRequest request) {
        Long carrierId = getUserIdFromRequest(request);
        if (carrierId == null) { return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); }
        if (!hasRole(request, "CARRIER")) { return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); }
        var issues = issueQueryService.handle(new GetIssuesByCarrierIdQuery(carrierId));
        if (issues.isEmpty()) { return ResponseEntity.noContent().build(); }
        var resources = IssueResourceFromEntityAssembler.toResourceFromEntities(issues);
        return ResponseEntity.ok(resources);
    }

    @GetMapping("/carrier/{carrierId}")
    @Operation(summary = "Get all issues created by a specific carrier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Issues found successfully"),
            @ApiResponse(responseCode = "204", description = "No issues found for the specified carrier"),
            @ApiResponse(responseCode = "403", description = "User not authorized to access these issues")
    })
    public ResponseEntity<List<IssueResource>> getIssuesByCarrierId(@PathVariable Long carrierId, 
                                                                  HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) { return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); }
        if (!hasRole(request, "ADMIN") &&
            !(hasRole(request, "CARRIER") && userId.equals(carrierId))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        var issues = issueQueryService.handle(new GetIssuesByCarrierIdQuery(carrierId));
        if (issues.isEmpty()) { return ResponseEntity.noContent().build(); }
        var resources = IssueResourceFromEntityAssembler.toResourceFromEntities(issues);
        return ResponseEntity.ok(resources);
    }

    @GetMapping("/manager/{managerId}")
    @Operation(summary = "Get all issues created by a specific manager")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Issues found successfully"),
            @ApiResponse(responseCode = "204", description = "No issues found for the specified manager"),
            @ApiResponse(responseCode = "403", description = "User not authorized to access these issues")
    })
    public ResponseEntity<List<IssueResource>> getIssuesByManagerId(@PathVariable Long managerId, 
                                                                  HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) { return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); }
        if (!hasRole(request, "ADMIN") && 
            !(hasRole(request, "MANAGER") && userId.equals(managerId))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        var issues = issueQueryService.handle(new GetIssuesByManagerIdQuery(managerId));
        if (issues.isEmpty()) { return ResponseEntity.noContent().build(); }
        var resources = IssueResourceFromEntityAssembler.toResourceFromEntities(issues);
        return ResponseEntity.ok(resources);
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Get all issues by type")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Issues found successfully"),
            @ApiResponse(responseCode = "204", description = "No issues found for the specified type"),
            @ApiResponse(responseCode = "403", description = "User not authorized to access these issues")
    })
    public ResponseEntity<List<IssueResource>> getIssuesByType(@PathVariable IssueType type,
                                                             HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) { return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); }
        var issues = issueQueryService.handle(new GetIssuesByTypeQuery(type));
        var filteredIssues = issues.stream()
                .filter(issue -> canAccessIssue(request, issue.getCarrierId(), issue.getManagerId()))
                .toList();

        if (filteredIssues.isEmpty()) { return ResponseEntity.noContent().build(); }
        var resources = IssueResourceFromEntityAssembler.toResourceFromEntities(filteredIssues);
        return ResponseEntity.ok(resources);
    }

    @PutMapping(value = "/{issueId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update an existing issue")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Issue updated successfully"),
            @ApiResponse(responseCode = "404", description = "Issue not found"),
            @ApiResponse(responseCode = "403", description = "User not authorized to update this issue")
    })
    public ResponseEntity<IssueResource> updateIssue(
            @PathVariable Long issueId,
            @Valid @RequestBody UpdateIssueResource resource,
            HttpServletRequest request) {

        var issueOptional = issueQueryService.handle(new GetIssueByIdQuery(issueId));
        if (issueOptional.isEmpty()) { return ResponseEntity.notFound().build(); }
        var issue = issueOptional.get();
        Long userId = getUserIdFromRequest(request);
        if (userId == null || !issue.getCarrierId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        var command = UpdateIssueCommandFromResourceAssembler.toCommandFromResource(resource);
        var updatedIssueOptional = issueCommandService.handle(issueId, command);
        return updatedIssueOptional
                .map(updatedIssue -> ResponseEntity.ok(IssueResourceFromEntityAssembler.toResourceFromEntity(updatedIssue)))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{issueId}")
    @Operation(summary = "Delete an issue")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Issue deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Issue not found"),
            @ApiResponse(responseCode = "403", description = "User not authorized to delete this issue")
    })
    public ResponseEntity<Void> deleteIssue(@PathVariable Long issueId, HttpServletRequest request) {
        var issueOptional = issueQueryService.handle(new GetIssueByIdQuery(issueId));
        if (issueOptional.isEmpty()) { return ResponseEntity.notFound().build(); }
        var issue = issueOptional.get();
        Long userId = getUserIdFromRequest(request);
        if (userId == null ||
            (!hasRole(request, "ADMIN") && !issue.getCarrierId().equals(userId))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        boolean deleted = issueCommandService.deleteIssue(issueId);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    private Long getUserIdFromRequest(HttpServletRequest request) {
        String userIdHeader = request.getHeader("X-User-Id");
        if (userIdHeader != null && !userIdHeader.isEmpty()) {
            try {
                return Long.valueOf(userIdHeader);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private boolean hasRole(HttpServletRequest request, String role) {
        String rolesHeader = request.getHeader("X-User-Roles");
        if (rolesHeader != null && !rolesHeader.isEmpty()) {
            List<String> roles = Arrays.asList(rolesHeader.split(","));
            for (String userRole : roles) {
                String normalizedUserRole = userRole.trim().replace("ROLE_", "").toUpperCase();
                String normalizedRole = role.replace("ROLE_", "").toUpperCase();
                if (normalizedUserRole.equals(normalizedRole)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean canAccessIssue(HttpServletRequest request, Long issueCarrierId, Long issueManagerId) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) { return false; }
        if (hasRole(request, "ADMIN")) { return true; }
        if (hasRole(request, "MANAGER") && issueManagerId != null && issueManagerId.equals(userId)) { return true; }
        return hasRole(request, "CARRIER") && issueCarrierId.equals(userId);
    }
}

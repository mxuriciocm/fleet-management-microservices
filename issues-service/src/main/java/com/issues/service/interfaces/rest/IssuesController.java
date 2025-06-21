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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger log = LoggerFactory.getLogger(IssuesController.class);

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

    /**
     * Create a new issue.
     * @param resource the resource containing the issue details
     * @return ResponseEntity with the created issue resource
     */
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

        // Verificar que el usuario es un CARRIER
        if (!hasRole(request, "CARRIER")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Obtener el managerId para el carrier
        Long managerId = eventsConsumer.getManagerForCarrier(carrierId);

        if (managerId == null) {
            log.warn("No se pudo determinar el manager para el carrier {}. El carrier debe estar asignado a un vehículo primero.", carrierId);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(null);
        }

        // Obtener el vehicleId automáticamente si la incidencia es de tipo VEHICLE
        Long vehicleId = null;
        if (resource.type() == IssueType.VEHICLE) {
            // Obtenemos el vehicleId a partir del carrierId usando el mapa carrierVehicleMap
            vehicleId = eventsConsumer.getVehicleIdForCarrier(carrierId);

            if (vehicleId == null) {
                log.warn("No se pudo determinar el vehículo para el carrier {} y la incidencia es de tipo VEHICLE", carrierId);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(null);
            }

            log.info("Asignando automáticamente vehicleId {} para incidencia de tipo VEHICLE del carrier {}", vehicleId, carrierId);
        }

        var command = CreateIssueCommandFromResourceAssembler.toCommandFromResource(resource, carrierId, managerId, vehicleId);
        var issue = issueCommandService.handle(command);
        var issueResource = IssueResourceFromEntityAssembler.toResourceFromEntity(issue);
        return ResponseEntity.status(HttpStatus.CREATED).body(issueResource);
    }

    /**
     * Get an issue by its ID.
     * @param issueId the ID of the issue to retrieve
     * @return ResponseEntity with the issue resource if found, or appropriate error status
     */
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

    /**
     * Get all issues created by the authenticated carrier.
     * @return ResponseEntity with a list of issue resources if found, or no content status
     */
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

    /**
     * Get all issues created by a specific carrier.
     * @param carrierId the ID of the carrier whose issues to retrieve
     * @return ResponseEntity with a list of issue resources if found, or appropriate error status
     */
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
        // Solo un ADMIN o el propio CARRIER pueden acceder a sus issues
        // o un MANAGER que gestiona a ese CARRIER (en este caso, necesitamos mantener una vista local con esta info)
        if (!hasRole(request, "ADMIN") && 
            !(hasRole(request, "CARRIER") && userId.equals(carrierId))) {
            // Verificar si es un manager que gestiona a este carrier
            // Esto requeriría una consulta a una vista local que mantenga esta relación
            // Por ahora, simplemente rechazamos si no es ADMIN o el propio CARRIER
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        var issues = issueQueryService.handle(new GetIssuesByCarrierIdQuery(carrierId));
        if (issues.isEmpty()) { return ResponseEntity.noContent().build(); }
        var resources = IssueResourceFromEntityAssembler.toResourceFromEntities(issues);
        return ResponseEntity.ok(resources);
    }

    /**
     * Get all issues created by a specific manager.
     * @param managerId the ID of the manager whose issues to retrieve
     * @return ResponseEntity with a list of issue resources if found, or appropriate error status
     */
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

    /**
     * Get all issues by type.
     * @param type the type of issues to retrieve
     * @return ResponseEntity with a list of issue resources if found, or appropriate error status
     */
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

    /**
     * Update an existing issue.
     * @param issueId the ID of the issue to update
     * @param resource the UpdateIssueResource containing the updated issue data
     * @return ResponseEntity containing the updated IssueResource if successful, or an error response
     */
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

    /**
     * Delete an issue.
     * @param issueId the ID of the issue to delete
     * @return ResponseEntity indicating success or failure
     */
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

    /**
     * Extract user ID from request headers set by the gateway
     */
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

    /**
     * Check if the user has a specific role from the roles header set by the gateway
     */
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

    /**
     * Check if the user can access a specific issue based on their role and the issue's owners
     */
    private boolean canAccessIssue(HttpServletRequest request, Long issueCarrierId, Long issueManagerId) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) { return false; }
        if (hasRole(request, "ADMIN")) { return true; }
        if (hasRole(request, "MANAGER") && issueManagerId != null && issueManagerId.equals(userId)) { return true; }
        return hasRole(request, "CARRIER") && issueCarrierId.equals(userId);
    }
}

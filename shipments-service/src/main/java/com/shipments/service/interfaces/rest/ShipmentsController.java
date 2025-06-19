package com.shipments.service.interfaces.rest;

import com.shipments.service.domain.model.queries.GetShipmentByIdQuery;
import com.shipments.service.domain.model.queries.GetShipmentsByCarrierIdQuery;
import com.shipments.service.domain.model.queries.GetShipmentsByManagerIdQuery;
import com.shipments.service.domain.model.queries.GetShipmentsByStatusQuery;
import com.shipments.service.domain.model.valueobjects.ShipmentStatus;
import com.shipments.service.domain.services.ShipmentCommandService;
import com.shipments.service.domain.services.ShipmentQueryService;
import com.shipments.service.interfaces.rest.resources.CreateShipmentResource;
import com.shipments.service.interfaces.rest.resources.ShipmentResource;
import com.shipments.service.interfaces.rest.resources.UpdateShipmentResource;
import com.shipments.service.interfaces.rest.transform.CreateShipmentCommandFromResourceAssembler;
import com.shipments.service.interfaces.rest.transform.ShipmentResourceFromEntityAssembler;
import com.shipments.service.interfaces.rest.transform.UpdateShipmentCommandFromResourceAssembler;
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
@RequestMapping(value = "/api/v1/shipments", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Shipments", description = "Shipments Management Endpoints")
public class ShipmentsController {
    private final ShipmentCommandService shipmentCommandService;
    private final ShipmentQueryService shipmentQueryService;

    public ShipmentsController(ShipmentCommandService shipmentCommandService, ShipmentQueryService shipmentQueryService) {
        this.shipmentCommandService = shipmentCommandService;
        this.shipmentQueryService = shipmentQueryService;
    }


    /**
     * Creates a new shipment.
     * Requires the user to be authenticated and have the role of "ROLE_MANAGER".
     * @param resource the resource containing shipment details
     * @return ResponseEntity with the created ShipmentResource or an error status
     */
    @PostMapping
    @Operation(summary = "Create a new shipment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Shipment created successfully"),
            @ApiResponse(responseCode = "401", description = "User not autheticated or not a manager"),
    })
    public ResponseEntity<ShipmentResource> createShipment(@Valid @RequestBody CreateShipmentResource resource, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) { return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); }
        if (!hasRole(request, "ROLE_MANAGER")) { return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); }
        var command = CreateShipmentCommandFromResourceAssembler.toCommandFromResource(resource, userId);
        var shipment = shipmentCommandService.handle(command);
        var shipmentResource = ShipmentResourceFromEntityAssembler.toResourceFromEntity(shipment);
        return ResponseEntity.status(HttpStatus.CREATED).body(shipmentResource);
    }

    /**
     * Retrieves a shipment by its ID.
     * @param shipmentId the ID of the shipment to retrieve
     * @return ResponseEntity with the ShipmentResource if found, or 404 Not Found if not found,
     */
    @GetMapping("/{shipmentId}")
    @Operation(summary = "Get shipment by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Shipment found"),
            @ApiResponse(responseCode = "404", description = "Shipment not found")
    })
    public ResponseEntity<ShipmentResource> getShipmentById(@PathVariable Long shipmentId, HttpServletRequest request) {
        var shipmentOptional = shipmentQueryService.handle(new GetShipmentByIdQuery(shipmentId));
        if (shipmentOptional.isEmpty()) { return ResponseEntity.notFound().build(); }
        var shipment = shipmentOptional.get();
        if (!canAccessShipment(request, shipment.getManagerId(), shipment.getCarrierId())) { return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); }
        return ResponseEntity.ok(ShipmentResourceFromEntityAssembler.toResourceFromEntity(shipment));
    }

    /**
     * Retrieves all shipments for the authenticated manager.
     * Requires the user to be authenticated and have the role of "ROLE_MANAGER".
     * @return ResponseEntity with a list of ShipmentResource or 204 No Content if no shipments found
     */
    @GetMapping("/manager/shipments")
    @Operation(summary = "Get all shipments for the authenticated manager")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Shipments found"),
            @ApiResponse(responseCode = "204", description = "No shipments found"),
            @ApiResponse(responseCode = "401", description = "User not authenticated or not a manager")
    })
    public ResponseEntity<List<ShipmentResource>> getAllShipmentsForManager(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) { return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); }
        if (!hasRole(request, "ROLE_MANAGER")) { return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); }
        var shipments = shipmentQueryService.handle(new GetShipmentsByManagerIdQuery(userId));
        if (shipments.isEmpty()) { return ResponseEntity.noContent().build(); }
        var resources = ShipmentResourceFromEntityAssembler.toResourceFromEntities(shipments);
        return ResponseEntity.ok(resources);
    }

    /**
     * Retrieves all shipments assigned to the authenticated carrier.
     * Requires the user to be authenticated and have the role of "ROLE_CARRIER".
     * @return ResponseEntity with a list of ShipmentResource or 204 No Content if no shipments found
     */
    @GetMapping("/carrier/shipments")
    @Operation(summary = "Get all shipments assigned to the authenticated carrier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Shipments found"),
            @ApiResponse(responseCode = "204", description = "No shipments assigned"),
            @ApiResponse(responseCode = "401", description = "User not authenticated or not a carrier")
    })
    public ResponseEntity<List<ShipmentResource>> getAllShipmentsForCarrier(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) { return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); }
        if (!hasRole(request, "ROLE_CARRIER")) { return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); }
        var shipments = shipmentQueryService.handle(new GetShipmentsByCarrierIdQuery(userId));
        if (shipments.isEmpty()) { return ResponseEntity.noContent().build(); }
        var resources = ShipmentResourceFromEntityAssembler.toResourceFromEntities(shipments);
        return ResponseEntity.ok(resources);
    }

    /**
     * Retrieves all shipments by their status.
     * @param status the status of the shipments to retrieve
     * @return ResponseEntity with a list of ShipmentResource or 204 No Content if no shipments found
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "Get all shipments by status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Shipments found"),
            @ApiResponse(responseCode = "204", description = "No shipments found"),
            @ApiResponse(responseCode = "401", description = "User not authenticated")
    })
    public ResponseEntity<List<ShipmentResource>> getShipmentsByStatus(@PathVariable ShipmentStatus status, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) { return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); }
        var shipments = shipmentQueryService.handle(new GetShipmentsByStatusQuery(status));
        var filteredShipments = shipments.stream()
                .filter(shipment -> canAccessShipment(request, shipment.getManagerId(), shipment.getCarrierId()))
                .toList();
        if (filteredShipments.isEmpty()) { return ResponseEntity.noContent().build(); }
        var resources = ShipmentResourceFromEntityAssembler.toResourceFromEntities(filteredShipments);
        return ResponseEntity.ok(resources);
    }

    /**
     * Updates an existing shipment.
     * Requires the user to be authenticated and have the role of "ROLE_MANAGER" or "ROLE_ADMIN" with access to the shipment.
     * @param shipmentId the ID of the shipment to update
     * @param resource the resource containing updated shipment details
     * @return ResponseEntity with the updated ShipmentResource or an error status
     */
    @PutMapping(value = "/{shipmentId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update an existing shipment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Shipment updated successfully"),
            @ApiResponse(responseCode = "404", description = "Shipment not found"),
            @ApiResponse(responseCode = "403", description = "User not authorized to update this shipment")
    })
    public ResponseEntity<ShipmentResource> updateShipment(@PathVariable Long shipmentId, @Valid @RequestBody UpdateShipmentResource resource, HttpServletRequest request) {
        var shipmentOptional = shipmentQueryService.handle(new GetShipmentByIdQuery(shipmentId));
        if (shipmentOptional.isEmpty()) { return ResponseEntity.notFound().build(); }
        var shipment = shipmentOptional.get();
        if (!canAccessShipment(request, shipment.getManagerId(), null)) { return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); }
        var command = UpdateShipmentCommandFromResourceAssembler.toCommandFromResource(resource);
        var updatedShipmentOptional = shipmentCommandService.handle(shipmentId, command);
        if (updatedShipmentOptional.isEmpty()) { return ResponseEntity.notFound().build(); }
        return ResponseEntity.ok(ShipmentResourceFromEntityAssembler.toResourceFromEntity(updatedShipmentOptional.get()));
    }

    /**
     * Assigns a carrier to a shipment.
     * @param shipmentId the ID of the shipment to assign the carrier to
     * @param carrierId the ID of the carrier to assign
     * @return ResponseEntity with the updated ShipmentResource or an error status
     */
    @PutMapping("/{shipmentId}/carrier/{carrierId}")
    @Operation(summary = "Assign a carrier to a shipment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Carrier assigned successfully"),
            @ApiResponse(responseCode = "404", description = "Shipment not found"),
            @ApiResponse(responseCode = "403", description = "User not authorized to assign carrier to this shipment")
    })
    public ResponseEntity<ShipmentResource> assignCarrierToShipment(@PathVariable Long shipmentId, @PathVariable Long carrierId, HttpServletRequest request) {
        var shipmentOptional = shipmentQueryService.handle(new GetShipmentByIdQuery(shipmentId));
        if (shipmentOptional.isEmpty()) { return ResponseEntity.notFound().build(); }
        var shipment = shipmentOptional.get();
        if (!canAccessShipment(request, shipment.getManagerId(), null)) { return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); }
        var updatedShipmentOptional = shipmentCommandService.assignCarrier(shipmentId, carrierId);
        if (updatedShipmentOptional.isEmpty()) { return ResponseEntity.notFound().build(); }
        return ResponseEntity.ok(ShipmentResourceFromEntityAssembler.toResourceFromEntity(updatedShipmentOptional.get()));
    }

    /**
     * Starts a shipment by changing its status to IN_PROGRESS.
     * @param shipmentId the ID of the shipment to start
     * @return ResponseEntity with the updated ShipmentResource or an error status
     */
    @PutMapping("/{shipmentId}/start")
    @Operation(summary = "Start a shipment (change status to IN_PROGRESS)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Shipment started successfully"),
            @ApiResponse(responseCode = "404", description = "Shipment not found"),
            @ApiResponse(responseCode = "403", description = "User not authorized to start this shipment")
    })
    public ResponseEntity<ShipmentResource> startShipment(@PathVariable Long shipmentId, HttpServletRequest request) {
        var shipmentOptional = shipmentQueryService.handle(new GetShipmentByIdQuery(shipmentId));
        if (shipmentOptional.isEmpty()) { return ResponseEntity.notFound().build(); }
        var shipment = shipmentOptional.get();
        Long userId = getUserIdFromRequest(request);
        if (shipment.getCarrierId() == null || !shipment.getCarrierId().equals(userId)) { return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); }
        var updatedShipmentOptional = shipmentCommandService.startShipment(shipmentId);
        if (updatedShipmentOptional.isEmpty()) { return ResponseEntity.notFound().build(); }
        return ResponseEntity.ok(ShipmentResourceFromEntityAssembler.toResourceFromEntity(updatedShipmentOptional.get()));
    }

    /**
     * Completes a shipment by changing its status to COMPLETE.
     * @param shipmentId the ID of the shipment to complete
     * @return ResponseEntity with the updated ShipmentResource or an error status
     */
    @PutMapping("/{shipmentId}/complete")
    @Operation(summary = "Complete a shipment (change status to COMPLETED)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Shipment completed successfully"),
            @ApiResponse(responseCode = "404", description = "Shipment not found"),
            @ApiResponse(responseCode = "403", description = "User not authorized to complete this shipment")
    })
    public ResponseEntity<ShipmentResource> completeShipment(@PathVariable Long shipmentId, HttpServletRequest request) {
        var shipmentOptional = shipmentQueryService.handle(new GetShipmentByIdQuery(shipmentId));
        if (shipmentOptional.isEmpty()) { return ResponseEntity.notFound().build(); }
        var shipment = shipmentOptional.get();
        Long userId = getUserIdFromRequest(request);
        if (shipment.getCarrierId() == null || !shipment.getCarrierId().equals(userId)) { return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); }
        var updatedShipmentOptional = shipmentCommandService.completeShipment(shipmentId);
        if (updatedShipmentOptional.isEmpty()) { return ResponseEntity.notFound().build(); }
        return ResponseEntity.ok(ShipmentResourceFromEntityAssembler.toResourceFromEntity(updatedShipmentOptional.get()));
    }

    /**
     * Cancels a shipment by changing its status to CANCEL.
     * @param shipmentId the ID of the shipment to cancel
     * @return ResponseEntity with the updated ShipmentResource or an error status
     */
    @PutMapping("/{shipmentId}/cancel")
    @Operation(summary = "Cancel a shipment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Shipment cancelled successfully"),
            @ApiResponse(responseCode = "404", description = "Shipment not found"),
            @ApiResponse(responseCode = "403", description = "User not authorized to cancel this shipment")
    })
    public ResponseEntity<ShipmentResource> cancelShipment(@PathVariable Long shipmentId, HttpServletRequest request) {
        var shipmentOptional = shipmentQueryService.handle(new GetShipmentByIdQuery(shipmentId));
        if (shipmentOptional.isEmpty()) { return ResponseEntity.notFound().build(); }
        var shipment = shipmentOptional.get();
        if (!canAccessShipment(request, shipment.getManagerId(), null)) { return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); }
        var updatedShipmentOptional = shipmentCommandService.cancelShipment(shipmentId);
        if (updatedShipmentOptional.isEmpty()) { return ResponseEntity.notFound().build(); }
        return ResponseEntity.ok(ShipmentResourceFromEntityAssembler.toResourceFromEntity(updatedShipmentOptional.get()));
    }

    /**
     * Deletes a shipment by its ID.
     * Requires the user to be authenticated and have the role of "ROLE_MANAGER" or "ROLE_ADMIN" with access to the shipment.
     * @param shipmentId the ID of the shipment to delete
     * @return ResponseEntity with no content if deleted successfully, or an error status
     */
    @DeleteMapping("/{shipmentId}")
    @Operation(summary = "Delete a shipment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Shipment deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Shipment not found"),
            @ApiResponse(responseCode = "403", description = "User not authorized to delete this shipment")
    })
    public ResponseEntity<Void> deleteShipment(@PathVariable Long shipmentId, HttpServletRequest request) {
        var shipmentOptional = shipmentQueryService.handle(new GetShipmentByIdQuery(shipmentId));
        if (shipmentOptional.isEmpty()) { return ResponseEntity.notFound().build(); }
        var shipment = shipmentOptional.get();
        if (!canAccessShipment(request, shipment.getManagerId(), null)) { return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); }
        if (shipment.getStatus() == ShipmentStatus.IN_PROGRESS || shipment.getStatus() == ShipmentStatus.COMPLETED) {
            return ResponseEntity.status(HttpStatus.CONFLICT).header("X-error-message", "Cannot delete shipment that is in progress or completed").build();
        }
        boolean deleted = shipmentCommandService.deleteShipment(shipmentId);
        if (deleted) { return ResponseEntity.noContent().build(); }
        else { return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); }
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
                // Normalizar los roles para la comparación (con o sin el prefijo ROLE_)
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
     * Check if the user can access a specific shipment based on their role and the shipment's owner/carrier
     */
    private boolean canAccessShipment(HttpServletRequest request, Long shipmentManagerId, Long shipmentCarrierId) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return false;
        }

        // Los administradores pueden acceder a cualquier envío
        if (hasRole(request, "ADMIN")) {
            return true;
        }

        // Los managers pueden acceder a sus propios envíos
        if (hasRole(request, "MANAGER") && shipmentManagerId != null && shipmentManagerId.equals(userId)) {
            return true;
        }

        // Los carriers pueden acceder solo a los envíos asignados a ellos
        return hasRole(request, "CARRIER") && shipmentCarrierId != null && shipmentCarrierId.equals(userId);
    }
}

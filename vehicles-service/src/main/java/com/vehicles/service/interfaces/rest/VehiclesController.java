package com.vehicles.service.interfaces.rest;

import com.vehicles.service.domain.model.queries.GetVehicleByCarrierIdQuery;
import com.vehicles.service.domain.model.queries.GetVehicleByIdQuery;
import com.vehicles.service.domain.model.queries.GetVehicleByLicensePlateQuery;
import com.vehicles.service.domain.model.queries.GetVehiclesByManagerIdQuery;
import com.vehicles.service.domain.model.valueobjects.VehicleStatus;
import com.vehicles.service.domain.services.VehicleCommandService;
import com.vehicles.service.domain.services.VehicleQueryService;
import com.vehicles.service.interfaces.rest.resources.CreateVehicleResource;
import com.vehicles.service.interfaces.rest.resources.UpdateVehicleResource;
import com.vehicles.service.interfaces.rest.resources.VehicleResource;
import com.vehicles.service.interfaces.rest.transform.CreateVehicleCommandFromResourceAssembler;
import com.vehicles.service.interfaces.rest.transform.UpdateVehicleCommandFromResourceAssembler;
import com.vehicles.service.interfaces.rest.transform.VehicleResourceFromEntityAssembler;
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
@RequestMapping(value = "/api/v1/vehicles", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Vehicles", description = "Vehicles Management Endpoints")
public class VehiclesController {
    private final VehicleCommandService vehicleCommandService;
    private final VehicleQueryService vehicleQueryService;

    public VehiclesController(VehicleCommandService vehicleCommandService, VehicleQueryService vehicleQueryService) {
        this.vehicleCommandService = vehicleCommandService;
        this.vehicleQueryService = vehicleQueryService;
    }

    /**
     * Create a new vehicle.
     * Only managers can create vehicles, and they must not have reached their vehicle limit.
     * @param resource the CreateVehicleResource containing the vehicle data.
     * @return ResponseEntity containing the created VehicleResource if successful, or an error response.
     */
    @PostMapping
    @Operation(summary = "Create a new vehicle")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Vehicle created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid vehicle data provided"),
            @ApiResponse(responseCode = "403", description = "User not authorized or has reached vehicle limit")
    })
    public ResponseEntity<VehicleResource> createVehicle(
            @Valid @RequestBody CreateVehicleResource resource,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!hasRole(request, "MANAGER")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        boolean isPro = false; // Esto podría venir de un servicio de suscripción en el futuro
        if (vehicleCommandService.hasReachedVehicleLimit(userId, isPro)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        var command = CreateVehicleCommandFromResourceAssembler.toCommandFromResource(resource, userId);
        var vehicle = vehicleCommandService.handle(command);
        var vehicleResource = VehicleResourceFromEntityAssembler.toResourceFromEntity(vehicle);
        return ResponseEntity.status(HttpStatus.CREATED).body(vehicleResource);
    }

    /**
     * Get a vehicle by its ID.
     * @param vehicleId the ID of the vehicle to retrieve
     * @return ResponseEntity containing the VehicleResource if found, or 404 Not Found if not found.
     */
    @GetMapping("/{vehicleId}")
    @Operation(summary = "Get vehicle by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vehicle found and returned successfully"),
            @ApiResponse(responseCode = "404", description = "Vehicle not found"),
            @ApiResponse(responseCode = "403", description = "User not authorized to access this vehicle")
    })
    public ResponseEntity<VehicleResource> getVehicleById(
            @PathVariable Long vehicleId,
            HttpServletRequest request) {
        var vehicleOptional = vehicleQueryService.handle(new GetVehicleByIdQuery(vehicleId));
        if (vehicleOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        var vehicle = vehicleOptional.get();
        if (!canAccessVehicle(request, vehicle.getManagerId(), vehicle.getCarrierId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(VehicleResourceFromEntityAssembler.toResourceFromEntity(vehicle));
    }

    /**
     * Get a vehicle by its license plate.
     * @param licensePlate the license plate of the vehicle to retrieve
     * @return ResponseEntity containing the VehicleResource if found, or 404 Not Found if not found.
     */
    @GetMapping("/license-plate/{licensePlate}")
    @Operation(summary = "Get vehicle by license plate")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vehicle found and returned successfully"),
            @ApiResponse(responseCode = "404", description = "Vehicle not found"),
            @ApiResponse(responseCode = "403", description = "User not authorized to access this vehicle")
    })
    public ResponseEntity<VehicleResource> getVehicleByLicensePlate(
            @PathVariable String licensePlate,
            HttpServletRequest request) {
        var vehicleOptional = vehicleQueryService.handle(new GetVehicleByLicensePlateQuery(licensePlate));
        if (vehicleOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var vehicle = vehicleOptional.get();
        if (!canAccessVehicle(request, vehicle.getManagerId(), vehicle.getCarrierId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(VehicleResourceFromEntityAssembler.toResourceFromEntity(vehicle));
    }

    /**
     * Get all vehicles for the authenticated manager.
     * @return ResponseEntity containing a list of VehicleResource if found, or 204 No Content if no vehicles found.
     */
    @GetMapping("/manager/vehicles")
    @Operation(summary = "Get all vehicles for the authenticated manager")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vehicles found and returned successfully"),
            @ApiResponse(responseCode = "204", description = "No vehicles found"),
            @ApiResponse(responseCode = "403", description = "User not authorized")
    })
    public ResponseEntity<List<VehicleResource>> getAllVehiclesForManager(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!hasRole(request, "MANAGER")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        var vehicles = vehicleQueryService.handle(new GetVehiclesByManagerIdQuery(userId));
        if (vehicles.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        var resources = VehicleResourceFromEntityAssembler.toResourceFromEntities(vehicles);
        return ResponseEntity.ok(resources);
    }

    /**
     * Get the assigned vehicle for the authenticated carrier.
     * @return ResponseEntity containing the VehicleResource if found, or 204 No Content if not found.
     */
    @GetMapping("/carrier/vehicle")
    @Operation(summary = "Get the assigned vehicle for the authenticated carrier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vehicle found and returned successfully"),
            @ApiResponse(responseCode = "204", description = "No vehicle assigned to carrier"),
            @ApiResponse(responseCode = "403", description = "User not authorized")
    })
    public ResponseEntity<VehicleResource> getAssignedVehicleForCarrier(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!hasRole(request, "CARRIER")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return vehicleQueryService.handle(new GetVehicleByCarrierIdQuery(userId))
                .map(vehicle -> ResponseEntity.ok(VehicleResourceFromEntityAssembler.toResourceFromEntity(vehicle)))
                .orElse(ResponseEntity.noContent().build());
    }

    /**
     * Get a vehicle assigned to a specific carrier.
     * Only accessible by managers who own the vehicle or admins.
     * @param carrierId the ID of the carrier to retrieve the vehicle for
     * @return ResponseEntity containing the VehicleResource if found, or 404 Not Found if not found.
     */
    @GetMapping("/carrier/{carrierId}/vehicle")
    @Operation(summary = "Get vehicle assigned to a specific carrier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vehicle found and returned successfully"),
            @ApiResponse(responseCode = "404", description = "Vehicle not found"),
            @ApiResponse(responseCode = "403", description = "User not authorized to access this vehicle")
    })
    public ResponseEntity<VehicleResource> getVehicleByCarrierId(
            @PathVariable Long carrierId,
            HttpServletRequest request) {
        var vehicleOptional = vehicleQueryService.handle(new GetVehicleByCarrierIdQuery(carrierId));
        if (vehicleOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var vehicle = vehicleOptional.get();
        if (!hasRole(request, "ADMIN") &&
            !(hasRole(request, "MANAGER") && vehicle.getManagerId().equals(getUserIdFromRequest(request)))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(VehicleResourceFromEntityAssembler.toResourceFromEntity(vehicle));
    }

    /**
     * Get all vehicles for a specific manager.
     * Only accessible by Admins.
     * @param managerId the ID of the manager to retrieve vehicles for
     * @return ResponseEntity containing a list of VehicleResource if found, or 204 No Content if no vehicles found.
     */
    @GetMapping("/manager/{managerId}/vehicles")
    @Operation(summary = "Get all vehicles for a specific manager (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vehicles found and returned successfully"),
            @ApiResponse(responseCode = "204", description = "No vehicles found"),
            @ApiResponse(responseCode = "403", description = "User not authorized")
    })
    public ResponseEntity<List<VehicleResource>> getVehiclesByManagerId(
            @PathVariable Long managerId,
            HttpServletRequest request) {
        if (!hasRole(request, "ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        var vehicles = vehicleQueryService.handle(new GetVehiclesByManagerIdQuery(managerId));
        if (vehicles.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        var resources = VehicleResourceFromEntityAssembler.toResourceFromEntities(vehicles);
        return ResponseEntity.ok(resources);
    }

    /**
     * Update an existing vehicle.
     * @param vehicleId the ID of the vehicle to update
     * @param resource the UpdateVehicleResource containing the updated vehicle data.
     * @return ResponseEntity containing the updated VehicleResource if successful, or an error response.
     */
    @PutMapping(value = "/{vehicleId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update an existing vehicle")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vehicle updated successfully"),
            @ApiResponse(responseCode = "404", description = "Vehicle not found"),
            @ApiResponse(responseCode = "403", description = "User not authorized")
    })
    public ResponseEntity<VehicleResource> updateVehicle(
            @PathVariable Long vehicleId,
            @Valid @RequestBody UpdateVehicleResource resource,
            HttpServletRequest request) {
        var vehicleOptional = vehicleQueryService.handle(new GetVehicleByIdQuery(vehicleId));
        if (vehicleOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var vehicle = vehicleOptional.get();
        if (!canAccessVehicle(request, vehicle.getManagerId(), null)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        var command = UpdateVehicleCommandFromResourceAssembler.toCommandFromResource(resource);
        var updatedVehicleOptional = vehicleCommandService.handle(vehicleId, command);
        if (updatedVehicleOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(VehicleResourceFromEntityAssembler.toResourceFromEntity(updatedVehicleOptional.get()));
    }

    /**
     * Assign a carrier to a vehicle.
     * @param vehicleId the ID of the vehicle to assign the carrier to
     * @param carrierId the ID of the carrier to assign to the vehicle
     * @return ResponseEntity containing the updated VehicleResource if successful, or an error response.
     */
    @PutMapping("/{vehicleId}/carrier/{carrierId}")
    @Operation(summary = "Assign a carrier to a vehicle")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Carrier assigned successfully"),
            @ApiResponse(responseCode = "404", description = "Vehicle not found"),
            @ApiResponse(responseCode = "403", description = "User not authorized")
    })
    public ResponseEntity<VehicleResource> assignCarrierToVehicle(
            @PathVariable Long vehicleId,
            @PathVariable Long carrierId,
            HttpServletRequest request) {
        var vehicleOptional = vehicleQueryService.handle(new GetVehicleByIdQuery(vehicleId));
        if (vehicleOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var vehicle = vehicleOptional.get();
        if (!canAccessVehicle(request, vehicle.getManagerId(), null)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        var updatedVehicleOptional = vehicleCommandService.handle(vehicleId, carrierId);
        if (updatedVehicleOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(VehicleResourceFromEntityAssembler.toResourceFromEntity(updatedVehicleOptional.get()));
    }

    /**
     * Change the status of a vehicle.
     * @param vehicleId the ID of the vehicle to change the status of
     * @param status the new status to set for the vehicle
     * @return ResponseEntity containing the updated VehicleResource if successful, or an error response.
     */
    @PutMapping("/{vehicleId}/status/{status}")
    @Operation(summary = "Change the status of a vehicle")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status changed successfully"),
            @ApiResponse(responseCode = "404", description = "Vehicle not found"),
            @ApiResponse(responseCode = "403", description = "User not authorized")
    })
    public ResponseEntity<VehicleResource> changeVehicleStatus(
            @PathVariable Long vehicleId,
            @PathVariable VehicleStatus status,
            HttpServletRequest request) {
        var vehicleOptional = vehicleQueryService.handle(new GetVehicleByIdQuery(vehicleId));
        if (vehicleOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var vehicle = vehicleOptional.get();
        if (!canAccessVehicle(request, vehicle.getManagerId(), null)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        var updatedVehicleOptional = vehicleCommandService.handle(vehicleId, status);
        if (updatedVehicleOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(VehicleResourceFromEntityAssembler.toResourceFromEntity(updatedVehicleOptional.get()));
    }

    /**
     * Remove the carrier assignment from a vehicle.
     * @param vehicleId the ID of the vehicle to remove the carrier from
     * @return ResponseEntity containing the updated VehicleResource if successful, or an error response.
     */
    @DeleteMapping("/{vehicleId}/carrier")
    @Operation(summary = "Remove carrier assignment from a vehicle")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Carrier removed successfully"),
            @ApiResponse(responseCode = "404", description = "Vehicle not found"),
            @ApiResponse(responseCode = "403", description = "User not authorized")
    })
    public ResponseEntity<VehicleResource> removeCarrierFromVehicle(
            @PathVariable Long vehicleId,
            HttpServletRequest request) {
        var vehicleOptional = vehicleQueryService.handle(new GetVehicleByIdQuery(vehicleId));
        if (vehicleOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var vehicle = vehicleOptional.get();
        if (!canAccessVehicle(request, vehicle.getManagerId(), null)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        var updatedVehicleOptional = vehicleCommandService.handle(vehicleId);
        if (updatedVehicleOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(VehicleResourceFromEntityAssembler.toResourceFromEntity(updatedVehicleOptional.get()));
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
     * Check if the user can access a specific vehicle based on their role and the vehicle's owner/carrier
     */
    private boolean canAccessVehicle(HttpServletRequest request, Long vehicleManagerId, Long vehicleCarrierId) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return false;
        }

        // Los administradores pueden acceder a cualquier vehículo
        if (hasRole(request, "ADMIN")) {
            return true;
        }

        // Los managers pueden acceder a sus propios vehículos
        if (hasRole(request, "MANAGER") && vehicleManagerId != null && vehicleManagerId.equals(userId)) {
            return true;
        }

        // Los carriers pueden acceder solo a los vehículos asignados a ellos
        return hasRole(request, "CARRIER") && vehicleCarrierId != null && vehicleCarrierId.equals(userId);
    }
}

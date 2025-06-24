package com.iam.service.domain.model.queries;

/**
 * Query to get all carriers by a specific manager.
 * <p>
 *     This query is used to get all carriers created by a specific manager.
 * </p>
 */
public record GetCarriersByManagerQuery(Long managerId) {
}

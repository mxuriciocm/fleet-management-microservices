package com.profiles.service.domain.services;

import com.profiles.service.domain.model.aggregates.Profile;
import com.profiles.service.domain.model.queries.GetAllProfilesQuery;
import com.profiles.service.domain.model.queries.GetProfileByIdQuery;

import java.util.List;
import java.util.Optional;

/**
 * User profile query service.
 * <p>
 *     This service is responsible for handling user profile queries.
 *     It provides methods to handle queries for getting a user profile by ID, by email, and for getting all user profiles.
 * </p>
 */
public interface ProfileQueryService {
    /**
     * Handle get user profile by ID query.
     *
     * @param query the query containing the user ID
     * @return an optional of UserProfile if found
     */
    Optional<Profile> handle(GetProfileByIdQuery query);

    /**
     * Handle get all user profiles queries.
     *
     * @param query the query to get all user profiles
     * @return a list of UserProfile
     */
    List<Profile> handle(GetAllProfilesQuery query);
}
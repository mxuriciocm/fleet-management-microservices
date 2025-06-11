package com.iam.service.domain.services;

import com.iam.service.domain.model.aggregates.User;
import com.iam.service.domain.model.queries.GetAllUsersQuery;
import com.iam.service.domain.model.queries.GetUserByEmailQuery;
import com.iam.service.domain.model.queries.GetUserByIdQuery;

import java.util.List;
import java.util.Optional;

/**
 * User query service.
 * <p>
 *     This service is responsible for handling user queries.
 *     It provides methods to handle queries for getting all users, getting a user by id, and getting a user by email.
 * </p>
 */
public interface UserQueryService {
    /**
     * Handle get all user queries.
     *
     * @param query the query
     * @return a list of users
     */
    List<User> handle(GetAllUsersQuery query);

    /**
     * Handle get user by id query.
     *
     * @param query the query
     * @return an optional of user if the user was found
     */
    Optional<User> handle(GetUserByIdQuery query);

    /**
     * Handle get user by email query.
     *
     * @param query the query
     * @return an optional of user if the user was found
     */
    Optional<User> handle(GetUserByEmailQuery query);
}

package com.iam.service.application.internal.queryservices;

import com.iam.service.domain.model.aggregates.User;
import com.iam.service.domain.model.queries.GetAllUsersQuery;
import com.iam.service.domain.model.queries.GetUserByEmailQuery;
import com.iam.service.domain.model.queries.GetUserByIdQuery;
import com.iam.service.domain.model.queries.GetCarriersByManagerQuery;
import com.iam.service.domain.model.valueobjects.Roles;
import com.iam.service.domain.services.UserQueryService;
import com.iam.service.infrastructure.persistence.jpa.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of {@link UserQueryService} interface.
 */
@Service
public class UserQueryServiceImpl implements UserQueryService {
    private final UserRepository userRepository;

    /**
     * Constructor.
     *
     * @param userRepository {@link UserRepository} instance.
     */
    public UserQueryServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // inherited javadoc
    @Override
    public List<User> handle(GetAllUsersQuery query) {
        return userRepository.findAll();
    }

    // inherited javadoc
    @Override
    public Optional<User> handle(GetUserByIdQuery query) {
        return userRepository.findById(query.userId());
    }

    // inherited javadoc
    @Override
    public Optional<User> handle(GetUserByEmailQuery query) {
        return userRepository.findByEmail(query.email());
    }

    // inherited javadoc
    @Override
    public List<User> handle(GetCarriersByManagerQuery query) {
        return userRepository.findAll().stream()
                .filter(user ->
                    user.getRoles().stream().anyMatch(role -> role.getName() == Roles.ROLE_CARRIER) &&
                    user.getCreatedBy() != null &&
                    user.getCreatedBy().equals(query.managerId())
                )
                .collect(Collectors.toList());
    }
}

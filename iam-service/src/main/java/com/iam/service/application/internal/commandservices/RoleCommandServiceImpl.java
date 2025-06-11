package com.iam.service.application.internal.commandservices;

import com.iam.service.domain.model.commands.SeedRolesCommand;
import com.iam.service.domain.model.entities.Role;
import com.iam.service.domain.model.valueobjects.Roles;
import com.iam.service.domain.services.RoleCommandService;
import com.iam.service.infrastructure.persistence.jpa.repositories.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * The type Role command service.
 */
@Service
public class RoleCommandServiceImpl implements RoleCommandService {
    private final RoleRepository roleRepository;

    public RoleCommandServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    /**
     * Handle seed roles command.
     * @param command the {@link SeedRolesCommand} command
     */
    @Override
    public void handle(SeedRolesCommand command) {
        Arrays.stream(Roles.values()).forEach(role -> {
            if (!roleRepository.existsByName(role)) {
                roleRepository.save(new Role(Roles.valueOf(role.name())));
            }
        });
    }
}

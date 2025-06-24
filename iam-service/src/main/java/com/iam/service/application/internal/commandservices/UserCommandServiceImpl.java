package com.iam.service.application.internal.commandservices;

import com.iam.service.application.internal.outboundservices.hashing.HashingService;
import com.iam.service.application.internal.outboundservices.tokens.TokenService;
import com.iam.service.domain.model.aggregates.User;
import com.iam.service.domain.model.commands.ChangeEmailCommand;
import com.iam.service.domain.model.commands.ChangePasswordCommand;
import com.iam.service.domain.model.commands.SignInCommand;
import com.iam.service.domain.model.commands.SignUpCommand;
import com.iam.service.domain.model.commands.RegisterCarrierCommand;
import com.iam.service.domain.model.events.UserCreatedEvent;
import com.iam.service.domain.model.valueobjects.Roles;
import com.iam.service.domain.services.UserCommandService;
import com.iam.service.infrastructure.persistence.jpa.repositories.RoleRepository;
import com.iam.service.infrastructure.persistence.jpa.repositories.UserRepository;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.stream.Collectors;

/**
 * User command service implementation.
 * <p>
 *     This class implements the {@link UserCommandService} interface.
 *     It is used to handle the sign-up and sign in commands.
 * </p>
 *
 */
@Service
public class UserCommandServiceImpl implements UserCommandService {
    private static final Logger log = LoggerFactory.getLogger(UserCommandServiceImpl.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final HashingService hashingService;
    private final TokenService tokenService;
    private final StreamBridge streamBridge;

    /**
     * Constructor.
     *
     * @param userRepository the {@link UserRepository} user repository.
     * @param roleRepository the {@link RoleRepository} role repository.
     * @param hashingService the {@link HashingService} hashing service.
     * @param tokenService the {@link TokenService} token service.
     */
    public UserCommandServiceImpl(UserRepository userRepository, RoleRepository roleRepository, HashingService hashingService, TokenService tokenService, StreamBridge streamBridge) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.hashingService = hashingService;
        this.tokenService = tokenService;
        this.streamBridge = streamBridge;
    }

    // inherited javadoc
    @Override
    public Optional<User> handle(SignUpCommand command) {
        if (userRepository.existsByEmail(command.username())) throw new RuntimeException("Email already exists");
        var roles = command.roles().stream().map(role -> roleRepository.findByName(role.getName())
                .orElseThrow(() -> new RuntimeException("Role name not found"))).toList();
        var user = new User(command.username(), hashingService.encode(command.password()), roles);
        var savedUser = userRepository.save(user);

        try {
            UserCreatedEvent event = new UserCreatedEvent(savedUser.getId(), savedUser.getEmail());
            streamBridge.send("user-events", event);
            log.info("User created event published for userId: {}", savedUser.getId());
        } catch (Exception e) {
            log.error("Failed to publish UserCreatedEvent for userId: {}", savedUser.getId(), e);
        }
        return Optional.of(savedUser);
    }


    @Override
    public Optional<ImmutablePair<User, String>> handle(SignInCommand command) {
        var user = userRepository.findByEmail(command.username())
                .orElseThrow(() -> new RuntimeException("Email not found"));
        if (!hashingService.matches(command.password(), user.getPassword()))
            throw new RuntimeException("Invalid password");
        var token = tokenService.generateToken(user.getEmail());
        return Optional.of(new ImmutablePair<>(user, token));
    }

    @Override
    public Optional<User> handle(ChangePasswordCommand command) {
        var userOptional = userRepository.findById(command.userId());
        if (userOptional.isEmpty()) { throw new RuntimeException("User not found with ID: " + command.userId());}
        var user = userOptional.get();
        if (!hashingService.matches(command.currentPassword(), user.getPassword())) { throw new RuntimeException("Current password is incorrect"); }
        user.setPassword(hashingService.encode(command.newPassword()));
        var savedUser = userRepository.save(user);
        return Optional.of(savedUser);
    }

    @Override
    public Optional<User> handle(ChangeEmailCommand command) {
        var userOptional = userRepository.findById(command.userId());
        if (userOptional.isEmpty()) { throw new RuntimeException("User not found with ID: " + command.userId()); }
        var user = userOptional.get();
        if (!hashingService.matches(command.password(), user.getPassword())) { throw new RuntimeException("Password is incorrect"); }
        if (userRepository.existsByEmail(command.newEmail())) { throw new RuntimeException("Email already exists: " + command.newEmail()); }
        user.setEmail(command.newEmail());
        var savedUser = userRepository.save(user);
        return Optional.of(savedUser);
    }

    @Override
    public boolean deleteUser(Long userId) {
        log.info("Attempting to delete user with ID: {}", userId);
        var userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            log.warn("Failed to delete user: User with ID {} not found", userId);
            return false;
        }

        try {
            userRepository.deleteById(userId);
            log.info("User with ID: {} deleted successfully", userId);
            return true;
        } catch (Exception e) {
            log.error("Error deleting user with ID: {}", userId, e);
            throw new RuntimeException("Failed to delete user: " + e.getMessage());
        }
    }

    @Override
    public Optional<User> handle(RegisterCarrierCommand command) {
        if (userRepository.existsByEmail(command.username())) {
            throw new RuntimeException("Email already exists");
        }

        // Obtener los roles (asegurándonos que incluya ROLE_CARRIER)
        var roles = command.roles().stream()
                .map(role -> roleRepository.findByName(role.getName())
                    .orElseThrow(() -> new RuntimeException("Role name not found")))
                .collect(Collectors.toList());

        // Verificar que al menos tenga el rol CARRIER
        boolean hasCarrierRole = roles.stream()
                .anyMatch(role -> role.getName() == Roles.ROLE_CARRIER);

        if (!hasCarrierRole) {
            roles.add(roleRepository.findByName(Roles.ROLE_CARRIER)
                    .orElseThrow(() -> new RuntimeException("Carrier role not found")));
        }

        // Crear el usuario carrier con referencia al manager que lo creó
        var user = new User(command.username(), hashingService.encode(command.password()), roles, command.managerId());
        var savedUser = userRepository.save(user);

        // Publicar evento de usuario creado
        try {
            UserCreatedEvent event = new UserCreatedEvent(savedUser.getId(), savedUser.getEmail());
            streamBridge.send("user-events", event);
            log.info("User created event published for carrier userId: {}, created by manager: {}",
                    savedUser.getId(), command.managerId());
        } catch (Exception e) {
            log.error("Failed to publish UserCreatedEvent for userId: {}", savedUser.getId(), e);
        }

        return Optional.of(savedUser);
    }
}

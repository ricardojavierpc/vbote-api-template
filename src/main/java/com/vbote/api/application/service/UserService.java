package com.vbote.api.application.service;

import com.vbote.api.domain.exception.UserAlreadyExistsException;
import com.vbote.api.domain.model.User;
import com.vbote.api.domain.port.in.UserUseCase;
import com.vbote.api.domain.port.out.PasswordEncoder;
import com.vbote.api.domain.port.out.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service  // ESto lo registra como un bean
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserService implements UserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User createUser(User user) {
        log.info("Creating user with username: {}", user.getUsername());

        if(userRepository.existsByUsername(user.getUsername())) {
            throw new UserAlreadyExistsException(user.getUsername());
        }

        // Encode password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Set default values
        user.setBlocked(Optional.ofNullable(user.getBlocked()).orElse(false));
        user.setRole(Optional.ofNullable(user.getRole()).orElse(User.Role.USER));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);
        log.info("User created successfully with id: {}", savedUser.getId());
        return savedUser;
    }

    /*
     * Transactional readOnly para optimizar la consulta
     */
    @Override
    @Transactional(readOnly = true)
    public List<User> getAllUsers(String userName, User.Role role, Boolean bloqued) {
        log.debug("Getting all users with filters - username: {}, role: {}, bloqued: {}",
                userName, role, bloqued);
        return userRepository.findAllWithFilters(userName, role, bloqued);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> getUserById(Long id) {
        log.debug("Getting user by id: {}", id);
        return userRepository.findById(id);
    }

    @Override
    public User updateUser(Long id, User user) {
        log.info("Updating user with id: {}", id);

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserAlreadyExistsException(user.getUsername()));

        if (!existingUser.getUsername().equals(user.getUsername()) &&
                userRepository.existsByUsername(user.getUsername())) {
            throw new UserAlreadyExistsException(user.getUsername());
        }

        existingUser.setUsername(user.getUsername());
        existingUser.setRole(user.getRole());
        existingUser.setBlocked(user.getBlocked());

        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        existingUser.markAsUpdated();

        User updatedUser = userRepository.save(existingUser);
        log.info("User updated successfully with id: {}", updatedUser.getId());

        return updatedUser;
    }

    @Override
    public User blockUser(Long id) {
        log.info("Blocking user with id: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserAlreadyExistsException("User not found with id: " + id));

        user.block();
        User blockedUser = userRepository.save(user);
        log.info("User blocked successfully with id: {}", blockedUser.getId());
        return blockedUser;
    }

    @Override
    public User unblockUser(Long id) {
        log.info("Unblocking user with id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserAlreadyExistsException("User not found with id: " + id));
        user.unblock();
        User unblockedUser = userRepository.save(user);
        log.info("User unblocked successfully with id: {}", unblockedUser.getId());
        return unblockedUser;
    }
}

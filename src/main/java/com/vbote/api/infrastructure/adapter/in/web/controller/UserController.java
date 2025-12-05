package com.vbote.api.infrastructure.adapter.in.web.controller;

import com.vbote.api.domain.exception.UserNotFoundException;
import com.vbote.api.domain.model.User;
import com.vbote.api.domain.port.in.UserUseCase;
import com.vbote.api.infrastructure.adapter.in.web.dto.UserDto;
import com.vbote.api.infrastructure.adapter.in.web.mapper.UserWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Users", description = "User management API")
public class UserController {

    private final UserUseCase userUseCase;
    private final UserWebMapper mapper;

    @PostMapping
    @Operation(summary = "Create a new user")
    public ResponseEntity<UserDto.Response> createUser(
            @Valid @RequestBody UserDto.CreateRequest request) {
        log.info("REST request to create user: {}", request.getUsername());

        User user = mapper.toDomain(request);
        User createdUser = userUseCase.createUser(user);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapper.toResponse(createdUser));
    }

    @GetMapping
    @Operation(summary = "Get all users with optional filters")
    public ResponseEntity<List<UserDto.Response>> getAllUsers(
            @Parameter(description = "Filter by username (partial match)")
            @RequestParam(required = false) String username,
            @Parameter(description = "Filter by role (ADMIN or USER)")
            @RequestParam(required = false) String role,
            @Parameter(description = "Filter by blocked status")
            @RequestParam(required = false) Boolean blocked) {
        log.debug("REST request to get all users with filters");

        User.Role userRole = null;
        if (role != null && !role.isEmpty()) {
            try {
                userRole = User.Role.valueOf(role.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid role filter: {}", role);
            }
        }

        List<User> users = userUseCase.getAllUsers(username, userRole, blocked);
        return ResponseEntity.ok(mapper.toResponseList(users));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a user by ID")
    public ResponseEntity<UserDto.Response> getUserById(
            @Parameter(description = "User ID")
            @PathVariable Long id) {
        log.debug("REST request to get user by id: {}", id);

        return userUseCase.getUserById(id)
                .map(user -> ResponseEntity.ok(mapper.toResponse(user)))
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a user completely")
    public ResponseEntity<UserDto.Response> updateUser(
            @Parameter(description = "User ID")
            @PathVariable Long id,
            @Valid @RequestBody UserDto.UpdateRequest request) {
        log.info("REST request to update user with id: {}", id);

        User user = mapper.toDomain(request);
        User updatedUser = userUseCase.updateUser(id, user);

        return ResponseEntity.ok(mapper.toResponse(updatedUser));
    }

    @PatchMapping("/{id}/block")
    @Operation(summary = "Block a user")
    public ResponseEntity<UserDto.Response> blockUser(
            @Parameter(description = "User ID")
            @PathVariable Long id) {
        log.info("REST request to block user with id: {}", id);

        User blockedUser = userUseCase.blockUser(id);
        return ResponseEntity.ok(mapper.toResponse(blockedUser));
    }

    @PatchMapping("/{id}/unblock")
    @Operation(summary = "Unblock a user")
    public ResponseEntity<UserDto.Response> unblockUser(
            @Parameter(description = "User ID")
            @PathVariable Long id) {
        log.info("REST request to unblock user with id: {}", id);

        User unblockedUser = userUseCase.unblockUser(id);
        return ResponseEntity.ok(mapper.toResponse(unblockedUser));
    }
}

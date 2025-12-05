package com.vbote.api.infrastructure.adapter.in.web.controller;

import com.vbote.api.domain.model.Session;
import com.vbote.api.domain.port.in.SessionUseCase;
import com.vbote.api.infrastructure.adapter.in.web.dto.SessionDto;
import com.vbote.api.infrastructure.adapter.in.web.mapper.SessionWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Sessions", description = "Session management API")
public class SessionController {

    private final SessionUseCase sessionUseCase;
    private final SessionWebMapper mapper;

    @PostMapping("/login")
    @Operation(summary = "Login - Create a new session")
    public ResponseEntity<SessionDto.LoginResponse> login(
            @Valid @RequestBody SessionDto.LoginRequest request,
            HttpServletRequest httpRequest) {
        log.info("REST request to login user: {}", request.getUsername());

        String ipAddress = getClientIpAddress(httpRequest);
        Session session = sessionUseCase.login(request.getUsername(), request.getPassword(), ipAddress);

        return ResponseEntity.ok(mapper.toLoginResponse(session));
    }

    @GetMapping
    @Operation(summary = "Get all active sessions")
    public ResponseEntity<List<SessionDto.Response>> getActiveSessions() {
        log.debug("REST request to get all active sessions");

        List<Session> sessions = sessionUseCase.getActiveSessions();
        return ResponseEntity.ok(mapper.toResponseList(sessions));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get active sessions for a specific user")
    public ResponseEntity<List<SessionDto.Response>> getActiveSessionsByUserId(
            @Parameter(description = "User ID")
            @PathVariable Long userId) {
        log.debug("REST request to get active sessions for user id: {}", userId);

        List<Session> sessions = sessionUseCase.getActiveSessionsByUserId(userId);
        return ResponseEntity.ok(mapper.toResponseList(sessions));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout - Close current session")
    public ResponseEntity<SessionDto.LogoutResponse> logout(
            @RequestHeader("Authorization") String authHeader) {
        log.info("REST request to logout");

        String token = extractToken(authHeader);
        sessionUseCase.logout(token);

        return ResponseEntity.ok(SessionDto.LogoutResponse.builder()
                .message("Logout successful")
                .sessionsClosedCount(1)
                .build());
    }

    @DeleteMapping("/user/{userId}")
    @Operation(summary = "Close all sessions for a user")
    public ResponseEntity<SessionDto.LogoutResponse> closeAllUserSessions(
            @Parameter(description = "User ID")
            @PathVariable Long userId) {
        log.info("REST request to close all sessions for user id: {}", userId);

        int closedCount = sessionUseCase.closeAllUserSessions(userId);

        return ResponseEntity.ok(SessionDto.LogoutResponse.builder()
                .message("All sessions closed for user")
                .sessionsClosedCount(closedCount)
                .build());
    }

    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return authHeader;
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }

}

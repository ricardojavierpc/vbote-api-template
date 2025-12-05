package com.vbote.api.application.service;

import com.vbote.api.domain.exception.InvalidCredentialsException;
import com.vbote.api.domain.exception.SessionNotFoundException;
import com.vbote.api.domain.exception.UserBlockedException;
import com.vbote.api.domain.exception.UserNotFoundException;
import com.vbote.api.domain.model.Session;
import com.vbote.api.domain.model.User;
import com.vbote.api.domain.port.in.SessionUseCase;
import com.vbote.api.domain.port.out.PasswordEncoder;
import com.vbote.api.domain.port.out.SessionRepository;
import com.vbote.api.domain.port.out.TokenProvider;
import com.vbote.api.domain.port.out.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SessionServlce implements SessionUseCase {

    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;

    @Override
    public Session login(String username, String password, String ipAddress) {
        log.info("Login attempt for user: {} from IP: {}", username, ipAddress);

        User user = userRepository.findByUsername(username)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("Invalid password for user: {}", username);
            throw new InvalidCredentialsException();
        }

        String token = tokenProvider.generateToken(user);

        Session session = Session.builder()
                .user(user)
                .token("token")
                .ipAddress(ipAddress)
                .build();

        Session savedSession = sessionRepository.save(session);
        log.info("Login succesful for user {} session id: {}", username, savedSession.getId());
        return  savedSession;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Session> getActiveSessions() {
        log.debug("Getting all active sessions");
        return sessionRepository.findAllActive();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Session> getActiveSessionsByUserId(Long userId) {
        log.debug("Getting active sessions for user id: {}", userId);

        if (userRepository.findById(userId).isEmpty()) {
            throw new UserNotFoundException(userId);
        }

        return sessionRepository.findAllActiveByUserId(userId);
    }

    @Override
    public void logout(String token) {
        log.info("Logout attempt for token");

        Session session = sessionRepository.findByToken(token)
                .orElseThrow(() -> new SessionNotFoundException(token));

        session.deactivate();
        sessionRepository.save(session);
        log.info("Logout successful for session id: {}", session.getId());
    }

    @Override
    public int closeAllUserSessions(Long userId) {
        log.info("Closing all sessions for user id: {}", userId);

        if (userRepository.findById(userId).isEmpty()) {
            throw new UserNotFoundException(userId);
        }

        int closedSessions = sessionRepository.deactivateAllByUserId(userId);
        log.info("Closed {} sessions for user id: {}", closedSessions, userId);
        return closedSessions;
    }

    @Override
    @Transactional(readOnly = true)
    public Session validateSession(String token) {
        log.debug("Validating session token");

        if (!tokenProvider.validateToken(token)) {
            throw new SessionNotFoundException(token);
        }

        Session session = sessionRepository.findByToken(token)
                .orElseThrow(() -> new SessionNotFoundException(token));

        if (!session.isActive()) {
            throw new SessionNotFoundException(token);
        }

        if (!session.getUser().canLogin()) {
            throw new UserBlockedException(session.getUser().getUsername());
        }

        return session;
    }
}

package com.vbote.api.domain.port.in;

import com.vbote.api.domain.model.Session;

import java.util.List;

public interface SessionUseCase {

    Session login(String username, String password, String ipAddress);

    List<Session> getActiveSessions();

    List<Session> getActiveSessionsByUserId(Long userId);

    void logout(String token);

    int closeAllUserSessions(Long userId);

    Session validateSession(String token);

}

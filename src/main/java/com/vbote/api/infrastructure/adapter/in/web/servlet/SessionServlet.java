package com.vbote.api.infrastructure.adapter.in.web.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.vbote.api.domain.model.Session;
import com.vbote.api.domain.port.in.SessionUseCase;
import com.vbote.api.infrastructure.adapter.in.web.dto.SessionDto;
import com.vbote.api.infrastructure.adapter.in.web.mapper.SessionWebMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "SessionServlet", urlPatterns = {"/servlet/sessions/*"})
@Slf4j
public class SessionServlet extends HttpServlet {

    @Autowired
    private SessionUseCase sessionUseCase;

    @Autowired
    private SessionWebMapper mapper;

    private ObjectMapper objectMapper;

    @Override
    public void init() throws ServletException {
        super.init();
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        log.debug("SessionServlet - GET request: {}", req.getPathInfo());

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String pathInfo = req.getPathInfo();

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                // GET /servlet/sessions - Lista todas las sesiones activas
                List<Session> sessions = sessionUseCase.getActiveSessions();
                List<SessionDto.Response> response = mapper.toResponseList(sessions);

                resp.setStatus(HttpServletResponse.SC_OK);
                objectMapper.writeValue(resp.getWriter(), response);
            } else if (pathInfo.startsWith("/user/")) {
                // GET /servlet/sessions/user/{userId} - Sesiones de un usuario
                String userIdStr = pathInfo.substring("/user/".length());
                Long userId = Long.parseLong(userIdStr);

                List<Session> sessions = sessionUseCase.getActiveSessionsByUserId(userId);
                List<SessionDto.Response> response = mapper.toResponseList(sessions);

                resp.setStatus(HttpServletResponse.SC_OK);
                objectMapper.writeValue(resp.getWriter(), response);
            } else {
                sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
            }
        } catch (NumberFormatException e) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid user ID format");
        } catch (Exception e) {
            log.error("Error in SessionServlet GET", e);
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        log.debug("SessionServlet - POST request: {}", req.getPathInfo());

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String pathInfo = req.getPathInfo();

        try {
            if (pathInfo != null && pathInfo.equals("/login")) {
                // POST /servlet/sessions/login - Login
                SessionDto.LoginRequest request = objectMapper.readValue(req.getReader(), SessionDto.LoginRequest.class);
                String ipAddress = getClientIpAddress(req);
                Session session = sessionUseCase.login(request.getUsername(), request.getPassword(), ipAddress);

                resp.setStatus(HttpServletResponse.SC_OK);
                objectMapper.writeValue(resp.getWriter(), mapper.toLoginResponse(session));
            } else if (pathInfo != null && pathInfo.equals("/logout")) {
                // POST /servlet/sessions/logout - Logout
                String authHeader = req.getHeader("Authorization");
                String token = extractToken(authHeader);
                sessionUseCase.logout(token);

                resp.setStatus(HttpServletResponse.SC_OK);
                objectMapper.writeValue(resp.getWriter(), SessionDto.LogoutResponse.builder()
                        .message("Logout successful")
                        .sessionsClosedCount(1)
                        .build());
            } else {
                sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
            }
        } catch (Exception e) {
            log.error("Error in SessionServlet POST", e);
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        log.debug("SessionServlet - DELETE request: {}", req.getPathInfo());

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String pathInfo = req.getPathInfo();

        try {
            if (pathInfo != null && pathInfo.startsWith("/user/")) {
                // DELETE /servlet/sessions/user/{userId} - Cerrar todas las sesiones
                String userIdStr = pathInfo.substring("/user/".length());
                Long userId = Long.parseLong(userIdStr);

                int closedCount = sessionUseCase.closeAllUserSessions(userId);

                resp.setStatus(HttpServletResponse.SC_OK);
                objectMapper.writeValue(resp.getWriter(), SessionDto.LogoutResponse.builder()
                        .message("All sessions closed for user")
                        .sessionsClosedCount(closedCount)
                        .build());
            } else {
                sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
            }
        } catch (NumberFormatException e) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid user ID format");
        } catch (Exception e) {
            log.error("Error in SessionServlet DELETE", e);
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
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

    private void sendError(HttpServletResponse resp, int status, String message) throws IOException {
        resp.setStatus(status);
        resp.getWriter().write("{\"error\": \"" + message + "\"}");
    }
}

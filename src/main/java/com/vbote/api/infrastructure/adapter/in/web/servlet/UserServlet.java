package com.vbote.api.infrastructure.adapter.in.web.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.vbote.api.domain.model.User;
import com.vbote.api.domain.port.in.UserUseCase;
import com.vbote.api.infrastructure.adapter.in.web.dto.UserDto;
import com.vbote.api.infrastructure.adapter.in.web.mapper.UserWebMapper;
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

@WebServlet(name = "UserServlet", urlPatterns = {"/servlet/users/*"})
@Slf4j
public class UserServlet extends HttpServlet {

    @Autowired
    private UserUseCase userUseCase;

    @Autowired
    private UserWebMapper mapper;

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
        log.debug("UserServlet - GET request: {}", req.getPathInfo());

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String pathInfo = req.getPathInfo();

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                // GET /servlet/users - Lista todos
                String username = req.getParameter("username");
                String role = req.getParameter("role");
                String blockedStr = req.getParameter("blocked");
                Boolean blocked = blockedStr != null ? Boolean.parseBoolean(blockedStr) : null;

                User.Role userRole = null;
                if (role != null && !role.isEmpty()) {
                    try {
                        userRole = User.Role.valueOf(role.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        log.warn("Invalid role filter: {}", role);
                    }
                }

                List<User> users = userUseCase.getAllUsers(username, userRole, blocked);
                List<UserDto.Response> response = mapper.toResponseList(users);

                resp.setStatus(HttpServletResponse.SC_OK);
                objectMapper.writeValue(resp.getWriter(), response);
            } else {
                // GET /servlet/users/{id} - Obtener por ID
                Long id = extractIdFromPath(pathInfo);
                userUseCase.getUserById(id)
                        .ifPresentOrElse(
                                user -> {
                                    try {
                                        resp.setStatus(HttpServletResponse.SC_OK);
                                        objectMapper.writeValue(resp.getWriter(), mapper.toResponse(user));
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                },
                                () -> {
                                    try {
                                        sendError(resp, HttpServletResponse.SC_NOT_FOUND, "User not found");
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                        );
            }
        } catch (NumberFormatException e) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid user ID format");
        } catch (Exception e) {
            log.error("Error in UserServlet GET", e);
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        log.debug("UserServlet - POST request");

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            UserDto.CreateRequest request = objectMapper.readValue(req.getReader(), UserDto.CreateRequest.class);
            User user = mapper.toDomain(request);
            User createdUser = userUseCase.createUser(user);

            resp.setStatus(HttpServletResponse.SC_CREATED);
            objectMapper.writeValue(resp.getWriter(), mapper.toResponse(createdUser));
        } catch (Exception e) {
            log.error("Error in UserServlet POST", e);
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        log.debug("UserServlet - PUT request: {}", req.getPathInfo());

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "User ID is required");
            return;
        }

        try {
            Long id = extractIdFromPath(pathInfo);
            UserDto.UpdateRequest request = objectMapper.readValue(req.getReader(), UserDto.UpdateRequest.class);
            User user = mapper.toDomain(request);
            User updatedUser = userUseCase.updateUser(id, user);

            resp.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(resp.getWriter(), mapper.toResponse(updatedUser));
        } catch (NumberFormatException e) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid user ID format");
        } catch (Exception e) {
            log.error("Error in UserServlet PUT", e);
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private Long extractIdFromPath(String pathInfo) {
        String idStr = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;
        if (idStr.contains("/")) {
            idStr = idStr.split("/")[0];
        }
        return Long.parseLong(idStr);
    }

    private void sendError(HttpServletResponse resp, int status, String message) throws IOException {
        resp.setStatus(status);
        resp.getWriter().write("{\"error\": \"" + message + "\"}");
    }
}

package com.vbote.api.infrastructure.adapter.in.web.servlet;

import com.vbote.api.domain.model.User;
import com.vbote.api.domain.port.in.UserUseCase;
import com.vbote.api.infrastructure.adapter.in.web.dto.UserDto;
import com.vbote.api.infrastructure.adapter.in.web.mapper.UserWebMapper;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UserServletTest {

    @Mock
    private UserUseCase userUseCase;

    @Mock
    private UserWebMapper mapper;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private UserServlet userServlet;

    private User createUser(Long id, String username, User.Role role, Boolean blocked) {
        return User.builder()
                .id(id)
                .username(username)
                .password("encoded_password")
                .role(role)
                .blocked(blocked)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private UserDto.Response createUserResponse(Long id, String username, String role, Boolean blocked) {
        return UserDto.Response.builder()
                .id(id)
                .username(username)
                .role(role)
                .blocked(blocked)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }


    @Test
    void doGet() {
    }

    @Test
    void doPost() {
    }

    @Test
    void doPut() {
    }
}
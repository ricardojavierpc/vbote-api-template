package com.vbote.api.infrastructure.adapter.in.web.mapper;

import com.vbote.api.domain.model.Session;
import com.vbote.api.infrastructure.adapter.in.web.dto.SessionDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;
import java.util.List;

@Mapper(componentModel = "spring")
public interface SessionWebMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    SessionDto.Response toResponse(Session session);

    List<SessionDto.Response> toResponseList(List<Session> sessions);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "role", source = "user.role")
    @Mapping(target = "expiresAt", expression = "java(calculateExpiresAt(session))")
    SessionDto.LoginResponse toLoginResponse(Session session);

    default LocalDateTime calculateExpiresAt(Session session) {
        return session.getCreatedAt().plusHours(24);
    }
}

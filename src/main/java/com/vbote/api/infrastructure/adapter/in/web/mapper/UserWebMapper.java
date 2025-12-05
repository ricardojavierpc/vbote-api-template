package com.vbote.api.infrastructure.adapter.in.web.mapper;

import com.vbote.api.domain.model.User;
import com.vbote.api.infrastructure.adapter.in.web.dto.UserDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserWebMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "blocked", constant = "false")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "role", expression = "java(mapRoleToDomain(request.getRole()))")
    User toDomain(UserDto.CreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "role", expression = "java(mapRoleToDomain(request.getRole()))")
    User toDomain(UserDto.UpdateRequest request);

    @Mapping(target = "role", expression = "java(user.getRole() != null ? user.getRole().name() : null)")
    UserDto.Response toResponse(User user);

    List<UserDto.Response> toResponseList(List<User> users);

    default User.Role mapRoleToDomain(String role) {
        if (role == null || role.isEmpty()) {
            return User.Role.USER;
        }
        try {
            return User.Role.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            return User.Role.USER;
        }
    }
}

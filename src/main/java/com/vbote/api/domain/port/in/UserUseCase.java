package com.vbote.api.domain.port.in;

import com.vbote.api.domain.model.User;

import java.util.List;
import java.util.Optional;

public interface UserUseCase {

    User createUser(User user);

    List<User> getAllUsers(String userName, User.Role role, Boolean bloqued);

    Optional<User> getUserById(Long id);

    User updateUser(Long id, User user);

    User blockUser(Long id);

    User unblockUser(Long id);
}

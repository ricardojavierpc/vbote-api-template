package com.vbote.api.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User  {

    private Long id;
    private String username;
    private String password;
    private Role role;
    private Boolean blocked;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum Role {
        ADMIN,
        USER
    }

    public boolean canLogin() {
        return !Boolean.TRUE.equals(blocked);
    }

    public void block() {
        this.blocked = true;
        this.updatedAt = LocalDateTime.now();
    }

    public void unblock() {
        this.blocked = false;
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsUpdated() {
        this.updatedAt = LocalDateTime.now();
    }

}

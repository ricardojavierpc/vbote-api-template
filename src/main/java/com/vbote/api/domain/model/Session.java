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
public class Session {

    private Long id;
    private User user;
    private String token;
    private String ipAddress;
    private LocalDateTime createdAt;
    private Boolean active;

    public void deactivate() {
        this.active = false;
    }

    public boolean isActive() {
        return Boolean.TRUE.equals(active);
    }

    public boolean belongsTo(Long userId) {
        return user != null && user.getId() != null && user.getId().equals(userId);
    }
}

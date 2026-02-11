package org.example.events;

import lombok.Data;

@Data
public class UserCreatedEvents {
    private Long userId;
    private String email;
    private String name;
}

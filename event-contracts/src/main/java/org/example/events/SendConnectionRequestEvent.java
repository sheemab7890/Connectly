package org.example.events;

import lombok.AllArgsConstructor;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SendConnectionRequestEvent {
    private Long senderId;
    private Long receiverId;
}

package com.sheemab.linkedin.user_service.Services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sheemab.linkedin.user_service.DTO.LogInResponseDto;
import com.sheemab.linkedin.user_service.DTO.LoginRequestDto;
import com.sheemab.linkedin.user_service.DTO.SignUpRequestDto;
import com.sheemab.linkedin.user_service.DTO.UserDto;
import com.sheemab.linkedin.user_service.Entities.OutboxEvent;
import com.sheemab.linkedin.user_service.Entities.User;
import com.sheemab.linkedin.user_service.Exception.BadRequestException;
import com.sheemab.linkedin.user_service.Exception.ResourceNotFoundException;
import com.sheemab.linkedin.user_service.Repositories.OutboxEventRepository;
import com.sheemab.linkedin.user_service.Repositories.UserRepository;
import com.sheemab.linkedin.user_service.Utils.PasswordUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.events.UserCreatedEvents;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final JwtService jwtService;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public UserDto signUp(SignUpRequestDto signUpRequestDto) {

        log.info("Starting signup for email={}", signUpRequestDto.getEmail());

        if (userRepository.existsByEmail(signUpRequestDto.getEmail())) {
            log.warn("Signup failed - email already exists: {}", signUpRequestDto.getEmail());
            throw new BadRequestException("Email is already in use: " + signUpRequestDto.getEmail());
        }

        User user = modelMapper.map(signUpRequestDto, User.class);
        user.setPassword(PasswordUtils.hashPassword(signUpRequestDto.getPassword()));

        User savedUser = userRepository.save(user);

        log.info("User saved successfully. userId={}", savedUser.getId());

        UserCreatedEvents event = new UserCreatedEvents();
        event.setUserId(savedUser.getId());
        event.setEmail(savedUser.getEmail());
        event.setName(savedUser.getName());

        String payload;
        try {
            payload = objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize UserCreatedEvent for userId={}", savedUser.getId(), e);
            throw new RuntimeException("Failed to serialize UserCreatedEvent", e);
        }

        OutboxEvent outbox = new OutboxEvent();
        outbox.setId(UUID.randomUUID());
        outbox.setAggregateType("USER");
        outbox.setAggregateId(savedUser.getId().toString());
        outbox.setEventType("UserCreatedEvent");
        outbox.setPayload(payload);
        outbox.setStatus("PENDING");
        outbox.setCreatedAt(Instant.now());

        log.info("Saving outbox event. outboxId={}, eventType={}, aggregateId={}",
                outbox.getId(),
                outbox.getEventType(),
                outbox.getAggregateId()
        );

        outboxEventRepository.saveAndFlush(outbox);

        log.info("Outbox event saved successfully. outboxId={}", outbox.getId());

        return modelMapper.map(savedUser, UserDto.class);
    }


    public LogInResponseDto logIn(LoginRequestDto loginRequestDto) {
        User user = userRepository.findByEmail(loginRequestDto.getEmail()).orElseThrow(
                ()-> new ResourceNotFoundException("User not exist with email:"+loginRequestDto.getEmail())
        );
        boolean isMatched = PasswordUtils.checkPassword(loginRequestDto.getPassword(), user.getPassword());

        if(!isMatched){
            throw new BadRequestException("Incorrect password");
        }
        String accessToken = jwtService.generateAccessToken(user);
       return new LogInResponseDto(accessToken);
    }
}

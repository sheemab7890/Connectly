package com.sheemab.linkedin.user_service.Advices;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ApiResponse<T>{

    @JsonFormat(pattern = "hh:mm:ss dd-MM-yyyy")
    private LocalDateTime timeStamp;
    private ApiError errors;
    private T data;

    public ApiResponse( ) {
        this.timeStamp = LocalDateTime.now();
    }

    public ApiResponse(ApiError errors) {
        this();
        this.errors = errors;
    }

    public ApiResponse(T data) {
        this();
        this.data = data;
    }


}

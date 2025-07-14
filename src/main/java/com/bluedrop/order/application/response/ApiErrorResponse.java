package com.bluedrop.order.application.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class ApiErrorResponse {
    private String error;
    private String message;
    private LocalDateTime timestamp;

    public ApiErrorResponse(String error, String message) {
        this.error = error;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

}

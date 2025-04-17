package com.jobflow.user_service.handler;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseError {

    private String message;

    private int status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime time;

    public static ResponseError buildResponseError(String message, int status) {
        return new ResponseError(message, status, LocalDateTime.now());
    }
}

package com.jobflow.user_service.handler;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response error details")
public class ResponseError {

    @Schema(description = "Error message", example = "Error message")
    private String message;

    @Schema(description = "Error status", example = "000")
    private int status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    @Schema(description = "Error occurrence time", example = "2021-01-01 01:01")
    private LocalDateTime time;

    public static ResponseError buildResponseError(String message, int status) {
        return new ResponseError(message, status, LocalDateTime.now());
    }
}

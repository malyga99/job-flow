package com.jobflow.notification_service.notification.history;

import com.jobflow.notification_service.handler.ResponseError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationHistoryController {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationHistoryController.class);

    private final NotificationHistoryService notificationHistoryService;

    @Operation(
            summary = "Find all notifications of the current user",
            description = "Searches all sent notifications of the current user with pagination and sorting." +
                          " Returns only successfully sent notifications",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Notifications successfully received"),

                    @ApiResponse(responseCode = "401", description = "Authentication exception",
                            content = @Content(mediaType = "application/json", schema =
                            @Schema(implementation = ResponseError.class)))
            }
    )
    @GetMapping("/my")
    public ResponseEntity<Page<NotificationHistoryDto>> findMy(
            @RequestParam(value = "pageNumber", defaultValue = "0") @Parameter(description = "Page number", example = "1") int pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "10") @Parameter(description = "Page size", example = "10") int pageSize,
            @RequestParam(value = "direction", defaultValue = "DESC") @Parameter(description = "Sorting direction", example = "DESC") Direction direction
    ) {
        LOGGER.info("[GET] Request for find all notifications of the current user - pageNumber: {}, pageSize: {}, direction: {}",
                pageNumber, pageSize, direction);
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(direction, "createdAt"));

        return ResponseEntity.ok(notificationHistoryService.findMy(pageable));
    }
}

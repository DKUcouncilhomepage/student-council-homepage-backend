package com.dku.council.global.error;

import com.dku.council.global.error.exception.LocalizedMessageException;
import lombok.Getter;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

@Getter
public class ErrorResponseDto {
    private final String timestamp;
    private final String trackingId;
    private final HttpStatus status;
    private final String code;
    private final String message;


    public ErrorResponseDto(MessageSource messageSource, Locale locale, LocalizedMessageException e) {
        this.timestamp = LocalDateTime.now().toString();
        this.trackingId = UUID.randomUUID().toString();
        this.status = e.getStatus();
        this.code = e.getClass().getSimpleName();

        String messageId = e.getMessageId();
        this.message = messageSource.getMessage(messageId, e.getArguments(), locale);
    }

    public ErrorResponseDto(Exception e) {
        this.timestamp = LocalDateTime.now().toString();
        this.trackingId = UUID.randomUUID().toString();
        this.status = HttpStatus.INTERNAL_SERVER_ERROR;
        this.code = e.getClass().getSimpleName();
        this.message = e.getMessage();
    }
}
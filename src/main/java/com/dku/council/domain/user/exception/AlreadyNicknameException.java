package com.dku.council.domain.user.exception;

import com.dku.council.global.error.exception.LocalizedMessageException;
import org.springframework.http.HttpStatus;

public class AlreadyNicknameException extends LocalizedMessageException {
    public AlreadyNicknameException() {
        super(HttpStatus.BAD_REQUEST, "already.nickname");
    }
}
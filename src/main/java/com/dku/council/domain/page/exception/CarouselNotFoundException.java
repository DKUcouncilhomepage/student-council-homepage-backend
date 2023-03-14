package com.dku.council.domain.page.exception;

import com.dku.council.global.error.exception.LocalizedMessageException;
import org.springframework.http.HttpStatus;

public class CarouselNotFoundException extends LocalizedMessageException {
    public CarouselNotFoundException() {
        super(HttpStatus.NOT_FOUND, "notfound.carousel");
    }
}

package com.dku.council.domain.user.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

@Getter
@RequiredArgsConstructor
public class SignupAuth {
    private final Instant expiresAt;
    private final Object value;
}

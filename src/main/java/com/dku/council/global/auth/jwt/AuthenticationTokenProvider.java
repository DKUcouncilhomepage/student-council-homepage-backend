package com.dku.council.global.auth.jwt;

import org.springframework.security.core.Authentication;

import javax.servlet.http.HttpServletRequest;

public interface AuthenticationTokenProvider {
    AuthenticationToken getTokenFromHeader(HttpServletRequest request);
    Authentication getAuthentication(String accessToken);

}

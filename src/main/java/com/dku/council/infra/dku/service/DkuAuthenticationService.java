package com.dku.council.infra.dku.service;

import com.dku.council.global.config.webclient.ChromeAgentWebClient;
import com.dku.council.global.util.WebUtil;
import com.dku.council.infra.dku.exception.DkuFailedLoginException;
import com.dku.council.infra.dku.model.DkuAuth;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// TODO 접속이 너무 몰리면 약간 지연하도록
// TODO 한번에 로그인 너무 많이 요청하면 blocking 당할지도?
// TODO 현재 동기식이지만, 비동기식으로 바꿔보기
@Service
@RequiredArgsConstructor
public class DkuAuthenticationService {

    private static final Pattern ERROR_ALERT_PATTERN = Pattern.compile("<li>\\s*<p\\s*class=\"warn\">\\s*(.*)\\s*</p>\\s*</li>");

    @ChromeAgentWebClient
    private final WebClient webClient;

    @Value("${dku.login.api-path}")
    private final String loginApiPath;

    /**
     * 단국대학교 웹정보 시스템에 로그인합니다.
     *
     * @param classId  아이디 (학번)
     * @param password 비밀번호
     * @return 토큰 쿠키가 포함된 DkuAuth
     */
    public DkuAuth login(String classId, String password) {
        MultiValueMap<String, String> cookies = new LinkedMultiValueMap<>();

        ResponseEntity<String> response = tryLogin(classId, password);
        HttpHeaders headers = response.getHeaders();
        addMappedCookies(cookies, headers);

        URI ssoLocation = headers.getLocation();
        response = trySSOAuth(ssoLocation, cookies);
        addMappedCookies(cookies, response.getHeaders());

        return new DkuAuth(cookies);
    }

    private void addMappedCookies(MultiValueMap<String, String> dest, HttpHeaders src) {
        List<ResponseCookie> cookies = WebUtil.extractCookies(src);
        for (ResponseCookie ent : cookies) {
            dest.add(ent.getName(), ent.getValue());
        }
    }

    private ResponseEntity<String> tryLogin(String classId, String password) {
        String param = String.format("username=%s&password=%s&tabIndex=0", classId, password);

        ResponseEntity<String> response;
        try {
            response = webClient.post()
                    .uri(loginApiPath)
                    .header("Origin", "https://webinfo.dankook.ac.kr")
                    .header("Referer", "https://webinfo.dankook.ac.kr/member/logon.do?returnurl=http://webinfo.dankook.ac.kr:80/main.do&sso=ok")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromValue(param))
                    .retrieve()
                    .toEntity(String.class)
                    .block();
        } catch (Throwable t) {
            throw new DkuFailedLoginException(t);
        }

        validateResponse(response);

        HttpStatus statusCode = response.getStatusCode();
        if (statusCode == HttpStatus.OK) {
            throwLoginFailedWithAlertMessage(response);
        }

        validateStatusCode(statusCode);
        return response;
    }

    private ResponseEntity<String> trySSOAuth(URI ssoURI, MultiValueMap<String, String> cookies) {
        ResponseEntity<String> response;

        try {
            response = webClient.post()
                    .uri(ssoURI)
                    .cookies(map -> map.addAll(cookies))
                    .header("Referer", "https://webinfo.dankook.ac.kr/")
                    .retrieve()
                    .toEntity(String.class)
                    .block();
        } catch (Throwable t) {
            throw new DkuFailedLoginException(t);
        }

        validateResponse(response);
        validateStatusCode(response.getStatusCode());
        return response;
    }

    private static void throwLoginFailedWithAlertMessage(ResponseEntity<String> response) {
        String responseBody = response.getBody();
        if (responseBody == null) {
            throw new DkuFailedLoginException();
        }

        String loginMessage = extractLoginMessage(responseBody);
        if (loginMessage != null) {
            throw new DkuFailedLoginException(loginMessage);
        } else {
            throw new DkuFailedLoginException();
        }
    }

    private static String extractLoginMessage(String html) {
        Matcher matcher = ERROR_ALERT_PATTERN.matcher(html);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private static void validateResponse(ResponseEntity<String> response) {
        if (response == null) {
            throw new DkuFailedLoginException(new NullPointerException("response"));
        }
    }

    private static void validateStatusCode(HttpStatus statusCode) {
        if (statusCode != HttpStatus.FOUND) {
            throw new DkuFailedLoginException(new ResponseStatusException(statusCode));
        }
    }
}

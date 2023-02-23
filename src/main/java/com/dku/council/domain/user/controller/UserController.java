package com.dku.council.domain.user.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "사용자", description = "사용자 인증 및 정보 관련 api")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    /**
     * 회원가입
     */
    @PostMapping
    public void signup() {
    }

    /**
     * 로그인
     */
    @PostMapping("/login")
    public void login() {

    }

    /**
     * 로그아웃
     * 서버에 같은 토큰으로 로그인 할 수 없게 로그아웃합니다.
     */
    @DeleteMapping
    public void logout() {

    }

    /**
     * 토큰 재발급
     */
    @PostMapping("/reissue")
    public void refreshToken() {

    }

    /**
     * 모든 학과 정보 가져오기
     */
    @GetMapping("/major")
    public void getAllMajors() {

    }

    /**
     * 비밀번호 변경
     */
    @PatchMapping("/password")
    public void changePassword() {

    }
}
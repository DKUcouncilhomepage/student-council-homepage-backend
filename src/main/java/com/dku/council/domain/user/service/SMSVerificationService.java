package com.dku.council.domain.user.service;

import com.dku.council.domain.user.exception.AlreadyPhoneException;
import com.dku.council.domain.user.exception.NotSMSAuthorizedException;
import com.dku.council.domain.user.exception.NotSMSSentException;
import com.dku.council.domain.user.exception.WrongSMSCodeException;
import com.dku.council.domain.user.model.SMSAuth;
import com.dku.council.domain.user.model.entity.User;
import com.dku.council.domain.user.repository.SignupAuthRepository;
import com.dku.council.domain.user.repository.UserRepository;
import com.dku.council.infra.nhn.service.SMSService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class SMSVerificationService {

    public static final String SMS_AUTH_NAME = "sms";
    public static final String SMS_AUTH_COMPLETE_SIGN = "OK";
    private static final Random RANDOM = new Random();

    private final Clock clock;
    private final MessageSource messageSource;
    private final DKUAuthService dkuAuthService;
    private final SMSService smsService;
    private final UserRepository userRepository;
    private final SignupAuthRepository smsAuthRepository;

    @Value("${app.auth.sms.digit-count}")
    private final int digitCount;

    /**
     * 회원가입 토큰을 기반으로 인증된 휴대폰 정보를 가져옵니다. 휴대폰 인증이 되어있지 않으면 Exception이 발생합니다.
     * 이 메서드는 회원가입 진행자를 대상으로 합니다.
     *
     * @param signupToken 회원가입 토큰
     * @throws NotSMSAuthorizedException 휴대폰 인증을 하지 않았을 경우
     */
    public String getPhoneNumber(String signupToken) throws NotSMSAuthorizedException {
        Instant now = Instant.now(clock);
        SMSAuth authObj = smsAuthRepository.getAuthPayload(signupToken, SMS_AUTH_NAME, SMSAuth.class, now)
                .orElseThrow(NotSMSSentException::new);

        if (!authObj.getCode().equals(SMS_AUTH_COMPLETE_SIGN)) {
            throw new NotSMSAuthorizedException();
        }

        return authObj.getPhone();
    }

    /**
     * 회원가입 토큰을 기반으로 인증된 휴대폰 정보를 삭제합니다. 중복 가입을 막기 위해
     * 회원가입이 끝나면 인증된 휴대폰 정보를 모두 삭제해야합니다.
     *
     * @param signupToken 회원가입 토큰
     */
    public boolean deleteSMSAuth(String signupToken) {
        return smsAuthRepository.deleteAuthPayload(signupToken, SMS_AUTH_NAME);
    }

    /**
     * 해당 전화번호로 SMS인증 메시지를 전송합니다.
     *
     * @param signupToken 회원가입 토큰
     * @param phoneNumber 전화번호
     */
    public void sendSMSCode(String signupToken, String phoneNumber) {
        dkuAuthService.getStudentInfo(signupToken);
        checkAlreadyPhone(phoneNumber);

        String code = generateDigitCode(digitCount);
        phoneNumber = phoneNumber.trim().replaceAll("-", "");

        Instant now = Instant.now(clock);
        smsAuthRepository.setAuthPayload(signupToken, SMS_AUTH_NAME, new SMSAuth(phoneNumber, code), now);

        Locale locale = LocaleContextHolder.getLocale();
        smsService.sendSMS(phoneNumber, messageSource.getMessage("sms.auth-message", new Object[]{code}, locale));
    }

    private void checkAlreadyPhone(String phone) {
        Optional<User> alreadyUser = userRepository.findByPhone(phone);
        if (alreadyUser.isPresent()) {
            throw new AlreadyPhoneException();
        }
    }

    /**
     * SMS코드가 정확한지 검사합니다. 틀릴 경우 Exception이 발생합니다.
     *
     * @param signupToken 회원가입 토큰
     * @param code        받은 SMS 코드
     */
    public void verifySMSCode(String signupToken, String code) {
        Instant now = Instant.now(clock);
        SMSAuth authObj = smsAuthRepository.getAuthPayload(signupToken, SMS_AUTH_NAME, SMSAuth.class, now)
                .orElseThrow(NotSMSSentException::new);

        if (!authObj.getCode().equals(code.trim())) {
            throw new WrongSMSCodeException();
        }

        SMSAuth newAuthObj = new SMSAuth(authObj.getPhone(), SMS_AUTH_COMPLETE_SIGN);
        smsAuthRepository.setAuthPayload(signupToken, SMS_AUTH_NAME, newAuthObj, now);
    }

    public static String generateDigitCode(int digitCount) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < digitCount; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        return sb.toString();
    }
}

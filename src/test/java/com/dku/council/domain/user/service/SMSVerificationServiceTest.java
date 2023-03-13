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
import com.dku.council.mock.UserMock;
import com.dku.council.util.ClockUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.time.Clock;
import java.util.Optional;

import static com.dku.council.domain.user.service.SMSVerificationService.SMS_AUTH_COMPLETE_SIGN;
import static com.dku.council.domain.user.service.SMSVerificationService.SMS_AUTH_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SMSVerificationServiceTest {

    @Mock
    private MessageSource messageSource;

    @Mock
    private DKUAuthService dkuAuthService;

    @Mock
    private SMSService smsService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SignupAuthRepository smsAuthRepository;

    private SMSVerificationService service;

    private final Clock clock = ClockUtil.create();
    private final String token = "token";
    private final int count = 6;
    private final String phone = "12-12-12";


    @BeforeEach
    public void setup() {
        service = new SMSVerificationService(clock, messageSource, dkuAuthService, smsService,
                userRepository, smsAuthRepository, count);
    }


    @Test
    @DisplayName("휴대폰 정보를 잘 가져오는지")
    void getPhoneNumber() {
        // given
        SMSAuth auth = new SMSAuth(phone, SMS_AUTH_COMPLETE_SIGN);
        when(smsAuthRepository.getAuthPayload(eq(token),
                eq(SMS_AUTH_NAME), eq(SMSAuth.class), any()))
                .thenReturn(Optional.of(auth));

        // when
        String phone = service.getPhoneNumber(token);

        // then
        assertThat(phone).isEqualTo(phone);
    }

    @Test
    @DisplayName("가져오기 실패 - 휴대폰 정보가 없으면 오류")
    void failedGetPhoneNumberByNotFound() {
        // given
        when(smsAuthRepository.getAuthPayload(eq(token), eq(SMS_AUTH_NAME),
                eq(SMSAuth.class), any()))
                .thenReturn(Optional.empty());

        // when & then
        assertThrows(NotSMSSentException.class, () ->
                service.getPhoneNumber(token));
    }

    @Test
    @DisplayName("가져오기 실패 - 인증되지 않은 경우")
    void failedGetPhoneNumberByNotAuthorized() {
        // given
        SMSAuth auth = new SMSAuth(phone, "CODE");
        when(smsAuthRepository.getAuthPayload(eq(token), eq(SMS_AUTH_NAME),
                eq(SMSAuth.class), any()))
                .thenReturn(Optional.of(auth));

        // when & then
        assertThrows(NotSMSAuthorizedException.class, () ->
                service.getPhoneNumber(token));
    }

    @Test
    @DisplayName("Auth 정보 삭제")
    void deleteSMSAuth() {
        // given
        when(smsAuthRepository.deleteAuthPayload(token, SMS_AUTH_NAME))
                .thenReturn(true);

        // when
        boolean result = service.deleteSMSAuth(token);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("SMS Code 전송")
    void sendSMSCode() {
        // given
        String actualPhone = phone.replaceAll("-", "");
        String authMessage = "Auth Message";
        when(userRepository.findByPhone(phone)).thenReturn(Optional.empty());
        when(messageSource.getMessage(eq("sms.auth-message"), any(), any())).thenReturn(authMessage);

        // when
        service.sendSMSCode(token, phone);

        // then
        verify(smsAuthRepository).setAuthPayload(eq(token),
                eq(SMS_AUTH_NAME), argThat(o -> {
                    SMSAuth auth = (SMSAuth) o;
                    assertThat(auth.getPhone()).isEqualTo(actualPhone);
                    assertThat(auth.getCode().length()).isEqualTo(count);
                    return true;
                }), any());
        verify(dkuAuthService).getStudentInfo(token);
        verify(smsService).sendSMS(actualPhone, authMessage);
    }

    @Test
    @DisplayName("SMS Code 전송 실패 - 이미 존재하는 번호")
    void failedSendSMSCodeByAlreadyUser() {
        // given
        User user = UserMock.createDummyMajor();
        when(userRepository.findByPhone(phone)).thenReturn(Optional.of(user));

        // when
        assertThrows(AlreadyPhoneException.class, () ->
                service.sendSMSCode(token, phone));
    }

    @Test
    @DisplayName("SMS 인증 성공")
    void verifySMSCode() {
        // given
        String code = "121314";
        SMSAuth auth = new SMSAuth(phone, code);
        when(smsAuthRepository.getAuthPayload(eq(token), eq(SMS_AUTH_NAME),
                eq(SMSAuth.class), any()))
                .thenReturn(Optional.of(auth));

        // then
        service.verifySMSCode(token, code);

        // then
        verify(smsAuthRepository).setAuthPayload(eq(token), eq(SMS_AUTH_NAME),
                argThat(o -> {
                    SMSAuth au = (SMSAuth) o;
                    assertThat(au.getPhone()).isEqualTo(phone);
                    assertThat(au.getCode()).isEqualTo(SMS_AUTH_COMPLETE_SIGN);
                    return true;
                }), any());
    }

    @Test
    @DisplayName("SMS 인증 실패 - 틀린 Code")
    void failedVerifySMSCodeByWrong() {
        // given
        String code = "121314";
        SMSAuth auth = new SMSAuth(phone, code);
        when(smsAuthRepository.getAuthPayload(eq(token), eq(SMS_AUTH_NAME),
                eq(SMSAuth.class), any()))
                .thenReturn(Optional.of(auth));

        // then
        assertThrows(WrongSMSCodeException.class, () ->
                service.verifySMSCode(token, code + "aa"));
    }

    @Test
    @DisplayName("SMS 인증 실패 - 인증 요청을 보내지 않은 경우")
    void failedVerifySMSCodeByNotFound() {
        // given
        when(smsAuthRepository.getAuthPayload(eq(token), eq(SMS_AUTH_NAME),
                eq(SMSAuth.class), any()))
                .thenReturn(Optional.empty());

        // then
        assertThrows(NotSMSSentException.class, () ->
                service.verifySMSCode(token, ""));
    }

    @Test
    @DisplayName("코드가 숫자에 정확한 자리수로 잘 생성되는지")
    void generateDigitCode() {
        // given
        int count = 10;

        // when
        String code = SMSVerificationService.generateDigitCode(count);

        // then
        assertThat(code.length()).isEqualTo(count);
        for (char c : code.toCharArray()) {
            assertThat(Character.isDigit(c)).isTrue();
        }
    }
}
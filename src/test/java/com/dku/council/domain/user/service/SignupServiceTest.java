package com.dku.council.domain.user.service;

import com.dku.council.domain.user.exception.AlreadyNicknameException;
import com.dku.council.domain.user.exception.AlreadyStudentIdException;
import com.dku.council.domain.user.model.dto.request.RequestSignupDto;
import com.dku.council.domain.user.model.entity.Major;
import com.dku.council.domain.user.model.entity.User;
import com.dku.council.domain.user.repository.MajorRepository;
import com.dku.council.domain.user.repository.UserRepository;
import com.dku.council.global.auth.role.UserRole;
import com.dku.council.infra.dku.model.StudentInfo;
import com.dku.council.mock.MajorMock;
import com.dku.council.mock.UserMock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SignupServiceTest {

    @Mock
    private SMSVerificationService smsVerificationService;

    @Mock
    private DKUAuthService dkuAuthService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MajorRepository majorRepository;

    @InjectMocks
    private SignupService service;


    private final String token = "token";
    private final String encodedPwd = "Encoded";
    private final String phone = "01011112222";
    private final String studentId = "id";
    private final StudentInfo info = new StudentInfo("name", studentId, 0, "Major", "Department");
    private final RequestSignupDto dto = new RequestSignupDto("nickname", "pwd");


    @Test
    @DisplayName("회원가입이 잘 되는가")
    void signup() {
        // given
        Major major = MajorMock.create();

        when(userRepository.findByStudentId(studentId)).thenReturn(Optional.empty());
        when(dkuAuthService.getStudentInfo(token)).thenReturn(info);
        when(passwordEncoder.encode(dto.getPassword())).thenReturn(encodedPwd);
        when(smsVerificationService.getPhoneNumber(token)).thenReturn(phone);
        when(majorRepository.findByName("Major", "Department"))
                .thenReturn(Optional.of(major));

        // when
        service.signup(dto, token);

        // then
        verify(userRepository).save(argThat(user -> {
            assertThat(user.getStudentId()).isEqualTo(info.getStudentId());
            assertThat(user.getPassword()).isEqualTo(encodedPwd);
            assertThat(user.getName()).isEqualTo(info.getStudentName());
            assertThat(user.getPhone()).isEqualTo(phone);
            assertThat(user.getYearOfAdmission()).isEqualTo(0);
            assertThat(user.getUserRole()).isEqualTo(UserRole.USER);
            assertThat(user.getMajor()).isEqualTo(major);
            return true;
        }));
        verify(dkuAuthService).deleteStudentAuth(token);
        verify(smsVerificationService).deleteSMSAuth(token);
    }

    @Test
    @DisplayName("회원가입 실패 - 이미 있는 회원인 경우")
    void failedSignupByAlreadyUser() {
        // given
        User user = UserMock.createDummyMajor();

        when(dkuAuthService.getStudentInfo(token)).thenReturn(info);
        when(userRepository.findByStudentId(studentId)).thenReturn(Optional.of(user));

        // when & then
        assertThrows(AlreadyStudentIdException.class, () ->
                service.signup(dto, token));
    }

    @Test
    @DisplayName("회원가입 실패 - 이미 있는 닉네임인 경우")
    void failedSignupByAlreadyNickname() {
        // given
        User user = UserMock.createDummyMajor();

        when(dkuAuthService.getStudentInfo(token)).thenReturn(info);
        when(userRepository.findByStudentId(studentId)).thenReturn(Optional.empty());
        when(userRepository.findByNickname(dto.getNickname())).thenReturn(Optional.of(user));

        // when & then
        assertThrows(AlreadyNicknameException.class, () ->
                service.signup(dto, token));
    }
}
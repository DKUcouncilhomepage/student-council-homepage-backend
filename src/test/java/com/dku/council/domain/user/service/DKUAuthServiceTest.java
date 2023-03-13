package com.dku.council.domain.user.service;

import com.dku.council.domain.user.exception.AlreadyStudentIdException;
import com.dku.council.domain.user.exception.NotDKUAuthorizedException;
import com.dku.council.domain.user.model.dto.request.RequestVerifyStudentDto;
import com.dku.council.domain.user.model.dto.response.ResponseVerifyStudentDto;
import com.dku.council.domain.user.model.entity.User;
import com.dku.council.domain.user.repository.SignupAuthRepository;
import com.dku.council.domain.user.repository.UserRepository;
import com.dku.council.infra.dku.model.DkuAuth;
import com.dku.council.infra.dku.model.StudentInfo;
import com.dku.council.infra.dku.service.DkuAuthenticationService;
import com.dku.council.infra.dku.service.DkuCrawlerService;
import com.dku.council.mock.UserMock;
import com.dku.council.util.ClockUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.LinkedMultiValueMap;

import java.time.Clock;
import java.util.Optional;

import static com.dku.council.domain.user.service.DKUAuthService.DKU_AUTH_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DKUAuthServiceTest {

    @Mock
    private DkuCrawlerService crawlerService;

    @Mock
    private DkuAuthenticationService authenticationService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SignupAuthRepository dkuAuthRepository;

    private final Clock clock = ClockUtil.create();
    private DKUAuthService service;

    @BeforeEach
    public void setup(){
        this.service = new DKUAuthService(clock, crawlerService,
                authenticationService, userRepository, dkuAuthRepository);
    }


    @Test
    @DisplayName("studentInfo를 잘 가져오는지")
    void getStudentInfo() {
        // given
        StudentInfo info = new StudentInfo("", "", 1, "", "");
        when(dkuAuthRepository.getAuthPayload(any(),
                eq(DKU_AUTH_NAME), eq(StudentInfo.class), any()))
                .thenReturn(Optional.of(info));

        // when
        StudentInfo result = service.getStudentInfo("token");

        // then
        assertThat(result).isEqualTo(info);
    }

    @Test
    @DisplayName("studentInfo 없으면 가져오기 실패")
    void getStudentInfoWhenNotFound() {
        // given
        when(dkuAuthRepository.getAuthPayload(any(),
                eq(DKU_AUTH_NAME), eq(StudentInfo.class), any()))
                .thenReturn(Optional.empty());

        // when & then
        assertThrows(NotDKUAuthorizedException.class, () ->
                service.getStudentInfo("token"));
    }

    @Test
    @DisplayName("auth 정보 삭제")
    void deleteStudentAuth() {
        // given
        String token = "token";
        when(dkuAuthRepository.deleteAuthPayload(token, DKU_AUTH_NAME))
                .thenReturn(true);

        // when
        boolean result = service.deleteStudentAuth(token);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("학생 인증 진행")
    void verifyStudent() {
        // given
        String id = "id";
        String pwd = "pwd";
        DkuAuth auth = new DkuAuth(new LinkedMultiValueMap<>());
        StudentInfo info = new StudentInfo("name", "1212", 0, "", "");
        RequestVerifyStudentDto dto = new RequestVerifyStudentDto(id, pwd);

        when(authenticationService.login(id, pwd)).thenReturn(auth);
        when(crawlerService.crawlStudentInfo(auth)).thenReturn(info);
        when(userRepository.findByStudentId(id)).thenReturn(Optional.empty());

        // when
        ResponseVerifyStudentDto response = service.verifyStudent(dto);

        // then
        assertThat(response.getStudent().getStudentId()).isEqualTo("1212");
        assertThat(response.getStudent().getStudentName()).isEqualTo("name");
        verify(dkuAuthRepository).setAuthPayload(any(), eq(DKU_AUTH_NAME), eq(info), any());
    }

    @Test
    @DisplayName("학생 인증 실패 - 이미 등록된 id")
    void failedVerifyStudentByAlreadyStudentId() {
        // given
        String id = "id";
        String pwd = "pwd";
        RequestVerifyStudentDto dto = new RequestVerifyStudentDto(id, pwd);
        User user = UserMock.createDummyMajor();

        when(userRepository.findByStudentId(id)).thenReturn(Optional.of(user));

        // when & then
        assertThrows(AlreadyStudentIdException.class, () ->
                service.verifyStudent(dto));
    }
}
package com.dku.council.domain.user.service;

import com.dku.council.domain.user.exception.AlreadyStudentIdException;
import com.dku.council.domain.user.exception.NotDKUAuthorizedException;
import com.dku.council.domain.user.model.dto.request.RequestVerifyStudentDto;
import com.dku.council.domain.user.model.dto.response.ResponseScrappedStudentInfoDto;
import com.dku.council.domain.user.model.dto.response.ResponseVerifyStudentDto;
import com.dku.council.domain.user.model.entity.User;
import com.dku.council.domain.user.repository.SignupAuthRepository;
import com.dku.council.domain.user.repository.UserRepository;
import com.dku.council.infra.dku.model.DkuAuth;
import com.dku.council.infra.dku.model.StudentInfo;
import com.dku.council.infra.dku.service.DkuAuthenticationService;
import com.dku.council.infra.dku.service.DkuCrawlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DKUAuthService {

    public static final String DKU_AUTH_NAME = "dku";

    private final Clock clock;
    private final DkuCrawlerService crawlerService;
    private final DkuAuthenticationService authenticationService;
    private final UserRepository userRepository;
    private final SignupAuthRepository dkuAuthRepository;

    /**
     * 회원가입 토큰을 기반으로 인증된 학생 정보를 가져옵니다. 학생인증이 되어있지 않으면 Exception이 발생합니다.
     * 이 메서드는 회원가입 진행자를 대상으로 합니다.
     *
     * @param signupToken 회원가입 토큰
     * @throws NotDKUAuthorizedException 학생 인증을 하지 않았을 경우
     */
    public StudentInfo getStudentInfo(String signupToken) throws NotDKUAuthorizedException {
        Instant now = Instant.now(clock);
        return dkuAuthRepository.getAuthPayload(signupToken, DKU_AUTH_NAME, StudentInfo.class, now)
                .orElseThrow(NotDKUAuthorizedException::new);
    }

    /**
     * 회원가입 토큰을 기반으로 인증된 학생 정보를 삭제합니다. 회원가입이 끝나면
     * 학생 인증 정보를 모두 삭제해야합니다.
     *
     * @param signupToken 회원가입 토큰
     */
    public boolean deleteStudentAuth(String signupToken) {
        return dkuAuthRepository.deleteAuthPayload(signupToken, DKU_AUTH_NAME);
    }

    /**
     * 단대 id, password로 학생 인증을 진행합니다.
     *
     * @param dto 요청 dto
     * @return 학생 인증 결과 dto
     */
    public ResponseVerifyStudentDto verifyStudent(RequestVerifyStudentDto dto) {
        String signupToken = UUID.randomUUID().toString();
        checkAlreadyStudentId(dto);

        DkuAuth auth = authenticationService.login(dto.getDkuStudentId(), dto.getDkuPassword());
        StudentInfo studentInfo = crawlerService.crawlStudentInfo(auth);

        Instant now = Instant.now(clock);
        dkuAuthRepository.setAuthPayload(signupToken, DKU_AUTH_NAME, studentInfo, now);

        ResponseScrappedStudentInfoDto studentInfoDto = new ResponseScrappedStudentInfoDto(studentInfo);
        return new ResponseVerifyStudentDto(signupToken, studentInfoDto);
    }

    private void checkAlreadyStudentId(RequestVerifyStudentDto dto) {
        Optional<User> alreadyUser = userRepository.findByStudentId(dto.getDkuStudentId());
        if (alreadyUser.isPresent()) {
            throw new AlreadyStudentIdException();
        }
    }
}

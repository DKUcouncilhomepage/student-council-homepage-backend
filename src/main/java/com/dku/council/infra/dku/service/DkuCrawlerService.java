package com.dku.council.infra.dku.service;

import com.dku.council.domain.user.model.MajorData;
import com.dku.council.global.config.qualifier.ChromeAgentWebClient;
import com.dku.council.infra.dku.exception.DkuFailedCrawlingException;
import com.dku.council.infra.dku.model.DkuAuth;
import com.dku.council.infra.dku.model.StudentInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DkuCrawlerService {

    @ChromeAgentWebClient
    private final WebClient webClient;

    private final MessageSource messageSource;

    @Value("${dku.student-info.api-path}")
    private final String studentInfoApiPath;


    /**
     * 학생 정보를 크롤링해옵니다.
     *
     * @param auth 인증 토큰
     * @return 학생 정보
     */
    public StudentInfo crawlStudentInfo(DkuAuth auth) {
        String html = requestStudentInfo(auth);
        return parseHtml(html);
    }

    private String requestStudentInfo(DkuAuth auth) {
        String result;
        try {
            result = webClient.post()
                    .uri(studentInfoApiPath)
                    .cookies(auth.authCookies())
                    .header("Referer", "https://webinfo.dankook.ac.kr/")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Throwable t) {
            throw new DkuFailedCrawlingException(t);
        }
        return result;
    }

    private StudentInfo parseHtml(String html) {
        Document doc = Jsoup.parse(html);

        String studentName = getElementValueOrNull(doc, "nm");
        String studentId = getElementValueOrNull(doc, "stuid");

        String major, department = "";
        MajorData majorData;
        int yearOfAdmission;

        try {
            String pstnOrgzNm = getElementValueOrNull(doc, "pstnOrgzNm");
            String[] orgToken = pstnOrgzNm.trim().split(" ");

            major = orgToken[orgToken.length - 1];
            if (orgToken.length > 1) {
                department = orgToken[orgToken.length - 2];
            }

            majorData = MajorData.of(messageSource, major);

            String etrsYy = getElementValueOrNull(doc, "etrsYy");
            yearOfAdmission = Integer.parseInt(etrsYy);
        } catch (Throwable t) {
            throw new DkuFailedCrawlingException(t);
        }

        if (majorData == null) {
            log.error("Unexpected major name: {} {}. It will be treated as {}", department, major, MajorData.NO_DATA.name());
            return new StudentInfo(studentName, studentId, yearOfAdmission, major, department);
        } else {
            return new StudentInfo(studentName, studentId, yearOfAdmission, majorData);
        }
    }

    private String getElementValueOrNull(Document doc, String id) {
        return Optional.ofNullable(doc.getElementById(id))
                .map(Element::val)
                .orElseThrow(() -> new DkuFailedCrawlingException(new NullPointerException(id)));
    }
}

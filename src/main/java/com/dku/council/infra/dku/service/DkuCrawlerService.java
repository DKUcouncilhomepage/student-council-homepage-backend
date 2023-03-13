package com.dku.council.infra.dku.service;

import com.dku.council.global.config.webclient.ChromeAgentWebClient;
import com.dku.council.infra.dku.exception.DkuFailedCrawlingException;
import com.dku.council.infra.dku.model.DkuAuth;
import com.dku.council.infra.dku.model.StudentInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DkuCrawlerService {

    @ChromeAgentWebClient
    private final WebClient webClient;

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

        String studentName = getElementValueOrThrow(doc, "nm");
        String studentId = getElementValueOrThrow(doc, "stuid");

        String major, department = "";
        int yearOfAdmission;

        try {
            // 사회과학대학 정치외교학과
            major = getElementValueOrThrow(doc, "pstnOrgzNm");
            major = major.trim();

            int spaceIdx = major.lastIndexOf(' ');
            if (spaceIdx >= 0) {
                department = major.substring(0, spaceIdx);
                major = major.substring(spaceIdx + 1);
            }

            String etrsYy = getElementValueOrThrow(doc, "etrsYy");
            yearOfAdmission = Integer.parseInt(etrsYy);
        } catch (Throwable t) {
            throw new DkuFailedCrawlingException(t);
        }

        return new StudentInfo(studentName, studentId, yearOfAdmission, major, department);
    }

    private String getElementValueOrThrow(Document doc, String id) {
        String value = Optional.ofNullable(doc.getElementById(id))
                .map(Element::val)
                .orElseThrow(() -> new DkuFailedCrawlingException(new NullPointerException(id)));
        if (value.isBlank()) {
            throw new DkuFailedCrawlingException(new NullPointerException(id));
        }
        return value;
    }
}

package com.dku.council.domain.user;

import com.dku.council.domain.user.model.MajorData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MajorDataTest {

    @Mock
    MessageSource messageSource;

    @Test
    @DisplayName("Major name이 국제화가 잘 되는지?")
    void getName() {
        // given
        when(messageSource.getMessage(Mockito.startsWith("Major."), any(), any())).thenReturn("컴퓨터공학과");

        // when
        String majorName = MajorData.COMPUTER_SCIENCE.getName(messageSource);

        // then
        assertThat(majorName).isEqualTo("컴퓨터공학과");
    }

    @Test
    @DisplayName("of로 잘 변환 되는지?")
    public void successfulOf() {
        // given
        when(messageSource.getMessage(Mockito.startsWith("Major."), any(), any())).thenAnswer((invo) -> {
            if (invo.getArgument(0).equals("Major.KOREAN_LITERATURE")) {
                return "국어국문학과";
            } else {
                return "모르는 학과";
            }
        });

        // when
        // 띄어쓰기나 tab 해도 모두 삭제
        MajorData majorData = MajorData.of(messageSource, "  국어국 문학과 ");

        // then
        assertThat(majorData).isEqualTo(MajorData.KOREAN_LITERATURE);
    }

    @Test
    @DisplayName("of로 없는 Major 조회시 null 반환")
    public void failedOfByNotFound() {
        // given
        when(messageSource.getMessage(Mockito.startsWith("Major."), any(), any())).thenReturn("모르는 학과");

        // when
        MajorData majorData = MajorData.of(messageSource, "국어국문학과");

        // then
        assertThat(majorData).isEqualTo(null);
    }
}
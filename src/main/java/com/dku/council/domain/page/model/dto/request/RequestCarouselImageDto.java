package com.dku.council.domain.page.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Getter
@RequiredArgsConstructor
public class RequestCarouselImageDto {

    @NotNull(message = "파일을 등록해주세요")
    @Schema(description = "이미지 파일 등록", example = "myImage.png")
    private final MultipartFile imageFile;

    @NotEmpty(message = "리다이렉트 URL 이 필요합니다.")
    @Schema(description = "이미지 리다이렉트 경로", example = "redirect.com/redirectPath")
    @Pattern(regexp = "^[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b(?:[-a-zA-Z0-9()@:%_\\+.~#?&//=]*)$",
    message = "url 형식을 맞춰야 합니다.")
    private final String redirectUrl;

}

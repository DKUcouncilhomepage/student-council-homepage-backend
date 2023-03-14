package com.dku.council.domain.page.controller;

import com.dku.council.domain.page.model.dto.request.RequestCarouselImageDto;
import com.dku.council.domain.page.model.dto.response.CarouselImageResponse;
import com.dku.council.domain.page.model.dto.response.MainPageResponseDto;
import com.dku.council.domain.page.service.PageService;
import com.dku.council.global.auth.role.AdminOnly;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Tag(name = "메인 페이지", description = "메인 페이지 관련 api")
@RestController
@RequestMapping("/main")
@RequiredArgsConstructor
public class PageController {
    private final PageService pageService;

    /**
     * 메인 페이지 화면 데이터. 캐러셀이미지 & 총학소식 & 청원 & 회의록
     * @return 총학소식, 청원, 회의록 최신 5개의 데이터를 반환합니다.
     */
    @GetMapping
    public MainPageResponseDto index(){
        return pageService.getMainPage();
    }

    /**
     * 캐러셀 이미지 업로드 ONLY FOR ADMIN
     * @param dto 이미지 파일 & 리다이렉트 URL
     */
    @PostMapping("/carousel")
    @AdminOnly
    public void uploadCarouselImage(@Valid @ModelAttribute RequestCarouselImageDto dto) {
        pageService.addCarouselImage(dto);
    }

    /**
     * 캐러셀 목록 가져오기
     * @return 저장되어 있는 모든 캐러셀 이미지 파일을 반환합니다.
     */
    @GetMapping("/carousel")
    public List<CarouselImageResponse> getCarouselImages(){
        return pageService.getCarouselImages();
    }

    /**
     * 캐러셀 삭제
     * @param carouselId 캐러셀 ID로 삭제합니다. ONLY FOR ADMIN
     */
    @DeleteMapping("/carousel/{id}")
    @AdminOnly
    public void deleteCarouselImage(@PathVariable("id") Long carouselId){
        pageService.deleteCarouselImage(carouselId);
    }
}

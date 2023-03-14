package com.dku.council.domain.page.model.dto.response;

import com.dku.council.domain.page.model.dto.PetitionSummary;
import com.dku.council.domain.page.model.dto.PostSummary;
import lombok.Getter;

import java.util.List;

@Getter
public class MainPageResponseDto {
    private final List<CarouselImageResponse> carousels;
    private final List<PostSummary> recentNews;
    private final List<PostSummary> recentConferences;
    private final List<PetitionSummary> popularPetitions;


    public MainPageResponseDto(List<CarouselImageResponse> carousels, List<PostSummary> recentNews, List<PostSummary> recentConferences, List<PetitionSummary> popularPetitions) {
        this.carousels = carousels;
        this.recentNews = recentNews;
        this.recentConferences = recentConferences;
        this.popularPetitions = popularPetitions;
    }

}

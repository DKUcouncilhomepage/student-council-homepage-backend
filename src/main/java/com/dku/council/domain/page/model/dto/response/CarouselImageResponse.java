package com.dku.council.domain.page.model.dto.response;

import com.dku.council.domain.page.model.CarouselImage;
import com.dku.council.infra.nhn.service.ObjectStorageService;
import lombok.Getter;

import java.net.URI;

@Getter
public class CarouselImageResponse {
    private final Long id;
    private final String url;
    private final String redirectUrl;

    public CarouselImageResponse(String baseUrl, CarouselImage image) {
        this.id = image.getId();
        this.url = URI.create(baseUrl + "/").resolve(image.getFileId()).toString();
        this.redirectUrl = image.getRedirectUrl();
    }
}

package com.dku.council.domain.page.model;

import com.dku.council.global.base.BaseEntity;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CarouselImage extends BaseEntity {
    @Id
    @GeneratedValue
    @Column(name = "carousel_id")
    private Long id;

    @Column(name = "carousel_file_id")
    private String fileId;

    private String redirectUrl;

    @Builder
    private CarouselImage(@NonNull String fileId,
                          @NonNull String redirectUrl){
        this.fileId = fileId;
        this.redirectUrl = redirectUrl;
    }
}

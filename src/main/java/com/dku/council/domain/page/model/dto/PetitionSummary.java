package com.dku.council.domain.page.model.dto;

import com.dku.council.domain.post.model.entity.posttype.Petition;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.Period;

@Getter
public class PetitionSummary {
    private final Long id;
    private final String title;
    private final String petitionStatus;
    private final int D_day;

    public PetitionSummary(Petition petition) {
        this.id = petition.getId();
        this.title = petition.getTitle();
        this.petitionStatus = petition.getStatus().toString();
        Period period = Period.between(petition.getCreatedAt().plusDays(15).toLocalDate(), LocalDateTime.now().toLocalDate());
        this.D_day = period.getDays();
    }
}

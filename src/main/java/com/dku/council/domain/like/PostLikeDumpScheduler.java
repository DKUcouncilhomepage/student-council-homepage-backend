package com.dku.council.domain.like;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostLikeDumpScheduler {

    private final PostLikeService service;

    @Scheduled(fixedDelayString = "${app.post.dump-time}")
    public void dumpToDB() {
        if (service.dumpToDB() > 0) {
            log.info("PostLikes in memory dump to DB.");
        }
    }
}

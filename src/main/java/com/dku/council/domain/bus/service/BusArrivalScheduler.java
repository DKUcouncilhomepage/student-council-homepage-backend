package com.dku.council.domain.bus.service;

import com.dku.council.domain.bus.model.BusStation;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class BusArrivalScheduler {

    private final Clock clock;
    private final BusService busService;

    @Scheduled(fixedDelayString = "${bus.cache-time}")
    public void schedule() {
        Instant now = Instant.now(clock);
        for (BusStation station : BusStation.values()) {
            busService.cacheBusArrival(station, now);
        }
    }
}

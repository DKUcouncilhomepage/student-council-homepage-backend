package com.dku.council.domain.bus.repository.impl;

import com.dku.council.domain.bus.model.repository.CachedBusArrivals;
import com.dku.council.domain.bus.repository.BusArrivalMemoryRepository;
import com.dku.council.global.config.redis.RedisKeys;
import com.dku.council.infra.bus.model.BusArrival;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

// TODO 다른 클래스들과 일반화하기
@Repository
@RequiredArgsConstructor
public class BusArrivalMemoryRepositoryImpl implements BusArrivalMemoryRepository {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${bus.cache-time}")
    private final Duration busCacheTime;

    @Override
    public CachedBusArrivals getArrivals(String stationId, Instant now) {
        Object value = redisTemplate.opsForHash().get(RedisKeys.BUS_ARRIVAL_KEY, stationId);
        if (value == null) {
            return null;
        }

        CachedBusArrivals cachedData;
        try {
            cachedData = objectMapper.readValue((String) value, CachedBusArrivals.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        Instant capturedAt = cachedData.getCapturedAt();
        if (now.isAfter(capturedAt.plus(busCacheTime))) {
            redisTemplate.opsForHash().delete(RedisKeys.BUS_ARRIVAL_KEY, stationId);
            return null;
        }

        return cachedData;
    }

    @Override
    public CachedBusArrivals cacheArrivals(String stationId, List<BusArrival> arrivals, Instant now) {
        CachedBusArrivals cachedData = new CachedBusArrivals(now, arrivals);
        String value;
        try {
            value = objectMapper.writeValueAsString(cachedData);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        redisTemplate.opsForHash().put(RedisKeys.BUS_ARRIVAL_KEY, stationId, value);
        return cachedData;
    }
}

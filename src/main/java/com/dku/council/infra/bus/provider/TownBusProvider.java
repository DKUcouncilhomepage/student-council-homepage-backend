package com.dku.council.infra.bus.provider;

import com.dku.council.domain.bus.model.BusStation;
import com.dku.council.global.error.exception.UnexpectedResponseException;
import com.dku.council.infra.bus.exception.CannotGetBusArrivalException;
import com.dku.council.infra.bus.exception.InvalidBusStationException;
import com.dku.council.infra.bus.model.BusArrival;
import com.dku.council.infra.bus.model.ResponseKakaoBusApi;
import com.dku.council.infra.bus.model.mapper.BusResponseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TownBusProvider implements BusArrivalProvider {

    private final WebClient webClient;

    @Value("${bus.town.api-path}")
    private final String apiPath;

    @Override
    public List<BusArrival> retrieveBusArrival(BusStation station) {
        try {
            ResponseKakaoBusApi response = request(station.getTownNodeId());

            if (response == null) {
                throw new UnexpectedResponseException("Failed response");
            }

            String name = response.getName();
            if (name == null) {
                throw new InvalidBusStationException();
            }

            return response.getLines().stream()
                    .filter(ResponseKakaoBusApi.BusLine::isRunning)
                    .map(BusResponseMapper::to)
                    .filter(TownBusProvider::filter24Bus)
                    .collect(Collectors.toList());
        } catch (Throwable e) {
            throw new CannotGetBusArrivalException(e);
        }
    }

    @Override
    public String getProviderPrefix() {
        return "T_";
    }

    // 곰상에 정차중인 24번 버스는 제외
    private static boolean filter24Bus(BusArrival busArrival) {
        if (busArrival.getBusNo().equals("24")) {
            return busArrival.getLocationNo1() != 2;
        }
        return true;
    }

    private ResponseKakaoBusApi request(String stationId) {
        URI uri = UriComponentsBuilder.fromHttpUrl(apiPath)
                .queryParam("busstopid", stationId)
                .build()
                .toUri();

        return webClient.mutate()
                .build().get()
                .uri(uri)
                .header("Referer", "https://map.kakao.com/")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36")
                .retrieve()
                .bodyToMono(ResponseKakaoBusApi.class)
                .block();
    }
}

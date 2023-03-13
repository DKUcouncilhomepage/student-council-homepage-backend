package com.dku.council.domain.bus.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.function.Predicate;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum Bus {
    GG_102("102", "206000007", 2),
    GG_1101("1101", "234000879", 2),
    GG_8100("8100", "234000878", 2),
    GG_720_3("720-3", "234000068", 5, (s) -> s < 10), // 기점에서 출발하는 방향의 720-3번만
    T_24("24", "B39979", 2),
    DKU_SHUTTLE_BUS("shuttle-bus", "", 2);

    private final String name;
    private final String routeId;
    private final int predictionStationOrder;
    private final Predicate<Integer> stationOrderFilter;

    Bus(String name, String routeId, int predictionStationOrder) {
        this(name, routeId, predictionStationOrder, null);
    }

    public boolean filterStationOrder(int stationOrder) {
        return stationOrderFilter == null || stationOrderFilter.test(stationOrder);
    }

    /**
     * routeId로 Bus를 찾습니다.
     *
     * @param routeId 찾을 bus routeId
     * @return 찾은 Bus. 없으면 null
     */
    public static Bus of(String routeId) {
        for (Bus bus : values()) {
            if (bus.routeId.equals(routeId)) {
                return bus;
            }
        }
        return null;
    }
}

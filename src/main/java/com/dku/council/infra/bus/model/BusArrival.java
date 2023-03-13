package com.dku.council.infra.bus.model;

import com.dku.council.domain.bus.model.Bus;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@EqualsAndHashCode
public class BusArrival {
    private final BusStatus status;
    private final Integer stationOrder;
    private final Integer locationNo1;
    private final Integer predictTimeSec1;
    private final String plateNo1;
    private final Integer locationNo2;
    private final Integer predictTimeSec2;
    private final String plateNo2;
    private final String busNo;

    public static BusArrival stopped(String busNo) {
        return new BusArrival(BusStatus.STOP, 0,
                null, null, null,
                null, null, null,
                busNo);
    }

    public static BusArrival predict(Bus bus, int predictTimeSec) {
        return new BusArrival(BusStatus.PREDICT, 0,
                bus.getPredictionStationOrder(), predictTimeSec, "",
                null, null, null, bus.getName());
    }
}
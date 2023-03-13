package com.dku.council.mock;

import com.dku.council.infra.bus.model.BusArrival;
import com.dku.council.infra.bus.model.BusStatus;

import java.util.ArrayList;
import java.util.List;

public class BusArrivalMock {

    public static final int PREDICT_TIME_SEC1 = 10;

    public static BusArrival create() {
        return create("24");
    }

    public static BusArrival create(String busNo) {
        return create(busNo, 0);
    }

    public static BusArrival create(String busNo, int stationOrder) {
        return create(busNo, stationOrder, BusStatus.RUN);
    }

    public static BusArrival create(String busNo, BusStatus status) {
        return create(busNo, 0, status);
    }

    public static BusArrival create(String busNo, int stationOrder, BusStatus status) {
        return new BusArrival(status, stationOrder,
                5, PREDICT_TIME_SEC1, "경기16바5555",
                7, 15, "경기16바1111",
                busNo);
    }

    public static List<BusArrival> createList(int size) {
        List<BusArrival> result = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            result.add(create());
        }
        return result;
    }
}

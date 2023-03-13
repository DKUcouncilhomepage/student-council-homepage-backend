package com.dku.council.domain.bus.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum BusStation {
    DKU_GATE("단국대정문", "228001978", "BS83664", Set.of(
            Bus.GG_102, Bus.GG_1101, Bus.GG_8100,
            Bus.GG_720_3, Bus.T_24, Bus.DKU_SHUTTLE_BUS
    )),
    BEAR_STATUE("곰상", "228001980", "BS130194", Set.of(
            Bus.GG_720_3, Bus.T_24, Bus.DKU_SHUTTLE_BUS
    ));

    private final String name;
    private final String ggNodeId;
    private final String townNodeId;
    private final Set<Bus> busSet;

    /**
     * 이름으로 BusStation을 찾습니다.
     *
     * @param name 찾을 bus station name
     * @return 찾은 BusStation. 없으면 null
     */
    public static BusStation of(String name) {
        for (BusStation station : values()) {
            if (station.name.equals(name)) {
                return station;
            }
        }
        return null;
    }
}

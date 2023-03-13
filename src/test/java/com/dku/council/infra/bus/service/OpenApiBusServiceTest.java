package com.dku.council.infra.bus.service;

import com.dku.council.domain.bus.model.BusStation;
import com.dku.council.infra.bus.model.BusArrival;
import com.dku.council.infra.bus.model.BusStatus;
import com.dku.council.infra.bus.predict.BusArrivalPredictService;
import com.dku.council.infra.bus.provider.BusArrivalProvider;
import com.dku.council.infra.bus.provider.GGBusProvider;
import com.dku.council.infra.bus.provider.ShuttleBusProvider;
import com.dku.council.infra.bus.provider.TownBusProvider;
import com.dku.council.mock.BusArrivalMock;
import com.dku.council.util.ClockUtil;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;

import static com.dku.council.infra.bus.service.OpenApiBusService.PREDICTION_LOWER_BOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpenApiBusServiceTest {

    private final Clock clock = ClockUtil.create();

    @Mock
    private GGBusProvider ggBusProvider;

    @Mock
    private TownBusProvider townBusProvider;

    @Mock
    private ShuttleBusProvider shuttleBusProvider;

    @Mock
    private BusArrivalPredictService predictService;

    private OpenApiBusService service;


    @BeforeEach
    public void setup() {
        List<BusArrivalProvider> providers = List.of(ggBusProvider, townBusProvider, shuttleBusProvider);
        service = new OpenApiBusService(clock, predictService, providers);

        when(ggBusProvider.getProviderPrefix()).thenReturn("GG_");
        when(townBusProvider.getProviderPrefix()).thenReturn("T_");
        when(shuttleBusProvider.getProviderPrefix()).thenReturn("DKU_");
    }

    @Test
    @DisplayName("여러 API를 합쳐 잘 필터링 하는지")
    public void retrieveBusArrival() {
        // given
        BusStation station = BusStation.DKU_GATE;
        when(ggBusProvider.retrieveBusArrival(station)).thenReturn(ggBusArrival());
        when(townBusProvider.retrieveBusArrival(station)).thenReturn(townBusArrival());
        when(shuttleBusProvider.retrieveBusArrival(station)).thenReturn(List.of());

        when(predictService.remainingNextBusArrival(any(), eq(station), any())).thenReturn(Duration.ofSeconds(80));
        when(predictService.remainingNextBusArrival(eq("102"), eq(station), any())).thenReturn(null);

        // when
        List<BusArrival> arrivals = service.retrieveBusArrival(station);

        // then
        HashMap<String, BusStatus> statusMap = expected1();
        assertThat(arrivals.size()).isEqualTo(statusMap.size());
        for (BusArrival arrival : arrivals) {
            BusStatus status = statusMap.remove(arrival.getBusNo());
            if (status == null) {
                fail("not contains: " + arrival.getBusNo());
            } else {
                assertThat(arrival.getStatus()).isEqualTo(status);
            }
        }
        assertThat(statusMap).isEmpty();
    }

    private static HashMap<String, BusStatus> expected1() {
        HashMap<String, BusStatus> statusMap = new HashMap<>();
        statusMap.put("102", BusStatus.STOP);
        statusMap.put("1101", BusStatus.PREDICT);
        statusMap.put("8100", BusStatus.PREDICT);
        statusMap.put("720-3", BusStatus.RUN);
        statusMap.put("24", BusStatus.RUN);
        statusMap.put("shuttle-bus", BusStatus.PREDICT);
        return statusMap;
    }

    @NotNull
    private static List<BusArrival> townBusArrival() {
        return List.of(
                BusArrivalMock.create("24"),
                BusArrivalMock.create("1234"),
                BusArrivalMock.create("7007-1"),
                BusArrivalMock.create("720-3", 5),
                BusArrivalMock.create("102")
        );
    }

    @NotNull
    private static List<BusArrival> ggBusArrival() {
        return List.of(
                BusArrivalMock.create("24"),
                BusArrivalMock.create("101"),
                BusArrivalMock.create("1234"),
                BusArrivalMock.create("720-3", 5),
                BusArrivalMock.create("720-3", 150)
        );
    }

    @Test
    @DisplayName("예측 시간이 60초 이하일 때는, 다음 버스가 올 때 까지 예측시간 동결")
    public void retrieveBusArrivalWhenPredictionAtLowerBound() {
        // given
        BusStation station = BusStation.DKU_GATE;
        when(ggBusProvider.retrieveBusArrival(station)).thenReturn(List.of());
        when(townBusProvider.retrieveBusArrival(station)).thenReturn(List.of());
        when(shuttleBusProvider.retrieveBusArrival(station)).thenReturn(List.of());

        // when & then
        when(predictService.remainingNextBusArrival(any(), any(), any())).thenReturn(null);
        whenPrediction1101(PREDICTION_LOWER_BOUND + 10);
        checkBusArrival(station, BusStatus.PREDICT, PREDICTION_LOWER_BOUND + 10);

        whenPrediction1101(PREDICTION_LOWER_BOUND - 1);
        checkBusArrival(station, BusStatus.PREDICT, PREDICTION_LOWER_BOUND);

        whenPrediction1101(500);
        checkBusArrival(station, BusStatus.PREDICT, PREDICTION_LOWER_BOUND);

        when(ggBusProvider.retrieveBusArrival(station)).thenReturn(List.of(
                BusArrivalMock.create("1101")
        ));
        checkBusArrival(station, BusStatus.RUN, BusArrivalMock.PREDICT_TIME_SEC1);

        when(ggBusProvider.retrieveBusArrival(station)).thenReturn(List.of());
        checkBusArrival(station, BusStatus.PREDICT, 500);
    }

    private void whenPrediction1101(int predictSeconds) {
        when(predictService.remainingNextBusArrival(eq("1101"), eq(BusStation.DKU_GATE), any()))
                .thenReturn(Duration.ofSeconds(predictSeconds));
    }

    private void checkBusArrival(BusStation station, BusStatus status, int predictTime) {
        List<BusArrival> arrivals = service.retrieveBusArrival(station);
        BusArrival arrival = getArrivalBus(arrivals, "1101");
        assertThat(arrival.getStatus()).isEqualTo(status);
        assertThat(arrival.getPredictTimeSec1()).isEqualTo(predictTime);
    }

    private static BusArrival getArrivalBus(List<BusArrival> arrivals, String busNo) {
        for (BusArrival arrival : arrivals) {
            if (arrival.getBusNo().equals(busNo)) {
                return arrival;
            }
        }
        return null;
    }
}
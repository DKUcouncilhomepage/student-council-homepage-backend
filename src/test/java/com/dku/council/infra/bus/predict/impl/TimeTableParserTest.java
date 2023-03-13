package com.dku.council.infra.bus.predict.impl;

import com.dku.council.util.FieldReflector;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;

class TimeTableParserTest {

    private TimeTableParser parser;

    @BeforeEach
    public void setup() {
        this.parser = new TimeTableParser();
    }

    @Test
    @DisplayName("파싱이 잘 되는지?")
    void parse() {
        // given
        List<LocalTime> expected = List.of(
                LocalTime.of(9, 5),
                LocalTime.of(10, 5),
                LocalTime.of(10, 20),
                LocalTime.of(10, 35),
                LocalTime.of(10, 50),
                LocalTime.of(11, 5),
                LocalTime.of(11, 15),
                LocalTime.of(11, 25),
                LocalTime.of(11, 35),
                LocalTime.of(11, 50),
                LocalTime.of(12, 5),
                LocalTime.of(12, 20),
                LocalTime.of(12, 35),
                LocalTime.of(12, 50),
                LocalTime.of(13, 10),
                LocalTime.of(13, 30),
                LocalTime.of(13, 50)
        );

        // when
        TimeTable table = parser.parse("/mockdata/bus/test-bus.table");

        // then
        List<LocalTime> times = FieldReflector.get(TimeTable.class, table, "timeTables");
        Assertions.assertThat(times).containsExactlyElementsOf(expected);
    }
}
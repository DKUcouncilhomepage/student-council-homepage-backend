package com.dku.council.infra.bus.predict.impl;

import com.dku.council.infra.bus.exception.CannotGetTimeTable;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TimeTableParser {

    private static final DateTimeFormatter LOCAL_TIME_FORMATTER = DateTimeFormatter.ofPattern("H:mm");

    private Duration offset;
    private List<LocalTime> timeTables;

    private void reset() {
        this.offset = Duration.ZERO;
        timeTables = new ArrayList<>();
    }

    /**
     * 시간표를 resources로부터 불러옵니다.
     */
    public TimeTable parse(String path) {
        reset();
        try (
                InputStream stream = TimeTable.class.getResourceAsStream(path);
                InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
                BufferedReader bReader = new BufferedReader(reader)
        ) {
            String line;
            while ((line = bReader.readLine()) != null) {
                parseTableLine(line);
            }

            if (timeTables.isEmpty()) {
                throw new CannotGetTimeTable("시간표를 불러올 수 없습니다: " + path);
            }

            if (offset != Duration.ZERO) {
                timeTables = timeTables.stream()
                        .map(t -> t.plus(offset))
                        .collect(Collectors.toList());
            }

            return new TimeTable(timeTables);
        } catch (IOException | NullPointerException e) {
            throw new CannotGetTimeTable(e);
        }
    }

    private void parseTableLine(String line) {
        line = line.trim();
        if (line.startsWith("#") || line.isBlank()) {
            return;
        }

        if (line.startsWith("@")) {
            String[] token = line.substring(1).split(" ");
            if (token[0].equalsIgnoreCase("offset")) {
                this.offset = Duration.parse(token[1]);
            }
            return;
        }

        timeTables.add(LocalTime.parse(line, LOCAL_TIME_FORMATTER));
    }
}

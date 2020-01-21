package com.michielboekhoff.starlingtest.client;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

class IntervalTest {

    @Test
    @DisplayName("it should return an interval starting 7 days ago and ending now")
    void shouldReturnInterval() {
        Clock clock = Clock.fixed(Instant.parse("2020-01-21T10:15:30Z"), ZoneId.of("Z"));

        Interval interval = Interval.lastWeek(clock);

        assertThat(interval.getBegin().toString()).isEqualTo("2020-01-14T10:15:30Z");
        assertThat(interval.getEnd().toString()).isEqualTo("2020-01-21T10:15:30Z");
    }
}
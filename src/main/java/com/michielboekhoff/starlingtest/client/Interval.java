package com.michielboekhoff.starlingtest.client;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

public class Interval {

    private final OffsetDateTime begin;
    private final OffsetDateTime end;

    private Interval(OffsetDateTime begin, OffsetDateTime end) {
        this.begin = begin;
        this.end = end;
    }

    public OffsetDateTime getBegin() {
        return begin;
    }

    public OffsetDateTime getEnd() {
        return end;
    }

    public static Interval lastWeek(Clock clock) {
        OffsetDateTime end = OffsetDateTime.now(clock);
        OffsetDateTime start = end.minus(7, ChronoUnit.DAYS);

        return new Interval(start, end);
    }
}

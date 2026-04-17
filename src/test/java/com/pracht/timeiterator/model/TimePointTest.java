package com.pracht.timeiterator.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.Test;

public class TimePointTest {

    @Test
    void testFromLocalDateTime() {
        LocalDateTime now = LocalDateTime.now();
        TimePoint tp = TimePoint.from(now);
        assertThat(tp.isZoned()).isFalse();
        assertThat(tp.toLocalDateTime()).isEqualTo(now);
    }

    @Test
    void testFromZonedDateTime() {
        ZonedDateTime nowZoned = ZonedDateTime.now();
        TimePoint tp = TimePoint.from(nowZoned);
        assertThat(tp.isZoned()).isTrue();
        assertThat(tp.toZonedDateTime()).isEqualTo(nowZoned);
        assertThat(tp.toLocalDateTime()).isEqualTo(nowZoned.toLocalDateTime());
    }

    @Test
    void testNow() {
        TimePoint tp = TimePoint.now();
        assertThat(tp.isZoned()).isFalse();
        assertThat(tp.toLocalDateTime()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    void testNowZoned() {
        TimePoint tp = TimePoint.nowZoned();
        assertThat(tp.isZoned()).isTrue();
        assertThat(tp.toZonedDateTime().getZone()).isEqualTo(ZoneId.systemDefault());
    }

    @Test
    void testNowZonedWithZone() {
        ZoneId zone = ZoneId.of("America/New_York");
        TimePoint tp = TimePoint.nowZoned(zone);
        assertThat(tp.isZoned()).isTrue();
        assertThat(tp.toZonedDateTime().getZone()).isEqualTo(zone);
    }

    @Test
    void testToLocalDateTimeConversion() {
        ZonedDateTime zdt = ZonedDateTime.of(2023, 1, 1, 10, 0, 0, 0, ZoneId.of("UTC"));
        TimePoint tp = TimePoint.from(zdt);
        assertThat(tp.toLocalDateTime()).isEqualTo(LocalDateTime.of(2023, 1, 1, 10, 0));
    }

    @Test
    void testToZonedDateTimeConversion() {
        LocalDateTime ldt = LocalDateTime.of(2023, 1, 1, 10, 0);
        TimePoint tp = TimePoint.from(ldt);
        ZoneId zoneId = ZoneId.of("America/New_York");
        ZonedDateTime zdt = tp.toZonedDateTime(zoneId);
        assertThat(zdt.toLocalDateTime()).isEqualTo(ldt);
        assertThat(zdt.getZone()).isEqualTo(zoneId);
    }

    @Test
    void testEqualsAndHashCode() {
        LocalDateTime ldt = LocalDateTime.now();
        TimePoint tp1 = TimePoint.from(ldt);
        TimePoint tp2 = TimePoint.from(ldt);
        assertThat(tp1).isEqualTo(tp2);
        assertThat(tp1.hashCode()).isEqualTo(tp2.hashCode());

        ZonedDateTime zdt = ZonedDateTime.now();
        TimePoint tp3 = TimePoint.from(zdt);
        assertThat(tp1).isNotEqualTo(tp3);
    }

    @Test
    void testToString() {
        LocalDateTime ldt = LocalDateTime.of(2023, 1, 1, 10, 0);
        TimePoint tp = TimePoint.from(ldt);
        assertThat(tp.toString()).isEqualTo(ldt.toString());

        ZonedDateTime zdt = ldt.atZone(ZoneId.of("UTC"));
        TimePoint tpZoned = TimePoint.from(zdt);
        assertThat(tpZoned.toString()).isEqualTo(zdt.toString());
    }

    @Test
    void testBuilder() {
        TimePoint tp = TimePoint.builder()
                .year(2024)
                .month(5)
                .day(15)
                .hour(10)
                .minute(30)
                .build();
        
        assertThat(tp.isZoned()).isFalse();
        assertThat(tp.toLocalDateTime()).isEqualTo(LocalDateTime.of(2024, 5, 15, 10, 30));
    }

    @Test
    void testBuilderZoned() {
        ZoneId zone = ZoneId.of("America/New_York");
        TimePoint tp = TimePoint.builder()
                .year(2024)
                .month(12)
                .day(25)
                .zoneId(zone)
                .build();
        
        assertThat(tp.isZoned()).isTrue();
        assertThat(tp.toZonedDateTime()).isEqualTo(ZonedDateTime.of(2024, 12, 25, 0, 0, 0, 0, zone));
    }

    @Test
    void testBuilderWithBoundaries() {
        // Last day of Feb 2024 (Leap year)
        TimePoint tp = TimePoint.builder()
                .year(2024)
                .month(2)
                .day(TimePoint.UnitBoundary.MAX)
                .hour(TimePoint.UnitBoundary.MAX)
                .minute(TimePoint.UnitBoundary.MAX)
                .second(TimePoint.UnitBoundary.MAX)
                .nano(TimePoint.UnitBoundary.MAX)
                .build();
        
        LocalDateTime expected = LocalDateTime.of(2024, 2, 29, 23, 59, 59, 999_999_999);
        assertThat(tp.toLocalDateTime()).isEqualTo(expected);
    }

    @Test
    void testBuilderMonthBoundary() {
        TimePoint tp = TimePoint.builder()
                .year(2023)
                .month(TimePoint.UnitBoundary.MAX)
                .day(TimePoint.UnitBoundary.MIN)
                .build();
        
        assertThat(tp.toLocalDateTime()).isEqualTo(LocalDateTime.of(2023, 12, 1, 0, 0));
    }
}

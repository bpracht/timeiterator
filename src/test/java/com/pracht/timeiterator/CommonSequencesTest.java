package com.pracht.timeiterator;

import static org.assertj.core.api.Assertions.assertThat;

import com.pracht.timeiterator.model.TimePoint;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

public class CommonSequencesTest {

    @Test
    void testUSBusinessDays() {
        TimePoint start = TimePoint.builder().year(2024).month(1).day(1).build(); // Monday
        TimePointIterator businessDays = CommonSequences.US.BUSINESS_DAYS(start);
        
        List<TimePoint> firstWeek = businessDays.stream().limit(7).collect(Collectors.toList());
        
        // Should skip Sat (Jan 6) and Sun (Jan 7)
        assertThat(firstWeek).hasSize(7);
        assertThat(firstWeek.get(0).toLocalDateTime().getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        assertThat(firstWeek.get(1).toLocalDateTime().getDayOfWeek()).isEqualTo(DayOfWeek.TUESDAY);
        assertThat(firstWeek.get(2).toLocalDateTime().getDayOfWeek()).isEqualTo(DayOfWeek.WEDNESDAY);
        assertThat(firstWeek.get(3).toLocalDateTime().getDayOfWeek()).isEqualTo(DayOfWeek.THURSDAY);
        assertThat(firstWeek.get(4).toLocalDateTime().getDayOfWeek()).isEqualTo(DayOfWeek.FRIDAY);
        assertThat(firstWeek.get(5).toLocalDateTime().getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        assertThat(firstWeek.get(5).toLocalDateTime().getDayOfMonth()).isEqualTo(8);
    }

    @Test
    void testThanksgiving() {
        TimePointIterator thanksgiving = CommonSequences.US.THANKSGIVING(2023, 2024);
        List<TimePoint> results = thanksgiving.stream().collect(Collectors.toList());
        
        assertThat(results).hasSize(2);
        // 2023: Nov 23
        assertThat(results.get(0).toLocalDateTime().toLocalDate()).isEqualTo(LocalDate.of(2023, 11, 23));
        // 2024: Nov 28
        assertThat(results.get(1).toLocalDateTime().toLocalDate()).isEqualTo(LocalDate.of(2024, 11, 28));
    }

    @Test
    void testMemorialDay() {
        TimePointIterator memorialDay = CommonSequences.US.MEMORIAL_DAY(2023, 2024);
        List<TimePoint> results = memorialDay.stream().collect(Collectors.toList());
        
        assertThat(results).hasSize(2);
        // 2023: May 29
        assertThat(results.get(0).toLocalDateTime().toLocalDate()).isEqualTo(LocalDate.of(2023, 5, 29));
        // 2024: May 27
        assertThat(results.get(1).toLocalDateTime().toLocalDate()).isEqualTo(LocalDate.of(2024, 5, 27));
    }

    @Test
    void testEaster() {
        TimePointIterator easter = CommonSequences.US.EASTER(2024, 2025);
        List<TimePoint> results = easter.stream().collect(Collectors.toList());
        
        assertThat(results).hasSize(2);
        // 2024: March 31
        assertThat(results.get(0).toLocalDateTime().toLocalDate()).isEqualTo(LocalDate.of(2024, 3, 31));
        // 2025: April 20
        assertThat(results.get(1).toLocalDateTime().toLocalDate()).isEqualTo(LocalDate.of(2025, 4, 20));
    }

    @Test
    void testJuneteenth() {
        TimePointSequence juneteenth = CommonSequences.US.JUNETEENTH(2024);
        TimePoint first = juneteenth.next();
        assertThat(first.toLocalDateTime().toLocalDate()).isEqualTo(LocalDate.of(2024, 6, 19));
    }

    @Test
    void testHalloween() {
        TimePointSequence halloween = CommonSequences.US.HALLOWEEN(2024);
        assertThat(halloween.next().toLocalDateTime().toLocalDate()).isEqualTo(LocalDate.of(2024, 10, 31));
    }

    @Test
    void testSeasons() {
        assertThat(CommonSequences.US.SPRING_EQUINOX(2024).next().toLocalDateTime().toLocalDate())
                .isEqualTo(LocalDate.of(2024, 3, 20));
        assertThat(CommonSequences.US.SUMMER_SOLSTICE(2024).next().toLocalDateTime().toLocalDate())
                .isEqualTo(LocalDate.of(2024, 6, 21));
        assertThat(CommonSequences.US.AUTUMNAL_EQUINOX(2024).next().toLocalDateTime().toLocalDate())
                .isEqualTo(LocalDate.of(2024, 9, 22));
        assertThat(CommonSequences.US.WINTER_SOLSTICE(2024).next().toLocalDateTime().toLocalDate())
                .isEqualTo(LocalDate.of(2024, 12, 21));
    }

    @Test
    void testDST() {
        // DST Start 2024: March 10
        TimePointIterator dstStart = CommonSequences.US.DST_START(2024, 2024);
        assertThat(dstStart.next().toLocalDateTime().toLocalDate()).isEqualTo(LocalDate.of(2024, 3, 10));

        // DST End 2024: Nov 3
        TimePointIterator dstEnd = CommonSequences.US.DST_END(2024, 2024);
        assertThat(dstEnd.next().toLocalDateTime().toLocalDate()).isEqualTo(LocalDate.of(2024, 11, 3));
    }
}

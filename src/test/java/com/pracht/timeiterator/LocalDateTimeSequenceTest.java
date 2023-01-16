package com.pracht.timeiterator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LocalDateTimeSequenceTest {

	private LocalDateTimeSequence timeSequence;

	@BeforeEach
	public void setupBeforeEach() {
		timeSequence = LocalDateTimeSequence.builder()
				.startingPoint(LocalDateTime.of(LocalDate.of(2022, 9, 12), LocalTime.of(20, 00, 0))).cycleCount(4L)
				.cycleUnit(ChronoUnit.YEARS).build();
	}

	@Test
	void testFactoryBuild() {
		assertThat(timeSequence).isNotNull();
		assertThat(timeSequence.getCycleCount()).isEqualTo(4L);
		assertThat(timeSequence.getCycleUnit()).isEqualTo(ChronoUnit.YEARS);
	}

	@Test
	void testStartingPoint() {
		assertThat(timeSequence.hasNext()).isTrue();
		assertThat(timeSequence.next()).isEqualTo(LocalDateTime.of(LocalDate.of(2022, 9, 12), LocalTime.of(20, 00, 0)));
	}

	@Test
	void testHasNextOnInitialization() {
		assertThat(timeSequence.hasNext()).isTrue();
	}

	@Test
	void testPeekFirst() {
		LocalDateTime expected = LocalDateTime.of(LocalDate.of(2022, 9, 12), LocalTime.of(20, 00, 0));
		assertThat(timeSequence.peekNext()).isEqualTo(expected);
	}

	@Test
	void testFirstIncrement() {
		timeSequence.next();
		LocalDateTime firstIncrement = timeSequence.next();
		assertThat(firstIncrement).isEqualTo(LocalDateTime.of(LocalDate.of(2026, 9, 12), LocalTime.of(20, 00, 0)));
	}

	@Test
	void testLastIncrement() {
		timeSequence = LocalDateTimeSequence.builder()
				.startingPoint(LocalDateTime.of(LocalDate.of(2022, 9, 12), LocalTime.of(20, 00, 0))).cycleCount(1L)
				.cycleUnit(ChronoUnit.YEARS).maximumPointCount(1L).build();
		assertThat(timeSequence.hasNext()).isEqualTo(true);
		LocalDateTime firstIncrement = timeSequence.next();
		assertThat(timeSequence.hasNext()).isEqualTo(false);
		assertThatThrownBy(() -> {
			timeSequence.next();
		}).isExactlyInstanceOf(NoSuchElementException.class);
	}

	@Test
	void testLimit5() {
		timeSequence = LocalDateTimeSequence.builder()
				.startingPoint(LocalDateTime.of(LocalDate.of(2022, 9, 12), LocalTime.of(20, 00, 0))).cycleCount(4L)
				.cycleUnit(ChronoUnit.YEARS).maximumPointCount(5L).build();
		List<LocalDateTime> actualLocalDateTime = new ArrayList<LocalDateTime>();
		while (timeSequence.hasNext()) {
			LocalDateTime current = timeSequence.next();
			actualLocalDateTime.add(current);
		}
		assertThat(actualLocalDateTime.size()).isEqualTo(5);
		List<LocalDateTime> expectedLocalDateTime = new ArrayList<LocalDateTime>();
		expectedLocalDateTime.add(LocalDateTime.of(LocalDate.of(2022, 9, 12), LocalTime.of(20, 00, 0)));
		expectedLocalDateTime.add(LocalDateTime.of(LocalDate.of(2026, 9, 12), LocalTime.of(20, 00, 0)));
		expectedLocalDateTime.add(LocalDateTime.of(LocalDate.of(2030, 9, 12), LocalTime.of(20, 00, 0)));
		expectedLocalDateTime.add(LocalDateTime.of(LocalDate.of(2034, 9, 12), LocalTime.of(20, 00, 0)));
		expectedLocalDateTime.add(LocalDateTime.of(LocalDate.of(2038, 9, 12), LocalTime.of(20, 00, 0)));
		assertThat(actualLocalDateTime).isEqualTo(expectedLocalDateTime);
	}

	@Test
	void testLimitToEndDate() {
		timeSequence = LocalDateTimeSequence.builder()
				.startingPoint(LocalDateTime.of(LocalDate.of(2022, 9, 12), LocalTime.of(20, 00, 0))).cycleCount(4L)
				.cycleUnit(ChronoUnit.YEARS).maximumPointCount(5L)
				.endingPoint(LocalDateTime.of(LocalDate.of(2039, 9, 12), LocalTime.of(20, 00, 0))).build();
		List<LocalDateTime> actualLocalDateTime = new ArrayList<LocalDateTime>();
		while (timeSequence.hasNext()) {
			LocalDateTime current = timeSequence.next();
			actualLocalDateTime.add(current);
		}
		System.out.println(actualLocalDateTime.toString());
		assertThat(actualLocalDateTime.size()).isEqualTo(5);
		List<LocalDateTime> expectedLocalDateTime = new ArrayList<LocalDateTime>();
		expectedLocalDateTime.add(LocalDateTime.of(LocalDate.of(2022, 9, 12), LocalTime.of(20, 00, 0)));
		expectedLocalDateTime.add(LocalDateTime.of(LocalDate.of(2026, 9, 12), LocalTime.of(20, 00, 0)));
		expectedLocalDateTime.add(LocalDateTime.of(LocalDate.of(2030, 9, 12), LocalTime.of(20, 00, 0)));
		expectedLocalDateTime.add(LocalDateTime.of(LocalDate.of(2034, 9, 12), LocalTime.of(20, 00, 0)));
		expectedLocalDateTime.add(LocalDateTime.of(LocalDate.of(2038, 9, 12), LocalTime.of(20, 00, 0)));
		assertThat(actualLocalDateTime).isEqualTo(expectedLocalDateTime);
	}

	@Test
	void testLimitToEndDateExactly() {
		timeSequence = LocalDateTimeSequence.builder()
				.startingPoint(LocalDateTime.of(LocalDate.of(2022, 9, 12), LocalTime.of(20, 00, 0))).cycleCount(4L)
				.cycleUnit(ChronoUnit.YEARS).maximumPointCount(5L)
				.endingPoint(LocalDateTime.of(LocalDate.of(2038, 9, 12), LocalTime.of(20, 00, 0))).build();
		List<LocalDateTime> actualLocalDateTime = new ArrayList<LocalDateTime>();
		while (timeSequence.hasNext()) {
			LocalDateTime current = timeSequence.next();
			actualLocalDateTime.add(current);
		}
		assertThat(actualLocalDateTime.size()).isEqualTo(5);
		List<LocalDateTime> expectedLocalDateTime = new ArrayList<LocalDateTime>();
		expectedLocalDateTime.add(LocalDateTime.of(LocalDate.of(2022, 9, 12), LocalTime.of(20, 00, 0)));
		expectedLocalDateTime.add(LocalDateTime.of(LocalDate.of(2026, 9, 12), LocalTime.of(20, 00, 0)));
		expectedLocalDateTime.add(LocalDateTime.of(LocalDate.of(2030, 9, 12), LocalTime.of(20, 00, 0)));
		expectedLocalDateTime.add(LocalDateTime.of(LocalDate.of(2034, 9, 12), LocalTime.of(20, 00, 0)));
		expectedLocalDateTime.add(LocalDateTime.of(LocalDate.of(2038, 9, 12), LocalTime.of(20, 00, 0)));
		assertThat(actualLocalDateTime).isEqualTo(expectedLocalDateTime);
	}

	@Test
	void testStream() {
		timeSequence = LocalDateTimeSequence.builder()
				.startingPoint(LocalDateTime.of(LocalDate.of(2022, 9, 12), LocalTime.of(20, 00, 0))).cycleCount(4L)
				.cycleUnit(ChronoUnit.YEARS).maximumPointCount(5L)
				.endingPoint(LocalDateTime.of(LocalDate.of(2039, 9, 12), LocalTime.of(20, 00, 0))).build();
		List<LocalDateTime> actualLocalDateTime = timeSequence.stream().collect(Collectors.toList());
		assertThat(actualLocalDateTime.size()).isEqualTo(5);
		List<LocalDateTime> expectedLocalDateTime = new ArrayList<LocalDateTime>();
		expectedLocalDateTime.add(LocalDateTime.of(LocalDate.of(2022, 9, 12), LocalTime.of(20, 00, 0)));
		expectedLocalDateTime.add(LocalDateTime.of(LocalDate.of(2026, 9, 12), LocalTime.of(20, 00, 0)));
		expectedLocalDateTime.add(LocalDateTime.of(LocalDate.of(2030, 9, 12), LocalTime.of(20, 00, 0)));
		expectedLocalDateTime.add(LocalDateTime.of(LocalDate.of(2034, 9, 12), LocalTime.of(20, 00, 0)));
		expectedLocalDateTime.add(LocalDateTime.of(LocalDate.of(2038, 9, 12), LocalTime.of(20, 00, 0)));
		assertThat(actualLocalDateTime).isEqualTo(expectedLocalDateTime);

	}

	@Test
	void testToString() {
		timeSequence = LocalDateTimeSequence.builder()
				.startingPoint(LocalDateTime.of(LocalDate.of(2022, 9, 12), LocalTime.of(20, 00, 0))).cycleCount(4L)
				.cycleUnit(ChronoUnit.YEARS).maximumPointCount(5L).dateTimeFormatter(DateTimeFormatter.ISO_LOCAL_DATE)
				.endingPoint(LocalDateTime.of(LocalDate.of(2039, 9, 12), LocalTime.of(20, 00, 0))).build();
		String printableString = timeSequence.toString();
		assertThat(printableString).isNotNull();
		assertThat(printableString).isEqualTo("[2022-09-12, 2026-09-12, 2030-09-12, 2034-09-12, 2038-09-12]");

	}

	@Test
	void testThanksgiving() {

		LocalDateTime firstDayOfNovember = LocalDateTime.of(LocalDate.of(2022, 11, 1), LocalTime.now());
		timeSequence = LocalDateTimeSequence.builder().startingPoint(firstDayOfNovember, 3L, DayOfWeek.THURSDAY)
				.cycleUnit(ChronoUnit.YEARS).cycleCount(1L).maximumPointCount(5L)
				.dateTimeFormatter(DateTimeFormatter.ISO_LOCAL_DATE).build();
		System.out.println(timeSequence.toString());
	}

	@Test
	void testEquals() {
		LocalDateTime firstDayOfNovember = LocalDateTime.of(LocalDate.of(2022, 11, 1), LocalTime.now());
		LocalDateTimeSequence timeIterator1 = LocalDateTimeSequence.builder()
				.startingPoint(firstDayOfNovember, 3L, DayOfWeek.THURSDAY).cycleUnit(ChronoUnit.YEARS).cycleCount(1L)
				.maximumPointCount(5L).dateTimeFormatter(DateTimeFormatter.ISO_LOCAL_DATE).build();

		LocalDateTimeSequence timeIterator2 = LocalDateTimeSequence.builder()
				.startingPoint(firstDayOfNovember, 3L, DayOfWeek.THURSDAY).cycleUnit(ChronoUnit.YEARS).cycleCount(1L)
				.maximumPointCount(5L).dateTimeFormatter(DateTimeFormatter.ISO_LOCAL_DATE).build();
		assertThat(timeIterator1).isEqualTo(timeIterator2);

	}

	@Test
	void testNormalIterator() {
		List<Integer> numbers = new ArrayList<>(List.of(1, 2, 3, 4));
		assertThat(numbers.iterator().hasNext()).isEqualTo(true);

		List<Integer> singleNumberList = new ArrayList<>(List.of(1));
		Iterator<Integer> singleNumberListIterator = singleNumberList.iterator();
		assertThat(singleNumberListIterator.hasNext()).isEqualTo(true);
		Integer first = singleNumberListIterator.next();
		assertThat(first).isNotNull();
		assertThatThrownBy(() -> {
			singleNumberListIterator.next();
		}).isExactlyInstanceOf(NoSuchElementException.class);
	}
	
	@Test
	void testInitializeByDayOfWeek() {
		LocalDateTime firstDayOfNovember = LocalDateTime.of(LocalDate.of(2022, 11, 1), LocalTime.now());
        /* Test search forward for correct day of week */		
		LocalDateTimeSequence timeIterator1 = LocalDateTimeSequence.builder()
				.startingPoint(firstDayOfNovember, 1L, DayOfWeek.THURSDAY).cycleUnit(ChronoUnit.YEARS).cycleCount(1L)
				.maximumPointCount(5L).dateTimeFormatter(DateTimeFormatter.ISO_LOCAL_DATE).build();
		assertThat(timeIterator1.getStartingPoint().toLocalDate()).isEqualTo(LocalDate.of(2022, 11,3));
		
		LocalDateTimeSequence timeIterator2 = LocalDateTimeSequence.builder()
				.startingPoint(firstDayOfNovember, 1L, DayOfWeek.MONDAY).cycleUnit(ChronoUnit.YEARS).cycleCount(1L)
				.maximumPointCount(5L).dateTimeFormatter(DateTimeFormatter.ISO_LOCAL_DATE).build();
		assertThat(timeIterator2.getStartingPoint().toLocalDate()).isEqualTo(LocalDate.of(2022, 11,7));
	}
	
	@Test
	void testGettersAndSetters() {
		LocalDateTime firstDayOfNovember = LocalDateTime.of(LocalDate.of(2022, 11, 1), LocalTime.now());
		LocalDateTimeSequence timeSequence = LocalDateTimeSequence.builder()
				.startingPoint(firstDayOfNovember, 1L, DayOfWeek.THURSDAY).cycleUnit(ChronoUnit.YEARS).cycleCount(1L)
				.maximumPointCount(5L).dateTimeFormatter(DateTimeFormatter.ISO_LOCAL_DATE).build();
		final LocalDateTime expected = LocalDateTime.now();
		timeSequence.setCurrentPoint(expected);
		assertThat(timeSequence.getCurrentPoint()).isEqualTo(expected);
		
		timeSequence.setCurrentPointCount(3L);
		assertThat(timeSequence.getCurrentPointCount()).isEqualTo(3L);
		
		timeSequence.setCycleCount(5L);
		assertThat(timeSequence.getCycleCount()).isEqualTo(5L);
		
		timeSequence.setCycleUnit(ChronoUnit.DAYS);
		assertThat(timeSequence.getCycleUnit()).isEqualTo(ChronoUnit.DAYS);
		
		timeSequence.setDateTimeFormatter(DateTimeFormatter.ISO_DATE);
		assertThat(timeSequence.getDateTimeFormatter()).isEqualTo(DateTimeFormatter.ISO_DATE);
		
	    timeSequence.setEndingPoint(expected);
		assertThat(timeSequence.getEndingPoint()).isEqualTo(expected);
		
		timeSequence.setIncrementing(false);
		assertThat(timeSequence.getIncrementing()).isEqualTo(false);
		
		timeSequence.setMaximumPointCount(7L);
		assertThat(timeSequence.getMaximumPointCount()).isEqualTo(7L);
		
		timeSequence.setStartingPoint(expected);
		assertThat(timeSequence.getStartingPoint()).isEqualTo(expected);
		
	}
	
	@Test
	void testEqualsAndHashCode() {
		LocalDateTime firstDayOfNovember = LocalDateTime.of(LocalDate.of(2022, 11, 1), LocalTime.now());
		LocalDateTime secondDayOfNovember = LocalDateTime.of(LocalDate.of(2022, 11, 2), LocalTime.now());
		LocalDateTimeSequence timeSequence1 = LocalDateTimeSequence.builder()
				.startingPoint(firstDayOfNovember, 1L, DayOfWeek.THURSDAY).cycleUnit(ChronoUnit.YEARS).cycleCount(1L)
				.maximumPointCount(5L).dateTimeFormatter(DateTimeFormatter.ISO_LOCAL_DATE).build();
		LocalDateTimeSequence timeSequence2 = LocalDateTimeSequence.builder()
				.startingPoint(firstDayOfNovember, 1L, DayOfWeek.THURSDAY).cycleUnit(ChronoUnit.YEARS).cycleCount(1L)
				.maximumPointCount(5L).dateTimeFormatter(DateTimeFormatter.ISO_LOCAL_DATE).build();
		assertThat(timeSequence1).isEqualTo(timeSequence2);
		assertThat(timeSequence1.hashCode()).isEqualTo(timeSequence2.hashCode());
		timeSequence1.setStartingPoint(secondDayOfNovember);
		assertThat(timeSequence1).isNotEqualTo(timeSequence2);
		assertThat(timeSequence1.hashCode()).isNotEqualTo(timeSequence2.hashCode());
		timeSequence1.setStartingPoint(firstDayOfNovember);
		timeSequence1.setCycleCount(2L);
		assertThat(timeSequence1).isNotEqualTo(timeSequence2);
		assertThat(timeSequence1.hashCode()).isNotEqualTo(timeSequence2.hashCode());
		timeSequence1.setCycleCount(1L);
		timeSequence1.setCycleUnit(ChronoUnit.MONTHS);
		assertThat(timeSequence1).isNotEqualTo(timeSequence2);
		assertThat(timeSequence1.hashCode()).isNotEqualTo(timeSequence2.hashCode());
	}
}

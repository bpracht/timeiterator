package com.pracht.timeiterator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.pracht.timeiterator.model.TimePoint;

public class TimePointIteratorTest {

	private TimePointSequence timePointSequence1;

	private TimePointSequence timePointSequence2;

	private TimePointIterator timePointIterator;

	private final TimePoint initialStartTimePoint = TimePoint.from(LocalDateTime.of(LocalDate.of(2022, 9, 12),
			LocalTime.of(20, 00, 0)));

	@BeforeEach
	public void setupBeforeEach() {
		timePointSequence1 = TimePointSequence.builder().startingPoint(initialStartTimePoint).cycleCount(1L)
				.cycleUnit(ChronoUnit.MONTHS).build();
		timePointSequence2 = TimePointSequence.builder()
				.startingPoint(TimePoint.from(LocalDateTime.of(LocalDate.of(2022, 9, 20), LocalTime.of(20, 00, 0)))).cycleCount(1L)
				.cycleUnit(ChronoUnit.MONTHS).build();
		timePointIterator = TimePointIterator.builder().startingPoint(initialStartTimePoint)
				.incrementing(true).sequence(timePointSequence1).sequence(timePointSequence2).build();
	}

	@Test
	void testFactoryBuild() {
		assertThat(timePointIterator).isNotNull();
		assertThat(timePointIterator.getStartingPoint())
				.isEqualTo(TimePoint.from(LocalDateTime.of(LocalDate.of(2022, 9, 12), LocalTime.of(20, 00, 0))));
		assertThat(timePointIterator.getSequences().size()).isEqualTo(2);
	}

	@Test
	void testStartingPoint() {
		assertThat(timePointIterator.hasNext()).isTrue();
		assertThat(timePointIterator.peekNext())
				.isEqualTo(TimePoint.from(LocalDateTime.of(LocalDate.of(2022, 9, 12), LocalTime.of(20, 00, 0))));
	}

	@Test
	void testPeekFirst() {
		TimePoint expected = TimePoint.from(LocalDateTime.of(LocalDate.of(2022, 9, 12), LocalTime.of(20, 00, 0)));
		assertThat(timePointIterator.peekNext()).isEqualTo(expected);
	}

	@Test
	void testFirstIncrement() {
		timePointIterator.next();
		TimePoint firstIncrement = timePointIterator.next();
		assertThat(firstIncrement).isEqualTo(TimePoint.from(LocalDateTime.of(LocalDate.of(2022, 9, 20), LocalTime.of(20, 00, 0))));
	}

	@Test
	void testLimit5() {
		TimePointSequence timeSequence1 = TimePointSequence.builder()
				.startingPoint(TimePoint.from(LocalDateTime.of(LocalDate.of(2022, 9, 12), LocalTime.of(20, 00, 0)))).cycleCount(1L)
				.cycleUnit(ChronoUnit.MONTHS).maximumPointCount(5L).build();

		TimePointSequence timeSequence2 = TimePointSequence.builder()
				.startingPoint(TimePoint.from(LocalDateTime.of(LocalDate.of(2022, 9, 13), LocalTime.of(20, 00, 0)))).cycleCount(1L)
				.cycleUnit(ChronoUnit.MONTHS).maximumPointCount(5L).build();

		TimePointIterator localDateTimeIterator = TimePointIterator.builder()
				.startingPoint(initialStartTimePoint).maximumPointCount(5L).sequence(timeSequence1)
				.sequence(timeSequence2).build();

		List<TimePoint> actualTimePoints = new ArrayList<>();
		while (localDateTimeIterator.hasNext()) {
			TimePoint current = localDateTimeIterator.next();
			actualTimePoints.add(current);
		}
		assertThat(actualTimePoints.size()).isEqualTo(5);
		List<TimePoint> expectedTimePoints = new ArrayList<>();
		expectedTimePoints.add(TimePoint.from(LocalDateTime.of(LocalDate.of(2022, 9, 12), LocalTime.of(20, 00, 0))));
		expectedTimePoints.add(TimePoint.from(LocalDateTime.of(LocalDate.of(2022, 9, 13), LocalTime.of(20, 00, 0))));
		expectedTimePoints.add(TimePoint.from(LocalDateTime.of(2022, 10, 12, 20, 0)));
		expectedTimePoints.add(TimePoint.from(LocalDateTime.of(2022, 10, 13, 20, 0)));
		expectedTimePoints.add(TimePoint.from(LocalDateTime.of(2022, 11, 12, 20, 0)));
		assertThat(actualTimePoints).isEqualTo(expectedTimePoints);
	}

	@Test
	void testLimitToEndDate() {
		TimePointSequence timeSequence1 = TimePointSequence.builder()
				.startingPoint(TimePoint.from(LocalDateTime.of(LocalDate.of(2022, 9, 12), LocalTime.of(20, 00, 0)))).cycleCount(1L)
				.cycleUnit(ChronoUnit.MONTHS).maximumPointCount(5L).build();

		TimePointIterator localDateTimeIterator = TimePointIterator.builder()
				.startingPoint(initialStartTimePoint).maximumPointCount(5L).sequence(timeSequence1)
				.endingPoint(TimePoint.from(LocalDateTime.of(LocalDate.of(2022, 10, 15), LocalTime.of(20, 00, 0)))).build();
		List<TimePoint> actualTimePoints = new ArrayList<>();
		while (localDateTimeIterator.hasNext()) {
			TimePoint current = localDateTimeIterator.next();
			actualTimePoints.add(current);
		}
		List<TimePoint> expectedTimePoints = new ArrayList<>();
		expectedTimePoints.add(TimePoint.from(LocalDateTime.of(LocalDate.of(2022, 9, 12), LocalTime.of(20, 00, 0))));
		expectedTimePoints.add(TimePoint.from(LocalDateTime.of(2022, 10, 12, 20, 0)));
		assertThat(actualTimePoints.size()).isEqualTo(2);
		assertThat(actualTimePoints).isEqualTo(expectedTimePoints);
	}

	@Test
	void testDump() {
		TimePointSequence timeSequence1 = TimePointSequence.builder()
				.startingPoint(TimePoint.from(LocalDateTime.of(LocalDate.of(2022, 9, 12), LocalTime.of(20, 00, 0)))).cycleCount(1L)
				.cycleUnit(ChronoUnit.MONTHS).maximumPointCount(5L).build();

		TimePointIterator localDateTimeIterator = TimePointIterator.builder()
				.startingPoint(initialStartTimePoint).maximumPointCount(3L).sequence(timeSequence1)
				.dateTimeFormatter(DateTimeFormatter.ISO_LOCAL_DATE).build();

		String dump = localDateTimeIterator.dump();
		assertThat(dump).isEqualTo("[2022-09-12, 2022-10-12, 2022-11-12]");
	}

	@Test
	void testDumpWithLimit() {
		TimePointSequence timeSequence1 = TimePointSequence.builder()
				.startingPoint(TimePoint.from(LocalDateTime.of(LocalDate.of(2022, 9, 12), LocalTime.of(20, 00, 0)))).cycleCount(1L)
				.cycleUnit(ChronoUnit.MONTHS).maximumPointCount(5L).build();

		TimePointIterator localDateTimeIterator = TimePointIterator.builder()
				.startingPoint(initialStartTimePoint).maximumPointCount(5L).sequence(timeSequence1)
				.dateTimeFormatter(DateTimeFormatter.ISO_LOCAL_DATE).build();

		String dump = localDateTimeIterator.dump(2L);
		assertThat(dump).isEqualTo("[2022-09-12, 2022-10-12]");
	}

	@Test
	void testToString() {
		TimePointIterator localDateTimeIterator = TimePointIterator.builder()
				.startingPoint(initialStartTimePoint).maximumPointCount(5L).incrementing(true).build();
		String summary = localDateTimeIterator.toString();
		assertThat(summary).contains("TimePointIterator");
		assertThat(summary).contains("start=2022-09-12T20:00");
	}

	@Test
	void testLastIncrement() {
		TimePointSequence timeSequence = TimePointSequence.builder()
				.startingPoint(TimePoint.from(LocalDateTime.of(LocalDate.of(2022, 9, 12), LocalTime.of(20, 00, 0)))).cycleCount(1L)
				.cycleUnit(ChronoUnit.YEARS).maximumPointCount(5L).build();
		TimePointIterator localDateTimeIterator = TimePointIterator.builder()
				.startingPoint(initialStartTimePoint).maximumPointCount(1L).sequence(timeSequence)
				.endingPoint(TimePoint.from(LocalDateTime.of(LocalDate.of(2022, 10, 15), LocalTime.of(20, 00, 0)))).build();
		assertThat(localDateTimeIterator.hasNext()).isEqualTo(true);
		TimePoint firstIncrement = localDateTimeIterator.next();
		assertThat(localDateTimeIterator.hasNext()).isEqualTo(false);
		assertThatThrownBy(() -> {
			localDateTimeIterator.next();
		}).isExactlyInstanceOf(NoSuchElementException.class);
	}

}

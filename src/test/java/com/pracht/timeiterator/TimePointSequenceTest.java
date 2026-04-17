package com.pracht.timeiterator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.pracht.timeiterator.model.TimePoint;

public class TimePointSequenceTest {

	private TimePointSequence timeSequence;

	@BeforeEach
	public void setupBeforeEach() {
		timeSequence = TimePointSequence.builder()
				.startingPoint(TimePoint.from(LocalDateTime.of(LocalDate.of(2022, 9, 12), LocalTime.of(20, 00, 0)))).cycleCount(4L)
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
		assertThat(timeSequence.next()).isEqualTo(TimePoint.from(LocalDateTime.of(LocalDate.of(2022, 9, 12), LocalTime.of(20, 00, 0))));
	}

	@Test
	void testHasNextOnInitialization() {
		assertThat(timeSequence.hasNext()).isTrue();
	}

	@Test
	void testPeekFirst() {
		TimePoint expected = TimePoint.from(LocalDateTime.of(LocalDate.of(2022, 9, 12), LocalTime.of(20, 00, 0)));
		assertThat(timeSequence.peekNext()).isEqualTo(expected);
	}

	@Test
	void testFirstIncrement() {
		timeSequence.next();
		TimePoint firstIncrement = timeSequence.next();
		assertThat(firstIncrement).isEqualTo(TimePoint.from(LocalDateTime.of(LocalDate.of(2026, 9, 12), LocalTime.of(20, 00, 0))));
	}

	@Test
	void testLastIncrement() {
		timeSequence = TimePointSequence.builder()
				.startingPoint(TimePoint.from(LocalDateTime.of(LocalDate.of(2022, 9, 12), LocalTime.of(20, 00, 0)))).cycleCount(1L)
				.cycleUnit(ChronoUnit.YEARS).maximumPointCount(1L).build();
		assertThat(timeSequence.hasNext()).isEqualTo(true);
		TimePoint firstIncrement = timeSequence.next();
		assertThat(timeSequence.hasNext()).isEqualTo(false);
		assertThatThrownBy(() -> {
			timeSequence.next();
		}).isExactlyInstanceOf(NoSuchElementException.class);
	}

	@Test
	void testLimit5() {
		timeSequence = TimePointSequence.builder()
				.startingPoint(TimePoint.from(LocalDateTime.of(LocalDate.of(2022, 9, 12), LocalTime.of(20, 00, 0)))).cycleCount(4L)
				.cycleUnit(ChronoUnit.YEARS).maximumPointCount(5L).build();
		List<TimePoint> actualTimePoints = new ArrayList<>();
		while (timeSequence.hasNext()) {
			TimePoint current = timeSequence.next();
			actualTimePoints.add(current);
		}
		assertThat(actualTimePoints.size()).isEqualTo(5);
		List<TimePoint> expectedTimePoints = new ArrayList<>();
		expectedTimePoints.add(TimePoint.from(LocalDateTime.of(2022, 9, 12, 20, 0)));
		expectedTimePoints.add(TimePoint.from(LocalDateTime.of(2026, 9, 12, 20, 0)));
		expectedTimePoints.add(TimePoint.from(LocalDateTime.of(2030, 9, 12, 20, 0)));
		expectedTimePoints.add(TimePoint.from(LocalDateTime.of(2034, 9, 12, 20, 0)));
		expectedTimePoints.add(TimePoint.from(LocalDateTime.of(2038, 9, 12, 20, 0)));
		assertThat(actualTimePoints).isEqualTo(expectedTimePoints);
	}

	@Test
	void testLimitToEndDate() {
		timeSequence = TimePointSequence.builder()
				.startingPoint(TimePoint.from(LocalDateTime.of(LocalDate.of(2022, 9, 12), LocalTime.of(20, 00, 0)))).cycleCount(4L)
				.cycleUnit(ChronoUnit.YEARS).maximumPointCount(5L)
				.endingPoint(TimePoint.from(LocalDateTime.of(LocalDate.of(2039, 9, 12), LocalTime.of(20, 00, 0)))).build();
		List<TimePoint> actualTimePoints = new ArrayList<>();
		while (timeSequence.hasNext()) {
			TimePoint current = timeSequence.next();
			actualTimePoints.add(current);
		}
		assertThat(actualTimePoints.size()).isEqualTo(5);
		List<TimePoint> expectedTimePoints = new ArrayList<>();
		expectedTimePoints.add(TimePoint.from(LocalDateTime.of(2022, 9, 12, 20, 0)));
		expectedTimePoints.add(TimePoint.from(LocalDateTime.of(2026, 9, 12, 20, 0)));
		expectedTimePoints.add(TimePoint.from(LocalDateTime.of(2030, 9, 12, 20, 0)));
		expectedTimePoints.add(TimePoint.from(LocalDateTime.of(2034, 9, 12, 20, 0)));
		expectedTimePoints.add(TimePoint.from(LocalDateTime.of(2038, 9, 12, 20, 0)));
		assertThat(actualTimePoints).isEqualTo(expectedTimePoints);
	}

	@Test
	void testLimitToEndDateExactly() {
		timeSequence = TimePointSequence.builder()
				.startingPoint(TimePoint.from(LocalDateTime.of(LocalDate.of(2022, 9, 12), LocalTime.of(20, 00, 0)))).cycleCount(4L)
				.cycleUnit(ChronoUnit.YEARS).maximumPointCount(5L)
				.endingPoint(TimePoint.from(LocalDateTime.of(LocalDate.of(2038, 9, 12), LocalTime.of(20, 00, 0)))).build();
		List<TimePoint> actualTimePoints = new ArrayList<>();
		while (timeSequence.hasNext()) {
			TimePoint current = timeSequence.next();
			actualTimePoints.add(current);
		}
		assertThat(actualTimePoints.size()).isEqualTo(5);
		List<TimePoint> expectedTimePoints = new ArrayList<>();
		expectedTimePoints.add(TimePoint.from(LocalDateTime.of(2022, 9, 12, 20, 0)));
		expectedTimePoints.add(TimePoint.from(LocalDateTime.of(2026, 9, 12, 20, 0)));
		expectedTimePoints.add(TimePoint.from(LocalDateTime.of(2030, 9, 12, 20, 0)));
		expectedTimePoints.add(TimePoint.from(LocalDateTime.of(2034, 9, 12, 20, 0)));
		expectedTimePoints.add(TimePoint.from(LocalDateTime.of(2038, 9, 12, 20, 0)));
		assertThat(actualTimePoints).isEqualTo(expectedTimePoints);
	}

	@Test
	void testStream() {
		timeSequence = TimePointSequence.builder()
				.startingPoint(TimePoint.from(LocalDateTime.of(LocalDate.of(2022, 9, 12), LocalTime.of(20, 00, 0)))).cycleCount(4L)
				.cycleUnit(ChronoUnit.YEARS).maximumPointCount(5L)
				.endingPoint(TimePoint.from(LocalDateTime.of(LocalDate.of(2039, 9, 12), LocalTime.of(20, 00, 0)))).build();
		List<TimePoint> actualTimePoints = timeSequence.stream().collect(Collectors.toList());
		assertThat(actualTimePoints.size()).isEqualTo(5);
		List<TimePoint> expectedTimePoints = new ArrayList<>();
		expectedTimePoints.add(TimePoint.from(LocalDateTime.of(2022, 9, 12, 20, 0)));
		expectedTimePoints.add(TimePoint.from(LocalDateTime.of(2026, 9, 12, 20, 0)));
		expectedTimePoints.add(TimePoint.from(LocalDateTime.of(2030, 9, 12, 20, 0)));
		expectedTimePoints.add(TimePoint.from(LocalDateTime.of(2034, 9, 12, 20, 0)));
		expectedTimePoints.add(TimePoint.from(LocalDateTime.of(2038, 9, 12, 20, 0)));
		assertThat(actualTimePoints).isEqualTo(expectedTimePoints);
	}

	@Test
	void testDump() {
		timeSequence = TimePointSequence.builder()
				.startingPoint(TimePoint.from(LocalDateTime.of(LocalDate.of(2022, 9, 12), LocalTime.of(20, 00, 0)))).cycleCount(4L)
				.cycleUnit(ChronoUnit.YEARS).maximumPointCount(5L).dateTimeFormatter(DateTimeFormatter.ISO_LOCAL_DATE)
				.endingPoint(TimePoint.from(LocalDateTime.of(LocalDate.of(2039, 9, 12), LocalTime.of(20, 00, 0)))).build();
		String printableString = timeSequence.dump();
		assertThat(printableString).isEqualTo("[2022-09-12, 2026-09-12, 2030-09-12, 2034-09-12, 2038-09-12]");
	}

	@Test
	void testDumpWithLimit() {
		timeSequence = TimePointSequence.builder()
				.startingPoint(TimePoint.from(LocalDateTime.of(LocalDate.of(2022, 9, 12), LocalTime.of(20, 00, 0)))).cycleCount(4L)
				.cycleUnit(ChronoUnit.YEARS).maximumPointCount(5L).dateTimeFormatter(DateTimeFormatter.ISO_LOCAL_DATE)
				.endingPoint(TimePoint.from(LocalDateTime.of(LocalDate.of(2039, 9, 12), LocalTime.of(20, 00, 0)))).build();
		String printableString = timeSequence.dump(2L);
		assertThat(printableString).isEqualTo("[2022-09-12, 2026-09-12]");
	}

	@Test
	void testToString() {
		timeSequence = TimePointSequence.builder()
				.startingPoint(TimePoint.from(LocalDateTime.of(LocalDate.of(2022, 9, 12), LocalTime.of(20, 00, 0)))).cycleCount(4L)
				.cycleUnit(ChronoUnit.YEARS).maximumPointCount(5L).dateTimeFormatter(DateTimeFormatter.ISO_LOCAL_DATE)
				.endingPoint(TimePoint.from(LocalDateTime.of(LocalDate.of(2039, 9, 12), LocalTime.of(20, 00, 0)))).build();
		String summary = timeSequence.toString();
		assertThat(summary).contains("TimePointSequence");
		assertThat(summary).contains("start=2022-09-12T20:00");
	}

	@Test
	void testThanksgiving() {
		LocalDateTime firstDayOfNovember = LocalDateTime.of(LocalDate.of(2022, 11, 1), LocalTime.of(12, 0));
		timeSequence = TimePointSequence.builder().startingPoint(TimePoint.from(firstDayOfNovember), 3L, DayOfWeek.THURSDAY)
				.cycleUnit(ChronoUnit.YEARS).cycleCount(1L).maximumPointCount(5L)
				.dateTimeFormatter(DateTimeFormatter.ISO_LOCAL_DATE).build();
		assertThat(timeSequence.dump()).contains("2022-11-17");
	}

	@Test
	void testEquals() {
		LocalDateTime firstDayOfNovember = LocalDateTime.of(LocalDate.of(2022, 11, 1), LocalTime.of(12, 0));
		TimePointSequence timeIterator1 = TimePointSequence.builder()
				.startingPoint(TimePoint.from(firstDayOfNovember), 3L, DayOfWeek.THURSDAY).cycleUnit(ChronoUnit.YEARS).cycleCount(1L)
				.maximumPointCount(5L).dateTimeFormatter(DateTimeFormatter.ISO_LOCAL_DATE).build();

		TimePointSequence timeIterator2 = TimePointSequence.builder()
				.startingPoint(TimePoint.from(firstDayOfNovember), 3L, DayOfWeek.THURSDAY).cycleUnit(ChronoUnit.YEARS).cycleCount(1L)
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
		LocalDateTime firstDayOfNovember = LocalDateTime.of(LocalDate.of(2022, 11, 1), LocalTime.of(12, 0));
		TimePointSequence timeIterator1 = TimePointSequence.builder()
				.startingPoint(TimePoint.from(firstDayOfNovember), 1L, DayOfWeek.THURSDAY).cycleUnit(ChronoUnit.YEARS).cycleCount(1L)
				.maximumPointCount(5L).dateTimeFormatter(DateTimeFormatter.ISO_LOCAL_DATE).build();
		assertThat(timeIterator1.getStartingPoint().toLocalDateTime().toLocalDate()).isEqualTo(LocalDate.of(2022, 11, 3));
	}

	@Test
	void testGettersAndSetters() {
		LocalDateTime firstDayOfNovember = LocalDateTime.of(LocalDate.of(2022, 11, 1), LocalTime.of(12, 0));
		TimePointSequence timeSequence = TimePointSequence.builder()
				.startingPoint(TimePoint.from(firstDayOfNovember), 1L, DayOfWeek.THURSDAY).cycleUnit(ChronoUnit.YEARS).cycleCount(1L)
				.maximumPointCount(5L).dateTimeFormatter(DateTimeFormatter.ISO_LOCAL_DATE).build();
		final TimePoint expected = TimePoint.now();
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
		LocalDateTime firstDayOfNovember = LocalDateTime.of(LocalDate.of(2022, 11, 1), LocalTime.of(12, 0));
		LocalDateTime secondDayOfNovember = LocalDateTime.of(LocalDate.of(2022, 11, 2), LocalTime.of(12, 0));
		TimePointSequence timeSequence1 = TimePointSequence.builder()
				.startingPoint(TimePoint.from(firstDayOfNovember), 1L, DayOfWeek.THURSDAY).cycleUnit(ChronoUnit.YEARS).cycleCount(1L)
				.maximumPointCount(5L).dateTimeFormatter(DateTimeFormatter.ISO_LOCAL_DATE).build();
		TimePointSequence timeSequence2 = TimePointSequence.builder()
				.startingPoint(TimePoint.from(firstDayOfNovember), 1L, DayOfWeek.THURSDAY).cycleUnit(ChronoUnit.YEARS).cycleCount(1L)
				.maximumPointCount(5L).dateTimeFormatter(DateTimeFormatter.ISO_LOCAL_DATE).build();
		assertThat(timeSequence1).isEqualTo(timeSequence2);
		assertThat(timeSequence1.hashCode()).isEqualTo(timeSequence2.hashCode());
		timeSequence1.setStartingPoint(TimePoint.from(secondDayOfNovember));
		assertThat(timeSequence1).isNotEqualTo(timeSequence2);
		assertThat(timeSequence1.hashCode()).isNotEqualTo(timeSequence2.hashCode());
	}

	@Test
	void testZonedDateTimeSupport() {
		ZoneId zone = ZoneId.of("America/New_York");
		ZonedDateTime startZdt = ZonedDateTime.of(2023, 1, 1, 10, 0, 0, 0, zone);
		TimePointSequence seq = TimePointSequence.builder()
				.startingPoint(TimePoint.from(startZdt))
				.cycleCount(1L)
				.cycleUnit(ChronoUnit.DAYS)
				.maximumPointCount(3L)
				.build();

		List<TimePoint> results = seq.stream().collect(Collectors.toList());
		assertThat(results).hasSize(3);
		assertThat(results.get(0).isZoned()).isTrue();
		assertThat(results.get(0).toZonedDateTime()).isEqualTo(startZdt);
		assertThat(results.get(1).toZonedDateTime()).isEqualTo(startZdt.plusDays(1));
		assertThat(results.get(2).toZonedDateTime()).isEqualTo(startZdt.plusDays(2));
	}

	@Test
	void testPeekNextIncludedChildNoChildren() {
		TimePoint current = TimePoint.from(LocalDateTime.of(2022, 9, 12, 10, 0));
		TimePoint nextChild = timeSequence.peekNextIncludedChild(current);
		assertThat(nextChild).isNull();
	}

	@Test
	void testPeekNextIncludedChildOneChild() {
		TimePoint start = TimePoint.from(LocalDateTime.of(2022, 9, 12, 10, 0));
		TimePointSequence child = TimePointSequence.builder()
				.startingPoint(TimePoint.from(LocalDateTime.of(2022, 9, 13, 10, 0)))
				.build();

		TimePointSequence parent = TimePointSequence.builder()
				.startingPoint(start)
				.includedChildSequence(child)
				.build();

		TimePoint nextChild = parent.peekNextIncludedChild(start);
		assertThat(nextChild).isEqualTo(child.peekNext());
	}

	@Test
	void testPeekNextIncludedChildThreeChildren() {
		TimePoint base = TimePoint.from(LocalDateTime.of(2022, 9, 12, 10, 0));

		TimePointSequence child1 = TimePointSequence.builder()
				.startingPoint(TimePoint.from(LocalDateTime.of(2022, 9, 15, 10, 0)))
				.build();
		TimePointSequence child2 = TimePointSequence.builder()
				.startingPoint(TimePoint.from(LocalDateTime.of(2022, 9, 13, 10, 0)))
				.build();
		TimePointSequence child3 = TimePointSequence.builder()
				.startingPoint(TimePoint.from(LocalDateTime.of(2022, 9, 14, 10, 0)))
				.build();

		TimePointSequence parent = TimePointSequence.builder()
				.startingPoint(base)
				.includedChildSequence(child1)
				.includedChildSequence(child2)
				.includedChildSequence(child3)
				.build();

		TimePoint nextChild = parent.peekNextIncludedChild(base);
		assertThat(nextChild).isEqualTo(child2.peekNext());
	}

	@Test
	void testPeekNextWithIncludedChild() {
		TimePoint start = TimePoint.from(LocalDateTime.of(2022, 9, 12, 10, 0));
		TimePoint includedPoint = TimePoint.from(LocalDateTime.of(2022, 9, 12, 10, 30));

		TimePointSequence includedChild = TimePointSequence.builder()
				.startingPoint(includedPoint)
				.build();

		TimePointSequence parent = TimePointSequence.builder()
				.startingPoint(start)
				.cycleCount(1L)
				.cycleUnit(ChronoUnit.HOURS)
				.includedChildSequence(includedChild)
				.build();

		// First point should be 'start' (10:00)
		assertThat(parent.peekNext()).isEqualTo(start);
		assertThat(parent.next()).isEqualTo(start);

		// parent.currentPoint is now 10:00.
		// nativeNext should be 11:00.
		// includedNext should be 10:30.
		// soonest is 10:30.

		// Second point should be 'includedPoint' (10:30) because it's before the next native increment (11:00)
		assertThat(parent.peekNext()).isEqualTo(includedPoint);
		assertThat(parent.next()).isEqualTo(includedPoint);

		// parent.currentPoint is now 10:30.
		// nativeNext should be 11:00 (next native after 10:30).
		// includedNext should be null (includedPoint was at 10:30, no more increments).

		// Third point should be the next native increment (11:00)
		TimePoint thirdPeek = parent.peekNext();
		assertThat(thirdPeek).isEqualTo(start.plus(1, ChronoUnit.HOURS));
	}

	@Test
	void testPeekNextWithExcludedChild() {
		TimePoint start = TimePoint.from(LocalDateTime.of(2022, 9, 12, 10, 0));
		TimePoint excludedPoint = TimePoint.from(LocalDateTime.of(2022, 9, 12, 11, 0));

		TimePointSequence excludedChild = TimePointSequence.builder()
				.startingPoint(excludedPoint)
				.build();

		TimePointSequence parent = TimePointSequence.builder()
				.startingPoint(start)
				.cycleCount(1L)
				.cycleUnit(ChronoUnit.HOURS)
				.excludedChildSequence(excludedChild)
				.build();

		// First point should be 'start' (10:00)
		assertThat(parent.next()).isEqualTo(start);

		// Second point (11:00) is excluded, so it should return 12:00
		assertThat(parent.peekNext()).isEqualTo(start.plus(2, ChronoUnit.HOURS));
		assertThat(parent.next()).isEqualTo(start.plus(2, ChronoUnit.HOURS));
	}

	@Test
	void testPeekNextComplexUnion() {
		// Native: 10:00, 11:00, 12:00
		// Included: 10:30, 11:30
		// Excluded: 11:00

		TimePoint start = TimePoint.from(LocalDateTime.of(2022, 9, 12, 10, 0));

		TimePointSequence included = TimePointSequence.builder()
				.startingPoint(TimePoint.from(LocalDateTime.of(2022, 9, 12, 10, 30)))
				.cycleCount(1L)
				.cycleUnit(ChronoUnit.HOURS)
				.build();

		TimePointSequence excluded = TimePointSequence.builder()
				.startingPoint(TimePoint.from(LocalDateTime.of(2022, 9, 12, 11, 0)))
				.build();

		TimePointSequence parent = TimePointSequence.builder()
				.startingPoint(start)
				.cycleCount(1L)
				.cycleUnit(ChronoUnit.HOURS)
				.includedChildSequence(included)
				.excludedChildSequence(excluded)
				.build();

		List<TimePoint> result = parent.stream().limit(3).collect(Collectors.toList());

		// Expected: 10:00, 10:30, 11:30 (11:00 is native but excluded)
		assertThat(result.get(0)).isEqualTo(TimePoint.from(LocalDateTime.of(2022, 9, 12, 10, 0)));
		assertThat(result.get(1)).isEqualTo(TimePoint.from(LocalDateTime.of(2022, 9, 12, 10, 30)));
		assertThat(result.get(2)).isEqualTo(TimePoint.from(LocalDateTime.of(2022, 9, 12, 11, 30)));
	}

}

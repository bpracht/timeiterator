package com.pracht.timeiterator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LocalDateTimeIteratorTest {

	private LocalDateTimeSequence localDateTimeSequence1;

	private LocalDateTimeSequence localDateTimeSequence2;

	private LocalDateTimeIterator localDateTimeIterator;

	private final LocalDateTime initialStartLocalDateTime = LocalDateTime.of(LocalDate.of(2022, 9, 12),
			LocalTime.of(20, 00, 0));

	@BeforeEach
	public void setupBeforeEach() {
		localDateTimeSequence1 = LocalDateTimeSequence.builder().startingPoint(initialStartLocalDateTime).cycleCount(1L)
				.cycleUnit(ChronoUnit.MONTHS).build();
		localDateTimeSequence2 = LocalDateTimeSequence.builder()
				.startingPoint(LocalDateTime.of(LocalDate.of(2022, 9, 20), LocalTime.of(20, 00, 0))).cycleCount(1L)
				.cycleUnit(ChronoUnit.MONTHS).build();
		localDateTimeIterator = LocalDateTimeIterator.builder().startingPoint(initialStartLocalDateTime)
				.incrementing(true).sequence(localDateTimeSequence1).sequence(localDateTimeSequence2).build();
	}

	@Test
	void testFactoryBuild() {
		assertThat(localDateTimeIterator).isNotNull();
		assertThat(localDateTimeIterator.getStartingPoint())
				.isEqualTo(LocalDateTime.of(LocalDate.of(2022, 9, 12), LocalTime.of(20, 00, 0)));
		assertThat(localDateTimeIterator.getLocalDateTimeSequences().size()).isEqualTo(2);
	}

	@Test
	void testStartingPoint() {
		assertThat(localDateTimeIterator.hasNext()).isTrue();
		assertThat(localDateTimeIterator.peekNext())
				.isEqualTo(LocalDateTime.of(LocalDate.of(2022, 9, 12), LocalTime.of(20, 00, 0)));
	}

	@Test
	void testPeekFirst() {
		LocalDateTime expected = LocalDateTime.of(LocalDate.of(2022, 9, 12), LocalTime.of(20, 00, 0));
		assertThat(localDateTimeIterator.peekNext()).isEqualTo(expected);
	}

	@Test
	void testFirstIncrement() {
		localDateTimeIterator.next();
		LocalDateTime firstIncrement = localDateTimeIterator.next();
		assertThat(firstIncrement).isEqualTo(LocalDateTime.of(LocalDate.of(2022, 9, 20), LocalTime.of(20, 00, 0)));
	}

	@Test
	void testLimit5() {
		LocalDateTimeSequence timeSequence1 = LocalDateTimeSequence.builder()
				.startingPoint(LocalDateTime.of(LocalDate.of(2022, 9, 12), LocalTime.of(20, 00, 0))).cycleCount(1L)
				.cycleUnit(ChronoUnit.MONTHS).maximumPointCount(5L).build();

		LocalDateTimeSequence timeSequence2 = LocalDateTimeSequence.builder()
				.startingPoint(LocalDateTime.of(LocalDate.of(2022, 9, 13), LocalTime.of(20, 00, 0))).cycleCount(1L)
				.cycleUnit(ChronoUnit.MONTHS).maximumPointCount(5L).build();

		LocalDateTimeIterator localDateTimeIterator = LocalDateTimeIterator.builder()
				.startingPoint(initialStartLocalDateTime).maximumPointCount(5L).sequence(timeSequence1)
				.sequence(timeSequence2).build();

		List<LocalDateTime> actualLocalDateTime = new ArrayList<LocalDateTime>();
		while (localDateTimeIterator.hasNext()) {
			LocalDateTime current = localDateTimeIterator.next();
			actualLocalDateTime.add(current);
		}
		assertThat(actualLocalDateTime.size()).isEqualTo(5);
		List<LocalDateTime> expectedLocalDateTime = new ArrayList<LocalDateTime>();
		expectedLocalDateTime.add(LocalDateTime.of(LocalDate.of(2022, 9, 12), LocalTime.of(20, 00, 0)));
		expectedLocalDateTime.add(LocalDateTime.of(LocalDate.of(2022, 9, 13), LocalTime.of(20, 00, 0)));
		expectedLocalDateTime.add(LocalDateTime.of(LocalDate.of(2022, 10, 12), LocalTime.of(20, 00, 0)));
		expectedLocalDateTime.add(LocalDateTime.of(LocalDate.of(2022, 10, 13), LocalTime.of(20, 00, 0)));
		expectedLocalDateTime.add(LocalDateTime.of(LocalDate.of(2022, 11, 12), LocalTime.of(20, 00, 0)));
		assertThat(actualLocalDateTime).isEqualTo(expectedLocalDateTime);
	}

	@Test
	void testLimitToEndDate() {
		LocalDateTimeSequence timeSequence1 = LocalDateTimeSequence.builder()
				.startingPoint(LocalDateTime.of(LocalDate.of(2022, 9, 12), LocalTime.of(20, 00, 0))).cycleCount(1L)
				.cycleUnit(ChronoUnit.MONTHS).maximumPointCount(5L).build();

		LocalDateTimeIterator localDateTimeIterator = LocalDateTimeIterator.builder()
				.startingPoint(initialStartLocalDateTime).maximumPointCount(5L).sequence(timeSequence1)
				.endingPoint(LocalDateTime.of(LocalDate.of(2022, 10, 15), LocalTime.of(20, 00, 0))).build();
		List<LocalDateTime> actualLocalDateTime = new ArrayList<LocalDateTime>();
		while (localDateTimeIterator.hasNext()) {
			LocalDateTime current = localDateTimeIterator.next();
			actualLocalDateTime.add(current);
		}
		System.out.println(actualLocalDateTime.toString());
		List<LocalDateTime> expectedLocalDateTime = new ArrayList<LocalDateTime>();
		expectedLocalDateTime.add(LocalDateTime.of(LocalDate.of(2022, 9, 12), LocalTime.of(20, 00, 0)));
		expectedLocalDateTime.add(LocalDateTime.of(LocalDate.of(2022, 10, 12), LocalTime.of(20, 00, 0)));
		assertThat(actualLocalDateTime.size()).isEqualTo(2);
		assertThat(actualLocalDateTime).isEqualTo(expectedLocalDateTime);
	}

	@Test
	void testLastIncrement() {
		LocalDateTimeSequence timeSequence = LocalDateTimeSequence.builder()
				.startingPoint(LocalDateTime.of(LocalDate.of(2022, 9, 12), LocalTime.of(20, 00, 0))).cycleCount(1L)
				.cycleUnit(ChronoUnit.YEARS).maximumPointCount(5L).build();
		LocalDateTimeIterator localDateTimeIterator = LocalDateTimeIterator.builder()
				.startingPoint(initialStartLocalDateTime).maximumPointCount(1L).sequence(timeSequence)
				.endingPoint(LocalDateTime.of(LocalDate.of(2022, 10, 15), LocalTime.of(20, 00, 0))).build();
		assertThat(localDateTimeIterator.hasNext()).isEqualTo(true);
		LocalDateTime firstIncrement = localDateTimeIterator.next();
		assertThat(localDateTimeIterator.hasNext()).isEqualTo(false);
		assertThatThrownBy(() -> {
			localDateTimeIterator.next();
		}).isExactlyInstanceOf(NoSuchElementException.class);
	}

}

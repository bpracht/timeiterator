package com.pracht.timeiterator;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder(builderClassName = "Builder")
public class LocalDateTimeIterator implements Iterator<LocalDateTime> {

	private List<LocalDateTimeSequence> localDateTimeSequences;

	private LocalDateTime startingPoint;

	private LocalDateTime currentPoint;

	private Long maximumPointCount;

	private LocalDateTime endingPoint;

	private Long currentPointCount;

	private DateTimeFormatter dateTimeFormatter;

	private Boolean incrementing;

	public LocalDateTimeSequence findNextSequence() {
		Iterator<LocalDateTimeSequence> localDateTimeSequenceIterators = localDateTimeSequences.iterator();
		LocalDateTimeSequence nextLocalDateTimeSequence = null;
		LocalDateTime nextLocalDateTime = startingPoint;
		LocalDateTimeSequence currentLocalDateTimeSequence = null;
		LocalDateTime currentLocalDateTime = startingPoint;
		if (getIncrementing()) {
			if (localDateTimeSequenceIterators.hasNext()) {
				nextLocalDateTimeSequence = localDateTimeSequenceIterators.next();
				nextLocalDateTime = nextLocalDateTimeSequence.peekNext();
				while (localDateTimeSequenceIterators.hasNext()) {
					currentLocalDateTimeSequence = localDateTimeSequenceIterators.next();
					currentLocalDateTime = currentLocalDateTimeSequence.peekNext();
					if (currentLocalDateTime != null && currentLocalDateTime.isBefore(nextLocalDateTime)) {
						nextLocalDateTime = currentLocalDateTime;
						nextLocalDateTimeSequence = currentLocalDateTimeSequence;
					}
				}
			}
		} else {
			if (localDateTimeSequenceIterators.hasNext()) {
				nextLocalDateTimeSequence = localDateTimeSequenceIterators.next();
				nextLocalDateTime = nextLocalDateTimeSequence.peekNext();
				while (localDateTimeSequenceIterators.hasNext()) {
					currentLocalDateTimeSequence = localDateTimeSequenceIterators.next();
					currentLocalDateTime = currentLocalDateTimeSequence.peekNext();
					if (currentLocalDateTime != null && currentLocalDateTime.isAfter(nextLocalDateTime)) {
						nextLocalDateTime = currentLocalDateTime;
						nextLocalDateTimeSequence = currentLocalDateTimeSequence;
					}
				}
			}
		}
		return nextLocalDateTimeSequence;
	}

	public LocalDateTime peekNext() {
		LocalDateTime result = null;
		if (!hasEndBeenReached()) {
			LocalDateTimeSequence nextLocalDateTimeSequence = findNextSequence();
			result = nextLocalDateTimeSequence.peekNext();
		}
		return result;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this)
			return true;
		LocalDateTimeIterator otherTimeIterator = (LocalDateTimeIterator) o;
		return localDateTimeSequences.equals(otherTimeIterator.localDateTimeSequences)
				&& startingPoint.equals(otherTimeIterator.startingPoint)
				&& incrementing.equals(otherTimeIterator.incrementing);
	}

	@Override
	public int hashCode() {
		return Objects.hash(localDateTimeSequences, startingPoint, incrementing);
	}

	public static class Builder {

		public Builder startingPoint(LocalDateTime startingPoint, long occurrenceNumber, DayOfWeek dayOfWeek) {
			LocalDateTime actualStartingPoint = startingPoint;
			int advancementInDays = 0;
			if (!startingPoint.getDayOfWeek().equals(dayOfWeek)) {
				advancementInDays = dayOfWeek.getValue() - startingPoint.getDayOfWeek().getValue();
				advancementInDays = (advancementInDays < 0) ? 7 + advancementInDays : advancementInDays;
			}
			advancementInDays += (occurrenceNumber - 1) * 7;
			actualStartingPoint = startingPoint.plus(advancementInDays, ChronoUnit.DAYS);
			this.startingPoint = actualStartingPoint;
			return this;
		}

		public Builder startingPoint(LocalDateTime startingPoint) {
			this.startingPoint = startingPoint;
			return this;
		}

		public Builder localDateTimeSequence(LocalDateTimeSequence sequence) {
			if (localDateTimeSequences == null) {
				localDateTimeSequences = new LinkedList<>();
			}
			localDateTimeSequences.add(sequence);
			return this;
		}

		public Builder sequence(LocalDateTimeSequence sequence) {
			return localDateTimeSequence(sequence);
		}

		private void setDefaults() {
			dateTimeFormatter = (dateTimeFormatter == null) ? DateTimeFormatter.ISO_LOCAL_DATE_TIME : dateTimeFormatter;
			incrementing = (incrementing == null) ? true : incrementing;
			if (localDateTimeSequences == null) {
				localDateTimeSequences = new LinkedList<>();
			}
		}

		LocalDateTimeIterator build() {
			setDefaults();
			if (startingPoint == null) {
				throw new IllegalArgumentException("Missing startingPoint");
			}
			return new LocalDateTimeIterator(localDateTimeSequences, startingPoint, currentPoint, maximumPointCount,
					endingPoint, currentPointCount, dateTimeFormatter, incrementing);
		}
	}

	private boolean isEndDefined() {
		return maximumPointCount != null || endingPoint != null;
	}

	private boolean hasEndBeenReached() {
		boolean result = false;
		if (isEndDefined()) {
			LocalDateTimeSequence testLocalDateTimeSequence = findNextSequence();
			LocalDateTime testLocalDateTime = testLocalDateTimeSequence.peekNext();
			if (endingPoint != null) {
				result |= (getIncrementing() && testLocalDateTime.isAfter(endingPoint));
				result |= (!getIncrementing() && endingPoint.isBefore(testLocalDateTime));
			}
			if (maximumPointCount != null && currentPointCount != null) {
				result |= currentPointCount >= maximumPointCount;
			}
		}
		return result;
	}

	@Override
	public boolean hasNext() {
		return peekNext() != null;
	}

	@Override
	public LocalDateTime next() {
		if (!hasEndBeenReached()) {
			LocalDateTimeSequence nextLocalDateTimeSequence = findNextSequence();
			currentPoint = nextLocalDateTimeSequence.next();
			currentPointCount = (currentPointCount == null) ? 1 : currentPointCount + 1;
		} else {
			throw new NoSuchElementException();
		}
		return currentPoint;
	}

	public Stream<LocalDateTime> stream() {
		Spliterator<LocalDateTime> spliterator = Spliterators.spliteratorUnknownSize(this, Spliterator.ORDERED);
		Stream<LocalDateTime> result = StreamSupport.stream(spliterator, false);
		return result;
	}

	@Override
	public String toString() {
		StringBuilder resultBuilder = new StringBuilder();
		DateTimeFormatter dateTimeFormatter = (this.dateTimeFormatter == null) ? DateTimeFormatter.ISO_LOCAL_DATE_TIME
				: this.dateTimeFormatter;
		resultBuilder.append(stream().map(localDateTime -> dateTimeFormatter.format(localDateTime))
				.collect(Collectors.joining(", ", "[", "]")));
		return resultBuilder.toString();
	}

}

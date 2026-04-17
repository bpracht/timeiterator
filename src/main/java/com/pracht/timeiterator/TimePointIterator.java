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

import com.pracht.timeiterator.model.TimePoint;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * TimePointIterator provides a way to iterate through multiple {@link TimePointSequence}s
 * in a unified chronological stream. It ensures that points from all sequences are
 * returned in order (ascending or descending) and handles limit constraints.
 */
@Data
@AllArgsConstructor
@Builder(builderClassName = "Builder")
public class TimePointIterator implements Iterator<TimePoint> {

	private List<TimePointSequence> sequences;

	private TimePoint startingPoint;

	private TimePoint currentPoint;

	private Long maximumPointCount;

	private TimePoint endingPoint;

	private Long currentPointCount;

	private DateTimeFormatter dateTimeFormatter;

	private Boolean incrementing;

	/**
	 * Identifies the sequence that contains the next chronological point.
	 * 
	 * @return The TimePointSequence containing the next point, or null if none remain.
	 */
	public TimePointSequence findNextSequence() {
		Iterator<TimePointSequence> sequenceIterators = sequences.iterator();
		TimePointSequence nextSequence = null;
		TimePoint nextPoint = startingPoint;
		TimePointSequence currentSequence = null;
		TimePoint currentPointValue = startingPoint;
		if (getIncrementing()) {
			if (sequenceIterators.hasNext()) {
				nextSequence = sequenceIterators.next();
				nextPoint = nextSequence.peekNext();
				while (sequenceIterators.hasNext()) {
					currentSequence = sequenceIterators.next();
					currentPointValue = currentSequence.peekNext();
					if (currentPointValue != null && (nextPoint == null || currentPointValue.isBefore(nextPoint))) {
						nextPoint = currentPointValue;
						nextSequence = currentSequence;
					}
				}
			}
		} else {
			if (sequenceIterators.hasNext()) {
				nextSequence = sequenceIterators.next();
				nextPoint = nextSequence.peekNext();
				while (sequenceIterators.hasNext()) {
					currentSequence = sequenceIterators.next();
					currentPointValue = currentSequence.peekNext();
					if (currentPointValue != null && (nextPoint == null || currentPointValue.isAfter(nextPoint))) {
						nextPoint = currentPointValue;
						nextSequence = currentSequence;
					}
				}
			}
		}
		return nextSequence;
	}

	/**
	 * Peeks at the next chronological point across all monitored sequences.
	 * 
	 * @return The next TimePoint, or null if the end is reached.
	 */
	public TimePoint peekNext() {
		TimePoint resultPoint = null;
		if (!hasEndBeenReached()) {
			TimePointSequence nextSequence = findNextSequence();
			if (nextSequence != null) {
				resultPoint = nextSequence.peekNext();
			}
		}
		return resultPoint;
	}

	@Override
	public boolean equals(final Object otherObject) {
		if (otherObject == this)
			return true;
		if (otherObject == null || getClass() != otherObject.getClass()) return false;
		TimePointIterator otherTimePointIterator = (TimePointIterator) otherObject;
		return Objects.equals(sequences, otherTimePointIterator.sequences)
				&& Objects.equals(startingPoint, otherTimePointIterator.startingPoint)
				&& Objects.equals(incrementing, otherTimePointIterator.incrementing);
	}

	@Override
	public int hashCode() {
		return Objects.hash(sequences, startingPoint, incrementing);
	}

	/**
	 * Builder for TimePointIterator.
	 */
	public static class Builder {

		/**
		 * Configures the starting point to be the Nth occurrence of a specific day of week.
		 * 
		 * @param startingPoint The anchor point to start searching from.
		 * @param occurrenceNumber The Nth occurrence (e.g., 3 for 3rd Monday).
		 * @param dayOfWeek The day of week to find.
		 * @return this builder.
		 */
		public Builder startingPoint(TimePoint startingPoint, long occurrenceNumber, DayOfWeek dayOfWeek) {
			TimePoint actualStartingPoint = startingPoint;
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

		/**
		 * Sets the starting point.
		 * @param startingPoint The point to start from.
		 * @return this builder.
		 */
		public Builder startingPoint(TimePoint startingPoint) {
			this.startingPoint = startingPoint;
			return this;
		}

		/**
		 * Adds a sequence to be monitored by this iterator.
		 * @param sequence The sequence to add.
		 * @return this builder.
		 */
		public Builder timePointSequence(TimePointSequence sequence) {
			if (sequences == null) {
				sequences = new LinkedList<>();
			}
			sequences.add(sequence);
			return this;
		}

		/**
		 * Alias for {@link #timePointSequence(TimePointSequence)}.
		 * @param sequence The sequence to add.
		 * @return this builder.
		 */
		public Builder sequence(TimePointSequence sequence) {
			return timePointSequence(sequence);
		}

		private void setDefaults() {
			dateTimeFormatter = (dateTimeFormatter == null) ? DateTimeFormatter.ISO_LOCAL_DATE_TIME : dateTimeFormatter;
			incrementing = (incrementing == null) ? true : incrementing;
			if (sequences == null) {
				sequences = new LinkedList<>();
			}
		}

		/**
		 * Builds the TimePointIterator.
		 * @return A new TimePointIterator instance.
		 */
		TimePointIterator build() {
			setDefaults();
			if (startingPoint == null) {
				throw new IllegalArgumentException("Missing startingPoint");
			}
			return new TimePointIterator(sequences, startingPoint, currentPoint, maximumPointCount,
					endingPoint, currentPointCount, dateTimeFormatter, incrementing);
		}
	}

	private boolean isEndDefined() {
		return maximumPointCount != null || endingPoint != null;
	}

	private boolean hasEndBeenReached() {
		boolean result = false;
		if (isEndDefined()) {
			TimePointSequence testSequence = findNextSequence();
			if (testSequence == null) {
				return true;
			}
			TimePoint testPoint = testSequence.peekNext();
			if (testPoint == null) {
				return true;
			}
			if (endingPoint != null) {
				result |= (getIncrementing() && testPoint.isAfter(endingPoint));
				result |= (!getIncrementing() && endingPoint.isBefore(testPoint));
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

	/**
	 * Advances to and returns the next chronological point across all monitored sequences.
	 * 
	 * @return The next TimePoint.
	 * @throws NoSuchElementException if no more points are available.
	 */
	@Override
	public TimePoint next() {
		if (!hasEndBeenReached()) {
			TimePointSequence nextSequence = findNextSequence();
			if (nextSequence == null) {
				throw new NoSuchElementException();
			}
			currentPoint = nextSequence.next();
			currentPointCount = (currentPointCount == null) ? 1 : currentPointCount + 1;
		} else {
			throw new NoSuchElementException();
		}
		return currentPoint;
	}

	/**
	 * Returns a sequential Stream of TimePoints from this iterator.
	 * 
	 * @return A Stream of TimePoint.
	 */
	public Stream<TimePoint> stream() {
		Spliterator<TimePoint> spliterator = Spliterators.spliteratorUnknownSize(this, Spliterator.ORDERED);
		Stream<TimePoint> resultStream = StreamSupport.stream(spliterator, false);
		return resultStream;
	}

	/**
	 * Returns a string representation of all points in the iterator.
	 * @return A formatted string of points.
	 */
	public String dump() {
		return dump(null);
	}

	/**
	 * Returns a string representation of points in the iterator up to a maximum count.
	 * 
	 * @param maximumCount The maximum number of points to include.
	 * @return A formatted string of points.
	 */
	public String dump(Long maximumCount) {
		Stream<TimePoint> streamToDump = stream();
		if (maximumCount != null) {
			streamToDump = streamToDump.limit(maximumCount);
		}
		DateTimeFormatter formatter = (this.dateTimeFormatter == null) ? DateTimeFormatter.ISO_LOCAL_DATE_TIME
				: this.dateTimeFormatter;
		return streamToDump.map(timePoint -> timePoint.isZoned() ? formatter.format(timePoint.toZonedDateTime()) : formatter.format(timePoint.toLocalDateTime()))
				.collect(Collectors.joining(", ", "[", "]"));
	}

	@Override
	public String toString() {
		return String.format("TimePointIterator[start=%s, direction=%s, max=%s]",
				startingPoint,
				(incrementing != null && incrementing) ? "FORWARD" : "BACKWARD",
				maximumPointCount != null ? maximumPointCount : "NONE");
	}

}

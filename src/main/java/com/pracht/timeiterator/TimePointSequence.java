package com.pracht.timeiterator;

import java.time.DayOfWeek;
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

import com.pracht.timeiterator.model.EventRelationship;
import com.pracht.timeiterator.model.TimePoint;
import static com.pracht.timeiterator.model.EventRelationship.*;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * TimePointSequence models a series of points in time based on a starting point,
 * an increment size (cycle), and optional included or excluded child sequences.
 * It implements {@link Iterator} to provide a stream-like interface for time iteration.
 * 
 * @author bpracht
 */
@Data
@AllArgsConstructor
@Builder(builderClassName = "Builder", buildMethodName = "build")
public class TimePointSequence implements Iterator<TimePoint> {
	private TimePoint startingPoint;

	private TemporalUnit cycleUnit;

	private Long cycleCount;
	
	private Long eventDuration;
	
	private TemporalUnit eventDurationUnit;
	
	private EventRelationship eventRelationship;

	private TimePoint currentPoint;

	private Long maximumPointCount;

	private TimePoint endingPoint;

	private Long currentPointCount;

	private DateTimeFormatter dateTimeFormatter;

	private Boolean incrementing;
	
	private List<TimePointSequence> includedChildSequences = new LinkedList<>();

	private List<TimePointSequence> excludedChildSequences = new LinkedList<>();
	
	/**
	 * Returns the finish point of the current event based on its duration.
	 * @return The finish TimePoint.
	 */
	public TimePoint peekFinish() {
		return currentPoint.plus(eventDuration, eventDurationUnit);
	}

	/**
	 * Peeks at the next point in the sequence without advancing the iterator.
	 * This considers native increments, included children, and exclusions.
	 * 
	 * @return The next TimePoint, or null if the end is reached.
	 */
	public TimePoint peekNext() {
		return peekNextAfter(currentPoint);
	}

	/**
	 * Peeks at the next valid point strictly after the specified reference point.
	 * 
	 * @param referencePoint The point to look after.
	 * @return The next valid TimePoint, or null.
	 */
	public TimePoint peekNextAfter(TimePoint referencePoint) {
		if (hasEndBeenReached()) {
			return null;
		}

		TimePoint nativeNext = peekNativeNext(referencePoint);
		TimePoint includedNext = peekNextIncludedChild(referencePoint);

		TimePoint candidate = soonest(nativeNext, includedNext, referencePoint);
		if (candidate == null) {
			return null;
		}

		TimePoint excludedNext = peekNextExcludedChild(referencePoint);
		if (excludedNext != null && candidate.equals(excludedNext)) {
			// Skip this candidate and find the next one after it
			return peekNextAfter(candidate);
		}

		return candidate;
	}

	private TimePoint soonest(TimePoint point1, TimePoint point2, TimePoint referencePoint) {
		if (point1 == null) return point2;
		if (point2 == null) return point1;
		return isBetterCandidate(point1, referencePoint, point2) ? point1 : point2;
	}

	private TimePoint peekNativeNext(TimePoint referencePoint) {
		if (referencePoint == null) {
			return startingPoint;
		}

		if (getIncrementing()) {
			if (referencePoint.isBefore(startingPoint)) {
				return startingPoint;
			}
			
			TimePoint point = startingPoint;
			// Search for the first native point strictly AFTER referencePoint
			while (point != null && !point.isAfter(referencePoint)) {
				point = calculateNextPoint(point);
			}

			if (point != null && endingPoint != null && point.isAfter(endingPoint)) {
				return null;
			}
			return point;
		} else {
			if (referencePoint.isAfter(startingPoint)) {
				return startingPoint;
			}
			TimePoint point = startingPoint;
			while (point != null && !point.isBefore(referencePoint)) {
				point = calculateNextPoint(point);
			}
			if (point != null && endingPoint != null && point.isBefore(endingPoint)) {
				return null;
			}
			return point;
		}
	}

	private TimePoint calculateNextPoint(TimePoint fromPoint) {
		if (fromPoint == null) return startingPoint;
		if (getIncrementing()) {
			switch(eventRelationship) {
			case FINISH_TO_START:
				return fromPoint.plus(eventDuration, eventDurationUnit).plus(cycleCount, cycleUnit);
			default:
				return fromPoint.plus(cycleCount, cycleUnit);
			}
		} else {
			return fromPoint.minus(cycleCount, cycleUnit);
		}
	}

	/**
	 * Recursively finds the soonest point from all included child sequences.
	 * 
	 * @param currentTimePoint The current time context.
	 * @return The soonest included point, or null.
	 */
	public TimePoint peekNextIncludedChild(TimePoint currentTimePoint) {
		return peekNextChildFromList(includedChildSequences, currentTimePoint);
	}

	/**
	 * Recursively finds the soonest point from all excluded child sequences.
	 * 
	 * @param currentTimePoint The current time context.
	 * @return The soonest excluded point, or null.
	 */
	public TimePoint peekNextExcludedChild(TimePoint currentTimePoint) {
		return peekNextChildFromList(excludedChildSequences, currentTimePoint);
	}

	private TimePoint peekNextChildFromList(List<TimePointSequence> listToSearch, TimePoint currentTimePoint) {
		TimePoint bestNext = null;
		for (TimePointSequence child : listToSearch) {
			TimePoint childNext = child.peekNextAfter(currentTimePoint);
			if (childNext != null) {
				if (isBetterCandidate(childNext, currentTimePoint, bestNext)) {
					bestNext = childNext;
				}
			}
		}
		return bestNext;
	}

	private boolean isBetterCandidate(TimePoint candidate, TimePoint currentTimePoint, TimePoint currentBest) {
		// Candidate must be after currentTimePoint (if incrementing) or before (if decrementing)
		if (getIncrementing()) {
			if (currentTimePoint != null && !candidate.isAfter(currentTimePoint)) return false;
			if (currentBest == null) return true;
			return candidate.isBefore(currentBest);
		} else {
			if (currentTimePoint != null && !candidate.isBefore(currentTimePoint)) return false;
			if (currentBest == null) return true;
			return candidate.isAfter(currentBest);
		}
	}

	@Override
	public boolean equals(final Object otherObject) {
		if (otherObject == this)
			return true;
		if (otherObject == null || getClass() != otherObject.getClass()) return false;
		TimePointSequence otherTimeIterator = (TimePointSequence) otherObject;
		return Objects.equals(startingPoint, otherTimeIterator.startingPoint) && Objects.equals(cycleUnit, otherTimeIterator.cycleUnit)
				&& Objects.equals(cycleCount, otherTimeIterator.cycleCount) && Objects.equals(eventDuration, otherTimeIterator.eventDuration) 
				&& Objects.equals(eventDurationUnit, otherTimeIterator.eventDurationUnit) && Objects.equals(eventRelationship, otherTimeIterator.eventRelationship)
				&& Objects.equals(includedChildSequences, otherTimeIterator.includedChildSequences)
				&& Objects.equals(excludedChildSequences, otherTimeIterator.excludedChildSequences);
	}

	@Override
	public int hashCode() {
		return Objects.hash(startingPoint, cycleUnit, cycleCount, eventDuration, eventDurationUnit, eventRelationship, includedChildSequences, excludedChildSequences);
	}

	/**
	 * Builder for TimePointSequence.
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
		public Builder startingPoint(TimePoint startingPoint, long occurrenceNumber,
				DayOfWeek dayOfWeek) {
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
		 * Adds a child sequence whose points will be included in the iteration.
		 * @param sequence The sequence to include.
		 * @return this builder.
		 */
		public Builder includedChildSequence(TimePointSequence sequence) {
			if (includedChildSequences == null) {
				includedChildSequences = new LinkedList<>();
			}
			includedChildSequences.add(sequence);
			return this;
		}

		/**
		 * Adds a child sequence whose points will be excluded (skipped) from the iteration.
		 * @param sequence The sequence to exclude.
		 * @return this builder.
		 */
		public Builder excludedChildSequence(TimePointSequence sequence) {
			if (excludedChildSequences == null) {
				excludedChildSequences = new LinkedList<>();
			}
			excludedChildSequences.add(sequence);
			return this;
		}

		private void setDefaults() {
			cycleCount = (cycleCount == null) ? 1L : cycleCount;
			cycleUnit = (cycleUnit == null) ? ChronoUnit.DAYS : cycleUnit;
			dateTimeFormatter = (dateTimeFormatter == null) ? DateTimeFormatter.ISO_LOCAL_DATE_TIME : dateTimeFormatter;
			incrementing = (incrementing == null) ? true : incrementing;
			includedChildSequences = (includedChildSequences == null) ? new LinkedList<>() : includedChildSequences;
			excludedChildSequences = (excludedChildSequences == null) ? new LinkedList<>() : excludedChildSequences;
			eventDuration = (eventDuration == null) ? 1L : eventDuration;
			eventDurationUnit = (eventDurationUnit == null) ? ChronoUnit.DAYS : eventDurationUnit;
			eventRelationship = (eventRelationship == null) ? START_TO_START : eventRelationship;
		}

		/**
		 * Builds the TimePointSequence.
		 * @return A new TimePointSequence.
		 */
		TimePointSequence build() {
			setDefaults();
			if (startingPoint == null) {
				throw new IllegalArgumentException("Missing startingPoint");
			}
			if (cycleCount == null) {
				throw new IllegalArgumentException("missing cycleCount");
			}
			if (cycleUnit == null) {
				throw new IllegalArgumentException("missing cycleUnit");
			}

			return new TimePointSequence(startingPoint, cycleUnit, cycleCount, eventDuration, eventDurationUnit, eventRelationship, currentPoint, maximumPointCount,
					endingPoint, currentPointCount, dateTimeFormatter, incrementing, includedChildSequences, excludedChildSequences);
		}
	}

	private boolean isEndDefined() {
		return maximumPointCount != null || endingPoint != null;
	}

	private boolean hasEndBeenReached() {
		boolean result = false;
		if (isEndDefined()) {
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
	 * Advances to and returns the next valid TimePoint in the sequence.
	 * 
	 * @return The next TimePoint.
	 * @throws NoSuchElementException if no more points are available.
	 */
	@Override
	public TimePoint next() {
		if (hasNext()) {
			TimePoint result = peekNext();
			
			// We need to advance all child iterators to 'result'
			// so that subsequent calls to peekNextAfter(result) don't see them.
			advanceChildIterators(result);

			currentPoint = result;
			currentPointCount = (currentPointCount == null) ? 1 : currentPointCount + 1;
			return result;
		} else {
			throw new NoSuchElementException();
		}
	}

	private void advanceChildIterators(TimePoint resultPoint) {
		for (TimePointSequence child : includedChildSequences) {
			advanceChildTo(child, resultPoint);
		}
		for (TimePointSequence child : excludedChildSequences) {
			advanceChildTo(child, resultPoint);
		}
	}

	private void advanceChildTo(TimePointSequence child, TimePoint targetPoint) {
		while (child.hasNext()) {
			TimePoint nextPoint = child.peekNext();
			if (getIncrementing()) {
				if (nextPoint.isAfter(targetPoint)) break;
			} else {
				if (nextPoint.isBefore(targetPoint)) break;
			}
			child.next();
		}
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
	 * Returns a string representation of all points in the sequence.
	 * @return A formatted string of points.
	 */
	public String dump() {
		return dump(null);
	}

	/**
	 * Returns a string representation of points in the sequence up to a maximum count.
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
		return String.format("TimePointSequence[start=%s, direction=%s, max=%s]",
				startingPoint,
				(incrementing != null && incrementing) ? "FORWARD" : "BACKWARD",
				maximumPointCount != null ? maximumPointCount : "NONE");
	}

}

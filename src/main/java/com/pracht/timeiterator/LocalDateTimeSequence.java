package com.pracht.timeiterator;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Period;
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
import static com.pracht.timeiterator.model.EventRelationship.*;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * LocalDateTimeSequence
 * Class that models a start LocalDateTime, a direction and a unit of change.
 * 
 * @author bpracht
 *
 */
@Data
@AllArgsConstructor
@Builder(builderClassName = "Builder", buildMethodName = "build")
public class LocalDateTimeSequence implements Iterator<LocalDateTime> {
	private LocalDateTime startingPoint;

	private TemporalUnit cycleUnit;

	private Long cycleCount;
	
	private Long eventDuration;
	
	private TemporalUnit eventDurationUnit;
	
	private EventRelationship eventRelationship;

	private LocalDateTime currentPoint;

	private Long maximumPointCount;

	private LocalDateTime endingPoint;

	private Long currentPointCount;

	private DateTimeFormatter dateTimeFormatter;

	private Boolean incrementing;
	
	private List<LocalDateTimeSequence> unionedSequences = new LinkedList<>();
	
	public LocalDateTime peekFinish() {
		return currentPoint.plus(eventDuration, eventDurationUnit);
	}

	public LocalDateTime peekNext() {
		LocalDateTime result = null;
		if (!hasEndBeenReached()) {
			if (currentPoint == null) {
				result = startingPoint;
			} else {
				if (getIncrementing()) {
					switch(eventRelationship) {
					case FINISH_TO_START:
						result = peekFinish().plus(cycleCount, cycleUnit);
						break;
					case FINISH_TO_FINISH:
						result = currentPoint.plus(cycleCount, cycleUnit);
						break;
					case START_TO_FINISH:
						result = currentPoint.plus(cycleCount, cycleUnit);
						break;
					case START_TO_START:
						result = currentPoint.plus(cycleCount, cycleUnit);
						break;
					}
				} else {
					result = currentPoint.minus(cycleCount, cycleUnit);
				}
			}
		}

		return result;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this)
			return true;
		LocalDateTimeSequence otherTimeIterator = (LocalDateTimeSequence) o;
		return startingPoint.equals(otherTimeIterator.startingPoint) && cycleUnit.equals(otherTimeIterator.cycleUnit)
				&& cycleCount.equals(otherTimeIterator.cycleCount) && eventDuration.equals(otherTimeIterator.eventDuration) && eventDurationUnit.equals(otherTimeIterator.eventDurationUnit) && eventRelationship.equals(otherTimeIterator.eventRelationship);
	}

	@Override
	public int hashCode() {
		return Objects.hash(startingPoint, cycleUnit, cycleCount, eventDuration, eventDurationUnit,eventRelationship);
	}

	public static class Builder {

		public Builder startingPoint(LocalDateTime startingPoint, long occurrenceNumber,
				DayOfWeek dayOfWeek) {
			LocalDateTime actualStartingPoint = startingPoint;
			int advancementInDays = 0;
			if (!startingPoint.getDayOfWeek().equals(dayOfWeek)) {
				advancementInDays = dayOfWeek.getValue() - startingPoint.getDayOfWeek().getValue();
				advancementInDays = (advancementInDays <0)?7+advancementInDays:advancementInDays;
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

		private void setDefaults() {
			cycleCount = (cycleCount == null) ? 1L : cycleCount;
			cycleUnit = (cycleUnit == null) ? ChronoUnit.DAYS : cycleUnit;
			dateTimeFormatter = (dateTimeFormatter == null) ? DateTimeFormatter.ISO_LOCAL_DATE_TIME : dateTimeFormatter;
			incrementing = (incrementing == null) ? true : incrementing;
			unionedSequences = new LinkedList<>();
			eventDuration = (eventDuration == null) ? 1L : eventDuration;
			eventDurationUnit = (eventDurationUnit == null) ? ChronoUnit.DAYS : eventDurationUnit;
			eventRelationship = (eventRelationship == null) ? START_TO_START : eventRelationship;
		}

		LocalDateTimeSequence build() {
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

			return new LocalDateTimeSequence(startingPoint, cycleUnit, cycleCount, eventDuration, eventDurationUnit, eventRelationship, currentPoint, maximumPointCount,
					endingPoint, currentPointCount, dateTimeFormatter, incrementing, unionedSequences);
		}
	}

	private boolean isEndDefined() {
		return maximumPointCount != null || endingPoint != null;
	}

	private boolean hasEndBeenReached() {
		boolean result = false;
		if (isEndDefined()) {
			LocalDateTime testPointInTime = (currentPoint == null) ? startingPoint : currentPoint;
			if (testPointInTime != null && endingPoint != null) {
				if (getIncrementing()) {
					LocalDateTime nextPoint = testPointInTime.plus(cycleCount, cycleUnit);

					result = testPointInTime.plus(cycleCount, cycleUnit).isAfter(endingPoint);
				} else {
					result = testPointInTime.minus(cycleCount, cycleUnit).isBefore(endingPoint);
				}
			}
			if (maximumPointCount != null && currentPointCount != null) {
				result |= currentPointCount  >= maximumPointCount;
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
		LocalDateTime result = null;
		if (hasNext()) {
			result = peekNext();
			currentPoint = result;
			currentPointCount = (currentPointCount == null) ? 1 : currentPointCount + 1;
		} else {
			throw new NoSuchElementException();
		}
		return result;
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

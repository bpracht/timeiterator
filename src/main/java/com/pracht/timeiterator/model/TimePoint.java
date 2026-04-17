package com.pracht.timeiterator.model;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * TimePoint is a composite class that can represent either a LocalDateTime
 * or a ZonedDateTime.
 */
public class TimePoint {

    private final LocalDateTime localDateTime;
    private final ZonedDateTime zonedDateTime;

    private TimePoint(LocalDateTime localDateTime, ZonedDateTime zonedDateTime) {
        this.localDateTime = localDateTime;
        this.zonedDateTime = zonedDateTime;
    }

    /**
     * Creates a TimePoint from a LocalDateTime.
     */
    public static TimePoint from(LocalDateTime localDateTime) {
        return new TimePoint(Objects.requireNonNull(localDateTime), null);
    }

    /**
     * Creates a TimePoint from a ZonedDateTime.
     */
    public static TimePoint from(ZonedDateTime zonedDateTime) {
        return new TimePoint(null, Objects.requireNonNull(zonedDateTime));
    }

    /**
     * Creates a TimePoint with the current system timestamp as a LocalDateTime.
     */
    public static TimePoint now() {
        return from(LocalDateTime.now());
    }

    /**
     * Creates a TimePoint with the current system timestamp as a ZonedDateTime
     * using the system default zone.
     */
    public static TimePoint nowZoned() {
        return from(ZonedDateTime.now());
    }

    /**
     * Creates a TimePoint with the current system timestamp as a ZonedDateTime
     * using the specified zone.
     */
    public static TimePoint nowZoned(ZoneId zone) {
        return from(ZonedDateTime.now(zone));
    }

    /**
     * Returns true if this TimePoint represents a ZonedDateTime.
     */
    public boolean isZoned() {
        return zonedDateTime != null;
    }

    /**
     * Returns the representation as a LocalDateTime.
     * If this is a ZonedDateTime, it returns the local part of that zoned date time.
     */
    public LocalDateTime toLocalDateTime() {
        return isZoned() ? zonedDateTime.toLocalDateTime() : localDateTime;
    }

    /**
     * Returns the representation as a ZonedDateTime.
     * If this is already a ZonedDateTime, it returns it.
     * If this is a LocalDateTime, it uses the provided ZoneId to create the ZonedDateTime.
     */
    public ZonedDateTime toZonedDateTime(ZoneId zoneId) {
        if (isZoned()) {
            return zonedDateTime;
        }
        return localDateTime.atZone(zoneId);
    }

    /**
     * Convenience method to get ZonedDateTime using the system default zone if it is currently a LocalDateTime.
     */
    public ZonedDateTime toZonedDateTime() {
        return toZonedDateTime(ZoneId.systemDefault());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimePoint timePoint = (TimePoint) o;
        return Objects.equals(localDateTime, timePoint.localDateTime) &&
                Objects.equals(zonedDateTime, timePoint.zonedDateTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(localDateTime, zonedDateTime);
    }

    @Override
    public String toString() {
        return isZoned() ? zonedDateTime.toString() : localDateTime.toString();
    }
}

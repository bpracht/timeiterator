package com.pracht.timeiterator.model;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalUnit;
import java.util.Objects;

/**
 * TimePoint is a composite class that can represent either a LocalDateTime
 * or a ZonedDateTime. It provides a unified API for temporal calculations
 * and comparisons regardless of whether a time zone is present.
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
     * 
     * @param localDateTime The local date time to wrap.
     * @return A new TimePoint instance.
     */
    public static TimePoint from(LocalDateTime localDateTime) {
        return new TimePoint(Objects.requireNonNull(localDateTime), null);
    }

    /**
     * Creates a TimePoint from a ZonedDateTime.
     * 
     * @param zonedDateTime The zoned date time to wrap.
     * @return A new TimePoint instance.
     */
    public static TimePoint from(ZonedDateTime zonedDateTime) {
        return new TimePoint(null, Objects.requireNonNull(zonedDateTime));
    }

    /**
     * Creates a TimePoint with the current system timestamp as a LocalDateTime.
     * 
     * @return A new TimePoint instance representing 'now' without a zone.
     */
    public static TimePoint now() {
        return from(LocalDateTime.now());
    }

    /**
     * Creates a TimePoint with the current system timestamp as a ZonedDateTime
     * using the system default zone.
     * 
     * @return A new TimePoint instance representing 'now' with the system default zone.
     */
    public static TimePoint nowZoned() {
        return from(ZonedDateTime.now());
    }

    /**
     * Creates a TimePoint with the current system timestamp as a ZonedDateTime
     * using the specified zone.
     * 
     * @param zone The zone to use.
     * @return A new TimePoint instance representing 'now' in the specified zone.
     */
    public static TimePoint nowZoned(ZoneId zone) {
        return from(ZonedDateTime.now(zone));
    }

    /**
     * Returns true if this TimePoint represents a ZonedDateTime.
     * 
     * @return true if zoned, false if local.
     */
    public boolean isZoned() {
        return zonedDateTime != null;
    }

    /**
     * Returns the representation as a LocalDateTime.
     * If this is a ZonedDateTime, it returns the local part of that zoned date time.
     * 
     * @return The local date time representation.
     */
    public LocalDateTime toLocalDateTime() {
        return isZoned() ? zonedDateTime.toLocalDateTime() : localDateTime;
    }

    /**
     * Returns the representation as a ZonedDateTime.
     * If this is already a ZonedDateTime, it returns it.
     * If this is a LocalDateTime, it uses the provided ZoneId to create the ZonedDateTime.
     * 
     * @param zoneId The zone to apply if this is a local time point.
     * @return The zoned date time representation.
     */
    public ZonedDateTime toZonedDateTime(ZoneId zoneId) {
        if (isZoned()) {
            return zonedDateTime;
        }
        return localDateTime.atZone(zoneId);
    }

    /**
     * Convenience method to get ZonedDateTime using the system default zone if it is currently a LocalDateTime.
     * 
     * @return The zoned date time representation in the system default zone.
     */
    public ZonedDateTime toZonedDateTime() {
        return toZonedDateTime(ZoneId.systemDefault());
    }

    /**
     * Returns a new TimePoint with the specified amount added.
     * 
     * @param amountToAdd The amount of the unit to add.
     * @param unit The unit of the amount.
     * @return A new TimePoint with the addition applied.
     */
    public TimePoint plus(long amountToAdd, TemporalUnit unit) {
        if (isZoned()) {
            return from(zonedDateTime.plus(amountToAdd, unit));
        }
        return from(localDateTime.plus(amountToAdd, unit));
    }

    /**
     * Returns a new TimePoint with the specified amount subtracted.
     * 
     * @param amountToSubtract The amount of the unit to subtract.
     * @param unit The unit of the amount.
     * @return A new TimePoint with the subtraction applied.
     */
    public TimePoint minus(long amountToSubtract, TemporalUnit unit) {
        if (isZoned()) {
            return from(zonedDateTime.minus(amountToSubtract, unit));
        }
        return from(localDateTime.minus(amountToSubtract, unit));
    }

    /**
     * Checks if this point is after the specified point.
     * 
     * @param other The other point to compare against.
     * @return true if this is strictly after the other point.
     */
    public boolean isAfter(TimePoint other) {
        if (isZoned() && other.isZoned()) {
            return zonedDateTime.isAfter(other.zonedDateTime);
        }
        // Fallback to local comparison if either is not zoned, or compare local parts
        return toLocalDateTime().isAfter(other.toLocalDateTime());
    }

    /**
     * Checks if this point is before the specified point.
     * 
     * @param other The other point to compare against.
     * @return true if this is strictly before the other point.
     */
    public boolean isBefore(TimePoint other) {
        if (isZoned() && other.isZoned()) {
            return zonedDateTime.isBefore(other.zonedDateTime);
        }
        // Fallback to local comparison if either is not zoned, or compare local parts
        return toLocalDateTime().isBefore(other.toLocalDateTime());
    }

    /**
     * Returns the day of week for this point.
     * 
     * @return The DayOfWeek enum value.
     */
    public DayOfWeek getDayOfWeek() {
        if (isZoned()) {
            return zonedDateTime.getDayOfWeek();
        }
        return localDateTime.getDayOfWeek();
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

    /**
     * Defines boundaries (Minimum or Maximum) for temporal units.
     */
    public enum UnitBoundary {
        /** The smallest value for the unit (e.g., 1st day of month, 0th hour). */
        MIN, 
        /** The largest value for the unit (e.g., last day of month, 23rd hour). */
        MAX
    }

    /**
     * Returns a new fluent builder for creating a TimePoint.
     * 
     * @return A new Builder instance.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Fluent builder for TimePoint.
     * If year, month, or day are omitted, they default to the current system date.
     * Time components default to 0 if not specified.
     */
    public static class Builder {
        private Integer year;
        private Integer monthValue;
        private UnitBoundary monthBoundary;
        private Integer dayValue;
        private UnitBoundary dayBoundary;
        private Integer hourValue;
        private UnitBoundary hourBoundary;
        private Integer minuteValue;
        private UnitBoundary minuteBoundary;
        private Integer secondValue;
        private UnitBoundary secondBoundary;
        private Integer nanoValue;
        private UnitBoundary nanoBoundary;
        private ZoneId zoneId;

        /**
         * Sets the year.
         * @param year The year to set.
         * @return this builder.
         */
        public Builder year(int year) {
            this.year = year;
            return this;
        }

        /**
         * Sets a specific month (1-12).
         * @param month The month value.
         * @return this builder.
         */
        public Builder month(int month) {
            this.monthValue = month;
            this.monthBoundary = null;
            return this;
        }

        /**
         * Sets the month to a boundary (MIN/MAX).
         * @param boundary The boundary to use.
         * @return this builder.
         */
        public Builder month(UnitBoundary boundary) {
            this.monthBoundary = boundary;
            this.monthValue = null;
            return this;
        }

        /**
         * Sets a specific day of the month (1-31).
         * @param day The day value.
         * @return this builder.
         */
        public Builder day(int day) {
            this.dayValue = day;
            this.dayBoundary = null;
            return this;
        }

        /**
         * Sets the day to a boundary (MIN/MAX).
         * @param boundary The boundary to use.
         * @return this builder.
         */
        public Builder day(UnitBoundary boundary) {
            this.dayBoundary = boundary;
            this.dayValue = null;
            return this;
        }

        /**
         * Sets a specific hour (0-23).
         * @param hour The hour value.
         * @return this builder.
         */
        public Builder hour(int hour) {
            this.hourValue = hour;
            this.hourBoundary = null;
            return this;
        }

        /**
         * Sets the hour to a boundary (MIN/MAX).
         * @param boundary The boundary to use.
         * @return this builder.
         */
        public Builder hour(UnitBoundary boundary) {
            this.hourBoundary = boundary;
            this.hourValue = null;
            return this;
        }

        /**
         * Sets a specific minute (0-59).
         * @param minute The minute value.
         * @return this builder.
         */
        public Builder minute(int minute) {
            this.minuteValue = minute;
            this.minuteBoundary = null;
            return this;
        }

        /**
         * Sets the minute to a boundary (MIN/MAX).
         * @param boundary The boundary to use.
         * @return this builder.
         */
        public Builder minute(UnitBoundary boundary) {
            this.minuteBoundary = boundary;
            this.minuteValue = null;
            return this;
        }

        /**
         * Sets a specific second (0-59).
         * @param second The second value.
         * @return this builder.
         */
        public Builder second(int second) {
            this.secondValue = second;
            this.secondBoundary = null;
            return this;
        }

        /**
         * Sets the second to a boundary (MIN/MAX).
         * @param boundary The boundary to use.
         * @return this builder.
         */
        public Builder second(UnitBoundary boundary) {
            this.secondBoundary = boundary;
            this.secondValue = null;
            return this;
        }

        /**
         * Sets a specific nanosecond.
         * @param nano The nanosecond value.
         * @return this builder.
         */
        public Builder nano(int nano) {
            this.nanoValue = nano;
            this.nanoBoundary = null;
            return this;
        }

        /**
         * Sets the nano to a boundary (MIN/MAX).
         * @param boundary The boundary to use.
         * @return this builder.
         */
        public Builder nano(UnitBoundary boundary) {
            this.nanoBoundary = boundary;
            this.nanoValue = null;
            return this;
        }

        /**
         * Sets the time zone.
         * @param zoneId The zone ID to use.
         * @return this builder.
         */
        public Builder zoneId(ZoneId zoneId) {
            this.zoneId = zoneId;
            return this;
        }

        /**
         * Sets the time zone by string ID.
         * @param zoneId The string ID of the zone.
         * @return this builder.
         */
        public Builder zoneId(String zoneId) {
            this.zoneId = ZoneId.of(zoneId);
            return this;
        }

        /**
         * Builds the TimePoint based on the configured values.
         * If year, month, or day were not set, current system values are used.
         * If hour, minute, second, or nano were not set, 0 is used.
         * 
         * @return A new TimePoint instance.
         */
        public TimePoint build() {
            LocalDateTime now = LocalDateTime.now();
            int yearToUse = (year != null) ? year : now.getYear();
            
            int monthToUse;
            if (monthBoundary != null) {
                monthToUse = (monthBoundary == UnitBoundary.MIN) ? 1 : 12;
            } else {
                monthToUse = (monthValue != null) ? monthValue : now.getMonthValue();
            }

            int dayToUse;
            if (dayBoundary != null) {
                if (dayBoundary == UnitBoundary.MIN) {
                    dayToUse = 1;
                } else {
                    // Last day of month depends on year and month
                    java.time.YearMonth yearMonth = java.time.YearMonth.of(yearToUse, monthToUse);
                    dayToUse = yearMonth.lengthOfMonth();
                }
            } else {
                dayToUse = (dayValue != null) ? dayValue : now.getDayOfMonth();
            }

            int hourToUse = resolve(hourValue, hourBoundary, 0, 23, 0);
            int minuteToUse = resolve(minuteValue, minuteBoundary, 0, 59, 0);
            int secondToUse = resolve(secondValue, secondBoundary, 0, 59, 0);
            int nanoToUse = resolve(nanoValue, nanoBoundary, 0, 999_999_999, 0);

            if (zoneId != null) {
                return TimePoint.from(ZonedDateTime.of(yearToUse, monthToUse, dayToUse, hourToUse, minuteToUse, secondToUse, nanoToUse, zoneId));
            } else {
                return TimePoint.from(LocalDateTime.of(yearToUse, monthToUse, dayToUse, hourToUse, minuteToUse, secondToUse, nanoToUse));
            }
        }

        private int resolve(Integer value, UnitBoundary boundary, int min, int max, int defaultValue) {
            if (boundary != null) {
                return (boundary == UnitBoundary.MIN) ? min : max;
            }
            return (value != null) ? value : defaultValue;
        }
    }
}

package com.pracht.timeiterator;

import com.pracht.timeiterator.model.TimePoint;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

/**
 * CommonSequences provides pre-defined sequences for various regions and purposes.
 * It includes business day definitions, national holidays, and astronomical events.
 */
public class CommonSequences {

    /**
     * Pre-defined sequences specifically for the United States.
     */
    public static class US {

        /**
         * Returns a sequence representing every Monday through Friday, starting from the given point.
         * 
         * @param startingPoint The point in time from which the business day sequence begins.
         * @return A TimePointIterator that returns only weekdays (Mon-Fri).
         * @implNote This sequence assumes a standard 5-day work week and does not account for 
         *           individual company holidays or holiday-driven weekend shifts.
         */
        public static TimePointIterator BUSINESS_DAYS(TimePoint startingPoint) {
            TimePointIterator.Builder iteratorBuilder = TimePointIterator.builder()
                    .startingPoint(startingPoint)
                    .incrementing(true);

            // Create a sequence for each weekday (Monday through Friday)
            for (int i = 0; i < 5; i++) {
                DayOfWeek dayOfWeek = DayOfWeek.MONDAY.plus(i);
                
                // Use a temporary sequence to find the first occurrence of this day of week on or after startingPoint
                TimePoint firstOccurrence = TimePointSequence.builder()
                        .startingPoint(startingPoint, 1, dayOfWeek)
                        .build()
                        .getStartingPoint();

                iteratorBuilder.sequence(TimePointSequence.builder()
                        .startingPoint(firstOccurrence)
                        .cycleCount(1L)
                        .cycleUnit(ChronoUnit.WEEKS)
                        .build());
            }

            return iteratorBuilder.build();
        }

        /**
         * New Year's Day: Fixed annual date on January 1st.
         * @param startYear The first year to include in the sequence.
         * @return A TimePointSequence for Jan 1st.
         */
        public static TimePointSequence NEW_YEARS_DAY(int startYear) {
            return annualFixedHoliday(startYear, 1, 1);
        }

        /**
         * New Year's Eve: Fixed annual date on December 31st.
         * @param startYear The first year to include in the sequence.
         * @return A TimePointSequence for Dec 31st.
         */
        public static TimePointSequence NEW_YEARS_EVE(int startYear) {
            return annualFixedHoliday(startYear, 12, 31);
        }

        /**
         * Juneteenth National Independence Day: Fixed annual date on June 19th.
         * @param startYear The first year to include in the sequence.
         * @return A TimePointSequence for June 19th.
         */
        public static TimePointSequence JUNETEENTH(int startYear) {
            return annualFixedHoliday(startYear, 6, 19);
        }

        /**
         * Independence Day: Fixed annual date on July 4th.
         * @param startYear The first year to include in the sequence.
         * @return A TimePointSequence for July 4th.
         */
        public static TimePointSequence INDEPENDENCE_DAY(int startYear) {
            return annualFixedHoliday(startYear, 7, 4);
        }

        /**
         * Halloween: Fixed annual date on October 31st.
         * @param startYear The first year to include in the sequence.
         * @return A TimePointSequence for Oct 31st.
         */
        public static TimePointSequence HALLOWEEN(int startYear) {
            return annualFixedHoliday(startYear, 10, 31);
        }

        /**
         * Spring Equinox: Approximated as March 20th.
         * @param startYear The first year to include in the sequence.
         * @return A TimePointSequence for the first day of Spring.
         * @implNote Astronomical equinoxes vary between March 19-21. This sequence uses 
         *           March 20 as a common static approximation.
         */
        public static TimePointSequence SPRING_EQUINOX(int startYear) {
            return annualFixedHoliday(startYear, 3, 20);
        }

        /**
         * Summer Solstice: Approximated as June 21st.
         * @param startYear The first year to include in the sequence.
         * @return A TimePointSequence for the first day of Summer.
         * @implNote Astronomical solstices vary between June 20-22. This sequence uses 
         *           June 21 as a common static approximation.
         */
        public static TimePointSequence SUMMER_SOLSTICE(int startYear) {
            return annualFixedHoliday(startYear, 6, 21);
        }

        /**
         * Autumnal Equinox: Approximated as September 22nd.
         * @param startYear The first year to include in the sequence.
         * @return A TimePointSequence for the first day of Autumn.
         * @implNote Astronomical equinoxes vary between September 21-23. This sequence uses 
         *           September 22 as a common static approximation.
         */
        public static TimePointSequence AUTUMNAL_EQUINOX(int startYear) {
            return annualFixedHoliday(startYear, 9, 22);
        }

        /**
         * Winter Solstice: Approximated as December 21st.
         * @param startYear The first year to include in the sequence.
         * @return A TimePointSequence for the first day of Winter.
         * @implNote Astronomical solstices vary between December 20-22. This sequence uses 
         *           December 21 as a common static approximation.
         */
        public static TimePointSequence WINTER_SOLSTICE(int startYear) {
            return annualFixedHoliday(startYear, 12, 21);
        }

        /**
         * Daylight Saving Time Begins: 2nd Sunday in March.
         * 
         * @param startYear The first year in the range.
         * @param endYear The last year in the range.
         * @return A TimePointIterator for the start of DST.
         * @implNote Follows the US Energy Policy Act of 2005 rules. Does not account for 
         *           historical rules prior to 2007 or regions that do not observe DST.
         */
        public static TimePointIterator DST_START(int startYear, int endYear) {
            return annualFloatingHoliday(startYear, endYear, 3, 2, DayOfWeek.SUNDAY);
        }

        /**
         * Daylight Saving Time Ends: 1st Sunday in November.
         * 
         * @param startYear The first year in the range.
         * @param endYear The last year in the range.
         * @return A TimePointIterator for the end of DST.
         * @implNote Follows the US Energy Policy Act of 2005 rules. Does not account for 
         *           historical rules prior to 2007 or regions that do not observe DST.
         */
        public static TimePointIterator DST_END(int startYear, int endYear) {
            return annualFloatingHoliday(startYear, endYear, 11, 1, DayOfWeek.SUNDAY);
        }

        /**
         * Veterans Day: Fixed annual date on November 11th.
         * @param startYear The first year to include in the sequence.
         * @return A TimePointSequence for Nov 11th.
         */
        public static TimePointSequence VETERANS_DAY(int startYear) {
            return annualFixedHoliday(startYear, 11, 11);
        }

        /**
         * Christmas: Fixed annual date on December 25th.
         * @param startYear The first year to include in the sequence.
         * @return A TimePointSequence for Dec 25th.
         */
        public static TimePointSequence CHRISTMAS(int startYear) {
            return annualFixedHoliday(startYear, 12, 25);
        }

        /**
         * Martin Luther King Jr. Day: 3rd Monday in January.
         * @param startYear The first year in the range.
         * @param endYear The last year in the range.
         * @return A TimePointIterator for MLK Day.
         */
        public static TimePointIterator MARTIN_LUTHER_KING_DAY(int startYear, int endYear) {
            return annualFloatingHoliday(startYear, endYear, 1, 3, DayOfWeek.MONDAY);
        }

        /**
         * Presidents' Day: 3rd Monday in February.
         * @param startYear The first year in the range.
         * @param endYear The last year in the range.
         * @return A TimePointIterator for Presidents' Day.
         */
        public static TimePointIterator PRESIDENTS_DAY(int startYear, int endYear) {
            return annualFloatingHoliday(startYear, endYear, 2, 3, DayOfWeek.MONDAY);
        }

        /**
         * Memorial Day: Last Monday in May.
         * @param startYear The first year in the range.
         * @param endYear The last year in the range.
         * @return A TimePointIterator for Memorial Day.
         */
        public static TimePointIterator MEMORIAL_DAY(int startYear, int endYear) {
            TimePointIterator.Builder builder = TimePointIterator.builder()
                    .startingPoint(TimePoint.builder().year(startYear).month(5).day(1).build());
            
            for (int year = startYear; year <= endYear; year++) {
                LocalDate lastMonday = LocalDate.of(year, 5, 31)
                        .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                
                builder.sequence(TimePointSequence.builder()
                        .startingPoint(TimePoint.from(lastMonday.atStartOfDay()))
                        .maximumPointCount(1L)
                        .build());
            }
            return builder.build();
        }

        /**
         * Labor Day: 1st Monday in September.
         * @param startYear The first year in the range.
         * @param endYear The last year in the range.
         * @return A TimePointIterator for Labor Day.
         */
        public static TimePointIterator LABOR_DAY(int startYear, int endYear) {
            return annualFloatingHoliday(startYear, endYear, 9, 1, DayOfWeek.MONDAY);
        }

        /**
         * Columbus Day / Indigenous Peoples' Day: 2nd Monday in October.
         * @param startYear The first year in the range.
         * @param endYear The last year in the range.
         * @return A TimePointIterator for Columbus Day.
         */
        public static TimePointIterator COLUMBUS_DAY(int startYear, int endYear) {
            return annualFloatingHoliday(startYear, endYear, 10, 2, DayOfWeek.MONDAY);
        }

        /**
         * Thanksgiving: 4th Thursday in November.
         * @param startYear The first year in the range.
         * @param endYear The last year in the range.
         * @return A TimePointIterator for Thanksgiving.
         */
        public static TimePointIterator THANKSGIVING(int startYear, int endYear) {
            return annualFloatingHoliday(startYear, endYear, 11, 4, DayOfWeek.THURSDAY);
        }

        /**
         * Easter Sunday: Calculated using the Meeus/Jones/Butcher Gregorian algorithm.
         * 
         * @param startYear The first year in the range.
         * @param endYear The last year in the range.
         * @return A TimePointIterator for Easter Sunday.
         * @implNote This calculation follows the Western (Roman Catholic) church calendar.
         *           The algorithm is accurate for all Gregorian years from 1583 to 4099.
         */
        public static TimePointIterator EASTER(int startYear, int endYear) {
            TimePointIterator.Builder builder = TimePointIterator.builder()
                    .startingPoint(TimePoint.builder().year(startYear).month(3).day(1).build());
            
            for (int year = startYear; year <= endYear; year++) {
                builder.sequence(TimePointSequence.builder()
                        .startingPoint(calculateEaster(year))
                        .maximumPointCount(1L)
                        .build());
            }
            return builder.build();
        }

        /**
         * Good Friday: The Friday before Easter Sunday.
         * 
         * @param startYear The first year in the range.
         * @param endYear The last year in the range.
         * @return A TimePointIterator for Good Friday.
         */
        public static TimePointIterator GOOD_FRIDAY(int startYear, int endYear) {
            TimePointIterator.Builder builder = TimePointIterator.builder()
                    .startingPoint(TimePoint.builder().year(startYear).month(3).day(1).build());
            
            for (int year = startYear; year <= endYear; year++) {
                TimePoint easter = calculateEaster(year);
                builder.sequence(TimePointSequence.builder()
                        .startingPoint(easter.minus(2, ChronoUnit.DAYS))
                        .maximumPointCount(1L)
                        .build());
            }
            return builder.build();
        }

        private static TimePoint calculateEaster(int year) {
            int a = year % 19;
            int b = year / 100;
            int c = year % 100;
            int d = b / 4;
            int e = b % 4;
            int f = (b + 8) / 25;
            int g = (b - f + 1) / 3;
            int h = (19 * a + b - d - g + 15) % 30;
            int i = c / 4;
            int k = c % 4;
            int l = (32 + 2 * e + 2 * i - h - k) % 7;
            int m = (a + 11 * h + 22 * l) / 451;
            int month = (h + l - 7 * m + 114) / 31;
            int day = ((h + l - 7 * m + 114) % 31) + 1;
            return TimePoint.builder().year(year).month(month).day(day).build();
        }

        private static TimePointSequence annualFixedHoliday(int startYear, int month, int day) {
            return TimePointSequence.builder()
                    .startingPoint(TimePoint.builder().year(startYear).month(month).day(day).build())
                    .cycleCount(1L)
                    .cycleUnit(ChronoUnit.YEARS)
                    .build();
        }

        private static TimePointIterator annualFloatingHoliday(int startYear, int endYear, int month, int occurrence, DayOfWeek dayOfWeek) {
            TimePointIterator.Builder builder = TimePointIterator.builder()
                    .startingPoint(TimePoint.builder().year(startYear).month(month).day(1).build());
            
            for (int year = startYear; year <= endYear; year++) {
                TimePoint startOfMonth = TimePoint.builder().year(year).month(month).day(1).build();
                TimePoint holiday = TimePointSequence.builder()
                        .startingPoint(startOfMonth, occurrence, dayOfWeek)
                        .build()
                        .getStartingPoint();
                
                builder.sequence(TimePointSequence.builder()
                        .startingPoint(holiday)
                        .maximumPointCount(1L)
                        .build());
            }
            return builder.build();
        }
    }
}

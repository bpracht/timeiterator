# timeiterator
TimeIterator is a project to help model a stream of dates. This provides the developer a way to 
get a stream of dates based on normal human usage. For example, a payroll every 2 weeks on a Friday could be 
modeled. Given a starting point, and and an increment size, a stream of dates, limited or unlimited can be iterated.

## Requirements
- **Java**: 17 or higher (Tested with 17.0.2 and 22.0.1)
- **Maven**: 3.9.x or higher (Tested with 3.9.9)

The **TimePointIterator** is used when the need calls for a starting point and an increment. If multiple streams need
to be combined, say payroll plus a quarterly bonus, the **TimePointSequence** allows multiple streams to be combined.

These classes use the **TimePoint** composite class, which supports both `LocalDateTime` and `ZonedDateTime`.

# Limitations and future direction
The project now supports both local and zoned timestamps via the `TimePoint` class. In the future, a series of timeframes will be added. Finally, the ability to skip certain dates will be supported, for example, moving a payroll date given a holiday.

# Example
This models dates from 9/12/2022 at 8pm, every 3 months using `TimePoint`:

```java
TimePoint start = TimePoint.from(LocalDateTime.of(2022, 9, 12, 20, 0));
TimePointSequence sequence = TimePointSequence.builder()
				.startingPoint(start)
				.cycleCount(3L)
				.cycleUnit(ChronoUnit.MONTHS)
				.build();
```

To use Zoned dates:

```java
TimePoint zonedStart = TimePoint.from(ZonedDateTime.now(ZoneId.of("America/New_York")));
TimePointSequence zonedSequence = TimePointSequence.builder()
				.startingPoint(zonedStart)
				.cycleCount(1L)
				.cycleUnit(ChronoUnit.DAYS)
				.build();
```

# timeiterator
TimeIterator is a project to help model a stream of dates. This provides the developer a way to 
get a stream of dates based on normal human usage. For example, a payroll every 2 weeks on a Friday could be 
modeled. Given a starting point, and and an increment size, a stream of dates, limited or unlimited can be iterated.

The LocalDateTimeIterator is used when the need calls for a starting point and an increment. If multiple streams need
to be combined, say payroll plus a quarterly bonus, the LocalDateTimeSequence allows multiple streams to be combined.

# Limitations and future direction
Initially it only supports the current timezone, though future versions will support global dates. These represent 
points in time, but in the future, a series of timeframes will be added. Finally, the ability to skip certain dates 
will be supported, for example, moving a payroll date given a holiday.

# Example
This model dates from 9/12/2022 at 8pm, every 3 months do:
timeSequence = LocalDateTimeSequence.builder()
				.startingPoint(LocalDateTime.of(LocalDate.of(2022, 9, 12), LocalTime.of(20, 00, 0))).cycleCount(3L)
				.cycleUnit(ChronoUnit.MONTHS).build();






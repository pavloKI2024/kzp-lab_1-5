package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class BookingQueriesTest {

    private static final List<Booking> DATA = List.of(
            Booking.fromCsv("101;Іваненко;3;1200.50;Standard"),
            Booking.fromCsv("102;Коваль;7;2000.00;Lux"),
            Booking.fromCsv("103;Шевченко;2;850.00;Economy"),
            Booking.fromCsv("104;Мельник;7;1200.50;Standard"),
            Booking.fromCsv("105;Бондар;10;2000.00;Lux"));

    private static final List<Booking> EMPTY = List.of();

    @Test
    void longStaysFiltersByMinNights() {
        List<Booking> result = BookingQueries.longStays(DATA, 7);

        assertEquals(List.of(102, 104, 105), BookingQueries.roomNumbers(result));
    }

    @Test
    void longStaysBoundaryIsInclusive() {
        assertEquals(1, BookingQueries.longStays(DATA, 10).size());
        assertTrue(BookingQueries.longStays(DATA, 11).isEmpty());
    }

    @Test
    void roomNumbersKeepsOrderAndDuplicates() {
        List<Booking> withDuplicate = List.of(DATA.get(0), DATA.get(0), DATA.get(1));

        assertEquals(List.of(101, 101, 102), BookingQueries.roomNumbers(withDuplicate));
    }

    @Test
    void countByRateGroupsWithDoubleKeys() {
        Map<Double, Long> result = BookingQueries.countByRate(DATA);

        assertEquals(Map.of(850.0, 1L, 1200.5, 2L, 2000.0, 2L), result);
        assertEquals(List.of(850.0, 1200.5, 2000.0), List.copyOf(result.keySet()));
    }

    @Test
    void nightsStatisticsAreComputed() {
        IntSummaryStatistics stats = BookingQueries.nightsStatistics(DATA);

        assertEquals(5, stats.getCount());
        assertEquals(29, stats.getSum());
        assertEquals(2, stats.getMin());
        assertEquals(10, stats.getMax());
        assertEquals(5.8, stats.getAverage(), 1e-9);
    }

    @Test
    void topLongestOrdersByNightsDescThenRoom() {
        List<Booking> top = BookingQueries.topLongest(DATA, 3);

        assertEquals(List.of(105, 102, 104), BookingQueries.roomNumbers(top));
    }

    @Test
    void topLongestLimitLargerThanSizeReturnsAll() {
        assertEquals(5, BookingQueries.topLongest(DATA, 100).size());
    }

    @Test
    void topLongestZeroLimitIsEmpty() {
        assertTrue(BookingQueries.topLongest(DATA, 0).isEmpty());
    }

    @Test
    void topLongestRejectsNegativeLimit() {
        assertThrows(IllegalArgumentException.class, () -> BookingQueries.topLongest(DATA, -1));
    }

    @Test
    void findByRoomReturnsBooking() {
        Optional<Booking> found = BookingQueries.findByRoom(DATA, 103);

        assertTrue(found.isPresent());
        assertEquals("Шевченко", found.get().getGuest());
    }

    @Test
    void findByRoomReturnsEmptyWhenAbsent() {
        assertTrue(BookingQueries.findByRoom(DATA, 999).isEmpty());
    }

    @Test
    void findByRoomReturnsFirstOfDuplicates() {
        List<Booking> list = List.of(
                Booking.fromCsv("1;Перший;2;100;Standard"),
                Booking.fromCsv("1;Другий;3;100;Standard"));

        assertEquals("Перший", BookingQueries.findByRoom(list, 1).orElseThrow().getGuest());
    }

    @Test
    void longestStayTakesFirstOnTie() {
        List<Booking> list = List.of(
                Booking.fromCsv("1;Перший;5;100;Standard"),
                Booking.fromCsv("2;Другий;5;100;Standard"));

        assertEquals("Перший", BookingQueries.longestStay(list).orElseThrow().getGuest());
    }

    @Test
    void totalRevenueUsesPolymorphicCost() {
        List<Booking> list = List.of(
                Booking.fromCsv("1;А;11;100;Standard"),
                Booking.fromCsv("2;Б;11;100;Lux"));

        assertEquals(2090.0, BookingQueries.totalRevenue(list), 1e-9);
    }

    @Test
    void emptyInputGivesEmptyResults() {
        assertTrue(BookingQueries.longStays(EMPTY, 1).isEmpty());
        assertTrue(BookingQueries.roomNumbers(EMPTY).isEmpty());
        assertTrue(BookingQueries.countByRate(EMPTY).isEmpty());
        assertTrue(BookingQueries.topLongest(EMPTY, 5).isEmpty());
        assertTrue(BookingQueries.findByRoom(EMPTY, 1).isEmpty());
        assertTrue(BookingQueries.longestStay(EMPTY).isEmpty());
        assertEquals(0.0, BookingQueries.totalRevenue(EMPTY), 1e-9);
    }

    @Test
    void emptyStatisticsHaveZeroCountAndSum() {
        IntSummaryStatistics stats = BookingQueries.nightsStatistics(EMPTY);

        assertEquals(0, stats.getCount());
        assertEquals(0, stats.getSum());
        assertEquals(0.0, stats.getAverage(), 1e-9);
    }

    @Test
    void queriesDoNotMutateInput() {
        List<Booking> input = new java.util.ArrayList<>(DATA);

        BookingQueries.longStays(input, 1);
        BookingQueries.topLongest(input, 5);
        BookingQueries.countByRate(input);

        assertEquals(DATA, input);
    }
}

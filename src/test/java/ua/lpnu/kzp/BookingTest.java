package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BookingTest {

    private static final NightsRate OK = new NightsRate(3, 1200.50);

    @Test
    void constructorStoresAllFields() {
        Booking b = new StandardBooking(101, "Іваненко", "Standard", OK);

        assertEquals(101, b.getRoom());
        assertEquals("Іваненко", b.getGuest());
        assertEquals("Standard", b.getCategory());
        assertEquals(3, b.getNights());
        assertEquals(3601.5, b.getRevenue(), 1e-9);
    }

    @Test
    void constructorRejectsNullGuest() {
        assertThrows(NullPointerException.class, () -> new StandardBooking(101, null, "Standard", OK));
    }

    @Test
    void constructorRejectsBlankGuest() {
        assertThrows(IllegalArgumentException.class, () -> new StandardBooking(101, "  ", "Standard", OK));
    }

    @Test
    void constructorRejectsBlankCategory() {
        assertThrows(IllegalArgumentException.class, () -> new StandardBooking(101, "Іваненко", "", OK));
    }

    @Test
    void constructorRejectsZeroRoom() {
        assertThrows(IllegalArgumentException.class, () -> new StandardBooking(0, "Іваненко", "Standard", OK));
    }

    @Test
    void constructorRejectsNegativeRoom() {
        assertThrows(IllegalArgumentException.class, () -> new StandardBooking(-1, "Іваненко", "Standard", OK));
    }

    @Test
    void constructorRejectsNullNightsRate() {
        assertThrows(NullPointerException.class, () -> new StandardBooking(101, "Іваненко", "Standard", null));
    }

    @Test
    void fromCsvAcceptsValidLine() {
        Booking b = Booking.fromCsv("101;Іваненко Петро;3;1200.50;Standard");

        assertEquals(101, b.getRoom());
        assertEquals("Іваненко Петро", b.getGuest());
        assertEquals("Standard", b.getCategory());
        assertEquals(3, b.getNights());
    }

    @Test
    void fromCsvTrimsTextFields() {
        Booking b = Booking.fromCsv("101; Іваненко ;3;1200.50; Lux ");

        assertEquals("Іваненко", b.getGuest());
        assertEquals("Lux", b.getCategory());
    }

    @Test
    void fromCsvRejectsWrongFieldCount() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> Booking.fromCsv("101;Іваненко;3;1200.50"));
        assertTrue(e.getMessage().contains("неправильна кількість полів"));
    }

    @Test
    void fromCsvRejectsBlankLine() {
        assertThrows(IllegalArgumentException.class, () -> Booking.fromCsv(" "));
    }

    @Test
    void fromCsvRejectsNonNumericNights() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> Booking.fromCsv("101;Іваненко;abc;1200.50;Standard"));
        assertTrue(e.getMessage().contains("нечислове значення"));
    }

    @Test
    void fromCsvRejectsEmptyGuest() {
        assertThrows(IllegalArgumentException.class, () -> Booking.fromCsv("101;;3;1200.50;Standard"));
    }

    @Test
    void fromCsvRejectsNegativeNights() {
        assertThrows(IllegalArgumentException.class,
                () -> Booking.fromCsv("101;Іваненко;-2;1200.50;Standard"));
    }

    @Test
    void toStringUsesRootLocale() {
        Booking b = Booking.fromCsv("101;Іваненко;3;1200.5;Standard");

        assertEquals("Кімната 101: Іваненко, Standard, 3 ночей, 1200.50", b.toString());
    }
}

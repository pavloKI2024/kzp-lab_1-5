package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class BookingRecordTest {

    private static final LocalDate IN = LocalDate.of(2026, 10, 1);
    private static final LocalDate OUT = LocalDate.of(2026, 10, 4);

    @Test
    void constructorStoresFields() {
        BookingRecord r = new BookingRecord(101, " Іваненко ", IN, OUT, 1200.5);

        assertEquals(101, r.getRoom());
        assertEquals("Іваненко", r.getGuest());
        assertEquals(IN, r.getCheckIn());
        assertEquals(OUT, r.getCheckOut());
        assertEquals(1200.5, r.getRate(), 1e-9);
    }

    @Test
    void constructorRejectsInvalidValues() {
        assertThrows(IllegalArgumentException.class, () -> new BookingRecord(0, "А", IN, OUT, 1));
        assertThrows(IllegalArgumentException.class, () -> new BookingRecord(1, " ", IN, OUT, 1));
        assertThrows(IllegalArgumentException.class, () -> new BookingRecord(1, "А", OUT, IN, 1));
        assertThrows(IllegalArgumentException.class, () -> new BookingRecord(1, "А", IN, IN, 1));
        assertThrows(IllegalArgumentException.class, () -> new BookingRecord(1, "А", IN, OUT, -1));
        assertThrows(IllegalArgumentException.class,
                () -> new BookingRecord(1, "А", IN, OUT, Double.NaN));
        assertThrows(NullPointerException.class, () -> new BookingRecord(1, null, IN, OUT, 1));
    }

    @Test
    void equalsAndHashCodeUseAllFields() {
        BookingRecord a = new BookingRecord(1, "А", IN, OUT, 100);
        BookingRecord b = new BookingRecord(1, "А", IN, OUT, 100);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, new BookingRecord(2, "А", IN, OUT, 100));
        assertNotEquals(a, new BookingRecord(1, "А", IN, OUT, 101));
    }
}

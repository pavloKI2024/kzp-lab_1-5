package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class BookingPolymorphismTest {

    @Test
    void fromCsvCreatesSubtypeByCategory() {
        assertInstanceOf(PremiumBooking.class, Booking.fromCsv("1;Гість;3;100;Lux"));
        assertInstanceOf(StandardBooking.class, Booking.fromCsv("2;Гість;3;100;Standard"));
        assertInstanceOf(StandardBooking.class, Booking.fromCsv("3;Гість;3;100;Economy"));
        assertInstanceOf(StandardBooking.class, Booking.fromCsv("4;Гість;3;100;Suite"));
    }

    @Test
    void getKindMatchesCategory() {
        assertEquals(RoomKind.LUX, Booking.fromCsv("1;Гість;3;100;lux").getKind());
    }

    @Test
    void sameInputGivesDifferentRevenueBySubtype() {
        Booking standard = Booking.fromCsv("1;Гість;11;100;Standard");
        Booking premium = Booking.fromCsv("1;Гість;11;100;Lux");

        assertEquals(1100.0, standard.getRevenue(), 1e-9);
        assertEquals(990.0, premium.getRevenue(), 1e-9);
    }

    @Test
    void mixedListIsProcessedThroughBaseType() {
        List<Booking> list = List.of(
                Booking.fromCsv("1;А;11;100;Standard"),
                Booking.fromCsv("2;Б;11;100;Lux"));

        double total = 0;
        for (Booking b : list) {
            total += b.getRevenue();
        }

        assertEquals(2090.0, total, 1e-9);
    }

    @Test
    void equalBookingsCollapseInHashSet() {
        Set<Booking> set = new HashSet<>();
        set.add(Booking.fromCsv("1;Гість;3;100;Standard"));
        set.add(Booking.fromCsv("1;Гість;3;100;Standard"));

        assertEquals(1, set.size());
    }

    @Test
    void differentFieldsAreDifferentInHashSet() {
        Set<Booking> set = new HashSet<>();
        set.add(Booking.fromCsv("1;Гість;3;100;Standard"));
        set.add(Booking.fromCsv("1;Гість;4;100;Standard"));

        assertEquals(2, set.size());
    }

    @Test
    void equalBookingsHaveEqualHashCodeAndWorkAsMapKey() {
        Booking a = Booking.fromCsv("1;Гість;3;100;Lux");
        Booking b = Booking.fromCsv("1;Гість;3;100;Lux");
        Map<Booking, String> map = new HashMap<>();
        map.put(a, "x");

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertEquals("x", map.get(b));
    }

    @Test
    void differentSubtypesAreNotEqualEvenWithSameFields() {
        Booking standard = new StandardBooking(1, "Гість", "Lux", new NightsRate(3, 100));
        Booking premium = new PremiumBooking(1, "Гість", "Lux", new NightsRate(3, 100));

        assertNotEquals(standard, premium);
    }

    @Test
    void equalsHandlesNullAndOtherTypes() {
        Booking b = Booking.fromCsv("1;Гість;3;100;Standard");

        assertNotEquals(null, b);
        assertNotEquals("текст", b);
        assertTrue(b.equals(b));
    }
}

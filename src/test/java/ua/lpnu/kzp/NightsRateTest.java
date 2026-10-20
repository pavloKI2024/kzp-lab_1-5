package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class NightsRateTest {

    @Test
    void storesComponents() {
        NightsRate r = new NightsRate(3, 1200.5);

        assertEquals(3, r.nights());
        assertEquals(1200.5, r.nightlyRate(), 1e-9);
    }

    @Test
    void revenueMultipliesNightsByRate() {
        assertEquals(3601.5, new NightsRate(3, 1200.5).revenue(), 1e-9);
    }

    @Test
    void zeroRateIsAllowed() {
        assertEquals(0.0, new NightsRate(2, 0).revenue(), 1e-9);
    }

    @Test
    void equalValuesAreEqual() {
        assertEquals(new NightsRate(2, 100.0), new NightsRate(2, 100.0));
        assertNotEquals(new NightsRate(2, 100.0), new NightsRate(3, 100.0));
    }

    @Test
    void rejectsZeroNights() {
        assertThrows(IllegalArgumentException.class, () -> new NightsRate(0, 100.0));
    }

    @Test
    void rejectsNegativeRate() {
        assertThrows(IllegalArgumentException.class, () -> new NightsRate(1, -0.01));
    }

    @Test
    void rejectsNaNRate() {
        assertThrows(IllegalArgumentException.class, () -> new NightsRate(1, Double.NaN));
    }

    @Test
    void rejectsInfiniteRate() {
        assertThrows(IllegalArgumentException.class,
                () -> new NightsRate(1, Double.POSITIVE_INFINITY));
    }
}

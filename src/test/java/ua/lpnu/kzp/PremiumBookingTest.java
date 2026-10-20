package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class PremiumBookingTest {

    private static Booking premium(int nights) {
        return new PremiumBooking(201, "Коваль", "Lux", new NightsRate(nights, 2000.0));
    }

    @Test
    void noDiscountBelowThreshold() {
        assertEquals(14000.0, premium(7).getRevenue(), 1e-9);
    }

    @Test
    void noDiscountExactlyAtThreshold() {
        assertEquals(20000.0, premium(PremiumBooking.DISCOUNT_AFTER_NIGHTS).getRevenue(), 1e-9);
    }

    @Test
    void discountAppliesJustAboveThreshold() {
        assertEquals(22000.0 * PremiumBooking.DISCOUNT_FACTOR, premium(11).getRevenue(), 1e-9);
    }

    @Test
    void inheritsValidationFromBase() {
        assertThrows(IllegalArgumentException.class,
                () -> new PremiumBooking(201, "", "Lux", new NightsRate(1, 100)));
    }
}

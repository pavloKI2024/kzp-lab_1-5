package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class StandardBookingTest {

    @Test
    void revenueIsNightsTimesRate() {
        Booking b = new StandardBooking(101, "Іваненко", "Standard", new NightsRate(3, 1200.50));

        assertEquals(3601.5, b.getRevenue(), 1e-9);
    }

    @Test
    void noDiscountForLongStay() {
        Booking b = new StandardBooking(101, "Іваненко", "Standard", new NightsRate(30, 100));

        assertEquals(3000.0, b.getRevenue(), 1e-9);
    }

    @Test
    void inheritsValidationFromBase() {
        assertThrows(IllegalArgumentException.class,
                () -> new StandardBooking(0, "Іваненко", "Standard", new NightsRate(1, 100)));
        assertThrows(IllegalArgumentException.class,
                () -> new StandardBooking(1, " ", "Standard", new NightsRate(1, 100)));
    }
}

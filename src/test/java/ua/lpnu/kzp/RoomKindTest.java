package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RoomKindTest {

    @Test
    void knownCategoriesAreMapped() {
        assertEquals(RoomKind.ECONOMY, RoomKind.fromCategory("Economy"));
        assertEquals(RoomKind.STANDARD, RoomKind.fromCategory("Standard"));
        assertEquals(RoomKind.LUX, RoomKind.fromCategory("Lux"));
    }

    @Test
    void mappingIgnoresCaseAndSpaces() {
        assertEquals(RoomKind.LUX, RoomKind.fromCategory("  lUx "));
    }

    @Test
    void unknownCategoryIsStandard() {
        assertEquals(RoomKind.STANDARD, RoomKind.fromCategory("Suite"));
    }

    @Test
    void onlyLuxIsPremium() {
        assertTrue(RoomKind.LUX.isPremium());
        assertFalse(RoomKind.ECONOMY.isPremium());
        assertFalse(RoomKind.STANDARD.isPremium());
    }

    @Test
    void nullCategoryIsRejected() {
        assertThrows(NullPointerException.class, () -> RoomKind.fromCategory(null));
    }
}

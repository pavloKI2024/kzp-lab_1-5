package ua.lpnu.kzp;

import java.util.Locale;

/** Вид номера, визначений за категорією із вхідного файлу. */
public enum RoomKind {
    /** Економ-номер. */
    ECONOMY(false),
    /** Стандартний номер. */
    STANDARD(false),
    /** Номер класу люкс. */
    LUX(true);

    private final boolean premium;

    RoomKind(boolean premium) {
        this.premium = premium;
    }

    /**
     * Повідомляє, чи належить номер до преміальних.
     *
     * @return {@code true} для преміальних номерів
     */
    public boolean isPremium() {
        return premium;
    }

    /**
     * Визначає вид номера за текстовою категорією без урахування регістру.
     * Невідома категорія вважається стандартною, тому ЛР1 приймала будь-який непорожній текст.
     *
     * @param category текст категорії
     * @return відповідний вид номера
     */
    public static RoomKind fromCategory(String category) {
        return switch (category.trim().toLowerCase(Locale.ROOT)) {
            case "economy" -> ECONOMY;
            case "lux" -> LUX;
            default -> STANDARD;
        };
    }
}

package ua.lpnu.kzp;

import java.util.Locale;
import java.util.Objects;

/** Незмінний запис про проживання гостя в готелі. */
public final class Booking {
    private static final int FIELDS = 5;

    private final int room;
    private final String guest;
    private final String category;
    private final NightsRate nightsRate;

    /**
     * Створює коректний запис після перевірки всіх полів.
     *
     * @param room       номер кімнати (більше нуля)
     * @param guest      ім'я гостя (не порожнє)
     * @param category   категорія номера (не порожня)
     * @param nightsRate кількість ночей і тариф
     * @throws IllegalArgumentException якщо значення некоректні
     */
    public Booking(int room, String guest, String category, NightsRate nightsRate) {
        this.guest = requireText(guest);
        this.category = requireText(category);
        this.nightsRate = Objects.requireNonNull(nightsRate, "Ночі й тариф не можуть бути null");
        if (room <= 0) {
            throw new IllegalArgumentException("недопустиме числове значення");
        }
        this.room = room;
    }

    /**
     * Створює запис із CSV-рядка {@code room;guest;nights;nightlyRate;category}.
     *
     * @param line рядок вхідного файлу
     * @return створений запис
     * @throws IllegalArgumentException якщо структура, число або значення некоректні
     */
    public static Booking fromCsv(String line) {
        Objects.requireNonNull(line, "Рядок не може бути null");
        if (line.isBlank()) {
            throw new IllegalArgumentException("порожній рядок");
        }
        String[] f = line.split(";", -1);
        if (f.length != FIELDS) {
            throw new IllegalArgumentException("неправильна кількість полів ("
                    + f.length + " замість " + FIELDS + ")");
        }

        // Порядок перевірок збережено з лабораторної роботи № 1: спершу текст, потім числа.
        String guest = requireText(f[1]);
        String category = requireText(f[4]);
        try {
            int room = Integer.parseInt(f[0].trim());
            int nights = Integer.parseInt(f[2].trim());
            double rate = Double.parseDouble(f[3].trim());
            return new Booking(room, guest, category, new NightsRate(nights, rate));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("нечислове значення в числовому полі", e);
        }
    }

    private static String requireText(String value) {
        Objects.requireNonNull(value, "Текстове поле не може бути null");
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("порожнє текстове поле");
        }
        return trimmed;
    }

    /**
     * Повертає номер кімнати.
     *
     * @return номер кімнати
     */
    public int getRoom() {
        return room;
    }

    /**
     * Повертає ім'я гостя.
     *
     * @return ім'я гостя
     */
    public String getGuest() {
        return guest;
    }

    /**
     * Повертає категорію номера.
     *
     * @return категорія
     */
    public String getCategory() {
        return category;
    }

    /**
     * Повертає кількість ночей.
     *
     * @return кількість ночей
     */
    public int getNights() {
        return nightsRate.nights();
    }

    /**
     * Повертає вартість проживання.
     *
     * @return кількість ночей, помножена на тариф
     */
    public double getRevenue() {
        return nightsRate.revenue();
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT, "Кімната %d: %s, %s, %d ночей, %.2f",
                room, guest, category, nightsRate.nights(), nightsRate.nightlyRate());
    }
}

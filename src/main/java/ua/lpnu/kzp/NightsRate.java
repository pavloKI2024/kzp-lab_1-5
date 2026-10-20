package ua.lpnu.kzp;

/**
 * Зберігає незмінну пару «кількість ночей — тариф за ніч».
 *
 * @param nights      кількість ночей проживання
 * @param nightlyRate вартість однієї ночі
 */
public record NightsRate(int nights, double nightlyRate) {

    /** Перевіряє допустимість значень. */
    public NightsRate {
        if (nights <= 0 || nightlyRate < 0 || !Double.isFinite(nightlyRate)) {
            throw new IllegalArgumentException("недопустиме числове значення");
        }
    }

    /**
     * Обчислює вартість проживання.
     *
     * @return кількість ночей, помножена на тариф
     */
    public double revenue() {
        return nights * nightlyRate;
    }
}

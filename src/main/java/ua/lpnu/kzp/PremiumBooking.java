package ua.lpnu.kzp;

/** Проживання в преміальному номері: для тривалого проживання діє знижка. */
public final class PremiumBooking extends Booking {
    /** Кількість ночей, після якої діє знижка (строго більше). */
    static final int DISCOUNT_AFTER_NIGHTS = 10;
    /** Коефіцієнт вартості зі знижкою. */
    static final double DISCOUNT_FACTOR = 0.9;

    /**
     * Створює преміальне проживання після перевірки полів.
     *
     * @param room       номер кімнати
     * @param guest      ім'я гостя
     * @param category   категорія номера
     * @param nightsRate кількість ночей і тариф
     */
    public PremiumBooking(int room, String guest, String category, NightsRate nightsRate) {
        super(room, guest, category, nightsRate);
    }

    @Override
    public double getRevenue() {
        double base = getNightsRate().revenue();
        return getNights() > DISCOUNT_AFTER_NIGHTS ? base * DISCOUNT_FACTOR : base;
    }
}

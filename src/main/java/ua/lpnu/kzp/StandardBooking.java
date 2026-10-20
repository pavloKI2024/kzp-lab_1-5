package ua.lpnu.kzp;

/** Проживання в стандартному номері: вартість — ночі, помножені на тариф. */
public final class StandardBooking extends Booking {

    /**
     * Створює стандартне проживання після перевірки полів.
     *
     * @param room       номер кімнати
     * @param guest      ім'я гостя
     * @param category   категорія номера
     * @param nightsRate кількість ночей і тариф
     */
    public StandardBooking(int room, String guest, String category, NightsRate nightsRate) {
        super(room, guest, category, nightsRate);
    }

    @Override
    public double getRevenue() {
        return getNightsRate().revenue();
    }
}

package ua.lpnu.kzp;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;

/** Плоский запис бронювання з датами для CSV-експорту та імпорту. */
public final class BookingRecord {
    private static final int FIELDS = 5;

    @CsvColumn("номер")
    private final int room;

    @CsvColumn("гість")
    private final String guest;

    @CsvColumn("заїзд")
    private final LocalDate checkIn;

    @CsvColumn("виїзд")
    private final LocalDate checkOut;

    @CsvColumn("тариф")
    private final double rate;

    /**
     * Створює запис після перевірки всіх полів.
     *
     * @param room     номер кімнати (більше 0)
     * @param guest    ім'я гостя (не порожнє)
     * @param checkIn  дата заїзду
     * @param checkOut дата виїзду (пізніше за заїзд)
     * @param rate     тариф за ніч (скінченний, не менше 0)
     * @throws IllegalArgumentException якщо значення некоректні
     */
    public BookingRecord(int room, String guest, LocalDate checkIn, LocalDate checkOut, double rate) {
        Objects.requireNonNull(guest, "Гість не може бути null");
        Objects.requireNonNull(checkIn, "Заїзд не може бути null");
        Objects.requireNonNull(checkOut, "Виїзд не може бути null");
        if (room <= 0) {
            throw new IllegalArgumentException("Номер кімнати має бути додатним");
        }
        if (guest.isBlank()) {
            throw new IllegalArgumentException("Гість не може бути порожнім");
        }
        if (!checkOut.isAfter(checkIn)) {
            throw new IllegalArgumentException("Виїзд має бути пізніше за заїзд");
        }
        if (rate < 0 || !Double.isFinite(rate)) {
            throw new IllegalArgumentException("Тариф має бути скінченним і не від'ємним");
        }
        this.room = room;
        this.guest = guest.trim();
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.rate = rate;
    }

    /**
     * Відновлює запис із полів CSV у порядку, який видає {@link CsvExporter}
     * (за іменами полів: checkIn, checkOut, guest, rate, room).
     *
     * @param fields значення полів одного запису
     * @return відновлений запис
     * @throws DataStorageException якщо кількість полів, число чи дата хибні, або порушено інваріант
     */
    public static BookingRecord fromFields(List<String> fields) throws DataStorageException {
        if (fields.size() != FIELDS) {
            throw new DataStorageException(
                    "Очікується " + FIELDS + " полів, отримано " + fields.size());
        }
        try {
            LocalDate checkIn = LocalDate.parse(fields.get(0).trim());
            LocalDate checkOut = LocalDate.parse(fields.get(1).trim());
            String guest = fields.get(2);
            double rate = Double.parseDouble(fields.get(3).trim());
            int room = Integer.parseInt(fields.get(4).trim());
            return new BookingRecord(room, guest, checkIn, checkOut, rate);
        } catch (DateTimeParseException | NumberFormatException e) {
            throw new DataStorageException("Хибне значення поля запису: " + e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            throw new DataStorageException("Порушено правило запису: " + e.getMessage(), e);
        }
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
     * Повертає дату заїзду.
     *
     * @return дата заїзду
     */
    public LocalDate getCheckIn() {
        return checkIn;
    }

    /**
     * Повертає дату виїзду.
     *
     * @return дата виїзду
     */
    public LocalDate getCheckOut() {
        return checkOut;
    }

    /**
     * Повертає тариф за ніч.
     *
     * @return тариф
     */
    public double getRate() {
        return rate;
    }

    /**
     * Порівнює записи за всіма полями.
     *
     * @param o інший об'єкт
     * @return {@code true}, якщо всі поля збігаються
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BookingRecord)) {
            return false;
        }
        BookingRecord other = (BookingRecord) o;
        return room == other.room
                && Double.compare(rate, other.rate) == 0
                && guest.equals(other.guest)
                && checkIn.equals(other.checkIn)
                && checkOut.equals(other.checkOut);
    }

    @Override
    public int hashCode() {
        return Objects.hash(room, guest, checkIn, checkOut, rate);
    }

    @Override
    public String toString() {
        return "Кімната " + room + ": " + guest + ", " + checkIn + " — " + checkOut + ", " + rate;
    }
}

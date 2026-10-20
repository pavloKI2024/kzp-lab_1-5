package ua.lpnu.kzp;

import java.util.Comparator;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Запити Stream API до колекції бронювань. Усі методи створюють новий потік,
 * не змінюють вхідний список і працюють через спільний тип {@link Booking}.
 */
public final class BookingQueries {

    private BookingQueries() {
    }

    /**
     * Відбір: проживання, що триває не менше заданої кількості ночей.
     *
     * @param bookings бронювання
     * @param minNights мінімальна кількість ночей
     * @return нова змінювана копія відібраних бронювань у вихідному порядку
     */
    public static List<Booking> longStays(List<Booking> bookings, int minNights) {
        return bookings.stream()
                .filter(b -> b.getNights() >= minNights)
                .collect(Collectors.toList());
    }

    /**
     * Перетворення: номери кімнат у вихідному порядку.
     *
     * @param bookings бронювання
     * @return список номерів кімнат
     */
    public static List<Integer> roomNumbers(List<Booking> bookings) {
        return bookings.stream()
                .map(Booking::getRoom)
                .toList();
    }

    /**
     * Групування: кількість бронювань за тарифом за ніч, ключі за зростанням.
     *
     * @param bookings бронювання
     * @return карта «тариф → кількість»
     */
    public static Map<Double, Long> countByRate(List<Booking> bookings) {
        return bookings.stream()
                .collect(Collectors.groupingBy(Booking::getNightlyRate, TreeMap::new,
                        Collectors.counting()));
    }

    /**
     * Статистика тривалості проживання (кількість, сума, мінімум, середнє, максимум).
     *
     * @param bookings бронювання
     * @return статистика; для порожнього входу кількість і сума дорівнюють 0
     */
    public static IntSummaryStatistics nightsStatistics(List<Booking> bookings) {
        return bookings.stream()
                .collect(Collectors.summarizingInt(Booking::getNights));
    }

    /**
     * Топ-N найдовших проживань: за кількістю ночей спадаючи, за рівності — за номером кімнати.
     *
     * @param bookings бронювання
     * @param limit    скільки елементів повернути
     * @return не більше {@code limit} бронювань
     * @throws IllegalArgumentException якщо {@code limit} від'ємний
     */
    public static List<Booking> topLongest(List<Booking> bookings, int limit) {
        if (limit < 0) {
            throw new IllegalArgumentException("Обмеження не може бути від'ємним");
        }
        Comparator<Booking> order = Comparator.comparingInt(Booking::getNights).reversed()
                .thenComparingInt(Booking::getRoom);
        return bookings.stream()
                .sorted(order)
                .limit(limit)
                .toList();
    }

    /**
     * Пошук першого бронювання за номером кімнати.
     *
     * @param bookings бронювання
     * @param room     номер кімнати
     * @return знайдене бронювання або {@link Optional#empty()}
     */
    public static Optional<Booking> findByRoom(List<Booking> bookings, int room) {
        return bookings.stream()
                .filter(b -> b.getRoom() == room)
                .findFirst();
    }

    /**
     * Найдовше проживання; за рівної тривалості повертається перше у списку.
     *
     * @param bookings бронювання
     * @return найдовше проживання або {@link Optional#empty()} для порожнього входу
     */
    public static Optional<Booking> longestStay(List<Booking> bookings) {
        return bookings.stream()
                .max(Comparator.comparingInt(Booking::getNights));
    }

    /**
     * Загальний виторг: сума поліморфних вартостей проживання.
     *
     * @param bookings бронювання
     * @return сума вартостей
     */
    public static double totalRevenue(List<Booking> bookings) {
        return bookings.stream()
                .map(Booking::getRevenue)
                .reduce(0.0, Double::sum);
    }
}

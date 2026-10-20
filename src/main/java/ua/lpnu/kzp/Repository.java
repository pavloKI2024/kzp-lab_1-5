package ua.lpnu.kzp;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Типобезпечне сховище об'єктів у пам'яті.
 *
 * @param <T> тип записів
 */
public final class Repository<T> {
    private final List<T> items = new ArrayList<>();

    /**
     * Додає непорожній запис до сховища.
     *
     * @param item запис
     * @throws NullPointerException якщо запис {@code null}
     */
    public void add(T item) {
        items.add(Objects.requireNonNull(item, "Запис не може бути null"));
    }

    /**
     * Повертає незмінний знімок усіх записів.
     *
     * @return знімок у порядку додавання
     */
    public List<T> all() {
        return List.copyOf(items);
    }

    /**
     * Повертає записи, що задовольняють умову.
     *
     * @param condition умова відбору
     * @return незмінний список знайдених записів
     */
    public List<T> find(Predicate<? super T> condition) {
        return items.stream().filter(condition).toList();
    }
}

package ua.lpnu.kzp;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Універсальний CSV-експортер: читає рефлексією поля з {@link CsvColumn} будь-якого класу.
 * Порядок стовпців детермінований — за іменем поля.
 */
public final class CsvExporter {

    private CsvExporter() {
    }

    /**
     * Записує анотовані поля об'єктів у CSV-файл (UTF-8, старий вміст перезаписується).
     *
     * @param <T>     тип записів
     * @param path    шлях до CSV-файлу
     * @param type    клас записів (потрібен для заголовка навіть без записів)
     * @param records записи для експорту
     * @throws DataStorageException якщо поля неможливо прочитати або файл записати
     */
    public static <T> void write(Path path, Class<T> type, List<? extends T> records)
            throws DataStorageException {
        String csv = toCsv(type, records);
        try {
            Path parent = path.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(path, csv, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new DataStorageException("Не вдалося записати CSV: " + path, e);
        }
    }

    /**
     * Формує CSV-текст із заголовком і рядками.
     *
     * @param <T>     тип записів
     * @param type    клас записів
     * @param records записи для експорту
     * @return текст CSV
     * @throws DataStorageException якщо немає анотованих полів або доступ до поля заборонено
     */
    public static <T> String toCsv(Class<T> type, List<? extends T> records)
            throws DataStorageException {
        List<Field> fields = annotatedFields(type);
        String nl = System.lineSeparator();
        StringBuilder csv = new StringBuilder(header(fields)).append(nl);
        for (T record : records) {
            List<String> values = new ArrayList<>();
            for (Field field : fields) {
                try {
                    values.add(escape(String.valueOf(field.get(record))));
                } catch (IllegalAccessException e) {
                    throw new DataStorageException(
                            "Немає доступу до поля " + field.getName() + " класу " + type.getName(), e);
                }
            }
            csv.append(String.join(",", values)).append(nl);
        }
        return csv.toString();
    }

    /**
     * Повертає рядок заголовка (без переведення рядка).
     *
     * @param type клас записів
     * @return назви стовпців через кому
     * @throws DataStorageException якщо немає анотованих полів
     */
    public static String header(Class<?> type) throws DataStorageException {
        return header(annotatedFields(type));
    }

    private static String header(List<Field> fields) {
        return fields.stream()
                .map(f -> escape(f.getAnnotation(CsvColumn.class).value()))
                .collect(Collectors.joining(","));
    }

    /**
     * Екранує значення: поле з комою, лапкою або переносом рядка береться в лапки,
     * а лапки всередині подвоюються.
     *
     * @param value текст поля
     * @return екрановане поле
     */
    public static String escape(String value) {
        if (value.indexOf(',') >= 0 || value.indexOf('"') >= 0
                || value.indexOf('\n') >= 0 || value.indexOf('\r') >= 0) {
            return '"' + value.replace("\"", "\"\"") + '"';
        }
        return value;
    }

    private static List<Field> annotatedFields(Class<?> type) throws DataStorageException {
        List<Field> fields = new ArrayList<>();
        for (Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass()) {
            for (Field field : c.getDeclaredFields()) {
                if (field.isAnnotationPresent(CsvColumn.class)) {
                    fields.add(field);
                }
            }
        }
        if (fields.isEmpty()) {
            throw new DataStorageException(
                    "Клас " + type.getName() + " не має полів з анотацією @CsvColumn");
        }
        fields.sort(Comparator.comparing(Field::getName));
        for (Field field : fields) {
            if (!field.trySetAccessible()) {
                throw new DataStorageException(
                        "Поле " + field.getName() + " класу " + type.getName() + " недоступне");
            }
        }
        return fields;
    }
}

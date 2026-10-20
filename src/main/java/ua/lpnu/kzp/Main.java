package ua.lpnu.kzp;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Properties;

/**
 * Консольна програма «Готель» (варіант 14).
 *
 * <p>Читає записи формату {@code room;guest;nights;nightlyRate;category}
 * з CSV-файлу, пропускає некоректні рядки з поясненням причини та обчислює:
 * кількість коректних записів, сумарну кількість ночей, загальний виторг
 * і найдовше проживання.
 */
public final class Main {

    private static final String DEFAULT_INPUT = "data/input.csv";
    private static final String DEFAULT_OUTPUT = "output/report.txt";
    private static final String DEFAULT_EXPORT = "output/records.csv";

    private Main() {
    }

    /**
     * Точка входу. Підтримує {@code --help}, {@code --version},
     * {@code --input <шлях>}, {@code --output <шлях>}, а також імпорт і експорт CSV із датами:
     * {@code --records <шлях>} та {@code --export <шлях>}.
     *
     * @param args аргументи командного рядка
     */
    public static void main(String[] args) {
        String input = DEFAULT_INPUT;
        String output = DEFAULT_OUTPUT;
        String records = null;
        String export = DEFAULT_EXPORT;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--help" -> {
                    printHelp();
                    return;
                }
                case "--version" -> {
                    System.out.println("lab01 " + readVersion());
                    return;
                }
                case "--input" -> {
                    if (++i >= args.length) {
                        fail("Для --input потрібно вказати шлях");
                        return;
                    }
                    input = args[i];
                }
                case "--output" -> {
                    if (++i >= args.length) {
                        fail("Для --output потрібно вказати шлях");
                        return;
                    }
                    output = args[i];
                }
                case "--records" -> {
                    if (++i >= args.length) {
                        fail("Для --records потрібно вказати шлях");
                        return;
                    }
                    records = args[i];
                }
                case "--export" -> {
                    if (++i >= args.length) {
                        fail("Для --export потрібно вказати шлях");
                        return;
                    }
                    export = args[i];
                }
                default -> {
                    fail("Невідомий аргумент: " + args[i]);
                    return;
                }
            }
        }

        try {
            run(Path.of(input), Path.of(output));
            if (records != null) {
                runRecords(Path.of(records), Path.of(export));
            }
        } catch (IOException e) {
            fail("Помилка вводу/виводу: " + e.getMessage());
        } catch (DataStorageException e) {
            Throwable cause = e.getCause();
            fail(e.getMessage() + (cause == null ? "" : " (причина: " + cause + ")"));
        }
    }

    /**
     * Імпортує CSV із датами у сховище та експортує його назад.
     *
     * @param records шлях до CSV із заголовком
     * @param export  шлях для експортованого CSV
     * @throws DataStorageException якщо файл не прочитано, заголовок чи записи хибні
     */
    private static void runRecords(Path records, Path export) throws DataStorageException {
        Repository<BookingRecord> repository = importRecords(records);
        CsvExporter.write(export, BookingRecord.class, repository.all());
        System.out.println("Імпортовано записів: " + repository.all().size());
        System.out.println("Експортовано в " + export);
    }

    /**
     * Читає CSV-файл (UTF-8), перевіряє заголовок і відновлює записи.
     *
     * @param path шлях до CSV
     * @return сховище відновлених записів
     * @throws DataStorageException якщо файл недоступний, заголовок не збігається або запис хибний
     */
    static Repository<BookingRecord> importRecords(Path path) throws DataStorageException {
        String text;
        try {
            text = Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new DataStorageException("Не вдалося прочитати CSV: " + path, e);
        }
        List<List<String>> rows = CsvParser.parseDocument(text);
        String expected = CsvExporter.header(BookingRecord.class);
        if (rows.isEmpty() || !String.join(",", rows.get(0).stream().map(CsvExporter::escape).toList())
                .equals(expected)) {
            throw new DataStorageException("Очікується заголовок: " + expected);
        }
        Repository<BookingRecord> repository = new Repository<>();
        for (int i = 1; i < rows.size(); i++) {
            try {
                repository.add(BookingRecord.fromFields(rows.get(i)));
            } catch (DataStorageException e) {
                throw new DataStorageException("Запис " + i + ": " + e.getMessage(), e);
            }
        }
        return repository;
    }

    /**
     * Читає версію програми з ресурсу {@code version.properties},
     * у який Maven підставляє версію з {@code pom.xml}.
     *
     * @return версія або {@code unknown}, якщо ресурс недоступний
     */
    private static String readVersion() {
        try (InputStream in = Main.class.getResourceAsStream("/version.properties")) {
            if (in == null) {
                return "unknown";
            }
            Properties props = new Properties();
            props.load(in);
            return props.getProperty("version", "unknown");
        } catch (IOException e) {
            return "unknown";
        }
    }

    private static void printHelp() {
        System.out.println("Використання: java -jar lab01.jar [--input <файл>] [--output <файл>]");
        System.out.println("  --input   вхідний CSV (за замовчуванням " + DEFAULT_INPUT + ")");
        System.out.println("  --output  файл звіту (за замовчуванням " + DEFAULT_OUTPUT + ")");
        System.out.println("  --records CSV із датами (заголовок + записи) для імпорту");
        System.out.println("  --export  куди експортувати імпортовані записи (за замовчуванням "
                + DEFAULT_EXPORT + ")");
        System.out.println("  --help    показати довідку");
        System.out.println("  --version показати версію");
        System.out.println("Формат рядка: room;guest;nights;nightlyRate;category");
        System.out.println("Формат --records: " + recordsHeader());
    }

    private static String recordsHeader() {
        try {
            return CsvExporter.header(BookingRecord.class);
        } catch (DataStorageException e) {
            return "невідомий";
        }
    }

    private static void fail(String message) {
        System.err.println(message);
        System.exit(1);
    }

    /**
     * Читає файл, обробляє записи та виводить звіт у консоль і файл.
     *
     * @param input  шлях до вхідного CSV
     * @param output шлях до файлу звіту
     * @throws IOException якщо файл не вдалося прочитати або записати
     */
    private static void run(Path input, Path output) throws IOException {
        List<String> lines = Files.readAllLines(input, StandardCharsets.UTF_8);
        String report = buildReport(lines);
        System.out.print(report);

        Path parent = output.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.writeString(output, report, StandardCharsets.UTF_8);
    }

    /**
     * Перевіряє рядки, обчислює показники та формує текст звіту.
     *
     * @param lines рядки вхідного файлу
     * @return текст звіту разом із повідомленнями про пропущені рядки
     */
    static String buildReport(List<String> lines) {
        List<String> messages = new ArrayList<>();
        List<Booking> bookings = new ArrayList<>();

        for (int i = 0; i < lines.size(); i++) {
            try {
                bookings.add(Booking.fromCsv(lines.get(i)));
            } catch (IllegalArgumentException e) {
                messages.add("Рядок " + (i + 1) + " пропущено: " + e.getMessage());
            }
        }

        int count = bookings.size();
        IntSummaryStatistics nights = BookingQueries.nightsStatistics(bookings);
        long totalNights = nights.getSum();
        double revenue = BookingQueries.totalRevenue(bookings);
        Optional<Booking> longest = BookingQueries.longestStay(bookings);

        StringBuilder sb = new StringBuilder();
        String nl = System.lineSeparator();
        for (String m : messages) {
            sb.append(m).append(nl);
        }
        sb.append("=== Звіт: Готель ===").append(nl);
        sb.append("Коректних записів: ").append(count).append(nl);
        if (longest.isEmpty()) {
            sb.append("Коректних записів немає, показники не обчислено").append(nl);
        } else {
            sb.append(String.format(Locale.ROOT, "Сумарна кількість ночей: %d%n", totalNights));
            sb.append(String.format(Locale.ROOT, "Загальний виторг: %.2f%n", revenue));
            sb.append(String.format(Locale.ROOT, "Найдовше проживання: %d ночей (%s)%n",
                    longest.get().getNights(), longest.get().getGuest()));
        }

        return sb.toString();
    }
}

package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CsvRoundTripTest {

    private static final String HEADER = "заїзд,виїзд,гість,тариф,номер";

    private static BookingRecord record(int room, String guest, String in, String out, double rate) {
        return new BookingRecord(room, guest, LocalDate.parse(in), LocalDate.parse(out), rate);
    }

    private static Path writeCsv(Path dir, String content) throws IOException {
        Path file = dir.resolve("records.csv");
        Files.writeString(file, content, StandardCharsets.UTF_8);
        return file;
    }

    @Test
    void roundTripReturnsEqualObjects(@TempDir Path dir) throws Exception {
        List<BookingRecord> original = List.of(
                record(101, "Іваненко Петро", "2026-10-01", "2026-10-04", 1200.5),
                record(102, "Коваль, Олена", "2026-10-02", "2026-10-09", 2000.0),
                record(103, "Бондар \"Макс\" Максим", "2026-10-05", "2026-10-07", 850.25),
                record(104, "Перший\nДругий рядок", "2026-11-01", "2026-11-03", 0.0));
        Path file = dir.resolve("out").resolve("records.csv");

        CsvExporter.write(file, BookingRecord.class, original);
        List<BookingRecord> restored = Main.importRecords(file).all();

        assertEquals(original, restored);
    }

    @Test
    void multilineFieldIsOneRecordButSeveralPhysicalLines(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("records.csv");
        CsvExporter.write(file, BookingRecord.class,
                List.of(record(1, "А\nБ", "2026-10-01", "2026-10-02", 1)));

        assertEquals(3, Files.readAllLines(file, StandardCharsets.UTF_8).size());
        assertEquals(1, Main.importRecords(file).all().size());
    }

    @Test
    void emptyDataExportsHeaderOnlyAndImportsEmptyRepository(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("records.csv");

        CsvExporter.write(file, BookingRecord.class, List.of());

        assertEquals(List.of(HEADER), Files.readAllLines(file, StandardCharsets.UTF_8));
        assertTrue(Main.importRecords(file).all().isEmpty());
    }

    @Test
    void completelyEmptyFileIsRejectedAsMissingHeader(@TempDir Path dir) throws Exception {
        Path file = writeCsv(dir, "");

        DataStorageException e = assertThrows(DataStorageException.class,
                () -> Main.importRecords(file));
        assertTrue(e.getMessage().contains("заголовок"));
    }

    @Test
    void wrongHeaderIsRejected(@TempDir Path dir) throws Exception {
        Path file = writeCsv(dir, "a,b,c\n");

        assertThrows(DataStorageException.class, () -> Main.importRecords(file));
    }

    @Test
    void corruptedRowWithWrongFieldCountIsReportedWithRowNumber(@TempDir Path dir) throws Exception {
        Path file = writeCsv(dir, HEADER + "\n2026-10-01,2026-10-04,Іваненко,1200.5\n");

        DataStorageException e = assertThrows(DataStorageException.class,
                () -> Main.importRecords(file));

        assertTrue(e.getMessage().contains("Запис 1"));
        assertInstanceOf(DataStorageException.class, e.getCause());
        assertNull(e.getCause().getCause());
    }

    @Test
    void corruptedDateKeepsParseExceptionAsCause() {
        DataStorageException e = assertThrows(DataStorageException.class, () -> BookingRecord
                .fromFields(List.of("не-дата", "2026-10-04", "Іваненко", "1200.5", "101")));

        assertInstanceOf(DateTimeParseException.class, e.getCause());
    }

    @Test
    void corruptedNumberKeepsNumberFormatExceptionAsCause() {
        DataStorageException e = assertThrows(DataStorageException.class, () -> BookingRecord
                .fromFields(List.of("2026-10-01", "2026-10-04", "Іваненко", "abc", "101")));

        assertInstanceOf(NumberFormatException.class, e.getCause());
    }

    @Test
    void brokenModelRuleKeepsIllegalArgumentExceptionAsCause() {
        DataStorageException e = assertThrows(DataStorageException.class, () -> BookingRecord
                .fromFields(List.of("2026-10-04", "2026-10-01", "Іваненко", "1200.5", "101")));

        assertInstanceOf(IllegalArgumentException.class, e.getCause());
    }

    @Test
    void missingFileKeepsIoExceptionAsCause(@TempDir Path dir) {
        Path missing = dir.resolve("немає.csv");

        DataStorageException e = assertThrows(DataStorageException.class,
                () -> Main.importRecords(missing));

        assertInstanceOf(IOException.class, e.getCause());
        assertTrue(e.getMessage().contains("немає.csv"));
    }

    @Test
    void writeFailureKeepsIoExceptionAsCause(@TempDir Path dir) throws Exception {
        Path blocker = writeCsv(dir, "файл, а не тека");
        Path target = blocker.resolve("records.csv");

        DataStorageException e = assertThrows(DataStorageException.class,
                () -> CsvExporter.write(target, BookingRecord.class, List.of()));

        assertNotNull(e.getCause());
        assertInstanceOf(IOException.class, e.getCause());
    }

    @Test
    void exceptionWithoutCauseHasNoCause() {
        assertNull(new DataStorageException("тест").getCause());
    }
}

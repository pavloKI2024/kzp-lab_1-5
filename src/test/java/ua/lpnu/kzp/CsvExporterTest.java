package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CsvExporterTest {

    /** Довільний клас поза доменом готелю: доводить універсальність експортера. */
    static class Item {
        @CsvColumn("Назва")
        private final String title;

        @CsvColumn("Рік")
        private final int year;

        @SuppressWarnings("unused")
        private final String notExported = "приховано";

        Item(String title, int year) {
            this.title = title;
            this.year = year;
        }
    }

    static class NoColumns {
        @SuppressWarnings("unused")
        private int x;
    }

    private static final String NL = System.lineSeparator();

    @Test
    void headerIsSortedByFieldNameAndSkipsUnannotated() throws DataStorageException {
        assertEquals("Назва,Рік", CsvExporter.header(Item.class));
    }

    @Test
    void bookingRecordHeaderMatchesColumns() throws DataStorageException {
        assertEquals("заїзд,виїзд,гість,тариф,номер", CsvExporter.header(BookingRecord.class));
    }

    @Test
    void exportsAnyAnnotatedClass() throws DataStorageException {
        String csv = CsvExporter.toCsv(Item.class, List.of(new Item("Карти", 1988)));

        assertEquals("Назва,Рік" + NL + "Карти,1988" + NL, csv);
    }

    @Test
    void escapesCommaQuoteAndNewline() {
        assertEquals("plain", CsvExporter.escape("plain"));
        assertEquals("\"a,b\"", CsvExporter.escape("a,b"));
        assertEquals("\"a\"\"b\"", CsvExporter.escape("a\"b"));
        assertEquals("\"a\nb\"", CsvExporter.escape("a\nb"));
    }

    @Test
    void classWithoutAnnotatedFieldsIsRejected() {
        assertThrows(DataStorageException.class, () -> CsvExporter.header(NoColumns.class));
    }

    @Test
    void writesUtf8FileAndOverwritesOldContent(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("nested").resolve("items.csv");
        CsvExporter.write(file, Item.class,
                List.of(new Item("Дуже довга назва першого запису", 1), new Item("Б", 2)));
        CsvExporter.write(file, Item.class, List.of(new Item("А", 3)));

        String text = Files.readString(file, StandardCharsets.UTF_8);
        assertEquals("Назва,Рік" + NL + "А,3" + NL, text);
        assertTrue(Files.size(file) > 0);
    }
}

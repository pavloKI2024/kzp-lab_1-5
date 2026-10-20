package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

class CsvParserTest {

    @Test
    void parsesSimpleRows() throws DataStorageException {
        assertEquals(List.of(List.of("a", "b"), List.of("c", "d")),
                CsvParser.parseDocument("a,b\nc,d\n"));
    }

    @Test
    void commaInsideQuotesStaysInField() throws DataStorageException {
        assertEquals(List.of(List.of("Коваль, Олена", "1")),
                CsvParser.parseDocument("\"Коваль, Олена\",1"));
    }

    @Test
    void doubledQuoteBecomesOneQuote() throws DataStorageException {
        assertEquals(List.of(List.of("Макс \"Лі\"")),
                CsvParser.parseDocument("\"Макс \"\"Лі\"\"\""));
    }

    @Test
    void newlineInsideQuotesIsNotRecordBoundary() throws DataStorageException {
        List<List<String>> rows = CsvParser.parseDocument("a,\"x\ny\"\nb,c\n");

        assertEquals(2, rows.size());
        assertEquals("x\ny", rows.get(0).get(1));
    }

    @Test
    void handlesCrLfAndMissingFinalNewline() throws DataStorageException {
        assertEquals(List.of(List.of("a", "b"), List.of("c", "d")),
                CsvParser.parseDocument("a,b\r\nc,d"));
    }

    @Test
    void keepsEmptyFields() throws DataStorageException {
        assertEquals(List.of(List.of("a", "", "")), CsvParser.parseDocument("a,,\n"));
    }

    @Test
    void unterminatedQuoteIsRejected() {
        assertThrows(DataStorageException.class, () -> CsvParser.parseDocument("\"abc"));
    }

    @Test
    void quoteInsideUnquotedFieldIsRejected() {
        assertThrows(DataStorageException.class, () -> CsvParser.parseDocument("ab\"c\"\n"));
    }
}

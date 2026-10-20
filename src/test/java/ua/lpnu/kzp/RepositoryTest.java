package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

class RepositoryTest {

    @Test
    void addAndAllKeepOrder() {
        Repository<String> repo = new Repository<>();
        repo.add("a");
        repo.add("b");

        assertEquals(List.of("a", "b"), repo.all());
    }

    @Test
    void allReturnsImmutableSnapshot() {
        Repository<String> repo = new Repository<>();
        repo.add("a");
        List<String> snapshot = repo.all();
        repo.add("b");

        assertEquals(List.of("a"), snapshot);
        assertThrows(UnsupportedOperationException.class, () -> snapshot.add("x"));
    }

    @Test
    void findFiltersByPredicate() {
        Repository<Integer> repo = new Repository<>();
        repo.add(1);
        repo.add(2);
        repo.add(3);

        assertEquals(List.of(2, 3), repo.find(n -> n > 1));
    }

    @Test
    void findAcceptsPredicateOfSupertype() {
        Repository<String> repo = new Repository<>();
        repo.add("abc");
        repo.add("d");
        java.util.function.Predicate<Object> notNullText = o -> o.toString().length() > 1;

        assertEquals(List.of("abc"), repo.find(notNullText));
    }

    @Test
    void addRejectsNull() {
        assertThrows(NullPointerException.class, () -> new Repository<String>().add(null));
    }
}

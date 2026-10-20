package ua.lpnu.kzp;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Позначає поле, яке експортується в CSV, і задає назву його стовпця. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface CsvColumn {
    /**
     * Повертає заголовок CSV-стовпця.
     *
     * @return назва стовпця
     */
    String value();
}

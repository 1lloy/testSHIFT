package unit;

import com.illoy.LineStats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LineStatsTest {

    private LineStats stats;

    @BeforeEach
    void setUp() {
        stats = new LineStats();
    }

    @Test
    void addLong_singleValue() {
        stats.addLong(5);

        assertEquals(1, stats.getLongCount());
        assertEquals(BigDecimal.valueOf(5), stats.getLongMin());
        assertEquals(BigDecimal.valueOf(5), stats.getLongMax());
        assertEquals(0, stats.getLongAverage().compareTo(BigDecimal.valueOf(5.0)));
    }

    @Test
    void addLong_multipleValues() {
        stats.addLong(10);
        stats.addLong(-5);
        stats.addLong(15);

        assertEquals(3, stats.getLongCount());
        assertEquals(BigDecimal.valueOf(-5), stats.getLongMin());
        assertEquals(BigDecimal.valueOf(15), stats.getLongMax());
        assertEquals(20.0 / 3, stats.getLongAverage().doubleValue(), 1e-6);
    }

    @Test
    void addLong_onlyNegativeValues() {
        stats.addLong(-10);
        stats.addLong(-20);

        assertEquals(2, stats.getLongCount());
        assertEquals(BigDecimal.valueOf(-20), stats.getLongMin());
        assertEquals(BigDecimal.valueOf(-10), stats.getLongMax());
    }

    @Test
    void addDouble_singleValue() {
        stats.addDouble(2.5);

        assertEquals(1, stats.getDoubleCount());
        assertEquals(0, stats.getDoubleMin().compareTo(BigDecimal.valueOf(2.5)));
        assertEquals(0, stats.getDoubleMax().compareTo(BigDecimal.valueOf(2.5)));
        assertEquals(0, stats.getDoubleAverage().compareTo(BigDecimal.valueOf(2.5)));
    }

    @Test
    void addDouble_multipleValues() {
        stats.addDouble(1.5);
        stats.addDouble(-2.5);
        stats.addDouble(3.0);

        assertEquals(3, stats.getDoubleCount());
        assertEquals(0, stats.getDoubleMin().compareTo(BigDecimal.valueOf(-2.5)));
        assertEquals(0, stats.getDoubleMax().compareTo(BigDecimal.valueOf(3.0)));
        assertEquals(2.0 / 3, stats.getDoubleAverage().doubleValue(), 1e-6);
    }

    @Test
    void addNumber_longGoesToLongStats() {
        stats.add(10L);

        assertEquals(1, stats.getLongCount());
        assertEquals(0, stats.getDoubleCount());
    }

    @Test
    void addNumber_doubleGoesToDoubleStats() {
        stats.add(3.14);

        assertEquals(0, stats.getLongCount());
        assertEquals(1, stats.getDoubleCount());
    }

    @Test
    void addString_singleString() {
        stats.addString("hello");

        assertEquals(1, stats.getStringCount());
        assertEquals(5, stats.getStringLengthMin());
        assertEquals(5, stats.getStringLengthMax());
    }

    @Test
    void addString_multipleStringsDifferentLengths() {
        stats.addString("a");
        stats.addString("abcd");
        stats.addString("abc");

        assertEquals(3, stats.getStringCount());
        assertEquals(1, stats.getStringLengthMin());
        assertEquals(4, stats.getStringLengthMax());
    }

    @Test
    void averagesWhenNoData_shouldReturnZero() {
        assertEquals(0, stats.getLongAverage().compareTo(BigDecimal.valueOf(0)));
        assertEquals(0, stats.getDoubleAverage().compareTo(BigDecimal.valueOf(0)));
    }
}

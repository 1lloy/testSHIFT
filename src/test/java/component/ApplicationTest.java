package component;

import com.illoy.Application;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static com.illoy.Application.filesNames;
import static com.illoy.Application.lineStats;
import static com.illoy.Application.isIntFileCreated;
import static com.illoy.Application.isFloatFileCreated;
import static com.illoy.Application.isStringFileCreated;
import static com.illoy.Application.outputPath;
import static com.illoy.Application.filePrefix;
import static com.illoy.Application.isAppendingMode;
import static com.illoy.Application.isStatisticsNeeded;
import static com.illoy.Application.isFullStatisticsMode;
import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ApplicationTest {
    private final Path tempDir = Paths.get("test_output");

    @BeforeAll
    void setup() throws IOException {
        if (Files.exists(tempDir)) {
            deleteDirectory(tempDir);
        }
        Files.createDirectories(tempDir);
    }

    @BeforeEach
    void setupEach() {
        filesNames.clear();
        lineStats = null;

        isIntFileCreated = false;
        isFloatFileCreated = false;
        isStringFileCreated = false;

        outputPath = "";
        filePrefix = "";
        isAppendingMode = false;
        isStatisticsNeeded = false;
        isFullStatisticsMode = false;
    }

    @AfterAll
    void cleanup() throws IOException {
        deleteDirectory(tempDir);
    }

    private void deleteDirectory(Path path) throws IOException {
        try (Stream<Path> paths = Files.walk(path)) {
            paths
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }

    @Test
    void testProcessingMixedFile() throws IOException {
        Path inputFile = tempDir.resolve("input.txt");
        Files.write(inputFile, List.of(
                "123",
                "45.67",
                "Hello World",
                "1e5",
                "3,14159",
                "12e-2",
                "-244455e2"
        ));

        // запускаем main с параметрами
        String[] args = {"-o", tempDir.toString(), "-p", "test_", "-s", inputFile.toString()};
        Application.main(args);

        // проверяем, что файлы созданы
        Path intFile = tempDir.resolve("test_integers.txt");
        Path floatFile = tempDir.resolve("test_floats.txt");
        Path stringFile = tempDir.resolve("test_strings.txt");

        assertTrue(Files.exists(intFile), "Integers file should exist");
        assertTrue(Files.exists(floatFile), "Floats file should exist");
        assertTrue(Files.exists(stringFile), "Strings file should exist");

        // проверяем содержимое файлов
        List<String> ints = Files.readAllLines(intFile);
        List<String> floats = Files.readAllLines(floatFile);
        List<String> strings = Files.readAllLines(stringFile);

        assertEquals(List.of("123", "1e5", "-244455e2"), ints);
        assertEquals(List.of("45.67", "3,14159", "12e-2"), floats);
        assertEquals(List.of("Hello World"), strings);
    }

    @Test
    void testAppendingMode() throws IOException {
        Path existingIntFile = tempDir.resolve("test_integers.txt");
        Files.write(existingIntFile, List.of("-1211", "24e+3", "0"));

        Path inputFile = tempDir.resolve("inputFile.txt");
        Files.write(inputFile, List.of("-102", "2020", "1000e-2"));

        String[] args = {"-o", tempDir.toString(), "-p", "test_", "-a", inputFile.toString()};
        Application.main(args);

        List<String> ints = Files.readAllLines(existingIntFile);
        assertEquals(6, ints.size(), "File should have appended lines");
    }

    @Test
    void testAppendingModeWithEqualsInputOutputPaths() throws IOException {
        Path intFile = tempDir.resolve("test_integers.txt");
        Files.write(intFile, List.of("-1211"));

        String[] args = {"-o", tempDir.toString(), "-p", "test_", "-a", intFile.toString()};
        Application.main(args);

        List<String> ints = Files.readAllLines(intFile);
        assertEquals(1, ints.size(), "File should have appended lines");
    }

    @Test
    void testFileNotFound() {
        String[] args = {"nonexistent.txt"};

        assertDoesNotThrow(() -> Application.main(args));
    }

    @Test
    void testStatisticsMode() throws IOException {
        Path inputFile = tempDir.resolve("stats.txt");
        Files.write(inputFile, List.of("10", "20", "30", "text"));

        String[] args = {"-o", tempDir.toString(), "-s", inputFile.toString()};

        assertDoesNotThrow(() -> Application.main(args));
    }
}

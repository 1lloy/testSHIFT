package com.illoy;

import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Главный класс приложения для обработки текстовых файлов.
 *
 * <p>Программа читает указанные файлы, классифицирует строки на целые числа,
 * числа с плавающей точкой и обычные строки. В зависимости от ключей командной строки
 * можно указать путь и префикс для файлов вывода, режим добавления, а также
 * включить сбор статистики по числам и строкам.</p>
 *
 * <p>Файлы вывода:
 * <ul>
 *     <li>integers.txt — для целых чисел</li>
 *     <li>floats.txt — для чисел с плавающей точкой</li>
 *     <li>strings.txt — для остальных строк</li>
 * </ul>
 * </p>
 */
public class Application {
    public static List<String> filesNames = new ArrayList<>();

    public static LineStats lineStats = null;

    public static boolean isIntFileCreated = false;
    public static boolean isFloatFileCreated = false;
    public static boolean isStringFileCreated = false;

    static final String outputStringFileName = "strings.txt";
    static final String outputIntFileName = "integers.txt";
    static final String outputFloatFileName = "floats.txt";

    public static String outputPath = "";
    public static String filePrefix = "";
    public static boolean isAppendingMode = false;
    public static boolean isStatisticsNeeded = false;
    public static boolean isFullStatisticsMode = false;

    public static void main(String[] args) {
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-o":
                    outputPath = args[++i];
                    break;
                case "-p":
                    filePrefix = args[++i];
                    break;
                case "-a":
                    isAppendingMode = true;
                    break;
                case "-s":
                    isStatisticsNeeded = true;
                    lineStats = new LineStats();
                    break;
                case "-f":
                    isStatisticsNeeded = true;
                    isFullStatisticsMode = true;
                    lineStats = new LineStats();
                    break;
            }

            if (args[i].toLowerCase().endsWith(".txt")) {
                filesNames.add(args[i]);
            }
        }

        if (filesNames.isEmpty()) {
            System.out.println("No files \".txt\" to process. Exiting...");
            return;
        }

        processFiles(filesNames);

        if (isStatisticsNeeded) lineStats.printStatistics(isFullStatisticsMode);
    }

    /**
     * Обрабатывает список файлов.
     *
     * <p>Считывает строки из каждого файла и распределяет их по типу:
     * числа или строки. Игнорирует пустые строки.</p>
     *
     * @param filesPaths список путей к файлам для обработки
     */
    private static void processFiles(List<String> filesPaths) {
        Scanner scanner = null;

        for (String path : filesPaths) {
            try{
                if (checkOutputInputPathsEquality(Path.of(path), outputPath)) {
                    System.out.println("Input file equals output file. Skipping: " + path);
                    continue;
                }

                scanner = new Scanner(new File(path));

                while(scanner.hasNextLine()) {
                    String line = scanner.nextLine().trim();

                    if (!line.isEmpty()){
                        if (Pattern.matches("^(-?(?:[1-9]\\d*|0))(?:[eE][+-]?[0-9]+)?$", line) ||
                                Pattern.matches("^[-+]?[0-9]+[.,][0-9]+(?:[eE][+-]?[0-9]+)?$", line)) {

                            processNumber(line);
                        }
                        else {
                            processString(line);
                        }
                    }
                }
            } catch (FileNotFoundException e) {
                System.out.println("File \"" + path + "\" not found. This file will be skipped.");
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
            finally {
                if (scanner != null) {
                    scanner.close();
                }
            }
        }
    }

    /**
     * Записывает строку в файл указанного типа.
     *
     * <p>Создает файл и директории при необходимости. Если файл уже существует,
     * поведение зависит от режима добавления.</p>
     *
     * @param line строка для записи
     * @param fileType тип файла (integers.txt, floats.txt, strings.txt)
     * @return true, чтобы параметр is[Int, Float, String]FileCreated позволял дозаписывать в файл последующие строки в случае создания нового файла
     * @throws IOException если произошла ошибка при создании или записи файла
     */
    private static boolean writeToFile(String line, String fileType) throws IOException {
        Path newPath = getUniversalPath(outputPath, fileType);
        Path parentDir = newPath.getParent();

        try {
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }

            if (!Files.exists(newPath)) {
                Files.createFile(newPath);
                System.out.println("File " + newPath + " created.\n");
                if (newPath.toString().contains(outputIntFileName)) isIntFileCreated = true;
                if (newPath.toString().contains(outputFloatFileName)) isFloatFileCreated = true;
                if (newPath.toString().contains(outputStringFileName)) isStringFileCreated = true;
            }
        } catch (IOException e){
            throw new IOException("Error while creating file: " + newPath);
        }

        boolean isCurrentFileCreated = false;
        if (newPath.toString().contains(outputIntFileName)) isCurrentFileCreated = isIntFileCreated;
        if (newPath.toString().contains(outputFloatFileName)) isCurrentFileCreated = isFloatFileCreated;
        if (newPath.toString().contains(outputStringFileName)) isCurrentFileCreated = isStringFileCreated;

        try (FileWriter fw = new FileWriter(newPath.toString(), (isCurrentFileCreated || isAppendingMode))) {
            fw.write(line + "\n");
        } catch (IOException e) {
            throw new IOException("Error while writing to file: " + newPath);
        }

        return true;
    }

    /**
     * Формирует универсальный путь выходного файла.
     *
     * @param path директория выходного файла в виде строки
     * @param fileType тип выходного файла в виде строки (integers.txt, floats.txt, strings.txt)
     * @return универсальный {@link Path} к выходному файлу конкретного типа с префиксом
     */
    private static Path getUniversalPath(String path, String fileType){
        Path normalizedPath = Paths.get(path).normalize();

        if (!normalizedPath.isAbsolute()) {
            normalizedPath = Paths.get(System.getProperty("user.dir")).resolve(normalizedPath);
        }

        return normalizedPath.resolve(filePrefix + fileType);
    }

    /**
     * Проверяет, является ли входной файл выходным файлом
     *
     * @param inputPath путь входного файла
     * @param outputPath путь выходного файла, указываемого с ключом "-o", в виде строки
     * @return true, если путь входного файла будет равен пути любому из возможных выходных файлов
     */
    private static boolean checkOutputInputPathsEquality(Path inputPath, String outputPath) {
        inputPath = Paths.get(inputPath.toUri()).toAbsolutePath().normalize();
        Path outputIntPath = getUniversalPath(outputPath, outputIntFileName).toAbsolutePath().normalize();
        Path outputFloatPath = getUniversalPath(outputPath, outputFloatFileName).toAbsolutePath().normalize();
        Path outputStringPath = getUniversalPath(outputPath, outputStringFileName).toAbsolutePath().normalize();

        return inputPath.equals(outputIntPath) ||
                inputPath.equals(outputFloatPath) ||
                inputPath.equals(outputStringPath);
    }

    /**
     * Обрабатывает строку как число.
     *
     * <p>Использует {@link BigDecimal} для определения, является ли число
     * целым или дробным, и записывает его в соответствующий файл.
     * При необходимости собирает статистику.</p>
     *
     * @param line строка с числом
     * @throws IOException если возникает ошибка записи в файл или ошибка создания файла
     */
    private static void processNumber(String line) throws IOException {
        BigDecimal bdNumber = new BigDecimal(line.replaceAll(",", "."));

        if (bdNumber.stripTrailingZeros().scale() <= 0) {
            isIntFileCreated = writeToFile(line, outputIntFileName);

            if (isStatisticsNeeded) lineStats.add(bdNumber.longValue());
        }
        else{
            isFloatFileCreated = writeToFile(line, outputFloatFileName);

            if (isStatisticsNeeded) lineStats.add(bdNumber.doubleValue());
        }
    }

    /**
     * Обрабатывает строки.
     *
     * <p>Записывает строку в соответствующий файл.
     * При необходимости собирает статистику.</p>
     *
     * @param line строка с числом
     * @throws IOException если возникает ошибка записи в файл или ошибка создания файла
     */
    private static void processString(String line) throws IOException {
        isStringFileCreated = writeToFile(line, outputStringFileName);

        if (isStatisticsNeeded) lineStats.addString(line);
    }
}

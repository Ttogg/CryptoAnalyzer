package com.ttogg.analyzer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
/*
 *  Вся реализация в одном классе. Есть ли смысл создавать другие, если нам не нужно создавать экземпляры классов?
 * */

public class Main {
    private static final char[] ALPHABET =
            {'а', 'б', 'в', 'г', 'д', 'е', 'ё', 'ж', 'з', 'и', 'й', 'к', 'л', 'м', 'н', 'о', 'п',
                    'р', 'с', 'т', 'у', 'ф', 'х', 'ц', 'ч', 'ш', 'щ', 'ъ', 'ы', 'ь', 'э', 'ю', 'я',
                    '.', ',', '"', ':', '-', '!', '?', ' '};
    private static final char[] SYMBOL_ALPHABET =
            {'.', ','};
    private static final String ORIGINAL_FILE = "Enter original file (*.txt):";
    private static final String DESTINATION_FILE = "Enter destination file (*.txt):";
    private static final String EXAMPLE_FILE = "Enter example file (*.txt):";
    private static final String FILE_IS_EXIST = "File exists. Do you want to rewrite it? (y/n)";
    private static final String FILE_NOT_EXIST = "File not exists";
    private static final String ORIGINAL_DESTINATION = "Original file and destination file must be different.";
    private static final String MODE = "Enter mode (1 - Encryption; 2 - Decryption; 3 - BruteForce; 4 - Statistical Analysis):";
    private static final String KEY = "Enter key 0.." + (ALPHABET.length - 1) + ":";

    private static Path originalFile;
    private static Path destinationFile;
    private static Path exampleFile;


    public static void main(String[] args) throws IOException {
        try (Scanner scanner = new Scanner(System.in)) {
            /*
             *       В данный момент взаимодействие с пользователем происходит через консоль, это методы:
             *       askKey(), askFile(), askMode(), askSomething().
             *       Планирую перенести их в другой класс.
             *       Правильно ли я понимаю, что нужно создать class UserInterface с public static методами
             *       для получения информации от пользователя (если не делать на JavaFX или Swing)?
             */

            int mode = askMode(scanner);
            int key = 0;

            /*
             *       Тут плохое решение, но я не придумал лучше
             * */

            switch (mode) {
                case 1:
                    askFiles(scanner, ORIGINAL_FILE, DESTINATION_FILE);
                    key = askKey(scanner);
                    break;
                case 2:
                    askFiles(scanner, ORIGINAL_FILE, DESTINATION_FILE);
                    key = -askKey(scanner);
                    break;
                case 3:
                    askFiles(scanner, ORIGINAL_FILE, DESTINATION_FILE);
                    key = bruteForce(originalFile);
                    break;
                case 4:
                    askFiles(scanner, ORIGINAL_FILE, DESTINATION_FILE, EXAMPLE_FILE);
                    key = statisticalAnalysis(originalFile, exampleFile);
                    break;
                default:
                    System.out.println("Something went wrong");
                    System.exit(-1);
            }

            converting(originalFile, destinationFile, key);
        }
    }

    private static void converting(Path originalFile, Path destinationFile, int key) throws IOException {
        /*
         *       Здесь и других местах используется конструкция:
         *       try (BufferedReader reader = Files.newBufferedReader(originalFile, StandardCharsets.UTF_8)) {
         *           ...
         *       } catch (IOException e) {
         *           e.printStackTrace();
         *       }
         *       Не уверен, что это грамотное решение.
         * */
        try (BufferedReader reader = Files.newBufferedReader(originalFile, StandardCharsets.UTF_8);
             BufferedWriter writer = Files.newBufferedWriter(destinationFile, StandardCharsets.UTF_8)) {

            while (reader.ready()) {
                char c = newChar((char) reader.read(), key);
                writer.write(c);
            }
        }
    }

    private static char newChar(char odlChar, int key) {
        int newCharNumber = indexInAlphabet(odlChar, ALPHABET);

        if (newCharNumber < 0) {
            return odlChar;
        } else {
            newCharNumber += key;
            if (newCharNumber > ALPHABET.length - 1) {
                newCharNumber -= ALPHABET.length;
            } else if (newCharNumber < 0) {
                newCharNumber += ALPHABET.length;
            }
            return ALPHABET[newCharNumber];
        }
    }

    /*
     *       А есть ли другой вариант проверки наличия элемента в алфавите? Не вижу смысла char[] во что-то преобразовывать.
     *       Была идея преобразовать char[] в TreeSet, но будет ли это быстрее работать для этого размера алфавита,
     *       либо есть другие причины использовать TreeSet, или другой подход?
     * */
    private static int indexInAlphabet(char c, char[] alphabet) {
        int index = -1;
        for (int i = 0; i < alphabet.length; i++) {
            if (alphabet[i] == c) {
                index = i;
                break;
            }
        }
        return index;
    }

    private static int statisticalAnalysis(Path encryptedFile, Path exampleFile) throws IOException {
        int encryptedCount = 0;
        int exampleCount = 0;
        int maxLengthExample = 1_000_000;

        char[] encryptedChars = new char[1_000_000];
        try (BufferedReader reader = Files.newBufferedReader(encryptedFile, StandardCharsets.UTF_8)) {
            while (reader.ready() && encryptedCount < encryptedChars.length) {
                encryptedChars[encryptedCount++] = (char) reader.read();
            }
        }

        TreeMap<Character, Double> exampleFrequencies = new TreeMap<>();
        for (char c : ALPHABET) {
            exampleFrequencies.put(c, 0.0);
        }
        try (BufferedReader reader = Files.newBufferedReader(exampleFile, StandardCharsets.UTF_8)) {

            char mapKey;
            while (reader.ready() && exampleCount < maxLengthExample) {
                mapKey = (char) reader.read();
                if (exampleFrequencies.containsKey(mapKey)) {
                    exampleFrequencies.put(mapKey, exampleFrequencies.get(mapKey) + 1);
                    exampleCount++;
                }
            }
        }

        if (exampleCount == 0) {
            return 0;
        }
        for (Map.Entry<Character, Double> entry : exampleFrequencies.entrySet()) {
            entry.setValue(entry.getValue() / exampleCount * encryptedCount);
        }


        TreeMap<Integer, Double> checking = new TreeMap<>();
        for (int key = 0; key < ALPHABET.length; key++) {
            double resultOfCheck = checkKeyStatisticalAnalysis(encryptedChars, encryptedCount, -key, exampleFrequencies);
            checking.put(-key, resultOfCheck);
        }
        int bestKey = 0;
        double bestValue = Double.MAX_VALUE;
        for (Map.Entry<Integer, Double> entry : checking.entrySet()) {
            if (entry.getValue() < bestValue) {
                bestValue = entry.getValue();
                bestKey = entry.getKey();
            }
        }
        return bestKey;
    }

    private static double checkKeyStatisticalAnalysis(char[] encryptedChars, int encryptedCharCount, int key, TreeMap<Character, Double> exampleFrequencies) {
        double xi = 0;

        char[] newChars = new char[encryptedCharCount];
        for (int i = 0; i < encryptedCharCount; i++) {
            newChars[i] = newChar(encryptedChars[i], key);
        }

        TreeMap<Character, Double> encryptedFrequencies = new TreeMap<>();
        for (char c : ALPHABET) {
            encryptedFrequencies.put(c, 0.0);
        }
        char mapKey;
        for (int i = 0; i < encryptedCharCount; i++) {
            mapKey = newChars[i];
            if (encryptedFrequencies.containsKey(mapKey)) {
                encryptedFrequencies.put(mapKey, encryptedFrequencies.get(mapKey) + 1);
            }
        }

        char currentKey;
        double curEnValue;
        double curExValue;
        for (Map.Entry<Character, Double> entry : exampleFrequencies.entrySet()) {
            currentKey = entry.getKey();
            curEnValue = encryptedFrequencies.get(currentKey);
            curExValue = exampleFrequencies.get(currentKey);
            if (curExValue >= 1) {
                xi += (curEnValue - curExValue) * (curEnValue - curExValue) / curExValue;
            }
        }

        return xi;
    }

    private static int bruteForce(Path originalFile) throws IOException {
        int originalCharCount = 0;
        /*
         *       Как вариант для хранения части файла для обработки кроме массива, либо создания временных файлов
         *       ничего не придумал (есть еще ByteStream, CharStream, но есть ли смысл их использовать
         *       не знаю, и не совсем понимаю как ими пользоваться)
         *       Массив ниже имеет размер 2*1_000_000 = 2 Мбайт, если не ошибаюсь, что не много.
         *       При этом не уверен, что размера достаточно для анализа правильности подбора ключа.
         * */

        char[] originalChars = new char[1_000_000];
        try (BufferedReader reader = Files.newBufferedReader(originalFile, StandardCharsets.UTF_8)) {
            while (reader.ready() && originalCharCount < originalChars.length) {
                originalChars[originalCharCount++] = (char) reader.read();
            }
        }

        TreeMap<Integer, Double> checking = new TreeMap<>();
        for (int key = 0; key < ALPHABET.length; key++) {
            double resultOfCheck = checkKeyBruteForce(originalChars, originalCharCount, -key);
            checking.put(-key, resultOfCheck);
        }

        int bestKey = 0;
        double bestValue = -1;
        for (Map.Entry<Integer, Double> entry : checking.entrySet()) {
            if (entry.getValue() > bestValue) {
                bestValue = entry.getValue();
                bestKey = entry.getKey();
            }
        }
        return bestKey;
    }

    private static double checkKeyBruteForce(char[] chars, int charCount, int key) {
        int spaceCount = 0;
        int symbolCheckCount = 0;

        char[] newChars = new char[charCount];

        for (int i = 0; i < charCount - 1; i++) {
            newChars[i] = newChar(chars[i], key);
        }

        for (int i = 0; i < charCount - 1; i++) {
            if (newChars[i] == ' ') {
                spaceCount++;
            }
            if (indexInAlphabet(newChars[i], SYMBOL_ALPHABET) >= 0 && newChars[i + 1] == ' ') {
                symbolCheckCount++;
            }
        }

        return 100.0 * spaceCount / charCount + 700.0 * symbolCheckCount / charCount;
    }

    private static int askMode(Scanner scanner) {
        int mode;
        while (true) {
            try {
                System.out.println(MODE);
                mode = Integer.parseInt(askSomething(scanner));
            } catch (NumberFormatException e) {
                continue;
            }
            if (mode >= 1 && mode <= 4) {
                return mode;
            }
        }
    }

    private static void askFiles(Scanner scanner, String type1, String type2) throws IOException {
        while (true) {
            System.out.println(type1);
            String path1 = askSomething(scanner);
            if (!path1.endsWith(".txt")) {
                continue;
            }

            Path file1 = new File(path1).toPath();
            if (Files.isRegularFile(file1)) {
                originalFile = file1;
                break;
            } else {
                System.out.println(FILE_NOT_EXIST);
            }
        }

        while (true) {
            System.out.println(type2);
            String path2 = askSomething(scanner);
            if (!path2.endsWith(".txt")) {
                continue;
            }

            Path file2 = new File(path2).toPath();
            if (Files.isSameFile(originalFile, file2)) {
                System.out.println(ORIGINAL_DESTINATION);
                continue;
            }
            if (Files.exists(file2) && Files.isRegularFile(file2)) {
                System.out.println(FILE_IS_EXIST);
                if (askSomething(scanner).equals("y")) {
                    destinationFile = file2;
                    break;
                }
            } else {
                if (Files.isDirectory(file2.getParent())) {
                    destinationFile = file2;
                    break;
                }
            }
        }
    }

    private static void askFiles(Scanner scanner, String type1, String type2, String type3) throws IOException {
        askFiles(scanner, type1, type2);
        while (true) {
            System.out.println(type3);
            String path1 = askSomething(scanner);
            if (!path1.endsWith(".txt")) {
                continue;
            }

            Path file3 = new File(path1).toPath();
            if (Files.isSameFile(destinationFile, file3)) {
                System.out.println(ORIGINAL_DESTINATION);
                continue;
            }
            if (Files.isRegularFile(file3)) {
                exampleFile = file3;
                break;
            } else {
                System.out.println(FILE_NOT_EXIST);
            }
        }
    }

    private static int askKey(Scanner scanner) {
        int key;
        while (true) {
            System.out.println(KEY);
            try {
                key = Integer.parseInt(askSomething(scanner));
            } catch (NumberFormatException e) {
                continue;
            }
            if (key >= 0 && key < ALPHABET.length) {
                return key;
            }
        }
    }

    private static String askSomething(Scanner scanner) {
        String s = scanner.nextLine();
        if ("exit".equals(s)) {
            System.exit(0);
        }
        return s;
    }
}

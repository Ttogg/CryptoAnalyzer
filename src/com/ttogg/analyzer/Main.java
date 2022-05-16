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
    private static final String ORIGINAL_FILE = "Enter original file (*.txt):";
    private static final String DESTINATION_FILE = "Enter destination file (*.txt):";
    private static final String ADDITIONAL_FILE = "Enter example file (*.txt):";
    private static final String FILE_IS_EXIST = "File exists. Do you want to rewrite it? (y/n)";
    private static final String FILE_NOT_EXIST = "File not exists";
    private static final String MODE = "Enter mode (1 - Encryption; 2 - Decryption; 3 - BruteForce):";
    private static final String KEY = "Enter key 0.." + (ALPHABET.length - 1) + ":";


    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
/*
 *       В данный момент взаимодействие с пользователем происходит через консоль, это методы:
 *       askKey(), askFile(), askMode(), askSomething().
 *       Планирую перенести их в другой класс.
 */

            int mode = askMode(scanner);
            int key = 0;

//            Это здесь для удобства моего тестирования
//
//            D:\Java\Projects\Files\OriginalFile.txt
//            D:\Java\Projects\Files\EncryptedFile.txt
//            D:\Java\Projects\Files\DecryptedFile.txt
//            D:\Java\Projects\Files\BruteForceFile.txt
//            final Path ORIGINAL = Path.of("D:\\Java\\Projects\\Files\\OriginalFile.txt");
//            final Path ENCRYPTED = Path.of("D:\\Java\\Projects\\Files\\EncryptedFile.txt");
//            final Path DECRYPTED = Path.of("D:\\Java\\Projects\\Files\\DecryptedFile.txt");
//            final Path BRUTE = Path.of("D:\\Java\\Projects\\Files\\BruteForceFile.txt");
//
//
//            switch (mode) {
//                case 1:
//                    key = askKey(scanner);
//                    converting(ORIGINAL, ENCRYPTED, key);
//                    break;
//                case 2:
//                    key = -askKey(scanner);
//                    converting(ENCRYPTED, DECRYPTED, key);
//                    break;
//                case 3:
//                    key = bruteForce(ENCRYPTED);
//                    converting(ENCRYPTED, BRUTE, key);
//                    break;
//                case 4:
//                    System.out.println("Not implemented yet");
//            }



            Path originalFile = askFile(scanner, ORIGINAL_FILE, true);
            Path destinationFile = askFile(scanner, DESTINATION_FILE, false);

            switch (mode) {
                case 1:
                    key = askKey(scanner);
                    break;
                case 2:
                    key = -askKey(scanner);
                    break;
                case 3:
                    key = bruteForce(originalFile);
                    break;
                case 4:
                    System.out.println("Not implemented yet");
            }

            converting(originalFile, destinationFile, key);
        }
    }

/*
 *   Проверка для моего тестового файла, пока не реализовано
 * */
    private static boolean checkKey(char[] chars, int key) {
        if (newChar(chars[2], key) == 'д') {
            return true;
        } else {
            return false;
        }
    }

/*
 *   Метод пока не реализован
 * */
    private static int bruteForce(Path originalFile) {
        int key = 0;
        int charCount;
/*
 *       Как вариант для хранения части файла для обработки кроме массива, либо создания временных файлов
 *       ничего не придумал (есть еще ByteStream, CharStream, но есть ли смысл их использовать
 *       не знаю, и не совсем понимаю как ими пользоваться)
 *       Массив ниже имеет размер 2*10_000_000 = 20 Мбайт, если не ошибаюсь, что не много.
 *       При этом не уверен, что размера достаточно для анализа правильности подбора ключа.
 * */
        char[] chars = new char[10_000_000];

        try (BufferedReader reader = Files.newBufferedReader(originalFile, StandardCharsets.UTF_8)) {
            for (key = 0; key < ALPHABET.length; key++) {
                charCount = 0;
                while (reader.ready() && charCount < chars.length) {
                    chars[charCount++] = (char) reader.read();
                }
                if (checkKey(chars, -key)) {
                    return -key;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return key;                     // Если не нашло ключ - создает копию оригинального файла
    }

    private static void converting(Path originalFile, Path destinationFile, int key) {
/*
 *       Здесь и далее используется конструкция:
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static char newChar(char odlChar, int key) {
        int newCharNumber;
        boolean isInAlphabet = false;
/*
 *       А есть ли другой вариант проверки наличия элемента в алфавите? Не вижу смысла char[] во что-то преобразовывать.
 *       Была идея преобразовать char[] в TreeSet, но будет ли это быстрее работать для этого размера алфавита,
 *       либо есть другие причины использовать TreeSet, или другой подход?
 * */
        for (newCharNumber = 0; newCharNumber < ALPHABET.length; newCharNumber++) {
            if (ALPHABET[newCharNumber] == odlChar) {
                isInAlphabet = true;
                break;
            }
        }

        if (!isInAlphabet) {
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

    private static Path askFile(Scanner scanner, String fileType, boolean isSource) {
        String path;
        while (true) {
            System.out.println(fileType);
            path = askSomething(scanner);
            Path file = new File(path).toPath();

            boolean ends = path.endsWith(".txt");
            if (!ends) {
                continue;
            }
            if (isSource) {
                if (Files.isRegularFile(file)) {
                    return file;
                } else {
                    System.out.println(FILE_NOT_EXIST);
                }
            } else {
                if (Files.exists(file) && Files.isRegularFile(file)) {
                    System.out.println(FILE_IS_EXIST);
                    if (askSomething(scanner).equals("y")) {
                        return file;
                    }
                } else {
                    if (Files.isDirectory(file.getParent())) {
                        return file;
                    }
                }
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

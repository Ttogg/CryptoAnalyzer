package com.ttogg.analyzer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;


public class Main {
    private static final char[] ALPHABET =
            {'а', 'б', 'в', 'г', 'д', 'е', 'ё', 'ж', 'з', 'и', 'й', 'к', 'л', 'м', 'н', 'о', 'п',
                    'р', 'с', 'т', 'у', 'ф', 'х', 'ц', 'ч', 'ш', 'щ', 'ъ', 'ы', 'ь', 'э', 'ю', 'я',
                    '.', ',', '"', ':', '-', '!', '?', ' '};
    private static final String ORIGINAL_FILE = "Enter original file:";
    private static final String DESTINATION_FILE = "Enter destination file (*.txt):";
    private static final String ADDITIONAL_FILE = "Enter example file:";
    private static final String MODE = "Enter mode (1 - Encryption; 2 - Decryption; 3 - BruteForce):";
    private static final String KEY = "Enter key 0.." + (ALPHABET.length - 1) + ":";


    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {

            int mode = askMode(scanner);

//            Path originalFile = askFile(scanner, ORIGINAL_FILE);
//            Path destinationFile = askFile(scanner, DESTINATION_FILE);

            final Path ORIGINAL = Path.of("D:\\Java\\Projects\\Files\\OriginalFile.txt");
            final Path ENCRYPTED = Path.of("D:\\Java\\Projects\\Files\\EncryptedFile.txt");
            final Path DECRYPTED = Path.of("D:\\Java\\Projects\\Files\\DecryptedFile.txt");
            final Path BRUTE = Path.of("D:\\Java\\Projects\\Files\\BruteForceFile.txt");

            int key = 0;

            switch (mode) {
                case 1:
                    key = askKey(scanner);
                    converting(ORIGINAL, ENCRYPTED, key);
                    break;
                case 2:
                    key = -askKey(scanner);
                    converting(ENCRYPTED, DECRYPTED, key);
                    break;
                case 3:
                    key = bruteForce(ENCRYPTED);
                    converting(ENCRYPTED, BRUTE, key);
                    break;
                case 4:
                    System.out.println("Not implemented yet");
            }

//            converting(originalFile, destinationFile, key);
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

    private static int bruteForce(Path originalFile) {
        int key = 0;
        int charCount;
/*
*       Как вариант для хранения части файла для обработки кроме массива, либо создания временных файлов
*       ничего не придумал (есть еще ByteStream, CharStream, но есть ли смысл их использовать
*       не знаю, и не совсем понимаю как ими пользоваться)
*/
        char[] chars = new char[10_000_000];
/*
*       Здесь и далее используется конструкция:
*       try (BufferedReader reader = Files.newBufferedReader(originalFile, StandardCharsets.UTF_8)) {
*           ...
*       } catch (IOException e) {
*           e.printStackTrace();
*       }
*       Не уверен, что это грамотное решение.
* */
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
        return key;                     // Дописать если не нашло
    }

    private static void converting(Path originalFile, Path destinationFile, int key) {
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
        А есть ли другой вариант проверки наличия элемента в алфавите? Не вижу смысла char[] во что-то преобразовывать
*/
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

//    Написать проверки на существование файлов
    private static Path askFile(Scanner scanner, String fileType) {
        String path;
        while (true) {
            System.out.println(fileType);
            path = askSomething(scanner);
            Path file = new File(path).toPath();
            //Дописать проверку для конечного файла
            if (Files.isRegularFile(file)) {
                return file;
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

    private static final String askSomething(Scanner scanner) {
        String s = scanner.nextLine();
        if ("exit".equals(s)) {
            System.exit(0);
        }
        return s;
    }

}

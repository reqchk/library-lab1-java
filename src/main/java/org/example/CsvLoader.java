package org.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public final class CsvLoader {

    public static final String CSV_HEADER = "Type;ISBN;Title;Authors;Year;Genre;OnHands;Course;PrintRun";
    public static final int EXPECTED_COLUMNS = 9;

    public static final String TYPE_BOOK = "BOOK";
    public static final String TYPE_TEXTBOOK = "TEXTBOOK";
    public static final String TYPE_ANTIQUARIAN = "ANTIQUARIAN";

    /** Загружает книги из CSV. Битые строки пропускаются. */
    public List<BookAll> load(Path file) throws IOException {
        List<BookAll> books = new ArrayList<>();
        Set<String> usedIsbns = new HashSet<>();

        try (var reader = Files.newBufferedReader(file)) {
            reader.readLine(); // строка заголовка

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                try {
                    BookAll book = parseLine(line);

                    // дубликат ISBN - битая строка, пропускаем
                    if (!usedIsbns.add(book.getISBN())) {
                        continue;
                    }
                    books.add(book);
                } catch (IllegalArgumentException e) {
                    // битая строка - пропускаем
                }
            }
        }
        return books;
    }

    private BookAll parseLine(String line) {
        String[] parts = line.split(";", -1);
        if (parts.length != EXPECTED_COLUMNS) {
            throw new IllegalArgumentException("Неверное число столбцов");
        }

        String type = parts[0].trim();
        String isbn = parts[1].trim();
        String title = parts[2].trim();
        List<String> authors = parseAuthors(parts[3]);
        int year = Integer.parseInt(parts[4].trim());
        String genre = parts[5].trim();
        boolean onHands = parseOnHands(parts[6].trim());

        BookAll book = switch (type) {
            case TYPE_BOOK -> new Book(isbn, title, authors, year, genre, onHands);
            case TYPE_ANTIQUARIAN -> new AntiquarianBook(isbn, title, authors, year, genre, onHands);
            case TYPE_TEXTBOOK -> new TextBook(isbn, title, authors, year, genre, onHands,
                    Integer.parseInt(parts[7].trim()), Integer.parseInt(parts[8].trim()));
            default -> throw new IllegalArgumentException("Неизвестный тип записи: " + type);
        };

        if (!book.validate().isEmpty()) {
            throw new IllegalArgumentException("Строка не прошла валидацию");
        }
        return book;
    }

    private static List<String> parseAuthors(String raw) {
        if (raw.trim().isEmpty()) {
            return List.of();
        }
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .toList();
    }

    private static boolean parseOnHands(String value) {
        if (value.equalsIgnoreCase("true")) return true;
        if (value.equalsIgnoreCase("false")) return false;
        throw new IllegalArgumentException("Ожидалось true/false, получено: '" + value + "'");
    }

    public void save(Path file, List<BookAll> books) throws IOException {
        try (var writer = Files.newBufferedWriter(file)) {
            writer.write(CSV_HEADER);
            writer.newLine();
            for (BookAll b : books) {
                writer.write(toCsvLine(b));
                writer.newLine();
            }
        }
    }

    private static String toCsvLine(BookAll b) {
        String type;
        String extraFields;
        if (b instanceof AntiquarianBook) {
            type = TYPE_ANTIQUARIAN;
            extraFields = ";;";
        } else if (b instanceof TextBook tb) {
            type = TYPE_TEXTBOOK;
            extraFields = ";" + tb.getCourse() + ";" + tb.getPrintRun();
        } else {
            type = TYPE_BOOK;
            extraFields = ";;";
        }
        String authors = String.join(",", b.getAuthors());
        return String.join(";",
                type, b.getISBN(), b.getTitle(), authors,
                String.valueOf(b.getYear()), b.getGenre(), String.valueOf(b.isOnHands()))
                + extraFields;
    }
}
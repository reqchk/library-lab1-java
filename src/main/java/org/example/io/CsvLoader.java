package org.example.io;

import org.example.model.BookValidationException;
import org.example.model.AntiqueBook;
import org.example.model.Book;
import org.example.model.OrdinaryBook;
import org.example.model.TextBook;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CsvLoader {
    private final List<String> loadErrors = new ArrayList<>();

    /**
     * Загружает книги из CSV. Битые строки пропускаются, ошибки сохраняются.
     * @return Список успешно загруженных книг.
     */
    public List<Book> loadFromFile(String filePath) {
        loadErrors.clear();
        List<Book> books = new ArrayList<>();

        String expectedHeader = "TYPE;ISBN;TITLE;AUTHORS;YEAR;GENRE;ON_HANDS;EXTRA1;EXTRA2";

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String header = br.readLine();

            try {
                if (header == null || !header.trim().equals(expectedHeader)) {
                    throw new CsvParseException(CsvParseException.ErrorCode.INVALID_HEADER, 1,
                            "Ожидался заголовок: " + expectedHeader);
                }
            } catch (CsvParseException e) {
                loadErrors.add(e.getFormattedMessage());
                return books;
            }

            int lineNumber = 1;
            String line;
            while ((line = br.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) continue;

                try {
                    Book book = parseLine(line, lineNumber);
                    if (book != null) {
                        books.add(book);
                    }
                } catch (CsvParseException e) {
                    loadErrors.add(e.getFormattedMessage());
                }
            }
        } catch (IOException e) {
            loadErrors.add("Критическая ошибка чтения файла: " + e.getMessage());
        }
        return books;
    }

    public List<String> getLoadErrors() {
        return loadErrors;
    }

    /**
     * Парсит одну строку CSV, создает соответствующий объект книги и проводит его валидацию.
     * @return Созданный и успешно валидированный объект книги.
     */
    private Book parseLine(String line, int lineNumber) throws CsvParseException {
        String[] parts = line.split(";");

        if (parts.length < 7) {
            throw new CsvParseException(CsvParseException.ErrorCode.WRONG_FIELD_COUNT, lineNumber,
                    "Ожидается минимум 7 полей, получено " + parts.length);
        }

        String type = parts[0].trim().toUpperCase();
        String isbn = parts[1].trim();
        String title = parts[2].trim();
        String authors = parts[3].trim();
        String genre = parts[5].trim();

        if (isbn.isEmpty()) {
            throw new CsvParseException(CsvParseException.ErrorCode.EMPTY_FIELD, lineNumber,
                    "Поле 'ISBN' не может быть пустым");
        }
        if (title.isEmpty()) {
            throw new CsvParseException(CsvParseException.ErrorCode.EMPTY_FIELD, lineNumber,
                    "Поле 'Название' не может быть пустым");
        }
        if (authors.isEmpty()) {
            throw new CsvParseException(CsvParseException.ErrorCode.EMPTY_FIELD, lineNumber,
                    "Поле 'Авторы' не может быть пустым");
        }
        if (genre.isEmpty()) {
            throw new CsvParseException(CsvParseException.ErrorCode.EMPTY_FIELD, lineNumber,
                    "Поле 'Жанр' не может быть пустым");
        }

        int year;
        try {
            year = Integer.parseInt(parts[4].trim());
        } catch (NumberFormatException e) {
            throw new CsvParseException(CsvParseException.ErrorCode.BAD_NUMBER, lineNumber,
                    "Год должен быть числом, получено: '" + parts[4].trim() + "'");
        }

        boolean isOnHands = Boolean.parseBoolean(parts[6].trim());

        Book book;
        switch (type) {
            case "BOOK":
                book = new OrdinaryBook(isbn, title, authors, year, genre, isOnHands);
                break;

            case "TEXTBOOK":
                if (parts.length < 9) {
                    throw new CsvParseException(CsvParseException.ErrorCode.WRONG_FIELD_COUNT, lineNumber,
                            "Для учебника требуется 9 полей, получено " + parts.length);
                }
                int course, printRun;
                try {
                    course = Integer.parseInt(parts[7].trim());
                    printRun = Integer.parseInt(parts[8].trim());
                } catch (NumberFormatException e) {
                    throw new CsvParseException(CsvParseException.ErrorCode.BAD_NUMBER, lineNumber,
                            "Курс и тираж должны быть числами");
                }
                book = new TextBook(isbn, title, authors, year, genre, isOnHands, course, printRun);
                break;

            case "ANTIQUE":
                book = new AntiqueBook(isbn, title, authors, year, genre, isOnHands);
                break;

            default:
                throw new CsvParseException(CsvParseException.ErrorCode.UNKNOWN_TYPE, lineNumber,
                        "Неизвестный тип книги: '" + type + "'");
        }

        try {
            book.validate();
        } catch (BookValidationException e) {
            CsvParseException.ErrorCode code = switch (e.getErrorCode()) {
                case EMPTY_FIELD -> CsvParseException.ErrorCode.EMPTY_FIELD;
                case INVALID_FIELD -> CsvParseException.ErrorCode.INVALID_FIELD;
                default -> CsvParseException.ErrorCode.BAD_NUMBER;
            };
            throw new CsvParseException(code, lineNumber, e.getMessage());
        }

        return book;
    }

    /**
     * Сохраняет список книг в CSV-файл по указанному пути.
     * Автоматически записывает заголовок и форматирует данные в зависимости от типа книги.
     */
    public void saveToFile(String filePath, List<Book> books) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            writer.println("TYPE;ISBN;TITLE;AUTHORS;YEAR;GENRE;ON_HANDS;EXTRA1;EXTRA2");
            for (Book book : books) {
                if (book instanceof TextBook t) {
                    writer.printf("TEXTBOOK;%s;%s;%s;%d;%s;%b;%d;%d%n",
                            t.getIsbn(), t.getTitle(), t.getAuthors(), t.getYear(), t.getGenre(), t.isOnHands(), t.getCourse(), t.getPrintRun());
                } else if (book instanceof AntiqueBook a) {
                    writer.printf("ANTIQUE;%s;%s;%s;%d;%s;%b;;%n",
                            a.getIsbn(), a.getTitle(), a.getAuthors(), a.getYear(), a.getGenre(), a.isOnHands());
                } else {
                    writer.printf("BOOK;%s;%s;%s;%d;%s;%b;;%n",
                            book.getIsbn(), book.getTitle(), book.getAuthors(), book.getYear(), book.getGenre(), book.isOnHands());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Ошибка записи файла: " + e.getMessage());
        }
    }
}
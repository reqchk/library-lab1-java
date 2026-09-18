package org.example;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;

public interface BookAll {
    String getIsbn();
    String getTitle();
    List<String> getAuthors();
    int getYear();
    String getGenre();
    boolean isOnHands();

    List<String> validate();

    /** Общие проверки полей, одинаковых для всех типов книг. */
    static List<String> validateBase(BookAll book) {
        List<String> errors = new ArrayList<>();
        int currentYear = Year.now().getValue();

        if (book.getYear() < 0 || book.getYear() > currentYear) {
            errors.add("Некорректный год издания.");
        }
        if (book.getTitle() == null || book.getTitle().trim().isEmpty()) {
            errors.add("Отсутствует название книги.");
        }
        if (book.getAuthors() == null || book.getAuthors().isEmpty()) {
            errors.add("У книги должен быть хотя бы один автор.");
        } else if (book.getAuthors().stream().anyMatch(a -> a.matches(".*\\d.*"))) {
            errors.add("Имя автора не может содержать цифры.");
        }
        if (book.getIsbn() == null || book.getIsbn().trim().isEmpty()) {
            errors.add("Отсутствует ISBN книги.");
        } else if (!book.getIsbn().matches("[\\d\\-X]+")) {
            errors.add("Некорректный формат ISBN. Допускаются только цифры, дефисы и символ 'X'.");
        }
        if (book.getGenre() == null || book.getGenre().trim().isEmpty()) {
            errors.add("Отсутствует жанр книги.");
        }
        return errors;
    }
}

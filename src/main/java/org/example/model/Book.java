package org.example.model;

import java.time.Year;

public class Book {
    protected String isbn;
    protected String title;
    protected String authors;
    protected int year;
    protected String genre;
    protected boolean onHands;

    public Book(String isbn, String title, String authors, int year, String genre, boolean onHands) {
        this.isbn = isbn;
        this.title = title;
        this.authors = authors;
        this.year = year;
        this.genre = genre;
        this.onHands = onHands;
    }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthors() { return authors; }
    public void setAuthors(String authors) { this.authors = authors; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public boolean isOnHands() { return onHands; }
    public void setOnHands(boolean onHands) { this.onHands = onHands; }

    public void validate() throws BookValidationException {
        if (isbn == null || isbn.trim().isEmpty()) {
            throw new BookValidationException(BookValidationException.ErrorCode.EMPTY_FIELD,
                    "Поле 'ISBN' не может быть пустым");
        }
        if (!isbn.trim().matches("[0-9X\\-]+")) {
            throw new BookValidationException(BookValidationException.ErrorCode.INVALID_FIELD,
                    "Поле 'ISBN' должно содержать только цифры, дефисы и символ 'X'");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new BookValidationException(BookValidationException.ErrorCode.EMPTY_FIELD,
                    "Поле 'Название' не может быть пустым");
        }
        if (authors == null || authors.trim().isEmpty()) {
            throw new BookValidationException(BookValidationException.ErrorCode.EMPTY_FIELD,
                    "Поле 'Авторы' не может быть пустым");
        }
        if (genre == null || genre.trim().isEmpty()) {
            throw new BookValidationException(BookValidationException.ErrorCode.EMPTY_FIELD,
                    "Поле 'Жанр' не может быть пустым");
        }
        if (year < 1 || year > Year.now().getValue()) {
            throw new BookValidationException(BookValidationException.ErrorCode.BAD_NUMBER,
                    "Год должен быть в диапазоне от 0 до " + Year.now().getValue());
        }
    }
}

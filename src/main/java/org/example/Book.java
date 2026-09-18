package org.example;
import java.util.List;


public class Book implements Editable {
    private String ISBN;
    private String title;
    private List<String> authors;
    private int year;
    private String genre;
    private boolean onHands;

    public Book(String ISBN, String title, List<String> authors, int year, String genre, boolean onHands){
        this.ISBN = ISBN;
        this.title = title;
        this.authors = authors;
        this.year = year;
        this.genre = genre;
        this.onHands = onHands;
    }

    public void setISBN(String ISBN) { this.ISBN = ISBN; }
    @Override public String getISBN() { return ISBN; }

    public void setTitle(String title) { this.title = title; }
    @Override public String getTitle() { return title; }

    public void setAuthors(List<String> authors) {  this.authors = authors; }
    @Override public List<String> getAuthors() { return authors; }

    public void setYear(int year) { this.year = year; }
    @Override public int getYear() { return year; }

    public void setGenre(String genre) { this.genre = genre; }
    @Override public String getGenre() { return genre; }

    public void setOnHands(boolean onHands) { this.onHands = onHands; }
    @Override public boolean isOnHands() { return onHands; }

    @Override
    public String toString() {
        return "Книга " + title + "\nГод: " + year + "\nЖанр: " + genre + "\nАвторы: " + authors + "\nISBN: " + ISBN +
                "\nНа руках: " + (onHands ? "да" : "нет");
    }

    @Override
    public List<String> validate() {
        return BookAll.validateBase(this);
    }

    /** Копирует все поля из другой книги при редактировании в GUI. */
    public void applyFrom(Book source) {
        this.ISBN = source.ISBN;
        this.title = source.title;
        this.authors = source.authors;
        this.year = source.year;
        this.genre = source.genre;
        this.onHands = source.onHands;
    }
}
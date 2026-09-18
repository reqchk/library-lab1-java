package org.example;

import java.util.List;

public record AntiquarianBook (
        String ISBN,
        String title,
        List<String> authors,
        int year,
        String genre,
        boolean onHands
) implements BookAll {

    public AntiquarianBook {
        authors = List.copyOf(authors);
    }

    @Override public String getIsbn() { return ISBN; }
    @Override public String getTitle() { return title; }
    @Override public List<String> getAuthors() { return authors; }
    @Override public int getYear() { return year; }
    @Override public String getGenre() { return genre; }
    @Override public boolean isOnHands() { return onHands; }

    @Override
    public List<String> validate() {
        return BookAll.validateBase(this);
    }

    @Override
    public String toString() {
        return "Антикварная книга " + title + "\nГод: " + year + "\nЖанр: " + genre +
                "\nАвторы: " + authors + "\nISBN: " + ISBN + "\nНа руках: " + (onHands ? "да" : "нет");
    }

}


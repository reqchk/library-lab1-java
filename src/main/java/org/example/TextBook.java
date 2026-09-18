package org.example;

import java.util.ArrayList;
import java.util.List;


public class TextBook extends Book {
    public static final int MIN_COURSE = 1;
    public static final int MAX_COURSE = 6;
    public static final int MIN_PRINT_RUN = 1;
    private int course;
    private int printRun;

    public TextBook(String ISBN, String title, List<String> authors, int year, String genre, boolean onHands, int course, int printRun) {
        super(ISBN, title, authors, year, genre, onHands);
        this.course = course;
        this.printRun = printRun;
    }

    public void setCourse(int course) { this.course = course; }
    public int getCourse() { return course; }

    public void setPrintRun(int printRun) { this.printRun = printRun; }
    public int getPrintRun() { return printRun; }

    @Override
    public String toString() {
        return "Учебник " + getTitle() + "\nГод: " + getYear() + "\nЖанр: " + getGenre() + "\nАвторы: " + getAuthors() +
                "\nISBN: " + getISBN() + "\nКурс: " + getCourse() + "\nТираж: " + getPrintRun() +
                "\nНа руках: " + (isOnHands() ? "да" : "нет");
    }

    @Override
    public List<String> validate() {
        List<String> errors = new ArrayList<>(super.validate());

        if (course < MIN_COURSE || course > MAX_COURSE) {
            errors.add("Некорректный курс обучения.");
        }
        if (printRun < MIN_PRINT_RUN) {
            errors.add("Тираж должен быть положительным значением.");
        }
        return errors;
    }
    @Override
    public void applyFrom(Book source) {
        super.applyFrom(source);
        if (source instanceof TextBook tb) {
            this.course = tb.course;
            this.printRun = tb.printRun;
        }
    }

}

package org.example.model;

public class OrdinaryBook extends Book implements Editable {
    public OrdinaryBook(String isbn, String title, String authors, int year, String genre, boolean onHands) {
        super(isbn, title, authors, year, genre, onHands);
    }

    @Override
    public void validate() throws BookValidationException {
        super.validate();
    }
}

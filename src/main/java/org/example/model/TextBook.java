package org.example.model;

public class TextBook extends OrdinaryBook{
    private int course;  // курс
    private int printRun; // тираж

    public TextBook(String isbn, String title, String authors, int year, String genre, boolean onHands, int course, int printRun) {
        super(isbn, title, authors, year, genre, onHands);
        this.course = course;
        this.printRun = printRun;
    }

    public int getCourse() { return course; }
    public void setCourse(int course) { this.course = course; }

    public int getPrintRun() { return printRun; }
    public void setPrintRun(int printRun) { this.printRun = printRun; }

    @Override
    public void validate() throws BookValidationException {
        super.validate();

        if (course < 1 || course > 6) {
            throw new BookValidationException(BookValidationException.ErrorCode.BAD_NUMBER,
                    "Курс должен быть от 1 до 6");
        }
        if (printRun <= 0) {
            throw new BookValidationException(BookValidationException.ErrorCode.BAD_NUMBER,
                    "Тираж должен быть больше нуля");
        }
    }
}

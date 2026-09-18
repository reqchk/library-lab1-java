package org.example;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Единая форма книги для «Добавить» и «Изменить».
 * initial == null → добавление, иначе — редактирование существующей книги.
 */
public class BookDialog {

    public static final String LABEL_BOOK = "Книга";
    public static final String LABEL_TEXTBOOK = "Учебник";

    private static final String ERR_YEAR = "YEAR";
    private static final String ERR_COURSE = "COURSE";
    private static final String ERR_PRINT_RUN = "PRINT_RUN";

    private final Dialog<ButtonType> dialog;
    private final ObservableList<BookAll> books; // для проверки уникальности ISBN
    private final BookAll initial;               // null → режим добавления

    private ComboBox<String> typeBox;
    private TextField titleField;
    private TextField isbnField;
    private TextField genreField;
    private TextField authorsField;
    private TextField yearField;
    private TextField courseField;
    private TextField printRunField;
    private CheckBox onHandsCheckBox;

    private Book result;

    private BookDialog(String title, BookAll initial, ObservableList<BookAll> books) {
        this.initial = initial;
        this.books = books;
        this.dialog = buildDialog(title);
    }

    public static BookDialog forAdd(ObservableList<BookAll> books) {
        return new BookDialog("Добавление книги", null, books);
    }

    public static BookDialog forEdit(Book book, ObservableList<BookAll> books) {
        return new BookDialog("Редактирование книги", book, books);
    }

    /** @return готовый объект книги при подтверждении, empty при отмене. */
    public Optional<Book> showAndWait() {
        dialog.showAndWait();
        return Optional.ofNullable(result);
    }

    private Dialog<ButtonType> buildDialog(String title) {
        Dialog<ButtonType> d = new Dialog<>();
        d.setTitle(title);
        d.setHeaderText(initial == null ? "Введите параметры новой книги" : "Изменение параметров книги");

        ButtonType okType = new ButtonType(initial == null ? "Добавить" : "Сохранить",
                ButtonBar.ButtonData.OK_DONE);
        d.getDialogPane().getButtonTypes().addAll(okType,
                new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE));

        boolean isTextBook = initial instanceof TextBook;

        typeBox = new ComboBox<>();
        typeBox.getItems().addAll(LABEL_BOOK, LABEL_TEXTBOOK);
        typeBox.setValue(isTextBook ? LABEL_TEXTBOOK : LABEL_BOOK);
        typeBox.setDisable(initial != null);
        typeBox.setPrefWidth(350);

        titleField = new TextField(initial == null ? "" : initial.getTitle());
        titleField.setPromptText("Например: Общий курс физики. Том 1. Механика");

        isbnField = new TextField(initial == null ? "" : initial.getISBN());
        isbnField.setPromptText("Например: 12-345-67");

        genreField = new TextField(initial == null ? "" : initial.getGenre());
        genreField.setPromptText("Например: Учебная литература");

        authorsField = new TextField(initial == null ? "" : String.join(", ", initial.getAuthors()));
        authorsField.setPromptText("Например: Сивухин Д.");
        Label authorsHint = new Label("(Если авторов несколько, то перечислите их через запятую)");
        authorsHint.setStyle("-fx-text-fill: gray; -fx-font-size: 11px;");

        yearField = new TextField(initial == null ? "" : String.valueOf(initial.getYear()));
        yearField.setPromptText("Например: 2014");

        courseField = new TextField(initial instanceof TextBook tb ? String.valueOf(tb.getCourse()) : "");
        courseField.setPromptText("Например: 1");
        courseField.setDisable(!isTextBook);

        printRunField = new TextField(initial instanceof TextBook tb ? String.valueOf(tb.getPrintRun()) : "");
        printRunField.setPromptText("Например: 1500");
        printRunField.setDisable(!isTextBook);

        onHandsCheckBox = new CheckBox("Книга на руках");
        onHandsCheckBox.setSelected(initial != null && initial.isOnHands());

        typeBox.valueProperty().addListener((obs, oldV, newV) -> {
            boolean tb = LABEL_TEXTBOOK.equals(newV);
            courseField.setDisable(!tb);
            printRunField.setDisable(!tb);
        });

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);
        grid.setPadding(new Insets(20, 30, 10, 10));

        addRow(grid, 0, "Тип", typeBox);
        addRow(grid, 1, "Название", titleField);
        addRow(grid, 2, "ISBN", isbnField);
        addRow(grid, 3, "Жанр", genreField);
        addRow(grid, 4, "Авторы", new VBox(2, authorsField, authorsHint));
        addRow(grid, 5, "Год издания", yearField);

        Label forTextBook = new Label("Параметры для типа \"Учебник\":");
        forTextBook.setStyle("-fx-font-size: 15px;");
        grid.add(forTextBook, 0, 6);

        addRow(grid, 7, "Курс", courseField);
        addRow(grid, 8, "Тираж", printRunField);
        grid.add(onHandsCheckBox, 0, 9);

        d.getDialogPane().setContent(grid);

        Button okButton = (Button) d.getDialogPane().lookupButton(okType);
        okButton.disableProperty().bind(createEmptyFieldsBinding());
        okButton.addEventFilter(ActionEvent.ACTION, this::onOk);

        return d;
    }

    private static void addRow(GridPane grid, int row, String labelText, Node field) {
        grid.add(new Label(labelText), 0, row);
        grid.add(field, 1, row);
    }

    private void onOk(ActionEvent event) {
        try {
            Book candidate = buildBook();

            BookAll duplicate = findDuplicateByIsbn(candidate.getISBN());
            if (duplicate != null && duplicate != initial) {
                errorDialog("Ошибка уникальности данных",
                        "Книга с ISBN '" + candidate.getISBN() + "' уже существует в каталоге библиотеки.");
                event.consume();
                return;
            }

            List<String> errors = candidate.validate();
            if (!errors.isEmpty()) {
                errorDialog("Ошибка валидации данных", String.join("\n", errors));
                event.consume();
                return;
            }

            result = candidate;
        } catch (NumberFormatException ex) {
            event.consume();
            showNumberError(ex.getMessage());
        }
    }

    private Book buildBook() {
        String isbn = isbnField.getText().trim();
        String title = titleField.getText().trim();
        String genre = genreField.getText().trim();
        List<String> authors = parseAuthors();
        int year = parseIntOr(yearField, ERR_YEAR);
        boolean onHands = onHandsCheckBox.isSelected();

        if (isTextBookSelected()) {
            int course = parseIntOr(courseField, ERR_COURSE);
            int printRun = parseIntOr(printRunField, ERR_PRINT_RUN);
            return new TextBook(isbn, title, authors, year, genre, onHands, course, printRun);
        }
        return new Book(isbn, title, authors, year, genre, onHands);
    }

    private List<String> parseAuthors() {
        return Arrays.stream(authorsField.getText().split(","))
                .map(String::trim)
                .filter(a -> !a.isEmpty())
                .toList();
    }

    /** Парсит число, при неудаче бросает NFE с маркером поля для showNumberError. */
    private static int parseIntOr(TextField field, String marker) {
        try {
            return Integer.parseInt(field.getText().trim());
        } catch (NumberFormatException e) {
            throw new NumberFormatException(marker);
        }
    }

    private BookAll findDuplicateByIsbn(String isbn) {
        return books.stream()
                .filter(b -> b.getISBN().equalsIgnoreCase(isbn))
                .findFirst()
                .orElse(null);
    }


    private BooleanBinding createEmptyFieldsBinding() {
        return Bindings.createBooleanBinding(() -> {
                    boolean baseEmpty = isBlank(titleField) || isBlank(isbnField) || isBlank(genreField)
                            || isBlank(authorsField) || isBlank(yearField);
                    boolean extraEmpty = isTextBookSelected()
                            && (isBlank(courseField) || isBlank(printRunField));
                    return baseEmpty || extraEmpty;
                },
                typeBox.valueProperty(), titleField.textProperty(), isbnField.textProperty(),
                genreField.textProperty(), authorsField.textProperty(), yearField.textProperty(),
                courseField.textProperty(), printRunField.textProperty());
    }

    private static boolean isBlank(TextField field) {
        return field.getText().trim().isEmpty();
    }

    private boolean isTextBookSelected() {
        return LABEL_TEXTBOOK.equals(typeBox.getValue());
    }

    private void showNumberError(String marker) {
        switch (marker) {
            case ERR_YEAR -> errorDialog("Ошибка ввода: Год издания",
                    "Поле 'Год издания' должно содержать только целые числа.\nПример: 2014");
            case ERR_COURSE -> errorDialog("Ошибка ввода: Рекомендуемый курс",
                    "Поле 'Курс' должно содержать только целое число (от "
                            + TextBook.MIN_COURSE + " до " + TextBook.MAX_COURSE + ").");
            case ERR_PRINT_RUN -> errorDialog("Ошибка ввода: Учетный тираж",
                    "Поле 'Тираж' должно содержать только целое положительное число.");
            case null, default -> errorDialog("Ошибка числового формата",
                    "Пожалуйста, проверьте корректность ввода числовых данных.");
        }
    }

    private void errorDialog(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
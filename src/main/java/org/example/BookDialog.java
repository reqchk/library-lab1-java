package org.example;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
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

        isbnField = new TextField(initial == null ? "" : initial.getIsbn());
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
        okButton.addEventFilter(ActionEvent.ACTION, this::onOk);

        return d;
    }

    private static void addRow(GridPane grid, int row, String labelText, Node field) {
        grid.add(new Label(labelText), 0, row);
        grid.add(field, 1, row);
    }

    private void onOk(ActionEvent event) {
        List<String> errors = new ArrayList<>();

        collectFormErrors(errors);

        Book candidate = null;
        if (errors.isEmpty()) {
            candidate = buildBook();

            BookAll duplicate = findDuplicateByIsbn(candidate.getIsbn());
            if (duplicate != null && duplicate != initial) {
                errors.add("Книга с ISBN '" + candidate.getIsbn()
                        + "' уже существует в каталоге библиотеки.");
            }
            errors.addAll(candidate.validate());
        }

        if (!errors.isEmpty()) {
            errorDialog("Ошибка валидации данных", String.join("\n", errors));
            event.consume();
            return;
        }

        result = candidate;
    }

    private void collectFormErrors(List<String> errors) {
        if (titleField.getText().trim().isEmpty()) errors.add("Отсутствует название книги.");
        if (isbnField.getText().trim().isEmpty()) errors.add("Отсутствует ISBN книги.");
        if (genreField.getText().trim().isEmpty()) errors.add("Отсутствует жанр книги.");
        if (authorsField.getText().trim().isEmpty()) errors.add("У книги должен быть хотя бы один автор.");

        parseNumber(yearField, "Год издания", errors);
        if (LABEL_TEXTBOOK.equals(typeBox.getValue())) {
            parseNumber(courseField, "Курс", errors);
            parseNumber(printRunField, "Тираж", errors);
        }
    }

    /** Проверяет поле: если пустое или нечисловое, то добавляет ошибку в список. */
    private static void parseNumber(TextField field, String fieldName, List<String> errors) {
        String text = field.getText().trim();
        if (text.isEmpty()) {
            errors.add("Не заполнено поле '" + fieldName + "'.");
        } else {
            try {
                Integer.parseInt(text);
            } catch (NumberFormatException e) {
                errors.add("Поле '" + fieldName + "' должно содержать целое число.");
            }
        }
    }
    private Book buildBook() {
        String isbn = isbnField.getText().trim();
        String title = titleField.getText().trim();
        String genre = genreField.getText().trim();
        List<String> authors = parseAuthors();
        int year = Integer.parseInt(yearField.getText().trim());
        boolean onHands = onHandsCheckBox.isSelected();

        if (LABEL_TEXTBOOK.equals(typeBox.getValue())) {
            int course = Integer.parseInt(courseField.getText().trim());
            int printRun = Integer.parseInt(printRunField.getText().trim());
            return new TextBook(isbn, title, authors, year, genre, onHands, course, printRun);
        }
        return new Book(isbn, title, authors, year, genre, onHands);
    }

    private List<String> parseAuthors() {
        return Arrays.stream(authorsField.getText().split(",")).map(String::trim)
                .filter(a -> !a.isEmpty()).toList();
    }

    private BookAll findDuplicateByIsbn(String isbn) {
        return books.stream().filter(b -> b.getIsbn().equalsIgnoreCase(isbn)).findFirst().orElse(null);
    }

    private void errorDialog(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
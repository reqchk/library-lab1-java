package org.example.gui;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.model.BookBST;
import org.example.model.BookValidationException;
import org.example.model.*;

import java.io.File;
import java.util.Optional;

public class View {

    private final TableView<Book> tableView = new TableView<>();

    public View() { setupTable(); }

    public Scene createScene(Runnable onLoad, Runnable onSave, Runnable onAdd, Runnable onEdit) {
        Button btnLoad = new Button("Загрузить из CSV");
        Button btnSave = new Button("Сохранить в CSV");
        Button btnAdd = new Button("Добавить");
        Button btnEdit = new Button("Изменить");

        btnEdit.disableProperty().bind(
                Bindings.createBooleanBinding(() -> {
                    Book selected = tableView.getSelectionModel().getSelectedItem();
                    return !(selected instanceof Editable);
                }, tableView.getSelectionModel().selectedItemProperty())
        );

        btnLoad.setOnAction(e -> onLoad.run());
        btnSave.setOnAction(e -> onSave.run());
        btnAdd.setOnAction(e -> onAdd.run());
        btnEdit.setOnAction(e -> onEdit.run());

        HBox buttonBox = new HBox(10, btnLoad, btnSave, btnAdd, btnEdit);
        buttonBox.setPadding(new Insets(10));
        buttonBox.setAlignment(Pos.BOTTOM_CENTER);

        VBox root = new VBox(10, tableView, buttonBox);
        root.setPadding(new Insets(10));
        VBox.setVgrow(tableView, Priority.ALWAYS);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        return new Scene(root, 900, 600);
    }

    public TableView<Book> getTableView() { return tableView; }

    private void setupTable() {
        TableColumn<Book, String> typeCol = new TableColumn<>("Тип");
        typeCol.setCellValueFactory(cellData -> {
            Book b = cellData.getValue();
            String type = (b instanceof TextBook) ? "Учебник" :
                    (b instanceof AntiqueBook) ? "Антикварная книга" : "Обычная книга";
            return new SimpleStringProperty(type);
        });

        TableColumn<Book, String> isbnCol = new TableColumn<>("ISBN");
        isbnCol.setCellValueFactory(new PropertyValueFactory<>("isbn"));

        TableColumn<Book, String> titleCol = new TableColumn<>("Название");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));

        TableColumn<Book, String> authorCol = new TableColumn<>("Авторы");
        authorCol.setCellValueFactory(new PropertyValueFactory<>("authors"));

        TableColumn<Book, Integer> yearCol = new TableColumn<>("Год");
        yearCol.setCellValueFactory(new PropertyValueFactory<>("year"));

        TableColumn<Book, String> genreCol = new TableColumn<>("Жанр");
        genreCol.setCellValueFactory(new PropertyValueFactory<>("genre"));

        TableColumn<Book, String> onHandsCol = new TableColumn<>("На руках");
        onHandsCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().isOnHands() ? "Да" : "Нет"));

        TableColumn<Book, String> courseCol = new TableColumn<>("Курс");
        courseCol.setCellValueFactory(cellData -> {
            Book b = cellData.getValue();
            return new SimpleStringProperty((b instanceof TextBook t) ? String.valueOf(t.getCourse()) : "-");
        });

        TableColumn<Book, String> printRunCol = new TableColumn<>("Тираж");
        printRunCol.setCellValueFactory(cellData -> {
            Book b = cellData.getValue();
            return new SimpleStringProperty((b instanceof TextBook t) ? String.valueOf(t.getPrintRun()) : "-");
        });

        tableView.getColumns().addAll(typeCol, isbnCol, titleCol, authorCol, yearCol, genreCol, onHandsCol, courseCol, printRunCol);
    }

    /**
     * Показывает стандартный диалог выбора файла для загрузки.
     * @return Абсолютный путь к выбранному файлу или null, если выбор отменен.
     */
    public String showOpenCsvDialog(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showOpenDialog(stage);
        return file != null ? file.getAbsolutePath() : null;
    }

    /**
     * Показывает стандартный диалог выбора пути для сохранения файла.
     * @return Абсолютный путь для сохранения или null, если выбор отменен.
     */
    public String showSaveCsvDialog(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(stage);
        return file != null ? file.getAbsolutePath() : null;
    }

    /**
     * Показывает диалог добавления новой книги.
     * @return Новый валидный объект Book или null, если диалог был отменен.
     */
    public Book showAddBookDialog(Stage stage) {
        Dialog<Book> dialog = new Dialog<>();
        dialog.setTitle("Добавление книги");
        dialog.setHeaderText("Выберите тип книги и заполните данные");
        dialog.getDialogPane().setPrefSize(450, 420);

        ButtonType addButtonType = new ButtonType("Добавить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, new ButtonType("Отменить", ButtonBar.ButtonData.CANCEL_CLOSE));

        GridPane grid = new GridPane();

        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("Обычная книга", "Учебник");
        typeCombo.setValue("Обычная книга");

        TextField isbn = new TextField(); TextField title = new TextField();
        TextField authors = new TextField(); TextField year = new TextField();
        TextField genre = new TextField(); TextField course = new TextField();
        TextField printRun = new TextField();
        CheckBox isOnHandsCheckBox = new CheckBox("На руках");

        Label courseLabel = new Label("Курс:");
        Label printRunLabel = new Label("Тираж:");

        courseLabel.setVisible(false); courseLabel.setManaged(false);
        course.setVisible(false); course.setManaged(false);
        printRunLabel.setVisible(false); printRunLabel.setManaged(false);
        printRun.setVisible(false); printRun.setManaged(false);

        typeCombo.setOnAction(e -> {
            boolean isText = "Учебник".equals(typeCombo.getValue());
            courseLabel.setVisible(isText); courseLabel.setManaged(isText);
            course.setVisible(isText); course.setManaged(isText);
            printRunLabel.setVisible(isText); printRunLabel.setManaged(isText);
            printRun.setVisible(isText); printRun.setManaged(isText);
        });

        grid.add(new Label("Тип книги:"), 0, 0); grid.add(typeCombo, 1, 0);
        grid.add(new Label("ISBN:"), 0, 1); grid.add(isbn, 1, 1);
        grid.add(new Label("Название:"), 0, 2); grid.add(title, 1, 2);
        grid.add(new Label("Авторы:"), 0, 3); grid.add(authors, 1, 3);
        grid.add(new Label("Год:"), 0, 4); grid.add(year, 1, 4);
        grid.add(new Label("Жанр:"), 0, 5); grid.add(genre, 1, 5);
        grid.add(courseLabel, 0, 6); grid.add(course, 1, 6);
        grid.add(printRunLabel, 0, 7); grid.add(printRun, 1, 7);
        grid.add(isOnHandsCheckBox, 0, 8);

        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        dialog.getDialogPane().setContent(grid);

        Button addButton = (Button) dialog.getDialogPane().lookupButton(addButtonType);
        addButton.addEventFilter(ActionEvent.ACTION, event -> {
            try {
                Book tempBook;
                int parsedYear = Integer.parseInt(year.getText().trim());
                boolean isOnHands = isOnHandsCheckBox.isSelected();

                if ("Учебник".equals(typeCombo.getValue())) {
                    int parsedCourse = Integer.parseInt(course.getText().trim());
                    int parsedPrintRun = Integer.parseInt(printRun.getText().trim());
                    tempBook = new TextBook(isbn.getText().trim(), title.getText().trim(), authors.getText().trim(),
                            parsedYear, genre.getText().trim(), isOnHands, parsedCourse, parsedPrintRun);
                } else {
                    tempBook = new OrdinaryBook(isbn.getText().trim(), title.getText().trim(), authors.getText().trim(),
                            parsedYear, genre.getText().trim(), isOnHands);
                }

                ((Editable) tempBook).validate();

            } catch (NumberFormatException e) {
                event.consume();
                showAlert(Alert.AlertType.ERROR, "Ошибка ввода", "Числовые поля должны содержать числа!");
            } catch (BookValidationException e) {
                event.consume();
                showAlert(Alert.AlertType.ERROR, "Ошибка валидации", e.getFormattedMessage());
            }
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                int parsedYear = Integer.parseInt(year.getText().trim());
                boolean isOnHands = isOnHandsCheckBox.isSelected();
                if ("Учебник".equals(typeCombo.getValue())) {
                    return new TextBook(isbn.getText().trim(), title.getText().trim(), authors.getText().trim(),
                            parsedYear, genre.getText().trim(), isOnHands, Integer.parseInt(course.getText().trim()),
                            Integer.parseInt(printRun.getText().trim()));
                } else {
                    return new OrdinaryBook(isbn.getText().trim(), title.getText().trim(), authors.getText().trim(),
                            parsedYear, genre.getText().trim(), isOnHands);
                }
            }
            return null;
        });

        Optional<Book> result = dialog.showAndWait();
        return  result.orElse(null);
    }

    /**
     * Показывает диалог редактирования выбранной книги.
     * @return true, если данные успешно сохранены; false, если диалог отменен.
     */
    public boolean showEditBookDialog(Stage stage, Book selected, BookBST bookTree, Runnable onRefresh) {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Редактирование книги");
        dialog.setHeaderText("Введите новые данные книги");
        dialog.getDialogPane().setPrefSize(450, 300);

        ButtonType saveButtonType = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, new ButtonType("Отменить", ButtonBar.ButtonData.CANCEL_CLOSE));

        GridPane grid = new GridPane();

        TextField title = new TextField(selected.getTitle());
        TextField authors = new TextField(selected.getAuthors());
        TextField year = new TextField(String.valueOf(selected.getYear()));
        TextField genre = new TextField(selected.getGenre());

        CheckBox isOnHandsCheckBox = new CheckBox("На руках");

        grid.add(new Label("Название:"), 0, 0); grid.add(title, 1, 0);
        grid.add(new Label("Авторы:"), 0, 1); grid.add(authors, 1, 1);
        grid.add(new Label("Год:"), 0, 2); grid.add(year, 1, 2);
        grid.add(new Label("Жанр:"), 0, 3); grid.add(genre, 1, 3);

        TextField course = null, printRun = null;
        if (selected instanceof TextBook tb) {
            course = new TextField(String.valueOf(tb.getCourse()));
            printRun = new TextField(String.valueOf(tb.getPrintRun()));
            grid.add(new Label("Курс:"), 0, 4); grid.add(course, 1, 4);
            grid.add(new Label("Тираж:"), 0, 5); grid.add(printRun, 1, 5);
        }

        isOnHandsCheckBox.setSelected(selected.isOnHands());
        grid.add(isOnHandsCheckBox, 0, 6);

        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        dialog.getDialogPane().setContent(grid);

        final TextField finalCourse = course;
        final TextField finalPrintRun = printRun;

        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.addEventFilter(ActionEvent.ACTION, event -> {
            try {
                Book tempBook;
                int parsedYear = Integer.parseInt(year.getText().trim());
                boolean isOnHands = isOnHandsCheckBox.isSelected();

                if (selected instanceof TextBook tb) {
                    int newCourse = Integer.parseInt(finalCourse.getText().trim());
                    int newPrintRun = Integer.parseInt(finalPrintRun.getText().trim());
                    tempBook = new TextBook(selected.getIsbn(), title.getText().trim(), authors.getText().trim(),
                            parsedYear, genre.getText().trim(), isOnHands, newCourse, newPrintRun);
                } else {
                    tempBook = new OrdinaryBook(selected.getIsbn(), title.getText().trim(), authors.getText().trim(),
                            parsedYear, genre.getText().trim(), isOnHands);
                }

                tempBook.validate();

            } catch (NumberFormatException e) {
                event.consume();
                showAlert(Alert.AlertType.ERROR, "Ошибка ввода", "Числовые поля должны содержать числа!");
            } catch (BookValidationException e) {
                event.consume();
                showAlert(Alert.AlertType.ERROR, "Ошибка валидации", e.getFormattedMessage());
            }
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                selected.setTitle(title.getText().trim());
                selected.setAuthors(authors.getText().trim());
                selected.setYear(Integer.parseInt(year.getText().trim()));
                selected.setGenre(genre.getText().trim());
                selected.setOnHands(isOnHandsCheckBox.isSelected());

                if (selected instanceof TextBook tb) {
                    tb.setCourse(Integer.parseInt(finalCourse.getText().trim()));
                    tb.setPrintRun(Integer.parseInt(finalPrintRun.getText().trim()));
                }
                return true;
            }
            return false;
        });

        Boolean saved = dialog.showAndWait().orElse(false);
        if (saved) {
            onRefresh.run();
        }
        return saved;
    }

    public void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
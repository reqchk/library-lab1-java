package org.example;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class Main extends Application {
    private static final String FILE_NAME = "books.csv";
    private static final String LABEL_ANTIQUARIAN = "Антикварная книга";

    private final CsvLoader csv = new CsvLoader();
    private final Path file = Path.of(FILE_NAME);
    private final ObservableList<BookAll> booksList = FXCollections.observableArrayList();
    private final TableView<BookAll> table = new TableView<>();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("Электронная библиотека");
        createTable();
        handleLoad(true);

        Button loadButton = new Button("Загрузить из CSV");
        Button saveButton = new Button("Сохранить в CSV");
        Button addButton = new Button("Добавить");
        Button editButton = new Button("Изменить");
        editButton.setDisable(true); // пока ничего не выбрано

        loadButton.setOnAction(event -> handleLoad(false));
        saveButton.setOnAction(event -> handleSave());
        addButton.setOnAction(event -> handleAdd());
        editButton.setOnAction(event -> handleEdit());

        // «Изменить» доступна только для объектов, реализующих Editable
        // (null тоже даёт false в instanceof, поэтому пустое выделение учтено автоматически)
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) ->
                editButton.setDisable(!(newSel instanceof Editable)));

        HBox buttons = new HBox(10, addButton, editButton, loadButton, saveButton);
        buttons.setAlignment(Pos.BOTTOM_CENTER);

        VBox root = new VBox(10, table, buttons);
        VBox.setVgrow(table, Priority.ALWAYS);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        root.setPadding(new Insets(10));

        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.show();
    }

    private void createTable() {
        TableColumn<BookAll, String> colType = new TableColumn<>("Тип");
        colType.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue() instanceof AntiquarianBook ? LABEL_ANTIQUARIAN :
                        data.getValue() instanceof TextBook ? BookDialog.LABEL_TEXTBOOK : BookDialog.LABEL_BOOK));

        TableColumn<BookAll, String> colIsbn = new TableColumn<>("ISBN");
        colIsbn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getISBN()));

        TableColumn<BookAll, String> colTitle = new TableColumn<>("Название");
        colTitle.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle()));

        TableColumn<BookAll, String> colAuthors = new TableColumn<>("Авторы");
        colAuthors.setCellValueFactory(data -> new SimpleStringProperty(
                String.join(", ", data.getValue().getAuthors())));

        TableColumn<BookAll, String> colYear = new TableColumn<>("Год");
        colYear.setCellValueFactory(data -> new SimpleStringProperty(
                String.valueOf(data.getValue().getYear())));

        TableColumn<BookAll, String> colGenre = new TableColumn<>("Жанр");
        colGenre.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getGenre()));

        TableColumn<BookAll, String> colCourse = new TableColumn<>("Курс");
        colCourse.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue() instanceof TextBook tb ? String.valueOf(tb.getCourse()) : ""));

        TableColumn<BookAll, String> colPrintRun = new TableColumn<>("Тираж");
        colPrintRun.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue() instanceof TextBook tb ? String.valueOf(tb.getPrintRun()) : ""));

        TableColumn<BookAll, String> colOnHands = new TableColumn<>("На руках");
        colOnHands.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().isOnHands() ? "да" : "нет"));

        table.getColumns().setAll(colType, colIsbn, colTitle, colAuthors, colYear,
                colGenre, colCourse, colPrintRun, colOnHands);
        table.setItems(booksList);
    }

    private void handleAdd() {
        BookDialog.forAdd(booksList).showAndWait().ifPresent(booksList::add);
    }

    private void handleEdit() {
        if (!(table.getSelectionModel().getSelectedItem() instanceof Book target)) return;

        BookDialog.forEdit(target, booksList).showAndWait().ifPresent(edited -> {
            target.applyFrom(edited);
            table.refresh();
        });
    }

    /**
     * Чтение файла в фоновом потоке, чтобы не блокировать GUI-поток.
     * Обновление таблицы и диалоги - в setOnSucceeded/setOnFailed, они выполняются уже в GUI-потоке.
     */
    private void handleLoad(boolean silent) {
        Task<List<BookAll>> task = new Task<>() {
            @Override protected List<BookAll> call() throws IOException {
                return csv.load(file);
            }
        };

        task.setOnSucceeded(event -> {
            booksList.setAll(task.getValue());
            if (!silent) {
                infoDialog("Успех", "Данные успешно загружены из файла '" + FILE_NAME + "'");
            }
        });

        task.setOnFailed(event -> {
            Throwable ex = task.getException();
            errorDialog("Ошибка загрузки",
                    "Не удалось прочитать файл '" + FILE_NAME + "'.\n" +
                            "Причина: " + ex.toString());
        });

        new Thread(task, "csv-load").start();
    }

    private void handleSave() {
        List<BookAll> snapshot = List.copyOf(booksList);

        Task<Void> task = new Task<>() {
            @Override protected Void call() throws IOException {
                csv.save(file, snapshot);
                return null;
            }
        };

        task.setOnSucceeded(event ->
                infoDialog("Успешное сохранение", "Данные успешно сохранены в файл '" + FILE_NAME + "'"));

        task.setOnFailed(event -> {
            Throwable ex = task.getException();
            errorDialog("Ошибка сохранения",
                    "Не удалось записать данные в файл '" + FILE_NAME + "'.\n\n" +
                            "Причина: " + ex.toString());
        });

        new Thread(task, "csv-save").start();
    }

    private void infoDialog(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void errorDialog(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
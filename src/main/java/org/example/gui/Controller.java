package org.example.gui;

import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.example.model.BookBST;
import org.example.io.CsvLoader;
import org.example.model.Book;

import java.util.List;

public class Controller {

    private final BookBST bookTree = new BookBST();
    private final CsvLoader loader = new CsvLoader();
    private final View view;
    private final Stage stage;

    public Controller(Stage stage, View view) {
        this.stage = stage;
        this.view = view;
    }

    public void onLoadCsv() {
        String filePath = view.showOpenCsvDialog(stage);
        if (filePath == null) return;

        List<Book> loadedBooks = loader.loadFromFile(filePath);
        for (Book book : loadedBooks) { bookTree.insert(book); }
        view.getTableView().getItems().setAll(bookTree.getAllBooks());

        List<String> errors = loader.getLoadErrors();
        if (!errors.isEmpty()) {
            StringBuilder errorText = new StringBuilder();
            errorText.append("Загружено книг: ").append(loadedBooks.size()).append("\n");
            errorText.append("Всего ошибок: ").append(errors.size()).append("\n\n");
            int limit = Math.min(errors.size(), 5);
            for (int i = 0; i < limit; i++) {
                errorText.append(errors.get(i)).append("\n");
            }
            if (errors.size() > 5) {
                errorText.append("\n... и ещё ").append(errors.size() - 5).append(" ошибок.");
            }
            view.showAlert(Alert.AlertType.WARNING, "Загрузка с предупреждениями", errorText.toString());
        } else {
            view.showAlert(Alert.AlertType.INFORMATION, "Успех", "Успешно загружено книг: " + loadedBooks.size());
        }
    }

    public void onSaveCsv() {
        String filePath = view.showSaveCsvDialog(stage);
        if (filePath == null) return;

        loader.saveToFile(filePath, bookTree.getAllBooks());
        view.showAlert(Alert.AlertType.INFORMATION, "Успех", "Данные успешно сохранены!");
    }

    public void onAddBook() {
        Book newBook = view.showAddBookDialog(stage);
        if (newBook != null) {
            bookTree.insert(newBook);
            view.getTableView().getItems().setAll(bookTree.getAllBooks());
        }
    }

    public void onEditBook() {
        Book selected = view.getTableView().getSelectionModel().getSelectedItem();
        view.showEditBookDialog(stage, selected, bookTree, () -> {
            bookTree.remove(selected);
            bookTree.insert(selected);
            view.getTableView().getItems().setAll(bookTree.getAllBooks());
        });
    }
}
package org.lawpavilion.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.lawpavilion.model.Book;

import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.LocalDate;
import java.util.List;

public class BookController {

    private final String BASE_URL = "http://localhost:8081/api/books";

    @FXML private TextField titleField;
    @FXML private TextField authorField;
    @FXML private TextField isbnField;
    @FXML private DatePicker publishedDateField;

    @FXML private TableView<Book> bookTable;
    @FXML private TableColumn<Book, Long> idColumn;
    @FXML private TableColumn<Book, String> titleColumn;
    @FXML private TableColumn<Book, String> authorColumn;
    @FXML private TableColumn<Book, String> isbnColumn;
    @FXML private TableColumn<Book, LocalDate> publishedDateColumn;

    private final ObservableList<Book> bookList = FXCollections.observableArrayList();
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @FXML
    public void initialize() {

        idColumn.setCellValueFactory(cellData ->
                new SimpleLongProperty(cellData.getValue().getId()).asObject()
        );

        titleColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getTitle())
        );

        authorColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getAuthor())
        );

        isbnColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getIsbn())
        );

        publishedDateColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getPublishedDate())
        );
        bookTable.setItems(bookList);

        bookTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> populateForm(newSelection)
        );

        loadBooks();
    }

    private void populateForm(Book book) {
        if (book != null) {
            titleField.setText(book.getTitle());
            authorField.setText(book.getAuthor());
            isbnField.setText(book.getIsbn());
            publishedDateField.setValue(book.getPublishedDate());
        }
    }

    @FXML
    private void handleAdd() {
        try {
            Book book = new Book();
            book.setTitle(titleField.getText());
            book.setAuthor(authorField.getText());
            book.setIsbn(isbnField.getText());
            book.setPublishedDate(publishedDateField.getValue());

            String json = objectMapper.writeValueAsString(book);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL))
                    .header("Content-Type", "application/json")
                    .POST(BodyPublishers.ofString(json))
                    .build();

            httpClient.send(request, BodyHandlers.ofString());
            loadBooks();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void handleUpdate() {
        try {
            Book selected = bookTable.getSelectionModel().getSelectedItem();
            if (selected == null) return;

            selected.setTitle(titleField.getText());
            selected.setAuthor(authorField.getText());
            selected.setIsbn(isbnField.getText());
            selected.setPublishedDate(publishedDateField.getValue());

            String json = objectMapper.writeValueAsString(selected);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/" + selected.getId()))
                    .header("Content-Type", "application/json")
                    .PUT(BodyPublishers.ofString(json))
                    .build();

            httpClient.send(request, BodyHandlers.ofString());
            loadBooks();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void handleDelete() {
        try {
            Book selected = bookTable.getSelectionModel().getSelectedItem();
            if (selected == null) return;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/" + selected.getId()))
                    .DELETE()
                    .build();

            httpClient.send(request, BodyHandlers.ofString());
            loadBooks();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void handleRefresh() { loadBooks(); }

    private void loadBooks() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
            List<Book> books = objectMapper.readValue(response.body(), new TypeReference<>() {});
            bookList.setAll(books);
        } catch (Exception e) { e.printStackTrace(); }
    }
}

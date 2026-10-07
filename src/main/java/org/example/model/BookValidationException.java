package org.example.model;

public class BookValidationException extends Exception {

    public enum ErrorCode {
        EMPTY_FIELD("Пустое обязательное поле"),
        BAD_NUMBER("Некорректное числовое значение"),
        INVALID_FIELD("Некорректный формат строки");

        private final String description;

        ErrorCode(String description) { this.description = description; }

        public String getDescription() { return description; }
    }

    private final ErrorCode errorCode;

    public BookValidationException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() { return errorCode; }

    public String getFormattedMessage() {
        return String.format("[%s]: %s", errorCode.getDescription(), getMessage());
    }
}
package org.example.io;

public class CsvParseException extends Exception {

    public enum ErrorCode {
        WRONG_FIELD_COUNT("Недостаточно полей"),
        BAD_NUMBER("Некорректное числовое значение"),
        UNKNOWN_TYPE("Неизвестный тип книги"),
        EMPTY_FIELD("Пустое обязательное поле"),
        INVALID_FIELD("Некорректный формат строки"),
        INVALID_HEADER("Неверный заголовок файла");

        private final String description;

        ErrorCode(String description) { this.description = description; }

        public String getDescription() { return description; }
    }

    private final ErrorCode errorCode;
    private final int lineNumber;
    private final String rawMessage; // Добавляем поле для хранения чистого сообщения

    public CsvParseException(ErrorCode errorCode, int lineNumber, String message) {
        super("Строка " + lineNumber + ": " + message);
        this.errorCode = errorCode;
        this.lineNumber = lineNumber;
        this.rawMessage = message; // Сохраняем чистый текст
    }

    public ErrorCode getErrorCode() { return errorCode; }

    public int getLineNumber() { return lineNumber; }

    public String getRawMessage() { return rawMessage; }

    public String getFormattedMessage() {
        return String.format("Строка %d [%s]: %s", lineNumber, errorCode.getDescription(), rawMessage);
    }
}
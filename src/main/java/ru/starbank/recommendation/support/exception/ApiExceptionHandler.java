package ru.starbank.recommendation.support.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import ru.starbank.recommendation.exception.ErrorResponse;
import ru.starbank.recommendation.exception.ValidationErrorResponse;

/**
 * Глобальный обработчик исключений для всех контроллеров.
 * Ловим различные исключения и возвращаем понятные ошибки с нужными HTTP статусами.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    /**
     * Обработка исключений при ошибках доступа к базе данных.
     * Логируем ошибку и возвращаем HTTP статус 500.
     *
     * @param ex исключение, возникшее при работе с БД
     * @return ResponseEntity с сообщением об ошибке
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> handleDatabaseException(DataAccessException ex) {
        // Логируем ошибку с полным стэком
        log.error("Ошибка при обращении к базе данных", ex);

        // Возвращаем HTTP 500 с ошибкой
        ErrorResponse errorResponse = new ErrorResponse("Ошибка при обращении к базе данных.");
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Обработка ошибок при отсутствии пользователя.
     * Логируем и возвращаем HTTP статус 404.
     *
     * @param ex исключение, когда не найден пользователь
     * @return ResponseEntity с сообщением об ошибке
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(UserNotFoundException ex) {
        log.warn("Пользователь не найден: ", ex);
        ErrorResponse errorResponse = new ErrorResponse("Пользователь не найден.");
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Обработка ошибок валидации данных.
     * Логируем и возвращаем HTTP статус 400.
     *
     * @param ex исключение, вызванное ошибками валидации
     * @return ResponseEntity с сообщением об ошибке
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.warn("Ошибка валидации входных данных: ", ex);

        // Формируем подробный ответ с ошибками валидации
        ValidationErrorResponse errorResponse = new ValidationErrorResponse("Ошибка валидации данных.");
        ex.getBindingResult().getAllErrors().forEach(error -> {
            errorResponse.addError(error.getDefaultMessage());
        });

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Обработка других исключений (непредвиденные ошибки).
     * Логируем и возвращаем HTTP статус 500.
     *
     * @param ex исключение
     * @return ResponseEntity с сообщением об ошибке
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        // Логируем любые другие ошибки
        log.error("Неизвестная ошибка", ex);

        // Возвращаем HTTP 500 с ошибкой
        ErrorResponse errorResponse = new ErrorResponse("Неизвестная ошибка.");
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
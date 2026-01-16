package ru.starbank.recommendation.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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
        log.error("Ошибка при обращении к базе данных", ex);
        ErrorResponse errorResponse = new ErrorResponse("Ошибка при обращении к базе данных.");
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Обработка ошибки невалидного userId (например, некорректный UUID в UuidParser).
     */
    @ExceptionHandler(InvalidUserIdException.class)
    public ResponseEntity<ErrorResponse> handleInvalidUserIdException(InvalidUserIdException ex) {
        log.warn("Некорректный userId: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Обработка ошибки "правило не найдено".
     */
    @ExceptionHandler(RuleNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleRuleNotFoundException(RuleNotFoundException ex) {
        log.warn("Динамическое правило не найдено: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Обработка ошибок некорректных аргументов правила/движка.
     * Это ошибки запроса/данных, поэтому возвращаем 400.
     */
    @ExceptionHandler({
            InvalidRuleArgumentsException.class,
            InvalidProductIdException.class,
            UnsupportedQueryTypeException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequestRuntime(RuntimeException ex) {
        log.warn("Ошибка запроса: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Обработка ошибок валидации данных (@Valid).
     * Возвращаем HTTP 400 с деталями.
     *
     * @param ex исключение, вызванное ошибками валидации
     * @return ResponseEntity с сообщением об ошибке
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.warn("Ошибка валидации входных данных", ex);

        ValidationErrorResponse errorResponse = new ValidationErrorResponse("Ошибка валидации данных.");
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String message = error.getDefaultMessage();
            errorResponse.addError(message != null ? message : "Некорректное значение");
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
        log.error("Неизвестная ошибка", ex);
        ErrorResponse errorResponse = new ErrorResponse("Неизвестная ошибка.");
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
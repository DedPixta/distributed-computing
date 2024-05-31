package dev.makos.publisher.exception.handler;

import dev.makos.publisher.exception.CustomException;
import dev.makos.publisher.model.dto.exception.ErrorResponseDTO;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  @Nullable HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  @Nullable WebRequest request) {
        Map<String, String> invalidFields = ex.getFieldErrors().stream()
                .filter(fieldError -> fieldError != null && fieldError.getDefaultMessage() != null)
                .collect(toMap(FieldError::getField, DefaultMessageSourceResolvable::getDefaultMessage));

        ErrorResponseDTO errorResponseDTO = ErrorResponseDTO.builder()
                .dateTime(LocalDateTime.now())
                .code(status.value())
                .message("Validation Error")
                .invalidFields(invalidFields)
                .build();

        return ResponseEntity.status(status).body(errorResponseDTO);
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<Object> handleCustomException(CustomException ex) {
        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                .dateTime(LocalDateTime.now())
                .code(ex.getHttpStatus().value())
                .message(ex.getMessage())
                .build();

        return ResponseEntity.status(ex.getHttpStatus()).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllExceptions(Exception ex) {
        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                .dateTime(LocalDateTime.now())
                .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("An unexpected error occurred: " + ex.getLocalizedMessage())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}

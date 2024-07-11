package dev.makos.discussion.exception;

import org.springframework.http.HttpStatus;

public class KafkaException extends CustomException {

    public KafkaException(HttpStatus httpStatus, String message) {
        super(httpStatus, message);
    }
}

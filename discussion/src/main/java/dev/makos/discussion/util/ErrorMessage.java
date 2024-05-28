package dev.makos.discussion.util;

import lombok.Getter;

@Getter
public enum ErrorMessage {
    COMMENT_NOT_FOUND("Comment not found");

    private final String text;

    ErrorMessage(String text) {
        this.text = text;
    }
}

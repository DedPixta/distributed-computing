package dev.makos.publisher.util;

import lombok.Getter;

@Getter
public enum ErrorMessage {
    STICKER_NOT_FOUND("Sticker not found"),
    COMMENT_NOT_FOUND("Comment not found"),
    TWEET_NOT_FOUND("Tweet not found"),
    TWEET_TITLE_ALREADY_EXISTS("Title already exists"),
    CREATOR_NOT_FOUND("Creator not found"),
    CREATOR_LOGIN_ALREADY_EXISTS("Login already exists");

    private final String text;

    ErrorMessage(String text) {
        this.text = text;
    }
}

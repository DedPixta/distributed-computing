package dev.makos.discussion.model.dto;

import lombok.Data;

@Data
public class CommentDTO {

    private String country;
    private Long id;
    private Long tweetId;
    private String content;

}

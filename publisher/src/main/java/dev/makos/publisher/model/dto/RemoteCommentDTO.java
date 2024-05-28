package dev.makos.publisher.model.dto;

import lombok.Data;

@Data
public class RemoteCommentDTO {

    private String country;
    private Long id;
    private Long tweetId;
    private String content;

}

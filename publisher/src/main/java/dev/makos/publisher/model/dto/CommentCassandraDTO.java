package dev.makos.publisher.model.dto;

import dev.makos.publisher.model.State;
import lombok.Data;

@Data
public class CommentCassandraDTO {

    private String country;
    private Long id;
    private Long tweetId;
    private String content;
    private State state;
    private String method;

}

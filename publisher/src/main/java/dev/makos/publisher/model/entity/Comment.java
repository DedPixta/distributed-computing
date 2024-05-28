package dev.makos.publisher.model.entity;

import lombok.Data;

@Data
public class Comment {

    private Long id;
    private String content;
    private Tweet tweet;

}

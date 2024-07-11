package dev.makos.publisher.model.entity;

import dev.makos.publisher.model.State;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Comment {

    private Long id;
    private String content;
    private Tweet tweet;
    private State state;

}

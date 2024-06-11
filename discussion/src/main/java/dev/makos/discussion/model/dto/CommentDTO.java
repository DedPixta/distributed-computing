package dev.makos.discussion.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommentDTO {

    @NotBlank(message = "Country is not provided")
    private String country;
    @Min(value = 1, message = "ID must be greater than 0")
    private Long id;
    @Min(value = 1, message = "Tweet ID must be greater than 0")
    @NotNull(message = "Tweet ID is not provided")
    private Long tweetId;
    @NotBlank(message = "Content is not provided")
    private String content;

}

package dev.makos.publisher.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Data
public class CommentDTO {

    @Schema(description = "Some ID", requiredMode = REQUIRED, example = "1")
    @Min(value = 1, message = "ID must be greater than 0")
    private Long id;

    @Schema(description = "Some content", requiredMode = REQUIRED, example = "Some content")
    @NotNull(message = "Content is required")
    @Size(min = 2, max = 2048, message = "Content must be between 2 and 2048 characters")
    private String content;

    @Schema(description = "Tweet ID", requiredMode = REQUIRED, example = "1")
    @NotNull(message = "Tweet ID is required")
    private Long tweetId;

}

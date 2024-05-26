package dev.makos.publisher.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Data
public class StickerDTO {

    @Schema(description = "Some ID", requiredMode = REQUIRED, example = "1")
    @Min(value = 1, message = "ID must be greater than 0")
    private Long id;

    @Schema(description = "Sticker name", requiredMode = REQUIRED, example = "Like")
    @NotNull(message = "Name is required")
    @Size(min = 2, max = 32, message = "Name must be between 2 and 32 characters")
    private String name;

}

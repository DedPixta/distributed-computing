package org.example.distributedcomputing.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class StickerDTO {

    @NotNull
    @Size(min = 2, max = 32, message = "Name must be between 2 and 32 characters")
    private String name;

}

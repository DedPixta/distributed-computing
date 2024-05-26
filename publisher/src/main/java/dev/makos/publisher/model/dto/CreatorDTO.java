package dev.makos.publisher.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Data
public class CreatorDTO {

    @Schema(description = "Some ID", requiredMode = REQUIRED, example = "1")
    @Min(value = 1, message = "ID must be greater than 0")
    private Long id;

    @Schema(description = "Some login", requiredMode = REQUIRED, example = "JohnyD")
    @NotNull(message = "Login is required")
    @Size(min = 2, max = 64, message = "Login must be between 2 and 64 characters")
    private String login;

    @Schema(description = "Some password", requiredMode = REQUIRED, example = "secret")
    @NotNull(message = "password is required")
    @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
    private String password;

    @Schema(description = "First name", requiredMode = REQUIRED, example = "John")
    @NotNull(message = "Password is required")
    @Size(min = 2, max = 64, message = "Firstname must be between 2 and 64 characters")
    private String firstname;

    @Schema(description = "Last name", requiredMode = REQUIRED, example = "Doe")
    @NotNull(message = "Firstname is required")
    @Size(min = 2, max = 64, message = "Lastname must be between 2 and 64 characters")
    private String lastname;

}

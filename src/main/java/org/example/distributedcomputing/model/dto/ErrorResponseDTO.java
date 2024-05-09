package org.example.distributedcomputing.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ErrorResponseDTO {

    private String message;
    private int code;
    private Map<String, String> invalidFields;
    private LocalDateTime dateTime;

}

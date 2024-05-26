package dev.makos.publisher.controller;

import dev.makos.publisher.model.dto.CreatorDTO;
import dev.makos.publisher.service.CreatorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Creators", description = "Creators API")
@RequiredArgsConstructor
@RequestMapping("/api/v1.0/creators")
@RestController
public class CreatorController {

    private final CreatorService creatorService;

    @Operation(summary = "Create a creator")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public CreatorDTO saveOne(@Valid @RequestBody CreatorDTO creatorDTO) {
        return creatorService.saveOne(creatorDTO);
    }

    @Operation(summary = "Get a creator by ID")
    @GetMapping("/{id}")
    public CreatorDTO getOne(@PathVariable Long id) {
        return creatorService.getOne(id);
    }

    @Operation(summary = "Get all creators")
    @GetMapping
    public List<CreatorDTO> getAll() {
        return creatorService.getAll();
    }

    @Operation(summary = "Delete a creator by ID")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void deleteOne(@PathVariable Long id) {
        creatorService.deleteOne(id);
    }

    @Operation(summary = "Update a creator by ID")
    @PutMapping
    public CreatorDTO updateOne(@Valid @RequestBody CreatorDTO creatorDTO) {
        return creatorService.updateOne(creatorDTO);
    }
}

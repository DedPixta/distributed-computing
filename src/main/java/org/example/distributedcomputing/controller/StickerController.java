package org.example.distributedcomputing.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.distributedcomputing.model.dto.StickerDTO;
import org.example.distributedcomputing.service.StickerService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Stickers", description = "Stickers API")
@RequiredArgsConstructor
@RequestMapping("/api/v1.0/stickers")
@RestController
public class StickerController {

    private final StickerService stickerService;

    @Operation(summary = "Create a sticker")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public StickerDTO saveOne(@Valid @RequestBody StickerDTO stickerDTO) {
        return stickerService.saveOne(stickerDTO);
    }

    @Operation(summary = "Get a sticker by ID")
    @GetMapping("/{id}")
    public StickerDTO getOne(@PathVariable Long id) {
        return stickerService.getOne(id);
    }

    @Operation(summary = "Get all stickers")
    @GetMapping
    public List<StickerDTO> getAll() {
        return stickerService.getAll();
    }

    @Operation(summary = "Delete a sticker by ID")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void deleteOne(@PathVariable Long id) {
        stickerService.deleteOne(id);
    }

    @Operation(summary = "Update a sticker by ID")
    @PutMapping
    public StickerDTO updateOne(@Valid @RequestBody StickerDTO stickerDTO) {
        return stickerService.updateOne(stickerDTO);
    }
}

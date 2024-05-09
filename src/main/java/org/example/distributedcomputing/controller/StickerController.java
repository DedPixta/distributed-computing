package org.example.distributedcomputing.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.distributedcomputing.model.dto.StickerDTO;
import org.example.distributedcomputing.service.StickerService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/v1/stickers")
@RestController
public class StickerController {

    private final StickerService stickerService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public StickerDTO saveOne(@Valid @RequestBody StickerDTO stickerDTO) {
        return stickerService.saveOne(stickerDTO);
    }

    @GetMapping("/{id}")
    public StickerDTO getOne(@PathVariable Long id) {
        return stickerService.getOne(id);
    }

    @GetMapping
    public List<StickerDTO> getAll() {
        return stickerService.getAll();
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void deleteOne(@PathVariable Long id) {
        stickerService.deleteOne(id);
    }

    @PutMapping("/{id}")
    public StickerDTO updateOne(@PathVariable Long id, @Valid @RequestBody StickerDTO stickerDTO) {
        return stickerService.updateOne(id, stickerDTO);
    }
}

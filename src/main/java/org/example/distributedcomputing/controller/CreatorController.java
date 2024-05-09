package org.example.distributedcomputing.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.distributedcomputing.model.dto.CreatorDTO;
import org.example.distributedcomputing.service.CreatorService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/v1/creators")
@RestController
public class CreatorController {

    private final CreatorService creatorService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public CreatorDTO saveOne(@Valid @RequestBody CreatorDTO creatorDTO) {
        return creatorService.saveOne(creatorDTO);
    }

    @GetMapping("/{id}")
    public CreatorDTO getOne(@PathVariable Long id) {
        return creatorService.getOne(id);
    }

    @GetMapping
    public List<CreatorDTO> getAll() {
        return creatorService.getAll();
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void deleteOne(@PathVariable Long id) {
        creatorService.deleteOne(id);
    }

    @PutMapping("/{id}")
    public CreatorDTO updateOne(@PathVariable Long id, @Valid @RequestBody CreatorDTO creatorDTO) {
        return creatorService.updateOne(id, creatorDTO);
    }
}

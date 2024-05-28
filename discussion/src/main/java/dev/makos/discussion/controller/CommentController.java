package dev.makos.discussion.controller;

import dev.makos.discussion.model.dto.CommentDTO;
import dev.makos.discussion.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Comments", description = "Comments API")
@RequiredArgsConstructor
@RequestMapping("/api/v1.0/comments")
@RestController
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "Create a comment")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public CommentDTO saveOne(@Valid @RequestBody CommentDTO commentDTO) {
        return commentService.saveOne(commentDTO);
    }

    @Operation(summary = "Get a comment by ID")
    @GetMapping("/{id}")
    public CommentDTO getOne(@PathVariable Long id) {
        return commentService.getOne(id);
    }

    @Operation(summary = "Get all comments")
    @GetMapping
    public List<CommentDTO> getAll() {
        return commentService.getAll();
    }

    @Operation(summary = "Delete a comment by ID")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void deleteOne(@PathVariable Long id) {
        commentService.deleteOne(id);
    }

    @Operation(summary = "Update a comment by ID")
    @PutMapping
    public CommentDTO updateOne(@Valid @RequestBody CommentDTO commentDTO) {
        return commentService.updateOne(commentDTO);
    }
}

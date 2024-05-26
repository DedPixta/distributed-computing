package dev.makos.publisher.controller;

import dev.makos.publisher.model.dto.TweetDTO;
import dev.makos.publisher.service.TweetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Tweets", description = "Tweets API")
@RequiredArgsConstructor
@RequestMapping("/api/v1.0/tweets")
@RestController
public class TweetController {

    private final TweetService tweetService;

    @Operation(summary = "Create a tweet")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public TweetDTO saveOne(@Valid @RequestBody TweetDTO tweetDTO) {
        return tweetService.saveOne(tweetDTO);
    }

    @Operation(summary = "Get a tweet by ID")
    @GetMapping("/{id}")
    public TweetDTO getOne(@PathVariable Long id) {
        return tweetService.getOne(id);
    }

    @Operation(summary = "Get all tweets")
    @GetMapping
    public List<TweetDTO> getAll() {
        return tweetService.getAll();
    }

    @Operation(summary = "Delete a tweet by ID")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void deleteOne(@PathVariable Long id) {
        tweetService.deleteOne(id);
    }

    @Operation(summary = "Update a tweet by ID")
    @PutMapping
    public TweetDTO updateOne(@Valid @RequestBody TweetDTO tweetDTO) {
        return tweetService.updateOne(tweetDTO);
    }
}

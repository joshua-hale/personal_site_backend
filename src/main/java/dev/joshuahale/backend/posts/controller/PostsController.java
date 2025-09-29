package dev.joshuahale.backend.posts.controller;

import dev.joshuahale.backend.posts.dto.PostRequest;
import dev.joshuahale.backend.posts.dto.PostUpdateRequest;
import dev.joshuahale.backend.posts.dto.PostResponse;
import dev.joshuahale.backend.posts.service.PostsService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@Validated
public class PostsController {

    private final PostsService service;

    public PostsController(PostsService service) {
        this.service = service;
    }

    // List all posts in your preferred order
    @GetMapping
    public List<PostResponse> listAllOrdered() {
        return service.listAllOrdered();
    }

    // Get by numeric ID
    @GetMapping("/{id}")
    public PostResponse getById(@PathVariable Long id) {
        return service.getById(id);
    }

    // Get by slug
    @GetMapping("/slug/{slug}")
    public PostResponse getBySlug(@PathVariable String slug) {
        return service.getBySlug(slug);
    }

    // Create a post
    @PostMapping
    public ResponseEntity<PostResponse> create(@Valid @RequestBody PostRequest request) {
        PostResponse created = service.create(request);
        // If PostResponse is a record, change getId() -> id()
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .header("Location", "/api/posts/slug/" + created.getSlug())
                .body(created);
    }

    // Partial update (PATCH)
    @PatchMapping("/{id}")
    public PostResponse update(@PathVariable Long id,
                               @Valid @RequestBody PostUpdateRequest request) {
        return service.update(id, request);
    }

    // Delete
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
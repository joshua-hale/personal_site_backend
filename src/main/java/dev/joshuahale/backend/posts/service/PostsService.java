package dev.joshuahale.backend.posts.service;

import dev.joshuahale.backend.posts.dto.PostResponse;
import dev.joshuahale.backend.posts.dto.PostRequest;
import dev.joshuahale.backend.posts.dto.PostUpdateRequest;
import java.util.List;

public interface PostsService {
    PostResponse create(PostRequest request);
    PostResponse getById(Long id);
    PostResponse getBySlug(String slug);
    List<PostResponse> listAllOrdered();
    PostResponse update(Long id, PostUpdateRequest request);
    void delete(Long id);
}
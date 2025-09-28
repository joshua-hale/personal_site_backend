package dev.joshuahale.backend.posts.service;

import dev.joshuahale.backend.posts.dto.PostRequest;
import dev.joshuahale.backend.posts.dto.PostResponse;
import dev.joshuahale.backend.posts.dto.PostUpdateRequest;
import dev.joshuahale.backend.posts.entity.PostsEntity;
import dev.joshuahale.backend.posts.repository.PostsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service                                   // 1) Registers this class as a Spring bean for DI
@Transactional                             // 2) Makes write methods atomic; reads marked readOnly below
public class PostsServiceImpl implements PostsService {

    private final PostsRepository repo;

    public PostsServiceImpl(PostsRepository repo) {
        this.repo = repo;
    }

    // ===============================
    // Create
    // ===============================
    @Override
    public PostResponse create(PostRequest request) {
        // 3) Normalize or generate a slug from the title if none is provided
        String baseSlug = (request.getSlug() == null || request.getSlug().isBlank())
        ? normalizeSlug(request.getTitle())
        : normalizeSlug(request.getSlug());

        // 4) Ensure the slug is unique (append -2, -3, ... if needed)
        String uniqueSlug = ensureUniqueSlug(baseSlug);

        // 5) Map DTO -> Entity (only the fields you allow clients to set)
        PostsEntity e = new PostsEntity();
        e.setTitle(request.getTitle());
        e.setContent(request.getContent());
        e.setHeroImage(request.getHeroImage());
        e.setSlug(uniqueSlug);

        // 6) Persist using your custom repository (EntityManager under the hood)
        PostsEntity saved = repo.save(e);

        // 7) Map Entity -> DTO response (so controllers never expose JPA entities)
        return toResponse(saved);
    }

    // ===============================
    // Read (by id)
    // ===============================
    @Override
    @Transactional(readOnly = true)        // 8) Read-only transaction: small perf/safety win
    public PostResponse getById(Long id) {
        PostsEntity e = repo.findById(id)
            .orElseThrow(() -> new PostNotFoundException("Post not found: id=" + id));
        return toResponse(e);
    }

    // ===============================
    // Read (by slug)
    // ===============================
    @Override
    @Transactional(readOnly = true)
    public PostResponse getBySlug(String slug) {
        PostsEntity e = repo.findBySlug(slug)
            .orElseThrow(() -> new PostNotFoundException("Post not found: slug=" + slug));
        return toResponse(e);
    }

    // ===============================
    // List all (ordered newest-first)
    // ===============================
    @Override
    @Transactional(readOnly = true)
    public List<PostResponse> listAllOrdered() {
        // 9) Delegate sorting to the repository; convert entities to DTOs
        return repo.listAllOrdered()
            .stream()
            .map(this::toResponse)
            .toList();
    }

    // ===============================
    // Update (partial edits supported)
    // ===============================
    @Override
    public PostResponse update(Long id, PostUpdateRequest request) {
        // 10) Load or 404, so downstream code never handles nulls
        PostsEntity e = repo.findById(id)
            .orElseThrow(() -> new PostNotFoundException("Post not found: id=" + id));

        // 11) Apply only the fields the client provided (null means "leave as-is")
        if (request.getTitle() != null) {
            e.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            e.setContent(request.getContent());
        }
        if (request.getHeroImage() != null) {
            e.setHeroImage(request.getHeroImage());
        }
        if (request.getSlug() != null) {
            String normalized = normalizeSlug(request.getSlug());

            // 12) If slug actually changes, ensure uniqueness without clashing with this same post
            if (!normalized.equals(e.getSlug())) {
                Optional<PostsEntity> existing = repo.findBySlug(normalized);
                if (existing.isPresent() && !existing.get().getId().equals(e.getId())) {
                    // 13) Either throw 409 or auto-suffix; throwing is clearer for admins
                    throw new DuplicateSlugException("Slug already in use: " + normalized);
                }
                e.setSlug(normalized);
            }
        }

        // 14) Save the updated entity; @PreUpdate on the entity can update timestamps
        PostsEntity saved = repo.save(e);
        return toResponse(saved);
    }

    // ===============================
    // Delete
    // ===============================
    @Override
    public void delete(Long id) {
        // 15) Enforce "404 when missing" semantics (clearer than silent no-op)
        boolean removed = repo.deleteById(id);
        if (!removed) {
            throw new PostNotFoundException("Post not found: id=" + id);
        }
    }

    // ===============================
    // Mapping helpers (Entity <-> DTO)
    // ===============================
    private PostResponse toResponse(PostsEntity e) {
        // 16) Centralize the shape you expose to clients
        PostResponse r = new PostResponse();
        r.setId(e.getId());
        r.setTitle(e.getTitle());
        r.setSlug(e.getSlug());
        r.setContent(e.getContent());
        r.setHeroImage(e.getHeroImage());
        r.setCreatedAt(e.getCreatedAt());
        r.setUpdatedAt(e.getUpdatedAt());
        return r;
    }

    // ===============================
    // Slug helpers (self-contained)
    // ===============================
    private String normalizeSlug(String raw) {
        // 17) Trim, lowercase, strip non [a-z0-9 -], collapse whitespace to dashes
        if (raw == null) return "post";
        String s = raw.trim().toLowerCase()
            .replaceAll("[^a-z0-9\\s-]", "")
            .replaceAll("\\s+", "-")
            .replaceAll("-{2,}", "-");
        return s.isBlank() ? "post" : s;
    }

    private String ensureUniqueSlug(String base) {
        // 18) If the base slug exists, append -2, -3, ... until unique
        String candidate = base;
        int i = 2;
        while (repo.findBySlug(candidate).isPresent()) {
            candidate = base + "-" + i++;
        }
        return candidate;
    }

    // ===============================
    // Domain exceptions (clean 404/409 via @ControllerAdvice)
    // ===============================
    public static class PostNotFoundException extends RuntimeException {
        public PostNotFoundException(String message) { super(message); }
    }

    public static class DuplicateSlugException extends RuntimeException {
        public DuplicateSlugException(String message) { super(message); }
    }
}

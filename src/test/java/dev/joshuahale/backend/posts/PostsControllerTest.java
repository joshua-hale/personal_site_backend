package dev.joshuahale.backend.posts;

import dev.joshuahale.backend.posts.controller.PostsController;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.joshuahale.backend.posts.dto.PostRequest;
import dev.joshuahale.backend.posts.dto.PostResponse;
import dev.joshuahale.backend.posts.dto.PostUpdateRequest;
import dev.joshuahale.backend.posts.service.PostsService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PostsController.class)
class PostsControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    @MockitoBean PostsService service; // note: package dev.joshuahale.backend.posts.service

    private PostResponse sample(Long id, String slug, String title) {
        PostResponse r = new PostResponse();
        r.setId(id);
        r.setSlug(slug);
        r.setTitle(title);
        r.setContent("Body");
        r.setHeroImage(null);
        r.setCreatedAt(OffsetDateTime.parse("2025-09-29T12:00:00Z"));
        r.setUpdatedAt(OffsetDateTime.parse("2025-09-29T12:00:00Z"));
        return r;
    }

    @Test
    void listAllOrdered_ok() throws Exception {
        Mockito.when(service.listAllOrdered()).thenReturn(List.of(sample(1L, "hello", "Hello")));

        mvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].slug").value("hello"))
                .andExpect(jsonPath("$[0].title").value("Hello"));
    }

    @Test
    void getById_ok() throws Exception {
        Mockito.when(service.getById(1L)).thenReturn(sample(1L, "hello", "Hello"));

        mvc.perform(get("/api/posts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.slug").value("hello"));
    }

    @Test
    void getBySlug_ok() throws Exception {
        Mockito.when(service.getBySlug("hello")).thenReturn(sample(1L, "hello", "Hello"));

        mvc.perform(get("/api/posts/slug/hello"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("hello"));
    }

    @Test
    void create_returns201_andLocationHeader_usesSlug() throws Exception {
        var req = new PostRequest();
        req.setTitle("Hello");
        req.setSlug("hello");
        req.setContent("Body");

        Mockito.when(service.create(any(PostRequest.class))).thenReturn(sample(42L, "hello", "Hello"));

        mvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/posts/slug/hello"))
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.slug").value("hello"));
    }

    @Test
    void create_validationError_400_whenMissingTitle() throws Exception {
        var req = new PostRequest();
        // missing title
        req.setSlug("hello");
        req.setContent("Body");

        mvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
        // Body shape depends on your @ControllerAdvice; we just assert 400 here.
    }

    @Test
    void patch_ok() throws Exception {
        var patch = new PostUpdateRequest();
        patch.setTitle("Updated title");

        Mockito.when(service.update(eq(1L), any(PostUpdateRequest.class)))
                .thenReturn(sample(1L, "hello", "Updated title"));

        mvc.perform(patch("/api/posts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(patch)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated title"));
    }

    @Test
    void delete_204_andDelegatesToService() throws Exception {
        mvc.perform(delete("/api/posts/1"))
                .andExpect(status().isNoContent());

        Mockito.verify(service).delete(1L);
    }
}

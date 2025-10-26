package dev.joshuahale.backend.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.joshuahale.backend.auth.controller.AuthController;
import dev.joshuahale.backend.auth.dto.AuthResponse;
import dev.joshuahale.backend.auth.dto.LoginRequest;
import dev.joshuahale.backend.auth.dto.SignupRequest;
import dev.joshuahale.backend.auth.service.AuthService;
import dev.joshuahale.backend.auth.service.SessionService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    @MockitoBean AuthService authService;
    @MockitoBean SessionService sessionService;

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private AuthResponse user(Long id, String email, String username, Set<String> roles) {
        // Matches your AuthResponse(userId, username, email, roles) constructor
        return new AuthResponse(id, username, email, roles);
    }

    private String signupBody(String email, String username, String password) throws Exception {
        // Build JSON without relying on DTO constructors/getters
        Map<String, Object> m = Map.of(
                "email", email,
                "username", username,
                "password", password
        );
        return json.writeValueAsString(m);
    }

    private String loginBody(String emailOrUsername, String password) throws Exception {
        Map<String, Object> m = Map.of(
                "emailOrUsername", emailOrUsername,
                "password", password
        );
        return json.writeValueAsString(m);
    }

    // -------------------------------------------------------------------------
    // Tests
    // -------------------------------------------------------------------------

    @Test
    void register_setsHttpOnlySecureSessionCookie_andReturnsCreatedUser() throws Exception {
        Mockito.when(authService.register(any(SignupRequest.class)))
                .thenReturn(user(7L, "alice@example.com", "alice", Set.of("USER")));
        Mockito.when(sessionService.create(eq(7L), any(), any()))
                .thenReturn("token123");

        var res = mvc.perform(post("/auth/register")
                        .header("User-Agent", "JUnit")
                        .header("X-Forwarded-For", "203.0.113.10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signupBody("alice@example.com", "alice", "StrongP@ss1")))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("sid=token123")))
                .andExpect(jsonPath("$.userId").value(7))
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.roles[0]").value("USER"))
                .andReturn();

        String setCookie = res.getResponse().getHeader(HttpHeaders.SET_COOKIE);
        assertThat(setCookie).contains("HttpOnly");
        assertThat(setCookie).contains("Secure");
        assertThat(setCookie).contains("SameSite=Lax");
        assertThat(setCookie).contains("Path=/");
        assertThat(setCookie).contains("Max-Age=");

        Mockito.verify(sessionService).create(eq(7L), eq("JUnit"), eq("203.0.113.10"));
    }

    @Test
    void login_ok_setsCookie_andReturnsUser() throws Exception {
        Mockito.when(authService.login(any(LoginRequest.class), any(), any()))
                .thenReturn(user(11L, "bob@example.com", "bob", Set.of("USER")));
        Mockito.when(sessionService.create(eq(11L), any(), any()))
                .thenReturn("tokXYZ");

        var res = mvc.perform(post("/auth/login")
                        .header("User-Agent", "JUnit-UA")
                        .header("X-Forwarded-For", "198.51.100.5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("bob@example.com", "CorrectHorseBatteryStaple1!")))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("sid=tokXYZ")))
                .andExpect(jsonPath("$.userId").value(11))
                .andExpect(jsonPath("$.email").value("bob@example.com"))
                .andExpect(jsonPath("$.username").value("bob"))
                .andExpect(jsonPath("$.roles[0]").value("USER"))
                .andReturn();

        String setCookie = res.getResponse().getHeader(HttpHeaders.SET_COOKIE);
        assertThat(setCookie).contains("HttpOnly");
        assertThat(setCookie).contains("Secure");
        assertThat(setCookie).contains("SameSite=Lax");
        assertThat(setCookie).contains("Path=/");

        Mockito.verify(authService).login(any(LoginRequest.class), eq("JUnit-UA"), eq("198.51.100.5"));
        Mockito.verify(sessionService).create(eq(11L), eq("JUnit-UA"), eq("198.51.100.5"));
    }

    @Test
    void logout_withCookie_revokesSession_andClearsCookie() throws Exception {
        mvc.perform(post("/auth/logout").cookie(new Cookie("sid", "abc123")))
                .andExpect(status().isNoContent())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("sid=")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Max-Age=0")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("HttpOnly")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Secure")));

        Mockito.verify(sessionService).revoke("abc123");
    }

    @Test
    void logout_withoutCookie_stillClearsCookie_andDoesNotRevoke() throws Exception {
        mvc.perform(post("/auth/logout"))
                .andExpect(status().isNoContent())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("sid=")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Max-Age=0")));

        Mockito.verify(sessionService, Mockito.never()).revoke(any());
    }

    @Test
    void me_withoutCookie_returns401() throws Exception {
        mvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void me_withInvalidToken_returns401() throws Exception {
        Mockito.when(authService.currentUser("badtoken")).thenReturn(Optional.empty());

        mvc.perform(get("/auth/me").cookie(new Cookie("sid", "badtoken")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void me_withValidToken_returnsUser() throws Exception {
        Mockito.when(authService.currentUser("goodtoken"))
                .thenReturn(Optional.of(user(99L, "carol@example.com", "carol", Set.of("ADMIN"))));

        mvc.perform(get("/auth/me").cookie(new Cookie("sid", "goodtoken")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(99))
                .andExpect(jsonPath("$.email").value("carol@example.com"))
                .andExpect(jsonPath("$.username").value("carol"))
                .andExpect(jsonPath("$.roles[0]").value("ADMIN"));
    }
}

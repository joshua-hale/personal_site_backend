package dev.joshuahale.backend.auth.controller;

import dev.joshuahale.backend.auth.dto.AuthResponse;
import dev.joshuahale.backend.auth.dto.LoginRequest;
import dev.joshuahale.backend.auth.dto.SignupRequest;
import dev.joshuahale.backend.auth.service.AuthService;
import dev.joshuahale.backend.auth.service.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final String SID_COOKIE = "sid";
    // Keep this aligned with SessionService TTL (default we used = 7 days)
    private static final Duration SESSION_TTL = Duration.ofDays(7);

    private final AuthService authService;
    private final SessionService sessionService;

    public AuthController(AuthService authService,
                          SessionService sessionService) {
        this.authService = authService;
        this.sessionService = sessionService;
    }

    // ---------------------------
    // Register
    // ---------------------------
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody SignupRequest request,
                                                 HttpServletRequest http) {
        AuthResponse user = authService.register(request);

        // Optional: auto-login on signup. If you want this, create a session here.
        String token = sessionService.create(user.getUserId(), userAgent(http), clientIp(http));
        ResponseCookie sid = sessionCookie(token, true);

        return ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.SET_COOKIE, sid.toString())
                .body(user);
    }

    // ---------------------------
    // Login
    // ---------------------------
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request,
                                              HttpServletRequest http) {
        // 1) Verify credentials (no session creation inside the service)
        AuthResponse user = authService.login(request, userAgent(http), clientIp(http));

        // 2) Create server-side session + set HttpOnly cookie
        String token = sessionService.create(user.getUserId(), userAgent(http), clientIp(http));
        ResponseCookie sid = sessionCookie(token, true);

        // If you implement CSRF (double-submit), also set a non-HttpOnly csrf cookie here

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, sid.toString())
                .body(user);
    }

    // ---------------------------
    // Logout current session
    // ---------------------------
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue(name = SID_COOKIE, required = false) String token) {
        if (token != null && !token.isBlank()) {
            sessionService.revoke(token);
        }
        // Clear cookie on client regardless
        ResponseCookie clear = clearSessionCookie();
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, clear.toString())
                .build();
    }

    // ---------------------------
    // Who am I
    // ---------------------------
    @GetMapping("/me")
    public ResponseEntity<AuthResponse> me(@CookieValue(name = SID_COOKIE, required = false) String token) {
        if (token == null || token.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Optional<AuthResponse> me = authService.currentUser(token);
        return me.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    // ---------------------------
    // Helpers
    // ---------------------------
    private ResponseCookie sessionCookie(String token, boolean secure) {
        return ResponseCookie.from(SID_COOKIE, token)
                .httpOnly(true)
                .secure(secure)                 // true in prod (HTTPS); allow false only for local dev over http
                .sameSite("Lax")                // or "Strict" if frontend and backend are same-site only
                .path("/")
                .maxAge(SESSION_TTL)
                .build();
    }

    private ResponseCookie clearSessionCookie() {
        return ResponseCookie.from(SID_COOKIE, "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ZERO)
                .build();
    }

    private String userAgent(HttpServletRequest req) {
        String ua = req.getHeader("User-Agent");
        return ua != null ? ua : "";
    }

    private String clientIp(HttpServletRequest req) {
        // If you’re behind a proxy, prefer X-Forwarded-For (and configure server to trust it)
        String xff = req.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            // XFF can be a comma-separated list. First is the original client IP.
            int comma = xff.indexOf(',');
            return comma > 0 ? xff.substring(0, comma).trim() : xff.trim();
        }
        String realIp = req.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) return realIp;
        return req.getRemoteAddr();
    }
}

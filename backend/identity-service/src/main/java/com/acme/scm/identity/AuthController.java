package com.acme.scm.identity;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/auth/register")
    @ResponseStatus(HttpStatus.CREATED)
    AuthService.TokenResponse register(@Valid @RequestBody Credentials request) {
        return authService.register(request.email(), request.password());
    }

    @PostMapping("/auth/login")
    AuthService.TokenResponse login(@Valid @RequestBody Credentials request) {
        return authService.login(request.email(), request.password());
    }

    @PostMapping("/auth/refresh")
    AuthService.TokenResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return authService.refresh(request.refreshToken());
    }

    @GetMapping("/admin/ping")
    @PreAuthorize("hasRole('ADMIN')")
    String adminPing() {
        return "admin";
    }

    @PostMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    AdminUserView createUser(@Valid @RequestBody AdminUserRequest request) {
        return authService.createUser(request.email(), request.password(), request.role());
    }

    record Credentials(@NotBlank @Email @Size(max = 320) String email,
                       @NotBlank @Size(min = 12, max = 128) String password) { }
    record RefreshRequest(@NotBlank @Size(max = 128) String refreshToken) { }
    record AdminUserRequest(@NotBlank @Email @Size(max = 320) String email,
                            @NotBlank @Size(min = 12, max = 128) String password,
                            @jakarta.validation.constraints.NotNull UserRole role) { }
    record AdminUserView(java.util.UUID id, String email, UserRole role) { }
}

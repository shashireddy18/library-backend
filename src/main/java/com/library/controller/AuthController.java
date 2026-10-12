package com.library.controller;

import com.library.dto.LoginRequest;
import com.library.dto.LoginResponse;
import com.library.entity.AppUser;
import com.library.exception.UnauthorizedException;
import com.library.repository.AppUserRepository;
import com.library.security.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AppUserRepository users;
    private final PasswordEncoder encoder;
    private final JwtUtil jwtUtil;

    public AuthController(AppUserRepository users, PasswordEncoder encoder, JwtUtil jwtUtil) {
        this.users = users;
        this.encoder = encoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        AppUser u = users.findByUsername(req.username().trim())
                .orElseThrow(() -> new UnauthorizedException("Invalid username or password"));
        if (!encoder.matches(req.password(), u.getPassword())) {
            throw new UnauthorizedException("Invalid username or password");
        }
        return new LoginResponse(jwtUtil.generateToken(u.getUsername(), u.getRole()),
                u.getUsername(), u.getFullName(), u.getRole());
    }
}

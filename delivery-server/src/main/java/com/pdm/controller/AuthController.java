package com.pdm.controller;

import com.pdm.common.ApiResult;
import com.pdm.dto.LoginRequest;
import com.pdm.dto.LoginResponse;
import com.pdm.dto.PasswordRequest;
import com.pdm.dto.RecheckRequest;
import com.pdm.security.CaptchaService;
import com.pdm.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CaptchaService captchaService;

    @GetMapping("/captcha")
    public ApiResult<Map<String, String>> captcha() {
        CaptchaService.Captcha c = captchaService.generate();
        return ApiResult.ok(Map.of("captchaId", c.captchaId(), "svg", c.svg()));
    }

    @PostMapping("/login")
    public ApiResult<LoginResponse> login(@Valid @RequestBody LoginRequest req, HttpServletRequest http) {
        return ApiResult.ok(authService.login(req, http));
    }

    @PostMapping("/logout")
    public ApiResult<Void> logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        String token = authorization != null && authorization.startsWith("Bearer ") ? authorization.substring(7) : null;
        authService.logout(token);
        return ApiResult.ok();
    }

    @GetMapping("/me")
    public ApiResult<LoginResponse.UserInfo> me() {
        return ApiResult.ok(authService.me());
    }

    @PostMapping("/password")
    public ApiResult<Void> changePassword(@Valid @RequestBody PasswordRequest req) {
        authService.changePassword(req.getOldPassword(), req.getNewPassword());
        return ApiResult.ok();
    }

    @PostMapping("/recheck")
    public ApiResult<Map<String, String>> recheck(@Valid @RequestBody RecheckRequest req) {
        return ApiResult.ok(Map.of("recheckToken", authService.recheck(req.getPassword())));
    }
}
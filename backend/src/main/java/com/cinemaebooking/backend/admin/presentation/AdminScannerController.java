package com.cinemaebooking.backend.admin.presentation;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Admin scanner page - serve HTML cho trang QR check-in.
 * Truy cap: GET /api/v1/admin/scanner
 * Trang HTML naу công khai (JWT nhập bên trong trang), nhưng API bên dưới cần ADMIN.
 */
@RestController
@RequestMapping("/api/v1/admin")
public class AdminScannerController {

    private static final String SCANNER_HTML_PATH = "static/admin/scanner/index.html";

    @GetMapping(value = "/scanner", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> getScannerPage() throws IOException {
        var resource = new ClassPathResource(SCANNER_HTML_PATH);
        byte[] htmlBytes = resource.getInputStream().readAllBytes();
        String html = new String(htmlBytes, StandardCharsets.UTF_8);
        return ResponseEntity.ok(html);
    }
}

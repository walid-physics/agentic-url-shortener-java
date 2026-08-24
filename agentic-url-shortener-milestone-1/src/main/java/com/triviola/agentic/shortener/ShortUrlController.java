package com.triviola.agentic.shortener;

import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
public class ShortUrlController {
    private final ShortUrlService service;

    public ShortUrlController(ShortUrlService service) { this.service = service; }

    @PostMapping("/api/urls")
    @ResponseStatus(HttpStatus.CREATED)
    public ShortUrlResponse create(@Valid @RequestBody CreateShortUrlRequest request) {
        return ShortUrlResponse.from(service.create(request));
    }

    @GetMapping("/r/{code}")
    public ResponseEntity<Void> redirect(@PathVariable String code) {
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(service.resolve(code).getOriginalUrl())).build();
    }

    @GetMapping("/api/urls/{code}")
    public ShortUrlResponse get(@PathVariable String code) { return ShortUrlResponse.from(service.get(code)); }

    @GetMapping("/api/urls/{code}/analytics")
    public UrlAnalyticsResponse analytics(@PathVariable String code) { return UrlAnalyticsResponse.from(service.get(code)); }

    @DeleteMapping("/api/urls/{code}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String code) { service.delete(code); }
}

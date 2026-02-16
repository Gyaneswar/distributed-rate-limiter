package com.gyaneswar.distributed_rate_limiter.controller;

import com.gyaneswar.distributed_rate_limiter.RateLimiterService.RateLimiterService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class RateLimiterController {

    private final RateLimiterService rateLimiterService;

    public RateLimiterController(@Qualifier("tokenBucket") RateLimiterService rateLimiterService) {
        this.rateLimiterService = rateLimiterService;
    }

    @GetMapping("/resource/{userId}")
    public ResponseEntity<String> accessResource(@PathVariable String userId) {
        String key = "rate:user:" + userId;

        if (rateLimiterService.isAllowed(key)) {
            return ResponseEntity.ok("Request allowed for user: " + userId);
        }

        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body("Rate limit exceeded for user: " + userId);
    }
}

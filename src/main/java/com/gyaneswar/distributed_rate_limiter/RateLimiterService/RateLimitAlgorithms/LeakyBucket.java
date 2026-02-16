package com.gyaneswar.distributed_rate_limiter.RateLimiterService.RateLimitAlgorithms;


import com.gyaneswar.distributed_rate_limiter.RateLimiterService.RateLimiterService;
import org.springframework.stereotype.Service;

@Service
public class LeakyBucket implements RateLimiterService {


    public boolean isAllowed(String key, int maxTokens, double refillRate, int requested) {
        return false;
    }

    @Override
    public boolean isAllowed(String key) {
        return false;
    }
}

package com.gyaneswar.distributed_rate_limiter.RateLimiterService;

public interface RateLimiterService {
    boolean isAllowed(String key);
}

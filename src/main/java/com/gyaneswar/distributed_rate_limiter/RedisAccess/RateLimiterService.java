package com.gyaneswar.distributed_rate_limiter.RedisAccess;

public interface RateLimiterService {
    boolean isAllowed(String key);
}

package com.gyaneswar.distributed_rate_limiter.RedisAccess;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RateLimiterService {

    private final StringRedisTemplate redisTemplate;
    private final DefaultRedisScript<Long> tokenBucketScript;

    public RateLimiterService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.tokenBucketScript = new DefaultRedisScript<>();
        this.tokenBucketScript.setLocation(new ClassPathResource("scripts/token_bucket.lua"));
        this.tokenBucketScript.setResultType(Long.class);
    }

    /**
     * Attempts to acquire a token for the given key.
     *
     * @param key        unique identifier (e.g. "rate:user:123" or "rate:ip:10.0.0.1")
     * @param maxTokens  maximum tokens in the bucket
     * @param refillRate tokens added per second
     * @param requested  number of tokens to consume
     * @return true if the request is allowed, false if rate-limited
     */
    public boolean isAllowed(String key, int maxTokens, double refillRate, int requested) {
        long nowMillis = System.currentTimeMillis();
        double nowSeconds = nowMillis / 1000.0;

        Long result = redisTemplate.execute(
                tokenBucketScript,
                List.of(key),
                String.valueOf(maxTokens),
                String.valueOf(refillRate),
                String.valueOf(nowSeconds),
                String.valueOf(requested)
        );

        return result != null && result == 1L;
    }

    /**
     * Convenience overload — consumes 1 token with default bucket settings.
     * 10 max tokens, refill rate of 1 token/second.
     */
    public boolean isAllowed(String key) {
        return isAllowed(key, 10, 1.0, 1);
    }
}

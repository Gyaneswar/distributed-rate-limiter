package com.gyaneswar.distributed_rate_limiter.RateLimiterService.RateLimitAlgorithms;

import com.gyaneswar.distributed_rate_limiter.RateLimiterService.RateLimiterService;
import com.gyaneswar.distributed_rate_limiter.dao.RedisDao;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TokenBucket implements RateLimiterService {

    private final RedisDao redisDao;
    private final DefaultRedisScript<Long> tokenBucketScript;

    @Value("${rate-limiter.max-tokens}")
    private int maxTokens;

    @Value("${rate-limiter.refill-rate}")
    private double refillRate;

    @Value("${rate-limiter.default-requested}")
    private int defaultRequested;

    public TokenBucket(RedisDao redisDao) {
        this.redisDao = redisDao;
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

        Long result = redisDao.executeScript(
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
     * Convenience overload — consumes tokens with configured default bucket settings.
     */
    public boolean isAllowed(String key) {
        return isAllowed(key, maxTokens, refillRate, defaultRequested);
    }
}

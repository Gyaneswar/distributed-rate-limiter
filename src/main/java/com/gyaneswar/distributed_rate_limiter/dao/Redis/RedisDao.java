package com.gyaneswar.distributed_rate_limiter.dao.Redis;

import com.gyaneswar.distributed_rate_limiter.dao.CacheDao;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RedisDao implements CacheDao {

    private final StringRedisTemplate stringRedisTemplate;

    public RedisDao(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public <T> T executeScript(RedisScript<T> script, List<String> keys, String... args) {
        return stringRedisTemplate.execute(script, keys, (Object[]) args);
    }
}

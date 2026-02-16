package com.gyaneswar.distributed_rate_limiter.dao;

import org.springframework.data.redis.core.script.RedisScript;

import java.util.List;

public interface CacheDao {

    <T> T executeScript(RedisScript<T> script, List<String> keys, String... args);
}

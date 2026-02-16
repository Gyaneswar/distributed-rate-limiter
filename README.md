# Distributed Rate Limiter

A high-performance distributed rate limiting library built with Spring Boot and Redis. Uses Lua scripting for atomic operations to ensure correctness across multiple application instances.

## Tech Stack

- **Java 25** + **Spring Boot 4.0.2**
- **Redis** (via Spring Data Redis + Lettuce client)
- **Lua scripting** for atomic rate limit checks

## Project Structure

```
src/main/java/com/gyaneswar/distributed_rate_limiter/
├── RateLimiterService/
│   ├── RateLimiterService.java          # Interface for rate limiter strategies
│   └── RateLimitAlgorithms/
│       ├── TokenBucket.java             # Token Bucket algorithm implementation
│       └── LeakyBucket.java             # Leaky Bucket (WIP)
├── dao/
│   ├── CacheDao.java                    # Interface for cache operations
│   └── Redis/
│       └── RedisDao.java                # Redis implementation of CacheDao
├── config/
│   └── RedisConfig.java                 # Redis bean configuration
├── controller/
│   └── RateLimiterController.java       # REST endpoint for testing
└── DistributedRateLimiterApplication.java

src/main/resources/
├── scripts/
│   └── token_bucket.lua                 # Lua script for atomic token bucket ops
└── application.properties
```

## How It Works

The rate limiter follows a **strategy pattern** — `RateLimiterService` is the interface, and each algorithm (Token Bucket, Leaky Bucket, etc.) is a separate implementation.

### Token Bucket (implemented)

Each user gets a bucket with a fixed capacity. Every request consumes a token. Tokens refill at a steady rate over time. If the bucket is empty, the request is denied.

All of this runs as a single Lua script on Redis, making it **atomic** — no race conditions even with multiple app instances hitting the same Redis.

**Redis data structure per user:**
| Field | Example | Description |
|---|---|---|
| `tokens` | `8.5` | Remaining tokens |
| `last_refill` | `1739712000.1` | Timestamp of last refill (seconds) |

Keys auto-expire after inactivity to prevent memory leaks.

### Leaky Bucket (WIP)

Stub implementation in place. Coming soon.

## Configuration

All rate limiter settings are in `application.properties`:

```properties
# Redis
spring.data.redis.host=localhost
spring.data.redis.port=6379

# Rate Limiter defaults
rate-limiter.max-tokens=10
rate-limiter.refill-rate=1.0
rate-limiter.default-requested=1
```

## Running

### Prerequisites
- Java 25
- Redis running on `localhost:6379` (e.g. via Docker)

```bash
docker run -d --name my-redis -p 6379:6379 redis:latest
```

### Start the app

```bash
./mvnw spring-boot:run
```

### Test it

```bash
# Should return 200 OK
curl http://localhost:8080/api/resource/user1

# Spam it — after 10 rapid requests you'll get 429 Too Many Requests
for i in {1..15}; do curl -s -o /dev/null -w "%{http_code}\n" http://localhost:8080/api/resource/user1; done
```

## Current State

### What's done
- **Token Bucket algorithm** — fully implemented with Redis + Lua scripting for atomic, distributed rate limiting
- **Strategy Pattern architecture** — `RateLimiterService` interface allows plugging in different algorithms without changing the controller or any downstream code
- **Configurable via properties** — bucket capacity, refill rate, and token cost are all externalized to `application.properties`
- **Auto-expiring keys** — stale rate limit buckets are cleaned up automatically by Redis TTL, preventing memory leaks
- **REST API** — test endpoint at `GET /api/resource/{userId}` returns `200 OK` or `429 Too Many Requests`

### Design decisions
- **Lua scripting over transactions** — a single Lua script handles read-compute-write atomically on the Redis server, avoiding round-trip overhead and race conditions across distributed instances
- **Time-based token refill** — tokens are calculated on each request using elapsed time rather than a background refill job, keeping the system stateless and efficient
- **Interface-driven design** — adding a new algorithm is as simple as implementing `RateLimiterService` and annotating it with `@Service`

## Future Goals

1. **More rate limiting algorithms**
   - Leaky Bucket (stub in place)
   - Fixed Window Counter
   - Sliding Window Log
   - Sliding Window Counter

2. **Benchmark suite** — side-by-side performance comparison of all algorithms under identical load conditions, measuring throughput, latency (p50/p95/p99), and Redis memory usage

3. **Production-readiness improvements**
   - Rate limit response headers (`X-RateLimit-Remaining`, `X-RateLimit-Reset`)
   - Spring Boot Starter packaging for easy integration into other projects
   - Support for multiple rate limit tiers (e.g. free vs premium users)
   - Configurable key strategies (per-user, per-IP, per-endpoint)

## Roadmap

- [x] Token Bucket algorithm
- [ ] Leaky Bucket algorithm
- [ ] Fixed Window Counter
- [ ] Sliding Window Log
- [ ] Sliding Window Counter
- [ ] Benchmark comparing all algorithms
- [ ] Rate limit response headers
- [ ] Spring Boot Starter packaging

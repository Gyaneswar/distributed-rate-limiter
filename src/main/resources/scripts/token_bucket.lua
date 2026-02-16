local key = KEYS[1]
local max_tokens = tonumber(ARGV[1])
local refill_rate = tonumber(ARGV[2])
local now = tonumber(ARGV[3])
local requested = tonumber(ARGV[4])

local bucket = redis.call('hmget', key, 'tokens', 'last_refill')
local tokens = tonumber(bucket[1])
local last_refill = tonumber(bucket[2])

if tokens == nil then
    tokens = max_tokens
    last_refill = now
end

local elapsed = math.max(0, now - last_refill)
tokens = math.min(max_tokens, tokens + elapsed * refill_rate)

local allowed = 0
if tokens >= requested then
    tokens = tokens - requested
    allowed = 1
end

redis.call('hmset', key, 'tokens', tokens, 'last_refill', now)
redis.call('pexpire', key, math.ceil(max_tokens / refill_rate) * 1000 * 2)

return allowed

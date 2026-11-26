package com.ape.smarthedge_risk_hub.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;

/**
 * 高频主数据缓存（Redis）。
 *
 * 设计要点：
 * 1. 值统一用 JSON 字符串存储，避免 JDK 序列化的兼容问题；
 * 2. Redis 不可用时不影响业务，自动回退数据库，并在 60 秒内不再重试，
 *    避免每次请求都等待连接超时。
 */
@Component
public class HotDataCache {

    private static final Logger log = LoggerFactory.getLogger(HotDataCache.class);

    /** 失败熔断时间窗：60 秒内直接走数据库 */
    private static final long SKIP_MILLIS = 60_000L;

    private volatile long skipUntil = 0L;

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public HotDataCache(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /** 读取缓存，未命中或 Redis 异常返回 null */
    public <T> T get(String key, TypeReference<T> type) {
        if (!available()) {
            return null;
        }
        try {
            String json = redisTemplate.opsForValue().get(key);
            return json == null ? null : objectMapper.readValue(json, type);
        } catch (Exception e) {
            markUnavailable(e);
            return null;
        }
    }

    /** 写入缓存（带过期时间） */
    public void put(String key, Object value, Duration ttl) {
        if (!available()) {
            return;
        }
        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(value), ttl);
        } catch (Exception e) {
            markUnavailable(e);
        }
    }

    /** 删除缓存（主数据变更时调用） */
    public void evict(String... keys) {
        if (!available() || keys == null || keys.length == 0) {
            return;
        }
        try {
            redisTemplate.delete(Arrays.asList(keys));
        } catch (Exception e) {
            markUnavailable(e);
        }
    }

    private boolean available() {
        return System.currentTimeMillis() >= skipUntil;
    }

    private void markUnavailable(Exception e) {
        skipUntil = System.currentTimeMillis() + SKIP_MILLIS;
        log.warn("Redis 不可用，60 秒内回退数据库：{}", e.getMessage());
    }
}

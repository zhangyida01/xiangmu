package com.pdm.security;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内存态令牌仓库：图形验证码、登出黑名单、二次校验令牌。
 * 单机部署足够使用；如将来多实例部署需替换为 Redis。
 */
@Component
public class TokenStore {

    private final Map<String, Entry> store = new ConcurrentHashMap<>();

    public void put(String key, String value, long ttlSeconds) {
        store.put(key, new Entry(value, System.currentTimeMillis() + ttlSeconds * 1000L));
    }

    public String get(String key) {
        Entry e = store.get(key);
        if (e == null) {
            return null;
        }
        if (e.expireAt < System.currentTimeMillis()) {
            store.remove(key);
            return null;
        }
        return e.value;
    }

    /** 取出并立即失效（一次性使用） */
    public String take(String key) {
        String v = get(key);
        if (v != null) {
            store.remove(key);
        }
        return v;
    }

    public void remove(String key) {
        store.remove(key);
    }

    public boolean contains(String key) {
        return get(key) != null;
    }

    @Scheduled(fixedDelay = 600_000L)
    public void cleanup() {
        long now = System.currentTimeMillis();
        store.entrySet().removeIf(e -> e.getValue().expireAt < now);
    }

    private record Entry(String value, long expireAt) {
    }
}
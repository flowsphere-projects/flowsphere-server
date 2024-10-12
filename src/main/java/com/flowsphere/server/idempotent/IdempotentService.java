package com.flowsphere.server.idempotent;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class IdempotentService {

    @Autowired
    private RedisTemplate redisTemplate;

    public void idempotent(String bizFuc, String uniqueId, long timeout, TimeUnit timeUnit) {
        String key = bizFuc + ":id:" + uniqueId;
        String randomValue = UUID.randomUUID().toString().replace("-", "");
        ThreadLocalUtils.setIdempotent(new IdempotentKV(key, randomValue));
        redisTemplate.opsForValue().set(key, randomValue, timeout, timeUnit);
    }


    public void delIdempotent() {
        IdempotentKV idempotent = ThreadLocalUtils.getIdempotent();
        Boolean result = redisTemplate.delete(idempotent.getKey());
        if (!result) {
            log.error("幂等删除失败 key={}", idempotent.getKey());
            throw new RuntimeException("幂等删除失败");
        }
        ThreadLocalUtils.clear();
    }


}

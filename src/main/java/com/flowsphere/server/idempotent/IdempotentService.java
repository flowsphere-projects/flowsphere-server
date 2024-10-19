package com.flowsphere.server.idempotent;


import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class IdempotentService {

    @Autowired
    private RedissonClient redissonClient;

    @SneakyThrows
    public Boolean idempotent(String bizFuc, String uniqueId, long idempotentTime, TimeUnit timeUnit) {
        String key = bizFuc + ":id:" + uniqueId;
        RLock lock = redissonClient.getLock(key);
        boolean result = lock.tryLock(0, idempotentTime, timeUnit);
        if (result) {
            ThreadLocalUtils.setIdempotent(key);
        }
        return result;
    }


    public void delIdempotent() {
        String key = ThreadLocalUtils.getIdempotent();
        RLock lock = redissonClient.getLock(key);
        lock.unlock();
        ThreadLocalUtils.clear();
    }


}

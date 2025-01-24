package com.flowsphere.server.heartbeat;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SimpleHeartbeatReceiver implements HeartbeatReceiver {

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public void receive(String applicationName, String ip) {
        RScoredSortedSet<String> heartbeatSet = redissonClient.getScoredSortedSet(applicationName);
        heartbeatSet.add(System.currentTimeMillis() / 1000, ip);
    }

}

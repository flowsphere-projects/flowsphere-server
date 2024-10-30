package com.flowsphere.server.heartbeat;

import com.flowsphere.server.config.HeartbeatProperties;
import com.flowsphere.server.service.ProviderService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class HeartbeatManager implements InitializingBean {

    private static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(1);

    @Autowired
    private HeartbeatReceiver heartbeatReceiver;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private ProviderService providerService;

    @Autowired
    private HeartbeatProperties heartbeatProperties;

    public void receive(String applicationName, String ip) {
        heartbeatReceiver.receive(applicationName, ip);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        SCHEDULER.scheduleWithFixedDelay(new HeartbeatCheck(redissonClient, heartbeatProperties.getTimeoutThreshold(), providerService, heartbeatProperties),
                heartbeatProperties.getDelay(), heartbeatProperties.getDelay(), TimeUnit.SECONDS);
    }

}

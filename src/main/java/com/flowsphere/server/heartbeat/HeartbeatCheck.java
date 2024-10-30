package com.flowsphere.server.heartbeat;

import com.flowsphere.server.config.HeartbeatProperties;
import com.flowsphere.server.entity.Provider;
import com.flowsphere.server.service.ProviderService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class HeartbeatCheck implements Runnable {

    private final RedissonClient redissonClient;

    private final long timeoutThreshold;

    private final ProviderService providerService;

    private final HeartbeatProperties heartbeatProperties;


    public HeartbeatCheck(RedissonClient redissonClient, long timeoutThreshold, ProviderService providerService, HeartbeatProperties heartbeatProperties) {
        this.redissonClient = redissonClient;
        this.timeoutThreshold = timeoutThreshold;
        this.providerService = providerService;
        this.heartbeatProperties = heartbeatProperties;
    }

    @Override
    public void run() {
        Page<Provider> page = providerService.findByName(null, null, PageRequest.of(0, heartbeatProperties.getPageSize()));
        while (!CollectionUtils.isEmpty(page.getContent())) {
            for (Provider provider : page.getContent()) {
                try {
                    RScoredSortedSet<String> heartbeatSet = redissonClient.getScoredSortedSet(provider.getName());
                    long currentTime = (System.currentTimeMillis() / 1000);
                    long minScore = (currentTime - timeoutThreshold);
                    heartbeatSet.removeRangeByScore(0, true, minScore, true);
                    List<String> ipList = heartbeatSet.stream().collect(Collectors.toList());

                    if (!CollectionUtils.isEmpty(ipList)) {
                        providerService.deleteProviderInstantByProviderNameAndIpNotIn(provider.getName(), ipList);
                        if (heartbeatSet.size() >= 1 && provider.getStatus() == 0) {
                            provider.setStatus(1);
                            providerService.saveProvider(provider);
                        }
                    } else {
                        provider.setStatus(0);
                        providerService.saveProvider(provider);
                    }
                } catch (Exception e) {
                    log.error("[HeartbeatCheck] handler error providerName={}", provider.getName(), e);
                }
            }
            page = providerService.findByName(null, null, PageRequest.of(page.getNumber() + 1, heartbeatProperties.getPageSize()));
        }
    }

}

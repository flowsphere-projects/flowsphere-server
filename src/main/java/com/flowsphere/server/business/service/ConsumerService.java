package com.flowsphere.server.business.service;

import com.flowsphere.server.business.entity.Consumer;
import com.flowsphere.server.business.entity.ConsumerInstant;
import com.flowsphere.server.business.repository.ConsumerInstantRepository;
import com.flowsphere.server.business.repository.ConsumerRepository;
import com.flowsphere.server.business.request.ConsumerRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ConsumerService {

    @Autowired
    private ConsumerRepository consumerRepository;

    @Autowired
    private ConsumerInstantRepository consumerInstantRepository;

    public void save(ConsumerRequest consumerRequest) {
        Map<String, List<String>> dependOnInterfaceList = consumerRequest.getDependOnInterfaceList();
        dependOnInterfaceList.entrySet().forEach(k -> {
            Consumer knownConsumer = consumerRepository.findByNameAndProviderName(consumerRequest.getApplicationName(), k.getKey());
            if (Objects.isNull(knownConsumer)) {
                knownConsumer = consumerRepository.save(new Consumer()
                        .setName(consumerRequest.getApplicationName())
                        .setStatus(1)
                        .setProviderName(k.getKey())
                );
                List<ConsumerInstant> knownConsumerUrlList = consumerInstantRepository.findByConsumerId(knownConsumer.getId());
                List<String> reportConsumerUrlList = k.getValue();

                if (CollectionUtils.isEmpty(knownConsumerUrlList)) {
                    batchSaveConsumerInstant(reportConsumerUrlList, knownConsumer.getId());
                } else {
                    List<ConsumerInstant> waitRemoveList = knownConsumerUrlList.stream()
                            .filter(filterKnownConsumer -> !reportConsumerUrlList.contains(filterKnownConsumer.getUrl()))
                            .collect(Collectors.toList());
                    List<String> waitAddList = reportConsumerUrlList.stream()
                            .filter(url -> !knownConsumerUrlList.stream().anyMatch(filterKnowConsumer -> filterKnowConsumer.getUrl().equals(url)))
                            .collect(Collectors.toList());
                    consumerInstantRepository.deleteAll(waitRemoveList);
                    batchSaveConsumerInstant(waitAddList, knownConsumer.getId());
                }
            }
        });
    }


    private void batchSaveConsumerInstant(List<String> waitAddList, int consumerId) {
        List<ConsumerInstant> addList = waitAddList.stream().map(url -> {
            return new ConsumerInstant()
                    .setConsumerId(consumerId)
                    .setUrl(url)
                    .setStatus(1)
                    .setLastUpdateTime(LocalDateTime.now());
        }).collect(Collectors.toList());
        consumerInstantRepository.saveAll(addList);
    }

}

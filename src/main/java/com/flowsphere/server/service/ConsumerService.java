package com.flowsphere.server.service;

import com.flowsphere.server.entity.*;
import com.flowsphere.server.enums.ConsumerInstanceStatusEnum;
import com.flowsphere.server.enums.ConsumerProviderStatusEnum;
import com.flowsphere.server.enums.ConsumerStatusEnum;
import com.flowsphere.server.repository.*;
import com.flowsphere.server.request.ConsumerRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private ConsumerInstanceRepository consumerInstantRepository;

    @Autowired
    private ConsumerProviderRepository consumerProviderRepository;

    @Autowired
    private ProviderRepository providerRepository;

    @Autowired
    private ProviderInstanceRepository providerInstantRepository;


    public Page<ConsumerProvider> findByConsumerIdOrProviderIp(int consumerId, String providerIp, Pageable pageable) {
        Specification<ConsumerProvider> specification = new Specification<ConsumerProvider>() {
            @Override
            public Predicate toPredicate(Root<ConsumerProvider> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                Predicate predicate = criteriaBuilder.equal(root.get("consumerId").as(Integer.class), consumerId);
                predicates.add(predicate);
                if (!StringUtils.isEmpty(providerIp)) {
                    predicate = criteriaBuilder.equal(root.get("providerIp").as(String.class), providerIp);
                    predicates.add(predicate);
                }
                if (predicates.size() == 0) {
                    return null;
                }
                Predicate[] p = new Predicate[predicates.size()];
                return criteriaBuilder.and(predicates.toArray(p));
            }
        };

        return consumerProviderRepository.findAll(specification, pageable);
    }

    public void save(ConsumerRequest consumerRequest) {
        Map<String, List<String>> dependOnInterfaceList = consumerRequest.getDependOnInterfaceList();
        dependOnInterfaceList.entrySet().forEach(k -> {
            Consumer knownConsumer = consumerRepository.findByNameAndProviderName(consumerRequest.getApplicationName(), k.getKey());
            if (Objects.isNull(knownConsumer)) {
                knownConsumer = consumerRepository.save(new Consumer()
                        .setName(consumerRequest.getApplicationName())
                        .setStatus(ConsumerStatusEnum.NORMAL.getStatus())
                        .setProviderName(k.getKey())
                );
                List<ConsumerInstance> knownConsumerUrlList = consumerInstantRepository.findByConsumerId(knownConsumer.getId());
                List<String> reportConsumerUrlList = k.getValue();

                if (CollectionUtils.isEmpty(knownConsumerUrlList)) {
                    batchSaveConsumerInstant(reportConsumerUrlList, knownConsumer);
                } else {
                    List<ConsumerInstance> waitRemoveList = knownConsumerUrlList.stream()
                            .filter(filterKnownConsumer -> !reportConsumerUrlList.contains(filterKnownConsumer.getUrl()))
                            .collect(Collectors.toList());
                    List<String> waitAddList = reportConsumerUrlList.stream()
                            .filter(url -> !knownConsumerUrlList.stream().anyMatch(filterKnowConsumer -> filterKnowConsumer.getUrl().equals(url)))
                            .collect(Collectors.toList());
                    consumerInstantRepository.deleteAll(waitRemoveList);
                    batchSaveConsumerInstant(waitAddList, knownConsumer);
                }
            }
            batchSaveConsumerProvider(k.getKey(), knownConsumer);
        });
    }

    private void batchSaveConsumerProvider(String applicationName, Consumer knownConsumer) {
        Provider provider = providerRepository.findByName(applicationName);
        if (Objects.isNull(provider)) {
            return;
        }
        List<ProviderInstance> providerInstantList = providerInstantRepository.findByProviderId(provider.getId());
        List<ConsumerProvider> existConsumerProviderList = consumerProviderRepository.findByConsumerIdAndProviderId(knownConsumer.getId(), provider.getId());
        if (!CollectionUtils.isEmpty(existConsumerProviderList)) {
            providerInstantList = providerInstantList.stream().filter(providerInstant ->
                            existConsumerProviderList
                                    .stream()
                                    .anyMatch(consumerProvider -> !providerInstant.getIp().equals(consumerProvider.getProviderIp()))
                    )
                    .collect(Collectors.toList());
        }
        List<ConsumerProvider> consumerProviderList = new ArrayList<>(providerInstantList.size());
        for (ProviderInstance providerInstant : providerInstantList) {
            ConsumerProvider consumerProvider = new ConsumerProvider();
            consumerProvider.setPort(providerInstant.getPort());
            consumerProvider.setConsumerId(knownConsumer.getId());
            consumerProvider.setProviderId(provider.getId());
            consumerProvider.setProviderIp(providerInstant.getIp());
            consumerProvider.setStatus(ConsumerProviderStatusEnum.NORMAL.getStatus());
            consumerProvider.setLastUpdateTime(LocalDateTime.now());
            consumerProviderList.add(consumerProvider);
        }
        consumerProviderRepository.saveAll(consumerProviderList);
    }


    private void batchSaveConsumerInstant(List<String> waitAddList, Consumer consumer) {
        List<ConsumerInstance> addList = waitAddList.stream().map(url -> {
            return new ConsumerInstance()
                    .setConsumer(consumer)
                    .setUrl(url)
                    .setStatus(ConsumerInstanceStatusEnum.NORMAL.getStatus())
                    .setLastUpdateTime(LocalDateTime.now());
        }).collect(Collectors.toList());
        consumerInstantRepository.saveAll(addList);
    }


    public Page<ConsumerInstance> findByProviderNameAndUrlAndConsumerName(String providerName, String url, String consumerName, Pageable pageable) {
        Specification<ConsumerInstance> specification = new Specification<ConsumerInstance>() {
            @Override
            public Predicate toPredicate(Root<ConsumerInstance> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                if (!StringUtils.isEmpty(url)) {
                    Predicate predicate = criteriaBuilder.equal(root.get("url").as(String.class), url);
                    predicates.add(predicate);
                }
                if (!StringUtils.isEmpty(consumerName)) {
                    Predicate predicate = criteriaBuilder.equal(root.join("consumer").get("name").as(String.class), consumerName);
                    predicates.add(predicate);
                }
                if (predicates.size() == 0) {
                    return null;
                }
                Predicate[] p = new Predicate[predicates.size()];
                return criteriaBuilder.and(predicates.toArray(p));
            }
        };

        return consumerInstantRepository.findAll(specification, pageable);
    }

    public Page<Consumer> findByProviderNameAndConsumerName(String providerName, String consumerName, Pageable pageable) {
        Specification<Consumer> specification = new Specification<Consumer>() {
            @Override
            public Predicate toPredicate(Root<Consumer> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                if (!StringUtils.isEmpty(consumerName)) {
                    Predicate predicate = criteriaBuilder.equal(root.get("name").as(String.class), consumerName);
                    predicates.add(predicate);
                }
                if (!StringUtils.isEmpty(providerName)) {
                    Predicate predicate = criteriaBuilder.equal(root.get("providerName").as(String.class), providerName);
                    predicates.add(predicate);
                }
                if (predicates.size() == 0) {
                    return null;
                }
                Predicate[] p = new Predicate[predicates.size()];
                return criteriaBuilder.and(predicates.toArray(p));
            }
        };
        return consumerRepository.findAll(specification, pageable);
    }

}

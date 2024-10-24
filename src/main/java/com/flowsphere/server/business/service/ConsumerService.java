package com.flowsphere.server.business.service;

import com.flowsphere.server.business.entity.Consumer;
import com.flowsphere.server.business.entity.ConsumerInstant;
import com.flowsphere.server.business.repository.ConsumerInstantRepository;
import com.flowsphere.server.business.repository.ConsumerRepository;
import com.flowsphere.server.business.request.ConsumerRequest;
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
                    batchSaveConsumerInstant(reportConsumerUrlList, knownConsumer);
                } else {
                    List<ConsumerInstant> waitRemoveList = knownConsumerUrlList.stream()
                            .filter(filterKnownConsumer -> !reportConsumerUrlList.contains(filterKnownConsumer.getUrl()))
                            .collect(Collectors.toList());
                    List<String> waitAddList = reportConsumerUrlList.stream()
                            .filter(url -> !knownConsumerUrlList.stream().anyMatch(filterKnowConsumer -> filterKnowConsumer.getUrl().equals(url)))
                            .collect(Collectors.toList());
                    consumerInstantRepository.deleteAll(waitRemoveList);
                    batchSaveConsumerInstant(waitAddList, knownConsumer);
                }
            }
        });
    }


    private void batchSaveConsumerInstant(List<String> waitAddList, Consumer consumer) {
        List<ConsumerInstant> addList = waitAddList.stream().map(url -> {
            return new ConsumerInstant()
                    .setConsumer(consumer)
                    .setUrl(url)
                    .setStatus(1)
                    .setLastUpdateTime(LocalDateTime.now());
        }).collect(Collectors.toList());
        consumerInstantRepository.saveAll(addList);
    }


    public Page<ConsumerInstant> findByProviderNameAndUrlAndConsumerName(String providerName, String url, String consumerName, Pageable pageable) {
        Specification<ConsumerInstant> specification = new Specification<ConsumerInstant>() {
            @Override
            public Predicate toPredicate(Root<ConsumerInstant> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
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

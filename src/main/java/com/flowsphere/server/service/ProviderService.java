package com.flowsphere.server.service;

import com.flowsphere.server.entity.*;
import com.flowsphere.server.enums.ConsumerProviderStatusEnum;
import com.flowsphere.server.enums.PrividerInstanceStatusEnum;
import com.flowsphere.server.enums.ProviderStatusEnum;
import com.flowsphere.server.heartbeat.HeartbeatManager;
import com.flowsphere.server.repository.*;
import com.flowsphere.server.request.InstanceOfflineRequest;
import com.flowsphere.server.request.ProviderFunctionRequest;
import com.flowsphere.server.request.ProviderInstanceRequest;
import com.flowsphere.server.response.ProviderResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProviderService {

    @Autowired
    private ProviderRepository providerRepository;

    @Autowired
    private ProviderInstanceRepository providerInstantRepository;

    @Autowired
    private ProviderFunctionRepository providerFunctionRepository;

    @Autowired
    private ConsumerProviderRepository consumerProviderRepository;

    @Autowired
    private ConsumerRepository consumerRepository;

    @Autowired
    private HeartbeatManager heartbeatManager;

    @Autowired
    private CmdService cmdService;

    @Transactional
    public void offline(InstanceOfflineRequest request) {
        ProviderInstance providerInstance = providerInstantRepository.findByProviderNameAndIpAndPort(request.getApplicationName(), request.getIp(), request.getPort());
        if (Objects.isNull(providerInstance)) {
            return;
        }
        providerInstance.setStatus(PrividerInstanceStatusEnum.OFFLINE.getStatus());
        providerInstance.setLastUpdateTime(LocalDateTime.now());
        providerInstantRepository.save(providerInstance);
        List<Consumer> consumerList = consumerRepository.findByProviderName(request.getApplicationName());
        createCmd(consumerList, request.getApplicationName());
    }


    private void createCmd(List<Consumer> consumerList, String applicationName) {
        for (Consumer consumer : consumerList) {
            Page<ProviderInstance> providerInstantPage = findInstanceByProviderIdAndIp(consumer.getProviderName(), null, null, PageRequest.of(0, 50));
            cmdService.batchSave(providerInstantPage.getContent(), applicationName);
            for (int i = 1; i < providerInstantPage.getTotalPages(); i++) {
                providerInstantPage = findInstanceByProviderIdAndIp(consumer.getProviderName(), null, null, PageRequest.of(i, 50));
                cmdService.batchSave(providerInstantPage.getContent(), applicationName);
            }
        }
    }

    public void modifyProviderInstanceRemoval(String providerIp, int port, int status) {
        List<ConsumerProvider> consumerProviderList = consumerProviderRepository.findByProviderIpAndPort(providerIp, port);
        consumerProviderList.forEach(consumerProvider -> {
            consumerProvider.setStatus(status);
        });
        consumerProviderRepository.saveAll(consumerProviderList);
    }

    public void saveProvider(Provider provider) {
        providerRepository.save(provider);
    }

    @Transactional
    public void deleteProviderInstantByProviderNameAndServerNotIn(String providerName, List<String> serverList) {
        List<String> ipList = serverList.stream()
                .map(server -> server.split(":")[0])
                .collect(Collectors.toList());
        List<Integer> portList = serverList.stream()
                .map(server -> Integer.parseInt(server.split(":")[1]))
                .collect(Collectors.toList());
        providerInstantRepository.deleteByProviderNameAndIpNotInAndPortNotIn(providerName, ipList, portList);
        List<ConsumerProvider> consumerProviderList = consumerProviderRepository.findByProviderIpNotInAndPortNotIn(ipList, portList);
        consumerProviderList.forEach(consumerProvider -> {
            consumerProvider.setStatus(ConsumerProviderStatusEnum.DELETED.getStatus());//删除
        });
        consumerProviderRepository.saveAll(consumerProviderList);
    }

    @Transactional
    public void registerInstance(ProviderInstanceRequest request) {
        Provider provider = null;
        provider = providerRepository.findByName(request.getProviderName());
        if (Objects.isNull(provider)) {
            provider = providerRepository.save(new Provider()
                    .setName(request.getProviderName())
                    .setStatus(ProviderStatusEnum.NORMAL.getStatus()));
        } else if (provider.getStatus() == ProviderStatusEnum.OFFLINE.getStatus()) {
            provider.setStatus(ProviderStatusEnum.NORMAL.getStatus());
            providerRepository.save(provider);
        }
        ProviderInstance providerInstant = providerInstantRepository.findByProviderNameAndIpAndPort(request.getProviderName(), request.getIp(), request.getPort());
        if (Objects.isNull(providerInstant)) {
            providerInstant = new ProviderInstance()
                    .setProviderId(provider.getId())
                    .setStatus(PrividerInstanceStatusEnum.NORMAL.getStatus())
                    .setIp(request.getIp())
                    .setPort(request.getPort())
                    .setProviderName(request.getProviderName())
                    .setLastUpdateTime(LocalDateTime.now());
        }
        providerInstant.setLastUpdateTime(LocalDateTime.now());
        providerInstantRepository.save(providerInstant);
        List<ConsumerProvider> consumerProviderList = consumerProviderRepository.findByProviderIp(request.getIp());
        consumerProviderList.forEach(consumerProvider -> {
            consumerProvider.setStatus(ConsumerProviderStatusEnum.NORMAL.getStatus());//删除
        });
        consumerProviderRepository.saveAll(consumerProviderList);
        saveConsumerProvider(provider, providerInstant);
    }


    private void saveConsumerProvider(Provider provider, ProviderInstance providerInstant) {
        List<Consumer> consumerList = consumerRepository.findByProviderName(provider.getName());
        List<Integer> consumerIdList = consumerList.stream().map(Consumer::getId).collect(Collectors.toList());
        List<ConsumerProvider> consumerProviderList = consumerProviderRepository.findByConsumerIdInAndProviderIpAndPort(consumerIdList, providerInstant.getIp(), providerInstant.getPort());
        List<Integer> noneMatchConsumerIdList = consumerIdList.stream()
                .filter(id -> !consumerProviderList.stream()
                        .anyMatch(consumerProvider -> {
                            if (consumerProvider.getConsumerId() == id && consumerProvider.getProviderIp().equals(providerInstant.getIp())) {
                                return true;
                            }
                            return false;
                        }))
                .collect(Collectors.toList());
        List<ConsumerProvider> addConsumerProviderList = new ArrayList<>(noneMatchConsumerIdList.size());
        for (int consumerId : noneMatchConsumerIdList) {
            ConsumerProvider consumerProvider = new ConsumerProvider();
            consumerProvider.setConsumerId(consumerId);
            consumerProvider.setProviderId(provider.getId());
            consumerProvider.setProviderIp(providerInstant.getIp());
            consumerProvider.setPort(providerInstant.getPort());
            consumerProvider.setStatus(ConsumerProviderStatusEnum.NORMAL.getStatus());
            consumerProvider.setLastUpdateTime(LocalDateTime.now());
            addConsumerProviderList.add(consumerProvider);
        }
        if (CollectionUtils.isEmpty(addConsumerProviderList)) {
            return;
        }
        consumerProviderRepository.saveAll(addConsumerProviderList);
    }


    @Transactional
    public void registerInstanceFunction(List<ProviderFunctionRequest> requestList) {
        String providerName = requestList.get(0).getProviderName();
        Provider provider = providerRepository.findByName(providerName);
        List<String> urlList = requestList.stream().map(ProviderFunctionRequest::getUrl).collect(Collectors.toList());
        List<ProviderFunction> providerFunctionList = providerFunctionRepository.findByProviderNameAndUrlIn(providerName, urlList);
        requestList = filterProviderFunctionRequest(requestList, providerFunctionList);
        providerFunctionRepository.saveAll(convertProviderFunction(requestList, provider));
    }


    private List<ProviderFunction> convertProviderFunction(List<ProviderFunctionRequest> requestList, Provider provider) {
        LocalDateTime currentTime = LocalDateTime.now();
        List<ProviderFunction> providerFunctionList = new ArrayList<>();
        for (ProviderFunctionRequest request : requestList) {
            providerFunctionList.add(new ProviderFunction()
                    .setProviderId(provider.getId())
                    .setProviderName(provider.getName())
                    .setLastUpdateTime(currentTime)
                    .setUrl(request.getUrl()));
        }
        return providerFunctionList;
    }


    private List<ProviderFunctionRequest> filterProviderFunctionRequest(List<ProviderFunctionRequest> requestList, List<ProviderFunction> providerFunctionList) {
        if (CollectionUtils.isEmpty(providerFunctionList)) {
            return requestList;
        }
        return requestList.stream().filter(request -> {
            return !providerFunctionList.stream().anyMatch(providerFunction -> providerFunction.getUrl().equals(request.getUrl()));
        }).collect(Collectors.toList());
    }


    public Page<Provider> findProviderByNameAndStatus(String providerName, Integer status, Pageable pageable) {
        Specification<Provider> specification = new Specification<Provider>() {
            @Override
            public Predicate toPredicate(Root<Provider> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                if (!StringUtils.isEmpty(providerName)) {
                    Predicate predicate = criteriaBuilder.equal(root.get("name").as(String.class), providerName);
                    predicates.add(predicate);
                }
                if (Objects.nonNull(status)) {
                    Predicate predicate = criteriaBuilder.equal(root.get("status").as(Integer.class), status);
                    predicates.add(predicate);
                }
                if (predicates.size() == 0) {
                    return null;
                }
                Predicate[] p = new Predicate[predicates.size()];
                return criteriaBuilder.and(predicates.toArray(p));
            }
        };
        return providerRepository.findAll(specification, pageable);
    }

    public Page<ProviderResponse> findProviderResponseByNameAndStatus(String providerName, Integer status, Pageable pageable) {
        Page<Provider> pageProvider = findProviderByNameAndStatus(providerName, status, pageable);
        return converterPageProviderResponse(pageProvider, pageable);

    }


    private Page<ProviderResponse> converterPageProviderResponse(Page<Provider> page, Pageable pageable) {
        List<ProviderResponse> responseList = new ArrayList<>();
        for (Provider provider : page.getContent()) {
            ProviderResponse providerResponse = new ProviderResponse();
            providerResponse.setId(provider.getId());
            providerResponse.setName(provider.getName());
            providerResponse.setStatus(provider.getStatus());
            providerResponse.setOnlineNumber(heartbeatManager.countOnline(provider.getName()));
            responseList.add(providerResponse);
        }
        PageImpl<ProviderResponse> result = new PageImpl(responseList, pageable, page.getTotalElements());
        return result;
    }

    public Page<ProviderInstance> findInstanceByProviderIdAndIp(String providerName, Integer providerId, String ip, Pageable pageable) {
        Specification<ProviderInstance> specification = new Specification<ProviderInstance>() {
            @Override
            public Predicate toPredicate(Root<ProviderInstance> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                if (Objects.nonNull(providerId)) {
                    Predicate predicate = criteriaBuilder.equal(root.get("providerId").as(Integer.class), providerId);
                    predicates.add(predicate);
                }
                if (!StringUtils.isEmpty(ip)) {
                    Predicate predicate = criteriaBuilder.equal(root.get("ip").as(String.class), ip);
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
        return providerInstantRepository.findAll(specification, pageable);
    }

    public Page<ProviderFunction> findFunctionByProviderIdAndUrl(Integer providerId, String url, Pageable pageable) {
        Specification<ProviderFunction> specification = new Specification<ProviderFunction>() {
            @Override
            public Predicate toPredicate(Root<ProviderFunction> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                if (Objects.nonNull(providerId)) {
                    Predicate predicate = criteriaBuilder.equal(root.get("providerId").as(Integer.class), providerId);
                    predicates.add(predicate);
                }
                if (!StringUtils.isEmpty(url)) {
                    Predicate predicate = criteriaBuilder.equal(root.get("url").as(String.class), url);
                    predicates.add(predicate);
                }
                if (predicates.size() == 0) {
                    return null;
                }
                Predicate[] p = new Predicate[predicates.size()];
                return criteriaBuilder.and(predicates.toArray(p));
            }
        };
        return providerFunctionRepository.findAll(specification, pageable);
    }

}

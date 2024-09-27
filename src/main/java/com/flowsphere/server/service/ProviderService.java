package com.flowsphere.server.service;

import com.flowsphere.server.entity.Provider;
import com.flowsphere.server.entity.ProviderFunction;
import com.flowsphere.server.entity.ProviderInstant;
import com.flowsphere.server.repository.ProviderFunctionRepository;
import com.flowsphere.server.repository.ProviderInstantRepository;
import com.flowsphere.server.repository.ProviderRepository;
import com.flowsphere.server.request.ProviderFunctionRequest;
import com.flowsphere.server.request.ProviderInstantRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class ProviderService {

    @Autowired
    private ProviderRepository providerRepository;

    @Autowired
    private ProviderInstantRepository providerInstantRepository;

    @Autowired
    private ProviderFunctionRepository providerFunctionRepository;

    public void registerInstant(ProviderInstantRequest request) {
        Provider provider = null;
        provider = providerRepository.findByName(request.getProviderName());
        if (Objects.isNull(provider)) {
            provider = providerRepository.save(new Provider()
                    .setName(request.getProviderName())
                    .setStatus(1));
        }
        ProviderInstant providerInstant = providerInstantRepository.findByProviderNameAndIp(request.getProviderName(), request.getIp());
        if (Objects.isNull(providerInstant)) {
            providerInstantRepository.save(new ProviderInstant()
                    .setProviderId(provider.getId())
                    .setStatus(1)
                    .setIp(request.getIp())
                    .setProviderName(request.getProviderName())
                    .setLastUpdateTime(LocalDateTime.now()));
            return;
        }
        providerInstant.setLastUpdateTime(LocalDateTime.now());
        providerInstantRepository.save(providerInstant);
    }

    public void registerInstantFunction(ProviderFunctionRequest request) {
        Provider provider = providerRepository.findByName(request.getProviderName());
        ProviderFunction providerFunction = providerFunctionRepository.findByProviderNameAndUrlAndIp(request.getProviderName(),
                request.getUrl(), request.getIp());
        if (Objects.isNull(providerFunction)) {
            providerFunctionRepository.save(new ProviderFunction()
                    .setProviderId(provider.getId())
                    .setProviderName(request.getProviderName())
                    .setStatus(1)
                    .setIp(request.getIp())
                    .setUrl(request.getUrl()));
            return;
        }
        providerFunction.setLastUpdateTime(LocalDateTime.now());
        providerFunctionRepository.save(providerFunction);
    }


    public Page<Provider> findByName(String providerName, Pageable pageable) {
        Specification<Provider> specification = new Specification<Provider>() {
            @Override
            public Predicate toPredicate(Root<Provider> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                if (!StringUtils.isEmpty(providerName)) {
                    Predicate predicate = criteriaBuilder.equal(root.get("name").as(String.class), providerName);
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


    public Page<ProviderInstant> findInstantByProviderId(Integer providerId, Pageable pageable) {
        Specification<ProviderInstant> specification = new Specification<ProviderInstant>() {
            @Override
            public Predicate toPredicate(Root<ProviderInstant> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                if (Objects.nonNull(providerId)) {
                    Predicate predicate = criteriaBuilder.equal(root.get("providerId").as(Integer.class), providerId);
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

    public Page<ProviderFunction> findFunctionByProviderId(Integer providerId, Pageable pageable) {
        Specification<ProviderFunction> specification = new Specification<ProviderFunction>() {
            @Override
            public Predicate toPredicate(Root<ProviderFunction> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                if (Objects.nonNull(providerId)) {
                    Predicate predicate = criteriaBuilder.equal(root.get("providerId").as(Integer.class), providerId);
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

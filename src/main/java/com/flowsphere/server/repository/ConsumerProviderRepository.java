package com.flowsphere.server.repository;


import com.flowsphere.server.entity.ConsumerProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConsumerProviderRepository extends JpaRepository<ConsumerProvider, Integer>, JpaSpecificationExecutor<ConsumerProvider> {

    List<ConsumerProvider> findByConsumerIdInAndProviderIp(List<Integer> consumerIdList, String ip);

    List<ConsumerProvider> findByProviderIpAndStatus(String providerIp, int status);

    List<ConsumerProvider> findByProviderIpNotIn(List<String> providerIpList);

    List<ConsumerProvider> findByConsumerIdAndProviderId(Integer consumerId, Integer providerId);

}

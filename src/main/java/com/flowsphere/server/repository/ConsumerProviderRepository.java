package com.flowsphere.server.repository;


import com.flowsphere.server.entity.ConsumerProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConsumerProviderRepository extends JpaRepository<ConsumerProvider, Integer>, JpaSpecificationExecutor<ConsumerProvider> {

    List<ConsumerProvider> findByConsumerIdInAndProviderIpAndPort(List<Integer> consumerIdList, String ip, int port);

    List<ConsumerProvider> findByProviderIpAndPort(String providerIp, int port);

    List<ConsumerProvider> findByProviderIpNotInAndPortNotIn(List<String> providerIpList, List<Integer> portList);

    List<ConsumerProvider> findByProviderIp(String providerIp);

    List<ConsumerProvider> findByConsumerIdAndProviderId(Integer consumerId, Integer providerId);

}

package com.flowsphere.server.repository;

import com.flowsphere.server.entity.ProviderInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ProviderInstanceRepository extends JpaRepository<ProviderInstance, Integer>, JpaSpecificationExecutor<ProviderInstance> {

    ProviderInstance findByProviderNameAndIpAndPort(String providerName, String ip, int port);

    void deleteByProviderNameAndIpNotInAndPortNotIn(String providerName, List<String> ipList, List<Integer> portList);

    List<ProviderInstance> findByProviderId(int providerId);

    List<ProviderInstance> findByProviderNameInAndStatus(List<String> providerNameList, int status);

}

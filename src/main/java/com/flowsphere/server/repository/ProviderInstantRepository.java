package com.flowsphere.server.repository;

import com.flowsphere.server.entity.ProviderInstant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ProviderInstantRepository extends JpaRepository<ProviderInstant, Integer>, JpaSpecificationExecutor<ProviderInstant> {

    ProviderInstant findByProviderNameAndIp(String providerName, String ip);

    void deleteByProviderNameAndIpNotIn(String providerName, List<String> ipList);

}

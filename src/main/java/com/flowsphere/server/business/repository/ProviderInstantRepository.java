package com.flowsphere.server.business.repository;

import com.flowsphere.server.business.entity.ProviderInstant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProviderInstantRepository extends JpaRepository<ProviderInstant, Integer>, JpaSpecificationExecutor<ProviderInstant> {

    ProviderInstant findByProviderNameAndIp(String providerName, String ip);

}

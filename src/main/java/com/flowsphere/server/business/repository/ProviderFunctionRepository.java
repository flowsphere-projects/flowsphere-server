package com.flowsphere.server.business.repository;

import com.flowsphere.server.business.entity.ProviderFunction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProviderFunctionRepository extends JpaRepository<ProviderFunction, Integer>, JpaSpecificationExecutor<ProviderFunction> {

    ProviderFunction findByProviderNameAndUrlAndIp(String providerName, String url, String ip);

}

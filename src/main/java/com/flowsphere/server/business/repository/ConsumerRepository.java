package com.flowsphere.server.business.repository;

import com.flowsphere.server.business.entity.Consumer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ConsumerRepository extends JpaRepository<Consumer, Integer>, JpaSpecificationExecutor<Consumer> {

    Consumer findByNameAndProviderName(String name, String providerName);

}

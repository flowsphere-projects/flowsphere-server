package com.flowsphere.server.repository;

import com.flowsphere.server.entity.Consumer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ConsumerRepository extends JpaRepository<Consumer, Integer>, JpaSpecificationExecutor<Consumer> {

    Consumer findByNameAndProviderName(String name, String providerName);

}

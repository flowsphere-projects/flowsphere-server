package com.flowsphere.server.business.repository;

import com.flowsphere.server.business.entity.Consumer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsumerRepository extends JpaRepository<Consumer, Integer> {

    Consumer findByNameAndProviderName(String name, String providerName);

}

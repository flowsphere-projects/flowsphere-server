package com.flowsphere.server.business.repository;

import com.flowsphere.server.business.entity.ConsumerInstant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ConsumerInstantRepository extends JpaRepository<ConsumerInstant, Integer>, JpaSpecificationExecutor<ConsumerInstant> {

    List<ConsumerInstant> findByConsumerId(int consumerId);

}

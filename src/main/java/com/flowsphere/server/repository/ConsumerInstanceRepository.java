package com.flowsphere.server.repository;

import com.flowsphere.server.entity.ConsumerInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ConsumerInstanceRepository extends JpaRepository<ConsumerInstance, Integer>, JpaSpecificationExecutor<ConsumerInstance> {

    List<ConsumerInstance> findByConsumerId(int consumerId);

}

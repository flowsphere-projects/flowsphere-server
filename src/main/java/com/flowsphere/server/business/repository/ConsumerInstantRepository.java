package com.flowsphere.server.business.repository;

import com.flowsphere.server.business.entity.ConsumerInstant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConsumerInstantRepository extends JpaRepository<ConsumerInstant, Integer> {

    List<ConsumerInstant> findByConsumerId(int consumerId);

}

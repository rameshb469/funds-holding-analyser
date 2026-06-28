package com.rms.funds.holdings.analyser.repository;

import com.rms.funds.holdings.analyser.entity.OrderAgentPositionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderAgentPositionRepository extends JpaRepository<OrderAgentPositionEntity, Long> {

    List<OrderAgentPositionEntity> findByCycleId(Long cycleId);

    List<OrderAgentPositionEntity> findByStatusIn(List<String> statuses);

    long countByCycleId(Long cycleId);
}
